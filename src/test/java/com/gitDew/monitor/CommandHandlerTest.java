package com.gitDew.monitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandHandlerTest {

  @Mock
  private PolygonService polygonService;

  @InjectMocks
  private CommandHandler objUnderTest;

  @Test
  void emptyCommand() {
    assertThat(objUnderTest.handle(null)).isEqualTo("Command not recognized.");
    assertThat(objUnderTest.handle("")).isEqualTo("Command not recognized.");
    assertThat(objUnderTest.handle("        ")).isEqualTo("Command not recognized.");
  }

  @Test
  void rsi() {
    when(polygonService.getLastRsi(anyString(), anyInt(), any())).thenReturn(12.34);
    assertThat(objUnderTest.handle("rsi")).isEqualTo(
        "Usage: rsi <window> <timespan> <symbol>, e.g. rsi 15 minute GOOG");
    assertThat(objUnderTest.handle("rsi 15 minute GOOG")).isEqualTo(
        "Last RSI for GOOG on timeframe 15 minute: 12.34");
    assertThat(objUnderTest.handle("RSI 15 minute GOOG")).isEqualTo(
        "Last RSI for GOOG on timeframe 15 minute: 12.34");
  }
}