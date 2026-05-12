package com.nslindia.procurezone.mapping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing the mapping between Company, Plant, and Material
 * for production/manufacturing locations.
 * 
 * Business Purpose:
 * - Tracks material availability at manufacturing plants
 * - Manages production inventory levels
 * - Different from Location mapping (stores vs production)
 * - Supports reorder alerts for production materials
 * 
 * Database: tbl_map_company_plant_material
 * Key Difference: map_quantity_stores (plant inventory) vs map_quantity
 * (location inventory)
 */
@Entity
@Table(name = "tbl_pz_map_company_plant_material")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyPlantMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Integer id;

    /**
     * Foreign key to tbl_company_master.comp_id
     * Represents which company owns this plant-material mapping
     */
    @Column(name = "map_comp", nullable = false)
    private Integer companyId;

    /**
     * Foreign key to tbl_plant_master.plant_id
     * Represents the manufacturing/production plant
     */
    @Column(name = "map_plant", nullable = false)
    private Integer plantId;

    /**
     * Foreign key to tbl_material_master.material_id
     * Represents the material/item stored at this plant
     */
    @Column(name = "map_material", nullable = false)
    private Integer materialId;

    /**
     * Current quantity in stores at the plant
     * Note: Using map_quantity column from legacy database
     */
    @Column(name = "map_quantity", precision = 20, scale = 2)
    private BigDecimal quantityStores;

    /**
     * Reorder level - minimum stock before replenishment needed
     * When quantityStores falls below this, reordering should be triggered
     * Note: Not in current database schema - stored as transient for future use
     */
    @Transient
    private BigDecimal reorderLevel;

    /**
     * Maximum storage capacity at this plant for this material
     * Prevents over-purchasing beyond plant storage limits
     * Note: Not in current database schema - stored as transient for future use
     */
    @Transient
    private BigDecimal maxLevel;

    /**
     * Status indicator:
     * 1 = Active (mapping is currently valid)
     * 0 = Inactive (soft deleted or disabled)
     */
    @Column(name = "map_status", nullable = false)
    private Integer status;

    /**
     * Last Modified Date - audit trail
     */
    @Column(name = "map_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    /**
     * Last Modified User ID
     * Foreign key to tbl_emp_master.emp_id
     */
    @Column(name = "map_lmu", nullable = false)
    private Integer lastModifiedBy;

    // ==================== Business Logic Methods ====================

    /**
     * Check if this mapping is currently active
     * 
     * @return true if status = 1, false otherwise
     */
    public boolean isActive() {
        return status != null && status == 1;
    }

    /**
     * Check if material at this plant needs reordering
     * Returns true when current stock is at or below reorder level
     * 
     * Use Case: Production planning, procurement alerts
     * 
     * @return true if stock needs replenishment
     */
    public boolean needsReorder() {
        if (quantityStores == null || reorderLevel == null) {
            return false;
        }
        return quantityStores.compareTo(reorderLevel) <= 0;
    }

    /**
     * Check if receiving additional quantity would exceed plant storage capacity
     * 
     * Use Case: GRN validation, purchase order approval for plants
     * 
     * @param additionalQuantity The quantity to be added
     * @return true if adding would exceed maxLevel
     */
    public boolean wouldExceedMaxLevel(BigDecimal additionalQuantity) {
        if (quantityStores == null || maxLevel == null || additionalQuantity == null) {
            return false;
        }
        BigDecimal newTotal = quantityStores.add(additionalQuantity);
        return newTotal.compareTo(maxLevel) > 0;
    }

    /**
     * Get available quantity in a null-safe manner
     * 
     * @return Current stock or ZERO if null
     */
    public BigDecimal getAvailableQuantity() {
        return quantityStores != null ? quantityStores : BigDecimal.ZERO;
    }

    /**
     * Check if plant has sufficient stock for production
     * 
     * @param requiredQuantity The quantity needed for production
     * @return true if sufficient stock available
     */
    public boolean hasSufficientStock(BigDecimal requiredQuantity) {
        if (quantityStores == null || requiredQuantity == null) {
            return false;
        }
        return quantityStores.compareTo(requiredQuantity) >= 0;
    }

    /**
     * Calculate remaining capacity at plant
     * 
     * @return Available space for more stock (maxLevel - currentStock)
     */
    public BigDecimal getRemainingCapacity() {
        if (maxLevel == null || quantityStores == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = maxLevel.subtract(quantityStores);
        return remaining.max(BigDecimal.ZERO); // Return 0 if negative
    }

    /**
     * Get status as human-readable text
     * 
     * @return "Active" or "Inactive"
     */
    public String getStatusText() {
        return isActive() ? "Active" : "Inactive";
    }
}
