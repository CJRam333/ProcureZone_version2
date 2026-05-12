package com.nslindia.procurezone.mapping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing the mapping between Company, Location, and Material.
 * Tracks material availability, stock levels, and reorder parameters at
 * specific locations.
 */
@Entity
@Table(name = "tbl_map_company_location_material")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyLocationMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Integer id;

    /**
     * Company ID (Foreign key to tbl_company_master.comp_id)
     */
    @Column(name = "map_comp", nullable = false)
    private Integer companyId;

    /**
     * Location ID (Foreign key to tbl_location_master.loc_id)
     */
    @Column(name = "map_loc", nullable = false)
    private Integer locationId;

    /**
     * Material ID (Foreign key to tbl_material_master.material_id)
     */
    @Column(name = "map_material", nullable = false)
    private Integer materialId;

    /**
     * Current quantity available at this location
     */
    @Column(name = "map_quantity", precision = 20, scale = 2)
    private BigDecimal quantity;

    /**
     * Reorder level - when stock falls below this, reorder is triggered
     */
    @Column(name = "map_reorder_level", precision = 20, scale = 2)
    private BigDecimal reorderLevel;

    /**
     * Maximum stock level for this location
     */
    @Column(name = "map_max_level", precision = 20, scale = 2)
    private BigDecimal maxLevel;

    /**
     * Status of the mapping (1 = Active, 0 = Inactive)
     */
    @Column(name = "map_status", nullable = false)
    private Integer status;

    /**
     * Last modified date
     */
    @Column(name = "map_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    /**
     * Last modified by (Employee ID)
     */
    @Column(name = "map_lmu", nullable = false)
    private Integer lastModifiedBy;

    /**
     * Check if this mapping is active
     */
    public boolean isActive() {
        return status != null && status == 1;
    }

    /**
     * Check if stock is below reorder level
     */
    public boolean needsReorder() {
        if (quantity == null || reorderLevel == null) {
            return false;
        }
        return quantity.compareTo(reorderLevel) <= 0;
    }

    /**
     * Check if adding quantity would exceed max level
     */
    public boolean wouldExceedMaxLevel(BigDecimal additionalQuantity) {
        if (quantity == null || maxLevel == null || additionalQuantity == null) {
            return false;
        }
        return quantity.add(additionalQuantity).compareTo(maxLevel) > 0;
    }

    /**
     * Get available quantity for issue
     */
    public BigDecimal getAvailableQuantity() {
        return quantity != null ? quantity : BigDecimal.ZERO;
    }
}
