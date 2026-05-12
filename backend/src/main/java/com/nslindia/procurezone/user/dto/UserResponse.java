package com.nslindia.procurezone.user.dto;

import java.time.LocalDate;

/**
 * Response DTO for user account details.
 */
public record UserResponse(
        Long id,
        String username,
        Integer employeeId,
        String employeeCode,
        String employeeName,
        String employeeEmail,
        Integer status,
        String statusName,
        String lastLoginIp,
        LocalDate lastModifiedDate) {
}
