package com.gitDew.monitor;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(cascade = CascadeType.PERSIST)
  private DomainUser user;

  @Enumerated(EnumType.STRING)
  private TaskType taskType;

  private String ticker;

  @Enumerated(EnumType.STRING)
  private Timespan timespan;

  public Task(DomainUser user, TaskType taskType, String ticker, Timespan timespan) {
    this.user = user;
    this.taskType = taskType;
    this.ticker = ticker;
    this.timespan = timespan;
  }

}
