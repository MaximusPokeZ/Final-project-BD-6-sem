package org.example.impl;

import com.typesafe.config.Config;
import org.example.DbReader;
import org.example.KafkaReader;
import org.example.Service;

public class ServiceFiltering implements Service {
    @Override
    public void start(Config config) {
        DbReader dbReader = new DbReaderFiltering(config);

        KafkaReader kafkaReader = new KafkaReaderFiltering(config, dbReader);

        kafkaReader.processing();
    }
}
