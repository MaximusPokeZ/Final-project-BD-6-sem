package org.example;

import org.example.models.Message;

public interface KafkaWriter {
    void processing(Message message);
    void close();
    Integer getInterval();
}
