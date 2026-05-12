package com.nslindia.procurezone.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Inventory Transaction Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {
    private Long id;
    private Integer inventoryId;
    private Integer materialId;
    private String materialCode;
    private String materialName;
    private Integer plantId;
    private String plantCode;
    private String plantName;
    private Integer companyId;
    private String companyCode;
    private String uomCode;
    private String transactionType;
    private String direction;
    private BigDecimal quantity;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal beforeBalance;
    private BigDecimal afterBalance;
    private String referenceType;
    private String referenceNumber;
    private Integer referenceId;
    private String remarks;
    private Integer createdBy;
    private String createdByName;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
}
