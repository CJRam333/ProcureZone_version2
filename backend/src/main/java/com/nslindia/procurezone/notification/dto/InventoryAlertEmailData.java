package com.nslindia.procurezone.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for inventory reorder alert email data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAlertEmailData {
    private int criticalCount;
    private int lowStockCount;
    private String criticalItemsTable;
    private String lowStockItemsTable;
    private double totalValueAtRisk;
    private List<String> recipients;
}
