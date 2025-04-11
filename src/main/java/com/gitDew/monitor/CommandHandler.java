package com.gitDew.monitor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandHandler {

  private static final String COMMAND_NOT_RECOGNIZED = "Command not recognized.";
  private static final String RSI_USAGE_HELP = "Usage: rsi <window> <timespan> <symbol>, e.g. rsi 15 minute GOOG";
  private final PolygonService polygonService;

  public String handle(String cmd) {
    if (cmd == null || cmd.isBlank()) {
      return "Command not recognized.";
    }

    cmd = cmd.toLowerCase();

    String[] args = cmd.split(" ");

    return switch (args[0]) {
      case "rsi" -> handleRsi(args);
      default -> COMMAND_NOT_RECOGNIZED;
    };
  }

  private String handleRsi(String[] args) {
    if (args.length < 4) {
      return RSI_USAGE_HELP;
    }

    int window;
    Timespan timespan;
    try {
      window = Integer.parseInt(args[1]);
      timespan = Timespan.valueOf(args[2].toUpperCase());
    } catch (IllegalArgumentException e) {
      return RSI_USAGE_HELP;
    }

    String ticker = args[3].toUpperCase();

    Double lastRsi = polygonService.getLastRsi(ticker, window, timespan);

    return String.format("Last RSI for %s on timeframe %d %s: %.2f", ticker, window, timespan,
        lastRsi);
  }

}
