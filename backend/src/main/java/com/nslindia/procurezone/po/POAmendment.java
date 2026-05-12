package com.nslindia.procurezone.po;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * B.1 FIX: PO Amendment Entity
 * Tracks all amendments made to Purchase Orders after vendor confirmation
 * Maintains full audit trail of original vs amended values
 */
@Entity
@Table(name = "tbl_po_amendments")
public class POAmendment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "po_id", nullable = false)
    private Integer poId;

    @Column(name = "amendment_version", nullable = false)
    private Integer amendmentVersion;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "original_value", columnDefinition = "TEXT")
    private String originalValue;

    @Column(name = "amended_value", columnDefinition = "TEXT")
    private String amendedValue;

    @Column(name = "amendment_reason", columnDefinition = "TEXT")
    private String amendmentReason;

    @Column(name = "amended_by", nullable = false)
    private Integer amendedBy;

    @Column(name = "amended_date", nullable = false)
    private LocalDateTime amendedDate;

    @Column(name = "approved_by")
    private Integer approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED

    // Constructors
    protected POAmendment() {
    }

    public POAmendment(Integer poId, Integer amendmentVersion, String fieldName,
            String originalValue, String amendedValue, String amendmentReason,
            Integer amendedBy) {
        this.poId = poId;
        this.amendmentVersion = amendmentVersion;
        this.fieldName = fieldName;
        this.originalValue = originalValue;
        this.amendedValue = amendedValue;
        this.amendmentReason = amendmentReason;
        this.amendedBy = amendedBy;
        this.amendedDate = LocalDateTime.now();
        this.status = "APPROVED"; // Auto-approved for tracking purposes
        this.approvedBy = amendedBy;
        this.approvedDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPoId() {
        return poId;
    }

    public void setPoId(Integer poId) {
        this.poId = poId;
    }

    public Integer getAmendmentVersion() {
        return amendmentVersion;
    }

    public void setAmendmentVersion(Integer amendmentVersion) {
        this.amendmentVersion = amendmentVersion;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getAmendedValue() {
        return amendedValue;
    }

    public void setAmendedValue(String amendedValue) {
        this.amendedValue = amendedValue;
    }

    public String getAmendmentReason() {
        return amendmentReason;
    }

    public void setAmendmentReason(String amendmentReason) {
        this.amendmentReason = amendmentReason;
    }

    public Integer getAmendedBy() {
        return amendedBy;
    }

    public void setAmendedBy(Integer amendedBy) {
        this.amendedBy = amendedBy;
    }

    public LocalDateTime getAmendedDate() {
        return amendedDate;
    }

    public void setAmendedDate(LocalDateTime amendedDate) {
        this.amendedDate = amendedDate;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
