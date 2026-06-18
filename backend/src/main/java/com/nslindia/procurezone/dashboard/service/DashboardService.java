package com.nslindia.procurezone.dashboard.service;

import com.nslindia.procurezone.dashboard.dto.DashboardStatisticsResponse;
import com.nslindia.procurezone.dashboard.dto.DashboardStatisticsResponse.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard Statistics Service
 * Aggregates metrics from all modules for comprehensive dashboard view
 * 
 * Actual Database Schema Reference:
 * ---------------------------------
 * tbl_indent_master: indent_id, indent_no, indent_date, indent_company,
 * indent_dept, indent_status, indent_createdby, indent_approvedby_date
 * tbl_indent_details: indent_details_id, indent_id, indent_details_material,
 * indent_details_qty, indent_details_status
 * tbl_purchase_orders: id, po_number, po_date, po_status (int), vendor_id,
 * total_amount, delivery_date
 * tbl_goods_receipt: goods_receipt_id, goods_receipt_no, goods_receipt_date,
 * goods_receipt_quantity, goods_receipt_amount, goods_receipt_status
 * tbl_issue_note: issue_note_id, issue_note_no, issue_note_date,
 * issue_note_company, issue_note_dept, issue_note_status
 * tbl_inventory_balance: inventory_id, material_id, current_balance,
 * available_quantity, reorder_level, min_level, max_level, avg_rate,
 * total_value
 * tbl_vendors: id, vendor_code, vendor_name, status (int), created_date
 * tbl_material_master: material_id, material_code, material_name,
 * material_status
 * tbl_department_master: dept_id, dept_code, dept_name, dept_status
 * tbl_emp_master: emp_number, emp_id, emp_name, emp_email
 * tbl_umo_master: umo_id, umo_code, umo_name
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM");

    /**
     * Get comprehensive dashboard statistics
     */
    public DashboardStatisticsResponse getDashboardStatistics(Integer companyId, Integer departmentId) {
        log.info("Fetching dashboard statistics for company: {}, department: {}", companyId, departmentId);

        return DashboardStatisticsResponse.builder()
                .summary(getSummaryCards(companyId))
                .indentStats(getIndentStats(companyId, departmentId))
                .purchaseOrderStats(getPurchaseOrderStats(companyId))
                .goodsReceiptStats(getGoodsReceiptStats(companyId))
                .issueNoteStats(getIssueNoteStats(companyId, departmentId))
                .inventoryStats(getInventoryStats(companyId))
                .vendorStats(getVendorStats())
                .monthlyTrends(getMonthlyTrends(companyId, 6))
                .departmentBreakdown(getDepartmentBreakdown(companyId))
                .topMaterials(getTopMaterials(companyId, 10))
                .pendingApprovals(getPendingApprovals(companyId, departmentId))
                .alerts(generateAlerts(companyId))
                .build();
    }

    /**
     * Get summary cards data
     */
    public SummaryCards getSummaryCards(Integer companyId) {
        String companyFilter = companyId != null ? "AND i.indent_company = :companyId" : "";

        // Indent counts
        Long totalIndents = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_indent_master i WHERE 1=1 " + companyFilter, companyId);
        Long pendingIndents = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_indent_master i WHERE i.indent_status IN (1, 2, 3) " + companyFilter,
                companyId);

        // PO counts - po has no company_id in the schema, skip company filter
        Long totalPOs = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_purchase_orders po WHERE 1=1", null);

        // GRN counts - grn has no company_id in the schema
        Long totalGRNs = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_goods_receipt grn WHERE 1=1", null);

        // Issue Note counts
        String issueCompanyFilter = companyId != null ? "AND isn.issue_note_company = :companyId" : "";
        Long totalIssues = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_issue_note isn WHERE 1=1 " + issueCompanyFilter, companyId);

        // Inventory alerts - using correct column names
        Long lowStock = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_inventory_balance inv WHERE inv.current_balance < COALESCE(inv.reorder_level, 10) AND inv.current_balance > 0",
                null);
        Long criticalStock = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_inventory_balance inv WHERE inv.current_balance <= COALESCE(inv.min_level, 5) AND inv.current_balance > 0",
                null);

        // Active vendors - status = 1 means active
        Long activeVendors = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_vendors v WHERE v.status = 1", null);

        // Calculate procurement value (from POs this financial year)
        BigDecimal procurementValue = executeSumQuery(
                "SELECT COALESCE(SUM(po.total_amount), 0) FROM tbl_purchase_orders po " +
                        "WHERE po.po_date >= :startDate",
                null, getFinancialYearStart());

        // Calculate inventory value - using total_value column
        BigDecimal inventoryValue = executeSumQuery(
                "SELECT COALESCE(SUM(inv.total_value), 0) FROM tbl_inventory_balance inv",
                null, null);

        return SummaryCards.builder()
                .totalIndents(totalIndents)
                .pendingIndents(pendingIndents)
                .totalPurchaseOrders(totalPOs)
                .totalGoodsReceipts(totalGRNs)
                .totalIssueNotes(totalIssues)
                .lowStockItems(lowStock)
                .criticalStockItems(criticalStock)
                .activeVendors(activeVendors)
                .totalProcurementValue(procurementValue)
                .totalInventoryValue(inventoryValue)
                .build();
    }

    /**
     * Get indent statistics
     */
    public IndentStats getIndentStats(Integer companyId, Integer departmentId) {
        StringBuilder baseQuery = new StringBuilder("FROM tbl_indent_master i WHERE 1=1");
        if (companyId != null)
            baseQuery.append(" AND i.indent_company = :companyId");
        if (departmentId != null)
            baseQuery.append(" AND i.indent_dept = :departmentId");

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);

        Long total = executeFilteredCount("SELECT COUNT(*) " + baseQuery, companyId, departmentId, null, null);
        Long draft = executeFilteredCount("SELECT COUNT(*) " + baseQuery + " AND i.indent_status = 1", companyId,
                departmentId, null, null);
        Long submitted = executeFilteredCount("SELECT COUNT(*) " + baseQuery + " AND i.indent_status = 2", companyId,
                departmentId, null, null);
        Long approved = executeFilteredCount("SELECT COUNT(*) " + baseQuery + " AND i.indent_status IN (3, 5)",
                companyId, departmentId, null, null);
        Long rejected = executeFilteredCount("SELECT COUNT(*) " + baseQuery + " AND i.indent_status = 4", companyId,
                departmentId, null, null);

        Long thisMonth = executeFilteredCount(
                "SELECT COUNT(*) " + baseQuery + " AND i.indent_date >= :startDate",
                companyId, departmentId, startOfMonth.atStartOfDay(), null);

        Long thisWeek = executeFilteredCount(
                "SELECT COUNT(*) " + baseQuery + " AND i.indent_date >= :startDate",
                companyId, departmentId, startOfWeek.atStartOfDay(), null);

        // Average approval time (in days) - using indent_approvedby_date
        Double avgApprovalDays = executeAvgQuery(
                "SELECT AVG(DATEDIFF(i.indent_approvedby_date, i.indent_date)) " + baseQuery +
                        " AND i.indent_approvedby_date IS NOT NULL AND i.indent_date IS NOT NULL",
                companyId, departmentId);

        return IndentStats.builder()
                .total(total)
                .draft(draft)
                .submitted(submitted)
                .approved(approved)
                .rejected(rejected)
                .inProgress(submitted) // Submitted = In Progress
                .completed(approved)
                .thisMonthCount(thisMonth)
                .thisWeekCount(thisWeek)
                .avgApprovalDays(avgApprovalDays != null ? avgApprovalDays : 0.0)
                .build();
    }

    /**
     * Get purchase order statistics
     * Note: po_status is an integer in the database:
     * 1=Draft, 2=Submitted, 3=Approved, 4=Partially Received, 5=Fully Received,
     * 6=Cancelled
     */
    public PurchaseOrderStats getPurchaseOrderStats(Integer companyId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);

        Long total = executeCountQuery("SELECT COUNT(*) FROM tbl_purchase_orders po WHERE 1=1", null);
        Long pending = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_purchase_orders po WHERE po.po_status IN (1, 2)", null);
        Long approved = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_purchase_orders po WHERE po.po_status = 3", null);
        Long partiallyReceived = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_purchase_orders po WHERE po.po_status = 4", null);
        Long fullyReceived = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_purchase_orders po WHERE po.po_status = 5", null);
        Long cancelled = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_purchase_orders po WHERE po.po_status = 6", null);

        BigDecimal totalValue = executeSumQuery(
                "SELECT COALESCE(SUM(po.total_amount), 0) FROM tbl_purchase_orders po WHERE 1=1",
                null, null);

        Long thisMonth = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_purchase_orders po WHERE po.po_date >= '" + startOfMonth + "'",
                null);

        return PurchaseOrderStats.builder()
                .total(total)
                .pending(pending)
                .approved(approved)
                .partiallyReceived(partiallyReceived)
                .fullyReceived(fullyReceived)
                .cancelled(cancelled)
                .totalValue(totalValue)
                .thisMonthCount(thisMonth)
                .build();
    }

    /**
     * Get goods receipt statistics
     */
    public GoodsReceiptStats getGoodsReceiptStats(Integer companyId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);

        Long total = executeCountQuery("SELECT COUNT(*) FROM tbl_goods_receipt grn WHERE 1=1", null);
        Long thisMonth = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_goods_receipt grn WHERE grn.goods_receipt_date >= '" + startOfMonth + "'",
                null);

        BigDecimal totalValue = executeSumQuery(
                "SELECT COALESCE(SUM(grn.goods_receipt_amount), 0) FROM tbl_goods_receipt grn WHERE 1=1",
                null, null);

        // Count by status (assuming status values similar to other tables)
        Long pendingQC = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_goods_receipt grn WHERE grn.goods_receipt_status = 1", null);
        Long rejectedCount = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_goods_receipt grn WHERE grn.goods_receipt_status = 6", null);

        return GoodsReceiptStats.builder()
                .total(total)
                .thisMonthCount(thisMonth)
                .totalValue(totalValue)
                .pendingQualityCheck(pendingQC)
                .rejectedCount(rejectedCount)
                .build();
    }

    /**
     * Get issue note statistics
     */
    public IssueNoteStats getIssueNoteStats(Integer companyId, Integer departmentId) {
        StringBuilder filter = new StringBuilder("WHERE 1=1");
        if (companyId != null)
            filter.append(" AND isn.issue_note_company = :companyId");
        if (departmentId != null)
            filter.append(" AND isn.issue_note_dept = :departmentId");

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);

        Long total = executeFilteredCount("SELECT COUNT(*) FROM tbl_issue_note isn " + filter, companyId, departmentId,
                null, null);
        Long thisMonth = executeFilteredCount(
                "SELECT COUNT(*) FROM tbl_issue_note isn " + filter + " AND isn.issue_note_date >= :startDate",
                companyId, departmentId, startOfMonth.atStartOfDay(), null);

        // Pending = status 1 or 2
        Long pending = executeFilteredCount(
                "SELECT COUNT(*) FROM tbl_issue_note isn " + filter + " AND isn.issue_note_status IN (1, 2)",
                companyId, departmentId, null, null);

        return IssueNoteStats.builder()
                .total(total)
                .thisMonthCount(thisMonth)
                .pendingApproval(pending)
                .build();
    }

    /**
     * Get inventory statistics
     */
    public InventoryStats getInventoryStats(Integer companyId) {
        Long totalMaterials = executeCountQuery("SELECT COUNT(DISTINCT m.material_id) FROM tbl_material_master m",
                null);
        Long totalSKUs = executeCountQuery("SELECT COUNT(*) FROM tbl_inventory_balance inv", null);

        // Use total_value column for inventory value
        BigDecimal totalValue = executeSumQuery(
                "SELECT COALESCE(SUM(inv.total_value), 0) FROM tbl_inventory_balance inv",
                null, null);

        // Low stock: current_balance < reorder_level
        Long lowStock = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_inventory_balance inv WHERE inv.current_balance < COALESCE(inv.reorder_level, 10) AND inv.current_balance > 0",
                null);

        // Critical stock: current_balance <= min_level (safety stock)
        Long criticalStock = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_inventory_balance inv WHERE inv.current_balance <= COALESCE(inv.min_level, 5) AND inv.current_balance > 0",
                null);

        // Out of stock
        Long outOfStock = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_inventory_balance inv WHERE inv.current_balance = 0 OR inv.current_balance IS NULL",
                null);

        // Overstock: current_balance > max_level or > 3x reorder_level
        Long overstock = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_inventory_balance inv WHERE inv.current_balance > COALESCE(inv.max_level, inv.reorder_level * 3, 100)",
                null);

        // Low stock value
        BigDecimal lowStockValue = executeSumQuery(
                "SELECT COALESCE(SUM(inv.total_value), 0) FROM tbl_inventory_balance inv " +
                        "WHERE inv.current_balance < COALESCE(inv.reorder_level, 10)",
                null, null);

        return InventoryStats.builder()
                .totalMaterials(totalMaterials)
                .totalSKUs(totalSKUs)
                .totalValue(totalValue)
                .lowStockCount(lowStock)
                .criticalStockCount(criticalStock)
                .outOfStockCount(outOfStock)
                .overstockCount(overstock)
                .lowStockValue(lowStockValue)
                .build();
    }

    /**
     * Get vendor statistics
     * Note: status is an integer (1=Active, 0=Inactive)
     */
    public VendorStats getVendorStats() {
        Long total = executeCountQuery("SELECT COUNT(*) FROM tbl_vendors v", null);
        Long active = executeCountQuery("SELECT COUNT(*) FROM tbl_vendors v WHERE v.status = 1", null);
        Long inactive = executeCountQuery("SELECT COUNT(*) FROM tbl_vendors v WHERE v.status = 0", null);

        // No blacklist column in schema, return 0
        Long blacklisted = 0L;

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        Long newThisMonth = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_vendors v WHERE v.created_date >= '" + startOfMonth + "'", null);

        return VendorStats.builder()
                .totalVendors(total)
                .activeVendors(active)
                .inactiveVendors(inactive)
                .blacklistedVendors(blacklisted)
                .newVendorsThisMonth(newThisMonth)
                .build();
    }

    /**
     * Get monthly trends for the last N months
     */
    public List<MonthlyTrend> getMonthlyTrends(Integer companyId, int months) {
        List<MonthlyTrend> trends = new ArrayList<>();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            Long indentCount = executeCountQuery(
                    "SELECT COUNT(*) FROM tbl_indent_master i WHERE i.indent_date BETWEEN '" +
                            startDate + "' AND '" + endDate + "'",
                    companyId);

            Long poCount = executeCountQuery(
                    "SELECT COUNT(*) FROM tbl_purchase_orders po WHERE po.po_date BETWEEN '" +
                            startDate + "' AND '" + endDate + "'",
                    null);

            Long grnCount = executeCountQuery(
                    "SELECT COUNT(*) FROM tbl_goods_receipt grn WHERE grn.goods_receipt_date BETWEEN '" +
                            startDate + "' AND '" + endDate + "'",
                    null);

            Long issueCount = executeCountQuery(
                    "SELECT COUNT(*) FROM tbl_issue_note isn WHERE isn.issue_note_date BETWEEN '" +
                            startDate + "' AND '" + endDate + "'",
                    null);

            trends.add(MonthlyTrend.builder()
                    .month(ym.format(MONTH_FORMATTER))
                    .year(String.valueOf(ym.getYear()))
                    .indentCount(indentCount)
                    .poCount(poCount)
                    .grnCount(grnCount)
                    .issueCount(issueCount)
                    .build());
        }

        return trends;
    }

    /**
     * Get department-wise breakdown
     */
    @SuppressWarnings("unchecked")
    public List<DepartmentWiseData> getDepartmentBreakdown(Integer companyId) {
        String sql = """
                SELECT d.dept_id, d.dept_name, COUNT(i.indent_id) as count
                FROM tbl_department_master d
                LEFT JOIN tbl_indent_master i ON i.indent_dept = d.dept_id
                WHERE d.dept_status = 1
                GROUP BY d.dept_id, d.dept_name
                ORDER BY count DESC
                LIMIT 10
                """;

        List<DepartmentWiseData> result = new ArrayList<>();
        try {
            List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
            long total = rows.stream().mapToLong(r -> ((Number) r[2]).longValue()).sum();

            for (Object[] row : rows) {
                long count = ((Number) row[2]).longValue();
                result.add(DepartmentWiseData.builder()
                        .departmentId(((Number) row[0]).intValue())
                        .departmentName((String) row[1])
                        .indentCount(count)
                        .percentage(total > 0 ? (count * 100.0 / total) : 0.0)
                        .build());
            }
        } catch (Exception e) {
            log.warn("Error fetching department breakdown: {}", e.getMessage());
        }

        return result;
    }

    /**
     * Get top materials by transaction count
     */
    @SuppressWarnings("unchecked")
    public List<TopMaterialData> getTopMaterials(Integer companyId, int limit) {
        String sql = """
                SELECT m.material_id, m.material_code, m.material_name,
                       COALESCE(uom.umo_name, 'N/A') as umo_name,
                       COALESCE(SUM(id.indent_details_qty), 0) as total_qty,
                       COUNT(id.indent_details_id) as tx_count
                FROM tbl_material_master m
                LEFT JOIN tbl_indent_details id ON id.indent_details_material = m.material_id
                LEFT JOIN tbl_umo_master uom ON uom.umo_id = (SELECT MIN(umo_id) FROM tbl_umo_master)
                GROUP BY m.material_id, m.material_code, m.material_name, umo_name
                ORDER BY tx_count DESC
                LIMIT :limit
                """;

        List<TopMaterialData> result = new ArrayList<>();
        try {
            List<Object[]> rows = entityManager.createNativeQuery(sql)
                    .setParameter("limit", limit)
                    .getResultList();

            for (Object[] row : rows) {
                result.add(TopMaterialData.builder()
                        .materialId(((Number) row[0]).intValue())
                        .materialCode((String) row[1])
                        .materialName((String) row[2])
                        .uom((String) row[3])
                        .totalQuantity(row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO)
                        .transactionCount(((Number) row[5]).longValue())
                        .build());
            }
        } catch (Exception e) {
            log.warn("Error fetching top materials: {}", e.getMessage());
        }

        return result;
    }

    /**
     * Get pending approvals for the user's department
     */
    @SuppressWarnings("unchecked")
    public List<PendingApprovalData> getPendingApprovals(Integer companyId, Integer departmentId) {
        String sql = """
                SELECT 'INDENT' as type, i.indent_no, e.emp_name, d.dept_name,
                       i.indent_date, DATEDIFF(NOW(), i.indent_date) as days, i.indent_id
                FROM tbl_indent_master i
                JOIN tbl_emp_master e ON i.indent_createdby = e.emp_number
                JOIN tbl_department_master d ON i.indent_dept = d.dept_id
                WHERE i.indent_status = 2
                ORDER BY i.indent_date ASC
                LIMIT 20
                """;

        List<PendingApprovalData> result = new ArrayList<>();
        try {
            List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();

            for (Object[] row : rows) {
                LocalDate requestDate = null;
                if (row[4] != null) {
                    if (row[4] instanceof java.sql.Date) {
                        requestDate = ((java.sql.Date) row[4]).toLocalDate();
                    } else if (row[4] instanceof java.sql.Timestamp) {
                        requestDate = ((java.sql.Timestamp) row[4]).toLocalDateTime().toLocalDate();
                    } else if (row[4] instanceof java.time.LocalDateTime) {
                        requestDate = ((java.time.LocalDateTime) row[4]).toLocalDate();
                    }
                }

                result.add(PendingApprovalData.builder()
                        .documentType((String) row[0])
                        .documentNumber((String) row[1])
                        .requestorName((String) row[2])
                        .departmentName((String) row[3])
                        .requestDate(requestDate)
                        .pendingDays(row[5] != null ? ((Number) row[5]).intValue() : 0)
                        .documentId(((Number) row[6]).intValue())
                        .status("Pending Approval")
                        .build());
            }
        } catch (Exception e) {
            log.warn("Error fetching pending approvals: {}", e.getMessage());
        }

        return result;
    }

    /**
     * Generate alerts based on current system state
     */
    public List<AlertItem> generateAlerts(Integer companyId) {
        List<AlertItem> alerts = new ArrayList<>();

        // Critical stock alerts - using min_level as safety stock
        Long criticalStock = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_inventory_balance inv WHERE inv.current_balance <= COALESCE(inv.min_level, 5) AND inv.current_balance > 0",
                null);
        if (criticalStock > 0) {
            alerts.add(AlertItem.builder()
                    .type("INVENTORY")
                    .severity("CRITICAL")
                    .message(criticalStock + " items at critical stock level")
                    .actionUrl("/inventory?filter=critical")
                    .count(criticalStock.intValue())
                    .generatedDate(LocalDate.now())
                    .build());
        }

        // Low stock alerts
        Long lowStock = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_inventory_balance inv WHERE inv.current_balance < COALESCE(inv.reorder_level, 10) AND inv.current_balance > COALESCE(inv.min_level, 5)",
                null);
        if (lowStock > 0) {
            alerts.add(AlertItem.builder()
                    .type("INVENTORY")
                    .severity("WARNING")
                    .message(lowStock + " items below reorder level")
                    .actionUrl("/inventory?filter=low-stock")
                    .count(lowStock.intValue())
                    .generatedDate(LocalDate.now())
                    .build());
        }

        // Pending approvals (over 3 days)
        Long pendingApprovals = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_indent_master i WHERE i.indent_status = 2 AND DATEDIFF(NOW(), i.indent_date) > 3",
                null);
        if (pendingApprovals > 0) {
            alerts.add(AlertItem.builder()
                    .type("INDENT")
                    .severity("WARNING")
                    .message(pendingApprovals + " indents pending approval for over 3 days")
                    .actionUrl("/indents?filter=pending")
                    .count(pendingApprovals.intValue())
                    .generatedDate(LocalDate.now())
                    .build());
        }

        // Overdue PO deliveries - po_status < 5 means not fully received
        Long overdueDeliveries = executeCountQuery(
                "SELECT COUNT(*) FROM tbl_purchase_orders po WHERE po.delivery_date < NOW() AND po.po_status NOT IN (5, 6)",
                null);
        if (overdueDeliveries > 0) {
            alerts.add(AlertItem.builder()
                    .type("PURCHASE_ORDER")
                    .severity("WARNING")
                    .message(overdueDeliveries + " POs with overdue deliveries")
                    .actionUrl("/purchase-orders?filter=overdue")
                    .count(overdueDeliveries.intValue())
                    .generatedDate(LocalDate.now())
                    .build());
        }

        return alerts;
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private Long executeCountQuery(String sql, Integer companyId) {
        try {
            var query = entityManager.createNativeQuery(sql);
            if (companyId != null && sql.contains(":companyId")) {
                query.setParameter("companyId", companyId);
            }
            Object result = query.getSingleResult();
            return result != null ? ((Number) result).longValue() : 0L;
        } catch (Exception e) {
            log.debug("Error executing count query: {}", e.getMessage());
            return 0L;
        }
    }

    private BigDecimal executeSumQuery(String sql, Integer companyId, LocalDate startDate) {
        try {
            var query = entityManager.createNativeQuery(sql);
            if (companyId != null && sql.contains(":companyId")) {
                query.setParameter("companyId", companyId);
            }
            if (startDate != null && sql.contains(":startDate")) {
                query.setParameter("startDate", startDate);
            }
            Object result = query.getSingleResult();
            return result != null ? new BigDecimal(result.toString()) : BigDecimal.ZERO;
        } catch (Exception e) {
            log.debug("Error executing sum query: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private Long executeFilteredCount(String sql, Integer companyId, Integer departmentId,
            LocalDateTime startDate, LocalDateTime endDate) {
        try {
            var query = entityManager.createNativeQuery(sql);
            if (companyId != null && sql.contains(":companyId")) {
                query.setParameter("companyId", companyId);
            }
            if (departmentId != null && sql.contains(":departmentId")) {
                query.setParameter("departmentId", departmentId);
            }
            if (startDate != null && sql.contains(":startDate")) {
                query.setParameter("startDate", startDate);
            }
            if (endDate != null && sql.contains(":endDate")) {
                query.setParameter("endDate", endDate);
            }
            Object result = query.getSingleResult();
            return result != null ? ((Number) result).longValue() : 0L;
        } catch (Exception e) {
            log.debug("Error executing filtered count: {}", e.getMessage());
            return 0L;
        }
    }

    private Double executeAvgQuery(String sql, Integer companyId, Integer departmentId) {
        try {
            var query = entityManager.createNativeQuery(sql);
            if (companyId != null && sql.contains(":companyId")) {
                query.setParameter("companyId", companyId);
            }
            if (departmentId != null && sql.contains(":departmentId")) {
                query.setParameter("departmentId", departmentId);
            }
            Object result = query.getSingleResult();
            return result != null ? ((Number) result).doubleValue() : null;
        } catch (Exception e) {
            log.debug("Error executing avg query: {}", e.getMessage());
            return null;
        }
    }

    private LocalDate getFinancialYearStart() {
        LocalDate today = LocalDate.now();
        int year = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
        return LocalDate.of(year, 4, 1);
    }
}
