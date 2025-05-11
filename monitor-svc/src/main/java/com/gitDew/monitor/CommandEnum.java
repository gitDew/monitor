package com.gitDew.monitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommandEnum {
  HELP("help", "Displays this help message."),
  RSI("rsi", "Subscribe to alerts for a stock's Relative Strength Index (RSI)"),
  ALERTS("alerts", "List all currently subscribed alerts.");

  private final String displayText;
  private final String description;
}
