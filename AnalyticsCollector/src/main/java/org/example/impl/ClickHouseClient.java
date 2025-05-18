//package org.example.impl;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.typesafe.config.Config;
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import lombok.extern.slf4j.Slf4j;
//import org.example.model.Message;
//import org.example.model.Rule;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import java.sql.Timestamp;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.time.temporal.ChronoUnit;
//
//@Slf4j
//public class ClickHouseClient {
//
//  private final ObjectMapper objectMapper = new ObjectMapper();
//  private final JdbcTemplate jdbcTemplate;
//
//  private static final DateTimeFormatter CH_FORMATTER =
//          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
//
//  public ClickHouseClient(Config config) {
//    HikariConfig hikariConfig = new HikariConfig();
//    hikariConfig.setJdbcUrl(config.getString("clickhouse.jdbcUrl"));
//    hikariConfig.setUsername(config.getString("clickhouse.user"));
//    hikariConfig.setPassword(config.getString("clickhouse.password"));
//    hikariConfig.setDriverClassName(config.getString("clickhouse.driver"));
//
//    HikariDataSource dataSource = new HikariDataSource(hikariConfig);
//    this.jdbcTemplate = new JdbcTemplate(dataSource);
//  }
//
//  public Message analysis(Rule rule, Message message) {
//    try {
//      JsonNode msg = objectMapper.readTree(message.getValue());
//      long timestamp = msg.get("timestamp").asLong();
//      Instant now = Instant.ofEpochMilli(timestamp);
//      Instant from = now.minus(rule.getWindowMinutes(), ChronoUnit.MINUTES);
//
//      return switch (rule.getType()) {
//        case COUNT_ACTIVE_BY_MODEL -> {
//          int count = countByState(rule.getModel(), "active", from, now);
//          yield wrapResult("COUNT_ACTIVE_BY_MODEL", String.valueOf(count));
//        }
//        case COUNT_INACTIVE_BY_MODEL -> {
//          int count = countByState(rule.getModel(), "inactive", from, now);
//          yield wrapResult("COUNT_INACTIVE_BY_MODEL", String.valueOf(count));
//        }
//        case MODELS_MORE_INACTIVE -> {
//          String fromStr = CH_FORMATTER.format(from);
//          String toStr = CH_FORMATTER.format(now);
//          var result = jdbcTemplate.query("""
//                    SELECT model FROM (
//                      SELECT model,
//                             countIf(state = 'inactive') AS inact,
//                             countIf(state = 'active') AS act
//                      FROM analytic
//                      WHERE dataTime BETWEEN ? AND ?
//                      GROUP BY model
//                    ) WHERE inact > act
//                    """,
//                  (rs, rowNum) -> rs.getString("model"), fromStr, toStr);
//          yield wrapResult("MODELS_MORE_INACTIVE", objectMapper.writeValueAsString(result));
//        }
//        case MODELS_ALL_INACTIVE -> {
//          String fromStr = CH_FORMATTER.format(from);
//          String toStr = CH_FORMATTER.format(now);
//          var result = jdbcTemplate.query("""
//                    SELECT model FROM (
//                      SELECT model, groupUniqArray(state) AS states
//                      FROM analytic
//                      WHERE dataTime BETWEEN ? AND ?
//                      GROUP BY model
//                    ) WHERE hasAll(states, ['inactive']) AND length(states) = 1
//                    """,
//                  (rs, rowNum) -> rs.getString("model"), fromStr, toStr);
//          yield wrapResult("MODELS_ALL_INACTIVE", objectMapper.writeValueAsString(result));
//        }
//      };
//
//    } catch (Exception e) {
//      log.error("Analysis failed", e);
//      return wrapResult("error", "\"" + rule.getDefaultValue() + "\"");
//    }
//  }
//
//  private int countByState(String model, String state, Instant from, Instant to) {
//    String fromStr = CH_FORMATTER.format(from);
//    String toStr = CH_FORMATTER.format(to);
//
//    return jdbcTemplate.queryForObject("""
//        SELECT count() FROM analytic
//        WHERE model = ? AND state = ? AND dataTime BETWEEN ? AND ?
//        """, Integer.class, model, state, fromStr, toStr);
//  }
//
//  private Message wrapResult(String field, String value) {
//    return new Message("{\"" + field + "\": " + value + "}");
//  }
//
////  public void insertRaw(Message message) throws JsonProcessingException {
//////    JsonNode jsonNode = objectMapper.readTree(message.getValue());
//////
//////    String model = jsonNode.get("model").asText();
//////    String state = jsonNode.get("state").asText();
//////
//////    long timestamp;
//////    timestamp = jsonNode.get("timestamp").asLong();
//////
//////    String formattedTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//////            .format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("UTC")));
//////
//////    String sql = "INSERT INTO analytic (model, state, dataTime) VALUES (?, ?, ?)";
//////
//////    log.info("Inserting into ClickHouse: {}, {}, {}", model, state, formattedTime);
//////
//////    jdbcTemplate.update(sql, model, state, formattedTime);
////
////    JsonNode jsonNode = objectMapper.readTree(message.getValue());
////
////    String model = jsonNode.get("model").asText();
////    JsonNode stateNode = jsonNode.get("state");
////
////    String state = stateNode.isNull() ? null : stateNode.asText(); // обрабатываем null
////
////    String timestampStr = jsonNode.get("timestamp").asText(); // ← строка вида "2025-05-16 14:34:27"
////
////    // Преобразуем строку в java.sql.Timestamp
////    Timestamp timestamp = Timestamp.valueOf(timestampStr); // формат "yyyy-MM-dd HH:mm:ss"
////
////    // Порядок должен соответствовать порядку в CREATE TABLE
////    String sql = "INSERT INTO analytic (dataTime, model, state) VALUES (?, ?, ?)";
////
////    log.info("Inserting into ClickHouse: {}, {}, {}", model, state, timestamp);
////
////    jdbcTemplate.update(sql, timestamp, model, state);
////  }
//
//
//}
//

