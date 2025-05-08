package com.gitDew.monitor;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {

  private final TelegramBot telegramBot;

  public ResponseService(@Lazy TelegramBot telegramBot) {
    this.telegramBot = telegramBot;
  }

  public void sendResponse(DomainUser user, String message) {
    switch (user.getResponseType()) {
      case TELEGRAM -> {
        telegramBot.sendResponse(user, message);
      }
    }
  }

}
