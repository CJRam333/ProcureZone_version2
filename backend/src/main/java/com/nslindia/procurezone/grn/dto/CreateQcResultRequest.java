package com.nslindia.procurezone.grn.dto;

import java.math.BigDecimal;

/**
 * DTO for creating GRN QC Result.
 * 
 * @author NSL India
 * @version 1.0
 */
public record CreateQcResultRequest(
        Integer grnId,
        Integer qcStatus, // 1=Pending, 2=Passed, 3=Failed, 4=Conditional

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
        Boolean reInspectionRequired) {
}
