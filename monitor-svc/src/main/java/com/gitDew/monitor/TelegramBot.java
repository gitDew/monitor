package com.gitDew.monitor;

import jakarta.annotation.PostConstruct;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

  private final String telegramToken;
  private final CommandHandler commandHandler;
  private final DomainUserRepository domainUserRepository;

  public TelegramBot(@Value("${telegram.token}") String telegramToken,
      CommandHandler commandHandler, DomainUserRepository domainUserRepository) {
    this.commandHandler = commandHandler;
    this.telegramToken = telegramToken;
    this.domainUserRepository = domainUserRepository;
  }

  @PostConstruct
  public void init() throws TelegramApiException {
    TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
    botsApi.registerBot(this);
  }

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
    DomainUser user = toDomainUser(update.getMessage().getFrom());
    String text = update.getMessage().getText();

    log.info("Update received from {}: {}", user.getName(), text);

    String response;
    if ("/start".equals(text)) {
      response = "Welcome to \uD83D\uDDA5\uFE0F<b>Moni</b>tor! Type <b>help</b> to get started.";
    } else {
      response = commandHandler.handle(text, user);
    }

    sendResponse(user, response);
  }

  private DomainUser toDomainUser(User user) {
    return domainUserRepository.findById(user.getId()).orElse(domainUserRepository.save(
        new DomainUser(
            user.getId(),
            user.getFirstName(),
            ResponseType.TELEGRAM
        )
    ));
  }

  public void sendResponse(DomainUser user, String response) {
    log.info("Sending message to {}: {}", user.getName(), response);
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
