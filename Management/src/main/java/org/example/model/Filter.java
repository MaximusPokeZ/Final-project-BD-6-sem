package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "filter_rules")
public class Filter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Positive
    private long id;

    @NotNull(message = "Filter id cannot be null!")
    @Positive(message = "Filter id must be greater than 0!")
    private long filterId;

    @NotNull(message = "Rule id cannot be null!")
    @Positive(message = "Rule id must be greater than 0!")
    private long ruleId;

    @NotNull(message = "FieldName cannot be null!")
    @Size(min = 1, message = "not empty must be")
    private String fieldName;

    @NotNull(message = "filterFunctionName cannot be null!")
    @Size(min = 1, message = "not empty must be")
    private String filterFunctionName;

    @NotNull(message = "filterValue cannot be null!")
    @Size(min = 1, message = "not empty must be")
    private String filterValue;
}
