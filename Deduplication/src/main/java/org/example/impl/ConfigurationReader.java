package org.example.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.example.ConfigReader;

public class ConfigurationReader implements ConfigReader {
    @Override
    public Config loadConfig() {
        return ConfigFactory.load();
    }
}
