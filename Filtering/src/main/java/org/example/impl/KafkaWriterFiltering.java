package org.example.impl;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.KafkaWriter;
import org.example.model.Message;

import java.util.Properties;

@Slf4j
public class KafkaWriterFiltering  implements KafkaWriter {

    private final String topicOut;

    static final String KAFKA_PRODUCER_CONFIG = "kafka.producer";

    private final KafkaProducer<String, String> producer;

    public KafkaWriterFiltering(Config config) {
        Properties props = new Properties();
        props.put("bootstrap.servers", config.getString(KAFKA_PRODUCER_CONFIG + ".bootstrap.servers"));
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        topicOut = config.getString(KAFKA_PRODUCER_CONFIG + ".topic");

        this.producer = new KafkaProducer<>(props);
    }

    @Override
    public void processing(Message message) {
        if (topicOut != null) {
            producer.send(new ProducerRecord<>(topicOut, message.getValue()));
        } else {
            log.error("Topic not set for producer!");
        }
    }

    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}
