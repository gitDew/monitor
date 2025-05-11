package com.gitDew.monitor;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class DomainUser {

  @Id
  private Long id;

  private String name;

  @Enumerated(value = EnumType.STRING)
  private ResponseType responseType;

  public DomainUser(Long id, String name, ResponseType responseType) {
    this.id = id;
    this.name = name;
    this.responseType = responseType;
  }

}
