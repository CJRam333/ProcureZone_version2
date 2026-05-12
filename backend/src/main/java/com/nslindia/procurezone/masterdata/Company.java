package com.nslindia.procurezone.masterdata;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing a company in the system.
 * Maps to tbl_company_master table.
 */
@Entity
@Table(name = "tbl_company_master")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comp_id")
    private Integer id;

    @Column(name = "comp_code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "comp_name", nullable = false, length = 100)
    private String name;

    @Column(name = "comp_status", nullable = false)
    private Integer status;

    @Column(name = "comp_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "comp_lmu")
    private Integer lastModifiedBy;

    public Company() {
    }

    // Getters and setters

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
