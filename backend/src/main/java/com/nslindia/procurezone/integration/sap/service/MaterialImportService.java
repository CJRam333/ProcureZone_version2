package com.nslindia.procurezone.integration.sap.service;

import com.nslindia.procurezone.integration.sap.dto.MaterialCsvRow;
import com.nslindia.procurezone.integration.sap.dto.MaterialImportResult;
import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Plant;
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.mapping.CompanyPlantMaterialMap;
import com.nslindia.procurezone.mapping.CompanyPlantMaterialMapRepository;
import com.nslindia.procurezone.masterdata.repository.CompanyRepository;
import com.nslindia.procurezone.masterdata.PlantRepository;
import com.nslindia.procurezone.masterdata.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for importing SAP material data from CSV files
 * Implements the business logic from legacy SapCsvImport class
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialImportService {

    private final CompanyRepository companyRepository;
    private final PlantRepository plantRepository;
    private final MaterialRepository materialRepository;
    // Writes the AUTHORITATIVE stock table tbl_map_company_plant_material (map_quantity_stores) —
    // the same table the dropdown, Inventory view and issue-note stock check read. Previously this
    // wrote tbl_pz_map_company_plant_material (CompanyPlantMaterial), which nothing reads for stock.
    private final CompanyPlantMaterialMapRepository companyPlantMaterialMapRepository;

    private static final Integer SYSTEM_USER_ID = 1; // System user for automated imports

    /**
     * Import materials from CSV rows
     * Main business logic matching legacy
     * SapCsvImport.InsertMaterialQuantityDetails()
     */
    @Transactional
    public MaterialImportResult importMaterials(List<MaterialCsvRow> rows, String filename) {
        log.info("Starting material import from {} with {} rows", filename, rows.size());

        MaterialImportResult result = MaterialImportResult.builder()
                .importDate(LocalDateTime.now())
                .filename(filename)
                .totalRows(rows.size())
                .build();

        for (MaterialCsvRow row : rows) {
            try {
                processRow(row, result);
            } catch (Exception e) {
                String error = String.format("Row %d: %s - %s",
                        row.getRowNumber(), e.getClass().getSimpleName(), e.getMessage());
                result.addError(error);
                log.error("Error processing row {}: {}", row.getRowNumber(), error, e);
            }
        }

        result.setSuccess(result.getFailures() < result.getTotalRows());
        result.setMessage(result.getSummary());

        log.info("Material import completed: {}", result.getSummary());
        return result;
    }

    /**
     * Process a single CSV row
     * Logic from legacy: lookup company/plant, create/update material, update
     * mapping
     */
    private void processRow(MaterialCsvRow row, MaterialImportResult result) {
        // Step 1: Lookup Company by code
        Company company = companyRepository.findByCode(row.getCompanyCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Company not found with code: " + row.getCompanyCode()));

        // Step 2: Lookup Plant by code
        Plant plant = plantRepository.findByCode(row.getPlantCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plant not found with code: " + row.getPlantCode()));

        // Step 3: Check if Material exists, create if not
        Material material = findOrCreateMaterial(row);
        boolean materialCreated = material.getId() == null;

        // Step 4: Create or update Company-Plant-Material mapping
        updateCompanyPlantMaterialMapping(company, plant, material, row.getQuantity());

        // Update result counters
        if (materialCreated) {
            result.setSuccessfulInserts(result.getSuccessfulInserts() + 1);
        } else {
            result.setSuccessfulUpdates(result.getSuccessfulUpdates() + 1);
        }
    }

    /**
     * Find existing material by code, or create new one
     * Logic from legacy: if material doesn't exist, insert with code as name/desc
     */
    private Material findOrCreateMaterial(MaterialCsvRow row) {
        Optional<Material> existing = materialRepository.findByCode(row.getMaterialCode());

        if (existing.isPresent()) {
            log.debug("Found existing material: {}", row.getMaterialCode());
            return existing.get();
        }

        // Create new material (matching legacy logic)
        log.info("Creating new material: {}", row.getMaterialCode());
        Material newMaterial = new Material();
        newMaterial.setCode(row.getMaterialCode());
        newMaterial.setName(row.getMaterialCode()); // Use code as name (legacy behavior)
        newMaterial.setDescription(row.getMaterialCode()); // Use code as desc (legacy behavior)
        newMaterial.setStatus(1); // Active
        newMaterial.setLastModifiedBy(SYSTEM_USER_ID);
        newMaterial.setLastModifiedDate(LocalDate.now());

        return materialRepository.save(newMaterial);
    }

    /**
     * Create or update Company-Plant-Material mapping with quantity
     * Logic from legacy: lookup mapping, create if doesn't exist, update quantity
     */
    private void updateCompanyPlantMaterialMapping(Company company, Plant plant,
            Material material, BigDecimal quantity) {
        Optional<CompanyPlantMaterialMap> existingMapping = companyPlantMaterialMapRepository
                .findByCompanyAndPlantAndMaterial(
                        company.getId(), plant.getId(), material.getId());

        CompanyPlantMaterialMap mapping;

        if (existingMapping.isPresent()) {
            // Update existing mapping
            mapping = existingMapping.get();
            log.debug("Updating existing mapping: company={}, plant={}, material={}",
                    company.getId(), plant.getId(), material.getId());
        } else {
            // Create new mapping (uses entity relationships for map_comp/map_plant/map_material)
            log.info("Creating new mapping: company={}, plant={}, material={}",
                    company.getId(), plant.getId(), material.getId());
            mapping = new CompanyPlantMaterialMap();
            mapping.setCompany(company);
            mapping.setPlant(plant);
            mapping.setMaterial(material);
            mapping.setStatus(1); // Active
            mapping.setLastModifiedBy(SYSTEM_USER_ID);
        }

        // Absolute overwrite of stock to the CSV value + stamp last modified date
        mapping.setQuantity(quantity);
        mapping.setLastModifiedDate(LocalDate.now());
        if (mapping.getLastModifiedBy() == null) {
            mapping.setLastModifiedBy(SYSTEM_USER_ID);
        }

        companyPlantMaterialMapRepository.save(mapping);
    }

    /**
     * Import materials from file path (for scheduled jobs)
     */
    @Transactional
    public MaterialImportResult importMaterialsFromFile(String filePath) {
        log.info("Importing materials from file: {}", filePath);

        try {
            CsvParserService parser = new CsvParserService();
            List<MaterialCsvRow> rows = parser.parseMaterialCsvFromPath(filePath);
            return importMaterials(rows, filePath);
        } catch (Exception e) {
            log.error("Failed to import materials from file: {}", filePath, e);

            MaterialImportResult errorResult = MaterialImportResult.builder()
                    .importDate(LocalDateTime.now())
                    .filename(filePath)
                    .success(false)
                    .message("Import failed: " + e.getMessage())
                    .build();
            errorResult.addError("File processing error: " + e.getMessage());

            return errorResult;
        }
    }
}
