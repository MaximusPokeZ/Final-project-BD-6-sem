package org.example.kafka;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Kafka API", description = "API для работы с Kafka: получение сообщений")
public interface KafkaApi {

    @Operation(
            summary = "Получить последние сообщения",
            description = "Возвращает 10 последних полученных сообщений из Kafka",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список сообщений",
                            content = @Content(
                                    mediaType = "application/json"
                            )
                    )
            }
    )
    @GetMapping("/messages")
    ResponseEntity<List<String>> getMessages();
}