package org.example.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticRequest {
    private long id;
    private long serviceId;
    private String type;
    private String model;
    private int windowMinutes;
    private String defaultValue;
}
