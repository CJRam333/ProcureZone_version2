package com.nslindia.procurezone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeptMappingRequest {
    
    @NotNull(message = "Department IDs list cannot be null")
    private List<Integer> departmentIds;
    
}
