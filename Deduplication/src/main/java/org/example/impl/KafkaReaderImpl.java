package org.example.impl;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.DbReader;
import org.example.KafkaReader;
import org.example.KafkaWriter;
import org.example.RuleProcessor;
import org.example.model.Message;
import org.example.model.Rule;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
public class KafkaReaderImpl implements KafkaReader {

//    static final String TOPIC_IN = "test_topic_in";
//
//    private final KafkaConsumer<String, String> consumer;
//
//    private final KafkaWriter producer;
//
//    private final RuleProcessor processor;
//
//    private final DbReader dbReader;
//
//    public KafkaReaderImpl(Config config, DbReader dbReader) {
//        Properties props = new Properties();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka.consumer.bootstrap.servers"));
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getString("kafka.consumer.group.id"));
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getString("kafka.consumer.auto.offset.reset"));
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//
//        this.consumer = new KafkaConsumer<>(props);
//        this.consumer.subscribe(Collections.singletonList(TOPIC_IN));
//        this.dbReader = dbReader;
//        this.producer = new KafkaWriterImpl(config);
//        this.processor = new RuleProcessorImpl(config);
//    }

    static final String KAFKA_CONSUMER_CONFIG = "kafka.consumer";

    private final KafkaConsumer<String, String> consumer;

    private final KafkaWriter producer;

    private final RuleProcessor processor;

    private final DbReader dbReader;

    public KafkaReaderImpl(Config config, DbReader dbReader) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString(KAFKA_CONSUMER_CONFIG + ".bootstrap.servers"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getString(KAFKA_CONSUMER_CONFIG + ".group.id"));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getString(KAFKA_CONSUMER_CONFIG + ".auto.offset.reset"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        List<String> topics = Arrays.asList(config.getStringList(KAFKA_CONSUMER_CONFIG + ".topics").toArray(new String[0]));
        log.info("Topics!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: {}", topics);

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(topics);
        this.dbReader = dbReader;
        this.producer = new KafkaWriterImpl(config);
        this.processor = new RuleProcessorImpl(config);
    }

    @Override
    public void processing() {
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(50));
            log.info("count:{}", records.count());

            for (ConsumerRecord<String, String> oneRecord : records) {
                Rule[] currentRules = dbReader.readRulesFromDB();
                log.info("rule reading");

                Message afterDedublicationMessage = processor.processing(new Message(oneRecord.value(), false), currentRules);

                if (afterDedublicationMessage != null && afterDedublicationMessage.isDeduplicationState()) {
                    log.info("message after deduplication{}", afterDedublicationMessage.getValue());
                    producer.processing(afterDedublicationMessage);
                }
            }
        }
    }
}
