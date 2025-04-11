package com.gitDew.monitor;

import io.polygon.kotlin.sdk.rest.BaseTechnicalIndicatorsParametersBuilder;
import io.polygon.kotlin.sdk.rest.PolygonRestClient;
import io.polygon.kotlin.sdk.rest.RSIParameters;
import io.polygon.kotlin.sdk.rest.RSIParametersBuilder;
import io.polygon.kotlin.sdk.rest.TechnicalIndicatorsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PolygonService {

  private final PolygonRestClient client;

  public PolygonService(@Value("${polygon.token}") String polygonKey) {
    this.client = new PolygonRestClient(polygonKey);
  }

  public Double getLastRsi(String ticker, int window, Timespan timespan) {
    RSIParameters params = new RSIParametersBuilder()
        .baseParameters(
            new BaseTechnicalIndicatorsParametersBuilder()
                .limit(1)
                .timespan(timespan.toString())
                .build()
        )
        .windowSize(window)
        .build();
    TechnicalIndicatorsResponse resp = client.getTechnicalIndicatorRSIBlocking(
        ticker, params);
    if (resp.getResults() != null && resp.getResults().getValues() != null) {
      return resp.getResults().getValues().get(0).getValue();
    }
    throw new RuntimeException("Couldn't retrieve last RSI value.");
  }
}
