package com.nslindia.procurezone.masterdata;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing a material/item in the system.
 * Maps to tbl_material_master table.
 */
@Entity
@Table(name = "tbl_material_master")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Integer id;

    @Column(name = "material_code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "material_name", nullable = false, length = 100)
    private String name;

    @Column(name = "material_desc", columnDefinition = "TEXT")
    private String description;

    @Column(name = "material_status", nullable = false)
    private Integer status;

    @Column(name = "material_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "material_lmu", nullable = false)
    private Integer lastModifiedBy;

    public Material() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
