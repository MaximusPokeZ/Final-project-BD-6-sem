package org.example.model;

import lombok.Data;


@Data
public class Rule {
  private Integer serviceId;
  private RuleType type;
  private String model;
  private int windowMinutes;
  private String defaultValue;
}
