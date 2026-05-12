package com.nslindia.procurezone.plantindent.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity for Crop Group (pz_crop_group)
 * Categorizes crops: BG1, BG2, HT, CONV, HYB, OPV
 */
@Entity
@Table(name = "pz_crop_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crop_id")
    private Integer id;

    @Column(name = "crop_code", length = 100, unique = true)
    private String cropCode;

    @Column(name = "crop_name", length = 200)
    private String cropName;

    @Column(name = "crop_status")
    private Integer status;

    @Column(name = "crop_lmd")
    private LocalDate lastModifiedDate;

    @Column(name = "crop_lmu")
    private Integer lastModifiedBy;
}
