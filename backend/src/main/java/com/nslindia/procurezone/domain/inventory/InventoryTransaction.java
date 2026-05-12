package com.nslindia.procurezone.domain.inventory;

import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.Plant;
import com.nslindia.procurezone.masterdata.UnitOfMeasure;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Transaction entity - tracks all inventory movements
 * Maps to tbl_inventory_transaction (new table to be created)
 */
@Entity
@Table(name = "tbl_inventory_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

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

    @Column(name = "transaction_type", length = 50, nullable = false)
    private String transactionType; // GRN, ISSUE_NOTE, ADJUSTMENT, OPENING_BALANCE, TRANSFER

    @Column(name = "transaction_direction", length = 10, nullable = false)
    private String direction; // IN, OUT

    @Column(name = "quantity", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "rate", precision = 15, scale = 2)
    private BigDecimal rate;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "before_balance", precision = 15, scale = 3, nullable = false)
    private BigDecimal beforeBalance;

    @Column(name = "after_balance", precision = 15, scale = 3, nullable = false)
    private BigDecimal afterBalance;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // GRN, ISSUE_NOTE, ADJUSTMENT, etc.

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Employee createdBy;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
        calculateAmount();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateAmount();
    }

    /**
     * Calculate amount = quantity × rate
     */
    private void calculateAmount() {
        if (quantity != null && rate != null) {
            amount = quantity.multiply(rate).setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
}
