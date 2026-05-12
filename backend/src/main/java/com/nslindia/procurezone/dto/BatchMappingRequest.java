package com.nslindia.procurezone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for batch mapping operations (e.g., Company-Location batch sync)
 */
@Data
public class BatchMappingRequest {
    
    @NotNull(message = "Location IDs list cannot be null")
    private List<Integer> locationIds;
    
}
