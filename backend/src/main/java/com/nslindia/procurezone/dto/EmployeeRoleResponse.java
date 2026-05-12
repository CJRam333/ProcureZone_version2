package com.nslindia.procurezone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRoleResponse {

    private Integer id;
    private Integer employeeNumber;
    private Integer roleId;
    private Integer status;
    /*
     * TODO: Restore when DB schema is updated
     * private Integer assignedBy;
     * private LocalDateTime assignedDate;
     * private String remarks;
     * private Integer removedBy;
     * private LocalDateTime removedDate;
     */
    private LocalDate lastModifiedDate;
    private Integer lastModifiedBy;

    // Computed fields
    private String statusText;
    private boolean active;

    public EmployeeRoleResponse(com.nslindia.procurezone.entity.EmployeeRole entity) {
        this.id = entity.getId();
        this.employeeNumber = entity.getEmployeeNumber();
        this.roleId = entity.getRoleId();
        this.status = entity.getStatus();
        /*
         * this.assignedBy = entity.getAssignedBy();
         * this.assignedDate = entity.getAssignedDate();
         * this.remarks = entity.getRemarks();
         * this.removedBy = entity.getRemovedBy();
         * this.removedDate = entity.getRemovedDate();
         */
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedBy = entity.getLastModifiedBy();
        this.statusText = entity.getStatusText();
        this.active = entity.isActive();
    }
}
