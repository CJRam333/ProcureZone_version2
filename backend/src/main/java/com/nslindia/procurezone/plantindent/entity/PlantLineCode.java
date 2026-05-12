package com.nslindia.procurezone.plantindent.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for Plant Line Code (pz_tbl_line_code)
 * Defines production lines at each plant
 */
@Entity
@Table(name = "pz_tbl_line_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantLineCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "line_code", length = 100)
    private String lineCode;

    @Column(name = "line_desc", length = 200)
    private String lineDescription;

    @Column(name = "plant_id")
    private Integer plantId;

    @Column(name = "line_type", length = 50)
    private String lineType;

    @Column(name = "line_status")
    private Integer status;
}
