package com.gitDew.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonitorApplication implements CommandLineRunner {

  private final TelegramService telegramService;

  @Autowired
  public MonitorApplication(TelegramService telegramService) {
    this.telegramService = telegramService;
  }


  public static void main(String[] args) {
    SpringApplication.run(MonitorApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    telegramService.init();
  }
}
