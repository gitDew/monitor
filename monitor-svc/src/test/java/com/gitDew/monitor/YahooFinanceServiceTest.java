package com.gitDew.monitor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {YahooFinanceService.class, TestConfig.class})
class YahooFinanceServiceTest {

  @Autowired
  private YahooFinanceService objUnderTest;

  @Test
  void isSupportedSymbol() {
    assertTrue(objUnderTest.isSupportedSymbol("AAPL"));
    assertFalse(objUnderTest.isSupportedSymbol("NOTREAL"));
  }

  @Test
  void getLastRsi() {
    for (Timespan timespan : Timespan.values()) {
      assertThat(objUnderTest.getLastRsi("AAPL", timespan)).isBetween(1d, 100d);
    }
  }
}