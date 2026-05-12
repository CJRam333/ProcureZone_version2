package com.nslindia.procurezone.sap.scheduler;

import com.nslindia.procurezone.sap.dto.SapImportResult;
import com.nslindia.procurezone.sap.service.SapImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scheduler for SAP Material Master Import
 * Runs daily at 1:30 AM (like legacy Quartz cron: 0 30 1 * * ?)
 * 
 * @author NSL India
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SapImportScheduler {

    private final SapImportService sapImportService;

    @Value("${app.sap.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${app.sap.import.path:/opt/tomcat/webapps/ProcureZone/uploads/issue}")
    private String importPath;

    /**
     * Scheduled SAP import - runs daily at 1:30 AM
     * Cron: 0 30 1 * * ? (second minute hour day month day-of-week)
     * Legacy equivalent: 0 30 1 * * ? from SapSchedulerListenerPlant.java
     */
    @Scheduled(cron = "${app.sap.scheduler.cron:0 30 1 * * ?}")
    public void runScheduledImport() {
        if (!schedulerEnabled) {
            log.debug("SAP import scheduler is disabled");
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        log.info("Starting scheduled SAP import at {}",
                startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            // Check if another import is already running
            if (sapImportService.isImportInProgress()) {
                log.warn("Skipping scheduled import - another import is already in progress");
                return;
            }

            // Run the import
            SapImportResult result = sapImportService.runScheduledImport();

            if (result.isSuccess()) {
                log.info("Scheduled SAP import completed successfully. Records: {} processed, {} inserted",
                        result.getRecordsProcessed(), result.getRecordsInserted());
            } else {
                log.error("Scheduled SAP import failed: {}", result.getErrorDetails());
            }

        } catch (Exception e) {
            log.error("Error during scheduled SAP import", e);
        }

        LocalDateTime endTime = LocalDateTime.now();
        log.info("Scheduled SAP import ended at {}",
                endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Health check - runs every hour to log scheduler status
     */
    @Scheduled(cron = "0 0 * * * ?") // Every hour
    public void logSchedulerStatus() {
        if (!schedulerEnabled) {
            return;
        }

        try {
            long materialCount = sapImportService.getMaterialCount();
            var latestImport = sapImportService.getLatestSuccessfulImport();

            if (latestImport.isPresent()) {
                log.info("SAP Import Status: {} materials loaded, last successful import at {}",
                        materialCount,
                        latestImport.get().getCompletedAt());
            } else {
                log.info("SAP Import Status: {} materials loaded, no successful imports recorded",
                        materialCount);
            }
        } catch (Exception e) {
            log.debug("Error checking SAP import status: {}", e.getMessage());
        }
    }
}
