package com.nslindia.procurezone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_map_emp_reporting")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeReporting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer id;

    @Column(name = "report_sub", nullable = false)
    private Integer subordinateEmployeeNumber;

    @Column(name = "report_sup", nullable = false)
    private Integer supervisorEmployeeNumber;

    @Column(name = "report_start")
    private LocalDate effectiveDate;

    @Column(name = "report_end")
    private LocalDate endDate;

    @Column(name = "report_status", nullable = false)
    private Integer status;

    @Column(name = "report_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "report_lmu", nullable = false)
    private Integer lastModifiedBy;

    // Business methods
    public boolean isActive() {
        return status != null && status == 1;
    }

    public String getStatusText() {
        if (status == null)
            return "Unknown";
        return switch (status) {
            case 1 -> "Active";
            case 0 -> "Inactive";
            default -> "Unknown";
        };
    }

    public boolean isEffectiveNow() {
        if (effectiveDate == null)
            return true;
        return !effectiveDate.isAfter(LocalDate.now());
    }

    public void activate() {
        this.status = 1;
    }

    public void deactivate() {
        this.status = 0;
    }
}
