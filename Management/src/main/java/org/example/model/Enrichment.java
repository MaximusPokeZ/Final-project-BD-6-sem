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
@Table(name = "enrichment_rules")
public class Enrichment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Positive
    private long id;

    @NotNull(message = "enrichmentId cannot be null!")
    @Positive(message = "enrichmentId must be greater than 0!")
    private long enrichmentId;

    @NotNull(message = "Rule id cannot be null!")
    @Positive(message = "Rule id must be greater than 0!")
    private long ruleId;

    @NotNull(message = "FieldName cannot be null!")
    @Size(min = 1, message = "not empty must be")
    private String fieldName;

    @NotNull(message = "fieldNameEnrichment cannot be null!")
    @Size(min = 1, message = "fieldNameEnrichment not empty must be")
    private String fieldNameEnrichment;

    @NotNull(message = "fieldValue cannot be null!")
    @Size(min = 1, message = "fieldValue not empty must be")
    private String fieldValue;

    @NotNull(message = "fieldValueDefault cannot be null!")
    @Size(min = 1, message = "fieldValueDefault not empty must be")
    private String fieldValueDefault;
}
