package com.nslindia.procurezone.issuenote;

import com.nslindia.procurezone.common.persistence.VarcharDateTimeConverter;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Issue Note Details Entity
 * Maps to tbl_issue_note_details table
 * Represents individual line items in an issue note
 * 
 * @author NSL India
 * @version 1.0
 */
@Entity
@Table(name = "tbl_issue_note_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueNoteDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_note_details_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_note_id", nullable = false)
    private IssueNote issueNote;

    @Column(name = "issue_note_material", nullable = false)
    private Integer materialId;

    @Column(name = "issue_note_details_umo", nullable = false)
    private Integer unitOfMeasureId;

    @Column(name = "issue_note_details_quantity", nullable = false, precision = 20, scale = 2)
    private BigDecimal quantity;

    /**
     * RM-adjusted quantity (Pass 3). Nullable — set only when the RM changes the line at approval.
     * The requester's original ({@link #quantity}) is NEVER overwritten.
     */
    @Column(name = "issue_note_details_rm_qty", precision = 18, scale = 2)
    private BigDecimal rmQuantity;

    @Column(name = "issue_note_details_rate", precision = 20, scale = 2)
    private BigDecimal rate;

    @Column(name = "issue_note_details_amount", precision = 20, scale = 2)
    private BigDecimal amount;

    @Column(name = "issue_note_details_quantity_stores", precision = 20, scale = 2)
    private BigDecimal quantityStores;

    @Column(name = "issue_note_details_purpose", columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "issue_note_details_status", nullable = false)
    private Integer status;

    @Column(name = "issue_note_details_lmd", nullable = false, length = 20)
    @Convert(converter = VarcharDateTimeConverter.class)
    private LocalDateTime lastModifiedDate;

    @Column(name = "issue_note_details_lmu", nullable = false)
    private Integer lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        if (lastModifiedDate == null) {
            lastModifiedDate = LocalDateTime.now();
        }
        if (status == null) {
            status = 1; // Active
        }
        // Calculate amount if rate and quantity present
        if (rate != null && quantity != null) {
            amount = rate.multiply(quantity);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
        // Recalculate amount
        if (rate != null && quantity != null) {
            amount = rate.multiply(quantity);
        }
    }

    /**
     * Calculate total amount
     */
    public BigDecimal calculateAmount() {
        if (rate != null && quantity != null) {
            return rate.multiply(quantity);
        }
        return BigDecimal.ZERO;
    }
}
