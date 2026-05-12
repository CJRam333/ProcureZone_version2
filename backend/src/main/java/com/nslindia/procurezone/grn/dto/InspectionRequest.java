package com.nslindia.procurezone.grn.dto;

import jakarta.validation.constraints.NotBlank;

public record InspectionRequest(
        @NotBlank(message = "Inspection remarks are required") String remarks,

        Boolean qualityApproved // true = pass, false = fail
) {
}
