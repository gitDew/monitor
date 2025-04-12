package com.gitDew.monitor;

import java.util.LinkedList;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertService {

  private static final int RSI_ALERT_MIN_THRESHOLD = 30;
  private static final int RSI_ALERT_MAX_THRESHOLD = 70;

  private final FinancialApi financialApi;
  private final Queue<Runnable> jobQueue = new LinkedList<>();

  @Scheduled(fixedDelay = 15000)
  public void runNextJob() {
    Runnable nextJob = jobQueue.poll();
    if (nextJob != null) {
      nextJob.run();
    }
  }

  public void subscribeRSI(DomainUser user, String ticker, Timespan timespan)
      throws ExternalApiException {
    // TODO retrieve available symbols once on startup and check if it exists?
    financialApi.getLastRsi(ticker, timespan);
    jobQueue.add(() -> checkRSI(user, ticker, timespan));
  }

  private void checkRSI(DomainUser user, String ticker, Timespan timespan) {
    Double lastRsi = financialApi.getLastRsi(ticker, timespan);

    if (Double.compare(lastRsi, RSI_ALERT_MIN_THRESHOLD) < 0 || Double.compare(lastRsi,
        RSI_ALERT_MAX_THRESHOLD) > 0) {
      user.sendResponse(
          String.format(
              "\uD83D\uDEA8 Alert triggered: RSI for %s %s is at %.2f. Alert subscription cleared.",
              ticker, timespan,
              lastRsi));
      return;
    }
    jobQueue.add(() -> checkRSI(user, ticker, timespan));
  }
}
