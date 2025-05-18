package org.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.example.dto.request.PaddingRequest;
import org.example.dto.response.PaddingResponse;

@Tag(
    name = "Контроллер набивки",
    description = "Контроллер для операций по обогащению данных"
)
public interface PaddingController {

  @Operation(
      summary = "Получить информацию о всех правилах набивки в БД",
      description = "Получить информацию о всех правилах набивки в БД",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Успешное получение информации",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(
                      schema = @Schema(implementation = PaddingResponse.class)
                  )
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Неверный формат запроса",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BadRequest.class)
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Ошибка на стороне сервиса",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = InternalServerError.class)
              )
          )
      }
  )
  @GetMapping("/findAll")
  Iterable<PaddingResponse> getAllEnrichmentRequests();

  @Operation(
      summary = "Получить информацию о всех правилах набивки в БД по enrichment id",
      description = "Получить информацию о всех правилах набивки в БД по enrichment id",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Успешное получение информации",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(
                      schema = @Schema(implementation = PaddingResponse.class)
                  )
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Неверный формат запроса",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BadRequest.class)
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Ошибка на стороне сервиса",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = InternalServerError.class)
              )
          )
      }
  )
  @GetMapping("/findAll/{id}")
  Iterable<PaddingResponse> getAllEnrichmentRequestsByEnrichmentRequestId(
      @PathVariable("id") long id);

  @Operation(
      summary = "Получить информацию о правиле набивки по enrichment id и rule id",
      description = "Получить информацию о правиле набивки по enrichment id и rule id",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Успешное получение информации",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PaddingResponse.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Неверный формат запроса",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BadRequest.class)
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Ошибка на стороне сервиса",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = InternalServerError.class)
              )
          )
      }
  )
  @GetMapping("/find/{enrichmentId}/{ruleId}")
  PaddingResponse getEnrichmentRequestById(
      @PathVariable("enrichmentId") long enrichmentId,
      @PathVariable("ruleId") long ruleId);

  @Operation(
      summary = "Удалить информацию о всех правилах набивки",
      description = "Удалить информацию о всех правилах набивки",
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
                  schema = @Schema(implementation = BadRequest.class)
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Ошибка на стороне сервиса",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = InternalServerError.class)
              )
          )
      }
  )
  @DeleteMapping("/delete")
  void deleteEnrichmentRequest();

  @Operation(
      summary = "Удалить информацию по конкретному правилу набивки с enrichment id и rule id",
      description = "Удалить информацию по конкретному правилу набивки с enrichment id и rule id",
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
                  schema = @Schema(implementation = BadRequest.class)
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Ошибка на стороне сервиса",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = InternalServerError.class)
              )
          )
      }
  )
  @DeleteMapping("/delete/{enrichmentId}/{ruleId}")
  void deleteEnrichmentRequestById(
      @PathVariable("enrichmentId") long enrichmentId,
      @PathVariable("ruleId") long ruleId);

  @Operation(
      summary = "Создать правило набивки",
      description = "Создать правило набивки",
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
                  schema = @Schema(implementation = BadRequest.class)
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Ошибка на стороне сервиса",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = InternalServerError.class)
              )
          )
      }
  )
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Запрос на создание правила набивки",
      required = true,
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = PaddingRequest.class)
      )
  )
  @PostMapping("/save")
  void save(@RequestBody @Valid PaddingRequest enrichment);
}
