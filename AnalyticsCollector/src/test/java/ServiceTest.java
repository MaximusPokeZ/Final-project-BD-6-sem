//import com.typesafe.config.Config;
//import com.typesafe.config.ConfigFactory;
//import com.typesafe.config.ConfigValueFactory;
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.admin.Admin;
//import org.apache.kafka.clients.admin.AdminClient;
//import org.apache.kafka.clients.admin.AdminClientConfig;
//import org.apache.kafka.clients.admin.NewTopic;
//import org.apache.kafka.clients.consumer.*;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.Producer;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.example.model.RuleType;
//import org.jooq.DSLContext;
//import org.jooq.SQLDialect;
//import org.jooq.impl.DSL;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.testcontainers.clickhouse.ClickHouseContainer;
//import org.testcontainers.containers.JdbcDatabaseContainer;
//import org.testcontainers.containers.KafkaContainer;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
//import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
//import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
//import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
//import org.testcontainers.utility.DockerImageName;
//import model.TestDataModel;
//import org.example.Service;
//import org.example.impl.ServiceAnalytics;
//
//import javax.sql.DataSource;
//import java.io.IOException;
//import java.sql.SQLException;
//import java.time.Duration;
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.stream.Stream;
//
//import static org.jooq.impl.DSL.field;
//import static org.jooq.impl.DSL.table;
//import static org.junit.jupiter.api.Assertions.*;
//
//@Slf4j
//@Testcontainers
//class ServiceTest {
//
//    @Container
//    private final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"));
//
//    @Container
//    private final JdbcDatabaseContainer<?> postgreSQL = new PostgreSQLContainer(DockerImageName.parse("postgres"))
//            .withDatabaseName("test_db")
//            .withUsername("user")
//            .withPassword("password")
//            .withInitScript("init_script.sql");
//
//    @Container
//    private final ClickHouseContainer clickHouse = new ClickHouseContainer(
//            DockerImageName.parse("clickhouse/clickhouse-server:23.3"))
//                .withInitScript("init_clickhouse.sql");
//
//    ExecutorService executorForTest = Executors.newFixedThreadPool(2);
//
//    private DataSource dataSource;
//    private static final String TEST_TOPIC_IN = "test_topic_in";
//    private static final String TEST_TOPIC_OUT = "test_topic_out";
//    private final short replicaFactor = 1;
//    private final int partitions = 3;
//
//    private final String tableName = "analytics_rules";
//
//    private final Service serviceAnalytics = new ServiceAnalytics();
//
//    private Consumer<String, String> consumer;
//    private Producer<String, String> producer;
//    private Admin adminClient;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    private static final List<String> LIST_DEFAULT_DB = List.of("admin", "config", "local");
//
//    private static final Integer UPDATE_INTERVAL_POSTGRESQL_RULE_SECS = 10;
//
//    private static final Long ENRICHMENT_ID = 1L;
//
//    @BeforeEach
//    void initClientsAndTestEnvironment() {
//        dataSource = Optional.ofNullable(dataSource).orElse(createConnectionPool());
//        adminClient = createAdminClient();
//        consumer = createConsumer();
//        producer = createProducer();
//        List<NewTopic> topics = Stream.of(TEST_TOPIC_IN, TEST_TOPIC_OUT)
//                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
//                .toList();
//
//        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);
//
//        checkAndCreateRequiredTopics(adminClient, topics);
//
//        consumer.subscribe(Collections.singletonList(TEST_TOPIC_OUT));
//
//    }
//
//    @AfterEach
//    void clearDataAndCloseClients() {
//        clearTable();
//        List.of(producer, consumer, adminClient).forEach(client -> {
//            Optional.ofNullable(client).ifPresent(c -> {
//                try {
//                    c.close();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        });
//    }
//
//    /**
//     * Проверяет готовность Kafka
//     */
//    @Test
//    void testStartKafka() {
//        assertTrue(kafka.isRunning());
//    }
//
//    /**
//     * Проверяет готовность postgreSQL
//     */
//    @Test
//    void testStartPostgreSQL() {
//        assertTrue(postgreSQL.isRunning());
//    }
//
//
//    @Test
//    void testСlickHouse() {
//        assertTrue(clickHouse.isRunning());
//    }
//
//    /**
//     * Проверяет возможность читать и писать из Kafka
//     */
//    @Test
//    void testKafkaWriteReadMessage() {
//        try {
//            log.info("Bootstrap.servers: {}", kafka.getBootstrapServers());
//            log.info("Sending message");
//            producer.send(new ProducerRecord<>(TEST_TOPIC_IN, "testKey", "json")).get(60, TimeUnit.SECONDS);
//            log.info("Sent message");
//
//            log.info("Consumer subscribe");
//            consumer.subscribe(Collections.singletonList(TEST_TOPIC_IN));
//            log.info("Consumer start reading");
//
//            getConsumerRecordsOutputTopic(consumer, 10, 1);
//        } catch (InterruptedException | TimeoutException | ExecutionException e) {
//            log.error("Error test execution", e);
//            fail();
//        }
//    }
//
//    /**
//     * Тест проверяет возможноть чтения данных из PostgreSQL
//     */
//    @Test
//    void testPostgreSQLReadValues() {
//        clearTable();
//        createAndCheckRuleInPostgreSQL(123L, String.valueOf(RuleType.COUNT_ACTIVE_BY_MODEL), "Lastochka", 15, "default");
//    }
//
//
//    @Test
//    void testAnalyticsServiceProcessing() throws InterruptedException {
//        clearTable();
//        clearClickHouse();
//
//        long serviceId = 1L;
//        createAndCheckRuleInPostgreSQL(serviceId, String.valueOf(RuleType.COUNT_ACTIVE_BY_MODEL), "Lastochka", 100000, "null");
//
//        Future<Boolean> serviceFuture = testStartService();
//
//        var ListData = List.of(
//                TestDataModel.builder().model("Lastochka").state("active").x1(100.1).x2(200.2).timestamp(System.currentTimeMillis()).build(),
//                TestDataModel.builder().model("Sapsan").state("active").x1(100.1).x2(200.2).timestamp(System.currentTimeMillis()).build(),
//                TestDataModel.builder().model("Lastochka").state("inactive").x1(200.1).x2(200.2).timestamp(System.currentTimeMillis()).build(),
//                TestDataModel.builder().model("Lastochka").state("active").x1(100.1).x2(200.2).timestamp(System.currentTimeMillis()).build());
//
//
//        ListData.forEach(data -> sendMessagesToTestTopic(producer, data));
//
//        Thread.sleep(6000);
//
//        TestDataModel analyticTrigger = TestDataModel.builder()
//                .model("Lastochka")
//                .state("active")
//                .x1(123.4)
//                .x2(567.8)
//                .timestamp(System.currentTimeMillis())
//                .build();
//        sendMessagesToTestTopic(producer, analyticTrigger);
//
//        ConsumerRecords<String, String> initialOutput = getConsumerRecordsOutputTopic(consumer, 5, 2);
//        for (ConsumerRecord<String, String> record : initialOutput) {
//            JsonNode node = toJsonNode(record.value());
//            assertFalse(node.has(String.valueOf(RuleType.COUNT_ACTIVE_BY_MODEL)),
//                    "Аналитика не должна появляться до истечения START_SECONDS");
//        }
//
//        ConsumerRecords<String, String> output = getConsumerRecordsOutputTopic(consumer, 10, 2);
//
//        assertFalse(output.isEmpty(), "Аналитическое сообщение не получено из Kafka");
//
//        JsonNode node = toJsonNode(output.iterator().next().value());
//        assertTrue(node.has(String.valueOf(RuleType.COUNT_ACTIVE_BY_MODEL)), "Отсутствует аналитическое поле");
//
//        int count = node.get(String.valueOf(RuleType.COUNT_ACTIVE_BY_MODEL)).asInt();
//        assertEquals(3, count, "Ожидается 3 активных записей для Lastochka");
//
//        boolean exists = checkAnalyticsWrittenToClickHouse("Lastochka", "active");
//        assertTrue(exists, "Запись в ClickHouse не найдена");
//
//        serviceFuture.cancel(true);
//    }
//
//
//    private boolean checkAnalyticsWrittenToClickHouse(String model, String expectedState) {
//        try (var conn = clickHouse.createConnection("")) {
//            var stmt = conn.createStatement();
//            var rs = stmt.executeQuery("SELECT model, state FROM analytic WHERE model = '" + model + "'");
//
//            while (rs.next()) {
//                if (rs.getString("state").equals(expectedState)) {
//                    return true;
//                }
//            }
//        } catch (Exception e) {
//            log.error("Ошибка при проверке ClickHouse", e);
//            fail();
//        }
//        return false;
//    }
//
//
//    private AdminClient createAdminClient() {
//        log.info("Create admin client");
//        return AdminClient.create(ImmutableMap.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()));
//    }
//
//    private KafkaConsumer<String, String> createConsumer() {
//        log.info("Create consumer");
//        return new KafkaConsumer<>(
//                ImmutableMap.of(
//                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
//                        ConsumerConfig.GROUP_ID_CONFIG, "tc-" + UUID.randomUUID(),
//                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
//                ),
//                new StringDeserializer(),
//                new StringDeserializer()
//        );
//    }
//
//    private KafkaProducer<String, String> createProducer() {
//        log.info("Create producer");
//        return new KafkaProducer<>(
//                ImmutableMap.of(
//                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
//                        ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString()
//                ),
//                new StringSerializer(),
//                new StringSerializer()
//        );
//    }
//
//    private HikariDataSource createConnectionPool() {
//        log.info("Cretae connection pool");
//        var config = new HikariConfig();
//        config.setJdbcUrl(postgreSQL.getJdbcUrl());
//        config.setUsername(postgreSQL.getUsername());
//        config.setPassword(postgreSQL.getPassword());
//        config.setDriverClassName(postgreSQL.getDriverClassName());
//        return new HikariDataSource(config);
//    }
//
//    private void checkAndCreateRequiredTopics(Admin adminClient, List<NewTopic> topics) {
//        log.info("Check required topics");
//        try {
//            Set<String> existingTopics = adminClient.listTopics().names().get();
//            if (existingTopics.isEmpty()) {
//                log.info("Topic not exist. Create topics {}", topics);
//                adminClient.createTopics(topics).all().get(30, TimeUnit.SECONDS);
//            } else {
//                topics.stream().map(NewTopic::name).filter(t -> !existingTopics.contains(t)).forEach(t -> {
//                    try {
//                        log.info("Topic not exist {}. Create topic {}", t, t);
//                        adminClient.createTopics(List.of(new NewTopic(t, partitions, replicaFactor))).all().get(30, TimeUnit.SECONDS);
//                    } catch (InterruptedException | TimeoutException | ExecutionException e) {
//                        log.error("Error creating topic Kafka", e);
//                    }
//                });
//            }
//        } catch (InterruptedException | ExecutionException | TimeoutException e) {
//            log.error("Error checking topics", e);
//        }
//    }
//
//    private void clearTable() {
//        log.info("Clear table PostgreSQL");
//        try (var conn = dataSource.getConnection()) {
//            DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
//            context.deleteFrom(table(tableName)).execute();
//
//            var result = context.fetch("SELECT * FROM " + tableName);
//            assertTrue(result.isEmpty());
//        } catch (SQLException ex) {
//            log.error("Error clearing table", ex);
//            fail();
//        }
//    }
//
//    private void clearClickHouse() {
//        try (var conn = clickHouse.createConnection("")) {
//            var stmt = conn.createStatement();
//            stmt.execute("TRUNCATE TABLE analytic");
//        } catch (Exception e) {
//            log.error("Ошибка при очистке ClickHouse", e);
//            fail();
//        }
//    }
//
//    private void createAndCheckRuleInPostgreSQL(Long serviceId, String type, String model,
//                                                int windowMinutes, String defaultValue) {
//        try {
//            log.info("Create analytics rule");
//            DSLContext context = DSL.using(dataSource.getConnection(), SQLDialect.POSTGRES);
//            context.insertInto(table("analytics_rules"))
//                    .columns(
//                            field("service_id"),
//                            field("type"),
//                            field("model"),
//                            field("window_minutes"),
//                            field("default_value")
//                    )
//                    .values(serviceId, type, model, windowMinutes, defaultValue)
//                    .execute();
//
//            log.info("Check rule from DB");
//
//            var result = context.select(
//                            field("service_id", Long.class),
//                            field("type", String.class),
//                            field("model", String.class),
//                            field("window_minutes", Integer.class),
//                            field("default_value", String.class)
//                    ).from(table(tableName))
//                    .where(field("service_id").eq(serviceId).and(field("type").eq(type)))
//                    .fetch();
//
//            String actual = result.formatCSV();
//
//            String expectedValue = String.format("service_id,type,model,window_minutes,default_value\n%d,%s,%s,%d,%s\n",
//                    serviceId, type, model, windowMinutes, defaultValue);
//
//            log.info("Expected:\n{}", expectedValue);
//            log.info("Actual:\n{}", actual);
//            assertNotNull(result);
//            assertEquals(expectedValue, actual);
//        } catch (SQLException ex) {
//            log.error("Error creating rule", ex);
//        }
//    }
//
//    private ConsumerRecords<String, String> getConsumerRecordsOutputTopic(Consumer<String, String> consumer, int retry, int timeoutSeconds) {
//        log.info("Start reading messages from kafka");
//        boolean state = false;
//        try {
//            while (!state && retry > 0) {
//                ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(100));
//                if (consumerRecords.isEmpty()) {
//                    log.info("Remaining attempts {}", retry);
//                    retry--;
//                    Thread.sleep(timeoutSeconds * 1000L);
//                } else {
//                    log.info("Read messages {}", consumerRecords.count());
//                    return consumerRecords;
//                }
//            }
//        } catch (InterruptedException ex) {
//            log.error("Interrupt read messages", ex);
//        }
//        return ConsumerRecords.empty();
//    }
//
//    private Config replaceConfigForTest(Config config) {
//        log.info("Replace test config by container values");
//        return config
//                .withValue("kafka.consumer.bootstrap.servers", ConfigValueFactory.fromAnyRef(kafka.getBootstrapServers()))
//                .withValue("kafka.producer.bootstrap.servers", ConfigValueFactory.fromAnyRef(kafka.getBootstrapServers()))
//                .withValue("db.jdbcUrl", ConfigValueFactory.fromAnyRef(postgreSQL.getJdbcUrl()))
//                .withValue("db.user", ConfigValueFactory.fromAnyRef(postgreSQL.getUsername()))
//                .withValue("db.password", ConfigValueFactory.fromAnyRef(postgreSQL.getPassword()))
//                .withValue("db.driver", ConfigValueFactory.fromAnyRef(postgreSQL.getDriverClassName()))
//                .withValue("application.updateIntervalSec", ConfigValueFactory.fromAnyRef(UPDATE_INTERVAL_POSTGRESQL_RULE_SECS))
//                .withValue("application.enrichmentId", ConfigValueFactory.fromAnyRef(ENRICHMENT_ID))
//                .withValue("clickhouse.jdbcUrl", ConfigValueFactory.fromAnyRef(clickHouse.getJdbcUrl()))
//                .withValue("clickhouse.user", ConfigValueFactory.fromAnyRef(clickHouse.getUsername()))
//                .withValue("clickhouse.password", ConfigValueFactory.fromAnyRef(clickHouse.getPassword()))
//                .withValue("clickhouse.driver", ConfigValueFactory.fromAnyRef(clickHouse.getDriverClassName()))
//                .withValue("kafka.consumer.topics", ConfigValueFactory.fromIterable(List.of(TEST_TOPIC_IN)));
//    }
//
//
//    private Future<Boolean> testStartService() {
//        log.info("Start test service enrichment");
//        Config config = ConfigFactory.load();
//        return executorForTest.submit(() -> {
//            serviceAnalytics.start(replaceConfigForTest(config));
//            return true;
//        });
//    }
//
//    private String toJson(Object object) {
//        String json = "{}";
//        try {
//            json = objectMapper.writeValueAsString(object);
//        } catch (JsonProcessingException e) {
//            log.error("Error convert object to json", e);
//        }
//        return json;
//    }
//
//    private void sendMessagesToTestTopic(Producer<String, String> producer, Object data) {
//        log.info("Send message to kafka {}", data);
//        try {
//            producer.send(new ProducerRecord<>(TEST_TOPIC_IN, "expected", toJson(data))).get();
//        } catch (InterruptedException | ExecutionException e) {
//            log.error("Error send message to kafka topic", e);
//            fail();
//        }
//    }
//
//    private JsonNode toJsonNode(String json) {
//        JsonNode jsonNode = objectMapper.createObjectNode();
//        try {
//            jsonNode = objectMapper.readTree(json);
//        } catch (IOException e) {
//            log.error("Error transformation json string to json node {}", json);
//            fail();
//        }
//        return jsonNode;
//    }
//}

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.model.RuleType;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;
import model.TestDataModel;
import org.example.Service;
import org.example.impl.ServiceAnalytics;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Testcontainers
class ServiceTest {

