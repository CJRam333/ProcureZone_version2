package com.nslindia.procurezone.grn;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing Quality Control inspection results for Goods Receipt
 * Notes.
 * Maps to tbl_grn_qc_results table.
 * 
 * @author NSL India
 * @version 1.0
 */
@Entity
@Table(name = "tbl_grn_qc_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrnQcResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qc_id")
    private Integer id;

    @Column(name = "grn_id", nullable = false)
    private Integer grnId;

    @Column(name = "inspector_id", nullable = false)
    private Integer inspectorId;

    @Column(name = "inspection_date", nullable = false)
    private LocalDateTime inspectionDate;

    @Column(name = "qc_status", nullable = false)
    private Integer qcStatus; // 1=Pending, 2=Passed, 3=Failed, 4=Conditional

    // ========== Physical Parameters ==========

    @Column(name = "moisture", precision = 10, scale = 2)
    private BigDecimal moisture;

    @Column(name = "pure_seed", precision = 10, scale = 2)
    private BigDecimal pureSeed;

    @Column(name = "inert_matter", precision = 10, scale = 2)
    private BigDecimal inertMatter;

    @Column(name = "ocs_count", precision = 10, scale = 2)
    private BigDecimal ocsCount; // Other Crop Seeds

    @Column(name = "weed_seed_count", precision = 10, scale = 2)
    private BigDecimal weedSeedCount;

    @Column(name = "grain", precision = 10, scale = 2)
    private BigDecimal grain;

    @Column(name = "black_seeds", precision = 10, scale = 2)
    private BigDecimal blackSeeds;

    @Column(name = "pinhole_seed", precision = 10, scale = 2)
    private BigDecimal pinholeSeed;

    @Column(name = "bulk_density", precision = 10, scale = 2)
    private BigDecimal bulkDensity;

    @Column(name = "thsw", precision = 10, scale = 2)
    private BigDecimal thsw; // Thousand Seed Weight

    // ========== Germination Parameters ==========

    @Column(name = "cold_vigour_germ_normal", precision = 10, scale = 2)
    private BigDecimal coldVigourGermNormal;

    @Column(name = "first_count_normal", precision = 10, scale = 2)
    private BigDecimal firstCountNormal;

    @Column(name = "germ_normal", precision = 10, scale = 2)
    private BigDecimal germNormal;

    @Column(name = "fet_normal", precision = 10, scale = 2)
    private BigDecimal fetNormal;

    @Column(name = "soil_count_days", precision = 10, scale = 2)
    private BigDecimal soilCountDays;

    @Column(name = "aav_germ_normal", precision = 10, scale = 2)
    private BigDecimal aavGermNormal;

    // ========== GOT Parameters ==========

    @Column(name = "got", precision = 10, scale = 2)
    private BigDecimal got;

    @Column(name = "got_gp", precision = 10, scale = 2)
    private BigDecimal gotGp;

    @Column(name = "got_female", precision = 10, scale = 2)
    private BigDecimal gotFemale;

    @Column(name = "got_others", precision = 10, scale = 2)
    private BigDecimal gotOthers;

    // ========== Trait Markers ==========

    @Column(name = "bg1", length = 50)
    private String bg1;

    @Column(name = "bg2", length = 50)
    private String bg2;

    @Column(name = "ht", length = 50)
    private String ht;

    @Column(name = "fqr", length = 50)
    private String fqr;

    // ========== Disease/Pest Tests ==========

    @Column(name = "elisa", length = 50)
    private String elisa;

    @Column(name = "stl", length = 50)
    private String stl;

    @Column(name = "odv", length = 50)
    private String odv;

    @Column(name = "odv_res", length = 50)
    private String odvRes;

    // ========== Additional QC Fields ==========

    @Column(name = "q1", length = 100)
    private String q1;

    @Column(name = "q2", length = 100)
    private String q2;

    @Column(name = "q3", length = 100)
    private String q3;

    @Column(name = "q4", length = 100)
    private String q4;

    @Column(name = "q5", length = 100)
    private String q5;

    @Column(name = "q6", length = 100)
    private String q6;

    @Column(name = "q7", length = 100)
    private String q7;

    @Column(name = "q8", length = 100)
    private String q8;

    @Column(name = "q9", length = 100)
    private String q9;

    // ========== Overall Assessment ==========

    @Column(name = "overall_remarks", columnDefinition = "TEXT")
    private String overallRemarks;

    @Column(name = "pass_fail_remarks", columnDefinition = "TEXT")
    private String passFailRemarks;

    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    @Column(name = "re_inspection_required")
    private Boolean reInspectionRequired;

    @Column(name = "re_inspection_date")
    private LocalDateTime reInspectionDate;

    // ========== Audit Fields ==========

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (inspectionDate == null) {
            inspectionDate = LocalDateTime.now();
        }
        if (qcStatus == null) {
            qcStatus = 1; // Pending
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // ========== QC Status Constants ==========

    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PASSED = 2;
    public static final int STATUS_FAILED = 3;
    public static final int STATUS_CONDITIONAL = 4;

    public boolean isPending() {
        return qcStatus != null && qcStatus == STATUS_PENDING;
    }

    public boolean isPassed() {
        return qcStatus != null && qcStatus == STATUS_PASSED;
    }

    public boolean isFailed() {
        return qcStatus != null && qcStatus == STATUS_FAILED;
    }

    public boolean isConditional() {
        return qcStatus != null && qcStatus == STATUS_CONDITIONAL;
    }
}
