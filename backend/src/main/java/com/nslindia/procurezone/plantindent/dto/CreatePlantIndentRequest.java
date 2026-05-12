package com.nslindia.procurezone.plantindent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO for creating a new Plant Indent.
 */
public record CreatePlantIndentRequest(
        @NotBlank(message = "Employee number is required") String employeeNumber,

        @NotNull(message = "Plant ID is required") Integer plantId,

        String remarks,

        Integer cropTypeId,

        Integer cropId,

        String packProcess,

        String outputMaterial,

        String outputDescription,

        Integer batchNumber,

        String masterUom,

        String lineCode,

        String lineDescription,

        String expectedQuantity,

        @NotNull(message = "At least one line item is required") @Valid List<PlantIndentDetailRequest> details) {
}
