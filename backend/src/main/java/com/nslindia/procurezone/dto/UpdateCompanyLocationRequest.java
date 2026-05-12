package com.nslindia.procurezone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing company-location mapping
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyLocationRequest {

    private Integer status;
}
