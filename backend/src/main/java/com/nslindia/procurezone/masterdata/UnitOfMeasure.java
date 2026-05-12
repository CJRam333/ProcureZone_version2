package com.nslindia.procurezone.masterdata;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing a unit of measure (UOM/UMO).
 * Maps to tbl_umo_master table.
 */
@Entity
@Table(name = "tbl_umo_master")
public class UnitOfMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "umo_id")
    private Integer id;

    @Column(name = "umo_code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "umo_name", nullable = false, length = 100)
    private String name;

    @Column(name = "umo_status", nullable = false)
    private Integer status;

    @Column(name = "umo_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "umo_lmu", nullable = false)
    private Integer lastModifiedBy;

    public UnitOfMeasure() {
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
