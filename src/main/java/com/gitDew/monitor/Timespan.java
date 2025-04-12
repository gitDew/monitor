package com.gitDew.monitor;

import java.util.Arrays;

public enum Timespan {
  MINUTE("1m"),
  MINUTE_5("5m"),
  MINUTE_15("15m"),
  MINUTE_30("30m"),
  HOUR("1h"),
  DAY("1d"),
  WEEK("week"),
  MONTH("month");

  private final String code;

  Timespan(String code) {
    this.code = code;
  }

  public static Timespan fromCode(String code) {
    return Arrays.stream(values())
        .filter(t -> t.code.equalsIgnoreCase(code)).findFirst().orElseThrow(
            () -> new IllegalArgumentException("Couldn't parse timespan code.")
        );
  }

  @Override
  public String toString() {
    return this.code;
  }
}
