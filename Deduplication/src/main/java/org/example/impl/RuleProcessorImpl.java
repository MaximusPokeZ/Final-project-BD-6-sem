package org.example.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;


import org.example.RedisClient;
import org.example.RuleProcessor;
import org.example.model.Message;
import org.example.model.Rule;
import org.example.model.Pair;

import java.util.Arrays;
import java.util.HashSet;

@Slf4j
public class RuleProcessorImpl implements RuleProcessor {

    private final RedisClient redis;

    public RuleProcessorImpl(Config config) {
        redis = new RedisClientImpl(config);
    }

    @Override
    public Message processing(Message message, Rule[] rules) {

        log.info("%%%%%%% message {}", message);
        log.info("%%%% rules {}", Arrays.toString(rules));

        if (message == null) {
            log.info("message is null");
            return null;
        }

        if (message.getValue().isEmpty()) {
            log.info("message is empty");
            message.setDeduplicationState(false);
            return message;
        }

        if (rules == null || rules.length == 0) {
            log.info("rules is null");
            message.setDeduplicationState(true);
            return message;
        }



        Pair<HashSet<String>, Long> pair = getUniqueFieldNamesFromRule(rules);

        log.info("time is {}", pair.value());

        String key = getStringForKey(message, pair.key());

        if (key == null) {
            message.setDeduplicationState(false);
            return message;
        }

        message.setDeduplicationState(redis.canPass(key, pair.value()));

        return message;
    }

    private Pair<HashSet<String>, Long> getUniqueFieldNamesFromRule(Rule[] rules) {
        Long maxTime = -10L;
        HashSet<String> set = new HashSet<>();
        for (Rule oneRule : rules) {
            if (Boolean.TRUE.equals(oneRule.getIsActive())) {
                set.add(oneRule.getFieldName());
                if (oneRule.getTimeToLiveSec() > maxTime) {
                    maxTime = oneRule.getTimeToLiveSec();
                }
            }
        }
        return new Pair<>(set, maxTime);
    }

    private String getStringForKey(Message message, HashSet<String> set) {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode messageInJson;
        try {
            messageInJson = objectMapper.readTree(message.getValue());
        } catch (JsonProcessingException e) {
            log.error("invalid json format");
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (String fieldNameInRules : set) {
            JsonNode nodeFromMessage = messageInJson.get(fieldNameInRules);
            if (nodeFromMessage == null) {
                log.error("there aren't such field!");
                return null;
            }

            String valueFromMessage = nodeFromMessage.asText();

            if (valueFromMessage == null || valueFromMessage.isEmpty()) {
                return null;
            }

            stringBuilder.append(valueFromMessage);

        }

        return stringBuilder.toString();
    }

}
