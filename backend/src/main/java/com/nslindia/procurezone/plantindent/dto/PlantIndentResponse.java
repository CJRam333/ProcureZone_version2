package com.nslindia.procurezone.plantindent.dto;

import java.util.List;

/**
 * DTO for Plant Indent response.
 */
public record PlantIndentResponse(
                Integer id,
                String indentCode,
                String employeeNumber,
                String employeeName,
                Integer plantId,
                String plantName,
                String indentNumber,
                String remarks,
                Integer cropTypeId,
                String cropTypeName,
                Integer cropId,
                String cropName,
                String packProcess,
                String outputMaterial,
                String outputDescription,
                Integer batchNumber,
                String masterUom,
                String lineCode,
                String lineDescription,
                String expectedQuantity,
                Integer status,
                String statusDescription,
                java.time.LocalDateTime createdDate,
                int detailCount,
                List<PlantIndentDetailResponse> details) {
}
