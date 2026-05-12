package com.nslindia.procurezone.po;

import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.indent.Indent;
import com.nslindia.procurezone.indent.IndentDetail;
import com.nslindia.procurezone.indent.IndentRepository;
import com.nslindia.procurezone.indent.IndentStatus;
import com.nslindia.procurezone.masterdata.Department;
import com.nslindia.procurezone.masterdata.repository.DepartmentRepository;
import com.nslindia.procurezone.notification.service.EmailService;
import com.nslindia.procurezone.po.dto.*;
import com.nslindia.procurezone.vendor.Vendor;
import com.nslindia.procurezone.vendor.VendorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for Purchase Order business logic
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class POService {

    private final PORepository poRepository;
    private final PODetailRepository poDetailRepository;
    private final POAmendmentRepository poAmendmentRepository;
    private final IndentRepository indentRepository;
    private final VendorRepository vendorRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;
    private final EmailService emailService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get all approved indents that are ready for PO creation
     */
    @Transactional(readOnly = true)
    public List<ApprovedIndentDTO> getApprovedIndents() {
        log.info("Fetching approved indents ready for PO creation");

        // Find all indents and filter by approved status
        List<Indent> allIndents = indentRepository.findAll();

        return allIndents.stream()
                .filter(indent -> indent.getFinalStatus() != null && indent.getFinalStatus().getId() == 5) // Approved
                .filter(indent -> poRepository.findByIndentId(indent.getId()).isEmpty())
                .map(this::mapToApprovedIndentDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create PO from an approved indent
     */
    public PurchaseOrderResponse createPOFromIndent(CreatePORequest request, Integer createdBy) {
        log.info("Creating PO from indent ID: {} for vendor ID: {}", request.indentId(), request.vendorId());

        // Validate indent
        Indent indent = indentRepository.findById(request.indentId())
                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with ID: " + request.indentId()));

        // Allow PO creation from Department Head approved (3) or fully approved (5)
        // indents
        boolean isDeptHeadApproved = indent.getApprovedStatus() != null && indent.getApprovedStatus().getId() == 3;
        boolean isFinallyApproved = indent.getFinalStatus() != null && indent.getFinalStatus().getId() == 5;

        if (!isDeptHeadApproved && !isFinallyApproved) {
            throw new BadRequestException(
                    "Only approved indents (Department Head or Final Approval) can be converted to PO. Current status: "
                            +
                            (indent.getStatus() != null ? indent.getStatus().getName() : "Unknown"));
        }

        // Check if PO already exists for this indent
        List<PurchaseOrder> existingPOs = poRepository.findByIndentId(request.indentId());
        if (!existingPOs.isEmpty()) {
            throw new BadRequestException("PO already exists for indent: " + indent.getIndentNumber());
        }

        // Validate vendor
        Vendor vendor = vendorRepository.findById(request.vendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + request.vendorId()));

        if (vendor.getStatus() != 1) {
            throw new BadRequestException("Vendor is not active: " + vendor.getVendorName());
        }

        // Create PO
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(generatePONumber());
        po.setPoDate(LocalDate.now());
        po.setIndentId(indent.getId());
        po.setVendorId(vendor.getId());
        po.setDepartmentId(indent.getDepartment() != null ? indent.getDepartment().getId() : null);
        po.setPoStatus(1); // Draft
        po.setPaymentTerms(request.paymentTerms());
        po.setDeliveryAddress(request.deliveryAddress() != null ? request.deliveryAddress() : "");
        po.setExpectedDeliveryDate(
                request.expectedDeliveryDate() != null ? request.expectedDeliveryDate() : indent.getDeliveryDate());
        po.setPriority(request.priority() != null ? request.priority() : "Medium");
        po.setTermsConditions(request.termsConditions());
        po.setNotes(request.notes());
        po.setCurrency("INR");
        po.setCreatedBy(createdBy);
        po.setCreatedDate(LocalDateTime.now());

        // Create PO details from indent details
        List<PurchaseOrderDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;

        int lineNumber = 1;
        for (POLineItemRequest lineItem : request.lineItems()) {
            IndentDetail indentDetail = indent.getDetails().stream()
                    .filter(id -> id.getId().equals(lineItem.indentDetailId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Indent detail not found with ID: " + lineItem.indentDetailId()));

            // C.1 CRITICAL FIX: Validate PO quantity does not exceed indent quantity
            BigDecimal indentQuantity = indentDetail.getQuantity();
            if (indentQuantity == null) {
                throw new BadRequestException("Indent detail " + indentDetail.getId() + " has no quantity specified");
            }
            if (lineItem.quantity().compareTo(indentQuantity) > 0) {
                throw new BadRequestException(
                        String.format("PO quantity (%.2f) cannot exceed indent quantity (%.2f) for material: %s",
                                lineItem.quantity(), indentQuantity,
                                indentDetail.getMaterial() != null ? indentDetail.getMaterial().getName()
                                        : "ID:" + lineItem.materialId()));
            }

            PurchaseOrderDetail detail = new PurchaseOrderDetail();
            detail.setLineNumber(lineNumber++);
            detail.setIndentDetailId(indentDetail.getId());
            detail.setMaterialId(
                    indentDetail.getMaterial() != null ? indentDetail.getMaterial().getId() : lineItem.materialId());
            detail.setMaterialCode(indentDetail.getMaterial() != null ? indentDetail.getMaterial().getCode() : "");
            detail.setMaterialName(indentDetail.getMaterial() != null ? indentDetail.getMaterial().getName() : "");
            detail.setMaterialDescription(
                    indentDetail.getMaterial() != null ? indentDetail.getMaterial().getDescription() : "");
            detail.setQuantity(lineItem.quantity());
            detail.setUnitOfMeasure(
                    indentDetail.getUnitOfMeasure() != null ? indentDetail.getUnitOfMeasure().getCode() : "");
            detail.setUnitPrice(lineItem.unitPrice());
            detail.setTaxRate(lineItem.taxRate() != null ? lineItem.taxRate() : BigDecimal.ZERO);
            detail.setDiscountRate(lineItem.discountRate() != null ? lineItem.discountRate() : BigDecimal.ZERO);
            detail.setExpectedDeliveryDate(lineItem.expectedDeliveryDate());
            detail.setNotes(lineItem.notes());
            detail.setDeliveryStatus(1); // Pending
            detail.setPendingQuantity(lineItem.quantity());
            detail.setReceivedQuantity(BigDecimal.ZERO);
            detail.setRejectedQuantity(BigDecimal.ZERO);
            detail.setCreatedDate(LocalDateTime.now());

            // Calculate amounts
            BigDecimal lineTotal = lineItem.quantity().multiply(lineItem.unitPrice());
            BigDecimal taxAmount = lineTotal.multiply(detail.getTaxRate()).divide(new BigDecimal("100"), 2,
                    RoundingMode.HALF_UP);
            BigDecimal discountAmount = lineTotal.multiply(detail.getDiscountRate()).divide(new BigDecimal("100"), 2,
                    RoundingMode.HALF_UP);

            detail.setTaxAmount(taxAmount);
            detail.setDiscountAmount(discountAmount);
            detail.setLineTotal(lineTotal.add(taxAmount).subtract(discountAmount));

            totalAmount = totalAmount.add(lineTotal);
            totalTaxAmount = totalTaxAmount.add(taxAmount);
            totalDiscountAmount = totalDiscountAmount.add(discountAmount);

            po.addDetail(detail);
            details.add(detail);
        }

        po.setTotalAmount(totalAmount);
        po.setTaxAmount(totalTaxAmount);
        po.setDiscountAmount(totalDiscountAmount);
        po.setNetAmount(totalAmount.add(totalTaxAmount).subtract(totalDiscountAmount));

        // Save PO
        PurchaseOrder savedPO = poRepository.save(po);

        // INDENT-PO STATUS LINKAGE: Update indent status to PO_CREATED (status=4)
        // This ensures the indent workflow reflects that a PO has been generated
        updateIndentStatusToPOCreated(indent, savedPO.getPoNumber(), createdBy);

        // Audit log
        auditService.logEntityChange(
                "CREATE",
                "PurchaseOrder",
                savedPO.getId(),
                createdBy,
                "System",
                "PO created from indent: " + indent.getIndentNumber());

        log.info("PO created successfully: {} (Indent {} status updated to PO_CREATED)",
                savedPO.getPoNumber(), indent.getIndentNumber());
        return mapToPurchaseOrderResponse(savedPO);
    }

    /**
     * Update indent status when PO is created.
     * Links the PO to the indent and updates status to PO_CREATED (4).
     */
    private void updateIndentStatusToPOCreated(Indent indent, String poNumber, Integer updatedBy) {
        try {
            // Status 4 = PO_CREATED in the legacy workflow
            IndentStatus poCreatedStatus = entityManager.find(IndentStatus.class, 4);
            if (poCreatedStatus != null) {
                indent.setStatus(poCreatedStatus);
            }

            indent.setPoNumber(poNumber);
            indent.setLastModifiedDate(LocalDateTime.now());
            indent.setLastModifiedBy(updatedBy);

            indentRepository.save(indent);

            log.info("Updated indent {} status to PO_CREATED, linked PO: {}",
                    indent.getIndentNumber(), poNumber);

            // Audit the status change on indent
            auditService.logEntityChange(
                    "STATUS_UPDATE",
                    "Indent",
                    indent.getId(),
                    updatedBy,
                    "System",
                    String.format("Indent status updated to PO_CREATED, PO: %s", poNumber));
        } catch (Exception e) {
            log.error("Failed to update indent status for PO creation: {}", e.getMessage());
            // Don't fail PO creation if indent update fails
        }
    }

    /**
     * Get PO by ID with all details
     */
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPOById(Integer id) {
        log.info("Fetching PO with ID: {}", id);
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
        return mapToPurchaseOrderResponse(po);
    }

    /**
     * Get PO by PO number
     */
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPOByNumber(String poNumber) {
        log.info("Fetching PO with number: {}", poNumber);
        PurchaseOrder po = poRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with number: " + poNumber));
        return mapToPurchaseOrderResponse(po);
    }

    /**
     * Update PO
     */
    public PurchaseOrderResponse updatePO(Integer id, UpdatePORequest request, Integer employeeNumber) {
        log.info("Updating PO ID: {} by user: {}", id, employeeNumber);

        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        // Only allow updates to DRAFT status POs
        POStatus currentStatus = POStatus.fromCode(po.getPoStatus());
        if (currentStatus != POStatus.DRAFT) {
            throw new BadRequestException(
                    "Cannot update PO. Only DRAFT purchase orders can be updated. Current status: "
                            + currentStatus.getDisplayName());
        }

        StringBuilder changes = new StringBuilder();

        // Update fields if provided
        if (request.deliveryDate() != null && !request.deliveryDate().equals(po.getDeliveryDate())) {
            changes.append("Delivery Date: ").append(po.getDeliveryDate()).append(" -> ").append(request.deliveryDate())
                    .append("; ");
            po.setDeliveryDate(request.deliveryDate());
        }

        if (request.deliveryAddress() != null && !request.deliveryAddress().equals(po.getDeliveryAddress())) {
            changes.append("Delivery Address changed; ");
            po.setDeliveryAddress(request.deliveryAddress());
        }

        if (request.paymentTerms() != null && !request.paymentTerms().equals(po.getPaymentTerms())) {
            changes.append("Payment Terms changed; ");
            po.setPaymentTerms(request.paymentTerms());
        }

        if (request.termsConditions() != null && !request.termsConditions().equals(po.getTermsConditions())) {
            changes.append("Terms & Conditions changed; ");
            po.setTermsConditions(request.termsConditions());
        }

        if (request.notes() != null && !request.notes().equals(po.getNotes())) {
            changes.append("Notes changed; ");
            po.setNotes(request.notes());
        }

        if (request.priority() != null && !request.priority().equals(po.getPriority())) {
            changes.append("Priority: ").append(po.getPriority()).append(" -> ").append(request.priority())
                    .append("; ");
            po.setPriority(request.priority());
        }

        if (changes.length() == 0) {
            log.info("No changes detected for PO ID: {}", id);
            return mapToPurchaseOrderResponse(po);
        }

        po.setLastModifiedBy(employeeNumber);
        po.setLastModifiedDate(LocalDateTime.now());

        PurchaseOrder savedPO = poRepository.save(po);

        auditService.logEntityChange(
                "UPDATE",
                "PurchaseOrder",
                savedPO.getId(),
                employeeNumber,
                "System",
                changes.toString());

        log.info("PO updated successfully: {}", savedPO.getPoNumber());
        return mapToPurchaseOrderResponse(savedPO);
    }

    /**
     * Get all POs with pagination and filtering
     */
    @Transactional(readOnly = true)
    public Page<POSummaryResponse> getAllPOs(Integer status, String searchTerm, Pageable pageable) {
        log.info("Fetching POs - status: {}, searchTerm: {}", status, searchTerm);

        Page<PurchaseOrder> pos;

        if (searchTerm != null && !searchTerm.isBlank()) {
            pos = poRepository.searchPOs(searchTerm, pageable);
        } else if (status != null) {
            pos = poRepository.findByPoStatus(status, pageable);
        } else {
            pos = poRepository.findAll(pageable);
        }

        return pos.map(this::mapToPOSummaryResponse);
    }

    /**
     * Get POs by vendor
     */
    @Transactional(readOnly = true)
    public Page<POSummaryResponse> getPOsByVendor(Integer vendorId, Pageable pageable) {
        log.info("Fetching POs for vendor ID: {}", vendorId);
        Page<PurchaseOrder> pos = poRepository.findByVendorId(vendorId, pageable);
        return pos.map(this::mapToPOSummaryResponse);
    }

    /**
     * Get POs by department
     */
    @Transactional(readOnly = true)
    public Page<POSummaryResponse> getPOsByDepartment(Integer departmentId, Pageable pageable) {
        log.info("Fetching POs for department ID: {}", departmentId);
        Page<PurchaseOrder> pos = poRepository.findByDepartmentId(departmentId, pageable);
        return pos.map(this::mapToPOSummaryResponse);
    }

    /**
     * Submit PO for approval
     */
    public PurchaseOrderResponse submitPOForApproval(Integer id, Integer submittedBy) {
        log.info("Submitting PO ID: {} for approval by user: {}", id, submittedBy);

        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        POStatus currentStatus = POStatus.fromCode(po.getPoStatus());
        POStatus newStatus = POStatus.SUBMITTED;

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BadRequestException("Cannot submit PO. Current status: " + currentStatus.getDisplayName());
        }

        po.setPoStatus(newStatus.getCode());
        po.setLastModifiedBy(submittedBy);
        po.setLastModifiedDate(LocalDateTime.now());

        PurchaseOrder savedPO = poRepository.save(po);

        auditService.logEntityChange(
                "UPDATE",
                "PurchaseOrder",
                savedPO.getId(),
                submittedBy,
                "System",
                "Status: " + currentStatus.getDisplayName() + " -> " + newStatus.getDisplayName()
                        + " - PO submitted for approval");

        log.info("PO submitted successfully: {}", savedPO.getPoNumber());
        return mapToPurchaseOrderResponse(savedPO);
    }

    /**
     * Approve PO
     */
    public PurchaseOrderResponse approvePO(Integer id, Integer approvedBy) {
        log.info("Approving PO ID: {} by user: {}", id, approvedBy);

        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        POStatus currentStatus = POStatus.fromCode(po.getPoStatus());
        POStatus newStatus = POStatus.APPROVED;

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BadRequestException("Cannot approve PO. Current status: " + currentStatus.getDisplayName());
        }

        po.setPoStatus(newStatus.getCode());
        po.setApprovedBy(approvedBy);
        po.setApprovedDate(LocalDateTime.now());
        po.setLastModifiedBy(approvedBy);
        po.setLastModifiedDate(LocalDateTime.now());

        PurchaseOrder savedPO = poRepository.save(po);

        auditService.logEntityChange(
                "UPDATE",
                "PurchaseOrder",
                savedPO.getId(),
                approvedBy,
                "System",
                "Status: " + currentStatus.getDisplayName() + " -> " + newStatus.getDisplayName() + " - PO approved");

        log.info("PO approved successfully: {}", savedPO.getPoNumber());
        return mapToPurchaseOrderResponse(savedPO);
    }

    /**
     * Send PO to vendor
     */
    public PurchaseOrderResponse sendPOToVendor(Integer id, Integer sentBy) {
        log.info("Sending PO ID: {} to vendor by user: {}", id, sentBy);

        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        POStatus currentStatus = POStatus.fromCode(po.getPoStatus());
        POStatus newStatus = POStatus.SENT_TO_VENDOR;

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BadRequestException(
                    "Cannot send PO to vendor. Current status: " + currentStatus.getDisplayName());
        }

        po.setPoStatus(newStatus.getCode());
        po.setSentToVendorBy(sentBy);
        po.setSentToVendorDate(LocalDateTime.now());
        po.setLastModifiedBy(sentBy);
        po.setLastModifiedDate(LocalDateTime.now());

        PurchaseOrder savedPO = poRepository.save(po);

        // Send email notification to vendor
        sendVendorNotification(savedPO);

        auditService.logEntityChange(
                "UPDATE",
                "PurchaseOrder",
                savedPO.getId(),
                sentBy,
                "System",
                "Status: " + currentStatus.getDisplayName() + " -> " + newStatus.getDisplayName()
                        + " - PO sent to vendor");

        log.info("PO sent to vendor successfully: {}", savedPO.getPoNumber());
        return mapToPurchaseOrderResponse(savedPO);
    }

    /**
     * Receive goods against PO
     */
    public PurchaseOrderResponse receiveGoods(Integer poId, List<ReceiveGoodsRequest> receiveRequests,
            Integer receivedBy) {
        log.info("Receiving goods for PO ID: {} by user: {}", poId, receivedBy);

        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + poId));

        POStatus currentStatus = POStatus.fromCode(po.getPoStatus());
        if (!currentStatus.canReceiveGoods()) {
            throw new BadRequestException("Cannot receive goods. Current status: " + currentStatus.getDisplayName());
        }

        // Process each receipt
        for (ReceiveGoodsRequest request : receiveRequests) {
            PurchaseOrderDetail detail = poDetailRepository.findById(request.poDetailId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "PO detail not found with ID: " + request.poDetailId()));

            if (!detail.getPurchaseOrder().getId().equals(poId)) {
                throw new BadRequestException("PO detail does not belong to this PO");
            }

            // Receive quantity
            detail.receiveQuantity(request.receivedQuantity());

            // Reject quantity if any
            if (request.rejectedQuantity() != null && request.rejectedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                detail.rejectQuantity(request.rejectedQuantity());
            }

            // Update notes if provided
            if (request.remarks() != null) {
                String existingNotes = detail.getNotes() != null ? detail.getNotes() : "";
                detail.setNotes(existingNotes + "\nReceipt: " + request.remarks());
            }

            detail.setLastModifiedDate(LocalDateTime.now());
            poDetailRepository.save(detail);

            auditService.logEntityChange(
                    "UPDATE",
                    "PurchaseOrderDetail",
                    detail.getId(),
                    receivedBy,
                    "System",
                    "Received: " + request.receivedQuantity() + " " + detail.getUnitOfMeasure() + " - Goods received");
        }

        // Check if all items are fully delivered
        boolean allFullyDelivered = poDetailRepository.areAllItemsFullyDelivered(poId);
        POStatus newStatus = allFullyDelivered ? POStatus.FULLY_RECEIVED : POStatus.PARTIALLY_RECEIVED;

        po.setPoStatus(newStatus.getCode());
        po.setLastModifiedBy(receivedBy);
        po.setLastModifiedDate(LocalDateTime.now());

        if (allFullyDelivered) {
            po.setActualDeliveryDate(LocalDate.now());
        }

        PurchaseOrder savedPO = poRepository.save(po);

        auditService.logEntityChange(
                "UPDATE",
                "PurchaseOrder",
                savedPO.getId(),
                receivedBy,
                "System",
                "Status: " + currentStatus.getDisplayName() + " -> " + newStatus.getDisplayName() + " - Goods received "
                        + receiveRequests.size() + " items");

        log.info("Goods received successfully for PO: {}", savedPO.getPoNumber());
        return mapToPurchaseOrderResponse(savedPO);
    }

    /**
     * Cancel PO
     */
    public PurchaseOrderResponse cancelPO(Integer id, CancelPORequest request, Integer cancelledBy) {
        log.info("Cancelling PO ID: {} by user: {}", id, cancelledBy);

        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        POStatus currentStatus = POStatus.fromCode(po.getPoStatus());
        POStatus newStatus = POStatus.CANCELLED;

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BadRequestException("Cannot cancel PO. Current status: " + currentStatus.getDisplayName());
        }

        po.setPoStatus(newStatus.getCode());
        po.setCancelledBy(cancelledBy);
        po.setCancelledDate(LocalDateTime.now());
        po.setCancellationReason(request.cancellationReason());
        po.setLastModifiedBy(cancelledBy);
        po.setLastModifiedDate(LocalDateTime.now());

        // Cancel all pending line items
        po.getDetails().forEach(detail -> {
            if (detail.getDeliveryStatus() != 3) { // Not fully delivered
                detail.setDeliveryStatus(4); // Cancelled
                detail.setLastModifiedDate(LocalDateTime.now());
            }
        });

        PurchaseOrder savedPO = poRepository.save(po);

        auditService.logEntityChange(
                "UPDATE",
                "PurchaseOrder",
                savedPO.getId(),
                cancelledBy,
                "System",
                "Status: " + currentStatus.getDisplayName() + " -> " + newStatus.getDisplayName() + " - Cancelled: "
                        + request.cancellationReason());

        log.info("PO cancelled successfully: {}", savedPO.getPoNumber());
        return mapToPurchaseOrderResponse(savedPO);
    }

    /**
     * Close PO
     */
    public PurchaseOrderResponse closePO(Integer id, Integer closedBy) {
        log.info("Closing PO ID: {} by user: {}", id, closedBy);

        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        POStatus currentStatus = POStatus.fromCode(po.getPoStatus());
        POStatus newStatus = POStatus.CLOSED;

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BadRequestException("Cannot close PO. Current status: " + currentStatus.getDisplayName());
        }

        po.setPoStatus(newStatus.getCode());
        po.setClosedBy(closedBy);
        po.setClosedDate(LocalDateTime.now());
        po.setLastModifiedBy(closedBy);
        po.setLastModifiedDate(LocalDateTime.now());

        PurchaseOrder savedPO = poRepository.save(po);

        auditService.logEntityChange(
                "UPDATE",
                "PurchaseOrder",
                savedPO.getId(),
                closedBy,
                "System",
                "Status: " + currentStatus.getDisplayName() + " -> " + newStatus.getDisplayName() + " - PO closed");

        log.info("PO closed successfully: {}", savedPO.getPoNumber());
        return mapToPurchaseOrderResponse(savedPO);
    }

    /**
     * Get pending POs for approval
     */
    @Transactional(readOnly = true)
    public Page<POSummaryResponse> getPendingForApproval(Pageable pageable) {
        log.info("Fetching POs pending for approval");
        Page<PurchaseOrder> pos = poRepository.findPendingForApproval(pageable);
        return pos.map(this::mapToPOSummaryResponse);
    }

    /**
     * Get overdue POs
     */
    @Transactional(readOnly = true)
    public Page<POSummaryResponse> getOverduePOs(Pageable pageable) {
        log.info("Fetching overdue POs");
        Page<PurchaseOrder> pos = poRepository.findOverduePOs(LocalDate.now(), pageable);
        return pos.map(this::mapToPOSummaryResponse);
    }

    /**
     * Get dashboard statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatistics() {
        log.info("Fetching PO dashboard statistics");
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalPOs", poRepository.count());
        stats.put("draftCount", poRepository.countByPoStatus(1));
        stats.put("pendingApprovalCount", poRepository.countByPoStatus(2));
        stats.put("approvedCount", poRepository.countByPoStatus(3));
        stats.put("sentToVendorCount", poRepository.countByPoStatus(4));
        stats.put("partiallyReceivedCount", poRepository.countByPoStatus(5));
        stats.put("fullyReceivedCount", poRepository.countByPoStatus(6));
        stats.put("cancelledCount", poRepository.countByPoStatus(7));
        stats.put("closedCount", poRepository.countByPoStatus(8));
        
        stats.put("totalValue", 0.0);
        stats.put("averageValue", 0.0);
        
        return stats;
    }

    // ==================== Helper Methods ====================

    private String generatePONumber() {
        String prefix = "PO";
        String year = String.valueOf(LocalDate.now().getYear());
        String month = String.format("%02d", LocalDate.now().getMonthValue());

        // Find last PO number for this year-month
        String pattern = prefix + year + month;
        long count = poRepository.count() + 1;
        String sequence = String.format("%05d", count);

        String poNumber = pattern + sequence;

        // Ensure uniqueness
        while (poRepository.existsByPoNumber(poNumber)) {
            count++;
            sequence = String.format("%05d", count);
            poNumber = pattern + sequence;
        }

        return poNumber;
    }

    private ApprovedIndentDTO mapToApprovedIndentDTO(Indent indent) {
        return new ApprovedIndentDTO(
                indent.getId(),
                indent.getIndentNumber(),
                indent.getIndentDate() != null ? indent.getIndentDate().toLocalDate() : null,
                indent.getDepartment() != null ? indent.getDepartment().getId() : null,
                indent.getDepartment() != null ? indent.getDepartment().getName() : null,
                indent.getEmployee() != null ? indent.getEmployee().getEmpNumber() : null,
                indent.getEmployee() != null ? indent.getEmployee().getFullName() : null,
                indent.getComments(),
                "Medium",
                indent.getDetails() != null ? indent.getDetails().size() : 0,
                indent.getDeliveryDate());
    }

    private PurchaseOrderResponse mapToPurchaseOrderResponse(PurchaseOrder po) {
        // Fetch indent and vendor details
        Indent indent = indentRepository.findById(po.getIndentId()).orElse(null);
        Vendor vendor = vendorRepository.findById(po.getVendorId()).orElse(null);

        // Fetch department name
        String departmentName = null;
        if (po.getDepartmentId() != null) {
            departmentName = departmentRepository.findById(po.getDepartmentId())
                    .map(Department::getName)
                    .orElse(null);
        }

        List<PODetailResponse> detailResponses = po.getDetails().stream()
                .map(this::mapToPODetailResponse)
                .collect(Collectors.toList());

        return new PurchaseOrderResponse(
                po.getId(),
                po.getPoNumber(),
                po.getPoDate(),
                po.getIndentId(),
                indent != null ? indent.getIndentNumber() : null,
                po.getVendorId(),
                vendor != null ? vendor.getVendorCode() : null,
                vendor != null ? vendor.getVendorName() : null,
                po.getDepartmentId(),
                departmentName,
                po.getPoStatus(),
                POStatus.fromCode(po.getPoStatus()).getDisplayName(),
                po.getTotalAmount(),
                po.getTaxAmount(),
                po.getDiscountAmount(),
                po.getNetAmount(),
                po.getCurrency(),
                po.getPaymentTerms(),
                po.getDeliveryAddress(),
                po.getDeliveryDate(),
                po.getExpectedDeliveryDate(),
                po.getActualDeliveryDate(),
                po.getTermsConditions(),
                po.getNotes(),
                po.getPriority(),
                po.getApprovedBy(),
                po.getApprovedDate(),
                po.getSentToVendorBy(),
                po.getSentToVendorDate(),
                po.getCancelledBy(),
                po.getCancelledDate(),
                po.getCancellationReason(),
                po.getClosedBy(),
                po.getClosedDate(),
                po.getCreatedDate(),
                po.getLastModifiedDate(),
                detailResponses);
    }

    private PODetailResponse mapToPODetailResponse(PurchaseOrderDetail detail) {
        String deliveryStatusName = switch (detail.getDeliveryStatus()) {
            case 1 -> "Pending";
            case 2 -> "Partially Delivered";
            case 3 -> "Fully Delivered";
            case 4 -> "Cancelled";
            default -> "Unknown";
        };

        return new PODetailResponse(
                detail.getId(),
                detail.getLineNumber(),
                detail.getIndentDetailId(),
                detail.getMaterialId(),
                detail.getMaterialCode(),
                detail.getMaterialName(),
                detail.getMaterialDescription(),
                detail.getQuantity(),
                detail.getUnitOfMeasure(),
                detail.getUnitPrice(),
                detail.getTaxRate(),
                detail.getTaxAmount(),
                detail.getDiscountRate(),
                detail.getDiscountAmount(),
                detail.getLineTotal(),
                detail.getReceivedQuantity(),
                detail.getPendingQuantity(),
                detail.getRejectedQuantity(),
                detail.getExpectedDeliveryDate(),
                detail.getActualDeliveryDate(),
                detail.getDeliveryStatus(),
                deliveryStatusName,
                detail.getDeliveryPercentage(),
                detail.getNotes());
    }

    private POSummaryResponse mapToPOSummaryResponse(PurchaseOrder po) {
        Vendor vendor = vendorRepository.findById(po.getVendorId()).orElse(null);

        // Calculate total delivery percentage
        BigDecimal totalDeliveryPercentage = BigDecimal.ZERO;
        if (po.getDetails() != null && !po.getDetails().isEmpty()) {
            BigDecimal sum = po.getDetails().stream()
                    .map(PurchaseOrderDetail::getDeliveryPercentage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalDeliveryPercentage = sum.divide(new BigDecimal(po.getDetails().size()), 2, RoundingMode.HALF_UP);
        }

        return new POSummaryResponse(
                po.getId(),
                po.getPoNumber(),
                po.getPoDate(),
                vendor != null ? vendor.getVendorName() : null,
                null, // departmentName
                po.getPoStatus(),
                POStatus.fromCode(po.getPoStatus()).getDisplayName(),
                po.getNetAmount(),
                po.getPriority(),
                po.getExpectedDeliveryDate(),
                po.getDetails() != null ? po.getDetails().size() : 0,
                totalDeliveryPercentage);
    }

    /**
     * Send email notification to vendor when PO is sent
     */
    private void sendVendorNotification(PurchaseOrder po) {
        try {
            Vendor vendor = vendorRepository.findById(po.getVendorId()).orElse(null);
            if (vendor == null || vendor.getContactEmail() == null || vendor.getContactEmail().isBlank()) {
                log.warn("Cannot send email notification for PO {}: vendor or email not found", po.getPoNumber());
                return;
            }

            Map<String, Object> variables = Map.of(
                    "vendorName", vendor.getVendorName(),
                    "poNumber", po.getPoNumber(),
                    "poDate", po.getPoDate().toString(),
                    "totalAmount", po.getNetAmount().toString(),
                    "expectedDeliveryDate", po.getExpectedDeliveryDate() != null
                            ? po.getExpectedDeliveryDate().toString()
                            : "To be confirmed",
                    "itemCount", String.valueOf(po.getDetails() != null ? po.getDetails().size() : 0),
                    "priority", po.getPriority() != null ? po.getPriority() : "NORMAL");

            boolean sent = emailService.sendEmailFromTemplate(
                    "PO_SENT_TO_VENDOR",
                    variables,
                    vendor.getContactEmail());

            if (sent) {
                log.info("Email notification sent to vendor {} for PO {}",
                        vendor.getVendorName(), po.getPoNumber());
            } else {
                log.warn("Failed to send email notification to vendor {} for PO {}",
                        vendor.getVendorName(), po.getPoNumber());
            }
        } catch (Exception e) {
            // Don't fail the PO send if email fails - just log the error
            log.error("Error sending email notification for PO {}: {}", po.getPoNumber(), e.getMessage());
        }
    }

    // =====================================================
    // B.1 FIX: PO AMENDMENT WORKFLOW - Track amendments after vendor confirmation
    // =====================================================

    /**
     * B.1 FIX: Amend a PO after it has been sent to vendor
     * All changes are tracked in POAmendment table for audit purposes
     */
    public PurchaseOrderResponse amendPO(Integer id, AmendPORequest request, Integer employeeNumber) {
        log.info("Amending PO ID: {} by user: {}", id, employeeNumber);

        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));

        // Only allow amendments to POs that have been sent to vendor but not fully
        // received/cancelled/closed
        POStatus currentStatus = POStatus.fromCode(po.getPoStatus());
        if (currentStatus != POStatus.SENT_TO_VENDOR && currentStatus != POStatus.PARTIALLY_RECEIVED) {
            throw new BadRequestException(
                    "Cannot amend PO. Only 'Sent to Vendor' or 'Partially Received' POs can be amended. Current status: "
                            + currentStatus.getDisplayName());
        }

        // Get the next amendment version
        Integer nextVersion = poAmendmentRepository.findMaxAmendmentVersionByPoId(id) + 1;
        List<POAmendment> amendments = new ArrayList<>();
        StringBuilder changes = new StringBuilder();

        // Track each field change
        if (request.deliveryDate() != null && !request.deliveryDate().equals(po.getDeliveryDate())) {
            amendments.add(new POAmendment(
                    id, nextVersion, "deliveryDate",
                    po.getDeliveryDate() != null ? po.getDeliveryDate().toString() : null,
                    request.deliveryDate().toString(),
                    request.amendmentReason(), employeeNumber));
            changes.append("Delivery Date: ").append(po.getDeliveryDate()).append(" -> ")
                    .append(request.deliveryDate()).append("; ");
            po.setDeliveryDate(request.deliveryDate());
        }

        if (request.deliveryAddress() != null && !request.deliveryAddress().equals(po.getDeliveryAddress())) {
            amendments.add(new POAmendment(
                    id, nextVersion, "deliveryAddress",
                    po.getDeliveryAddress(),
                    request.deliveryAddress(),
                    request.amendmentReason(), employeeNumber));
            changes.append("Delivery Address changed; ");
            po.setDeliveryAddress(request.deliveryAddress());
        }

        if (request.paymentTerms() != null && !request.paymentTerms().equals(po.getPaymentTerms())) {
            amendments.add(new POAmendment(
                    id, nextVersion, "paymentTerms",
                    po.getPaymentTerms(),
                    request.paymentTerms(),
                    request.amendmentReason(), employeeNumber));
            changes.append("Payment Terms changed; ");
            po.setPaymentTerms(request.paymentTerms());
        }

        if (request.termsConditions() != null && !request.termsConditions().equals(po.getTermsConditions())) {
            amendments.add(new POAmendment(
                    id, nextVersion, "termsConditions",
                    po.getTermsConditions(),
                    request.termsConditions(),
                    request.amendmentReason(), employeeNumber));
            changes.append("Terms & Conditions changed; ");
            po.setTermsConditions(request.termsConditions());
        }

        if (request.notes() != null && !request.notes().equals(po.getNotes())) {
            amendments.add(new POAmendment(
                    id, nextVersion, "notes",
                    po.getNotes(),
                    request.notes(),
                    request.amendmentReason(), employeeNumber));
            changes.append("Notes changed; ");
            po.setNotes(request.notes());
        }

        if (request.priority() != null && !request.priority().equals(po.getPriority())) {
            amendments.add(new POAmendment(
                    id, nextVersion, "priority",
                    po.getPriority(),
                    request.priority(),
                    request.amendmentReason(), employeeNumber));
            changes.append("Priority: ").append(po.getPriority()).append(" -> ").append(request.priority())
                    .append("; ");
            po.setPriority(request.priority());
        }

        if (amendments.isEmpty()) {
            log.info("No amendments detected for PO ID: {}", id);
            return mapToPurchaseOrderResponse(po);
        }

        // Save all amendments
        poAmendmentRepository.saveAll(amendments);

        po.setLastModifiedBy(employeeNumber);
        po.setLastModifiedDate(LocalDateTime.now());

        PurchaseOrder savedPO = poRepository.save(po);

        // Audit log with amendment details
        auditService.logEntityChange(
                "AMEND",
                "PurchaseOrder",
                savedPO.getId(),
                employeeNumber,
                "System",
                "Amendment Version " + nextVersion + ": " + changes.toString() + " Reason: "
                        + request.amendmentReason());

        log.info("PO amended successfully: {} (Amendment Version: {})", savedPO.getPoNumber(), nextVersion);
        return mapToPurchaseOrderResponse(savedPO);
    }

    /**
     * B.1 FIX: Get amendment history for a PO
     */
    @Transactional(readOnly = true)
    public List<POAmendmentResponse> getAmendmentHistory(Integer poId) {
        log.info("Fetching amendment history for PO ID: {}", poId);

        // Verify PO exists
        if (!poRepository.existsById(poId)) {
            throw new ResourceNotFoundException("Purchase Order not found with ID: " + poId);
        }

        List<POAmendment> amendments = poAmendmentRepository.findByPoIdOrderByAmendedDateDesc(poId);

        return amendments.stream()
                .map(this::mapToAmendmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * B.1 FIX: Get the current amendment version of a PO
     */
    @Transactional(readOnly = true)
    public Integer getCurrentAmendmentVersion(Integer poId) {
        if (!poRepository.existsById(poId)) {
            throw new ResourceNotFoundException("Purchase Order not found with ID: " + poId);
        }
        return poAmendmentRepository.findMaxAmendmentVersionByPoId(poId);
    }

    /**
     * B.1 FIX: Map POAmendment to response DTO
     */
    private POAmendmentResponse mapToAmendmentResponse(POAmendment amendment) {
        String amendedByName = null;
        String approvedByName = null;

        if (amendment.getAmendedBy() != null) {
            amendedByName = employeeRepository.findById(amendment.getAmendedBy())
                    .map(Employee::getFullName)
                    .orElse("Unknown");
        }

        if (amendment.getApprovedBy() != null) {
            approvedByName = employeeRepository.findById(amendment.getApprovedBy())
                    .map(Employee::getFullName)
                    .orElse("Unknown");
        }

        return new POAmendmentResponse(
                amendment.getId(),
                amendment.getPoId(),
                amendment.getAmendmentVersion(),
                amendment.getFieldName(),
                amendment.getOriginalValue(),
                amendment.getAmendedValue(),
                amendment.getAmendmentReason(),
                amendment.getAmendedBy(),
                amendedByName,
                amendment.getAmendedDate(),
                amendment.getApprovedBy(),
                approvedByName,
                amendment.getApprovedDate(),
                amendment.getStatus());
    }
}
