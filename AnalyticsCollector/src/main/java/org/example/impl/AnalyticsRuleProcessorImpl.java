package org.example.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.RuleProcessor;
import org.example.model.Message;

import org.example.model.Rule;
import org.example.model.RuleType;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AnalyticsRuleProcessorImpl implements RuleProcessor {

    private static final long START_SECONDS = 5;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClickHouseClient houseClient;

    private final Instant startTime = Instant.now();

    public AnalyticsRuleProcessorImpl(Config config) {
        houseClient = new ClickHouseClient(config);
    }

    @Override
    public Message processing(Message message, Rule[] rules) {
        try {
            ObjectNode json = (ObjectNode) objectMapper.readTree(message.getValue());
            houseClient.insertRaw(message);

            if (Duration.between(startTime, Instant.now()).toSeconds() < START_SECONDS) {
                return message;
            }

            if (rules == null || rules.length == 0) {
                return message;
            }

            Map<RuleType, Rule> ruleMap = Arrays.stream(rules)
                    .collect(Collectors.toMap(
                            Rule::getType,
                            rule -> rule,
                            (oldRule, newRule) -> (oldRule.getWindowMinutes() > newRule.getWindowMinutes() ? oldRule : newRule)
                    ));

            ObjectNode result = objectMapper.createObjectNode();
            JsonNode modelNode = json.get("model");
            if (modelNode != null) {
                result.put("model", modelNode.asText());
            }
            for (Rule rule : ruleMap.values()) {
                Message analyticMessage = houseClient.analysis(rule, message);

                JsonNode analyticJson = objectMapper.readTree(analyticMessage.getValue());
                String typeKey = String.valueOf(rule.getType());
                result.set(typeKey, analyticJson.get(typeKey));
            }

            return Message.builder()
                    .value(objectMapper.writeValueAsString(result))
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Error during rule processing", e);
        }
        return null;
    }
}
