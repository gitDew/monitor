package com.gitDew.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

  public static final String FILENAME = "tasks.json";
  private static final int RSI_ALERT_MIN_THRESHOLD = 30;
  private static final int RSI_ALERT_MAX_THRESHOLD = 70;
  private final ResponseService responseService;
  private final FinancialApi financialApi;
  private final ObjectMapper objectMapper;


  private final Queue<Task> mainQueue = new LinkedList<>();
  private final Queue<Task> minuteQueue = new LinkedList<>();
  private final Queue<Task> fiveMinuteQueue = new LinkedList<>();
  private final Queue<Task> fifteenMinuteQueue = new LinkedList<>();
  private final Queue<Task> thirtyMinuteQueue = new LinkedList<>();
  private final Queue<Task> hourlyQueue = new LinkedList<>();
  private final Queue<Task> dailyQueue = new LinkedList<>();
  private final Queue<Task> weeklyQueue = new LinkedList<>();
  private final Queue<Task> monthlyQueue = new LinkedList<>();


  @Scheduled(fixedRate = 10000)
  public void runMainJobQueue() {
    Task task = mainQueue.poll();
    if (task != null) {
      runTask(task);
    }
  }

  private void runTask(Task task) {
    switch (task.taskType()) {
      case RSI -> checkRSI(task.user(), task.params().get("ticker"),
          Timespan.fromCode(task.params().get("timespan")));
    }
  }

  private void checkRSI(DomainUser user, String ticker, Timespan timespan) {
    Double lastRsi;
    try {
      lastRsi = financialApi.getLastRsi(ticker, timespan);
    } catch (ExternalApiException e) {
      log.error("Couldn't fetch the last RSI from the external API:", e);
      responseService.sendResponse(user, String.format(
          "Sorry, something went wrong when trying to fetch the RSI for your subscribed alert for %s %s. The subscription has been cleared.",
          ticker, timespan));
      return;
    }

    if (Double.compare(lastRsi, RSI_ALERT_MIN_THRESHOLD) < 0 || Double.compare(lastRsi,
        RSI_ALERT_MAX_THRESHOLD) > 0) {
      responseService.sendResponse(user,
          String.format(
              "@%s \uD83D\uDEA8 <b>Alert triggered</b>: RSI for <code>%s %s</code> is at %.2f.\n\nAlert subscription cleared.",
              user.getName(), ticker, timespan,
              lastRsi));
      return;
    }

    log.info("RSI checked on behalf of {} for {} {}: {}. Re-adding to queue.", user.getName(),
        ticker, timespan, lastRsi);
    getTimespanQueue(timespan).add(
        new Task(user, TaskType.RSI, Map.of("ticker", ticker, "timespan", timespan.toString())));
  }

  private Queue<Task> getTimespanQueue(Timespan timespan) {
    return switch (timespan) {
      case MINUTE -> minuteQueue;
      case MINUTE_5 -> fiveMinuteQueue;
      case MINUTE_15 -> fifteenMinuteQueue;
      case MINUTE_30 -> thirtyMinuteQueue;
      case HOUR -> hourlyQueue;
      case DAY -> dailyQueue;
      case WEEK -> weeklyQueue;
      case MONTH -> monthlyQueue;
    };
  }

  @Scheduled(cron = "0 * * * * *")
  public void runEveryMinute() {
    emptyIntoMainJobQueue(minuteQueue);
  }

  private void emptyIntoMainJobQueue(Queue<Task> q) {
    while (!q.isEmpty()) {
      mainQueue.add(q.poll());
    }
  }

  @PostConstruct
  public void loadFromJson() throws IOException {
    File file = new File(FILENAME);

    if (file.exists()) {
      List<Task> tasks = objectMapper.readValue(file,
          objectMapper.getTypeFactory().constructCollectionType(
              List.class, Task.class));
      mainQueue.addAll(tasks);
      log.info("Loaded {} tasks from {}", tasks.size(), FILENAME);
    }
  }

  @PreDestroy
  public void saveToJson() throws IOException {
    for (Timespan timespan : Timespan.values()) {
      emptyIntoMainJobQueue(getTimespanQueue(timespan));
    }

    File file = new File(FILENAME);

    objectMapper.writeValue(file, mainQueue);
    log.info("Tasks saved to {}", FILENAME);
  }

  @Scheduled(cron = "0 */5 * * * *")
  public void runEvery5Minutes() {
    emptyIntoMainJobQueue(fiveMinuteQueue);
  }

  @Scheduled(cron = "0 */15 * * * *")
  public void runEvery15Minutes() {
    emptyIntoMainJobQueue(fifteenMinuteQueue);
  }

  @Scheduled(cron = "0 */30 * * * *")
  public void runEvery30Minutes() {
    emptyIntoMainJobQueue(thirtyMinuteQueue);
  }

  @Scheduled(cron = "0 0 * * * *")
  public void runEveryHour() {
    emptyIntoMainJobQueue(hourlyQueue);
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void runEveryDay() {
    emptyIntoMainJobQueue(dailyQueue);
  }

  @Scheduled(cron = "0 0 0 * * MON")
  public void runEveryWeek() {
    emptyIntoMainJobQueue(weeklyQueue);
  }

  @Scheduled(cron = "0 0 0 1 * *")
  public void runEveryMonth() {
    emptyIntoMainJobQueue(monthlyQueue);
  }

  public String subscribeRSI(DomainUser user, String ticker, Timespan timespan)
      throws ExternalApiException {
    if (!financialApi.isSupportedSymbol(ticker)) {
      return String.format("Sorry, symbol %s is not supported.", ticker);
    }

    getTimespanQueue(timespan).add(
        new Task(user, TaskType.RSI, Map.of("ticker", ticker, "timespan", timespan.toString())));

    log.info("Successfully subscribed {} to RSI for {} {}", user.getName(), ticker, timespan);
    return String.format(
        "Successfully subscribed to RSI alerts for <code>%s %s</code>. You will receive an alert once the RSI goes above %d or below %d.",
        ticker,
        timespan,
        RSI_ALERT_MAX_THRESHOLD,
        RSI_ALERT_MIN_THRESHOLD
    );

  }
}
