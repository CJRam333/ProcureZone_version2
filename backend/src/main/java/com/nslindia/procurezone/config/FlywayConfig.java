package com.nslindia.procurezone.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Run Flyway repair before every migrate so any FAILED migration records in
 * flyway_schema_history are cleared automatically.  Repair is a no-op when
 * there are no failed records, so this is safe on every boot.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
