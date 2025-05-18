package org.example.impl;

import com.typesafe.config.Config;
import org.example.DbReader;
import org.example.KafkaReader;
import org.example.Service;

public class ServiceAnalytics implements Service {
    @Override
    public void start(Config config) {
        DbReader dbReader = new AnalyticsDBReaderImpl(config);
        KafkaReader kafkaReader = new AnalyticsKafkaReaderImpl(config, dbReader);
        kafkaReader.processing();
    }
}
