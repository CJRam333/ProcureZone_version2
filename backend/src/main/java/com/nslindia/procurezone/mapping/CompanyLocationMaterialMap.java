package com.nslindia.procurezone.mapping;

import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Location;
import com.nslindia.procurezone.masterdata.Material;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Company-Location-Material Mapping Entity
 * Defines which materials are available at which location
 * Maps to tbl_map_company_location_material table
 * 
 * @author NSL India
 * @version 1.0
 */
@Entity
@Table(name = "tbl_map_company_location_material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyLocationMaterialMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_comp", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_loc", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_material", nullable = false)
    private Material material;

    @Column(name = "map_status")
    private Boolean isActive;

    @Column(name = "min_stock_level", precision = 20, scale = 2)
    private BigDecimal minStockLevel;

    @Column(name = "max_stock_level", precision = 20, scale = 2)
    private BigDecimal maxStockLevel;

    @Column(name = "reorder_level", precision = 20, scale = 2)
    private BigDecimal reorderLevel;

    @Column(name = "reorder_quantity", precision = 20, scale = 2)
    private BigDecimal reorderQuantity;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "map_lmu")
    private Integer lastModifiedBy;

    @Column(name = "map_lmd")
    private LocalDateTime lastModifiedDate;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (lastModifiedDate == null) {
            lastModifiedDate = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (minStockLevel == null) {
            minStockLevel = BigDecimal.ZERO;
        }
        if (maxStockLevel == null) {
            maxStockLevel = BigDecimal.ZERO;
        }
        if (reorderLevel == null) {
            reorderLevel = BigDecimal.ZERO;
        }
        if (reorderQuantity == null) {
            reorderQuantity = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
