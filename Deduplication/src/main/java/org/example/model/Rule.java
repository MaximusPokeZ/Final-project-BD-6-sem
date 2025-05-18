package org.example.model;

import lombok.Data;

@Data
public class Rule {
    private Long deduplicationId;
    private Long ruleId;
    private String fieldName;
    private Long timeToLiveSec;
    private Boolean isActive;
}
