package com.gitDew.monitor;

import org.springframework.stereotype.Service;

@Service
public class CommandHandler {

  public String handle(String cmd) {
    if (cmd == null || cmd.isBlank()) {
      return "Command not recognized.";
    }

    cmd = cmd.toLowerCase();

    String[] args = cmd.split(" ");

    return switch (args[0]) {
      case "rsi" -> handleRsi(args);
      default -> "Command not recognized.";
    };
  }

  private String handleRsi(String[] args) {
    if (args.length < 3) {
      return "Usage: rsi <timeframe> <symbol>";
    }

    String timeframe = args[1];
    String symbol = args[2];

    return String.format("Calculating RSI for %s on timeframe %s", symbol, timeframe);
  }

}
