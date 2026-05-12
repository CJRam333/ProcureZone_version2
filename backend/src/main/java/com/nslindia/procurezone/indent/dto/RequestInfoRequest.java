package com.nslindia.procurezone.indent.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for requesting additional information on an indent.
 */
public record RequestInfoRequest(
        @Size(max = 1000, message = "Info request must be less than 1000 characters") String infoRequested) {
}