    @Container
    private final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"));

    @Container
    private final JdbcDatabaseContainer<?> postgreSQL = new PostgreSQLContainer(DockerImageName.parse("postgres"))
            .withDatabaseName("test_db")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("init_script.sql");

    @Container
    private final ClickHouseContainer clickHouse = new ClickHouseContainer(
            DockerImageName.parse("clickhouse/clickhouse-server:23.3"))
            .withInitScript("init_clickhouse.sql");

    ExecutorService executorForTest = Executors.newFixedThreadPool(2);

    private DataSource dataSource;
    private static final String TEST_TOPIC_IN = "test_topic_in";
    private static final String TEST_TOPIC_OUT = "test_topic_out";
    private final short replicaFactor = 1;
    private final int partitions = 3;

    private final String tableName = "analytics_rules";

    private final Service serviceAnalytics = new ServiceAnalytics();

    private Consumer<String, String> consumer;
    private Producer<String, String> producer;
    private Admin adminClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> LIST_DEFAULT_DB = List.of("admin", "config", "local");

    private static final Integer UPDATE_INTERVAL_POSTGRESQL_RULE_SECS = 10;

    private static final Long ENRICHMENT_ID = 1L;

    @BeforeEach
    void initClientsAndTestEnvironment() {
        dataSource = Optional.ofNullable(dataSource).orElse(createConnectionPool());
        adminClient = createAdminClient();
        consumer = createConsumer();
        producer = createProducer();
        List<NewTopic> topics = Stream.of(TEST_TOPIC_IN, TEST_TOPIC_OUT)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        checkAndCreateRequiredTopics(adminClient, topics);

        consumer.subscribe(Collections.singletonList(TEST_TOPIC_OUT));

    }

