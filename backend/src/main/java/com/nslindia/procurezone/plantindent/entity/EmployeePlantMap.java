package com.nslindia.procurezone.plantindent.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for Employee-Plant Mapping (pz_tbl_emp_plant_map)
 * Maps employees to plants with roles for plant-specific authorization
 */
@Entity
@Table(name = "pz_tbl_emp_plant_map")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePlantMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "emp_id", nullable = false)
    private Integer employeeId;

    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    @Column(name = "plant_id", nullable = false)
    private Integer plantId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "role_status", length = 100)
    private String roleStatus;
}
