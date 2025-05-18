package org.example.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {
    private String value;

    private boolean filterState;

    public Message(String value, boolean filterState) {
        this.value = value;
        this.filterState = filterState;
    }
}
