package com.nslindia.procurezone.indent.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

/**
 * DTO for updating an existing indent (only in Draft status).
 */
public record UpdateIndentRequest(
        Integer companyId,

        Integer departmentId,

        Integer sectionId,

        Integer plantId,

        Integer employeeId,

        String comments,

        LocalDate deliveryDate,

        @Valid List<IndentDetailRequest> details) {
}
