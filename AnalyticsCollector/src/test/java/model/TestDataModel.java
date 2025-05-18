package model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestDataModel {
    private String model;
    private String state;
    private String timestamp;
    private double x1;
    private double x2;
}
