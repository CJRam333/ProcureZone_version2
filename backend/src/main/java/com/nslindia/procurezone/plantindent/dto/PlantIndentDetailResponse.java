package com.nslindia.procurezone.plantindent.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Plant Indent detail response with QC parameters.
 */
public record PlantIndentDetailResponse(
        Integer id,
        Integer materialId,
        String materialCode,
        String materialName,
        Integer unitOfMeasureId,
        String uomCode,
        String uomName,
        BigDecimal quantity,
        BigDecimal rmQuantity,
        BigDecimal deptQuantity,
        BigDecimal stockAvailable,
        BigDecimal pricing,
        String purpose,
        String vendor,
        Integer status,

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
        String q9,

        LocalDateTime lastModifiedDate) {
}
