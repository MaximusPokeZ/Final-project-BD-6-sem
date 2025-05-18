package org.example;

import org.example.model.Message;
import org.example.model.Rule;

public interface RuleProcessor {
    public Message processing(Message message, Rule[] rules); // применяет правила фильтрации к сообщениям и устанавливает в них filterState значение true, если сообщение удовлетворяет условиям всех правил.
}
