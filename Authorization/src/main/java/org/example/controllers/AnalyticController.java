package org.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.request.AnalyticRequest;
import org.example.dto.response.AnalyticResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Tag(
        name = "Контроллер аналитики",
        description = "Контроллер для операций по анализу данных"
)
public interface AnalyticController {
    @Operation(
            summary = "Получить информацию о всех правилах аналитики в БД",
            description = "Получить информацию о всех правилах аналитики в БД",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное получение информации",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = AnalyticResponse.class)
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверный формат запроса",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HttpClientErrorException.BadRequest.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ошибка на стороне сервиса",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HttpServerErrorException.InternalServerError.class)
                            )
                    )
            }
    )
    @GetMapping("/findAll")
    Iterable<AnalyticResponse> getAllAnalyticRequests();

    @Operation(
            summary = "Получить информацию о всех правилах аналитики в БД по service id",
            description = "Получить информацию о всех правилах аналитики в БД по service id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное получение информации",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = AnalyticResponse.class)
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверный формат запроса",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HttpClientErrorException.BadRequest.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ошибка на стороне сервиса",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HttpServerErrorException.InternalServerError.class)
                            )
                    )
            }
    )
    @GetMapping("/findAll/{id}")
    Iterable<AnalyticResponse> getAllAnalyticRequestsByServiceId(
            @PathVariable("id") long id);


    @Operation(
            summary = "Удалить информацию о всех правилах аналитики",
            description = "Удалить информацию о всех правилах аналитики",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное удаление записей"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверный формат запроса",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HttpClientErrorException.BadRequest.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ошибка на стороне сервиса",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HttpServerErrorException.InternalServerError.class)
                            )
                    )
            }
    )
    @DeleteMapping("/delete")
    void deleteAnalyticRequest();


    @Operation(
            summary = "Создать правило аналитики",
            description = "Создать правило аналитики",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешное создание правила"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверный формат запроса",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HttpClientErrorException.BadRequest.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ошибка на стороне сервиса",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HttpServerErrorException.InternalServerError.class)
                            )
                    )
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Запрос на создание правила аналитики",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AnalyticRequest.class)
            )
    )
    @PostMapping("/save")
    void save(@RequestBody @Valid AnalyticRequest analyticRequest);
}
