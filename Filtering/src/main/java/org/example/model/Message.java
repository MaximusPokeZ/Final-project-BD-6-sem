package org.example.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {
    private String value; // сообщение из Kafka в формате JSON

    private boolean filterState; // true - удовлетворены условиях всех правил (Rule), false - хотя бы одно условие не прошло проверку.

    public Message(String value, boolean filterState) {
        this.value = value;
        this.filterState = filterState;
    }
}
