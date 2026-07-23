package com.nslindia.procurezone.vendor;

import com.nslindia.procurezone.security.UserPrincipal;
import com.nslindia.procurezone.vendor.dto.CreateVendorRequest;
import com.nslindia.procurezone.vendor.dto.UpdateVendorRequest;
import com.nslindia.procurezone.vendor.dto.VendorPerformanceResponse;
import com.nslindia.procurezone.vendor.dto.VendorResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for vendor management operations.
 */
@RestController
@RequestMapping("/api/v1/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    /**
     * Creates a new vendor.
     * Endpoint: POST /api/v1/vendors
     * Roles: PROCUREMENT, ADMIN, SUPERADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<VendorResponse> createVendor(
            @Valid @RequestBody CreateVendorRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Integer currentEmpId = userPrincipal.employeeNumber().intValue();
        VendorResponse vendor = vendorService.createVendor(request, currentEmpId);

        return ResponseEntity.status(HttpStatus.CREATED).body(vendor);
    }

    /**
     * Lists all vendors with optional status filter and pagination.
     * Endpoint: GET /api/v1/vendors?status={status}&page={page}&size={size}
     * Roles: All authenticated users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<VendorResponse>> listVendors(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<VendorResponse> vendors = vendorService.listVendors(status, page, size);

        return ResponseEntity.ok(vendors);
    }

    /**
     * Gets vendor details by ID.
     * Endpoint: GET /api/v1/vendors/{id}
     * Roles: All authenticated users
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<VendorResponse> getVendorById(@PathVariable Integer id) {

        VendorResponse vendor = vendorService.getVendorById(id);

        return ResponseEntity.ok(vendor);
    }

    /**
     * Updates an existing vendor.
     * Endpoint: PUT /api/v1/vendors/{id}
     * Roles: PROCUREMENT, ADMIN, SUPERADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<VendorResponse> updateVendor(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateVendorRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Integer currentEmpId = userPrincipal.employeeNumber().intValue();
        VendorResponse vendor = vendorService.updateVendor(id, request, currentEmpId);

        return ResponseEntity.ok(vendor);
    }

    /**
     * Soft deletes a vendor.
     * Endpoint: DELETE /api/v1/vendors/{id}
     * Roles: ADMIN, SUPERADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> deleteVendor(
            @PathVariable Integer id,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Integer currentEmpId = userPrincipal.employeeNumber().intValue();
        vendorService.deleteVendor(id, currentEmpId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Lists all active vendors (status = 1).
     * Endpoint: GET /api/v1/vendors/active
     * Roles: All authenticated users
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('USER', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<VendorResponse>> listActiveVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<VendorResponse> vendors = vendorService.listActiveVendors(page, size);

        return ResponseEntity.ok(vendors);
    }

    /**
     * Searches vendors by keyword (name, code, contact, city).
     * Endpoint: GET
     * /api/v1/vendors/search?keyword={keyword}&page={page}&size={size}
     * Roles: All authenticated users
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Page<VendorResponse>> searchVendors(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<VendorResponse> vendors = vendorService.searchVendors(keyword, page, size);

        return ResponseEntity.ok(vendors);
    }

    /**
     * Updates vendor rating.
     * Endpoint: PUT /api/v1/vendors/{id}/rating
     * Roles: PROCUREMENT, ADMIN, SUPERADMIN
     */
    @PutMapping("/{id}/rating")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<VendorResponse> updateVendorRating(
            @PathVariable Integer id,
            @RequestParam Double rating,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Integer currentEmpId = userPrincipal.employeeNumber().intValue();
        VendorResponse vendor = vendorService.updateVendorRating(id, rating, currentEmpId);

        return ResponseEntity.ok(vendor);
    }

    /**
     * Gets vendor performance metrics.
     * Endpoint: GET /api/v1/vendors/{id}/performance
     * Roles: PROCUREMENT, ADMIN, SUPERADMIN
     */
    @GetMapping("/{id}/performance")
    @PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<VendorPerformanceResponse> getVendorPerformance(
            @PathVariable Integer id) {

        VendorPerformanceResponse performance = vendorService.getVendorPerformance(id);

        return ResponseEntity.ok(performance);
    }

    /**
     * Exports the vendor list as CSV or Excel, respecting the same filter and roles as
     * GET /api/v1/vendors.
     * Endpoint: GET /api/v1/vendors/export?format=csv|xlsx&status={status}
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('USER', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<byte[]> exportVendors(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) Integer status) throws java.io.IOException {

        java.util.List<VendorResponse> rows = vendorService.listVendors(status, 0, 50000).getContent();

        String base = "vendors";
        String date = java.time.LocalDate.now().toString();
        String[] headers = {"Vendor Code", "Vendor Name", "Type", "Contact Person", "Phone", "Email",
                "City", "State", "Status", "Rating", "Total Orders"};

        if ("csv".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.join(",", headers)).append('\n');
            for (VendorResponse r : rows) {
                sb.append(csv(r.vendorCode())).append(',')
                  .append(csv(r.vendorName())).append(',')
                  .append(csv(r.vendorType())).append(',')
                  .append(csv(r.contactPerson())).append(',')
                  .append(csv(r.contactPhone())).append(',')
                  .append(csv(r.contactEmail())).append(',')
                  .append(csv(r.city())).append(',')
                  .append(csv(r.state())).append(',')
                  .append(csv(r.statusName())).append(',')
                  .append(r.rating() != null ? r.rating() : "").append(',')
                  .append(r.totalOrders() != null ? r.totalOrders() : 0).append('\n');
            }
            byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + base + "-" + date + ".csv\"")
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .body(bytes);
        }

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Vendors");
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
                sheet.setColumnWidth(i, 18 * 256);
            }

            int rowNum = 1;
            for (VendorResponse r : rows) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.vendorCode() != null ? r.vendorCode() : "");
                row.createCell(1).setCellValue(r.vendorName() != null ? r.vendorName() : "");
                row.createCell(2).setCellValue(r.vendorType() != null ? r.vendorType() : "");
                row.createCell(3).setCellValue(r.contactPerson() != null ? r.contactPerson() : "");
                row.createCell(4).setCellValue(r.contactPhone() != null ? r.contactPhone() : "");
                row.createCell(5).setCellValue(r.contactEmail() != null ? r.contactEmail() : "");
                row.createCell(6).setCellValue(r.city() != null ? r.city() : "");
                row.createCell(7).setCellValue(r.state() != null ? r.state() : "");
                row.createCell(8).setCellValue(r.statusName() != null ? r.statusName() : "");
                row.createCell(9).setCellValue(r.rating() != null ? r.rating() : 0d);
                row.createCell(10).setCellValue(r.totalOrders() != null ? r.totalOrders() : 0);
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
