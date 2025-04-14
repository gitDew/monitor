package com.gitDew.monitor;

public interface FinancialApi {

  Double getLastRsi(String ticker, Timespan timespan) throws ExternalApiException;

  boolean isSupportedSymbol(String symbol);
}
