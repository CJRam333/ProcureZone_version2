package com.nslindia.procurezone.indent.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for approval workflow history.
 */
public record ApprovalWorkflowResponse(
        Integer id,
        Integer indentId,
        Integer approverEmpNumber,
        String approverName,
        String action,
        LocalDateTime actionDate,
        String remarks,
        Integer level,
        String infoRequested) {
}
