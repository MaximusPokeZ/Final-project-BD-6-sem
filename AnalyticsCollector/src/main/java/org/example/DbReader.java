package org.example;


import org.example.model.Rule;

public interface DbReader {
    Rule[] readRulesFromDB(); // метод получает набор правил из БД PostgreSQL. Конфигурация для подключения из файла *.conf. Метод также должен проверять в заданное время с периодом изменения в БД и обновлять правила.
}
