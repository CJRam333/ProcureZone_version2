package com.nslindia.procurezone.sap.dto;

import lombok.*;

/**
 * DTO for SAP Import configuration
 * 
 * @author NSL India
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SapImportConfig {

    /**
     * Base path for SAP CSV files
     */
    private String basePath;

    /**
     * File name pattern for plant indent files
     * Default: Plant_Indent({date}).CSV
     */
    private String fileNamePattern;

    /**
     * File name pattern for quality info files (optional merge)
     * Default: Quality_Info({date}).CSV
     */
    private String qualityFilePattern;

    /**
     * Date format used in file names
     * Default: yyyy-MM-dd
     */
    private String dateFormat;

    /**
     * Whether to truncate table before import
     */
    private boolean truncateBeforeImport;

    /**
     * Plant ID filter (null for all plants)
     */
    private Integer plantId;

    /**
     * Import type identifier
     */
    private String importType; // SCHEDULED, MANUAL, API

    /**
     * User who triggered the import
     */
    private String triggeredBy;

    /**
     * Build default configuration
     */
    public static SapImportConfig defaults() {
        return SapImportConfig.builder()
                .fileNamePattern("Plant_Indent({date}).CSV")
                .qualityFilePattern("Quality_Info({date}).CSV")
                .dateFormat("yyyy-MM-dd")
                .truncateBeforeImport(true)
                .importType("MANUAL")
                .build();
    }

    /**
     * Build configuration for scheduled import
     */
    public static SapImportConfig scheduled(String basePath) {
        return SapImportConfig.builder()
                .basePath(basePath)
                .fileNamePattern("Plant_Indent({date}).CSV")
                .qualityFilePattern("Quality_Info({date}).CSV")
                .dateFormat("yyyy-MM-dd")
                .truncateBeforeImport(true)
                .importType("SCHEDULED")
                .triggeredBy("SYSTEM")
                .build();
    }
}
