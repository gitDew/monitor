package com.gitDew.monitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class DomainUser {

  private final long id;
  private final String name;
  private final ResponseType responseType;
}
