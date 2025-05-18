package org.example.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
@Tag(name = "Kafka API", description = "API для работы с Kafka: получение сообщений")
public class KafkaController implements KafkaApi {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final List<String> receivedMessages = new CopyOnWriteArrayList<>();

    @Value("${kafka.producer.default-topic}")
    private String defaultTopic;

    @Override
    public ResponseEntity<List<String>> getMessages() {
        return ResponseEntity.ok(receivedMessages.stream()
                .limit(10)
                .toList());
    }

    @KafkaListener(topics = "${kafka.consumer.topics}", groupId = "group_consumer")
    public void listen(String message) {
        receivedMessages.add(0, message);
    }
}