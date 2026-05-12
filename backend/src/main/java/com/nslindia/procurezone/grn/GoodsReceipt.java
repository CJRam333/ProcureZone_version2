package com.nslindia.procurezone.grn;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Goods Receipt Note (GRN) Entity
 * Maps to tbl_goods_receipt table
 * Represents goods received against a Purchase Order
 * 
 * Status Workflow with RM Approval:
 * 1 = Created (pending inspection)
 * 2 = Inspected (pending RM approval)
 * 3 = RM Approved (pending Dept Head approval)
 * 4 = Approved (pending final approval)
 * 5 = Final Approved (pending storage)
 * 6 = Stored (completed)
 * 7 = Rejected
 * 
 * @author NSL India
 * @version 2.0
 */
@Entity
@Table(name = "tbl_goods_receipt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goods_receipt_id")
    private Integer id;

    @Column(name = "goods_receipt_no", nullable = false, unique = true, length = 100)
    private String grnNumber;

    @Column(name = "goods_receipt_date", nullable = false)
    private LocalDateTime receiptDate;

    @Column(name = "indent_id", nullable = false)
    private Integer indentId;

    @Column(name = "indent_details_id", nullable = false)
    private Integer indentDetailsId;

    @Column(name = "goods_receipt_quantity", nullable = false, precision = 20, scale = 2)
    private BigDecimal receivedQuantity;

    @Column(name = "goods_receipt_issued_quantity", precision = 20, scale = 2)
    private BigDecimal issuedQuantity;

    @Column(name = "goods_receipt_balance_inventory", precision = 20, scale = 2)
    private BigDecimal balanceInventory;

    @Column(name = "goods_receipt_opening_quantity", precision = 20, scale = 2)
    private BigDecimal openingQuantity;

    @Column(name = "goods_receipt_rate", precision = 20, scale = 2)
    private BigDecimal rate;

    @Column(name = "goods_receipt_amount", precision = 20, scale = 2)
    private BigDecimal amount;

    @Column(name = "goods_receipt_vendor", length = 200)
    private String vendorName;

    @Column(name = "goods_receipt_comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "goods_receipt_createdby", nullable = false)
    private Integer createdBy;

    @Column(name = "goods_receipt_created_date")
    private LocalDateTime createdDate;

    @Column(name = "goods_receipt_created_remarks", columnDefinition = "TEXT")
    private String createdRemarks;

    // RM (Reporting Manager) Approval Fields
    @Column(name = "goods_receipt_rm_approvedby")
    private Integer rmApprovedBy;

    @Column(name = "goods_receipt_rm_approvedby_date")
    private LocalDateTime rmApprovedDate;

    @Column(name = "goods_receipt_rm_approvedby_remarks", columnDefinition = "TEXT")
    private String rmApprovedRemarks;

    @Column(name = "goods_receipt_rm_status")
    private Integer rmApprovedStatus;

    @Column(name = "goods_receipt_approvedby")
    private Integer approvedBy;

    @Column(name = "goods_receipt_approvedby_date")
    private LocalDateTime approvedDate;

    @Column(name = "goods_receipt_approvedby_remarks", columnDefinition = "TEXT")
    private String approvedRemarks;

    @Column(name = "goods_receipt_final_approvedby")
    private Integer finalApprovedBy;

    @Column(name = "goods_receipt_final_approvedby_date")
    private LocalDateTime finalApprovedDate;

    @Column(name = "goods_receipt_final_approvedby_remarks", columnDefinition = "TEXT")
    private String finalApprovedRemarks;

    @Column(name = "goods_receipt_requested_quantity", precision = 20, scale = 2)
    private BigDecimal requestedQuantity;

    @Column(name = "goods_receipt_balance_quantity_stores", precision = 20, scale = 2)
    private BigDecimal balanceQuantityStores;

    @Column(name = "goods_receipt_storesby")
    private Integer storedBy;

    @Column(name = "goods_receipt_storesby_date")
    private LocalDateTime storedDate;

    @Column(name = "goods_receipt_storesby_remarks", columnDefinition = "TEXT")
    private String storedRemarks;

    @Column(name = "goods_receipt_status", nullable = false)
    private Integer status; // 1=Created, 2=Inspected, 3=RM Approved, 4=Approved, 5=Final Approved,
                            // 6=Stored, 7=Rejected

    @Column(name = "goods_receipt_approved_status")
    private Integer approvedStatus;

    @Column(name = "goods_receipt_final_status")
    private Integer finalStatus;

    @Column(name = "goods_receipt_storesby_status")
    private Integer storedStatus;

    // Supervisor bypass flag (if supervisor creates, skip RM approval)
    @Column(name = "goods_receipt_supervisor_bypass")
    private Boolean supervisorBypass;

    @Column(name = "goods_receipt_lmd", nullable = false)
    private LocalDateTime lastModifiedDate;

    @Column(name = "goods_receipt_lmu", nullable = false)
    private Integer lastModifiedBy;

    // Helper methods for status checks
    public boolean isCreated() {
        return this.status == 1;
    }

    public boolean isInspected() {
        return this.status == 2;
    }

    public boolean isRmApproved() {
        return this.status == 3;
    }

    public boolean isApproved() {
        return this.status == 4;
    }

    public boolean isFinalApproved() {
        return this.status == 5;
    }

    public boolean isStored() {
        return this.status == 6;
    }

    public boolean isRejected() {
        return this.status == 7;
    }

    public boolean canBeInspected() {
        return isCreated();
    }

    public boolean canBeRmApproved() {
        return isInspected() && !Boolean.TRUE.equals(supervisorBypass);
    }

    public boolean canBeApproved() {
        // Can be approved if RM approved OR if supervisor bypass is enabled
        return isRmApproved() || (isInspected() && Boolean.TRUE.equals(supervisorBypass));
    }

    public boolean canBeFinalApproved() {
        return isApproved();
    }

    public boolean canBeStored() {
        return isFinalApproved();
    }

    public boolean canBeRejected() {
        return !isRejected() && !isStored();
    }

    /**
     * Get status name for display
     */
    public String getStatusName() {
        if (status == null)
            return "Unknown";
        return switch (status) {
            case 1 -> "Created";
            case 2 -> "Inspected";
            case 3 -> "RM Approved";
            case 4 -> "Approved";
            case 5 -> "Final Approved";
            case 6 -> "Stored";
            case 7 -> "Rejected";
            default -> "Unknown";
        };
    }

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        lastModifiedDate = LocalDateTime.now();
        if (status == null) {
            status = 1; // Created
        }
        if (openingQuantity == null) {
            openingQuantity = BigDecimal.ZERO;
        }
        if (balanceInventory == null) {
            balanceInventory = receivedQuantity;
        }
        if (amount == null && rate != null && receivedQuantity != null) {
            amount = rate.multiply(receivedQuantity);
        }
        if (supervisorBypass == null) {
            supervisorBypass = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
