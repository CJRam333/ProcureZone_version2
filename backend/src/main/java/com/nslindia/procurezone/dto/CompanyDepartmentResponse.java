package com.nslindia.procurezone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for company-department mapping response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDepartmentResponse {

    private Integer id;
    private Integer companyId;
    private Integer departmentId;
    private Integer status;
    private String statusText;
    private LocalDate lastModifiedDate;
    private Integer lastModifiedBy;
    private boolean active;
}
