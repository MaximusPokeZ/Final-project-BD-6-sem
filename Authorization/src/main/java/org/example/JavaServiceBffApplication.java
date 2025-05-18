package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableFeignClients
@EnableJpaRepositories
@SpringBootApplication
@Slf4j
public class JavaServiceBffApplication {

  public static void main(String[] args) {
    log.info("made by ykwais");
    SpringApplication.run(JavaServiceBffApplication.class, args);
  }
}
