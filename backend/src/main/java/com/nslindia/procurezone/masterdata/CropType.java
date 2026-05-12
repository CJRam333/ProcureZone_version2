package com.nslindia.procurezone.masterdata;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a Crop Type.
 * Maps to pz_crop_type table.
 * 
 * @author NSL India
 * @version 1.0
 */
@Entity
@Table(name = "pz_crop_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "diivision_name", length = 255)
    private String divisionName;

    @Column(name = "division_code")
    private Integer divisionCode;

    @Column(name = "division_id")
    private Integer divisionId;

    @Column(name = "status")
    private Integer status;
}
