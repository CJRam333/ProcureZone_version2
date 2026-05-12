package com.nslindia.procurezone.integration.sap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a single row from SAP Material CSV file
 * CSV Format: CompanyCode,PlantCode,MaterialCode,MaterialDescription,Quantity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialCsvRow {

    private String companyCode; // Column 0
    private String plantCode; // Column 1
    private String materialCode; // Column 2
    private String materialDescription; // Column 3
    private BigDecimal quantity; // Column 4

    private int rowNumber; // For error reporting
}
