package com.nslindia.procurezone.report.service;

import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.Plant;
import com.nslindia.procurezone.masterdata.repository.CompanyRepository;
import com.nslindia.procurezone.masterdata.repository.MaterialRepository;
import com.nslindia.procurezone.masterdata.PlantRepository;
import com.nslindia.procurezone.mapping.entity.CompanyPlantMaterial;
import com.nslindia.procurezone.mapping.repository.CompanyPlantMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for generating Material Quantity Reports
 * Exports material stock levels by company and plant
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialQuantityReportService {

        private final CompanyPlantMaterialRepository companyPlantMaterialRepository;
        private final CompanyRepository companyRepository;
        private final PlantRepository plantRepository;
        private final MaterialRepository materialRepository;
        private final ExcelReportService excelReportService;

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        /**
         * Generate Material Quantity Report for all companies
         */
        public byte[] generateMaterialQuantityReport() throws IOException {
                log.info("Generating Material Quantity Report for all companies");

                List<Company> companies = companyRepository.findAll();

                Map<String, ExcelReportService.SheetData> sheets = new LinkedHashMap<>();

                for (Company company : companies) {
                        String sheetName = company.getCode();
                        ExcelReportService.SheetData sheetData = generateCompanySheet(company);
                        sheets.put(sheetName, sheetData);
                }

                // Add summary sheet
                sheets.put("Summary", generateSummarySheet());

                return excelReportService.generateMultiSheetExcelReport(sheets);
        }

        /**
         * Generate Material Quantity Report for specific company
         */
        public byte[] generateMaterialQuantityReportForCompany(String companyCode) throws IOException {
                log.info("Generating Material Quantity Report for company: {}", companyCode);

                Company company = companyRepository.findByCode(companyCode)
                                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyCode));

                ExcelReportService.SheetData sheetData = generateCompanySheet(company);

                List<String> headers = sheetData.getHeaders();
                List<Map<String, Object>> data = sheetData.getData();
                String title = String.format("Material Quantity Report - %s (%s)",
                                company.getName(), company.getCode());

                return excelReportService.generateExcelReport(headers, data,
                                company.getCode(), title);
        }

        /**
         * Generate sheet data for a company
         */
        private ExcelReportService.SheetData generateCompanySheet(Company company) {
                String title = String.format("Material Quantity Report - %s\nAs of: %s",
                                company.getName(),
                                LocalDate.now().format(DATE_FORMATTER));

                List<String> headers = Arrays.asList(
                                "S.No",
                                "Plant Code",
                                "Plant Name",
                                "Material Code",
                                "Material Name",
                                "Material Description",
                                "Quantity in Stores",
                                "Reorder Level",
                                "Min Stock",
                                "Max Stock",
                                "Status");

                List<Map<String, Object>> data = new ArrayList<>();

                // Get all active mappings for this company
                List<CompanyPlantMaterial> mappings = companyPlantMaterialRepository.findAllActiveList()
                                .stream()
                                .filter(m -> m.getCompanyId().equals(company.getId()))
                                .toList();

                int serialNo = 1;
                for (CompanyPlantMaterial mapping : mappings) {
                        Map<String, Object> row = new LinkedHashMap<>();

                        // Fetch related entities using IDs
                        Plant plant = plantRepository.findById(mapping.getPlantId()).orElse(null);
                        Material material = materialRepository.findById(mapping.getMaterialId()).orElse(null);

                        row.put("S.No", serialNo++);
                        row.put("Plant Code", plant != null ? plant.getCode() : "N/A");
                        row.put("Plant Name", plant != null ? plant.getName() : "N/A");
                        row.put("Material Code", material != null ? material.getCode() : "N/A");
                        row.put("Material Name", material != null ? material.getName() : "N/A");
                        row.put("Material Description", material != null ? material.getDescription() : "N/A");
                        row.put("Quantity in Stores", mapping.getQuantityStores() != null ? mapping.getQuantityStores()
                                        : BigDecimal.ZERO);
                        row.put("Reorder Level", mapping.getReorderLevel() != null ? mapping.getReorderLevel()
                                        : BigDecimal.ZERO);
                        row.put("Min Stock", mapping.getMaxLevel() != null ? BigDecimal.ZERO : BigDecimal.ZERO);
                        row.put("Max Stock", mapping.getMaxLevel() != null ? mapping.getMaxLevel() : BigDecimal.ZERO);
                        row.put("Status", mapping.getStatus() == 1 ? "Active" : "Inactive");

                        data.add(row);
                }

                return ExcelReportService.SheetData.builder()
                                .title(title)
                                .headers(headers)
                                .data(data)
                                .build();
        }

        /**
         * Generate summary sheet with aggregate data
         */
        private ExcelReportService.SheetData generateSummarySheet() {
                String title = String.format("Material Quantity Summary Report\nAs of: %s",
                                LocalDate.now().format(DATE_FORMATTER));

                List<String> headers = Arrays.asList(
                                "S.No",
                                "Company Code",
                                "Company Name",
                                "Total Plants",
                                "Total Materials",
                                "Total Mappings",
                                "Active Mappings",
                                "Inactive Mappings",
                                "Materials Below Reorder");

                List<Map<String, Object>> data = new ArrayList<>();
                List<Company> companies = companyRepository.findAll();

                int serialNo = 1;
                for (Company company : companies) {
                        Map<String, Object> row = new LinkedHashMap<>();

                        // Get all mappings for this company
                        var allMappings = companyPlantMaterialRepository.findAllActiveList()
                                        .stream()
                                        .filter(m -> m.getCompanyId().equals(company.getId()))
                                        .toList();

                        var activeMappings = allMappings.stream()
                                        .filter(m -> m.getStatus() == 1)
                                        .toList();

                        // Count unique plants
                        long uniquePlants = allMappings.stream()
                                        .map(CompanyPlantMaterial::getPlantId)
                                        .distinct()
                                        .count();

                        // Count unique materials
                        long uniqueMaterials = allMappings.stream()
                                        .map(CompanyPlantMaterial::getMaterialId)
                                        .distinct()
                                        .count();

                        // Count materials below reorder level
                        long belowReorder = activeMappings.stream()
                                        .filter(m -> m.getQuantityStores() != null &&
                                                        m.getReorderLevel() != null &&
                                                        m.getQuantityStores().compareTo(m.getReorderLevel()) < 0)
                                        .count();

                        row.put("S.No", serialNo++);
                        row.put("Company Code", company.getCode());
                        row.put("Company Name", company.getName());
                        row.put("Total Plants", uniquePlants);
                        row.put("Total Materials", uniqueMaterials);
                        row.put("Total Mappings", allMappings.size());
                        row.put("Active Mappings", activeMappings.size());
                        row.put("Inactive Mappings", allMappings.size() - activeMappings.size());
                        row.put("Materials Below Reorder", belowReorder);

                        data.add(row);
                }

                return ExcelReportService.SheetData.builder()
                                .title(title)
                                .headers(headers)
                                .data(data)
                                .build();
        }

        /**
         * Generate Low Stock Alert Report
         * Lists materials that are below reorder level
         */
        public byte[] generateLowStockAlertReport() throws IOException {
                log.info("Generating Low Stock Alert Report");

                String title = String.format("Low Stock Alert Report\nAs of: %s\nMaterials Below Reorder Level",
                                LocalDate.now().format(DATE_FORMATTER));

                List<String> headers = Arrays.asList(
                                "S.No",
                                "Company Code",
                                "Company Name",
                                "Plant Code",
                                "Plant Name",
                                "Material Code",
                                "Material Name",
                                "Current Quantity",
                                "Reorder Level",
                                "Shortage",
                                "Min Stock",
                                "Max Stock");

                List<Map<String, Object>> data = new ArrayList<>();

                // Get all mappings where quantity < reorder level
                var lowStockMappings = companyPlantMaterialRepository.findMappingsNeedingReorder();

                int serialNo = 1;
                for (var mapping : lowStockMappings) {
                        Map<String, Object> row = new LinkedHashMap<>();

                        // Fetch related entities using IDs
                        Company company = companyRepository.findById(mapping.getCompanyId()).orElse(null);
                        Plant plant = plantRepository.findById(mapping.getPlantId()).orElse(null);
                        Material material = materialRepository.findById(mapping.getMaterialId()).orElse(null);

                        var shortage = mapping.getReorderLevel().subtract(mapping.getQuantityStores());

                        row.put("S.No", serialNo++);
                        row.put("Company Code", company != null ? company.getCode() : "N/A");
                        row.put("Company Name", company != null ? company.getName() : "N/A");
                        row.put("Plant Code", plant != null ? plant.getCode() : "N/A");
                        row.put("Plant Name", plant != null ? plant.getName() : "N/A");
                        row.put("Material Code", material != null ? material.getCode() : "N/A");
                        row.put("Material Name", material != null ? material.getName() : "N/A");
                        row.put("Current Quantity", mapping.getQuantityStores());
                        row.put("Reorder Level", mapping.getReorderLevel());
                        row.put("Shortage", shortage);
                        row.put("Min Stock", BigDecimal.ZERO);
                        row.put("Max Stock", mapping.getMaxLevel() != null ? mapping.getMaxLevel() : BigDecimal.ZERO);

                        data.add(row);
                }

                return excelReportService.generateExcelReport(headers, data,
                                "Low Stock Alert", title);
        }
}
