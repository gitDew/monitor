package com.gitDew.monitor;

public abstract class Command {

  public static Command from(String code) throws IllegalArgumentException {
    CommandEnum commandEnum = CommandEnum.valueOf(code.toUpperCase());

    return switch (commandEnum) {
      case HELP -> new HelpCommand();
      case RSI -> new RsiCommand();
      case ALERTS -> new AlertsCommand();
    };
  }

  public abstract CommandEnum getCommandTag();

  public abstract String helpMessage();

  public static class HelpCommand extends Command {

    @Override
    public CommandEnum getCommandTag() {
      return CommandEnum.HELP;
    }

    @Override
    public String helpMessage() {
      StringBuilder sb = new StringBuilder();

      sb.append("All available commands: \n\n");

      for (CommandEnum commandEnum : CommandEnum.values()) {
        sb.append(String.format("<b>%s</b> - %s\n", commandEnum.getDisplayText(),
            commandEnum.getDescription()));
      }
      return sb.toString();
    }
  }

  private static class RsiCommand extends Command {

    @Override
    public CommandEnum getCommandTag() {
      return CommandEnum.RSI;
    }

    @Override
    public String helpMessage() {
      StringBuilder sb = new StringBuilder();

      sb.append("Usage: <code>rsi &lt;timespan&gt; &lt;ticker&gt;</code>\n\n");
      sb.append(String.format("%s\n\n", getCommandTag().getDescription()));

      sb.append("Required arguments:\n");
      sb.append(String.format(
          "  <code>&lt;timespan&gt;</code> - Time frame to use (available options: <code>%s</code>)\n",
          String.join(", ", Timespan.allCodes())));
      sb.append(
          "  <code>&lt;ticker&gt;</code> - Stock ticker symbol (e.g. <code>AAPL, TSLA</code>)\n\n");

      sb.append("Example:\n");
      sb.append("   <code>rsi 15m AAPL</code>\n");

      return sb.toString();
    }
  }


  private static class AlertsCommand extends Command {

    @Override
    public CommandEnum getCommandTag() {
      return CommandEnum.ALERTS;
    }

    @Override
    public String helpMessage() {
      return "Lists all currently subscribed to alerts.";
    }
  }
}
