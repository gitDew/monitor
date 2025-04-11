package com.gitDew.monitor;

public enum Timespan {
  MINUTE,
  HOUR,
  DAY,
  WEEK,
  MONTH,
  QUARTER,
  YEAR;

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}
