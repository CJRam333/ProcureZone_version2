package com.nslindia.procurezone.plantindent.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO for Plant Indent detail (line item) with QC parameters.
 */
public record PlantIndentDetailRequest(
        @NotNull(message = "Material ID is required") Integer materialId,

        @NotNull(message = "Unit of measure ID is required") Integer unitOfMeasureId,

        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") BigDecimal quantity,

        BigDecimal stockAvailable,

        BigDecimal pricing,

        String purpose,

        String vendor,

        // Plant-specific fields
        String indentMaterial,

        String indentMaterialDescription,

        String lotNumber,

        String storageLocation,

        // QC Test Parameters
        String stl,
        String odv,
        String got,
        String elisa,
        String sdcls,
        String stats,
        String skipd,
        String inspdt,

        // Physical Parameters
        String moisture,
        String pureSeed,
        String inertMatter,
        String ocsCount,
        String weedSeedCount,
        String grain,
        String blackSeeds,
        String pinholeSeed,
        String odvRes,
        String bulkDensity,
        String thsw,

        // Germination Parameters
        String coldVigourGermNormal,
        String firstCountNormal,
        String germNormal,
        String fetNormal,
        String soilCountDays,
        String aavGermNormal,

        // GOT Parameters
        String gotGp,
        String gotFemale,
        String gotOthers,

        // Trait Markers
        String bg1,
        String bg2,
        String ht,
        String fqr,

        // Additional QC Parameters
        String q1,
        String q2,
        String q3,
        String q4,
        String q5,
        String q6,
        String q7,
        String q8,
        String q9) {
}
