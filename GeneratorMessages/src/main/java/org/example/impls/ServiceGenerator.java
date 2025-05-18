package org.example.impls;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.example.KafkaWriter;
import org.example.Service;
import org.example.models.Message;

@Slf4j
public class ServiceGenerator implements Service {

    @Override
    public void start(Config config)  {
        KafkaWriter kafkaWriter = new KafkaWriterImpl(config);
        Integer interval = kafkaWriter.getInterval();
        log.info("INTERVAL: {}", interval);
        GeneretorMessagesJson generetorMessagesJson = new GeneretorMessagesJson();
        try {
            while (true) {
                try {
                    Thread.sleep(interval * 100L);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
                String newMessage = generetorMessagesJson.getNewMessage();
                kafkaWriter.processing(new Message(newMessage, true));
                log.info("send to output topic: {}", newMessage);
            }
        } finally {
            kafkaWriter.close();
        }
    }
}
