package org.example.model;

import lombok.Data;

@Data
public class Rule {
    private Long filterId;
    private Long ruleId;
    private String fieldName;
    private String filterFunctionName;
    private String filterValue;
}
