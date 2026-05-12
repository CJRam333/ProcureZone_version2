package com.nslindia.procurezone.bulkimport;

import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.bulkimport.dto.ImportResultResponse;
import com.nslindia.procurezone.bulkimport.dto.MaterialImportRequest;
import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.repository.MaterialRepository;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for bulk importing materials from CSV files.
 * Provides validation, duplicate detection, and batch processing.
 */
@Service
public class MaterialBulkImportService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialBulkImportService.class);
    private static final int BATCH_SIZE = 100;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB
    private static final String[] EXPECTED_HEADERS = { "code", "name", "description", "status" };

    private final MaterialRepository materialRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;
    private final Validator validator;

    public MaterialBulkImportService(
            MaterialRepository materialRepository,
            EmployeeRepository employeeRepository,
            AuditService auditService,
            Validator validator) {
        this.materialRepository = materialRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
        this.validator = validator;
    }

    /**
     * Imports materials from a CSV file.
     * 
     * @param file     the CSV file to import
     * @param username the username of the user performing the import
     * @return the import result with statistics and errors
     */
    @Transactional
    public ImportResultResponse importMaterials(MultipartFile file, String username) {
        long startTime = System.currentTimeMillis();
        logger.info("Starting material import from file: {} by user: {}", file.getOriginalFilename(), username);

        // Validate file
        validateFile(file);

        // Get current user
        Employee currentUser = getEmployeeByUsername(username);

        // Parse and process CSV
        ImportContext context = new ImportContext();
        processCSVFile(file, currentUser, context);

        // Calculate results
        long processingTime = System.currentTimeMillis() - startTime;
        int totalRecords = context.rowNumber - 1; // Exclude header row

        // Audit log
        logImportAudit(currentUser, username, totalRecords, context.successCount, context.failureCount);

        logger.info("Material import completed. Total: {}, Success: {}, Failed: {}, Time: {}ms",
                totalRecords, context.successCount, context.failureCount, processingTime);

        // Return appropriate response
        return buildImportResponse(totalRecords, context, processingTime);
    }

    /**
     * Processes the CSV file and imports materials.
     */
    private void processCSVFile(MultipartFile file, Employee currentUser, ImportContext context) {
        // Initialize existing codes
        context.existingCodes = new HashSet<>(materialRepository.findAllCodes());

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(reader,
                        CSVFormat.DEFAULT.builder()
                                .setHeader(EXPECTED_HEADERS)
                                .setSkipHeaderRecord(true)
                                .setIgnoreEmptyLines(true)
                                .setTrim(true)
                                .build())) {

            for (CSVRecord csvRecord : csvParser) {
                context.rowNumber++;
                processCSVRecord(csvRecord, currentUser, context);
            }

            // Save remaining materials
            saveBatch(context.materialsToSave);

        } catch (Exception e) {
            logger.error("Fatal error during material import", e);
            throw new BadRequestException("Failed to process CSV file: " + e.getMessage());
        }
    }

    /**
     * Processes a single CSV record.
     */
    private void processCSVRecord(CSVRecord csvRecord, Employee currentUser, ImportContext context) {
        try {
            MaterialImportRequest request = parseRecord(csvRecord);

            // Validate record
            if (!validateAndProcessRecord(request, currentUser, context)) {
                return;
            }

            // Batch save if needed
            if (context.materialsToSave.size() >= BATCH_SIZE) {
                saveBatch(context.materialsToSave);
            }

        } catch (Exception e) {
            context.failureCount++;
            logger.error("Error processing row {}: {}", context.rowNumber, e.getMessage(), e);
            context.errors.add(new ImportResultResponse.ImportError(
                    context.rowNumber,
                    "",
                    "",
                    "Processing error: " + e.getMessage(),
                    ImportResultResponse.ErrorType.PARSING_ERROR));
        }
    }

    /**
     * Validates and processes a single import request.
     * 
     * @return true if record was successfully added to batch, false if skipped
     */
    private boolean validateAndProcessRecord(MaterialImportRequest request, Employee currentUser,
            ImportContext context) {
        // Validate record
        List<String> validationErrors = validateRecord(request);
        if (!validationErrors.isEmpty()) {
            context.failureCount++;
            addValidationErrors(request, context, validationErrors);
            return false;
        }

        // Check for duplicates in database
        if (context.existingCodes.contains(request.code())) {
            context.failureCount++;
            context.errors.add(new ImportResultResponse.ImportError(
                    context.rowNumber,
                    request.code(),
                    request.name(),
                    "Material code already exists in database",
                    ImportResultResponse.ErrorType.DUPLICATE_CODE));
            return false;
        }

        // Check for duplicates within the CSV file
        if (context.codesInFile.contains(request.code())) {
            context.failureCount++;
            context.errors.add(new ImportResultResponse.ImportError(
                    context.rowNumber,
                    request.code(),
                    request.name(),
                    "Duplicate material code found in CSV file",
                    ImportResultResponse.ErrorType.DUPLICATE_CODE));
            return false;
        }

        // Create and add material to batch
        Material material = createMaterial(request, currentUser);
        context.materialsToSave.add(material);
        context.codesInFile.add(request.code());
        context.successCount++;
        return true;
    }

    /**
     * Creates a Material entity from import request.
     */
    private Material createMaterial(MaterialImportRequest request, Employee currentUser) {
        Material material = new Material();
        material.setCode(request.code());
        material.setName(request.name());
        material.setDescription(request.description());
        material.setStatus(request.status());
        material.setLastModifiedDate(LocalDate.now());
        material.setLastModifiedBy(currentUser.getEmpNumber());
        return material;
    }

    /**
     * Adds validation errors to context.
     */
    private void addValidationErrors(MaterialImportRequest request, ImportContext context,
            List<String> validationErrors) {
        for (String error : validationErrors) {
            context.errors.add(new ImportResultResponse.ImportError(
                    context.rowNumber,
                    request.code(),
                    request.name(),
                    error,
                    ImportResultResponse.ErrorType.VALIDATION_ERROR));
        }
    }

    /**
     * Saves a batch of materials and clears the list.
     */
    private void saveBatch(List<Material> materials) {
        if (!materials.isEmpty()) {
            materialRepository.saveAll(materials);
            logger.info("Batch saved {} materials", materials.size());
            materials.clear();
        }
    }

    /**
     * Retrieves employee by username.
     */
    private Employee getEmployeeByUsername(String username) {
        return employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    /**
     * Logs import audit entry.
     */
    private void logImportAudit(Employee currentUser, String username, int totalRecords, int successCount,
            int failureCount) {
        auditService.logEntityChange(
                "BULK_IMPORT",
                "Material",
                null,
                currentUser.getEmpNumber(),
                username,
                String.format("Bulk imported %d materials (Success: %d, Failed: %d)",
                        totalRecords, successCount, failureCount));
    }

    /**
     * Builds the import response based on results.
     */
    private ImportResultResponse buildImportResponse(int totalRecords, ImportContext context, long processingTime) {
        if (context.failureCount == 0) {
            return ImportResultResponse.success(totalRecords, context.successCount, processingTime);
        }
        return ImportResultResponse.partialSuccess(
                totalRecords, context.successCount, context.failureCount, context.errors, processingTime);
    }

    /**
     * Context class to hold import state.
     */
    private static class ImportContext {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        List<Material> materialsToSave = new ArrayList<>();
        Set<String> existingCodes;
        Set<String> codesInFile = new HashSet<>();
        int rowNumber = 1;
        int successCount = 0;
        int failureCount = 0;

        ImportContext() {
            // Initialize with empty set, will be populated before use
            this.existingCodes = new HashSet<>();
        }
    }

    /**
     * Validates the uploaded file.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                    "File size exceeds maximum limit of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new BadRequestException("File must be a CSV file");
        }
    }

    /**
     * Parses a CSV record into a MaterialImportRequest.
     */
    private MaterialImportRequest parseRecord(CSVRecord record) {
        String code = record.get("code");
        String name = record.get("name");
        String description = record.get("description");
        String statusStr = record.get("status");

        Integer status = null;
        try {
            status = Integer.parseInt(statusStr);
        } catch (NumberFormatException e) {
            // Will be caught in validation
        }

        return new MaterialImportRequest(code, name, description, status).sanitized();
    }

    /**
     * Validates a material import request.
     */
    private List<String> validateRecord(MaterialImportRequest request) {
        List<String> errors = new ArrayList<>();

        // Use Jakarta Bean Validation
        Set<ConstraintViolation<MaterialImportRequest>> violations = validator.validate(request);
        for (ConstraintViolation<MaterialImportRequest> violation : violations) {
            errors.add(violation.getMessage());
        }

        // Additional custom validation
        if (request.status() != null && !request.isValidStatus()) {
            errors.add("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        return errors;
    }

    /**
     * Generates a CSV template file content for material import.
     */
    public String generateTemplate() {
        StringBuilder template = new StringBuilder();
        template.append("code,name,description,status\n");
        template.append("MAT-001,Sample Material 1,This is a sample material description,1\n");
        template.append("MAT-002,Sample Material 2,Another sample material,1\n");
        template.append("MAT-003,Inactive Material,This material is inactive,0\n");
        return template.toString();
    }
}
