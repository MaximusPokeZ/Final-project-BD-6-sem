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
@Table(name = "deduplication_rules")
public class Deduplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Positive
    private long id;

    @NotNull(message = "deduplicationId cannot be null!")
    @Positive(message = "deduplicationId must be greater than 0!")
    private long deduplicationId;

    @NotNull(message = "ruleId cannot be null!")
    @Positive(message = "ruleId must be greater than 0!")
    private long ruleId;

    @NotNull(message = "FieldName cannot be null!")
    @Size(min = 1, message = "not empty must be")
    private String fieldName;

    @NotNull(message = "timeToLiveSec cannot be null!")
    @Positive(message = "timeToLiveSec must be greater than 0!")
    private long timeToLiveSec;

    @NotNull
    private boolean isActive;
}
