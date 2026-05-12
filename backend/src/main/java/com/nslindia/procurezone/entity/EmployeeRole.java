package com.nslindia.procurezone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entity representing the mapping between employees and roles (ID-based
 * version).
 * Maps to tbl_map_emp_roles in the database.
 * 
 * IMPORTANT: This is a SIMPLIFIED version that uses Integer IDs instead of
 * entity relationships.
 * Used for: EmployeeRoleMappingRepository, EmployeeRoleService, CRUD operations
 * on role mappings.
 * 
 * For entity relationship queries (joining with Employee/Role), use:
 * {@link com.nslindia.procurezone.identity.EmployeeRole} instead.
 * 
 * JPA Entity Name: "EmployeeRoleMapping" (to avoid conflict with
 * identity.EmployeeRole)
 */
@Entity(name = "EmployeeRoleMapping")
@Table(name = "tbl_map_emp_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_roles_id")
    private Integer id;

    @Column(name = "emp_number", nullable = false)
    private Integer employeeNumber;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "emp_roles_status", nullable = false)
    private Integer status;

    /*
     * TODO: Restore these fields when DB schema is updated
     * 
     * @Column(name = "emp_roles_assigned_by")
     * private Integer assignedBy;
     * 
     * @Column(name = "emp_roles_assigned_date")
     * private LocalDateTime assignedDate;
     * 
     * @Column(name = "emp_roles_remarks")
     * private String remarks;
     * 
     * @Column(name = "emp_roles_removed_by")
     * private Integer removedBy;
     * 
     * @Column(name = "emp_roles_removed_date")
     * private LocalDateTime removedDate;
     */

    @Column(name = "emp_roles_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "emp_roles_lmu", nullable = false)
    private Integer lastModifiedBy;

    public boolean isActive() {
        return this.status != null && this.status == 1;
    }

    public String getStatusText() {
        return isActive() ? "Active" : "Inactive";
    }

    public void activate(Integer activatedBy) {
        this.status = 1;
        /*
         * this.assignedBy = activatedBy;
         * this.assignedDate = LocalDateTime.now();
         * this.removedBy = null;
         * this.removedDate = null;
         */
    }

    public void deactivate(Integer deactivatedBy, String reason) {
        this.status = 0;
        /*
         * this.removedBy = deactivatedBy;
         * this.removedDate = LocalDateTime.now();
         * if (reason != null) {
         * this.remarks = (this.remarks != null ? this.remarks + "; " : "") +
         * "Removed: " + reason;
         * }
         */
    }
}
