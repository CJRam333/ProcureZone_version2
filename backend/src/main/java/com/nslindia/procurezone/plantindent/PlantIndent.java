package com.nslindia.procurezone.plantindent;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for Plant-specific Indents (R&D / Production workflow).
 * Maps to pz_tbl_indent_masterb table (legacy schema).
 * 
 * 8-Status Workflow:
 * 0 = Draft (created but not submitted)
 * 1 = Pending DEO/QM Review
 * 2 = Pending Manager Approval
 * 3 = Manager Approved
 * 4 = Rejected by DEO/QM
 * 5 = Rejected by Manager
 * 6 = Processing (materials being issued)
 * 7 = Completed
 * 
 * @author NSL India
 * @version 2.0
 */
@Entity
@Table(name = "pz_tbl_indent_masterb")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantIndent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indent_id")
    private Integer id;

    @Column(name = "indent_no", length = 100)
    private String indentNumber;

    @Column(name = "indent_year", length = 45)
    private String indentYear;

    @Column(name = "indent_date", length = 20)
    private String indentDate;

    // Company FK
    @Column(name = "indent_company")
    private Integer companyId;

    // Department FK
    @Column(name = "indent_dept")
    private Integer departmentId;

    // Section FK — present in legacy pz_tbl_indent_masterb schema (indent_sec column)
    @Column(name = "indent_sec")
    private Integer sectionId;

    // Plant FK
    @Column(name = "indent_plant")
    private Integer plantId;

    // We keep a transient plant name for display
    @Transient
    private String plantName;

    // Employee FK
    @Column(name = "indent_emp")
    private Integer employeeId;

    // Transient employee number string for API compatibility
    @Transient
    private String employeeNumber;

    @Column(name = "indent_comments", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "indent_delivery_date", length = 20)
    private String deliveryDate;

    @Column(name = "indent_po_number", length = 100)
    private String poNumber;

    // Indent code (for backward compat with old entity)
    @Transient
    private String indentCode;

    // ========== APPROVAL WORKFLOW FIELDS ==========

    @Column(name = "indent_approvedby")
    private Integer approvedBy;

    @Column(name = "indent_approvedby_date", length = 20)
    private String approvedByDate;

    @Column(name = "indent_approved_status")
    private Integer approvedStatus;

    @Column(name = "indent_final_approvedby")
    private Integer finalApprovedBy;

    @Column(name = "indent_final_date", length = 20)
    private String finalDate;

    @Column(name = "indent_final_status")
    private Integer finalStatus;

    @Column(name = "indent_final_remarks", columnDefinition = "MEDIUMTEXT")
    private String finalRemarks;

    @Column(name = "indent_remarks", columnDefinition = "MEDIUMTEXT")
    private String indentRemarks;

    @Column(name = "indent_procurementby")
    private Integer procurementBy;

    @Column(name = "indent_procurement_status")
    private Integer procurementStatus;

    @Column(name = "indent_createdby")
    private Integer createdBy;

    // ========== PLANT-SPECIFIC FIELDS ==========

    @Column(name = "indent_crop_type")
    private Integer cropTypeId;

    @Column(name = "indent_crop", length = 100)
    private String cropName;

    @Column(name = "inden_proc_pack", length = 20)
    private String packProcess;

    @Column(name = "indent_out_material", length = 20)
    private String outputMaterial;

    @Column(name = "indent_out_desc", length = 20)
    private String outputDescription;

    @Column(name = "indent_batchnumber", length = 20)
    private String batchNumber;

    @Column(name = "indent_uom", length = 20)
    private String masterUom;

    @Column(name = "indent_linecode", length = 20)
    private String lineCode;

    @Column(name = "indent_linedesc")
    private String lineDescription;

    @Column(name = "indent_outqty")
    private Integer expectedQuantity;

    @Column(name = "indent_order_type")
    private String orderType;

    @Column(name = "indent_order_desc")
    private String orderDescription;

    @Column(name = "indent_batch_number")
    private String batchNumberAlt;

    @Column(name = "indent_pack_processinga")
    private String packProcessAlt;

    @Column(name = "indent_actual_outqty", precision = 20)
    private java.math.BigDecimal actualOutputQuantity;

    @Column(name = "indent_final_number")
    private String finalNumber;

    @Column(name = "indent_grn_number")
    private String grnNumber;

    @Column(name = "indent_startdate", length = 20)
    private String startDate;

    @Column(name = "indent_flincahrge_commants", columnDefinition = "MEDIUMTEXT")
    private String floorInchargeComments;

    @Column(name = "indent_grn_commants", columnDefinition = "MEDIUMTEXT")
    private String grnComments;

    @Column(name = "indent_storage_location", columnDefinition = "MEDIUMTEXT")
    private String storageLocation;

    // ========== STATUS & TIMESTAMPS ==========

    @Column(name = "indent_status")
    @Builder.Default
    private Integer status = 0;

    @Column(name = "indent_lmd")
    private LocalDateTime lastModifiedDate;

    @Column(name = "indent_lmu")
    private Integer lastModifiedBy;

    @Transient
    private String statusDescription;

    @Transient
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        lastModifiedDate = LocalDateTime.now();
        if (status == null)
            status = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "plantIndent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PlantIndentDetail> details = new ArrayList<>();

    // Helper methods
    public void addDetail(PlantIndentDetail detail) {
        details.add(detail);
        detail.setPlantIndent(this);
    }

    public void removeDetail(PlantIndentDetail detail) {
        details.remove(detail);
        detail.setPlantIndent(null);
    }

    // Status helper methods
    public boolean isDraft() {
        return status != null && status == 0;
    }

    public boolean isPendingDeoReview() {
        return status != null && status == 1;
    }

    public boolean isPendingManagerApproval() {
        return status != null && status == 2;
    }

    public boolean isApproved() {
        return status != null && status == 3;
    }

    public boolean isRejectedByDeo() {
        return status != null && status == 4;
    }

    public boolean isRejectedByManager() {
        return status != null && status == 5;
    }

    public boolean isProcessing() {
        return status != null && status == 6;
    }

    public boolean isCompleted() {
        return status != null && status == 7;
    }

    public String getStatusName() {
        if (status == null)
            return "Unknown";
        return switch (status) {
            case 0 -> "Draft";
            case 1 -> "Pending DEO/QM Review";
            case 2 -> "Pending Manager Approval";
            case 3 -> "Manager Approved";
            case 4 -> "Rejected by DEO/QM";
            case 5 -> "Rejected by Manager";
            case 6 -> "Processing";
            case 7 -> "Completed";
            default -> "Unknown";
        };
    }

    // Convenience: get employeeNumber as string from employeeId
    public String getEmployeeNumber() {
        if (employeeNumber != null) return employeeNumber;
        return employeeId != null ? String.valueOf(employeeId) : null;
    }

    // Convenience: get indentCode
    public String getIndentCode() {
        if (indentCode != null) return indentCode;
        return indentNumber != null ? "PZ-" + indentNumber : null;
    }

    public LocalDateTime getCreatedDate() {
        // Use lastModifiedDate as proxy for created date
        return createdDate != null ? createdDate : lastModifiedDate;
    }
}
