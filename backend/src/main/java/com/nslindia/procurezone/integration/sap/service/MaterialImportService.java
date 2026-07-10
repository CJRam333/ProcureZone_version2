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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    // Self-reference (proxied) so per-row importSingleRow() runs through the Spring proxy and its
    // @Transactional(REQUIRES_NEW) takes effect. @Lazy breaks the construction-time self cycle.
    @Autowired
    @Lazy
    private MaterialImportService self;

    private static final Integer SYSTEM_USER_ID = 1; // System user for automated imports

    /**
     * Import materials from CSV rows
     * Main business logic matching legacy
     * SapCsvImport.InsertMaterialQuantityDetails()
     */
    /**
     * NOT @Transactional — orchestrates only. Each row is imported in its OWN transaction
     * (importSingleRow, REQUIRES_NEW), so a single failing row rolls back only itself; the good
     * rows are already committed. This is what stops "3 bad rows revert all 562".
     */
    public MaterialImportResult importMaterials(List<MaterialCsvRow> rows, String filename) {
        log.info("Starting material import from {} with {} rows", filename, rows.size());

        MaterialImportResult result = MaterialImportResult.builder()
                .importDate(LocalDateTime.now())
                .filename(filename)
                .totalRows(rows.size())
                .build();

        for (MaterialCsvRow row : rows) {
            try {
                // Call via the proxy so REQUIRES_NEW applies — each row commits independently.
                self.importSingleRow(row, result);
            } catch (Exception e) {
                String error = String.format("Row %d (material %s): %s - %s",
                        row.getRowNumber(), row.getMaterialCode(),
                        e.getClass().getSimpleName(), e.getMessage());
                result.addError(error);
                log.error("SAP import: row {} failed (material {}): {}",
                        row.getRowNumber(), row.getMaterialCode(), error);
            }
        }

        result.setSuccess(result.getFailures() < result.getTotalRows());
        result.setMessage(result.getSummary());

        log.info("Material import completed: {}", result.getSummary());
        return result;
    }

    /**
     * Import a single CSV row in its OWN transaction (REQUIRES_NEW) so its failure cannot mark the
     * batch rollback-only. Public so the Spring proxy applies the propagation when called via {@code self}.
     * Logic from legacy: lookup company/plant, create/update material, update mapping.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void importSingleRow(MaterialCsvRow row, MaterialImportResult result) {
        // Step 1: Lookup Company by code
        Company company = companyRepository.findByCode(row.getCompanyCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Company not found with code: " + row.getCompanyCode()));

        // Step 2: Lookup Plant by code
        Plant plant = plantRepository.findByCode(row.getPlantCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plant not found with code: " + row.getPlantCode()));

        // Step 3: Check if Material exists, create if not (first-match tolerant of duplicate codes)
        boolean materialExisted = materialRepository.findFirstByCodeOrderByIdAsc(row.getMaterialCode()).isPresent();
        Material material = findOrCreateMaterial(row);

        // Step 4: Create or update Company-Plant-Material mapping
        updateCompanyPlantMaterialMapping(company, plant, material, row.getQuantity());

        // Update result counters (guarded — mutated from the orchestrator's object across tx)
        if (materialExisted) {
            result.setSuccessfulUpdates(result.getSuccessfulUpdates() + 1);
        } else {
            result.setSuccessfulInserts(result.getSuccessfulInserts() + 1);
        }
    }

    /**
     * Find existing material by code, or create new one
     * Logic from legacy: if material doesn't exist, insert with code as name/desc
     */
    private Material findOrCreateMaterial(MaterialCsvRow row) {
        // First-match tolerant: tbl_material_master may contain duplicate codes (auto-created by
        // prior imports), so findByCode's single-result would throw NonUniqueResultException.
        Optional<Material> existing = materialRepository.findFirstByCodeOrderByIdAsc(row.getMaterialCode());

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
        // Duplicate-tolerant lookup — never throws on multiple rows for the same triple.
        List<CompanyPlantMaterialMap> existing = companyPlantMaterialMapRepository
                .findAllByCompanyAndPlantAndMaterial(
                        company.getId(), plant.getId(), material.getId());

        CompanyPlantMaterialMap mapping;

        if (existing.isEmpty()) {
            // Create new mapping (uses entity relationships for map_comp/map_plant/map_material)
            log.info("Creating new mapping: company={}, plant={}, material={}",
                    company.getId(), plant.getId(), material.getId());
            mapping = new CompanyPlantMaterialMap();
            mapping.setCompany(company);
            mapping.setPlant(plant);
            mapping.setMaterial(material);
            mapping.setStatus(1); // Active
            mapping.setLastModifiedBy(SYSTEM_USER_ID);
        } else {
            if (existing.size() > 1) {
                // Pre-existing data duplication — do NOT throw. Update the primary (lowest-id) row.
                // (Cleaning the extras is a separate task.)
                log.warn("SAP import: {} duplicate stock rows for company={}, plant={}, material={} — "
                                + "updating the primary (map_id={}) only; extras left untouched",
                        existing.size(), company.getId(), plant.getId(), material.getId(),
                        existing.get(0).getId());
            } else {
                log.debug("Updating existing mapping: company={}, plant={}, material={}",
                        company.getId(), plant.getId(), material.getId());
            }
            mapping = existing.get(0); // ORDER BY id ASC → primary row
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
