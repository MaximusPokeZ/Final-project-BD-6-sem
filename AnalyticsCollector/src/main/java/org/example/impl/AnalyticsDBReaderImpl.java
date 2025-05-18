package org.example.impl;

import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.example.model.RuleType;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.example.DbReader;
import org.example.model.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.jooq.impl.DSL.field;

@Slf4j
public class AnalyticsDBReaderImpl implements DbReader {

    static final String TABLE_NAME = "analytics_rules";

    private final DSLContext context;

    private final Integer intervalInSeconds;

    private final Integer serviceId;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final AtomicReference<Rule[]> currentRules = new AtomicReference<>();

    public AnalyticsDBReaderImpl(Config config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getString("db.jdbcUrl"));
        hikariConfig.setUsername(config.getString("db.user"));
        hikariConfig.setPassword(config.getString("db.password"));
        hikariConfig.setDriverClassName(config.getString("db.driver"));

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        this.context = DSL.using(dataSource, SQLDialect.POSTGRES);
        this.intervalInSeconds = config.getInt("application.updateIntervalSec");
        this.serviceId = config.getInt("application.serviceId");
        this.processing();
    }

    public void processing() {
        scheduler.scheduleAtFixedRate(() ->
        {
            try{
                this.currentRules.set(getRulesFromDb());
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        }, 0, intervalInSeconds, TimeUnit.SECONDS);
    }

    public Rule[] getRulesFromDb() {
        List<Rule> rules = new ArrayList<>();

        Result<Record> result = context.select()
                .from(TABLE_NAME)
                .where(field("service_id").eq(serviceId))
                .fetch();

        for (Record elem : result) {
            Rule rule = new Rule();
            rule.setServiceId(elem.get("service_id", Integer.class));
            String typeStr = elem.get("type", String.class);
            rule.setType(RuleType.valueOf(typeStr));
            rule.setModel(elem.get("model", String.class));
            rule.setWindowMinutes(elem.get("window_minutes", Integer.class));
            rule.setDefaultValue(elem.get("default_value", String.class));

            rules.add(rule);
        }

        log.info("Read RULES FROM DB for service_id {}: {}", serviceId, rules);

        return rules.toArray(new Rule[0]);
    }


    @Override
    public Rule[] readRulesFromDB() {
        return currentRules.get();
    }
}
