package com.gitDew.monitor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@Primary
public class TwelveDataService implements FinancialApi {

  private static final String BASE_URL = "https://api.twelvedata.com/";
  private final RestTemplate restTemplate;
  private final Set<String> supportedSymbols = new HashSet<>();
  private final String token;

  public TwelveDataService(@Value("${twelvedata.token}") String token) {
    this.token = token;
    restTemplate = new RestTemplate();
  }

  @PostConstruct
  public void init() {
    String stocksUrl = BASE_URL + "stocks";

    StocksDTO response = restTemplate.getForObject(stocksUrl, StocksDTO.class);

    if (response != null && response.data() != null) {
      supportedSymbols.addAll(
          response.data()
              .stream()
              .map(StockDTO::symbol)
              .map(String::toUpperCase)
              .collect(Collectors.toSet())
      );
      log.info("Initialized symbol set with {} entries.", supportedSymbols.size());
      return;
    }
    log.error("Couldn't initialize supported symbols set.");
  }

  @Override
  public Double getLastRsi(String ticker, Timespan timespan) throws ExternalApiException {
    String rsiUrl = BASE_URL + "rsi";

    String apiUrl = UriComponentsBuilder.fromUriString(rsiUrl)
        .queryParam("symbol", ticker)
        .queryParam("interval", toApiInterval(timespan))
        .queryParam("apikey", this.token)
        .queryParam("outputsize", 1)
        .toUriString();

    RSIResponse response = restTemplate.getForObject(apiUrl, RSIResponse.class);
    if (response != null && response.values() != null && !response.values().isEmpty()) {
      return response.values().get(0).getRsi();
    }
    throw new ExternalApiException(
        "Couldn't retrieve the last RSI value from the TwelveData response.");
  }

  private String toApiInterval(Timespan timespan) {
    return switch (timespan) {
      case MINUTE -> "1min";
      case MINUTE_5 -> "5min";
      case MINUTE_15 -> "15min";
      case MINUTE_30 -> "30min";
      case HOUR -> "1h";
      case DAY -> "1day";
      case WEEK -> "1week";
      case MONTH -> "1month";
    };
  }

  @Override
  public boolean isSupportedSymbol(String symbol) {
    return supportedSymbols.contains(symbol);
  }

  public record StocksDTO(List<StockDTO> data) {

  }

  public record StockDTO(@JsonProperty("symbol") String symbol) {

  }

  public record RSIResponse(
      List<RSIValue> values
  ) {

  }

  public record RSIValue(
      @JsonProperty("rsi") String rsiString
  ) {

    @JsonSetter("rsi")
    public double getRsi() {
      try {
        return Double.parseDouble(rsiString);
      } catch (NumberFormatException e) {
        throw new ExternalApiException("Couldn't parse RSI Value string to double.");
      }
    }
  }

}
