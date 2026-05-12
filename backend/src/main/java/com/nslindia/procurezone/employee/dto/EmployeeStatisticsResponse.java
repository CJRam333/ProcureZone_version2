package com.nslindia.procurezone.employee.dto;

import java.util.Map;

/**
 * Response DTO for employee statistics.
 */
public record EmployeeStatisticsResponse(
        Long totalEmployees,
        Long activeEmployees,
        Long inactiveEmployees,
        Map<String, Integer> employeesByDepartment,
        Map<String, Integer> employeesByLocation,
        Map<String, Integer> employeesByDesignation) {
}
