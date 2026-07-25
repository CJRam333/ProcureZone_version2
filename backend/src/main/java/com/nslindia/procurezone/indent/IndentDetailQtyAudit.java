package com.nslindia.procurezone.indent;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Audit row for a single RM/DeptHead line-quantity change on an indent detail.
 * Maps to tbl_indent_details_qty_audit. Written only when a stage actually changes the quantity.
 */
@Entity
@Table(name = "tbl_indent_details_qty_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndentDetailQtyAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Integer id;

    @Column(name = "detail_id", nullable = false)
    private Integer detailId;

    /** "RM" or "DEPTHEAD". */
    @Column(name = "stage", nullable = false, length = 20)
    private String stage;

    @Column(name = "old_quantity", precision = 18, scale = 2)
    private BigDecimal oldQuantity;

    @Column(name = "new_quantity", nullable = false, precision = 18, scale = 2)
    private BigDecimal newQuantity;

    @Column(name = "edited_by", nullable = false)
    private Integer editedBy;

    @Column(name = "edited_at", nullable = false)
    private LocalDateTime editedAt;

    public IndentDetailQtyAudit(Integer detailId, String stage, BigDecimal oldQuantity,
                                BigDecimal newQuantity, Integer editedBy, LocalDateTime editedAt) {
        this.detailId = detailId;
        this.stage = stage;
        this.oldQuantity = oldQuantity;
        this.newQuantity = newQuantity;
        this.editedBy = editedBy;
        this.editedAt = editedAt;
    }
}
