package com.nslindia.procurezone.plantindent;

import com.nslindia.procurezone.plantindent.dto.*;
import com.nslindia.procurezone.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Plant Indent operations.
 * 
 * Manages plant-specific material indents with QC parameters for R&D/Production
 * workflow.
 * 
 * @author NSL India
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/plant-indents")
@RequiredArgsConstructor
@Slf4j
public class PlantIndentController {

    private final PlantIndentService plantIndentService;

    // ========== CREATE ==========

    /**
     * Create a new plant indent with line items and QC parameters
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> createPlantIndent(
            @Valid @RequestBody CreatePlantIndentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Creating plant indent for plant: {}", request.plantId());
        PlantIndentResponse response = plantIndentService.createPlantIndent(request, currentUser.userId().intValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== READ ==========

    /**
     * Get plant indent by ID with all details and QC parameters
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> getPlantIndentById(@PathVariable Integer id) {
        log.info("Fetching plant indent by ID: {}", id);
        PlantIndentResponse response = plantIndentService.getPlantIndentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get plant indent by indent number
     */
    @GetMapping("/number/{indentNumber}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> getPlantIndentByNumber(@PathVariable String indentNumber) {
        log.info("Fetching plant indent by number: {}", indentNumber);
        PlantIndentResponse response = plantIndentService.getPlantIndentByNumber(indentNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * List all plant indents with optional combined filters.
     * All params are independent and can be combined freely.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<PlantIndentResponse>> listPlantIndents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer plantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String empSearch,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Listing plant indents — search={}, plantId={}, status={}, empSearch={}", search, plantId, status, empSearch);
        Page<PlantIndentResponse> response = plantIndentService.listPlantIndents(
                search, plantId, status, empSearch, fromDate, toDate, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * List plant indents filtered by plant ID
     */
    @GetMapping("/plant/{plantId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<PlantIndentResponse>> listPlantIndentsByPlant(
            @PathVariable Integer plantId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Listing plant indents for plant: {}", plantId);
        Page<PlantIndentResponse> response = plantIndentService.listPlantIndentsByPlant(plantId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * List plant indents created by a specific employee
     */
    @GetMapping("/employee/{employeeNumber}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<PlantIndentResponse>> listPlantIndentsByEmployee(
            @PathVariable String employeeNumber,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Listing plant indents for employee: {}", employeeNumber);
        Page<PlantIndentResponse> response = plantIndentService.listPlantIndentsByEmployee(employeeNumber, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Search plant indents by indent number, crop, or batch
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<PlantIndentResponse>> searchPlantIndents(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Searching plant indents with query: {}", query);
        Page<PlantIndentResponse> response = plantIndentService.searchPlantIndents(query, pageable);
        return ResponseEntity.ok(response);
    }

    // ========== UPDATE ==========

    /**
     * Update an existing plant indent and its line items
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> updatePlantIndent(
            @PathVariable Integer id,
            @Valid @RequestBody CreatePlantIndentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Updating plant indent ID: {}", id);
        PlantIndentResponse response = plantIndentService.updatePlantIndent(id, request,
                currentUser.userId().intValue());
        return ResponseEntity.ok(response);
    }

    // ========== DELETE ==========

    /**
     * Delete a plant indent and all its line items
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Void> deletePlantIndent(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Deleting plant indent ID: {}", id);
        plantIndentService.deletePlantIndent(id, currentUser.userId().intValue());
        return ResponseEntity.noContent().build();
    }

    // ========== 8-STATUS WORKFLOW ENDPOINTS ==========

    /**
     * Submit plant indent for DEO/QM review (0 -> 1)
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> submitForReview(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Submitting plant indent {} for DEO review", id);
        PlantIndentResponse response = plantIndentService.submitForReview(id, currentUser.userId().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * DEO/QM Approve (1 -> 2)
     */
    @PostMapping("/{id}/deo-approve")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> deoApprove(
            @PathVariable Integer id,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("DEO approving plant indent {}", id);
        PlantIndentResponse response = plantIndentService.deoApprove(id, remarks, currentUser.userId().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * DEO/QM Reject (1 -> 4)
     */
    @PostMapping("/{id}/deo-reject")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> deoReject(
            @PathVariable Integer id,
            @RequestParam String remarks,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("DEO rejecting plant indent {}", id);
        PlantIndentResponse response = plantIndentService.deoReject(id, remarks, currentUser.userId().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * Manager Approve (2 -> 3)
     */
    @PostMapping("/{id}/manager-approve")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> managerApprove(
            @PathVariable Integer id,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Manager approving plant indent {}", id);
        PlantIndentResponse response = plantIndentService.managerApprove(id, remarks, currentUser.userId().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * Manager Reject (2 -> 5)
     */
    @PostMapping("/{id}/manager-reject")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> managerReject(
            @PathVariable Integer id,
            @RequestParam String remarks,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Manager rejecting plant indent {}", id);
        PlantIndentResponse response = plantIndentService.managerReject(id, remarks, currentUser.userId().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * Start Processing (3 -> 6)
     */
    @PostMapping("/{id}/start-processing")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> startProcessing(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Starting processing of plant indent {}", id);
        PlantIndentResponse response = plantIndentService.startProcessing(id, currentUser.userId().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * Complete Processing (6 -> 7)
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> completeProcessing(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Completing plant indent {}", id);
        PlantIndentResponse response = plantIndentService.completeProcessing(id, currentUser.userId().intValue());
        return ResponseEntity.ok(response);
    }

    /**
     * Resubmit rejected indent (4,5 -> 0)
     */
    @PostMapping("/{id}/resubmit")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<PlantIndentResponse> resubmit(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Resubmitting plant indent {}", id);
        PlantIndentResponse response = plantIndentService.resubmit(id, currentUser.userId().intValue());
        return ResponseEntity.ok(response);
    }

    // ========== ROLE-SPECIFIC QUEUE ENDPOINTS ==========

    /**
     * Get DEO/QM approval queue
     */
    @GetMapping("/queue/deo/{plantId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<PlantIndentResponse>> getDeoQueue(
            @PathVariable Integer plantId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting DEO queue for plant {}", plantId);
        Page<PlantIndentResponse> response = plantIndentService.getDeoQueue(plantId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get Manager approval queue
     */
    @GetMapping("/queue/manager/{plantId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<PlantIndentResponse>> getManagerQueue(
            @PathVariable Integer plantId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting manager queue for plant {}", plantId);
        Page<PlantIndentResponse> response = plantIndentService.getManagerQueue(plantId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get Processing queue (approved + processing items)
     */
    @GetMapping("/queue/processing/{plantId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<PlantIndentResponse>> getProcessingQueue(
            @PathVariable Integer plantId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting processing queue for plant {}", plantId);
        Page<PlantIndentResponse> response = plantIndentService.getProcessingQueue(plantId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get rejected items
     */
    @GetMapping("/rejected/{plantId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<PlantIndentResponse>> getRejectedItems(
            @PathVariable Integer plantId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting rejected items for plant {}", plantId);
        Page<PlantIndentResponse> response = plantIndentService.getRejectedItems(plantId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get my drafts
     */
    @GetMapping("/my-drafts")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<PlantIndentResponse>> getMyDrafts(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting drafts for user {}", currentUser.username());
        // employeeNumber is an Integer PK — convert to String so parseEmployeeNumber
        // succeeds. Falling back to username (an email) always produced null and an
        // empty result set.
        String empIdentifier = currentUser.employeeNumber() != null
                ? String.valueOf(currentUser.employeeNumber())
                : currentUser.username();
        Page<PlantIndentResponse> response = plantIndentService.getMyDrafts(empIdentifier, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get indents by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Page<PlantIndentResponse>> getByStatus(
            @PathVariable Integer status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting plant indents by status {}", status);
        Page<PlantIndentResponse> response = plantIndentService.getByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get dashboard counts for a plant
     */
    @GetMapping("/dashboard/{plantId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<java.util.Map<String, Long>> getDashboardCounts(@PathVariable Integer plantId) {
        log.info("Getting dashboard counts for plant {}", plantId);
        java.util.Map<String, Long> counts = plantIndentService.getDashboardCounts(plantId);
        return ResponseEntity.ok(counts);
    }

    /**
     * Export the plant indent list as CSV or Excel, respecting the same filters and roles as
     * GET /api/v1/plant-indents.
     * GET /api/v1/plant-indents/export?format=csv|xlsx&search=...&plantId=...&status=...
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<byte[]> exportPlantIndents(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer plantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String empSearch,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) throws java.io.IOException {

        java.util.List<PlantIndentResponse> rows = plantIndentService.listPlantIndents(
                search, plantId, status, empSearch, fromDate, toDate,
                org.springframework.data.domain.PageRequest.of(0, 50000)).getContent();

        String base = "plant-indents";
        String date = java.time.LocalDate.now().toString();
        String[] headers = {"Indent Number", "Employee", "Plant", "Crop Type", "Crop", "Output Material",
                "Batch Number", "Expected Quantity", "Status", "Created Date", "Line Items"};

        if ("csv".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.join(",", headers)).append('\n');
            for (PlantIndentResponse r : rows) {
                sb.append(csv(r.indentNumber())).append(',')
                  .append(csv(r.employeeName())).append(',')
                  .append(csv(r.plantName())).append(',')
                  .append(csv(r.cropTypeName())).append(',')
                  .append(csv(r.cropName())).append(',')
                  .append(csv(r.outputMaterial())).append(',')
                  .append(r.batchNumber() != null ? r.batchNumber() : "").append(',')
                  .append(csv(r.expectedQuantity())).append(',')
                  .append(csv(r.statusDescription())).append(',')
                  .append(csv(r.createdDate() != null ? r.createdDate().toString() : "")).append(',')
                  .append(r.detailCount()).append('\n');
            }
            byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + base + "-" + date + ".csv\"")
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .body(bytes);
        }

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Plant Indents");
            org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            int rowNum = 1;
            for (PlantIndentResponse r : rows) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.indentNumber() != null ? r.indentNumber() : "");
                row.createCell(1).setCellValue(r.employeeName() != null ? r.employeeName() : "");
                row.createCell(2).setCellValue(r.plantName() != null ? r.plantName() : "");
                row.createCell(3).setCellValue(r.cropTypeName() != null ? r.cropTypeName() : "");
                row.createCell(4).setCellValue(r.cropName() != null ? r.cropName() : "");
                row.createCell(5).setCellValue(r.outputMaterial() != null ? r.outputMaterial() : "");
                row.createCell(6).setCellValue(r.batchNumber() != null ? r.batchNumber() : 0);
                row.createCell(7).setCellValue(r.expectedQuantity() != null ? r.expectedQuantity() : "");
                row.createCell(8).setCellValue(r.statusDescription() != null ? r.statusDescription() : "");
                row.createCell(9).setCellValue(r.createdDate() != null ? r.createdDate().toString() : "");
                row.createCell(10).setCellValue(r.detailCount());
            }

            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            wb.write(bos);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + base + "-" + date + ".xlsx\"")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(bos.toByteArray());
        }
    }

    private static String csv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
