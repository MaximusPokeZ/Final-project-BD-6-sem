package org.example.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {
    private String value;

    private boolean deduplicationState;

    public Message(String value, boolean filterState) {
        this.value = value;
        this.deduplicationState = filterState;
    }
}
