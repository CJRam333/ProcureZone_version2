package com.nslindia.procurezone.dashboard.controller;

import com.nslindia.procurezone.dashboard.dto.DashboardStatisticsResponse;
import com.nslindia.procurezone.dashboard.dto.DashboardStatisticsResponse.*;
import com.nslindia.procurezone.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Dashboard Statistics
 * Provides comprehensive analytics and metrics for the dashboard
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get comprehensive dashboard statistics
     * 
     * @param companyId    Optional company filter
     * @param departmentId Optional department filter
     * @return Complete dashboard statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'VIEWER', 'DEPTHEAD', 'FLOORINCHARGE', 'GOODSINCHARGE', 'ISSUECONFIRM', 'RECEIPTCONFIRM', 'PROCUREMENT', 'SUPERVISOR')")
    public ResponseEntity<DashboardStatisticsResponse> getDashboardStatistics(
            @RequestParam(required = false) Integer companyId,
            @RequestParam(required = false) Integer departmentId,
            Authentication authentication) {

        log.info("Fetching dashboard statistics for user: {}",
                authentication != null ? authentication.getName() : "anonymous");

        DashboardStatisticsResponse response = dashboardService.getDashboardStatistics(companyId, departmentId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get summary cards data only
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'VIEWER', 'DEPTHEAD', 'FLOORINCHARGE', 'GOODSINCHARGE', 'ISSUECONFIRM', 'RECEIPTCONFIRM', 'PROCUREMENT', 'SUPERVISOR')")
    public ResponseEntity<SummaryCards> getSummaryCards(
            @RequestParam(required = false) Integer companyId) {

        log.debug("Fetching summary cards");

        SummaryCards response = dashboardService.getSummaryCards(companyId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get indent statistics
     */
    @GetMapping("/indents/stats")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'VIEWER', 'DEPTHEAD', 'FLOORINCHARGE', 'GOODSINCHARGE', 'ISSUECONFIRM', 'RECEIPTCONFIRM', 'PROCUREMENT', 'SUPERVISOR')")
    public ResponseEntity<IndentStats> getIndentStats(
            @RequestParam(required = false) Integer companyId,
            @RequestParam(required = false) Integer departmentId) {

        log.debug("Fetching indent statistics");

        IndentStats response = dashboardService.getIndentStats(companyId, departmentId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get purchase order statistics
     */
    @GetMapping("/purchase-orders/stats")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER', 'DEPTHEAD', 'FLOORINCHARGE', 'GOODSINCHARGE', 'USER', 'SUPERVISOR')")
    public ResponseEntity<PurchaseOrderStats> getPurchaseOrderStats(
            @RequestParam(required = false) Integer companyId) {

        log.debug("Fetching purchase order statistics");

        PurchaseOrderStats response = dashboardService.getPurchaseOrderStats(companyId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get goods receipt statistics
     */
    @GetMapping("/goods-receipts/stats")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'FLOORINCHARGE', 'GOODSINCHARGE', 'VIEWER', 'DEPTHEAD', 'PROCUREMENT', 'USER', 'SUPERVISOR')")
    public ResponseEntity<GoodsReceiptStats> getGoodsReceiptStats(
            @RequestParam(required = false) Integer companyId) {

        log.debug("Fetching goods receipt statistics");

        GoodsReceiptStats response = dashboardService.getGoodsReceiptStats(companyId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get issue note statistics
     */
    @GetMapping("/issue-notes/stats")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'FLOORINCHARGE', 'GOODSINCHARGE', 'VIEWER', 'DEPTHEAD', 'PROCUREMENT', 'USER', 'SUPERVISOR')")
    public ResponseEntity<IssueNoteStats> getIssueNoteStats(
            @RequestParam(required = false) Integer companyId,
            @RequestParam(required = false) Integer departmentId) {

        log.debug("Fetching issue note statistics");

        IssueNoteStats response = dashboardService.getIssueNoteStats(companyId, departmentId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get inventory statistics
     */
    @GetMapping("/inventory/stats")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'FLOORINCHARGE', 'GOODSINCHARGE', 'VIEWER', 'DEPTHEAD', 'PROCUREMENT', 'USER', 'SUPERVISOR')")
    public ResponseEntity<InventoryStats> getInventoryStats(
            @RequestParam(required = false) Integer companyId) {

        log.debug("Fetching inventory statistics");

        InventoryStats response = dashboardService.getInventoryStats(companyId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get vendor statistics
     */
    @GetMapping("/vendors/stats")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'PROCUREMENT', 'VIEWER', 'DEPTHEAD', 'FLOORINCHARGE', 'GOODSINCHARGE', 'USER', 'SUPERVISOR')")
    public ResponseEntity<VendorStats> getVendorStats() {

        log.debug("Fetching vendor statistics");

        VendorStats response = dashboardService.getVendorStats();

        return ResponseEntity.ok(response);
    }

    /**
     * Get monthly trends for charts
     */
    @GetMapping("/trends/monthly")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'VIEWER', 'DEPTHEAD', 'FLOORINCHARGE', 'GOODSINCHARGE', 'PROCUREMENT', 'USER', 'SUPERVISOR')")
    public ResponseEntity<List<MonthlyTrend>> getMonthlyTrends(
            @RequestParam(required = false) Integer companyId,
            @RequestParam(defaultValue = "6") int months) {

        log.debug("Fetching monthly trends for last {} months", months);

        List<MonthlyTrend> response = dashboardService.getMonthlyTrends(companyId, months);

        return ResponseEntity.ok(response);
    }

    /**
     * Get department-wise breakdown
     */
    @GetMapping("/breakdown/department")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'VIEWER', 'DEPTHEAD', 'FLOORINCHARGE', 'GOODSINCHARGE', 'PROCUREMENT', 'USER', 'SUPERVISOR')")
    public ResponseEntity<List<DepartmentWiseData>> getDepartmentBreakdown(
            @RequestParam(required = false) Integer companyId) {

        log.debug("Fetching department breakdown");

        List<DepartmentWiseData> response = dashboardService.getDepartmentBreakdown(companyId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get top materials by consumption
     */
    @GetMapping("/top-materials")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'VIEWER', 'DEPTHEAD', 'FLOORINCHARGE', 'GOODSINCHARGE', 'PROCUREMENT', 'USER', 'SUPERVISOR')")
    public ResponseEntity<List<TopMaterialData>> getTopMaterials(
            @RequestParam(required = false) Integer companyId,
            @RequestParam(defaultValue = "10") int limit) {

        log.debug("Fetching top {} materials", limit);

        List<TopMaterialData> response = dashboardService.getTopMaterials(companyId, limit);

        return ResponseEntity.ok(response);
    }

    /**
     * Get pending approvals
     */
    @GetMapping("/pending-approvals")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'VIEWER', 'DEPTHEAD', 'FLOORINCHARGE', 'GOODSINCHARGE', 'PROCUREMENT', 'USER', 'SUPERVISOR')")
    public ResponseEntity<List<PendingApprovalData>> getPendingApprovals(
            @RequestParam(required = false) Integer companyId,
            @RequestParam(required = false) Integer departmentId) {

        log.debug("Fetching pending approvals");

        List<PendingApprovalData> response = dashboardService.getPendingApprovals(companyId, departmentId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get system alerts
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'VIEWER', 'DEPTHEAD', 'FLOORINCHARGE', 'GOODSINCHARGE', 'PROCUREMENT', 'USER', 'SUPERVISOR')")
    public ResponseEntity<List<AlertItem>> getAlerts(
            @RequestParam(required = false) Integer companyId) {

        log.debug("Fetching system alerts");

        List<AlertItem> response = dashboardService.generateAlerts(companyId);

        return ResponseEntity.ok(response);
    }
}
