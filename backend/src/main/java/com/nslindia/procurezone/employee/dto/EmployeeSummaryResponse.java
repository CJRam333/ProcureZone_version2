package com.nslindia.procurezone.employee.dto;

/**
 * Summary response DTO for employee list views.
 */
public record EmployeeSummaryResponse(
        Integer id, // maps to employeeNumber
        String empId, // maps to employeeId
        String empName, // maps to fullName
        String empEmail, // maps to email
        String empDesignation, // maps to designation
        Integer empStatus, // maps to status
        String statusName,
        String departmentName,
        String locationName,
        Integer activeRolesCount) {
}
