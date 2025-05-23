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
import org.example.dto.request.SifterRequest;
import org.example.dto.response.SifterResponse;

@Tag(
    name = "Контроллер просеивания",
    description = "Контроллер для операций по просеивания данных"
)
public interface SifterController {

  @Operation(
      summary = "Получить информацию о всех ситах в БД",
      description = "Получить информацию о всех ситах в БД",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Успешное получение информации",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(
                      schema = @Schema(implementation = SifterResponse.class)
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
  Iterable<SifterResponse> getAllFilters();

  @Operation(
      summary = "Получить информацию о всех ситах в БД по filter id",
      description = "Получить информацию о всех ситах в БД по filter id",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Успешное получение информации",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(
                      schema = @Schema(implementation = SifterResponse.class)
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
  Iterable<SifterResponse> getAllFiltersByFilterId(
      @PathVariable("id") long id);

  @Operation(
      summary = "Получить информацию о сите по filter id и rule id",
      description = "Получить информацию о сите по filter id и rule id",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Успешное получение информации",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = SifterResponse.class)
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
  @GetMapping("/find/{filterId}/{ruleId}")
  SifterResponse getFilterByFilterIdAndRuleId(
      @PathVariable("filterId") long filterId,
      @PathVariable("ruleId") long ruleId);

  @Operation(
      summary = "Удалить информацию о всех ситах",
      description = "Удалить информацию о всех ситах",
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
  void deleteFilter();

  @Operation(
      summary = "Удалить информацию по конкретному ситу filter id и rule id",
      description = "Удалить информацию по конкретному ситу filter id и rule id",
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
  @DeleteMapping("/delete/{filterId}/{ruleId}")
  void deleteFilterById(
      @PathVariable("filterId") long filterId,
      @PathVariable("ruleId") long ruleId);

  @Operation(
      summary = "Создать сито",
      description = "Создать сито",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Успешное создание сита"
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
      description = "Запрос на создание сита",
      required = true,
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = SifterRequest.class)
      )
  )
  @PostMapping("/save")
  void save(@RequestBody @Valid SifterRequest filter);
}
