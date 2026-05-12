package com.nslindia.procurezone.plantindent.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for SAP Material Master (pz_sap_material_masters)
 * Materials imported from SAP with plant association
 */
@Entity
@Table(name = "pz_sap_material_masters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SapMaterialMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "material_code", length = 100, nullable = false)
    private String materialCode;

    @Column(name = "material_desc", length = 200)
    private String materialDescription;

    @Column(name = "uom", length = 20)
    private String uom;

    @Column(name = "material_type", length = 50)
    private String materialType;

    @Column(name = "plant")
    private Integer plant;

    @Column(name = "materialstatus")
    private Integer status;
}
