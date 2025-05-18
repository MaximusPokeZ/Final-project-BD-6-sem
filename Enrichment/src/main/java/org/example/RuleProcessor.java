package org.example;

import org.example.model.Message;
import org.example.model.Rule;

public interface RuleProcessor {
    Message processing(Message message, Rule[] rules);
}
