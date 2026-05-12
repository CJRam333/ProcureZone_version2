package com.nslindia.procurezone.sap.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for SAP Schedule Material Master (pz_schedule_sap_material_master)
 * Stores materials imported from SAP CSV files via scheduled job
 * 
 * IMPORTANT: This entity matches the LEGACY table structure exactly
 * - PK is company_id (auto-increment)
 * - company_code and plant_code are INT (not VARCHAR)
 * - QC fields are stored as BigDecimal but parsed from String
 * 
 * @author NSL India
 * @version 1.0
 */
@Entity
@Table(name = "pz_schedule_sap_material_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SapScheduleMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id") // FIXED: matches legacy PK column name
    private Integer id;

    @Column(name = "company_code")
    private Integer companyCode; // INT to match legacy

    @Column(name = "plant_code")
    private Integer plantCode; // INT to match legacy

    @Column(name = "storage_location", length = 50)
    private String storageLocation;

    @Column(name = "material_code", length = 100)
    private String materialCode;

    @Column(name = "material_desc", length = 500)
    private String materialDesc;

    @Column(name = "material_uom", length = 20)
    private String materialUom;

    @Column(name = "batch", length = 100)
    private String batch;

    @Column(name = "quantity", precision = 20, scale = 4)
    private BigDecimal quantity;

    @Column(name = "material_type", length = 50)
    private String materialType;

    @Column(name = "material_group")
    private Integer materialGroup;

    @Column(name = "material_group_desc", length = 200)
    private String materialGroupDesc;

    @Column(name = "variety_type", length = 50)
    private String varietyType;

    @Column(name = "variety", length = 100)
    private String variety;

    @Column(name = "crop_type", length = 50)
    private String cropType;

    @Column(name = "crop_group", length = 50)
    private String cropGroup;

    // QC Parameters from SAP
    @Column(name = "stl", length = 50)
    private String stl;

    @Column(name = "odv", length = 50)
    private String odv;

    @Column(name = "got", length = 50)
    private String got;

    @Column(name = "elisa", length = 50)
    private String elisa;

    @Column(name = "status")
    private Integer status;

    @Column(name = "mat_code_id")
    private Integer matCodeId;

    // Additional QC fields
    @Column(name = "SDCLS", length = 50)
    private String sdcls;

    @Column(name = "STATS", length = 50)
    private String stats;

    @Column(name = "SKIPD", length = 50)
    private String skipd;

    @Column(name = "INSPDT", length = 50)
    private String inspdt;

    @Column(name = "MOISTURE", precision = 10, scale = 4)
    private BigDecimal moisture;

    @Column(name = "PURE_SEED", precision = 10, scale = 4)
    private BigDecimal pureSeed;

    @Column(name = "INERT_MATTER", precision = 10, scale = 4)
    private BigDecimal inertMatter;

    @Column(name = "OCS_COUNT", precision = 10, scale = 4)
    private BigDecimal ocsCount;

    @Column(name = "WEED_SEED_COUNT", precision = 10, scale = 4)
    private BigDecimal weedSeedCount;

    @Column(name = "GRAIN", precision = 10, scale = 4)
    private BigDecimal grain;

    @Column(name = "BLACK_SEEDS", precision = 10, scale = 4)
    private BigDecimal blackSeeds;

    @Column(name = "PINHOLE_SEEDS", precision = 10, scale = 4)
    private BigDecimal pinholeSeeds;

    @Column(name = "ODV_RES", length = 50)
    private String odvRes;

    @Column(name = "BULK_DENSITY", precision = 10, scale = 4)
    private BigDecimal bulkDensity;

    @Column(name = "THSW", precision = 10, scale = 4)
    private BigDecimal thsw;

    @Column(name = "COLD_VIGOUR_GERM_NORMAL", precision = 10, scale = 4)
    private BigDecimal coldVigourGermNormal;

    @Column(name = "FIRST_COUNT_NORMAL", precision = 10, scale = 4)
    private BigDecimal firstCountNormal;

    @Column(name = "GERM_NORMAL", precision = 10, scale = 4)
    private BigDecimal germNormal;

    @Column(name = "FET_NORMAL", precision = 10, scale = 4)
    private BigDecimal fetNormal;

    @Column(name = "SOIL_COUNT_DAYS", precision = 10, scale = 4)
    private BigDecimal soilCountDays;

    @Column(name = "AAV_GERM_NORMAL", precision = 10, scale = 4)
    private BigDecimal aavGermNormal;

    @Column(name = "GOT_GP", precision = 10, scale = 4)
    private BigDecimal gotGp;

    @Column(name = "GOT_FEMALE", precision = 10, scale = 4)
    private BigDecimal gotFemale;

    @Column(name = "GOT_OTHERS", precision = 10, scale = 4)
    private BigDecimal gotOthers;

    @Column(name = "BG1", length = 50)
    private String bg1;

    @Column(name = "BG2", length = 50)
    private String bg2;

    @Column(name = "HT", length = 50)
    private String ht;

    @Column(name = "FQR", length = 50)
    private String fqr;

    @Column(name = "stp_one", length = 50)
    private String stpOne;

    @Column(name = "lmd")
    private LocalDateTime lastModifiedDate;

    @PrePersist
    public void prePersist() {
        if (lastModifiedDate == null) {
            lastModifiedDate = LocalDateTime.now();
        }
        if (status == null) {
            status = 1;
        }
    }
}
