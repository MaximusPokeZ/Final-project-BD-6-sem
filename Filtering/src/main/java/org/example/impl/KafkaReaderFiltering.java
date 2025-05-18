package org.example.impl;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.DbReader;
import org.example.KafkaReader;
import org.example.KafkaWriter;
import org.example.model.Message;
import org.example.model.Rule;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;



@Slf4j
public class KafkaReaderFiltering implements KafkaReader {

    static final String KAFKA_CONSUMER_CONFIG = "kafka.consumer";

    private final KafkaConsumer<String, String> consumer;

    private final KafkaWriter producer;

    private final FilteringByRuleProcessor filter = new FilteringByRuleProcessor();

    private final DbReader dbReader;

    public KafkaReaderFiltering(Config config, DbReader dbReader) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString(KAFKA_CONSUMER_CONFIG + ".bootstrap.servers"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getString(KAFKA_CONSUMER_CONFIG + ".group.id"));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getString(KAFKA_CONSUMER_CONFIG + ".auto.offset.reset"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        List<String> topics = Arrays.asList(config.getStringList(KAFKA_CONSUMER_CONFIG + ".topics").toArray(new String[0]));

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(topics);
        this.dbReader = dbReader;
        this.producer = new KafkaWriterFiltering(config);
    }

    @Override
    public void processing() {
        try {
            while (true) {
                try {
                    log.info("Polling messages...");
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(50));
                    log.info("Received {} messages", records.count());

                    for (ConsumerRecord<String, String> oneRecord : records) {
                        Rule[] currentRules = dbReader.readRulesFromDB();
                        log.info("!!!{}", Arrays.toString(currentRules));
                        log.info("*** new message on KafkaReader: {}", oneRecord.value());
                        Message afterFilteringMessage = filter.processing(new Message(oneRecord.value(), false), currentRules);

                        if (afterFilteringMessage != null && afterFilteringMessage.isFilterState()) {
                            producer.processing(afterFilteringMessage);
                            log.info("send to out topic: {}", afterFilteringMessage);
                        }
                    }

                } catch (UnsupportedOperationException e) {
                    log.error("unsupported operation! :{}", e.getMessage());
                } catch (KafkaException e) {
                    log.info("container had been closed!");
                    break;
                }
            }
        } finally {
            close();
        }
     }

     private void close() {
             if(consumer != null) {
                 consumer.close();
             }
             if (producer != null) {
                 producer.close();
             }
             if (dbReader != null) {
                 dbReader.close();
             }
     }
}
