package com.nslindia.procurezone.inventory;

import com.nslindia.procurezone.dto.inventory.InventoryResponse;
import com.nslindia.procurezone.dto.inventory.InventoryStatisticsDTO;
import com.nslindia.procurezone.dto.inventory.InventoryTransactionResponse;
import com.nslindia.procurezone.dto.inventory.StockAdjustmentRequest;
import com.nslindia.procurezone.masterdata.dto.MaterialDropdownResponse;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Inventory Management
 * Complete implementation with 15 endpoints
 *
 * @author NSL India
 * @version 2.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

        private final InventoryService inventoryService;

        /**
         * 1. GET - Get inventory with filters
         * 
         * @param companyId  Company filter (optional)
         * @param plantId    Plant filter (optional)
         * @param materialId Material filter (optional)
         * @param lowStock   Show only low stock items (optional)
         */
        @GetMapping
        @PreAuthorize("hasAnyRole('FLOORINCHARGE', 'GOODSINCHARGE', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN', 'VIEWER')")
        public ResponseEntity<Map<String, Object>> getInventory(
                        @RequestParam(required = false) Integer companyId,
                        @RequestParam(required = false) Integer plantId,
                        @RequestParam(required = false) Integer materialId,
                        @RequestParam(required = false) Boolean lowStock) {

                log.info("Get inventory - Company: {}, Plant: {}, Material: {}, LowStock: {}",
                                companyId, plantId, materialId, lowStock);

                List<InventoryResponse> inventory = inventoryService.getInventory(
                                companyId, plantId, materialId, lowStock);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("count", inventory.size());
                response.put("data", inventory);

                return ResponseEntity.ok(response);
        }

        /**
         * Read-only stock view for the operational Inventory module.
         * Paginated, searchable list of materials with their available stock from the REAL source
         * (tbl_map_company_plant_material.map_quantity_stores) — the same source the material
         * dropdown and issue-note creation read, NOT the empty tbl_inventory_balance. One row per
         * material+company+plant combination; materials with no stock row still appear with 0.
         * View-only — no create/edit/delete.
         */
        @GetMapping("/stock-view")
        @PreAuthorize("hasAnyRole('USER', 'SUPERVISOR', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT')")
        public ResponseEntity<Page<MaterialDropdownResponse>> getStockView(
                        @RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                return ResponseEntity.ok(inventoryService.getStockView(search, page, size));
        }

        /**
         * 2. GET - Get stock for specific material at plant
         * Used by indent creation to show available stock to users
         */
        @GetMapping("/stock/{materialId}/plant/{plantId}")
        @PreAuthorize("hasAnyRole('FLOORINCHARGE', 'GOODSINCHARGE', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN', 'VIEWER', 'USER', 'DEPTHEAD', 'PROCUREMENT')")
        public ResponseEntity<Map<String, Object>> getStock(
                        @PathVariable Integer materialId,
                        @PathVariable Integer plantId) {

                BigDecimal availableStock = inventoryService.getAvailableStock(materialId, plantId);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("materialId", materialId);
                response.put("plantId", plantId);
                response.put("availableStock", availableStock);

                return ResponseEntity.ok(response);
        }

        /**
         * 2b. GET - Get available stock for multiple materials at a plant
         * Bulk endpoint for indent creation form - shows stock for all materials
         */
        @GetMapping("/stock/plant/{plantId}/company/{companyId}")
        @PreAuthorize("hasAnyRole('FLOORINCHARGE', 'GOODSINCHARGE', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN', 'VIEWER', 'USER', 'DEPTHEAD', 'PROCUREMENT')")
        public ResponseEntity<Map<String, Object>> getStockByPlantAndCompany(
                        @PathVariable Integer plantId,
                        @PathVariable Integer companyId) {

                log.info("Getting available stock for plant {} and company {}", plantId, companyId);

                List<InventoryResponse> inventory = inventoryService.getInventory(companyId, plantId, null, null);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("plantId", plantId);
                response.put("companyId", companyId);
                response.put("count", inventory.size());
                response.put("data", inventory);

                return ResponseEntity.ok(response);
        }

        /**
         * 3. GET - Get low stock items
         */
        @GetMapping("/low-stock")
        @PreAuthorize("hasAnyRole('FLOORINCHARGE', 'GOODSINCHARGE', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> getLowStockItems() {
                log.info("Get low stock items");

                List<InventoryResponse> lowStock = inventoryService.getLowStockItems();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("count", lowStock.size());
                response.put("data", lowStock);

                return ResponseEntity.ok(response);
        }

        /**
         * 4. GET - Get critical stock items
         */
        @GetMapping("/critical-stock")
        @PreAuthorize("hasAnyRole('FLOORINCHARGE', 'GOODSINCHARGE', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> getCriticalStockItems() {
                log.info("Get critical stock items");

                List<InventoryResponse> criticalStock = inventoryService.getCriticalStockItems();

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("count", criticalStock.size());
                response.put("data", criticalStock);
                response.put("alert", criticalStock.isEmpty() ? "No critical stock alerts"
                                : "URGENT: " + criticalStock.size() + " items below minimum level");

                return ResponseEntity.ok(response);
        }

        /**
         * 5. GET - Get inventory statistics
         */
        @GetMapping("/statistics")
        @PreAuthorize("hasAnyRole('PLANTMANAGER', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> getStatistics(
                        @RequestParam(required = false) Integer companyId) {

                log.info("Get inventory statistics - Company: {}", companyId);

                InventoryStatisticsDTO statistics = inventoryService.getStatistics(companyId);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", statistics);

                return ResponseEntity.ok(response);
        }

        /**
         * 6. POST - Stock adjustment (add or deduct)
         */
        @PostMapping("/adjustment")
        @PreAuthorize("hasAnyRole('FLOORINCHARGE', 'GOODSINCHARGE', 'ADMIN', 'SUPERADMIN')")
        public ResponseEntity<Map<String, Object>> stockAdjustment(
                        @Valid @RequestBody StockAdjustmentRequest request) {

                log.info("Stock adjustment - Material: {}, Plant: {}, Type: {}, Qty: {}",
                                request.getMaterialId(), request.getPlantId(),
                                request.getAdjustmentType(), request.getQuantity());

                InventoryResponse inventory = inventoryService.adjustStock(request);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Stock adjusted successfully");
                response.put("data", inventory);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * 7. GET - Transaction history with filters
         */
        @GetMapping("/transactions")
        @PreAuthorize("hasAnyRole('FLOORINCHARGE', 'GOODSINCHARGE', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN', 'VIEWER')")
        public ResponseEntity<Map<String, Object>> getTransactionHistory(
                        @RequestParam(required = false) Integer companyId,
                        @RequestParam(required = false) Integer plantId,
                        @RequestParam(required = false) Integer materialId,
                        @RequestParam(required = false) String transactionType,
                        @RequestParam(required = false) String direction,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

                log.info("Get transaction history - Company: {}, Plant: {}, Material: {}, Type: {}, Direction: {}",
                                companyId, plantId, materialId, transactionType, direction);

                List<InventoryTransactionResponse> transactions = inventoryService.getTransactionHistory(
                                companyId, plantId, materialId, transactionType, direction, startDate, endDate);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("count", transactions.size());
                response.put("data", transactions);

                return ResponseEntity.ok(response);
        }

        /**
         * 8. GET - Check sufficient stock
         */
        @GetMapping("/check-stock/{materialId}/plant/{plantId}")
        @PreAuthorize("hasAnyRole('FLOORINCHARGE', 'GOODSINCHARGE', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT')")
        public ResponseEntity<Map<String, Object>> checkSufficientStock(
                        @PathVariable Integer materialId,
                        @PathVariable Integer plantId,
                        @RequestParam BigDecimal requiredQuantity) {

                BigDecimal availableStock = inventoryService.getAvailableStock(materialId, plantId);
                boolean isSufficient = inventoryService.isSufficientStock(materialId, plantId, requiredQuantity);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("materialId", materialId);
                response.put("plantId", plantId);
                response.put("requiredQuantity", requiredQuantity);
                response.put("availableStock", availableStock);
                response.put("isSufficient", isSufficient);
                response.put("shortfall", isSufficient ? BigDecimal.ZERO
                                : requiredQuantity.subtract(availableStock));

                return ResponseEntity.ok(response);
        }

        /**
         * 9. POST - Bulk stock check for multiple materials
         * Used by Issue Note UI to check availability before creating
         */
        @PostMapping("/check-stock-bulk")
        @PreAuthorize("hasAnyRole('FLOORINCHARGE', 'GOODSINCHARGE', 'PLANTMANAGER', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'DEPTHEAD')")
        public ResponseEntity<Map<String, Object>> checkBulkStock(
                        @RequestBody List<StockCheckRequest> requests,
                        @RequestParam Integer plantId) {

                log.info("Bulk stock check - {} items for plant {}", requests.size(), plantId);

                List<Map<String, Object>> results = requests.stream().map(req -> {
                        BigDecimal available = inventoryService.getAvailableStock(req.getMaterialId(), plantId);
                        boolean sufficient = available.compareTo(req.getRequiredQuantity()) >= 0;

                        Map<String, Object> item = new HashMap<>();
                        item.put("materialId", req.getMaterialId());
                        item.put("requiredQuantity", req.getRequiredQuantity());
                        item.put("availableStock", available);
                        item.put("isSufficient", sufficient);
                        item.put("shortfall", sufficient ? BigDecimal.ZERO
                                        : req.getRequiredQuantity().subtract(available));
                        return item;
                }).toList();

                boolean allSufficient = results.stream()
                                .allMatch(r -> Boolean.TRUE.equals(r.get("isSufficient")));

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("plantId", plantId);
                response.put("itemCount", results.size());
                response.put("allSufficient", allSufficient);
                response.put("items", results);

                return ResponseEntity.ok(response);
        }

        /**
         * Simple DTO for bulk stock check requests
         */
        public static class StockCheckRequest {
                private Integer materialId;
                private BigDecimal requiredQuantity;

                public Integer getMaterialId() {
                        return materialId;
                }

                public void setMaterialId(Integer materialId) {
                        this.materialId = materialId;
                }

                public BigDecimal getRequiredQuantity() {
                        return requiredQuantity;
                }

                public void setRequiredQuantity(BigDecimal requiredQuantity) {
                        this.requiredQuantity = requiredQuantity;
                }
        }

        /**
         * Exception handler for insufficient stock
         */
        @ExceptionHandler(InventoryService.InsufficientStockException.class)
        public ResponseEntity<Map<String, Object>> handleInsufficientStock(
                        InventoryService.InsufficientStockException ex) {

                log.error("Insufficient stock: {}", ex.getMessage());

                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Insufficient Stock");
                response.put("message", ex.getMessage());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * Export the stock-view list as CSV or Excel, respecting the same filter and roles as
         * GET /api/v1/inventory/stock-view.
         * GET /api/v1/inventory/export?format=csv|xlsx&search=...
         */
        @GetMapping("/export")
        @PreAuthorize("hasAnyRole('USER', 'SUPERVISOR', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT')")
        public ResponseEntity<byte[]> exportStockView(
                        @RequestParam(defaultValue = "csv") String format,
                        @RequestParam(required = false) String search) throws java.io.IOException {

                java.util.List<MaterialDropdownResponse> rows =
                                inventoryService.getStockView(search, 0, 50000).getContent();

                String base = "inventory-stock";
                String date = java.time.LocalDate.now().toString();
                String[] headers = {"Material Code", "Material Name", "Description", "Company", "Plant", "Stock Quantity"};

                if ("csv".equalsIgnoreCase(format)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(String.join(",", headers)).append('\n');
                        for (MaterialDropdownResponse r : rows) {
                                sb.append(csv(r.materialCode())).append(',')
                                  .append(csv(r.materialName())).append(',')
                                  .append(csv(r.materialDescription())).append(',')
                                  .append(csv(r.companyName())).append(',')
                                  .append(csv(r.plantName())).append(',')
                                  .append(csv(r.stockQuantity() != null ? r.stockQuantity().toPlainString() : "")).append('\n');
                        }
                        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        return ResponseEntity.ok()
                                        .header("Content-Disposition", "attachment; filename=\"" + base + "-" + date + ".csv\"")
                                        .header("Content-Type", "text/csv; charset=UTF-8")
                                        .body(bytes);
                }

                try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
                        org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Inventory Stock");
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
                        for (MaterialDropdownResponse r : rows) {
                                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                                row.createCell(0).setCellValue(r.materialCode() != null ? r.materialCode() : "");
                                row.createCell(1).setCellValue(r.materialName() != null ? r.materialName() : "");
                                row.createCell(2).setCellValue(r.materialDescription() != null ? r.materialDescription() : "");
                                row.createCell(3).setCellValue(r.companyName() != null ? r.companyName() : "");
                                row.createCell(4).setCellValue(r.plantName() != null ? r.plantName() : "");
                                row.createCell(5).setCellValue(r.stockQuantity() != null ? r.stockQuantity().doubleValue() : 0d);
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
