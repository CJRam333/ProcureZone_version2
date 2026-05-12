package com.nslindia.procurezone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for company-location mapping response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLocationResponse {

    private Integer id;
    private Integer companyId;
    private Integer locationId;
    private Integer status;
    private String statusText;
    private LocalDate lastModifiedDate;
    private Integer lastModifiedBy;
    private boolean active;
}
