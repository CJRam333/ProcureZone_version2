package com.nslindia.procurezone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeReportingResponse {

    private Integer id;
    private Integer subordinateEmployeeNumber;
    private Integer supervisorEmployeeNumber;
    private LocalDate effectiveDate;
    private Integer status;
    private LocalDate lastModifiedDate;
    private Integer lastModifiedBy;

    // Computed fields
    private String statusText;
    private boolean active;
    private boolean effectiveNow;

    public EmployeeReportingResponse(com.nslindia.procurezone.entity.EmployeeReporting entity) {
        this.id = entity.getId();
        this.subordinateEmployeeNumber = entity.getSubordinateEmployeeNumber();
        this.supervisorEmployeeNumber = entity.getSupervisorEmployeeNumber();
        this.effectiveDate = entity.getEffectiveDate();
        this.status = entity.getStatus();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedBy = entity.getLastModifiedBy();
        this.statusText = entity.getStatusText();
        this.active = entity.isActive();
        this.effectiveNow = entity.isEffectiveNow();
    }
}
