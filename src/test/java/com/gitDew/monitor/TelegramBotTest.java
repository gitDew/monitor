package com.gitDew.monitor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TelegramBotTest {

  private final TelegramBot objUnderTest = new TelegramBot("testToken", null);

  @Test
  void getBotUsername() {
    assertThat(objUnderTest.getBotUsername()).isEqualTo("MONItor");
  }

}