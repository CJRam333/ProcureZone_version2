package com.nslindia.procurezone.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard Statistics Response DTO
 * Aggregates key metrics from all modules for dashboard display
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {

    // Summary Cards
    private SummaryCards summary;

    // Module-wise Statistics
    private IndentStats indentStats;
    private PurchaseOrderStats purchaseOrderStats;
    private GoodsReceiptStats goodsReceiptStats;
    private IssueNoteStats issueNoteStats;
    private InventoryStats inventoryStats;
    private VendorStats vendorStats;

    // Charts Data
    private List<MonthlyTrend> monthlyTrends;
    private List<DepartmentWiseData> departmentBreakdown;
    private List<TopMaterialData> topMaterials;
    private List<PendingApprovalData> pendingApprovals;

    // Alerts & Notifications
    private List<AlertItem> alerts;

    /**
     * Summary cards for top-level metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryCards {
        private Long totalIndents;
        private Long pendingIndents;
        private Long totalPurchaseOrders;
        private Long totalGoodsReceipts;
        private Long totalIssueNotes;
        private Long lowStockItems;
        private Long criticalStockItems;
        private Long activeVendors;
        private BigDecimal totalProcurementValue;
        private BigDecimal totalInventoryValue;
    }

    /**
     * Indent module statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndentStats {
        private Long total;
        private Long draft;
        private Long submitted;
        private Long approved;
        private Long rejected;
        private Long inProgress;
        private Long completed;
        private BigDecimal totalValue;
        private Double avgApprovalDays;
        private Long thisMonthCount;
        private Long thisWeekCount;
        private Long overdueCount;
    }

    /**
     * Purchase Order statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseOrderStats {
        private Long total;
        private Long pending;
        private Long approved;
        private Long partiallyReceived;
        private Long fullyReceived;
        private Long cancelled;
        private BigDecimal totalValue;
        private Long thisMonthCount;
        private Long overdueDeliveries;
    }

    /**
     * Goods Receipt statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoodsReceiptStats {
        private Long total;
        private Long thisMonthCount;
        private BigDecimal totalValue;
        private Long pendingQualityCheck;
        private Long rejectedCount;
    }

    /**
     * Issue Note statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueNoteStats {
        private Long total;
        private Long thisMonthCount;
        private BigDecimal totalValue;
        private Long pendingApproval;
    }

    /**
     * Inventory statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryStats {
        private Long totalMaterials;
        private Long totalSKUs;
        private BigDecimal totalValue;
        private Long lowStockCount;
        private Long criticalStockCount;
        private Long outOfStockCount;
        private Long overstockCount;
        private BigDecimal lowStockValue;
    }

    /**
     * Vendor statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorStats {
        private Long totalVendors;
        private Long activeVendors;
        private Long inactiveVendors;
        private Long blacklistedVendors;
        private Long newVendorsThisMonth;
    }

    /**
     * Monthly trend data for charts
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private String month;
        private String year;
        private Long indentCount;
        private Long poCount;
        private Long grnCount;
        private Long issueCount;
        private BigDecimal procurementValue;
        private BigDecimal issueValue;
    }

    /**
     * Department-wise breakdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentWiseData {
        private Integer departmentId;
        private String departmentName;
        private Long indentCount;
        private BigDecimal totalValue;
        private Double percentage;
    }

    /**
     * Top materials by consumption/value
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopMaterialData {
        private Integer materialId;
        private String materialCode;
        private String materialName;
        private String uom;
        private BigDecimal totalQuantity;
        private BigDecimal totalValue;
        private Long transactionCount;
    }

    /**
     * Pending approval items
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingApprovalData {
        private String documentType;
        private String documentNumber;
        private String requestorName;
        private String departmentName;
        private LocalDate requestDate;
        private Integer pendingDays;
        private BigDecimal value;
        private String status;
        private Integer documentId;
    }

    /**
     * Alert items for dashboard
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertItem {
        private String type;
        private String severity; // CRITICAL, WARNING, INFO
        private String message;
        private String actionUrl;
        private Integer count;
        private LocalDate generatedDate;
    }
}
