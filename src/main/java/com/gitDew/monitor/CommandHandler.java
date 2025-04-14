package com.gitDew.monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandHandler {

  private static final String COMMAND_NOT_RECOGNIZED = "Command not recognized.\n\nPlease type <b>help</b> to see all available commands.";
  private final AlertService alertService;

  public String handle(String cmd, DomainUser user) {
    if (cmd == null || cmd.isBlank() || cmd.length() > 50) {
      return COMMAND_NOT_RECOGNIZED;
    }

    cmd = cmd.toLowerCase();

    String[] args = cmd.split(" ");

    Command command;
    try {
      command = Command.from(args[0]);
    } catch (IllegalArgumentException e) {
      return COMMAND_NOT_RECOGNIZED;
    }

    return switch (command.getCommandTag()) {
      case RSI -> handleRSI(command, args, user);
      case HELP -> command.helpMessage();
    };
  }

  private String handleRSI(Command cmd, String[] args, DomainUser user) {
    if (args.length != 3) {
      return cmd.helpMessage();
    }

    try {
      Timespan timespan = Timespan.fromCode(args[1]);
      String ticker = args[2].toUpperCase();

      return alertService.subscribeRSI(user, ticker, timespan);
    } catch (IllegalArgumentException e) {
      return cmd.helpMessage();
    } catch (ExternalApiException e) {
      log.error("Error trying to fetch the RSI data from the API: {}", e.getMessage());
      return "Something went wrong trying to fetch the RSI data. Sorry!";
    }
  }


}
