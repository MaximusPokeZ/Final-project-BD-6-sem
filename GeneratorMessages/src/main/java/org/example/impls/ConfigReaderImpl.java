package org.example.impls;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.example.ConfigReader;

@Slf4j
public class ConfigReaderImpl implements ConfigReader {

    @Override
    public Config loadConfig() {
        log.info("Loading config...");
        Config conf = ConfigFactory.load();
        log.info("loaded config: {}", conf);
        return conf;
    }
}
