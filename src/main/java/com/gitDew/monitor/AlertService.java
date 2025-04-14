package com.gitDew.monitor;

import java.util.LinkedList;
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

  private final FinancialApi financialApi;
  private final Queue<Runnable> mainQueue = new LinkedList<>();
  private final Queue<Runnable> minuteQueue = new LinkedList<>();
  private final Queue<Runnable> fiveMinuteQueue = new LinkedList<>();
  private final Queue<Runnable> fifteenMinuteQueue = new LinkedList<>();
  private final Queue<Runnable> thirtyMinuteQueue = new LinkedList<>();
  private final Queue<Runnable> hourlyQueue = new LinkedList<>();
  private final Queue<Runnable> dailyQueue = new LinkedList<>();
  private final Queue<Runnable> weeklyQueue = new LinkedList<>();
  private final Queue<Runnable> monthlyQueue = new LinkedList<>();


  @Scheduled(fixedRate = 10000)
  public void runMainJobQueue() {
    Runnable job = mainQueue.poll();
    if (job != null) {
      job.run();
    }
  }

  @Scheduled(cron = "0 * * * * *")
  public void runEveryMinute() {
    emptyIntoMainJobQueue(minuteQueue);
  }

  private void emptyIntoMainJobQueue(Queue<Runnable> q) {
    while (!q.isEmpty()) {
      mainQueue.add(q.poll());
    }
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

    getTimespanQueue(timespan).add(() -> checkRSI(user, ticker, timespan));

    log.info("Successfully subscribed {} to RSI for {} {}", user.getName(), ticker, timespan);
    return String.format(
        "Successfully subscribed to RSI alerts for <code>%s %s</code>. You will receive an alert once the RSI goes above %d or below %d.",
        ticker,
        timespan,
        RSI_ALERT_MAX_THRESHOLD,
        RSI_ALERT_MIN_THRESHOLD
    );

  }

  private Queue<Runnable> getTimespanQueue(Timespan timespan) {
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

  private void checkRSI(DomainUser user, String ticker, Timespan timespan) {
    Double lastRsi;
    try {
      lastRsi = financialApi.getLastRsi(ticker, timespan);
    } catch (ExternalApiException e) {
      log.error(e.getMessage());
      user.sendResponse(String.format(
          "Sorry, something went wrong when trying to fetch the RSI for your subscribed alert for %s %s. The subscription has been cleared.",
          ticker, timespan));
      return;
    }

    if (Double.compare(lastRsi, RSI_ALERT_MIN_THRESHOLD) < 0 || Double.compare(lastRsi,
        RSI_ALERT_MAX_THRESHOLD) > 0) {
      user.sendResponse(
          String.format(
              "@%s \uD83D\uDEA8 <b>Alert triggered</b>: RSI for <code>%s %s</code> is at %.2f.\n\nAlert subscription cleared.",
              user.getName(), ticker, timespan,
              lastRsi));
      return;
    }

    log.info("RSI checked on behalf of {} for {} {}: {}. Re-adding to queue.", user.getName(),
        ticker, timespan, lastRsi);
    getTimespanQueue(timespan).add(() -> checkRSI(user, ticker, timespan));
  }
}
