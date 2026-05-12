package com.nslindia.procurezone.scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for Spring Scheduler
 * Enables scheduled jobs for SAP CSV import automation
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Spring will automatically detect @Scheduled methods
}
