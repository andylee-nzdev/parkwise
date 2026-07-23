package com.andy.parkwise.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** On startup, seed the DB from AT if it is empty (so a fresh clone works out of the box). */
@Component
public class StartupIngestRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupIngestRunner.class);

    private final JdbcClient jdbc;
    private final IngestionService ingestion;

    public StartupIngestRunner(JdbcClient jdbc, IngestionService ingestion) {
        this.jdbc = jdbc;
        this.ingestion = ingestion;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long count = jdbc.sql("SELECT count(*) FROM car_park").query(Long.class).single();
        if (count == 0) {
            log.info("car_park empty — running initial ingestion from AT");
            ingestion.ingestAll();
        } else {
            log.info("car_park already has {} rows — skipping startup ingestion", count);
        }
    }
}
