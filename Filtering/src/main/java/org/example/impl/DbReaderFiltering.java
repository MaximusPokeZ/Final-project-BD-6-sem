package org.example.impl;

import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.example.DbReader;
import org.example.model.Rule;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class DbReaderFiltering implements DbReader {

    static final String TABLE_NAME = "filter_rules";

    private final DSLContext context;

    private final HikariDataSource dataSource;

    private final Integer interval;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final AtomicReference<Rule[]> currentRules = new AtomicReference<>();

    public DbReaderFiltering(Config config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getString("db.jdbcUrl"));
        hikariConfig.setUsername(config.getString("db.user"));
        hikariConfig.setPassword(config.getString("db.password"));
        hikariConfig.setDriverClassName(config.getString("db.driver"));

        this.dataSource = new HikariDataSource(hikariConfig);
        this.context = DSL.using(dataSource, SQLDialect.POSTGRES);
        this.interval = config.getInt("application.updateIntervalSec");
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

                }, 0, interval, TimeUnit.SECONDS);
    }

    public Rule[] getRulesFromDb() {

        List<Rule> rules = context.select()
                .from(TABLE_NAME)
                .fetchInto(Rule.class);

        log.info("read RULES FROM DB!!! {}",rules);

        return rules.toArray(new Rule[0]);
    }

    @Override
    public Rule[] readRulesFromDB() {
        return this.currentRules.get();
    }

    public void close() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
