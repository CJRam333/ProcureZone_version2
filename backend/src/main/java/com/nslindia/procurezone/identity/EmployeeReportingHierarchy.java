package com.nslindia.procurezone.identity;

import java.time.LocalDate;

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
 * Entity representing the reporting hierarchy between employees.
 * Maps to tbl_map_emp_reporting table.
 * 
 * This table defines who reports to whom:
 * - report_sub = subordinate employee (who reports)
 * - report_sup = supervisor employee (to whom they report)
 * 
 * Used for:
 * - L1 (RM/Section Head) approval routing
 * - Determining approval chain for indents
 */
@Entity
@Table(name = "tbl_map_emp_reporting")
public class EmployeeReportingHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_sub", nullable = false)
    private Employee subordinate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_sup", nullable = false)
    private Employee supervisor;

    @Column(name = "report_start")
    private LocalDate effectiveDate;

    @Column(name = "report_end")
    private LocalDate endDate;

    @Column(name = "report_status", nullable = false)
    private Integer status = 1;

    @Column(name = "report_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "report_lmu", nullable = false)
    private Integer lastModifiedBy;

    protected EmployeeReportingHierarchy() {
    }

    public EmployeeReportingHierarchy(Employee subordinate, Employee supervisor) {
        this.subordinate = subordinate;
        this.supervisor = supervisor;
        this.status = 1;
        this.lastModifiedDate = LocalDate.now();
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public Employee getSubordinate() {
        return subordinate;
    }

    public void setSubordinate(Employee subordinate) {
        this.subordinate = subordinate;
    }

    public Employee getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Employee supervisor) {
        this.supervisor = supervisor;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDate getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDate lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Integer getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(Integer lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * Check if this hierarchy relationship is active
     */
    public boolean isActive() {
        return status != null && status == 1;
    }
}
