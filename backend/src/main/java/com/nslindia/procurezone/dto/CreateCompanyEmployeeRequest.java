package com.nslindia.procurezone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyEmployeeRequest {

    @NotNull(message = "Company ID is required")
    private Integer companyId;

    @NotNull(message = "Employee number is required")
    private Integer employeeNumber;

    private Integer status = 1;
}
