package com.nslindia.procurezone.indent.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new indent.
 */
public record CreateIndentRequest(
        @NotNull(message = "Company ID is required") Integer companyId,

        @NotNull(message = "Department ID is required") Integer departmentId,

        Integer sectionId,

        @NotNull(message = "Plant ID is required") Integer plantId,

        @NotNull(message = "Employee ID is required") Integer employeeId,

        String comments,

        LocalDate deliveryDate,

        @NotEmpty(message = "At least one indent detail is required") @Valid List<IndentDetailRequest> details) {
}
