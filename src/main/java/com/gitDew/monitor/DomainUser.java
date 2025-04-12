package com.gitDew.monitor;

import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class DomainUser {

  private final long id;
  private final String name;
  private final Consumer<String> responseCallback;

  public void sendResponse(String message) {
    this.responseCallback.accept(message);
  }
}
