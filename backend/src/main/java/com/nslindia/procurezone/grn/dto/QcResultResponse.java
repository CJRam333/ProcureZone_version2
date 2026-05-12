package com.nslindia.procurezone.grn.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for GRN QC Result response.
 * 
 * @author NSL India
 * @version 1.0
 */
public record QcResultResponse(
        Integer id,
        Integer grnId,
        String grnNumber,
        Integer inspectorId,
        String inspectorName,
        LocalDateTime inspectionDate,
        Integer qcStatus,
        String qcStatusName,

        // Physical Parameters
        BigDecimal moisture,
        BigDecimal pureSeed,
        BigDecimal inertMatter,
        BigDecimal ocsCount,
        BigDecimal weedSeedCount,
        BigDecimal grain,
        BigDecimal blackSeeds,
        BigDecimal pinholeSeed,
        BigDecimal bulkDensity,
        BigDecimal thsw,

        // Germination Parameters
        BigDecimal coldVigourGermNormal,
        BigDecimal firstCountNormal,
        BigDecimal germNormal,
        BigDecimal fetNormal,
        BigDecimal soilCountDays,
        BigDecimal aavGermNormal,

        // GOT Parameters
        BigDecimal got,
        BigDecimal gotGp,
        BigDecimal gotFemale,
        BigDecimal gotOthers,

        // Trait Markers
        String bg1,
        String bg2,
        String ht,
        String fqr,

        // Disease/Pest Tests
        String elisa,
        String stl,
        String odv,
        String odvRes,

        // Additional QC Fields
        String q1,
        String q2,
        String q3,
        String q4,
        String q5,
        String q6,
        String q7,
        String q8,
        String q9,

        // Overall Assessment
        String overallRemarks,
        String passFailRemarks,
        String correctiveAction,
        Boolean reInspectionRequired,
        LocalDateTime reInspectionDate,

        // Audit
        LocalDateTime createdDate,
        LocalDateTime updatedDate) {
    /**
     * Get status name from status code
     */
    public static String getStatusName(Integer status) {
        if (status == null)
            return "Unknown";
        return switch (status) {
            case 1 -> "Pending";
            case 2 -> "Passed";
            case 3 -> "Failed";
            case 4 -> "Conditional";
            default -> "Unknown";
        };
    }
}
