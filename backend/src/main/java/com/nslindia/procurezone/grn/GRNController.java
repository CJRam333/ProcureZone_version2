package com.nslindia.procurezone.grn;

import com.nslindia.procurezone.security.UserPrincipal;
import com.nslindia.procurezone.grn.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/grn")
@RequiredArgsConstructor
@Slf4j
// CORS handled by WebConfig - removed @CrossOrigin to avoid conflict with
// allowCredentials
public class GRNController {

    private final GRNService grnService;

    /**
     * Create new GRN
     * POST /api/v1/grn
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('GOODSINCHARGE', 'GRNINCHARGE', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> createGRN(
            @Valid @RequestBody CreateGRNRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Creating GRN for indent {} by user {}", request.indentId(), userPrincipal.username());

        GRNResponse response = grnService.createGRN(request, userPrincipal.employeeNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all GRNs with pagination and filtering
     * GET /api/v1/grn?page=0&size=10&status=1&search=vendor
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('GOODSINCHARGE', 'GRNINCHARGE', 'FLOORINCHARGE', 'ISSUECONFIRM', 'RECEIPTCONFIRM', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'DEPTHEAD', 'PLANTMANAGER', 'QUALITYMANAGER', 'USER')")
    public ResponseEntity<Map<String, Object>> getAllGRNs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("receiptDate").descending());
        Page<GRNSummaryResponse> grnPage = grnService.getAllGRNs(status, search, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", grnPage.getContent());
        response.put("currentPage", grnPage.getNumber());
        response.put("totalItems", grnPage.getTotalElements());
        response.put("totalPages", grnPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Get GRN by ID
     * GET /api/v1/grn/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GOODSINCHARGE', 'GRNINCHARGE', 'FLOORINCHARGE', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'DEPTHEAD', 'PLANTMANAGER', 'USER')")
    public ResponseEntity<GRNResponse> getGRNById(@PathVariable Integer id) {
        log.info("Fetching GRN with ID: {}", id);
        GRNResponse response = grnService.getGRNById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get GRN by number
     * GET /api/v1/grn/number/{grnNumber}
     */
    @GetMapping("/number/{grnNumber}")
    @PreAuthorize("hasAnyRole('GOODSINCHARGE', 'GRNINCHARGE', 'FLOORINCHARGE', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'DEPTHEAD', 'PLANTMANAGER')")
    public ResponseEntity<GRNResponse> getGRNByNumber(@PathVariable String grnNumber) {
        log.info("Fetching GRN with number: {}", grnNumber);
        GRNResponse response = grnService.getGRNByNumber(grnNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Quality inspection
     * POST /api/v1/grn/{id}/inspect
     */
    @PostMapping("/{id}/inspect")
    @PreAuthorize("hasAnyRole('QUALITYMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> qualityInspection(
            @PathVariable Integer id,
            @Valid @RequestBody InspectionRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Quality inspection for GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.qualityInspection(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * RM Approve GRN (Reporting Manager approval)
     * POST /api/v1/grn/{id}/rm-approve
     */
    @PostMapping("/{id}/rm-approve")
    @PreAuthorize("hasAnyRole('DEPTHEAD', 'SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> rmApproveGRN(
            @PathVariable Integer id,
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("RM approving GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.rmApproveGRN(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * RM Reject GRN (Reporting Manager rejection)
     * POST /api/v1/grn/{id}/rm-reject
     */
    @PostMapping("/{id}/rm-reject")
    @PreAuthorize("hasAnyRole('DEPTHEAD', 'SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> rmRejectGRN(
            @PathVariable Integer id,
            @Valid @RequestBody RejectionRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("RM rejecting GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.rmRejectGRN(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Approve GRN (First level approval)
     * POST /api/v1/grn/{id}/approve
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('PLANTMANAGER', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> approveGRN(
            @PathVariable Integer id,
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Approving GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.approveGRN(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Final approval (Second level approval)
     * POST /api/v1/grn/{id}/final-approve
     */
    @PostMapping("/{id}/final-approve")
    @PreAuthorize("hasAnyRole('PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> finalApproveGRN(
            @PathVariable Integer id,
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Final approving GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.finalApproveGRN(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Store goods in inventory
     * POST /api/v1/grn/{id}/store
     */
    @PostMapping("/{id}/store")
    @PreAuthorize("hasAnyRole('GOODSINCHARGE', 'GRNINCHARGE', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> storeGoods(
            @PathVariable Integer id,
            @Valid @RequestBody StoreRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Storing goods for GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.storeGoods(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Reject GRN
     * POST /api/v1/grn/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('QUALITYMANAGER', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<GRNResponse> rejectGRN(
            @PathVariable Integer id,
            @Valid @RequestBody RejectionRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Rejecting GRN {} by user {}", id, userPrincipal.username());

        GRNResponse response = grnService.rejectGRN(id, request, userPrincipal.employeeNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * Get GRNs pending inspection
     * GET /api/v1/grn/pending-inspection
     */
    @GetMapping("/pending-inspection")
    @PreAuthorize("hasAnyRole('QUALITYMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingInspection(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("receiptDate").ascending());
        Page<GRNSummaryResponse> grnPage = grnService.getPendingInspection(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", grnPage.getContent());
        response.put("currentPage", grnPage.getNumber());
        response.put("totalItems", grnPage.getTotalElements());
        response.put("totalPages", grnPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Get GRNs pending approval
     * GET /api/v1/grn/pending-approval
     */
    @GetMapping("/pending-approval")
    @PreAuthorize("hasAnyRole('PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingApproval(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("receiptDate").ascending());
        Page<GRNSummaryResponse> grnPage = grnService.getPendingApproval(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", grnPage.getContent());
        response.put("currentPage", grnPage.getNumber());
        response.put("totalItems", grnPage.getTotalElements());
        response.put("totalPages", grnPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Get dashboard statistics
     * GET /api/v1/grn/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        log.info("Fetching GRN dashboard statistics");
        Map<String, Object> stats = grnService.getDashboardStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Export the GRN list as CSV or Excel, respecting the same filters and roles as GET /api/v1/grn.
     * GET /api/v1/grn/export?format=csv|xlsx&status=...&search=...
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('GOODSINCHARGE', 'GRNINCHARGE', 'FLOORINCHARGE', 'ISSUECONFIRM', 'RECEIPTCONFIRM', 'VIEWER', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'DEPTHEAD', 'PLANTMANAGER', 'QUALITYMANAGER', 'USER')")
    public ResponseEntity<byte[]> exportGRNs(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String search) throws java.io.IOException {

        Pageable pageable = PageRequest.of(0, 50000, Sort.by("receiptDate").descending());
        java.util.List<GRNSummaryResponse> rows = grnService.getAllGRNs(status, search, pageable).getContent();

        String base = "grn";
        String date = java.time.LocalDate.now().toString();
        String[] headers = {"GRN Number", "Receipt Date", "Vendor", "Received Qty", "Amount", "Status", "Balance Inventory"};

        if ("csv".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.join(",", headers)).append('\n');
            for (GRNSummaryResponse r : rows) {
                sb.append(csv(r.grnNumber())).append(',')
                  .append(csv(r.receiptDate() != null ? r.receiptDate().toString() : "")).append(',')
                  .append(csv(r.vendorName())).append(',')
                  .append(csv(r.receivedQuantity() != null ? r.receivedQuantity().toPlainString() : "")).append(',')
                  .append(csv(r.amount() != null ? r.amount().toPlainString() : "")).append(',')
                  .append(csv(r.statusName())).append(',')
                  .append(csv(r.balanceInventory() != null ? r.balanceInventory().toPlainString() : "")).append('\n');
            }
            byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + base + "-" + date + ".csv\"")
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .body(bytes);
        }

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("GRN");
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
            for (GRNSummaryResponse r : rows) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.grnNumber() != null ? r.grnNumber() : "");
                row.createCell(1).setCellValue(r.receiptDate() != null ? r.receiptDate().toString() : "");
                row.createCell(2).setCellValue(r.vendorName() != null ? r.vendorName() : "");
                row.createCell(3).setCellValue(r.receivedQuantity() != null ? r.receivedQuantity().doubleValue() : 0d);
                row.createCell(4).setCellValue(r.amount() != null ? r.amount().doubleValue() : 0d);
                row.createCell(5).setCellValue(r.statusName() != null ? r.statusName() : "");
                row.createCell(6).setCellValue(r.balanceInventory() != null ? r.balanceInventory().doubleValue() : 0d);
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
