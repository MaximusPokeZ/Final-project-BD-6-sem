package org.example.impl;

import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class DbReaderImpl implements DbReader {

    static final String TABLE_NAME = "deduplication_rules";

    private final DSLContext context;

    private final Integer intervalInSeconds;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final AtomicReference<Rule[]> currentRules = new AtomicReference<>();

    public DbReaderImpl(Config config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getString("db.jdbcUrl"));
        hikariConfig.setUsername(config.getString("db.user"));
        hikariConfig.setPassword(config.getString("db.password"));
        hikariConfig.setDriverClassName(config.getString("db.driver"));

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        this.context = DSL.using(dataSource, SQLDialect.POSTGRES);
        this.intervalInSeconds = config.getInt("application.updateIntervalSec");
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

        Result<org.jooq.Record> result = context.select().from(TABLE_NAME).fetch();

        for (Record elem : result) {
            Rule rule = new Rule();
            rule.setDeduplicationId(elem.get("deduplication_id", Long.class));
            rule.setRuleId(elem.get("rule_id", Long.class));
            rule.setFieldName(elem.get("field_name", String.class));
            rule.setTimeToLiveSec(elem.get("time_to_live_sec", Long.class));
            rule.setIsActive(elem.get("is_active", Boolean.class));
            rules.add(rule);
        }

        log.info("read RULES FROM DB!!! {}",rules);

        return rules.toArray(new Rule[0]);
    }

    @Override
    public Rule[] readRulesFromDB() {
        return currentRules.get();
    }
}
