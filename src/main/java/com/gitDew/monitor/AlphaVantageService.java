package com.gitDew.monitor;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.parameters.Interval;
import com.crazzyghost.alphavantage.parameters.SeriesType;
import com.crazzyghost.alphavantage.technicalindicator.response.rsi.RSIResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class AlphaVantageService implements FinancialApi {

  private final AlphaVantage api;

  public AlphaVantageService(@Value("${alphavantage.token}") String token) {
    Config cfg = Config.builder()
        .key(token)
        .timeOut(10)
        .build();
    api = AlphaVantage.api();
    api.init(cfg);
  }

  @Override
  public Double getLastRsi(String ticker, Timespan timespan) throws ExternalApiException {
    RSIResponse response = this.api.technicalIndicator()
        .rsi()
        .forSymbol(ticker)
        .timePeriod(14)
        .interval(toApiInterval(timespan))
        .seriesType(SeriesType.CLOSE)
        .fetchSync();

    if (response.getErrorMessage() != null) {
      throw new ExternalApiException(response.getErrorMessage());
    }
    return response.getIndicatorUnits().get(0).getValue();
  }

  private Interval toApiInterval(Timespan timespan) {
    return switch (timespan) {
      case MINUTE -> Interval.ONE_MIN;
      case MINUTE_5 -> Interval.FIVE_MIN;
      case MINUTE_15 -> Interval.FIFTEEN_MIN;
      case MINUTE_30 -> Interval.THIRTY_MIN;
      case HOUR -> Interval.SIXTY_MIN;
      case DAY -> Interval.DAILY;
      case WEEK -> Interval.WEEKLY;
      case MONTH -> Interval.MONTHLY;
    };
  }
}
