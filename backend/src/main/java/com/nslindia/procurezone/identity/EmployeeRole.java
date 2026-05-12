package com.nslindia.procurezone.identity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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
 * Entity representing the mapping between employees and roles
 * (Relationship-based version).
 * Maps to tbl_map_emp_roles in the database.
 * 
 * IMPORTANT: This version uses JPA entity relationships (ManyToOne with
 * Employee and Role).
 * Used for: EmployeeRoleRepository, auth services, where you need to traverse
 * to Employee/Role.
 * 
 * For simple ID-based queries (CRUD operations), use:
 * {@link com.nslindia.procurezone.entity.EmployeeRole} instead (JPA name:
 * "EmployeeRoleMapping").
 */
@Entity
@Table(name = "tbl_map_emp_roles")
public class EmployeeRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_roles_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_number", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("employeeRoles")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "emp_roles_status", nullable = false)
    private Integer status;

    /*
     * TODO: Restore these fields when the database schema is updated to include
     * them
     * 
     * @Column(name = "emp_roles_assigned_date")
     * private LocalDate assignedDate;
     * 
     * @Column(name = "emp_roles_assigned_by", length = 100)
     * private String assignedBy;
     * 
     * @Column(name = "emp_roles_removed_date")
     * private LocalDate removedDate;
     * 
     * @Column(name = "emp_roles_removed_by", length = 100)
     * private String removedBy;
     * 
     * @Column(name = "emp_roles_remarks", length = 500)
     * private String remarks;
     */

    @Column(name = "emp_roles_lmd", nullable = false)
    private LocalDateTime lastModifiedDate;

    @Column(name = "emp_roles_lmu", nullable = false, length = 100)
    private String lastModifiedUser;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /*
     * TODO: Restore getters/setters when DB schema is updated
     * public LocalDate getAssignedDate() { return assignedDate; }
     * public void setAssignedDate(LocalDate assignedDate) { this.assignedDate =
     * assignedDate; }
     * public String getAssignedBy() { return assignedBy; }
     * public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy;
     * }
     * public LocalDate getRemovedDate() { return removedDate; }
     * public void setRemovedDate(LocalDate removedDate) { this.removedDate =
     * removedDate; }
     * public String getRemovedBy() { return removedBy; }
     * public void setRemovedBy(String removedBy) { this.removedBy = removedBy; }
     * public String getRemarks() { return remarks; }
     * public void setRemarks(String remarks) { this.remarks = remarks; }
     */

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(String lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public boolean isActive() {
        return Objects.equals(status, 1);
    }
}
