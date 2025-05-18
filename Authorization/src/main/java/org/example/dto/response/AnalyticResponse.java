package org.example.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticResponse {
    private long id;
    private long serviceId;
    private String type;
    private String model;
    private int windowMinutes;
    private String defaultValue;
}
