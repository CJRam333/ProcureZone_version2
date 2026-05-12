package com.nslindia.procurezone.plantindent.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for Plant Storage Location (pz_plant_storage_locations)
 * Defines storage locations/warehouses at each plant
 */
@Entity
@Table(name = "pz_plant_storage_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantStorageLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "plant_code")
    private Integer plantCode;

    @Column(name = "storage_code", length = 100, nullable = false)
    private String storageCode;

    @Column(name = "storage_desc", length = 200)
    private String storageDescription;

    @Column(name = "storage_type", length = 50)
    private String storageType;

    @Column(name = "status")
    private Integer status;
}
