package com.nslindia.procurezone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEmployeeResponse {

    private Integer id;
    private Integer companyId;
    private Integer employeeNumber;
    private Integer status;
    private String statusText;
    private LocalDate lastModifiedDate;
    private Integer lastModifiedBy;
    private boolean active;
}