package org.example.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Message;
import org.example.model.Rule;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Slf4j
public class ClickHouseClient {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final JdbcTemplate jdbcTemplate;

  private static final DateTimeFormatter CH_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

  public ClickHouseClient(Config config) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(config.getString("clickhouse.jdbcUrl"));
    hikariConfig.setUsername(config.getString("clickhouse.user"));
    hikariConfig.setPassword(config.getString("clickhouse.password"));
    hikariConfig.setDriverClassName(config.getString("clickhouse.driver"));

    HikariDataSource dataSource = new HikariDataSource(hikariConfig);
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public Message analysis(Rule rule, Message message) {
    try {
      JsonNode msg = objectMapper.readTree(message.getValue());
      String timestampStr = msg.get("timestamp").asText();
      Instant now;
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

      LocalDateTime localTime = LocalDateTime.parse(timestampStr, formatter);
      now = localTime.atZone(ZoneId.of("UTC")).toInstant();
      Instant from = now.minus(rule.getWindowMinutes(), ChronoUnit.MINUTES);

      return switch (rule.getType()) {
        case COUNT_ACTIVE_BY_MODEL -> {
          int count = countByState(rule.getModel(), "active", from, now);
          yield wrapResult("COUNT_ACTIVE_BY_MODEL", String.valueOf(count));
        }
        case COUNT_INACTIVE_BY_MODEL -> {
          int count = countByState(rule.getModel(), "non-active", from, now);
          yield wrapResult("COUNT_INACTIVE_BY_MODEL", String.valueOf(count));
        }
        case MODELS_MORE_INACTIVE -> {
          String fromStr = CH_FORMATTER.format(from);
          String toStr = CH_FORMATTER.format(now);
          var result = jdbcTemplate.query("""
                    SELECT model FROM (
                      SELECT model,
                             countIf(state = 'non-active') AS inact,
                             countIf(state = 'active') AS act
                      FROM analytic
                      WHERE dataTime BETWEEN ? AND ?
                      GROUP BY model
                    ) WHERE inact > act
                    """,
                  (rs, rowNum) -> rs.getString("model"), fromStr, toStr);
          yield wrapResult("MODELS_MORE_INACTIVE", objectMapper.writeValueAsString(result));
        }
        case MODELS_ALL_INACTIVE -> {
          String fromStr = CH_FORMATTER.format(from);
          String toStr = CH_FORMATTER.format(now);
          var result = jdbcTemplate.query("""
                    SELECT model FROM (
                      SELECT model, groupUniqArray(state) AS states
                      FROM analytic
                      WHERE dataTime BETWEEN ? AND ?
                      GROUP BY model
                    ) WHERE hasAll(states, ['non-active']) AND length(states) = 1
                    """,
                  (rs, rowNum) -> rs.getString("model"), fromStr, toStr);
          yield wrapResult("MODELS_ALL_INACTIVE", objectMapper.writeValueAsString(result));
        }
      };

    } catch (Exception e) {
      log.error("Analysis failed", e);
      return wrapResult("error", "\"" + rule.getDefaultValue() + "\"");
    }
  }
  private int countByState(String model, String state, Instant from, Instant to) {
    String fromStr = CH_FORMATTER.format(from);
    String toStr = CH_FORMATTER.format(to);

    return jdbcTemplate.queryForObject("""
        SELECT count() FROM analytic
        WHERE model = ? AND state = ? AND dataTime BETWEEN ? AND ?
        """, Integer.class, model, state, fromStr, toStr);
  }

