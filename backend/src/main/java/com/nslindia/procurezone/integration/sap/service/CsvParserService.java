package com.nslindia.procurezone.integration.sap.service;

import com.nslindia.procurezone.integration.sap.dto.MaterialCsvRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing SAP Material CSV files
 * Expected format:
 * CompanyCode,PlantCode,MaterialCode,MaterialDescription,Quantity
 */
@Service
@Slf4j
public class CsvParserService {

    private static final int EXPECTED_COLUMNS = 5;

    /**
     * Parse material CSV from uploaded file
     */
    public List<MaterialCsvRow> parseMaterialCsv(MultipartFile file) throws IOException {
        log.info("Parsing material CSV file: {}", file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            return parseMaterialCsv(inputStream, file.getOriginalFilename());
        }
    }

    /**
     * Parse material CSV from file path (for scheduled jobs)
     */
    public List<MaterialCsvRow> parseMaterialCsvFromPath(String filePath) throws IOException {
        log.info("Parsing material CSV file from path: {}", filePath);

        try (FileInputStream fis = new FileInputStream(filePath)) {
            return parseMaterialCsv(fis, filePath);
        }
    }

    /**
     * Parse material CSV from input stream
     */
    private List<MaterialCsvRow> parseMaterialCsv(InputStream inputStream, String source) throws IOException {
        List<MaterialCsvRow> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                        .setSkipHeaderRecord(false)
                        .setTrim(true)
                        .build())) {

            int rowNum = 0;
            for (CSVRecord record : csvParser) {
                rowNum++;

                // Skip empty rows
                if (isEmptyRecord(record)) {
                    log.debug("Skipping empty row {}", rowNum);
                    continue;
                }

                // Validate column count
                if (record.size() < EXPECTED_COLUMNS) {
                    log.warn("Row {} has insufficient columns ({}), expected {}",
                            rowNum, record.size(), EXPECTED_COLUMNS);
                    continue;
                }

                try {
                    MaterialCsvRow row = parseRow(record, rowNum);
                    rows.add(row);
                } catch (Exception e) {
                    log.error("Error parsing row {}: {}", rowNum, e.getMessage());
                    // Continue processing other rows
                }
            }
        }

        log.info("Parsed {} valid rows from {}", rows.size(), source);
        return rows;
    }

    /**
     * Parse a single CSV record into MaterialCsvRow
     */
    private MaterialCsvRow parseRow(CSVRecord record, int rowNum) {
        String companyCode = record.get(0).trim();
        String plantCode = record.get(1).trim();
        String materialCode = record.get(2).trim();
        String materialDescription = record.get(3).trim();
        String quantityStr = record.get(4).trim();

        // Validate required fields
        if (companyCode.isEmpty() || plantCode.isEmpty() || materialCode.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required fields: company, plant, or material code");
        }

        // Parse quantity
        BigDecimal quantity;
        try {
            quantity = new BigDecimal(quantityStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid quantity '{}' at row {}, defaulting to 0", quantityStr, rowNum);
            quantity = BigDecimal.ZERO;
        }

        return MaterialCsvRow.builder()
                .companyCode(companyCode)
                .plantCode(plantCode)
                .materialCode(materialCode)
                .materialDescription(materialDescription)
                .quantity(quantity)
                .rowNumber(rowNum)
                .build();
    }

    /**
     * Check if a CSV record is empty
     */
    private boolean isEmptyRecord(CSVRecord record) {
        for (int i = 0; i < record.size(); i++) {
            String value = record.get(i);
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validate CSV file format
     */
    public void validateCsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be a CSV file");
        }

        // Check file size (max 10 MB)
        long maxSize = 10 * 1024 * 1024; // 10 MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }
    }
}
