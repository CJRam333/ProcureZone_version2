package com.nslindia.procurezone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeReportingRequest {

    @NotNull(message = "Subordinate employee number is required")
    private Integer subordinateEmployeeNumber;

    @NotNull(message = "Supervisor employee number is required")
    private Integer supervisorEmployeeNumber;

    private LocalDate effectiveDate;

    private Integer status = 1;
}
