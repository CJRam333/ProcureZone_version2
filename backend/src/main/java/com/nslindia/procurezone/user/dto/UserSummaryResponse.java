package com.nslindia.procurezone.user.dto;

/**
 * Summary response DTO for user list.
 */
public record UserSummaryResponse(
        Long id,
        String username,
        Integer employeeId,
        String employeeName,
        String employeeEmail,
        Integer status,
        String statusName) {
}
