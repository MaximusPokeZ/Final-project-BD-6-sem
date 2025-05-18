package org.example.impl;

import com.typesafe.config.Config;
import org.example.DbReader;
import org.example.KafkaReader;
import org.example.Service;

public class ServiceDeduplication implements Service {
    @Override
    public void start(Config config) {
        DbReader dbReader = new DbReaderImpl(config);
        KafkaReader kafkaReader = new KafkaReaderImpl(config, dbReader);
        kafkaReader.processing();
    }
}
