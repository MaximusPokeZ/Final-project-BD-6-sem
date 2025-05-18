package model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestDataSpecific {
    private String model;
    private Double coordX1;
    private Double coordX2;
    private String state;
}
