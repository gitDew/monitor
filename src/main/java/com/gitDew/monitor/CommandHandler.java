package com.gitDew.monitor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandHandler {

  private static final String COMMAND_NOT_RECOGNIZED = "Command not recognized.";
  private static final String RSI_USAGE_HELP = "Usage: rsi <timespan> <symbol>, e.g. rsi 15m GOOG";
  private final AlertService alertService;

  public String handle(String cmd, DomainUser user) {
    if (cmd == null || cmd.isBlank()) {
      return COMMAND_NOT_RECOGNIZED;
    }

    cmd = cmd.toLowerCase();

    String[] args = cmd.split(" ");

    return switch (args[0]) {
      case "rsi" -> handleRsi(args, user);
      default -> COMMAND_NOT_RECOGNIZED;
    };
  }

  private String handleRsi(String[] args, DomainUser user) {
    if (args.length != 3) {
      return RSI_USAGE_HELP;
    }

    try {
      Timespan timespan = Timespan.fromCode(args[1]);
      String ticker = args[2].toUpperCase();

      alertService.subscribeRSI(user, ticker, timespan);
      return String.format("Successfully subscribed to RSI for %s %s", ticker, timespan);

    } catch (IllegalArgumentException e) {
      return RSI_USAGE_HELP;
    } catch (ExternalApiException e) {
      return String.format("Error when trying to fetch RSI data: %s", e);
    }
  }

}
