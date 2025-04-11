package com.gitDew.monitor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CommandHandlerTest {

  private final CommandHandler objUnderTest = new CommandHandler();

  @Test
  void emptyCommand() {
    assertThat(objUnderTest.handle(null)).isEqualTo("Command not recognized.");
    assertThat(objUnderTest.handle("")).isEqualTo("Command not recognized.");
    assertThat(objUnderTest.handle("        ")).isEqualTo("Command not recognized.");
  }

  @Test
  void rsi() {
    assertThat(objUnderTest.handle("rsi")).isEqualTo("Usage: rsi <timeframe> <symbol>");
    assertThat(objUnderTest.handle("rsi 15m GOOG")).isEqualTo(
        "Calculating RSI for goog on timeframe 15m");
    assertThat(objUnderTest.handle("RSI 15m GOOG")).isEqualTo(
        "Calculating RSI for goog on timeframe 15m");
  }
}