    @AfterEach
    void clearDataAndCloseClients() {
        clearTable();
        List.of(producer, consumer, adminClient).forEach(client -> {
            Optional.ofNullable(client).ifPresent(c -> {
                try {
                    c.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    /**
     * Проверяет готовность Kafka
     */
    @Test
    void testStartKafka() {
        assertTrue(kafka.isRunning());
    }

    /**
     * Проверяет готовность postgreSQL
     */
    @Test
    void testStartPostgreSQL() {
        assertTrue(postgreSQL.isRunning());
    }


    @Test
    void testСlickHouse() {
        assertTrue(clickHouse.isRunning());
    }

    /**
     * Проверяет возможность читать и писать из Kafka
     */
    @Test
    void testKafkaWriteReadMessage() {
        try {
            log.info("Bootstrap.servers: {}", kafka.getBootstrapServers());
            log.info("Sending message");
            producer.send(new ProducerRecord<>(TEST_TOPIC_IN, "testKey", "json")).get(60, TimeUnit.SECONDS);
            log.info("Sent message");

            log.info("Consumer subscribe");
            consumer.subscribe(Collections.singletonList(TEST_TOPIC_IN));
            log.info("Consumer start reading");

            getConsumerRecordsOutputTopic(consumer, 10, 1);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error("Error test execution", e);
            fail();
        }
    }

    /**
     * Тест проверяет возможноть чтения данных из PostgreSQL
     */
    @Test
    void testPostgreSQLReadValuesClick() {
        clearTable();
        createAndCheckRuleInPostgreSQL(123L, String.valueOf(RuleType.COUNT_ACTIVE_BY_MODEL), "Lastochka", 15, "default");
    }


    @Test
    void testAnalyticsServiceProcessing() throws InterruptedException {
        clearTable();
        clearClickHouse();

        long serviceId = 1L;
        createAndCheckRuleInPostgreSQL(serviceId, String.valueOf(RuleType.COUNT_ACTIVE_BY_MODEL), "Lastochka", 100000, "null");

        Future<Boolean> serviceFuture = testStartService();

        var ListData = List.of(
                TestDataModel.builder().model("Lastochka").state("active").x1(100.1).x2(200.2).timestamp("2025-05-16 11:34:27").build(),
                TestDataModel.builder().model("Sapsan").state("active").x1(100.1).x2(200.2).timestamp("2025-05-16 12:34:27").build(),
                TestDataModel.builder().model("Lastochka").state("inactive").x1(200.1).x2(200.2).timestamp("2025-05-16 14:34:27").build(),
                TestDataModel.builder().model("Lastochka").state("active").x1(100.1).x2(200.2).timestamp("2025-05-16 14:38:27").build());


        ListData.forEach(data -> sendMessagesToTestTopic(producer, data));

        Thread.sleep(6000);

        TestDataModel analyticTrigger = TestDataModel.builder()
                .model("Lastochka")
                .state("active")
                .x1(123.4)
                .x2(567.8)
                .timestamp("2025-05-16 14:40:27")
                .build();
        sendMessagesToTestTopic(producer, analyticTrigger);

        ConsumerRecords<String, String> initialOutput = getConsumerRecordsOutputTopic(consumer, 5, 2);
        for (ConsumerRecord<String, String> record : initialOutput) {
            JsonNode node = toJsonNode(record.value());
            assertFalse(node.has(String.valueOf(RuleType.COUNT_ACTIVE_BY_MODEL)),
                    "Аналитика не должна появляться до истечения START_SECONDS");
        }

        ConsumerRecords<String, String> output = getConsumerRecordsOutputTopic(consumer, 10, 2);

        assertFalse(output.isEmpty(), "Аналитическое сообщение не получено из Kafka");

        JsonNode node = toJsonNode(output.iterator().next().value());
        assertTrue(node.has(String.valueOf(RuleType.COUNT_ACTIVE_BY_MODEL)), "Отсутствует аналитическое поле");

        int count = node.get(String.valueOf(RuleType.COUNT_ACTIVE_BY_MODEL)).asInt();
        assertEquals(3, count, "Ожидается 3 активных записей для Lastochka");

        boolean exists = checkAnalyticsWrittenToClickHouse("Lastochka", "active");
        assertTrue(exists, "Запись в ClickHouse не найдена");

        serviceFuture.cancel(true);
    }


    private boolean checkAnalyticsWrittenToClickHouse(String model, String expectedState) {
        try (var conn = clickHouse.createConnection("")) {
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT model, state FROM analytic WHERE model = '" + model + "'");

            while (rs.next()) {
                if (rs.getString("state").equals(expectedState)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при проверке ClickHouse", e);
            fail();
        }
        return false;
    }


    private AdminClient createAdminClient() {
        log.info("Create admin client");
        return AdminClient.create(ImmutableMap.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()));
    }

    private KafkaConsumer<String, String> createConsumer() {
        log.info("Create consumer");
        return new KafkaConsumer<>(
                ImmutableMap.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                        ConsumerConfig.GROUP_ID_CONFIG, "tc-" + UUID.randomUUID(),
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
                ),
                new StringDeserializer(),
                new StringDeserializer()
        );
    }

    private KafkaProducer<String, String> createProducer() {
        log.info("Create producer");
        return new KafkaProducer<>(
                ImmutableMap.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                        ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString()
                ),
                new StringSerializer(),
                new StringSerializer()
        );
    }

    private HikariDataSource createConnectionPool() {
        log.info("Cretae connection pool");
        var config = new HikariConfig();
        config.setJdbcUrl(postgreSQL.getJdbcUrl());
        config.setUsername(postgreSQL.getUsername());
        config.setPassword(postgreSQL.getPassword());
        config.setDriverClassName(postgreSQL.getDriverClassName());
        return new HikariDataSource(config);
    }

    private void checkAndCreateRequiredTopics(Admin adminClient, List<NewTopic> topics) {
        log.info("Check required topics");
        try {
            Set<String> existingTopics = adminClient.listTopics().names().get();
            if (existingTopics.isEmpty()) {
                log.info("Topic not exist. Create topics {}", topics);
                adminClient.createTopics(topics).all().get(30, TimeUnit.SECONDS);
            } else {
                topics.stream().map(NewTopic::name).filter(t -> !existingTopics.contains(t)).forEach(t -> {
                    try {
                        log.info("Topic not exist {}. Create topic {}", t, t);
                        adminClient.createTopics(List.of(new NewTopic(t, partitions, replicaFactor))).all().get(30, TimeUnit.SECONDS);
                    } catch (InterruptedException | TimeoutException | ExecutionException e) {
                        log.error("Error creating topic Kafka", e);
                    }
                });
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Error checking topics", e);
        }
    }

    private void clearTable() {
        log.info("Clear table PostgreSQL");
        try (var conn = dataSource.getConnection()) {
            DSLContext context = DSL.using(conn, SQLDialect.POSTGRES);
            context.deleteFrom(table(tableName)).execute();

            var result = context.fetch("SELECT * FROM " + tableName);
            assertTrue(result.isEmpty());
        } catch (SQLException ex) {
            log.error("Error clearing table", ex);
            fail();
        }
    }

    private void clearClickHouse() {
        try (var conn = clickHouse.createConnection("")) {
            var stmt = conn.createStatement();
            stmt.execute("TRUNCATE TABLE analytic");
        } catch (Exception e) {
            log.error("Ошибка при очистке ClickHouse", e);
            fail();
        }
    }

    private void createAndCheckRuleInPostgreSQL(Long serviceId, String type, String model,
                                                int windowMinutes, String defaultValue) {
        try {
            log.info("Create analytics rule");
            DSLContext context = DSL.using(dataSource.getConnection(), SQLDialect.POSTGRES);
            context.insertInto(table("analytics_rules"))
                    .columns(
                            field("service_id"),
                            field("type"),
                            field("model"),
                            field("window_minutes"),
                            field("default_value")
                    )
                    .values(serviceId, type, model, windowMinutes, defaultValue)
                    .execute();

            log.info("Check rule from DB");

            var result = context.select(
                            field("service_id", Long.class),
                            field("type", String.class),
                            field("model", String.class),
                            field("window_minutes", Integer.class),
                            field("default_value", String.class)
                    ).from(table(tableName))
                    .where(field("service_id").eq(serviceId).and(field("type").eq(type)))
                    .fetch();

            String actual = result.formatCSV();

            String expectedValue = String.format("service_id,type,model,window_minutes,default_value\n%d,%s,%s,%d,%s\n",
                    serviceId, type, model, windowMinutes, defaultValue);

            log.info("Expected:\n{}", expectedValue);
            log.info("Actual:\n{}", actual);
            assertNotNull(result);
            assertEquals(expectedValue, actual);
        } catch (SQLException ex) {
            log.error("Error creating rule", ex);
        }
    }

    private ConsumerRecords<String, String> getConsumerRecordsOutputTopic(Consumer<String, String> consumer, int retry, int timeoutSeconds) {
        log.info("Start reading messages from kafka");
        boolean state = false;
        try {
            while (!state && retry > 0) {
                ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(100));
                if (consumerRecords.isEmpty()) {
                    log.info("Remaining attempts {}", retry);
                    retry--;
                    Thread.sleep(timeoutSeconds * 1000L);
                } else {
                    log.info("Read messages {}", consumerRecords.count());
                    return consumerRecords;
                }
            }
        } catch (InterruptedException ex) {
            log.error("Interrupt read messages", ex);
        }
        return ConsumerRecords.empty();
    }

    private Config replaceConfigForTest(Config config) {
        log.info("Replace test config by container values");
        return config
                .withValue("kafka.consumer.bootstrap.servers", ConfigValueFactory.fromAnyRef(kafka.getBootstrapServers()))
                .withValue("kafka.producer.bootstrap.servers", ConfigValueFactory.fromAnyRef(kafka.getBootstrapServers()))
                .withValue("db.jdbcUrl", ConfigValueFactory.fromAnyRef(postgreSQL.getJdbcUrl()))
                .withValue("db.user", ConfigValueFactory.fromAnyRef(postgreSQL.getUsername()))
                .withValue("db.password", ConfigValueFactory.fromAnyRef(postgreSQL.getPassword()))
                .withValue("db.driver", ConfigValueFactory.fromAnyRef(postgreSQL.getDriverClassName()))
                .withValue("application.updateIntervalSec", ConfigValueFactory.fromAnyRef(UPDATE_INTERVAL_POSTGRESQL_RULE_SECS))
                .withValue("application.enrichmentId", ConfigValueFactory.fromAnyRef(ENRICHMENT_ID))
                .withValue("clickhouse.jdbcUrl", ConfigValueFactory.fromAnyRef(clickHouse.getJdbcUrl()))
                .withValue("clickhouse.user", ConfigValueFactory.fromAnyRef(clickHouse.getUsername()))
                .withValue("clickhouse.password", ConfigValueFactory.fromAnyRef(clickHouse.getPassword()))
                .withValue("clickhouse.driver", ConfigValueFactory.fromAnyRef(clickHouse.getDriverClassName()))
                .withValue("kafka.consumer.topics", ConfigValueFactory.fromIterable(List.of(TEST_TOPIC_IN)));
    }


    private Future<Boolean> testStartService() {
        log.info("Start test service enrichment");
        Config config = ConfigFactory.load();
        return executorForTest.submit(() -> {
            serviceAnalytics.start(replaceConfigForTest(config));
            return true;
        });
    }

    private String toJson(Object object) {
        String json = "{}";
        try {
            json = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error convert object to json", e);
        }
        return json;
    }

    private void sendMessagesToTestTopic(Producer<String, String> producer, Object data) {
        log.info("Send message to kafka {}", data);
        try {
            producer.send(new ProducerRecord<>(TEST_TOPIC_IN, "expected", toJson(data))).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error send message to kafka topic", e);
            fail();
        }
    }

    private JsonNode toJsonNode(String json) {
        JsonNode jsonNode = objectMapper.createObjectNode();
        try {
            jsonNode = objectMapper.readTree(json);
        } catch (IOException e) {
            log.error("Error transformation json string to json node {}", json);
            fail();
        }
        return jsonNode;
    }
}