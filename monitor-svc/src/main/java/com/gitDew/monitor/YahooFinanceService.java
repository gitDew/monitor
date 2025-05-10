package com.gitDew.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@Primary
public class YahooFinanceService implements FinancialApi {

  private final RestTemplate restTemplate;
  private final String hostUrl;

  public YahooFinanceService(RestTemplateBuilder restTemplateBuilder,
      @Value("${YFINANCE_SVC_URL:http://localhost:5000/}") String hostUrl) {
    this.restTemplate = restTemplateBuilder.build();
    this.hostUrl = hostUrl;
  }


  @Override
  public Double getLastRsi(String symbol, Timespan timespan) throws ExternalApiException {
    String rsiUrl = UriComponentsBuilder.fromUriString(this.hostUrl + "rsi")
        .queryParam("symbol", symbol)
        .queryParam("interval", toApiInterval(timespan))
        .toUriString();

      RSIResponse response = restTemplate.getForObject(rsiUrl, RSIResponse.class);
      return response.rsi();
  }

  private String toApiInterval(Timespan timespan) {
    return switch (timespan) {
      case WEEK -> "1wk";
      case MONTH -> "1mo";
      default -> timespan.toString();
    };
  }

  @Override
  public boolean isSupportedSymbol(String symbol) {
    String rsiUrl = UriComponentsBuilder.fromUriString(this.hostUrl + "rsi")
        .queryParam("symbol", symbol).toUriString();

    try {
      ResponseEntity<RSIResponse> entity = restTemplate.getForEntity(rsiUrl, RSIResponse.class);
      return entity.getStatusCode().is2xxSuccessful();
    } catch (HttpClientErrorException | HttpServerErrorException e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  private record RSIResponse(double rsi, String symbol) {}
}
