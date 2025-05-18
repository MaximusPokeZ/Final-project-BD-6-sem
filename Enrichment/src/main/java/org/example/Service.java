package org.example;

import com.typesafe.config.Config;

public interface Service {

    void start(Config config); // стартует приложение.
}
