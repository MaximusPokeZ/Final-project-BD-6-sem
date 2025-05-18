package org.example;

import org.example.model.Message;

public interface KafkaWriter {
    public void processing(Message message);
    void close();
}
