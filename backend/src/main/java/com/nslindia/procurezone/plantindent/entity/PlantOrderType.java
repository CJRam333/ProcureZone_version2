package com.nslindia.procurezone.plantindent.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for Plant Order Type (pz_indent_ordertype)
 * Defines types of orders: PROD, QC, RND, TRIAL, SAMPLE, REWORK
 */
@Entity
@Table(name = "pz_indent_ordertype")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantOrderType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "order_type", length = 100)
    private String orderType;

    @Column(name = "order_desc", length = 200)
    private String orderDescription;

    @Column(name = "plant_id")
    private Integer plantId;

    @Column(name = "order_status")
    private Integer status;
}
