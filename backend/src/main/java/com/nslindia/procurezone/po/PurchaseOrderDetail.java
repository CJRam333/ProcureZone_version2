package com.nslindia.procurezone.po;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Purchase Order Detail Entity - Line items
 * Represents individual line items within a purchase order
 */
@Entity
@Table(name = "tbl_purchase_order_details")
public class PurchaseOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column(name = "indent_detail_id", nullable = false)
    private Integer indentDetailId;

    @Column(name = "material_id", nullable = false)
    private Integer materialId;

    @Column(name = "material_code", length = 50)
    private String materialCode;

    @Column(name = "material_name", length = 200, nullable = false)
    private String materialName;

    @Column(name = "material_description", columnDefinition = "TEXT")
    private String materialDescription;

    @Column(name = "quantity", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "line_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal lineTotal;

    @Column(name = "received_quantity", precision = 15, scale = 3)
    private BigDecimal receivedQuantity;

    @Column(name = "pending_quantity", precision = 15, scale = 3)
    private BigDecimal pendingQuantity;

    @Column(name = "rejected_quantity", precision = 15, scale = 3)
    private BigDecimal rejectedQuantity;

    @Column(name = "expected_delivery_date")
    private java.time.LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private java.time.LocalDate actualDeliveryDate;

    @Column(name = "delivery_status", nullable = false)
    private Integer deliveryStatus; // 1=Pending, 2=Partially Delivered, 3=Fully Delivered, 4=Cancelled

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    // Constructors
    protected PurchaseOrderDetail() {
    }

    public PurchaseOrderDetail(Integer lineNumber, Integer indentDetailId, Integer materialId,
            String materialName, BigDecimal quantity, String unitOfMeasure,
            BigDecimal unitPrice, BigDecimal lineTotal) {
        this.lineNumber = lineNumber;
        this.indentDetailId = indentDetailId;
        this.materialId = materialId;
        this.materialName = materialName;
        this.quantity = quantity;
        this.unitOfMeasure = unitOfMeasure;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.receivedQuantity = BigDecimal.ZERO;
        this.pendingQuantity = quantity;
        this.rejectedQuantity = BigDecimal.ZERO;
        this.deliveryStatus = 1; // Pending
        this.createdDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getIndentDetailId() {
        return indentDetailId;
    }

    public void setIndentDetailId(Integer indentDetailId) {
        this.indentDetailId = indentDetailId;
    }

    public Integer getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Integer materialId) {
        this.materialId = materialId;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialDescription() {
        return materialDescription;
    }

    public void setMaterialDescription(String materialDescription) {
        this.materialDescription = materialDescription;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public BigDecimal getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(BigDecimal receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

    public BigDecimal getPendingQuantity() {
        return pendingQuantity;
    }

    public void setPendingQuantity(BigDecimal pendingQuantity) {
        this.pendingQuantity = pendingQuantity;
    }

    public BigDecimal getRejectedQuantity() {
        return rejectedQuantity;
    }

    public void setRejectedQuantity(BigDecimal rejectedQuantity) {
        this.rejectedQuantity = rejectedQuantity;
    }

    public java.time.LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(java.time.LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public java.time.LocalDate getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(java.time.LocalDate actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public Integer getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(Integer deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    // Helper methods
    public boolean isPending() {
        return this.deliveryStatus == 1;
    }

    public boolean isPartiallyDelivered() {
        return this.deliveryStatus == 2;
    }

    public boolean isFullyDelivered() {
        return this.deliveryStatus == 3;
    }

    public boolean isCancelled() {
        return this.deliveryStatus == 4;
    }

    public void receiveQuantity(BigDecimal receivedQty) {
        if (receivedQty == null || receivedQty.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        this.receivedQuantity = this.receivedQuantity.add(receivedQty);
        this.pendingQuantity = this.quantity.subtract(this.receivedQuantity);

        // Update delivery status
        if (this.pendingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            this.deliveryStatus = 3; // Fully Delivered
            this.pendingQuantity = BigDecimal.ZERO;
        } else {
            this.deliveryStatus = 2; // Partially Delivered
        }

        this.actualDeliveryDate = java.time.LocalDate.now();
        this.lastModifiedDate = LocalDateTime.now();
    }

    public void rejectQuantity(BigDecimal rejectedQty) {
        if (rejectedQty == null || rejectedQty.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        this.rejectedQuantity = this.rejectedQuantity.add(rejectedQty);
        this.lastModifiedDate = LocalDateTime.now();
    }

    public BigDecimal getDeliveryPercentage() {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return receivedQuantity.divide(quantity, 2, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
