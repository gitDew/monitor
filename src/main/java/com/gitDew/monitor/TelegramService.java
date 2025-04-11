package com.gitDew.monitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Service
public class TelegramService {

  private final CommandHandler commandHandler;
  private final String telegramToken;

  public TelegramService(@Value("${telegram.token}") String telegramToken,
      CommandHandler commandHandler) {
    this.commandHandler = commandHandler;
    this.telegramToken = telegramToken;
  }

  public void init() throws TelegramApiException {
    TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
    botsApi.registerBot(new TelegramBot(telegramToken, commandHandler));
  }

}
