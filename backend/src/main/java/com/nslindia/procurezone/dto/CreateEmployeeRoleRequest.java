package com.nslindia.procurezone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRoleRequest {

    @NotNull(message = "Employee number is required")
    private Integer employeeNumber;

    @NotNull(message = "Role ID is required")
    private Integer roleId;

    private String remarks;

    private Integer status = 1;
}
