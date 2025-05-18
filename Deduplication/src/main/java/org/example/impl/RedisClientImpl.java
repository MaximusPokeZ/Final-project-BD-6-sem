package org.example.impl;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPooled;
import org.example.RedisClient;

@Slf4j
public class RedisClientImpl implements RedisClient {

    private final Config config;

    public RedisClientImpl(Config config) {
        this.config = config;
    }

    @Override
    public boolean canPass(String key, Long maxTimeTTL) {
        log.info("key for redis: {}", key);
        try (JedisPooled jedis = new JedisPooled(config.getString("redis.host"), config.getInt("redis.port")) ){
            if (jedis.exists(key)) {
                log.info("key exists!");
                return false;
            } else {
                log.info("key not exists");
                jedis.setex(key, maxTimeTTL, key);

            }
        } catch (Exception e) {
            log.info("can't connect to Redis");
        }
        return true;
    }
}
