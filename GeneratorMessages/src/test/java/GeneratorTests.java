import com.typesafe.config.Config;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.example.ConfigReader;
import org.example.ServiceGeneratorMain;
import org.example.impls.ConfigReaderImpl;
import org.example.impls.GeneretorMessagesJson;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@Testcontainers
public class GeneratorTests {


    private static final Logger log = LoggerFactory.getLogger(GeneratorTests.class);

    @Test
    public void testGetNewMessageReturnsValidJson() {
        GeneretorMessagesJson generator = new GeneretorMessagesJson();
        String message = generator.getNewMessage();
        log.info(message);
        assertNotNull(message);

    }

    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Test
    public void testMessageSentToKafka() {
        kafka.start();


        String topic = "test-topic";
        String bootstrapServers = kafka.getBootstrapServers();


        System.setProperty("KAFKA_PRODUCER_BOOTSTRAP_SERVERS", bootstrapServers);
        System.setProperty("KAFKA_PRODUCER_TOPIC", topic);
        System.setProperty("MESSAGE_UPDATE_INTERVAL_SEC", "1");

        Awaitility.await()
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> kafka.isRunning());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> ServiceGeneratorMain.main(new String[0]));




        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "test-group");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("auto.offset.reset", "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singleton(topic));
            log.info("Subscribed to topic !!! {}", topic);

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
            assertFalse("Сообщения не получены", records.isEmpty());
            log.info("cooбщения пришли!!!");

            for(ConsumerRecord<String, String> record : records) {
                log.info("GET: {}", record.value());
            }

        }

        executor.shutdownNow();
    }
}
