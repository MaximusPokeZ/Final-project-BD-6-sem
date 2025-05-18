package org.example;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;
import org.example.impl.ServiceFiltering;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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


    private Config replaceConfigForTest(Config config) {
        return config.withValue("kafka.consumer.bootstrap.servers", ConfigValueFactory.fromAnyRef(kafka.getBootstrapServers()))
                .withValue("kafka.producer.bootstrap.servers", ConfigValueFactory.fromAnyRef(kafka.getBootstrapServers()))
                .withValue("db.jdbcUrl", ConfigValueFactory.fromAnyRef(postgreSQL.getJdbcUrl()))
                .withValue("db.user", ConfigValueFactory.fromAnyRef(postgreSQL.getUsername()))
                .withValue("db.password", ConfigValueFactory.fromAnyRef(postgreSQL.getPassword()))
                .withValue("db.driver", ConfigValueFactory.fromAnyRef(postgreSQL.getDriverClassName()))
                .withValue("application.updateIntervalSec", ConfigValueFactory.fromAnyRef(10));
    }

    ExecutorService executorForTest = Executors.newFixedThreadPool(2);

    private DataSource dataSource;
    private final String topicIn = "test_topic_in";
    private final String topicOut = "test_topic_out";
    private final short replicaFactor = 1;
    private final int partitions = 3;

    private final String tableName = "filter_rules";

    private final Service serviceFiltering = new ServiceFiltering();

    @Test
    void testStartKafka() {
        assertTrue(kafka.isRunning());
    }

    @Test
    void testStartPostgreSQL() {
        assertTrue(postgreSQL.isRunning());
    }

    @Test
    void testKafkaWriteReadMessage() {
        log.info("Bootstrap.servers: {}", kafka.getBootstrapServers());
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {
            log.info("Creating topics");
            adminClient.createTopics(topics).all().get(30, TimeUnit.SECONDS);
            log.info("Created topics");
            log.info("Sending message");
            producer.send(new ProducerRecord<>(topicIn, "testKey", "json")).get();
            log.info("Sent message");

            log.info("Consumer subscribe");
            consumer.subscribe(Collections.singletonList(topicIn));
            log.info("Consumer start reading");

            getConsumerRecordsOutputTopic(consumer, 10, 1);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testPostgreSQLReadValues() {
        clearTable();
        createAndCheckRuleInPostgreSQL(0L, 0L, "test_field", "equals", "test_value");
    }


    @Test
    void testServiceFilteringEquals() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "model", "equals", "Lastochka");

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"no_Last\", \"coordX1\":67.89, \"state\": null}",
                    "{\"model\":\"\", \"coordX1\":18.00, \"state\":\"active\"}",
                    "{\"model\":null, \"place\":18, \"state\":\"M\"}",
                    "{\"coordX2\":18.90, \"wire\":\"M\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Lastochka\", \"coordX1\":18.90, \"coordX2\":\"9.90\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }

    @Test
    void testServiceFilteringNotEquals() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            clearTable();
            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            createAndCheckRuleInPostgreSQL(1L, 1L, "model", "not_equals", "Lastochka");

            Config config = ConfigFactory.load();
            log.info("Config: {}", config);
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"Lastochka\", \"coordX1\":19.89, \"state\":\"non-active\"}",
                    "{\"model\":\"Lastochka\", \"coordX1\":null}",
                    "{\"model\":\"Lastochka\", \"coordX1\":, \"state\":null}",
                    "{\"model\":\"Lastochka\", \"coordX1\":\"M\"}",
                    "{\"model\":\"Lastochka\"}").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Ivolga\", \"coordX1\":18, \"state\":\"active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }


    @Test
    void testServiceFilteringContains() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "model", "contains", "ast");

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"no_alex\", \"coordX1\":1.8, \"state\":null}",
                    "{\"model\":\"\", \"coordX1\":13456.00, \"state\":\"active\"}",
                    "{\"model\":null, \"coordX1\":184.7, \"state\":\"non-active\"}",
                    "{\"coordX1\":18, \"state\": null}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"prev_Lastoc\", \"coordX1\":345.46, \"state\":\"active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }


    @Test
    void testServiceFilteringNotContains() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "model", "not_contains", "ast");

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"Lastochka\", \"coordX\":19, \"state\":\"active\"}",
                    "{\"model\":\"Last\", \"coordX\":null}",
                    "{\"model\":\"astochka\", \"coordX\":, \"state\":null}",
                    "{\"model\":\"Lastoka\", \"state\":\"M\"}",
                    "{\"model\":\"ast\"}").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Ivolga\", \"coordX\":18, \"state\":\"M\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }


    @Test
    void testServiceFilteringEqualsTwoRules() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "model", "equals", "Lastochka");
            createAndCheckRuleInPostgreSQL(1L, 2L, "coordX", "equals", "1.99");

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"Ivolga\", \"coordX\":1.99, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coordX\":-, \"state\":\"active\"}",
                    "{\"model\":null, \"coordX\":\"\", \"state\":\"active\"}",
                    "{\"model\":null, \"coordX\":null, \"state\":\"active\"}",
                    "{\"coordX\":18.78, \"state\":\"active\"}",
                    "{\"model\":\"Ivolga\", \"coordX\":1.234, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Lastochka\", \"coordX\":1.99, \"state\": null}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();

            clearTable();
        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }


    @Test
    void testServiceFilteringEqualsNotEqualsTwoRules() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "model", "equals", "Ivolga");
            createAndCheckRuleInPostgreSQL(1L, 2L, "coordX", "not_equals", "1.99");

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"Lastochka\", \"coordX\":18.4534, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coordX\":-, \"state\":\"active\"}",
                    "{\"model\":null, \"coordX\":\"\", \"state\":\"active\"}",
                    "{\"model\":null, \"coordX\":null, \"state\":\"active\"}",
                    "{\"coordX\":18.45, \"state\":\"active\"}",
                    "{\"model\":\"Moskva\", \"coordX\":1.99, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Ivolga\", \"coordX\":20.90, \"state\":\"active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();

            clearTable();
        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }


    @Test
    void testServiceFilteringContainsTwoRules() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "model", "contains", "Lasto");
            createAndCheckRuleInPostgreSQL(1L, 2L, "model", "contains", "chka");

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"alexander\", \"coord\":18, \"state\":\"active\"}",
                    "{\"model\":\"pushkin\", \"coord\":18, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coord\":18, \"state\":\"active\"}",
                    "{\"model\":null, \"coord\":18, \"state\":\"active\"}",
                    "{\"coord\":18, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Lastochka\", \"coord\":18, \"state\":\"active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }


    @Test
    void testServiceFilteringContainsNotContainsTwoRules() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "model", "contains", "Lastochka");
            createAndCheckRuleInPostgreSQL(1L, 2L, "model", "not_contains", "-24");

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"Lastochka-24\", \"coord\":18.78, \"state\":\"active\"}",
                    "{\"model\":\"Ivolga-24\", \"coord\":1.347, \"state\":\"active\"}",
                    "{\"model\":\"Ivolga\", \"coord\":18, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coord\":18, \"state\":\"active\"}",
                    "{\"model\":null, \"coord\":18, \"state\":\"active\"}",
                    "{\"coord\":18, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Lastochka-25\", \"coord\":18, \"state\":\"non-active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }


    @Test
    void testServiceFilteringEqualsContainsTwoRules() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "coordX", "equals", "1.99");
            createAndCheckRuleInPostgreSQL(1L, 2L, "model", "contains", "Last");

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"Lastochka\", \"coordX\":1.89, \"state\":\"active\"}",
                    "{\"model\":\"Ivolga\", \"coordX\":18.789, \"state\":\"active\"}",
                    "{\"model\":\"Ivolga\", \"coordX\":18.23, \"state\":\"active\"}",
                    "{\"model\":\"Ivolga\", \"coordX\":1.99, \"state\":\"active\"}",
                    "{\"model\":\"Ivolga\", \"coordX\":null, \"state\":\"active\"}",
                    "{\"model\":\"Ivolga\", \"coordX\":, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coordX\":\"\", \"state\":\"active\"}",
                    "{\"model\":null, \"coordX\":11.8, \"state\":\"active\"}",
                    "{\"coordX\":198, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Lastochka\", \"coordX\":1.99, \"state\":\"non-active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }


    @Test
    void testServiceFilteringEqualsNotContainsTwoRules() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "coordX", "equals", "1.99");
            createAndCheckRuleInPostgreSQL(1L, 2L, "model", "not_contains", "olga");

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"Ivolga\", \"coordX\":1.99, \"state\":\"active\"}",
                    "{\"model\":\"Last\", \"coordX\":1.9, \"state\":\"active\"}",
                    "{\"model\":\"Ivolga\", \"coordX\":null, \"state\":\"active\"}",
                    "{\"model\":\"Ivolga\", \"coordX\":, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coordX\":\"\", \"state\":\"active\"}",
                    "{\"model\":null, \"state\":\"active\"}",
                    "{\"coordX\":1.98, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Moskva\", \"coordX\":1.99, \"state\":\"active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }


    @Test
    void testServiceFilteringContainsNotEqualsTwoRules() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "coordX", "not_equals", "1.99");
            createAndCheckRuleInPostgreSQL(1L, 2L, "model", "contains", "olga");

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"Ivolga\", \"coordX\":1.99, \"state\":\"active\"}",
                    "{\"model\":\"Last\", \"coordX\":1.99, \"state\":\"active\"}",
                    "{\"model\":\"Last\", \"coordX\":null, \"state\":\"active\"}",
                    "{\"model\":\"Last\", \"coordX\":, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coordX\":\"\", \"state\":\"active\"}",
                    "{\"model\":null, \"state\":\"active\"}",
                    "{\"coordX\":1.788, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Ivolga\", \"coordX\":2.00, \"state\":\"active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }

    @Test
    void testServiceFilteringNotRules() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"alex_ivanov\", \"coordX\":20, \"state\":\"active\"}",
                    "{\"model\":\"pushkin\", \"coordX\":19, \"state\":\"active\"}",
                    "{\"model\":\"alex\", \"coordX\":null, \"state\":\"active\"}",
                    "{\"model\":\"alex\", \"coordX\":, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coordX\":\"\", \"state\":\"active\"}",
                    "{\"model\":null, \"state\":\"active\"}",
                    "{\"coordX\":18, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertTrue(consumerRecords.isEmpty());

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }

    @Test
    void testServiceFilteringUpdateRule() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "model", "equals", "Ivolga");


            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"Last\", \"coordX\":1.8, \"state\":\"active\"}",
                    "{\"model\":\"Moskva\", \"coordX\":1.9, \"state\":\"active\"}",
                    "{\"model\":\"Ivolgaaa\", \"coordX\":null, \"state\":\"active\"}",
                    "{\"model\":, \"coordX\":, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coordX\":\"\", \"state\":\"active\"}",
                    "{\"model\":null, \"state\":\"active\"}",
                    "{\"coordX\":1.7, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Ivolga\", \"coordX\":1.99, \"state\":\"non-active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "model", "contains", "olga");
            log.info("Wait until application updated rules from DB");
            Thread.sleep(config.getLong("application.updateIntervalSec") * 1000 * 2 + 1);

            Set.of("{\"model\":\"Lst\", \"coordX\":1.8, \"state\":\"active\"}",
                    "{\"model\":\"cLst\", \"coordX\":1.9, \"state\":\"active\"}",
                    "{\"model\":\"Lst\", \"coordX\":null, \"state\":\"active\"}",
                    "{\"model\":, \"coordX\":, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coordX\":\"\", \"state\":\"active\"}",
                    "{\"model\":null, \"state\":\"active\"}",
                    "{\"coordX\":1.7, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJsonOther = "{\"model\":\"Ivolga\", \"coordX\":1.99, \"state\":\"active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJsonOther)).get();

            Future<ConsumerRecords<String, String>> resultOther = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecordsOther = resultOther.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecordsOther.isEmpty());
            assertEquals(1, consumerRecordsOther.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecordsOther) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJsonOther, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }


    @Test
    void testServiceFilteringAddNewRule() {
        List<NewTopic> topics = Stream.of(topicIn, topicOut)
                .map(topicName -> new NewTopic(topicName, partitions, replicaFactor))
                .toList();

        log.info("Topics: {}, replica factor {}, partitions {}", topics, replicaFactor, partitions);

        try (AdminClient adminClient = createAdminClient();
             KafkaConsumer<String, String> consumer = createConsumer();
             KafkaProducer<String, String> producer = createProducer()) {

            checkAndCreateRequiredTopics(adminClient, topics);

            consumer.subscribe(Collections.singletonList(topicOut));

            clearTable();
            createAndCheckRuleInPostgreSQL(1L, 1L, "model", "contains", "olga");

            Config config = ConfigFactory.load();
            config = replaceConfigForTest(config);
            Future<Boolean> serviceIsWork = testStartService(config);

            Set.of("{\"model\":\"Lst\", \"coordX\":1.8, \"state\":\"active\"}",
                    "{\"model\":\"cLst\", \"coordX\":1.9, \"state\":\"active\"}",
                    "{\"model\":\"Lst\", \"coordX\":null, \"state\":\"active\"}",
                    "{\"model\":, \"coordX\":, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coordX\":\"\", \"state\":\"active\"}",
                    "{\"model\":null, \"state\":\"active\"}",
                    "{\"coordX\":1.7, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJson = "{\"model\":\"Ivolga\", \"coordX\":1.99, \"state\":\"non-active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJson)).get();

            Future<ConsumerRecords<String, String>> result = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecords = result.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecords.isEmpty());
            assertEquals(1, consumerRecords.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJson, consumerRecord.value());
            }

            createAndCheckRuleInPostgreSQL(1L, 2L, "coordX", "equals", "1.99");
            log.info("Wait until application updated rules from DB");
            Thread.sleep(config.getLong("application.updateIntervalSec") * 1000 * 2 + 1);

            Set.of("{\"model\":\"Lst\", \"coordX\":1.8, \"state\":\"active\"}",
                    "{\"model\":\"cLst\", \"coordX\":1.9, \"state\":\"active\"}",
                    "{\"model\":\"Lst\", \"coordX\":null, \"state\":\"active\"}",
                    "{\"model\":, \"coordX\":, \"state\":\"active\"}",
                    "{\"model\":\"\", \"coordX\":\"\", \"state\":\"active\"}",
                    "{\"model\":null, \"state\":\"active\"}",
                    "{\"coordX\":1.7, \"state\":\"active\"}",
                    "").forEach(negativeJson -> {
                try {
                    producer.send(new ProducerRecord<>(topicIn, negativeJson)).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error send message to kafka topic", e);
                    fail();
                }
            });

            String expectedJsonOther = "{\"model\":\"Ivolga\", \"coordX\":1.99, \"state\":\"non-active\"}";
            producer.send(new ProducerRecord<>(topicIn, "expected", expectedJsonOther)).get();

            Future<ConsumerRecords<String, String>> resultOther = executorForTest.submit(() -> getConsumerRecordsOutputTopic(consumer, 10, 1));

            var consumerRecordsOther = resultOther.get(60, TimeUnit.SECONDS);

            assertFalse(consumerRecordsOther.isEmpty());
            assertEquals(1, consumerRecordsOther.count());

            for (ConsumerRecord<String, String> consumerRecord : consumerRecordsOther) {
                assertNotNull(consumerRecord.value());
                assertEquals(expectedJsonOther, consumerRecord.value());
            }

            serviceIsWork.cancel(true);
            executorForTest.shutdown();
            clearTable();

        } catch (Exception e) {
            log.error("Error test execution", e);
            fail();
        }
    }

    private AdminClient createAdminClient() {
        return AdminClient.create(ImmutableMap.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()));
    }

    private KafkaConsumer<String, String> createConsumer() {
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
        var config = new HikariConfig();
        config.setJdbcUrl(postgreSQL.getJdbcUrl());
        config.setUsername(postgreSQL.getUsername());
        config.setPassword(postgreSQL.getPassword());
        config.setDriverClassName(postgreSQL.getDriverClassName());
        return new HikariDataSource(config);
    }

    private void checkAndCreateRequiredTopics(AdminClient adminClient, List<NewTopic> topics) {
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
        try {
            if (dataSource == null) {
                dataSource = createConnectionPool();
            }
            DSLContext context = DSL.using(dataSource.getConnection(), SQLDialect.POSTGRES);
            context.deleteFrom(table(tableName)).execute();

            var result = context.select(
                            field("filter_id"),
                            field("rule_id"),
                            field("field_name"),
                            field("filter_function_name"),
                            field("filter_value")
                    )
                    .from(table(tableName))
                    .fetch();

            assertTrue(result.isEmpty());
        } catch (SQLException ex) {
            log.error("Error truncate table", ex);
        }
    }

    private void createAndCheckRuleInPostgreSQL(Long filterId, Long ruleId, String fieldName, String filterFunctionName, String filterValue) {
        try {
            if (dataSource == null) {
                dataSource = createConnectionPool();
            }
            log.info("Create filtering rule 1");
            DSLContext context = DSL.using(dataSource.getConnection(), SQLDialect.POSTGRES);
            context.insertInto(table(tableName)).columns(
                            field("filter_id"),
                            field("rule_id"),
                            field("field_name"),
                            field("filter_function_name"),
                            field("filter_value")
                    ).values(filterId, ruleId, fieldName, filterFunctionName, filterValue)
                    .execute();

            log.info("Check rule from DB");
            var result = context.select(
                            field("filter_id"),
                            field("rule_id"),
                            field("field_name"),
                            field("filter_function_name"),
                            field("filter_value")
                    )
                    .from(table(tableName))
                    .where(field("filter_id").eq(filterId).and(field("rule_id").eq(ruleId)))
                    .fetch();

            String expectedValue =
                    String.format("filter_id,rule_id,field_name,filter_function_name,filter_value\n%d,%d,%s,%s,%s\n",
                            filterId, ruleId, fieldName, filterFunctionName, filterValue);

            assertEquals(expectedValue, result.formatCSV());
        } catch (SQLException ex) {
            log.error("Error creating rule", ex);
        }
    }

    private ConsumerRecords<String, String> getConsumerRecordsOutputTopic(KafkaConsumer<String, String> consumer, int retry, int timeoutSeconds) {
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



    private Future<Boolean> testStartService(Config config) {
        return executorForTest.submit(() -> {
            serviceFiltering.start(config);
            return true;
        });
    }
}