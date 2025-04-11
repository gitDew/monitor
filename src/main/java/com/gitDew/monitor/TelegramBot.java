package com.gitDew.monitor;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final String telegramToken;
    private final PolygonService polygonService;


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

        logger.info("Update received from {}: {}", user.getFirstName(), update);

        SendMessage msg = SendMessage.builder()
                .chatId(String.valueOf(user.getId()))
                .text(update.getMessage().getText().toUpperCase())
                .build();

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error("Error while sending response message: {}", e);
        }
    }
}
