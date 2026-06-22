package com.nslindia.procurezone.indent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Department;
import com.nslindia.procurezone.masterdata.Plant;
import com.nslindia.procurezone.masterdata.Section;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entity representing a material indent/requisition.
 * Maps to tbl_indent_master table.
 */
@Entity
@Table(name = "tbl_indent_master")
public class Indent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indent_id")
    private Integer id;

    @Column(name = "indent_no", length = 100)
    private String indentNumber;

    @Column(name = "indent_year", length = 45)
    private String indentYear;

    @Column(name = "indent_date")
    private LocalDateTime indentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_company")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_dept")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_sec")
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_plant")
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_emp")
    private Employee employee;

    @Column(name = "indent_comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "indent_delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "indent_po_number", length = 100)
    private String poNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_createdby")
    private Employee createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_approvedby")
    private Employee approvedBy;

    @Column(name = "indent_approvedby_date")
    private LocalDateTime approvedByDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_final_approvedby")
    private Employee finalApprovedBy;

    @Column(name = "indent_final_date")
    private LocalDateTime finalApprovedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_procurementby")
    private Employee procurementBy;

    @Column(name = "indent_remarks", columnDefinition = "MEDIUMTEXT")
    private String remarks;

    @Column(name = "indent_final_remarks", columnDefinition = "MEDIUMTEXT")
    private String finalRemarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_status")
    private IndentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_approved_status")
    private IndentStatus approvedStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_final_status")
    private IndentStatus finalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_procurement_status")
    private IndentStatus procurementStatus;

    @Column(name = "indent_procurement_remarks", columnDefinition = "MEDIUMTEXT")
    private String procurementRemarks;

    @Column(name = "indent_lmd")
    private LocalDateTime lastModifiedDate;

    @Column(name = "indent_lmu")
    private Integer lastModifiedBy;

    @OneToMany(mappedBy = "indent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<IndentDetail> details = new ArrayList<>();

    protected Indent() {
    }

    // Helper methods

    public void addDetail(IndentDetail detail) {
        details.add(detail);
        detail.setIndent(this);
    }

    public void removeDetail(IndentDetail detail) {
        details.remove(detail);
        detail.setIndent(null);
    }

    // Getters and setters

    public Integer getId() {
        return id;
    }

    public String getIndentNumber() {
        return indentNumber;
    }

    public void setIndentNumber(String indentNumber) {
        this.indentNumber = indentNumber;
    }

    public String getIndentYear() {
        return indentYear;
    }

    public void setIndentYear(String indentYear) {
        this.indentYear = indentYear;
    }

    public LocalDateTime getIndentDate() {
        return indentDate;
    }

    public void setIndentDate(LocalDateTime indentDate) {
        this.indentDate = indentDate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public Employee getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Employee createdBy) {
        this.createdBy = createdBy;
    }

    public Employee getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Employee approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedByDate() {
        return approvedByDate;
    }

    public void setApprovedByDate(LocalDateTime approvedByDate) {
        this.approvedByDate = approvedByDate;
    }

    public Employee getFinalApprovedBy() {
        return finalApprovedBy;
    }

    public void setFinalApprovedBy(Employee finalApprovedBy) {
        this.finalApprovedBy = finalApprovedBy;
    }

    public LocalDateTime getFinalApprovedDate() {
        return finalApprovedDate;
    }

    public void setFinalApprovedDate(LocalDateTime finalApprovedDate) {
        this.finalApprovedDate = finalApprovedDate;
    }

    public Employee getProcurementBy() {
        return procurementBy;
    }

    public void setProcurementBy(Employee procurementBy) {
        this.procurementBy = procurementBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getFinalRemarks() {
        return finalRemarks;
    }

    public void setFinalRemarks(String finalRemarks) {
        this.finalRemarks = finalRemarks;
    }

    public IndentStatus getStatus() {
        return status;
    }

    public void setStatus(IndentStatus status) {
        this.status = status;
    }

    public IndentStatus getApprovedStatus() {
        return approvedStatus;
    }

    public void setApprovedStatus(IndentStatus approvedStatus) {
        this.approvedStatus = approvedStatus;
    }

    public IndentStatus getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(IndentStatus finalStatus) {
        this.finalStatus = finalStatus;
    }

    public IndentStatus getProcurementStatus() {
        return procurementStatus;
    }

    public void setProcurementStatus(IndentStatus procurementStatus) {
        this.procurementStatus = procurementStatus;
    }

    public String getProcurementRemarks() {
        return procurementRemarks;
    }

    public void setProcurementRemarks(String procurementRemarks) {
        this.procurementRemarks = procurementRemarks;
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

    public List<IndentDetail> getDetails() {
        return details;
    }

    public void setDetails(List<IndentDetail> details) {
        this.details = details;
    }
}
