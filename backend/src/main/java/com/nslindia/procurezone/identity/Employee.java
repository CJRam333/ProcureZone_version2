package com.nslindia.procurezone.identity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_emp_master")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_number")
    private Integer employeeNumber;

    @Column(name = "emp_id", length = 100, nullable = false)
    private String employeeId;

    @Column(name = "emp_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "emp_email", length = 100, nullable = false)
    private String email;

    @Column(name = "emp_password", length = 500, nullable = false)
    private String legacyPasswordHash;

    @Column(name = "emp_join_date")
    private LocalDate joinDate;

    @Column(name = "emp_designation", length = 100, nullable = false)
    private String designation;

    @Column(name = "emp_cost_center", length = 500)
    private String costCenter;

    @Column(name = "emp_path", columnDefinition = "MEDIUMTEXT")
    private String profileImagePath;

    @Column(name = "emp_status", nullable = false)
    private Integer status;

    @Column(name = "emp_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    // FK columns — SAFE MODE
    @Column(name = "emp_department")
    private Integer departmentId;

    @Column(name = "emp_location")
    private Integer locationId;

    @Column(name = "emp_plant", length = 100)
    private String plant;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("employee")
    private Set<EmployeeRole> employeeRoles = new LinkedHashSet<>();

    // Constructors
    public Employee() {}

    // Getters & setters
    public Integer getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(Integer employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLegacyPasswordHash() {
        return legacyPasswordHash;
    }

    public void setLegacyPasswordHash(String legacyPasswordHash) {
        this.legacyPasswordHash = legacyPasswordHash;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
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

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }

    // === BACKWARD COMPATIBILITY GETTERS ===
// DO NOT REMOVE until full refactor is done

    public Integer getEmpNumber() {
        return this.employeeNumber;
    }

    public String getEmpId() {
        return this.employeeId;
    }

    public String getEmpName() {
        return this.fullName;
    }

    public String getEmpEmail() {
        return this.email;
    }

    public Integer getEmpStatus() {
        return this.status;
    }

    public Integer getEmpDepartment() {
        return this.departmentId;
    }

    public Integer getEmpLocation() {
        return this.locationId;
    }

    public Set<EmployeeRole> getEmployeeRoles() {
        return Collections.unmodifiableSet(employeeRoles);
    }

    // ===== BACKWARD COMPATIBILITY (TEMP) =====

    public Integer getDepartment() {
        return this.departmentId;
    }

    public Integer getLocation() {
        return this.locationId;
    }

    // Business helpers
    public boolean isActive() {
        return Objects.equals(status, 1);
    }

    public void activate() {
        this.status = 1;
        this.lastModifiedDate = LocalDate.now();
    }

    public void deactivate() {
        this.status = 0;
        this.lastModifiedDate = LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee that = (Employee) o;
        return Objects.equals(employeeNumber, that.employeeNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeNumber);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeNumber=" + employeeNumber +
                ", employeeId='" + employeeId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                '}';
    }
}
