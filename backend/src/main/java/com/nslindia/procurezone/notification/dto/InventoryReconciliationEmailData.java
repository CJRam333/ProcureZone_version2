package com.nslindia.procurezone.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for Inventory Reconciliation Report email data
 * E.2 FIX: Used by InventoryReconciliationJob
 * 
 * @author NSL India
 * @version 1.0
 */
@Data
@Builder
public class InventoryReconciliationEmailData {

    /**
     * Report date/month
     */
    private String reportPeriod;

    /**
     * Number of materials with discrepancies
     */
    private int discrepancyCount;

    /**
     * HTML table of discrepancies found
     */
    private String discrepanciesTable;

    /**
     * Total variance in quantity
     */
    private double totalQuantityVariance;

    /**
     * Total variance in value
     */
    private double totalValueVariance;

    /**
     * Number of negative balance items
     */
    private int negativeBalanceCount;

    /**
     * HTML table of negative balance items
     */
    private String negativeBalanceTable;

    /**
     * Number of orphaned transactions
     */
    private int orphanedTransactionCount;

    /**
     * Total materials checked
     */
    private int totalMaterialsChecked;

    /**
     * Reconciliation status (PASS, ISSUES_FOUND, ERROR)
     */
    private String reconciliationStatus;

    /**
     * List of recipients for this email
     */
    private List<String> recipients;

    /**
     * Detailed error section if any
     */
    private String errorSection;
}
