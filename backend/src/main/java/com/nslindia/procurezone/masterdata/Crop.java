package com.nslindia.procurezone.masterdata;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity representing a Crop (e.g., Corn, Wheat, Cotton).
 * Maps to tbl_crop_master table.
 * 
 * @author NSL India
 * @version 1.0
 */
@Entity
@Table(name = "tbl_crop_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crop_id")
    private Integer id;

    @Column(name = "crop_code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "crop_name", nullable = false, length = 100)
    private String name;

    @Column(name = "crop_status", nullable = false)
    private Integer status;

    @Column(name = "crop_lmd", nullable = false)
    private LocalDate lastModifiedDate;

    @Column(name = "crop_lmu", nullable = false)
    private Integer lastModifiedBy;
}
