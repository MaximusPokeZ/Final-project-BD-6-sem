package org.example.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.RuleProcessor;
import org.example.model.Message;
import org.example.model.Rule;

import java.util.Arrays;

@Slf4j
public class FilteringByRuleProcessor implements RuleProcessor {
    @Override
    public Message processing(Message message, Rule[] rules) throws UnsupportedOperationException {

        log.info("%%%%%%% message {}", message);
        log.info("%%%% rules {}", Arrays.toString(rules));

        if (message == null ) {
            log.info("Message is null or empty");
            return null;
        }

        if (message.getValue() != null && message.getValue().isEmpty()) {
            log.info("Message is empty");
            message.setFilterState(false);
            return message;
        }

        if (rules == null || rules.length == 0) {
            log.info("Rules is null or empty");
            message.setFilterState(false);
            return message;
        }

        ObjectMapper mapper = new ObjectMapper();

        for (Rule rule : rules) {
                boolean result = applyTheRule(message, rule, mapper);
                if (!result) {
                    message.setFilterState(false);
                    return message;
                }
        }
        message.setFilterState(true);
        return message;
    }

    private boolean applyTheRule(Message message, Rule rule, ObjectMapper mapper) throws UnsupportedOperationException {
        String fieldNameFromRule = rule.getFieldName();
        String valueFromRule = rule.getFilterValue();
        String funcFromRule = rule.getFilterFunctionName();

        JsonNode messageInJson;
        try {
            messageInJson = mapper.readTree(message.getValue());
        } catch (JsonProcessingException e) {
            log.error("invalid json format");
            return false;
        }
        JsonNode nodeFromMessage = messageInJson.get(fieldNameFromRule);

        if(nodeFromMessage == null) {
            return false;
        }

        String valueFromMessage = nodeFromMessage.asText();

        if (valueFromRule == null || valueFromMessage.isEmpty() ) {
            return false;
        }

        return switch (funcFromRule) {
            case "equals" -> valueFromMessage.equals(valueFromRule);
            case "not_equals" -> !valueFromMessage.equals(valueFromRule);
            case "contains" -> valueFromMessage.contains(valueFromRule);
            case "not_contains" -> !valueFromMessage.contains(valueFromRule);
            default -> throw new UnsupportedOperationException("your operation is not supported! (can operate only equals, not_equals, contains, not_contains)");
        };
    }
}
