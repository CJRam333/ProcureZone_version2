package com.nslindia.procurezone.scheduler.job;

import com.nslindia.procurezone.integration.sap.dto.MaterialImportResult;
import com.nslindia.procurezone.integration.sap.service.MaterialImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scheduled job for SAP Material CSV Import
 * Matches legacy SchedulerJob.java and Job1.java functionality
 * 
 * Runs at:
 * - 10:21 AM daily (Trigger 1)
 * - 11:55 AM daily (Trigger 2)
 * - 10:43 AM daily (Job1)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SapMaterialImportJob {

    private final MaterialImportService materialImportService;

    @Value("${sap.csv.import.path:/opt/tomcat/uploads/issue}")
    private String csvImportPath;

    @Value("${sap.csv.archive.path:/opt/tomcat/uploads/issue/archive}")
    private String csvArchivePath;

    @Value("${sap.csv.archive.enabled:true}")
    private boolean archiveEnabled;

    @Value("${sap.csv.import.enabled:true}")
    private boolean importEnabled;

    /**
     * SAP CSV Import Job - Trigger 1
     * Runs daily at 10:21 AM (matching legacy cron: 0 21 10 * * ?)
     */
    @Scheduled(cron = "0 21 10 * * ?", zone = "Asia/Kolkata")
    public void importSapMaterialsTrigger1() {
        if (!importEnabled) {
            log.info("SAP material import is disabled");
            return;
        }

        log.info("=== SAP Material Import Job - Trigger 1 started at 10:21 AM ===");
        executeImport("Trigger 1 (10:21 AM)");
    }

    /**
     * SAP CSV Import Job - Trigger 2
     * Runs daily at 11:55 AM (matching legacy cron: 0 55 11 * * ?)
     */
    @Scheduled(cron = "0 55 11 * * ?", zone = "Asia/Kolkata")
    public void importSapMaterialsTrigger2() {
        if (!importEnabled) {
            log.info("SAP material import is disabled");
            return;
        }

        log.info("=== SAP Material Import Job - Trigger 2 started at 11:55 AM ===");
        executeImport("Trigger 2 (11:55 AM)");
    }

    /**
     * Material Quantity Insert Job
     * Runs daily at 10:43 AM (matching legacy Job1 cron: 0 43 10 * * ?)
     */
    @Scheduled(cron = "0 43 10 * * ?", zone = "Asia/Kolkata")
    public void insertMaterialQuantity() {
        if (!importEnabled) {
            log.info("Material quantity insert is disabled");
            return;
        }

        log.info("=== Material Quantity Insert Job started at 10:43 AM ===");
        executeImport("Material Quantity Insert (10:43 AM)");
    }

    /**
     * Execute the SAP material import
     * Matches legacy: SapCsvImport.ExcelToDatabase() and
     * InsertMaterialQuantityDetails()
     */
    private void executeImport(String jobName) {
        try {
            // Build filename with current date: Material(YYYY-MM-DD).CSV
            String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String filename = String.format("Material(%s).CSV", dateStr);
            String fullPath = new File(csvImportPath, filename).getAbsolutePath();

            log.info("{}: Looking for CSV file at: {}", jobName, fullPath);

            File csvFile = new File(fullPath);
            if (!csvFile.exists()) {
                log.warn("{}: CSV file not found: {}", jobName, fullPath);
                return;
            }

            log.info("{}: Found CSV file, starting import...", jobName);

            // Execute import
            MaterialImportResult result = materialImportService.importMaterialsFromFile(fullPath);

            // Log results
            if (result.isSuccess()) {
                log.info("{}: Import completed successfully - {}", jobName, result.getSummary());
            } else {
                log.error("{}: Import completed with errors - {}", jobName, result.getSummary());
                log.error("{}: Errors: {}", jobName, result.getErrors());
            }

            // Archive file after processing
            if (archiveEnabled) {
                archiveFile(csvFile);
            }

        } catch (Exception e) {
            log.error("{}: Exception during import", jobName, e);
        }

        log.info("=== {} completed ===", jobName);
    }

    /**
     * Archive processed CSV file to archive directory with timestamp
     * Creates archive directory if it doesn't exist
     */
    private void archiveFile(File file) {
        try {
            // Create archive directory if it doesn't exist
            Path archiveDir = Path.of(csvArchivePath);
            if (!Files.exists(archiveDir)) {
                Files.createDirectories(archiveDir);
                log.info("Created archive directory: {}", archiveDir);
            }

            // Build archive filename with timestamp:
            // Material(2025-01-01)_processed_20250101_103045.CSV
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String originalName = file.getName();
            String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
            String extension = originalName.substring(originalName.lastIndexOf('.'));
            String archiveName = baseName + "_processed_" + timestamp + extension;

            Path sourcePath = file.toPath();
            Path targetPath = archiveDir.resolve(archiveName);

            // Move file to archive
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archived file: {} -> {}", file.getName(), targetPath);

        } catch (IOException e) {
            log.error("Failed to archive file {}: {}", file.getName(), e.getMessage());
        }
    }
}
