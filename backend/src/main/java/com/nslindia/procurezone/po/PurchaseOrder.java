package com.nslindia.procurezone.po;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase Order Entity - Header/Master table
 * Represents a purchase order created from approved indent(s)
 */
@Entity
@Table(name = "tbl_purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "po_number", nullable = false, unique = true, length = 50)
    private String poNumber;

    @Column(name = "po_date", nullable = false)
    private LocalDate poDate;

    @Column(name = "indent_id", nullable = false)
    private Integer indentId;

    @Column(name = "vendor_id", nullable = false)
    private Integer vendorId;

    @Column(name = "department_id", nullable = false)
    private Integer departmentId;

    @Column(name = "po_status", nullable = false)
    private Integer poStatus; // 1=Draft, 2=Submitted, 3=Approved, 4=Sent to Vendor, 5=Partially Received,
                              // 6=Fully Received, 7=Cancelled, 8=Closed

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "net_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal netAmount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsConditions;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "priority", length = 20)
    private String priority; // High, Medium, Low

    @Column(name = "approved_by")
    private Integer approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "sent_to_vendor_by")
    private Integer sentToVendorBy;

    @Column(name = "sent_to_vendor_date")
    private LocalDateTime sentToVendorDate;

    @Column(name = "cancelled_by")
    private Integer cancelledBy;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "closed_by")
    private Integer closedBy;

    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_modified_by")
    private Integer lastModifiedBy;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    // One-to-many relationship with PO details
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PurchaseOrderDetail> details = new ArrayList<>();

    // Constructors
    protected PurchaseOrder() {
    }

    public PurchaseOrder(String poNumber, LocalDate poDate, Integer indentId, Integer vendorId,
            Integer departmentId, Integer poStatus, BigDecimal totalAmount,
            BigDecimal netAmount, Integer createdBy) {
        this.poNumber = poNumber;
        this.poDate = poDate;
        this.indentId = indentId;
        this.vendorId = vendorId;
        this.departmentId = departmentId;
        this.poStatus = poStatus;
        this.totalAmount = totalAmount;
        this.netAmount = netAmount;
        this.createdBy = createdBy;
        this.createdDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public LocalDate getPoDate() {
        return poDate;
    }

    public void setPoDate(LocalDate poDate) {
        this.poDate = poDate;
    }

    public Integer getIndentId() {
        return indentId;
    }

    public void setIndentId(Integer indentId) {
        this.indentId = indentId;
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getPoStatus() {
        return poStatus;
    }

    public void setPoStatus(Integer poStatus) {
        this.poStatus = poStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public LocalDate getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(LocalDate actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public String getTermsConditions() {
        return termsConditions;
    }

    public void setTermsConditions(String termsConditions) {
        this.termsConditions = termsConditions;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Integer getSentToVendorBy() {
        return sentToVendorBy;
    }

    public void setSentToVendorBy(Integer sentToVendorBy) {
        this.sentToVendorBy = sentToVendorBy;
    }

    public LocalDateTime getSentToVendorDate() {
        return sentToVendorDate;
    }

    public void setSentToVendorDate(LocalDateTime sentToVendorDate) {
        this.sentToVendorDate = sentToVendorDate;
    }

    public Integer getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(Integer cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public LocalDateTime getCancelledDate() {
        return cancelledDate;
    }

    public void setCancelledDate(LocalDateTime cancelledDate) {
        this.cancelledDate = cancelledDate;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Integer getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(Integer closedBy) {
        this.closedBy = closedBy;
    }

    public LocalDateTime getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(LocalDateTime closedDate) {
        this.closedDate = closedDate;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(Integer lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public List<PurchaseOrderDetail> getDetails() {
        return details;
    }

    public void setDetails(List<PurchaseOrderDetail> details) {
        this.details = details;
    }

    // Helper methods for managing bidirectional relationship
    public void addDetail(PurchaseOrderDetail detail) {
        details.add(detail);
        detail.setPurchaseOrder(this);
    }

    public void removeDetail(PurchaseOrderDetail detail) {
        details.remove(detail);
        detail.setPurchaseOrder(null);
    }

    // Status helper methods
    public boolean isDraft() {
        return this.poStatus == 1;
    }

    public boolean isSubmitted() {
        return this.poStatus == 2;
    }

    public boolean isApproved() {
        return this.poStatus == 3;
    }

    public boolean isSentToVendor() {
        return this.poStatus == 4;
    }

    public boolean isPartiallyReceived() {
        return this.poStatus == 5;
    }

    public boolean isFullyReceived() {
        return this.poStatus == 6;
    }

    public boolean isCancelled() {
        return this.poStatus == 7;
    }

    public boolean isClosed() {
        return this.poStatus == 8;
    }

    public boolean canBeEdited() {
        return isDraft();
    }

    public boolean canBeApproved() {
        return isSubmitted();
    }

    public boolean canBeSentToVendor() {
        return isApproved();
    }

    public boolean canBeCancelled() {
        return !isCancelled() && !isClosed() && !isFullyReceived();
    }

    public boolean canReceiveGoods() {
        return isSentToVendor() || isPartiallyReceived();
    }
}
