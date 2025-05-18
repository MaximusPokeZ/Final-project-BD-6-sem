package org.example;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.example.impls.ConfigReaderImpl;
import org.example.impls.ServiceGenerator;

@Slf4j
public class ServiceGeneratorMain {
    public static void main(String[] args) {
        log.info("Starting service generator");
        ConfigReader configReader = new ConfigReaderImpl();
        Config config = configReader.loadConfig();
        Service service = new ServiceGenerator();
        log.info("starting service generator");
        service.start(config);
    }
}