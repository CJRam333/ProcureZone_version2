package com.nslindia.procurezone.masterdata;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing a physical location (office, plant, warehouse, etc.)
 */
@Entity
@Table(name = "tbl_location_master")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loc_id")
    private Integer id;

    @Column(name = "loc_code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "loc_name", nullable = false, length = 100)
    private String name;

    @Column(name = "loc_status", nullable = false)
    private Integer status; // 1 = Active, 0 = Inactive

    @Column(name = "loc_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "loc_lmu", nullable = false)
    private Integer lastModifiedUser;

    // Constructors
    public Location() {
    }

    public Location(Integer id, String code, String name, Integer status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.status = status;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(Integer lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    // Business Logic
    public boolean isActive() {
        return Objects.equals(status, 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Location location = (Location) o;
        return Objects.equals(id, location.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
