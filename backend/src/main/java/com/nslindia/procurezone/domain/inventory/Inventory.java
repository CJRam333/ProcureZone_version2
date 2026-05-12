package com.nslindia.procurezone.domain.inventory;

import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.Plant;
import com.nslindia.procurezone.masterdata.UnitOfMeasure;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory entity - tracks current stock balance
 * Maps to tbl_inventory_balance (new table to be created)
 */
@Entity
@Table(name = "tbl_inventory_balance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "umo_id", nullable = false)
    private UnitOfMeasure unitOfMeasure;

    @Builder.Default
    @Column(name = "opening_balance", precision = 15, scale = 3, nullable = false)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "current_balance", precision = 15, scale = 3, nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "reserved_quantity", precision = 15, scale = 3, nullable = false)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "available_quantity", precision = 15, scale = 3, nullable = false)
    private BigDecimal availableQuantity = BigDecimal.ZERO;

    @Column(name = "reorder_level", precision = 15, scale = 3)
    private BigDecimal reorderLevel;

    @Column(name = "max_level", precision = 15, scale = 3)
    private BigDecimal maxLevel;

    @Column(name = "min_level", precision = 15, scale = 3)
    private BigDecimal minLevel;

    @Builder.Default
    @Column(name = "avg_rate", precision = 15, scale = 2)
    private BigDecimal avgRate = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    @Column(name = "last_receipt_date")
    private LocalDateTime lastReceiptDate;

    @Column(name = "last_issue_date")
    private LocalDateTime lastIssueDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
        calculateAvailableQuantity();
        calculateTotalValue();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
        calculateAvailableQuantity();
        calculateTotalValue();
    }

    /**
     * Calculate available quantity = current balance - reserved quantity
     */
    private void calculateAvailableQuantity() {
        if (currentBalance != null && reservedQuantity != null) {
            availableQuantity = currentBalance.subtract(reservedQuantity);
        }
    }

    /**
     * Calculate total value = current balance × average rate
     */
    private void calculateTotalValue() {
        if (currentBalance != null && avgRate != null) {
            totalValue = currentBalance.multiply(avgRate).setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Check if stock is below reorder level
     */
    public boolean isBelowReorderLevel() {
        return reorderLevel != null && availableQuantity != null
                && availableQuantity.compareTo(reorderLevel) < 0;
    }

    /**
     * Check if stock is below minimum level
     */
    public boolean isBelowMinLevel() {
        return minLevel != null && availableQuantity != null
                && availableQuantity.compareTo(minLevel) < 0;
    }

    /**
     * Check if stock is above maximum level
     */
    public boolean isAboveMaxLevel() {
        return maxLevel != null && currentBalance != null
                && currentBalance.compareTo(maxLevel) > 0;
    }
}
