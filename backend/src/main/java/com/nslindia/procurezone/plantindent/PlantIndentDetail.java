package com.nslindia.procurezone.plantindent;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.UnitOfMeasure;

/**
 * Entity for Plant Indent line items with QC parameters.
 * Maps to pz_tbl_indent_details table.
 * 
 * Contains QC testing parameters for seed quality:
 * - STL, ODV, GOT, ELISA - Tests
 * - Moisture, Pure Seed, Inert Matter - Physical parameters
 * - Germination values (Cold Vigor, First Count, etc.)
 * - Trait markers (BG1, BG2, HT, etc.)
 * 
 * @author NSL India
 * @version 1.0
 */
@Entity
@Table(name = "pz_tbl_indent_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantIndentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indent_details_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_id", nullable = false)
    private PlantIndent plantIndent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_details_material")
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_details_umo")
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "indent_details_qty", precision = 20, scale = 2)
    private BigDecimal quantity;

    @Column(name = "indent_details_rm_qty", precision = 20, scale = 2)
    private BigDecimal rmQuantity;

    @Column(name = "indent_details_dept_qty", precision = 20, scale = 2)
    private BigDecimal deptQuantity;

    @Column(name = "indent_details_stock_aval", precision = 20, scale = 2)
    private BigDecimal stockAvailable;

    @Column(name = "indent_details_pricing", precision = 20, scale = 2)
    private BigDecimal pricing;

    @Column(name = "indent_details_purpose", columnDefinition = "MEDIUMTEXT")
    private String purpose;

    @Column(name = "indent_details_vendor", length = 200)
    private String vendor;

    @Column(name = "indent_details_status")
    private Integer status;

    @Column(name = "indent_details_lmd")
    private LocalDateTime lastModifiedDate;

    @Column(name = "indent_details_lmu")
    private Integer lastModifiedBy;

    // Plant-specific fields
    @Column(name = "indent_material", length = 100)
    private String indentMaterial;

    @Column(name = "indent_mat_desc", length = 200)
    private String indentMaterialDescription;

    @Column(name = "indent_lot_number", length = 100)
    private String lotNumber;

    @Column(name = "indent_storage_loc", length = 200)
    private String storageLocation;

    // ========== QC TEST PARAMETERS ==========

    @Column(name = "stl", length = 100)
    private String stl; // Seed Testing Lab

    @Column(name = "odv", length = 100)
    private String odv; // Oligonucleotide Detection Value

    @Column(name = "got", length = 100)
    private String got; // Grow Out Test

    @Column(name = "elisa", length = 100)
    private String elisa; // ELISA Test

    @Column(name = "SDCLS", length = 100)
    private String sdcls;

    @Column(name = "STATS", length = 100)
    private String stats;

    @Column(name = "SKIPD", length = 100)
    private String skipd;

    @Column(name = "INSPDT", length = 100)
    private String inspdt;

    // ========== PHYSICAL PARAMETERS ==========

    @Column(name = "MOISTURE", length = 100)
    private String moisture;

    @Column(name = "PURE_SEED", length = 100)
    private String pureSeed;

    @Column(name = "INERT_MATTER", length = 100)
    private String inertMatter;

    @Column(name = "OCS_COUNT", length = 100)
    private String ocsCount;

    @Column(name = "WEED_SEED_COUNT", length = 100)
    private String weedSeedCount;

    @Column(name = "GRAIN", length = 100)
    private String grain;

    @Column(name = "BLACK_SEEDS", length = 100)
    private String blackSeeds;

    @Column(name = "PINHOLE_SEEDS", length = 100)
    private String pinholeSeed;

    @Column(name = "ODV_RES", length = 100)
    private String odvRes;

    @Column(name = "BULK_DENSITY", length = 100)
    private String bulkDensity;

    @Column(name = "THSW", length = 100)
    private String thsw; // Thousand Seed Weight

    // ========== GERMINATION PARAMETERS ==========

    @Column(name = "COLD_VIGOUR_GERM_NORMAL", length = 100)
    private String coldVigourGermNormal;

    @Column(name = "FIRST_COUNT_NORMAL", length = 100)
    private String firstCountNormal;

    @Column(name = "GERM_NORMAL", length = 100)
    private String germNormal;

    @Column(name = "FET_NORMAL", length = 100)
    private String fetNormal;

    @Column(name = "SOIL_COUNT_DAYS", length = 100)
    private String soilCountDays;

    @Column(name = "AAV_GERM_NORMAL", length = 100)
    private String aavGermNormal;

    // ========== GOT PARAMETERS ==========

    @Column(name = "GOT_GP", length = 100)
    private String gotGp;

    @Column(name = "GOT_FEMALE", length = 100)
    private String gotFemale;

    @Column(name = "GOT_OTHERS", length = 100)
    private String gotOthers;

    // ========== TRAIT MARKERS ==========

    @Column(name = "BG1", length = 100)
    private String bg1;

    @Column(name = "BG2", length = 100)
    private String bg2;

    @Column(name = "HT", length = 100)
    private String ht; // Herbicide Tolerance

    @Column(name = "FQR", length = 100)
    private String fqr;

    // ========== ADDITIONAL QC PARAMETERS (Q1-Q9) ==========

    @Column(name = "Q1", length = 100)
    private String q1;

    @Column(name = "Q2", length = 100)
    private String q2;

    @Column(name = "Q3", length = 100)
    private String q3;

    @Column(name = "Q4", length = 100)
    private String q4;

    @Column(name = "Q5", length = 100)
    private String q5;

    @Column(name = "Q6", length = 100)
    private String q6;

    @Column(name = "Q7", length = 100)
    private String q7;

    @Column(name = "Q8", length = 100)
    private String q8;

    @Column(name = "Q9", length = 100)
    private String q9;
}
