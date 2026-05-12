package com.nslindia.procurezone.mapping;

import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.Plant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Company-Plant-Material Mapping Entity
 * Defines which materials are available at which plant
 * Maps to tbl_pz_map_company_plant_material table
 * 
 * @author NSL India
 * @version 1.0
 */
@Entity
@Table(name = "tbl_map_company_plant_material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyPlantMaterialMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_comp", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_plant", nullable = false)
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_material", nullable = false)
    private Material material;

    @Column(name = "map_quantity_stores", precision = 20, scale = 2)
    private BigDecimal quantity;

    @Column(name = "map_reorder_level", precision = 20, scale = 2)
    private BigDecimal reorderLevel;

    @Column(name = "map_max_level", precision = 20, scale = 2)
    private BigDecimal maxLevel;

    @Column(name = "map_status")
    private Integer status;

    @Column(name = "map_lmd", nullable = false)
    private java.time.LocalDate lastModifiedDate;

    @Column(name = "map_lmu", nullable = false)
    private Integer lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        if (lastModifiedDate == null) {
            lastModifiedDate = java.time.LocalDate.now();
        }
        if (status == null) {
            status = 1;
        }
        if (quantity == null) {
            quantity = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = java.time.LocalDate.now();
    }
}