  private Message wrapResult(String field, String value) {
    return new Message("{\"" + field + "\": " + value + "}");
  }

  public void insertRaw(Message message) throws JsonProcessingException {

//    JsonNode jsonNode = objectMapper.readTree(message.getValue());
//
//    String model = jsonNode.get("model").asText();
//    JsonNode stateNode = jsonNode.get("state");
//    String state = (stateNode == null || stateNode.isNull()) ? null : stateNode.asText();
//
//    String timestampStr = jsonNode.get("timestamp").asText();
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    LocalDateTime localTime = LocalDateTime.parse(timestampStr, formatter);
//    String formattedTime = localTime.toString().replace('T', ' ');
//
//    StringBuilder sql = new StringBuilder();
//    sql.append("INSERT INTO analytic (model, state, dataTime) VALUES (");
//    sql.append("'").append(model).append("', ");
//    if (state == null) {
//      sql.append("null");
//    } else {
//      sql.append("'").append(state).append("'");
//    }
//    sql.append(", '").append(formattedTime).append("')");
//
//    log.info("Final SQL: {}", sql);
//    jdbcTemplate.execute(sql.toString());

    JsonNode jsonNode = objectMapper.readTree(message.getValue());

    String model = jsonNode.get("model").asText();
    JsonNode stateNode = jsonNode.get("state");
    String state = stateNode.isNull() ? null : stateNode.asText();

    String timestampStr = jsonNode.get("timestamp").asText();

    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm")
            .optionalStart()
            .appendPattern(":ss")
            .optionalEnd()
            .toFormatter();

    LocalDateTime localTime;
    try {
      localTime = LocalDateTime.parse(timestampStr, formatter);
    } catch (DateTimeParseException e) {
      log.error("Ошибка парсинга времени: {}", timestampStr, e);
      throw new IllegalArgumentException("Некорректный формат времени: " + timestampStr);
    }

    String formattedTime = localTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    String sql = "INSERT INTO analytic (model, state, dataTime) VALUES (?, ?, ?)";
    log.info("Final SQL: INSERT INTO analytic (model, state, dataTime) VALUES ({}, {}, {})", model, state, formattedTime);

    jdbcTemplate.update(sql, model, state, formattedTime);
  }
}
