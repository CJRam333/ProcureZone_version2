package com.nslindia.procurezone.employee.dto;

import java.time.LocalDate;
import java.util.Set;

/**
 * Response DTO for employee details.
 */
public record EmployeeResponse(
        Integer id,
        String empId,
        String empName,
        String empEmail,
        LocalDate empJoinDate,
        String empDesignation,
        String empCostCenter,
        String plantName,
        Integer empStatus,
        String statusName,
        LocalDate lastModifiedDate,
        Integer departmentId,
        String departmentName,
        Integer locationId,
        String locationName,
        Integer companyId,
        String companyName,
        Set<EmployeeRoleInfo> roles,
        Integer reportingManagerId,
        String reportingManagerName) {
    public record EmployeeRoleInfo(
            Integer roleId,
            String roleCode,
            String roleName,
            Integer status) {
    }
}
