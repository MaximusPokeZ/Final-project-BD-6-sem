package org.example.impl;

import com.typesafe.config.Config;
import org.example.DbReader;
import org.example.KafkaReader;
import org.example.Service;

public class ServiceEnrichment implements Service {
    @Override
    public void start(Config config) {
        DbReader dbReader = new EnrichDBReaderImpl(config);
        KafkaReader kafkaReader = new EnrichKafkaReaderImpl(config, dbReader);
        kafkaReader.processing();
    }
}
