package com.nslindia.procurezone.indent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nslindia.procurezone.masterdata.Material;
import com.nslindia.procurezone.masterdata.UnitOfMeasure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity representing a line item (material request) in an indent.
 * Maps to tbl_indent_details table.
 */
@Entity
@Table(name = "tbl_indent_details")
public class IndentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indent_details_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_id", nullable = false)
    private Indent indent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_details_material", nullable = false)
    private Material material;

    /**
     * The SPECIFIC company selected for this line at creation (the material+company combination the
     * user picked in the dropdown). Nullable — rows created before this feature have no value, in
     * which case the display falls back to the multi-company resolver. Stored as a plain id (no FK).
     */
    @Column(name = "indent_details_company")
    private Integer companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_details_umo", nullable = false)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "indent_details_qty", nullable = false, precision = 20, scale = 2)
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

    @Column(name = "indent_details_status", nullable = false)
    private Integer status;

    @Column(name = "indent_details_lmd", nullable = false)
    private LocalDateTime lastModifiedDate;

    @Column(name = "indent_details_lmu", nullable = false)
    private Integer lastModifiedBy;

    protected IndentDetail() {
    }

    // Getters and setters

    public Integer getId() {
        return id;
    }

    public Indent getIndent() {
        return indent;
    }

    public void setIndent(Indent indent) {
        this.indent = indent;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public UnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getRmQuantity() {
        return rmQuantity;
    }

    public void setRmQuantity(BigDecimal rmQuantity) {
        this.rmQuantity = rmQuantity;
    }

    public BigDecimal getDeptQuantity() {
        return deptQuantity;
    }

    public void setDeptQuantity(BigDecimal deptQuantity) {
        this.deptQuantity = deptQuantity;
    }

    public BigDecimal getStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(BigDecimal stockAvailable) {
        this.stockAvailable = stockAvailable;
    }

    public BigDecimal getPricing() {
        return pricing;
    }

    public void setPricing(BigDecimal pricing) {
        this.pricing = pricing;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Integer getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(Integer lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
