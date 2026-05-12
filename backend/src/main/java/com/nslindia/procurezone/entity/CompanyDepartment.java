package com.nslindia.procurezone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entity representing the mapping between companies and departments.
 * Maps to tbl_map_company_department in the database.
 */
@Entity
@Table(name = "tbl_map_company_department")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Integer id;

    @Column(name = "map_comp", nullable = false)
    private Integer companyId;

    @Column(name = "map_dept", nullable = false)
    private Integer departmentId;

    @Column(name = "map_status", nullable = false)
    private Integer status;

    @Column(name = "map_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "map_lmu", nullable = false)
    private Integer lastModifiedBy;

    public boolean isActive() {
        return this.status != null && this.status == 1;
    }

    public String getStatusText() {
        return isActive() ? "Active" : "Inactive";
    }

    public void activate() {
        this.status = 1;
    }

    public void deactivate() {
        this.status = 0;
    }
}
