package com.nslindia.procurezone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing company-department mapping
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyDepartmentRequest {

    private Integer status;
}
