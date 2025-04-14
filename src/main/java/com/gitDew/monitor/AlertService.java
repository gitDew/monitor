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
  private final Queue<Runnable> minuteQueue = new LinkedList<>();
  private final Queue<Runnable> fiveMinuteQueue = new LinkedList<>();
  private final Queue<Runnable> fifteenMinuteQueue = new LinkedList<>();
  private final Queue<Runnable> thirtyMinuteQueue = new LinkedList<>();
  private final Queue<Runnable> hourlyQueue = new LinkedList<>();
  private final Queue<Runnable> dailyQueue = new LinkedList<>();
  private final Queue<Runnable> weeklyQueue = new LinkedList<>();
  private final Queue<Runnable> monthlyQueue = new LinkedList<>();


  @Scheduled(cron = "11 * * * * *")
  public void runEveryMinute() {
    runNextJob(minuteQueue);
  }

  private void runNextJob(Queue<Runnable> jobQueue) {
    Runnable job = jobQueue.poll();
    if (job != null) {
      job.run();
    }
  }

  @Scheduled(cron = "10 */5 * * * *")
  public void runEvery5Minutes() {
    runNextJob(fiveMinuteQueue);
  }

  @Scheduled(cron = "9 */15 * * * *")
  public void runEvery15Minutes() {
    runNextJob(fifteenMinuteQueue);
  }

  @Scheduled(cron = "8 */30 * * * *")
  public void runEvery30Minutes() {
    runNextJob(thirtyMinuteQueue);
  }

  @Scheduled(cron = "7 0 * * * *")
  public void runEveryHour() {
    runNextJob(hourlyQueue);
  }

  @Scheduled(cron = "6 0 0 * * *")
  public void runEveryDay() {
    runNextJob(dailyQueue);
  }

  @Scheduled(cron = "4 0 0 * * MON")
  public void runEveryWeek() {
    runNextJob(weeklyQueue);
  }

  @Scheduled(cron = "3 0 0 1 * *")
  public void runEveryMonth() {
    runNextJob(monthlyQueue);
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
