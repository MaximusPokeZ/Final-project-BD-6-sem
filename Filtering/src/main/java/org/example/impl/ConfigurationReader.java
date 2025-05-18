package org.example.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.example.ConfigReader;

@Slf4j
public class ConfigurationReader implements ConfigReader {
    @Override
    public Config loadConfig() {
        log.info("Loading configuration");
        log.info("loaded config: {}", ConfigFactory.load());
        return ConfigFactory.load();
    }
}
