package com.gitDew.monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RequiredArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

  private final String telegramToken;
  private final CommandHandler commandHandler;


  @Override
  public String getBotUsername() {
    return "MONItor";
  }

  @Override
  public String getBotToken() {
    return telegramToken;
  }

  @Override
  public void onUpdateReceived(Update update) {
    User user = update.getMessage().getFrom();
    String text = update.getMessage().getText();

    log.info("Update received from {}: {}", user.getFirstName(), text);

    String response;
    if ("/start".equals(text)) {
      response = "Welcome to \uD83D\uDDA5\uFE0F<b>Moni</b>tor! Type <b>help</b> to get started.";
    } else {
      response = commandHandler.handle(text, toDomainUser(user));
    }

    sendResponse(user, response);
  }

  private DomainUser toDomainUser(User user) {
    return new DomainUser(
        user.getId(),
        user.getFirstName(),
        (msg) -> sendResponse(user, msg)
    );
  }

  private void sendResponse(User user, String response) {
    log.info("Sending message to {}: {}", user.getFirstName(), response);
    SendMessage msg = SendMessage.builder()
        .chatId(String.valueOf(user.getId()))
        .text(response)
        .parseMode("HTML")
        .build();

    try {
      execute(msg);
    } catch (TelegramApiException e) {
      log.error("Error while sending response message: ", e);
    }
  }
}
