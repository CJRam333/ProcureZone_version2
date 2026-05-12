package com.nslindia.procurezone.sap.service;

import com.nslindia.procurezone.sap.dto.SapImportConfig;
import com.nslindia.procurezone.sap.dto.SapImportResult;
import com.nslindia.procurezone.sap.entity.SapImportLog;
import com.nslindia.procurezone.sap.entity.SapScheduleMaterial;
import com.nslindia.procurezone.sap.repository.SapImportLogRepository;
import com.nslindia.procurezone.sap.repository.SapScheduleMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for importing SAP Material Master data from CSV files
 * Replicates the legacy SapCsvImport functionality
 * 
 * CSV Format (51 columns) - MATCHES LEGACY ExcelToDatabase mapping:
 * 0: company_code (INT), 1: plant_code (INT), 2: storage_location, 3:
 * material_code,
 * 4: material_desc, 5: material_uom, 6: batch, 7: quantity, 8: material_type,
 * 9: material_group (INT), 10: material_group_desc, 11: variety_type, 12:
 * variety,
 * 13: crop_type, 14: crop_group, 15: stl, 16: odv, 17: got, 18: elisa, 19:
 * status,
 * 20: mat_code_id, 21: SDCLS, 22: STATS, 23: SKIPD, 24: INSPDT, 25: MOISTURE,
 * 26: PURE_SEED, 27: INERT_MATTER, 28: OCS_COUNT, 29: WEED_SEED_COUNT, 30:
 * GRAIN,
 * 31: BLACK_SEEDS, 32: PINHOLE_SEEDS, 33: ODV_RES, 34: BULK_DENSITY, 35: THSW,
 * 36: COLD_VIGOUR_GERM_NORMAL, 37: FIRST_COUNT_NORMAL, 38: GERM_NORMAL, 39:
 * FET_NORMAL,
 * 40: SOIL_COUNT_DAYS, 41: AAV_GERM_NORMAL, 42: GOT_GP, 43: GOT_FEMALE, 44:
 * GOT_OTHERS,
 * 45: BG1, 46: BG2, 47: HT, 48: FQR, 49: stp_one, 50: lmd
 * 
 * @author NSL India
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SapImportService {

    private final SapScheduleMaterialRepository materialRepository;
    private final SapImportLogRepository importLogRepository;

    @Value("${app.sap.import.path:/opt/tomcat/webapps/ProcureZone/uploads/issue}")
    private String defaultImportPath;

    @Value("${app.sap.import.enabled:true}")
    private boolean importEnabled;

    // CSV column indices - MATCHES LEGACY SapCsvImport.java ExcelToDatabase mapping
    // IMPORTANT: Legacy CSV does NOT have company_id in first column - starts with
    // company_code
    private static final int COL_COMPANY_CODE = 0; // col1 in legacy
    private static final int COL_PLANT_CODE = 1; // col2 in legacy
    private static final int COL_STORAGE_LOCATION = 2; // col3 in legacy
    private static final int COL_MATERIAL_CODE = 3; // col4 in legacy
    private static final int COL_MATERIAL_DESC = 4; // col5 in legacy
    private static final int COL_MATERIAL_UOM = 5; // col6 in legacy
    private static final int COL_BATCH = 6; // col7 in legacy (NOT quantity!)
    private static final int COL_QUANTITY = 7; // col8 in legacy
    private static final int COL_MATERIAL_TYPE = 8; // col9 in legacy
    private static final int COL_MATERIAL_GROUP = 9; // col10 in legacy
    private static final int COL_GROUP_DESC = 10; // col11 in legacy
    private static final int COL_VARIETY_TYPE = 11; // col12 in legacy
    private static final int COL_VARIETY = 12; // col13 in legacy
    private static final int COL_CROP_TYPE = 13; // col14 in legacy
    private static final int COL_CROP_GROUP = 14; // col15 in legacy
    private static final int COL_STL = 15; // col16 in legacy
    private static final int COL_ODV = 16; // col17 in legacy
    private static final int COL_GOT = 17; // col18 in legacy
    private static final int COL_ELISA = 18; // col19 in legacy
    private static final int COL_STATUS = 19; // col20 in legacy
    private static final int COL_MAT_CODE_ID = 20; // col21 in legacy
    // QC columns start from 21 onwards (SDCLS, STATS, SKIPD, INSPDT, MOISTURE,
    // etc.)

    /**
     * Import from uploaded CSV file
     */
    @Transactional
    public SapImportResult importFromFile(MultipartFile file, SapImportConfig config) {
        if (!importEnabled) {
            return SapImportResult.failure("SAP import is disabled", 0);
        }

        if (importLogRepository.isImportInProgress()) {
            return SapImportResult.failure("Another import is already in progress", 0);
        }

        // Create import log
        SapImportLog importLog = SapImportLog.builder()
                .importType(config.getImportType() != null ? config.getImportType() : "API")
                .fileName(file.getOriginalFilename())
                .triggeredBy(config.getTriggeredBy())
                .plantId(config.getPlantId())
                .build();
        importLog = importLogRepository.save(importLog);

        try {
            // Parse CSV content
            List<SapScheduleMaterial> materials = parseCsvFromInputStream(
                    file.getInputStream(), file.getOriginalFilename());

            // Perform import
            return executeImport(materials, config, importLog);

        } catch (Exception e) {
            log.error("Error importing SAP data from file: {}", file.getOriginalFilename(), e);
            importLog.markFailed(e.getMessage());
            importLogRepository.save(importLog);
            return SapImportResult.failure(e.getMessage(), 0);
        }
    }

    /**
     * Import from file path (for scheduled imports)
     */
    @Transactional
    public SapImportResult importFromPath(String filePath, SapImportConfig config) {
        if (!importEnabled) {
            return SapImportResult.failure("SAP import is disabled", 0);
        }

        if (importLogRepository.isImportInProgress()) {
            return SapImportResult.failure("Another import is already in progress", 0);
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            log.warn("SAP import file not found: {}", filePath);
            return SapImportResult.failure("File not found: " + filePath, 0);
        }

        // Create import log
        SapImportLog importLog = SapImportLog.builder()
                .importType(config.getImportType() != null ? config.getImportType() : "MANUAL")
                .fileName(path.getFileName().toString())
                .filePath(filePath)
                .triggeredBy(config.getTriggeredBy())
                .plantId(config.getPlantId())
                .build();
        importLog = importLogRepository.save(importLog);

        try {
            // Parse CSV content
            List<SapScheduleMaterial> materials = parseCsvFromPath(path);

            // Perform import
            return executeImport(materials, config, importLog);

        } catch (Exception e) {
            log.error("Error importing SAP data from path: {}", filePath, e);
            importLog.markFailed(e.getMessage());
            importLogRepository.save(importLog);
            return SapImportResult.failure(e.getMessage(), 0);
        }
    }

    /**
     * Import from default scheduled path
     */
    public SapImportResult runScheduledImport() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileName = "Plant_Indent(" + today + ").CSV";
        String fullPath = defaultImportPath + "/" + fileName;

        log.info("Running scheduled SAP import from: {}", fullPath);

        SapImportConfig config = SapImportConfig.scheduled(defaultImportPath);
        return importFromPath(fullPath, config);
    }

    /**
     * Execute the actual import
     */
    private SapImportResult executeImport(List<SapScheduleMaterial> materials,
            SapImportConfig config,
            SapImportLog importLog) {
        LocalDateTime startTime = LocalDateTime.now();
        int recordsProcessed = materials.size();
        int recordsInserted = 0;
        int recordsFailed = 0;

        try {
            // Truncate if configured
            if (config.isTruncateBeforeImport()) {
                log.info("Truncating SAP material table before import");
                materialRepository.truncateTable();
            }

            // Batch insert materials
            List<SapScheduleMaterial> batch = new ArrayList<>();
            for (SapScheduleMaterial material : materials) {
                batch.add(material);

                if (batch.size() >= 500) {
                    materialRepository.saveAll(batch);
                    recordsInserted += batch.size();
                    batch.clear();
                }
            }

            // Save remaining batch
            if (!batch.isEmpty()) {
                materialRepository.saveAll(batch);
                recordsInserted += batch.size();
            }

            // Update import log
            importLog.setRecordsProcessed(recordsProcessed);
            importLog.setRecordsInserted(recordsInserted);
            importLog.setRecordsFailed(recordsFailed);
            importLog.markCompleted();
            importLogRepository.save(importLog);

            log.info("SAP import completed: {} records processed, {} inserted",
                    recordsProcessed, recordsInserted);

            return SapImportResult.builder()
                    .success(true)
                    .message("Import completed successfully")
                    .recordsProcessed(recordsProcessed)
                    .recordsInserted(recordsInserted)
                    .recordsFailed(recordsFailed)
                    .fileName(importLog.getFileName())
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .logId(importLog.getId())
                    .build();

        } catch (Exception e) {
            log.error("Error during SAP import execution", e);
            importLog.markFailed(e.getMessage());
            importLogRepository.save(importLog);
            throw e;
        }
    }

    /**
     * Parse CSV from file path
     */
    private List<SapScheduleMaterial> parseCsvFromPath(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return parseCsv(reader, path.getFileName().toString());
        }
    }

    /**
     * Parse CSV from input stream
     */
    private List<SapScheduleMaterial> parseCsvFromInputStream(InputStream inputStream, String fileName)
            throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return parseCsv(reader, fileName);
        }
    }

    /**
     * Parse CSV content into SapScheduleMaterial entities
     * Follows legacy column mapping from SapCsvImport.java
     */
    private List<SapScheduleMaterial> parseCsv(BufferedReader reader, String fileName) throws IOException {
        List<SapScheduleMaterial> materials = new ArrayList<>();
        String line;
        int lineNumber = 0;
        LocalDateTime importTime = LocalDateTime.now();

        while ((line = reader.readLine()) != null) {
            lineNumber++;

            // Skip empty lines
            if (line.trim().isEmpty()) {
                continue;
            }

            try {
                String[] fields = line.split(",", -1); // -1 to keep empty trailing fields

                SapScheduleMaterial material = parseRow(fields, importTime);
                if (material != null) {
                    materials.add(material);
                }
            } catch (Exception e) {
                log.warn("Error parsing line {} in {}: {}", lineNumber, fileName, e.getMessage());
            }
        }

        log.info("Parsed {} records from {}", materials.size(), fileName);
        return materials;
    }

    /**
     * Parse a single CSV row into SapScheduleMaterial entity
     * Column mapping based on legacy SapCsvImport.java ExcelToDatabase method
     * CORRECTED indices to match legacy exactly
     */
    private SapScheduleMaterial parseRow(String[] fields, LocalDateTime importTime) {
        if (fields.length < 10) {
            return null; // Minimum required fields
        }

        try {
            SapScheduleMaterial.SapScheduleMaterialBuilder builder = SapScheduleMaterial.builder();

            // Core fields (0-8) - CORRECTED to match legacy CSV structure
            builder.companyCode(parseIntSafe(fields, COL_COMPANY_CODE)); // Index 0
            builder.plantCode(parseIntSafe(fields, COL_PLANT_CODE)); // Index 1
            builder.storageLocation(getStringSafe(fields, COL_STORAGE_LOCATION)); // Index 2
            builder.materialCode(getStringSafe(fields, COL_MATERIAL_CODE)); // Index 3
            builder.materialDesc(getStringSafe(fields, COL_MATERIAL_DESC)); // Index 4
            builder.materialUom(getStringSafe(fields, COL_MATERIAL_UOM)); // Index 5
            builder.batch(getStringSafe(fields, COL_BATCH)); // Index 6 (NOT quantity!)
            builder.quantity(parseDecimalSafe(fields, COL_QUANTITY)); // Index 7
            builder.materialType(getStringSafe(fields, COL_MATERIAL_TYPE)); // Index 8

            // Extended fields (9-20)
            if (fields.length > COL_MATERIAL_GROUP) {
                builder.materialGroup(parseIntSafe(fields, COL_MATERIAL_GROUP)); // Index 9
            }
            if (fields.length > COL_GROUP_DESC) {
                builder.materialGroupDesc(getStringSafe(fields, COL_GROUP_DESC)); // Index 10
            }
            if (fields.length > COL_VARIETY_TYPE) {
                builder.varietyType(getStringSafe(fields, COL_VARIETY_TYPE)); // Index 11
            }
            if (fields.length > COL_VARIETY) {
                builder.variety(getStringSafe(fields, COL_VARIETY)); // Index 12
            }
            if (fields.length > COL_CROP_TYPE) {
                builder.cropType(getStringSafe(fields, COL_CROP_TYPE)); // Index 13
            }
            if (fields.length > COL_CROP_GROUP) {
                builder.cropGroup(getStringSafe(fields, COL_CROP_GROUP)); // Index 14
            }
            if (fields.length > COL_STL) {
                builder.stl(getStringSafe(fields, COL_STL)); // Index 15
            }
            if (fields.length > COL_ODV) {
                builder.odv(getStringSafe(fields, COL_ODV)); // Index 16
            }
            if (fields.length > COL_GOT) {
                builder.got(getStringSafe(fields, COL_GOT)); // Index 17
            }
            if (fields.length > COL_ELISA) {
                builder.elisa(getStringSafe(fields, COL_ELISA)); // Index 18
            }
            if (fields.length > COL_STATUS) {
                Integer statusVal = parseIntSafe(fields, COL_STATUS); // Index 19
                builder.status(statusVal != null ? statusVal : 1);
            }
            if (fields.length > COL_MAT_CODE_ID) {
                builder.matCodeId(parseIntSafe(fields, COL_MAT_CODE_ID)); // Index 20
            }

            // QC Parameters (columns 21+) - CORRECTED indices
            if (fields.length > 21) {
                builder.sdcls(getStringSafe(fields, 21)); // SDCLS
            }
            if (fields.length > 22) {
                builder.stats(getStringSafe(fields, 22)); // STATS
            }
            if (fields.length > 23) {
                builder.skipd(getStringSafe(fields, 23)); // SKIPD
            }
            if (fields.length > 24) {
                builder.inspdt(getStringSafe(fields, 24)); // INSPDT
            }
            if (fields.length > 25) {
                builder.moisture(parseDecimalSafe(fields, 25)); // MOISTURE
            }
            if (fields.length > 26) {
                builder.pureSeed(parseDecimalSafe(fields, 26)); // PURE_SEED
            }
            if (fields.length > 27) {
                builder.inertMatter(parseDecimalSafe(fields, 27)); // INERT_MATTER
            }
            if (fields.length > 28) {
                builder.ocsCount(parseDecimalSafe(fields, 28)); // OCS_COUNT
            }
            if (fields.length > 29) {
                builder.weedSeedCount(parseDecimalSafe(fields, 29)); // WEED_SEED_COUNT
            }
            if (fields.length > 30) {
                builder.grain(parseDecimalSafe(fields, 30)); // GRAIN
            }
            if (fields.length > 31) {
                builder.blackSeeds(parseDecimalSafe(fields, 31)); // BLACK_SEEDS
            }
            if (fields.length > 32) {
                builder.pinholeSeeds(parseDecimalSafe(fields, 32)); // PINHOLE_SEEDS
            }
            if (fields.length > 33) {
                builder.odvRes(getStringSafe(fields, 33)); // ODV_RES
            }
            if (fields.length > 34) {
                builder.bulkDensity(parseDecimalSafe(fields, 34)); // BULK_DENSITY
            }
            if (fields.length > 35) {
                builder.thsw(parseDecimalSafe(fields, 35)); // THSW
            }
            if (fields.length > 36) {
                builder.coldVigourGermNormal(parseDecimalSafe(fields, 36)); // COLD_VIGOUR_GERM_NORMAL
            }
            if (fields.length > 37) {
                builder.firstCountNormal(parseDecimalSafe(fields, 37)); // FIRST_COUNT_NORMAL
            }
            if (fields.length > 38) {
                builder.germNormal(parseDecimalSafe(fields, 38)); // GERM_NORMAL
            }
            if (fields.length > 39) {
                builder.fetNormal(parseDecimalSafe(fields, 39)); // FET_NORMAL
            }
            if (fields.length > 40) {
                builder.soilCountDays(parseDecimalSafe(fields, 40)); // SOIL_COUNT_DAYS
            }
            if (fields.length > 41) {
                builder.aavGermNormal(parseDecimalSafe(fields, 41)); // AAV_GERM_NORMAL
            }
            if (fields.length > 42) {
                builder.gotGp(parseDecimalSafe(fields, 42)); // GOT_GP
            }
            if (fields.length > 43) {
                builder.gotFemale(parseDecimalSafe(fields, 43)); // GOT_FEMALE
            }
            if (fields.length > 44) {
                builder.gotOthers(parseDecimalSafe(fields, 44)); // GOT_OTHERS
            }
            if (fields.length > 45) {
                builder.bg1(getStringSafe(fields, 45)); // BG1
            }
            if (fields.length > 46) {
                builder.bg2(getStringSafe(fields, 46)); // BG2
            }
            if (fields.length > 47) {
                builder.ht(getStringSafe(fields, 47)); // HT
            }
            if (fields.length > 48) {
                builder.fqr(getStringSafe(fields, 48)); // FQR
            }
            if (fields.length > 49) {
                builder.stpOne(getStringSafe(fields, 49)); // stp_one
            }

            // Set import timestamp
            builder.lastModifiedDate(importTime);

            return builder.build();

        } catch (Exception e) {
            log.warn("Error building material entity: {}", e.getMessage());
            return null;
        }
    }

    // Helper methods for safe parsing

    private String getStringSafe(String[] fields, int index) {
        if (index >= fields.length) {
            return null;
        }
        String value = fields[index].trim();
        return value.isEmpty() ? null : value;
    }

    private Integer parseIntSafe(String[] fields, int index) {
        String value = getStringSafe(fields, index);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseDecimalSafe(String[] fields, int index) {
        String value = getStringSafe(fields, index);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    // ==================== STATUS & HISTORY METHODS ====================

    /**
     * Get current import status
     */
    public boolean isImportInProgress() {
        return importLogRepository.isImportInProgress();
    }

    /**
     * Get latest import log
     */
    public Optional<SapImportLog> getLatestImportLog() {
        return importLogRepository.findFirstByOrderByStartedAtDesc();
    }

    /**
     * Get latest successful import
     */
    public Optional<SapImportLog> getLatestSuccessfulImport() {
        return importLogRepository.findFirstByStatusOrderByStartedAtDesc("COMPLETED");
    }

    /**
     * Get import history
     */
    public List<SapImportLog> getImportHistory(int limit) {
        return importLogRepository.findAll()
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Get count of materials in SAP table
     */
    public long getMaterialCount() {
        return materialRepository.countAllRecords();
    }

    /**
     * Get count by plant
     */
    public long getMaterialCountByPlant(Integer plantCode) {
        return materialRepository.countByPlantCode(plantCode);
    }

    /**
     * Get distinct plant codes
     */
    public List<Integer> getDistinctPlantCodes() {
        return materialRepository.findDistinctPlantCodes();
    }

    /**
     * Search materials
     */
    public List<SapScheduleMaterial> searchMaterials(String query) {
        return materialRepository.searchByMaterialCodeOrDesc("%" + query + "%");
    }

    /**
     * Get materials by plant
     */
    public List<SapScheduleMaterial> getMaterialsByPlant(Integer plantCode) {
        return materialRepository.findByPlantCode(plantCode);
    }

    /**
     * Get available quantity
     */
    public BigDecimal getAvailableQuantity(Integer plantCode, String materialCode, String storageLocation) {
        return materialRepository.getAvailableQuantity(plantCode, materialCode, storageLocation);
    }
}
