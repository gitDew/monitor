package com.gitDew.monitor;

import jakarta.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

  private static final int RSI_ALERT_MIN_THRESHOLD = 30;
  private static final int RSI_ALERT_MAX_THRESHOLD = 70;
  private final ResponseService responseService;
  private final FinancialApi financialApi;
  private final TaskRepository taskRepository;

  private final Queue<Task> taskQueue = new LinkedList<>();

  @Scheduled(fixedRate = 10000)
  public void runMainJobQueue() {
    Task task = taskQueue.poll();
    if (task != null) {
      runTask(task);
    }
  }

  private void runTask(Task task) {
    switch (task.getTaskType()) {
      case RSI -> checkRSI(task);
    }
  }

  private void checkRSI(Task task) {
    Double lastRsi;
    try {
      lastRsi = financialApi.getLastRsi(task.getTicker(), task.getTimespan());
    } catch (ExternalApiException e) {
      log.error("Couldn't fetch the last RSI from the external API:", e);
      taskRepository.delete(task);
      responseService.sendResponse(task.getUser(), String.format(
          "Sorry, something went wrong when trying to fetch the RSI for your subscribed alert for %s %s. The subscription has been cleared.",
          task.getTicker(), task.getTimespan()));
      return;
    }

    if (Double.compare(lastRsi, RSI_ALERT_MIN_THRESHOLD) < 0 || Double.compare(lastRsi,
        RSI_ALERT_MAX_THRESHOLD) > 0) {
      taskRepository.delete(task);
      responseService.sendResponse(task.getUser(),
          String.format(
              "@%s \uD83D\uDEA8 <b>Alert triggered</b>: RSI for <code>%s %s</code> is at %.2f.\n\nAlert subscription cleared.",
              task.getUser().getName(), task.getTicker(), task.getTimespan(),
              lastRsi));
      return;
    }

    log.info("RSI checked on behalf of {} for {} {}: {}. Re-adding to queue.",
        task.getUser().getName(),
        task.getTicker(), task.getTimespan(), lastRsi);
  }

  @Scheduled(cron = "0 * * * * *")
  public void runEveryMinute() {
    taskQueue.addAll(taskRepository.findAllByTimespan(Timespan.MINUTE));
  }


  @Scheduled(cron = "0 */5 * * * *")
  public void runEvery5Minutes() {
    taskQueue.addAll(taskRepository.findAllByTimespan(Timespan.MINUTE_5));
  }

  @Scheduled(cron = "0 */15 * * * *")
  public void runEvery15Minutes() {
    taskQueue.addAll(taskRepository.findAllByTimespan(Timespan.MINUTE_15));
  }

  @Scheduled(cron = "0 */30 * * * *")
  public void runEvery30Minutes() {
    taskQueue.addAll(taskRepository.findAllByTimespan(Timespan.MINUTE_30));
  }

  @Scheduled(cron = "0 0 * * * *")
  public void runEveryHour() {
    taskQueue.addAll(taskRepository.findAllByTimespan(Timespan.HOUR));
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void runEveryDay() {
    taskQueue.addAll(taskRepository.findAllByTimespan(Timespan.DAY));
  }

  @Scheduled(cron = "0 0 0 * * MON")
  public void runEveryWeek() {
    taskQueue.addAll(taskRepository.findAllByTimespan(Timespan.WEEK));
  }

  @Scheduled(cron = "0 0 0 1 * *")
  public void runEveryMonth() {
    taskQueue.addAll(taskRepository.findAllByTimespan(Timespan.MONTH));
  }

  public String subscribeRSI(DomainUser user, String ticker, Timespan timespan)
      throws ExternalApiException {
    if (!financialApi.isSupportedSymbol(ticker)) {
      return String.format("Sorry, symbol %s is not supported.", ticker);
    }

    taskRepository.save(new Task(user, TaskType.RSI, ticker, timespan));

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
