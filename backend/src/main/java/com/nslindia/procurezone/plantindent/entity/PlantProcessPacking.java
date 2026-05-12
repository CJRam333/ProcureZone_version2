package com.nslindia.procurezone.plantindent.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for Plant Process/Packing (pz_tbl_proccess_packing)
 * Defines packing options: PKG-450, PKG-900, PKG-1KG, etc.
 */
@Entity
@Table(name = "pz_tbl_proccess_packing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantProcessPacking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "col_name", length = 100)
    private String name;

    @Column(name = "col_desc", length = 200)
    private String description;

    @Column(name = "col_status")
    private Integer status;
}
