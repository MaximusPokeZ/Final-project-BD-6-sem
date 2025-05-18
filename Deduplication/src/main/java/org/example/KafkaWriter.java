package org.example;

import org.example.model.Message;

public interface KafkaWriter {
    void processing(Message message); // отправляет сообщения с deduplicationState = true в выходной топик. Конфигурация берется из файла *.conf
}
