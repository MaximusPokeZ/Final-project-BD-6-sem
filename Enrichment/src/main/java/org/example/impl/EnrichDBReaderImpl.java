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

import static org.jooq.impl.DSL.field;

@Slf4j
public class EnrichDBReaderImpl implements DbReader {

    static final String TABLE_NAME = "enrichment_rules";

    private final DSLContext context;

    private final HikariDataSource dataSource;

    private final Integer intervalInSeconds;

    private final Integer enrichmentId;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final AtomicReference<Rule[]> currentRules = new AtomicReference<>();

    public EnrichDBReaderImpl(Config config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getString("db.jdbcUrl"));
        hikariConfig.setUsername(config.getString("db.user"));
        hikariConfig.setPassword(config.getString("db.password"));
        hikariConfig.setDriverClassName(config.getString("db.driver"));

        this.dataSource = new HikariDataSource(hikariConfig);
        this.context = DSL.using(dataSource, SQLDialect.POSTGRES);
        this.intervalInSeconds = config.getInt("application.updateIntervalSec");
        this.enrichmentId = config.getInt("application.enrichmentId");
        this.processing();
    }

    public void processing() {
        scheduler.scheduleAtFixedRate(() ->
        {
            if (!dataSource.isClosed()) {
                try {
                    log.info("!!!! update rules");
                    this.currentRules.set(getRulesFromDb());
                } catch ( Exception e ) {
                    log.error(e.getMessage());
                }
            }

        }, 0, intervalInSeconds, TimeUnit.SECONDS);
    }

    public Rule[] getRulesFromDb() {
        List<Rule> rules = new ArrayList<>();

        log.info("sql query!!!");
        Result<org.jooq.Record> result = context.select().from(TABLE_NAME).where(field("enrichment_id").eq(enrichmentId)).fetch();
        log.info("sql query done!");
        for (Record elem : result) {
            Rule rule = new Rule();
            rule.setEnricherId(elem.get("enrichment_id", Long.class));
            rule.setRuleId(elem.get("rule_id", Long.class));
            rule.setFieldName(elem.get("field_name", String.class));
            rule.setFieldNameEnrichment(elem.get("field_name_enrichment", String.class));
            rule.setFieldValue(elem.get("field_value", String.class));
            rule.setFieldValueDefault(elem.get("field_value_default",String.class));
            rules.add(rule);
        }

        log.info("read RULES FROM DB by your enrichment_id!!! {}",rules);

        return rules.toArray(new Rule[0]);
    }

    @Override
    public Rule[] readRulesFromDB() {
        return currentRules.get();
    }
}
