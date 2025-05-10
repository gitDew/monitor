package com.gitDew.monitor;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

  @Bean
  RestTemplateBuilder restTemplateBuilder() {
    return new RestTemplateBuilder();
  }

}
