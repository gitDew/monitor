package com.gitDew.monitor;

import io.polygon.kotlin.sdk.rest.PolygonRestClient;
import io.polygon.kotlin.sdk.rest.reference.MarketsDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PolygonService {

    @Value("${polygon.token}")
    String polygonKey;

    public void getMarkets() {
        if (polygonKey == null || polygonKey.isEmpty()) {
            System.err.println("Make sure you set your polygon API key in the POLYGON_API_KEY environment variable!");
            System.exit(1);
        }

        PolygonRestClient client = new PolygonRestClient(polygonKey);

        System.out.println("Blocking for markets...");
        final MarketsDTO markets = client.getReferenceClient().getSupportedMarketsBlocking();
        System.out.println("Got markets synchronously: " + markets.toString());    }

}
