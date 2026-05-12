package com.nslindia.procurezone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeReportingRequest {

    private Integer supervisorEmployeeNumber;
    private LocalDate effectiveDate;
    private Integer status;
}
