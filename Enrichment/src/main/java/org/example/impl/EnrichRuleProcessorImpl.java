package org.example.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.MongoDBClientEnricher;
import org.example.RuleProcessor;
import org.example.model.Message;

import org.example.model.Quatrifollio;
import org.example.model.Rule;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EnrichRuleProcessorImpl implements RuleProcessor {


    private final MongoDBClientEnricher mongo;

    public EnrichRuleProcessorImpl(Config config) {
        mongo = new EnrichMongoDBClientImpl(config);
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

            return message;
        }

        if (rules == null || rules.length == 0) {
            log.info("rules is null");

            return message;
        }

        Map<String, Quatrifollio<Long, String, String, String>> mapFieldRuleId = getMapPriorityByRuleId(rules);


        log.info(mapFieldRuleId.toString());

        JsonNode messageInJson = getJsonNodeFromMessage(message);

        if (messageInJson == null) {
            log.info("some error with parse json");
            return null;
        }

        log.info(messageInJson.toString());

        mongo.enrichMessage(messageInJson, mapFieldRuleId);

        log.info("ENRICHMENT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");


        return new Message(messageInJson.toString());

    }

    private Map<String, Quatrifollio<Long, String, String, String>> getMapPriorityByRuleId(Rule[] rules) {
        Map<String, Quatrifollio<Long, String, String, String>> mapPriority = new HashMap<>();
        for (Rule oneRule : rules) {
            String fieldNameFromRule = oneRule.getFieldName();
            Long ruleId = oneRule.getRuleId();
            String fieldNameEnrichment = oneRule.getFieldNameEnrichment();
            String fieldValue = oneRule.getFieldValue();
            String fieldNameDefault = oneRule.getFieldValueDefault();


            Quatrifollio<Long, String, String, String> current = mapPriority.get(fieldNameFromRule);

            if (current == null || ruleId > current.ruleId()) {
                mapPriority.put(fieldNameFromRule, new Quatrifollio<>(ruleId, fieldNameEnrichment, fieldValue, fieldNameDefault));
            }
        }

        return mapPriority;
    }

    private JsonNode getJsonNodeFromMessage(Message message) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode messageInJson;
        try {
            messageInJson = objectMapper.readTree(message.getValue());
        } catch (JsonProcessingException e) {
            log.error("invalid json format");
            return null;
        }
        return messageInJson;
    }

}
