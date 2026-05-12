package com.nslindia.procurezone.dto.issuenote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueNoteDetailsDTO {
    private Integer id;
    private Integer materialId;
    private String materialCode;
    private String materialDescription;
    private Integer unitOfMeasureId;
    private String unitOfMeasureName;
    private BigDecimal quantity;
    private BigDecimal rate;
    private BigDecimal amount;
    private String purpose;
    private Integer status;
}
