package org.example;

import com.typesafe.config.Config;

public interface ConfigReader {
    Config loadConfig(); // метод читает конфигурацию из файла *.conf
}
