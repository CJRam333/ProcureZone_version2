package com.nslindia.procurezone.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for daily inventory summary email data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryEmailData {
    private int totalMaterials;
    private double totalInventoryValue;
    private int lowStockCount;
    private int criticalStockCount;
    private int receiptsCount;
    private double receiptsValue;
    private int issuesCount;
    private double issuesValue;
    private int adjustmentsCount;
    private double netMovement;
    private String topMaterialsTable;
    private List<String> recipients;
}
