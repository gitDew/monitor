package com.gitDew.monitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommandEnum {
  HELP("help", "Displays this help message."),
  RSI("rsi", "Subscribe to alerts for a stock's Relative Strength Index (RSI)");

  private final String displayText;
  private final String description;
}
