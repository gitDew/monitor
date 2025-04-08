package com.gitDew.monitor;

import io.polygon.kotlin.sdk.rest.PolygonRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.polygon.kotlin.sdk.rest.reference.*;

@Service
public class PolygonService {

    @Value("${polygon.token}")
    String polygonKey;

    public void getMarkets() {
        // TODO move key to properties file
        // String polygonKey = System.getenv("POLYGON_API_KEY");
        if (polygonKey == null || polygonKey.isEmpty()) {
            System.err.println("Make sure you set your polygon API key in the POLYGON_API_KEY environment variable!");
            System.exit(1);
        }

        PolygonRestClient client = new PolygonRestClient(polygonKey);

        System.out.println("Blocking for markets...");
        final MarketsDTO markets = client.getReferenceClient().getSupportedMarketsBlocking();
        System.out.println("Got markets synchronously: " + markets.toString());    }

}
