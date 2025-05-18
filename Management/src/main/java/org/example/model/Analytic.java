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
@Table(name = "analytics_rules")
public class Analytic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Positive
    private long id;

    @NotNull(message = "serviceId cannot be null!")
    @Positive(message = "serviceId must be greater than 0!")
    private long serviceId;

    @NotNull(message = "type cannot be null!")
    @Size(min = 1, message = "type not empty must be")
    private String type;

    @NotNull(message = "model cannot be null!")
    @Size(min = 1, message = "model not empty must be")
    private String model;

    @NotNull(message = "windowMinutes cannot be null!")
    @Positive(message = "windowMinutes not empty must be")
    private int windowMinutes;

    @NotNull(message = "defaultValue cannot be null!")
    @Size(min = 1, message = "defaultValue not empty must be")
    private String defaultValue;
}
