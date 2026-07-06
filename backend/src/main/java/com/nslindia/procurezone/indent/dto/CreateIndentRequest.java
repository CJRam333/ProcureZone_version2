package com.nslindia.procurezone.indent.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/**
 * DTO for creating a new indent.
 */
public record CreateIndentRequest(
        // company / department / plant / employee are captured server-side from the creating
        // employee's record (see IndentService.createIndent), so they are optional in the payload.
        // A value is still honoured as a fallback if the employee record can't supply it.
        Integer companyId,

        Integer departmentId,

        Integer sectionId,

        Integer plantId,

        Integer employeeId,

        String comments,

        LocalDate deliveryDate,

        @NotEmpty(message = "At least one indent detail is required") @Valid List<IndentDetailRequest> details) {
}
