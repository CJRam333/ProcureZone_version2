package com.nslindia.procurezone.grn;

import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.grn.dto.*;
import com.nslindia.procurezone.indent.Indent;
import com.nslindia.procurezone.indent.IndentDetail;
import com.nslindia.procurezone.indent.IndentDetailRepository;
import com.nslindia.procurezone.inventory.InventoryService;
import com.nslindia.procurezone.po.PODetailRepository;
import com.nslindia.procurezone.po.PurchaseOrderDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GRNService {

    private static final String ENTITY_TYPE = "Goods Receipt";
    private static final String NOT_FOUND_MSG = "GRN not found with ID: ";

    private final GoodsReceiptRepository grnRepository;
    private final AuditService auditService;
    private final InventoryService inventoryService;
    private final IndentDetailRepository indentDetailRepository;
    private final PODetailRepository poDetailRepository;

    /**
     * Create new GRN
     */
    public GRNResponse createGRN(CreateGRNRequest request, Integer userId) {
        log.info("Creating GRN for indent details ID: {}", request.indentDetailsId());

        // C.2 CRITICAL FIX: Validate GRN quantity against PO ordered quantity
        validateGRNQuantityAgainstPO(request.indentDetailsId(), request.receivedQuantity());

        // Generate GRN number
        String grnNumber = generateGRNNumber();

        // Calculate amount
        BigDecimal amount = request.rate().multiply(request.receivedQuantity());

        // Create GRN entity
        GoodsReceipt grn = GoodsReceipt.builder()
                .grnNumber(grnNumber)
                .receiptDate(LocalDateTime.now())
                .indentId(request.indentId())
                .indentDetailsId(request.indentDetailsId())
                .receivedQuantity(request.receivedQuantity())
                .rate(request.rate())
                .amount(amount)
                .vendorName(request.vendorName())
                .comments(request.comments())
                .openingQuantity(request.openingQuantity() != null ? request.openingQuantity() : BigDecimal.ZERO)
                .balanceInventory(request.receivedQuantity())
                .requestedQuantity(request.receivedQuantity())
                .createdBy(userId)
                .createdDate(LocalDateTime.now())
                .status(1) // Created
                .lastModifiedBy(userId)
                .lastModifiedDate(LocalDateTime.now())
                .build();

        grn = grnRepository.save(grn);

        auditService.logEntityChange("GRN_CREATED", ENTITY_TYPE, grn.getId(),
                userId, "User " + userId, "GRN created: " + grnNumber + " for vendor: " + request.vendorName());

        log.info("GRN created successfully: {}", grnNumber);
        return mapToResponse(grn);
    }

    /**
     * Get all GRNs with pagination and filtering
     */
    @Transactional(readOnly = true)
    public Page<GRNSummaryResponse> getAllGRNs(Integer status, String search, Pageable pageable) {
        Page<GoodsReceipt> grns;

        if (search != null && !search.isBlank()) {
            grns = grnRepository.searchGRNs(search, pageable);
        } else if (status != null) {
            grns = grnRepository.findByStatus(status, pageable);
        } else {
            grns = grnRepository.findAll(pageable);
        }

        return grns.map(this::mapToSummaryResponse);
    }

    /**
     * Get GRN by ID
     */
    @Transactional(readOnly = true)
    public GRNResponse getGRNById(Integer id) {
        GoodsReceipt grn = grnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MSG + id));
        return mapToResponse(grn);
    }

    /**
     * Get GRN by number
     */
    @Transactional(readOnly = true)
    public GRNResponse getGRNByNumber(String grnNumber) {
        GoodsReceipt grn = grnRepository.findByGrnNumber(grnNumber)
                .orElseThrow(() -> new ResourceNotFoundException("GRN not found with number: " + grnNumber));
        return mapToResponse(grn);
    }

    /**
     * Quality inspection
     */
    public GRNResponse qualityInspection(Integer id, InspectionRequest request, Integer userId) {
        GoodsReceipt grn = grnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MSG + id));

        if (!grn.canBeInspected()) {
            throw new IllegalStateException("GRN cannot be inspected in current status: " + grn.getStatus());
        }

        if (request.qualityApproved() != null && !request.qualityApproved()) {
            // Quality rejected
            grn.setStatus(6); // Rejected
            grn.setCreatedRemarks(request.remarks());
            auditService.logEntityChange("GRN_QUALITY_REJECTED", ENTITY_TYPE, id,
                    userId, "User " + userId, "GRN " + grn.getGrnNumber() + " quality rejected: " + request.remarks());
        } else {
            // Quality passed
            grn.setStatus(2); // Inspected
            grn.setCreatedRemarks(request.remarks());
            auditService.logEntityChange("GRN_INSPECTED", ENTITY_TYPE, id,
                    userId, "User " + userId, "GRN " + grn.getGrnNumber() + " inspected successfully");
        }

        grn.setLastModifiedBy(userId);
        grn.setLastModifiedDate(LocalDateTime.now());
        grn = grnRepository.save(grn);

        log.info("GRN {} inspection completed", grn.getGrnNumber());
        return mapToResponse(grn);
    }

    /**
     * RM (Reporting Manager) Approve GRN
     * This is the first approval level after inspection
     */
    public GRNResponse rmApproveGRN(Integer id, ApprovalRequest request, Integer userId) {
        GoodsReceipt grn = grnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MSG + id));

        if (!grn.canBeRmApproved()) {
            throw new IllegalStateException("GRN cannot be RM approved in current status: " + grn.getStatus() +
                    ". Either it's not inspected yet, or supervisor bypass is enabled.");
        }

        grn.setStatus(3); // RM Approved
        grn.setRmApprovedBy(userId);
        grn.setRmApprovedDate(LocalDateTime.now());
        grn.setRmApprovedRemarks(request.remarks());
        grn.setRmApprovedStatus(1); // RM Approved
        grn.setLastModifiedBy(userId);
        grn.setLastModifiedDate(LocalDateTime.now());

        grn = grnRepository.save(grn);

        auditService.logEntityChange("GRN_RM_APPROVED", ENTITY_TYPE, id,
                userId, "User " + userId, "GRN " + grn.getGrnNumber() + " RM approved");

        log.info("GRN {} RM approved by user {}", grn.getGrnNumber(), userId);
        return mapToResponse(grn);
    }

    /**
     * RM (Reporting Manager) Reject GRN
     */
    public GRNResponse rmRejectGRN(Integer id, RejectionRequest request, Integer userId) {
        GoodsReceipt grn = grnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MSG + id));

        if (!grn.canBeRmApproved()) {
            throw new IllegalStateException("GRN cannot be RM rejected in current status: " + grn.getStatus());
        }

        grn.setStatus(7); // Rejected
        grn.setRmApprovedBy(userId);
        grn.setRmApprovedDate(LocalDateTime.now());
        grn.setRmApprovedRemarks(request.reason());
        grn.setRmApprovedStatus(6); // Rejected
        grn.setComments((grn.getComments() != null ? grn.getComments() : "") + "\nRM REJECTED: " + request.reason());
        grn.setLastModifiedBy(userId);
        grn.setLastModifiedDate(LocalDateTime.now());

        grn = grnRepository.save(grn);

        auditService.logEntityChange("GRN_RM_REJECTED", ENTITY_TYPE, id,
                userId, "User " + userId, "GRN " + grn.getGrnNumber() + " RM rejected: " + request.reason());

        log.info("GRN {} RM rejected by user {}", grn.getGrnNumber(), userId);
        return mapToResponse(grn);
    }

    /**
     * Approve GRN (First approval)
     */
    public GRNResponse approveGRN(Integer id, ApprovalRequest request, Integer userId) {
        GoodsReceipt grn = grnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MSG + id));

        if (!grn.canBeApproved()) {
            throw new IllegalStateException("GRN cannot be approved in current status: " + grn.getStatus() +
                    ". It must be RM approved first, or have supervisor bypass enabled.");
        }

        grn.setStatus(4); // Approved
        grn.setApprovedBy(userId);
        grn.setApprovedDate(LocalDateTime.now());
        grn.setApprovedRemarks(request.remarks());
        grn.setApprovedStatus(1); // Approved
        grn.setLastModifiedBy(userId);
        grn.setLastModifiedDate(LocalDateTime.now());

        grn = grnRepository.save(grn);

        auditService.logEntityChange("GRN_APPROVED", ENTITY_TYPE, id,
                userId, "User " + userId, "GRN " + grn.getGrnNumber() + " approved");

        log.info("GRN {} approved by user {}", grn.getGrnNumber(), userId);
        return mapToResponse(grn);
    }

    /**
     * Final approval (Second level approval)
     * CRITICAL: This method adds received goods to inventory
     */
    public GRNResponse finalApproveGRN(Integer id, ApprovalRequest request, Integer userId) {
        GoodsReceipt grn = grnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MSG + id));

        if (!grn.canBeFinalApproved()) {
            throw new IllegalStateException("GRN cannot be final approved in current status: " + grn.getStatus());
        }

        // Store GRN details before updating
        final Integer indentDetailsId = grn.getIndentDetailsId();
        final String grnNumber = grn.getGrnNumber();
        final Integer grnId = grn.getId();
        final String vendorName = grn.getVendorName();
        final BigDecimal receivedQuantity = grn.getReceivedQuantity();
        final BigDecimal rate = grn.getRate();

        grn.setStatus(5); // Final Approved
        grn.setFinalApprovedBy(userId);
        grn.setFinalApprovedDate(LocalDateTime.now());
        grn.setFinalApprovedRemarks(request.remarks());
        grn.setFinalStatus(1); // Final Approved
        grn.setLastModifiedBy(userId);
        grn.setLastModifiedDate(LocalDateTime.now());

        GoodsReceipt savedGrn = grnRepository.save(grn);

        // 🔥 CRITICAL FIX: Add received goods to inventory
        try {
            IndentDetail indentDetail = indentDetailRepository.findById(indentDetailsId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Indent detail not found with ID: " + indentDetailsId));

            Indent indent = indentDetail.getIndent();

            inventoryService.addStock(
                    indentDetail.getMaterial().getId(),
                    indent.getPlant().getId(),
                    indent.getCompany().getId(),
                    indentDetail.getUnitOfMeasure().getId(),
                    receivedQuantity,
                    rate,
                    "GRN_IN",
                    grnNumber,
                    grnId,
                    "GRN Final Approved - Goods received from " + vendorName);

            log.info("✅ Inventory updated: Added {} units of material {} for GRN {}",
                    receivedQuantity, indentDetail.getMaterial().getId(), grnNumber);

        } catch (Exception e) {
            log.error("❌ Failed to update inventory for GRN {}: {}", grnNumber, e.getMessage(), e);
            throw new IllegalStateException("Failed to update inventory: " + e.getMessage(), e);
        }

        auditService.logEntityChange("GRN_FINAL_APPROVED", ENTITY_TYPE, id,
                userId, "User " + userId, "GRN " + grnNumber + " final approved and inventory updated");

        log.info("GRN {} final approved by user {} and inventory updated", grnNumber, userId);
        return mapToResponse(savedGrn);
    }

    /**
     * Store goods in inventory
     */
    public GRNResponse storeGoods(Integer id, StoreRequest request, Integer userId) {
        GoodsReceipt grn = grnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MSG + id));

        if (!grn.canBeStored()) {
            throw new IllegalStateException("GRN cannot be stored in current status: " + grn.getStatus());
        }

        grn.setStatus(6); // Stored
        grn.setStoredBy(userId);
        grn.setStoredDate(LocalDateTime.now());
        grn.setStoredRemarks(request.remarks());
        grn.setStoredStatus(1); // Stored
        grn.setBalanceQuantityStores(grn.getReceivedQuantity());
        grn.setLastModifiedBy(userId);
        grn.setLastModifiedDate(LocalDateTime.now());

        grn = grnRepository.save(grn);

        auditService.logEntityChange("GRN_STORED", ENTITY_TYPE, id,
                userId, "User " + userId, "GRN " + grn.getGrnNumber() + " stored in inventory");

        log.info("GRN {} stored by user {}", grn.getGrnNumber(), userId);
        return mapToResponse(grn);
    }

    /**
     * Reject GRN
     */
    public GRNResponse rejectGRN(Integer id, RejectionRequest request, Integer userId) {
        GoodsReceipt grn = grnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MSG + id));

        if (!grn.canBeRejected()) {
            throw new IllegalStateException("GRN cannot be rejected in current status: " + grn.getStatus());
        }

        grn.setStatus(7); // Rejected
        grn.setComments((grn.getComments() != null ? grn.getComments() : "") + "\nREJECTED: " + request.reason());
        grn.setLastModifiedBy(userId);
        grn.setLastModifiedDate(LocalDateTime.now());

        grn = grnRepository.save(grn);

        auditService.logEntityChange("GRN_REJECTED", ENTITY_TYPE, id,
                userId, "User " + userId, "GRN " + grn.getGrnNumber() + " rejected: " + request.reason());

        log.info("GRN {} rejected by user {}", grn.getGrnNumber(), userId);
        return mapToResponse(grn);
    }

    /**
     * Get GRNs pending inspection
     */
    @Transactional(readOnly = true)
    public Page<GRNSummaryResponse> getPendingInspection(Pageable pageable) {
        Page<GoodsReceipt> grns = grnRepository.findByStatus(1, pageable); // Created
        return grns.map(this::mapToSummaryResponse);
    }

    /**
     * Get GRNs pending RM approval
     */
    @Transactional(readOnly = true)
    public Page<GRNSummaryResponse> getPendingRmApproval(Pageable pageable) {
        Page<GoodsReceipt> grns = grnRepository.findByStatus(2, pageable); // Inspected
        return grns.map(this::mapToSummaryResponse);
    }

    /**
     * Get GRNs pending approval
     */
    @Transactional(readOnly = true)
    public Page<GRNSummaryResponse> getPendingApproval(Pageable pageable) {
        List<Integer> statuses = List.of(3, 4); // RM Approved, Approved (awaiting final approval)
        Page<GoodsReceipt> grns = grnRepository.findByStatusIn(statuses, pageable);
        return grns.map(this::mapToSummaryResponse);
    }

    /**
     * Get dashboard statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalGRNs", grnRepository.countAll());
        stats.put("pendingInspection", grnRepository.countByStatus(1));
        stats.put("pendingRmApproval", grnRepository.countByStatus(2));
        stats.put("pendingApproval", grnRepository.countByStatus(3));
        stats.put("pendingFinalApproval", grnRepository.countByStatus(4));
        stats.put("finalApproved", grnRepository.countByStatus(5));
        stats.put("stored", grnRepository.countByStatus(6));
        stats.put("rejected", grnRepository.countByStatus(7));

        Double totalValue = grnRepository.getTotalGRNValue();
        stats.put("totalValue", totalValue != null ? totalValue : 0.0);

        Long totalCount = grnRepository.countAll();
        if (totalCount > 0 && totalValue != null) {
            stats.put("averageValue", totalValue / totalCount);
        } else {
            stats.put("averageValue", 0.0);
        }

        return stats;
    }

    /**
     * Generate GRN number in format: GRN/YYYY/NNNNN
     */
    private String generateGRNNumber() {
        LocalDateTime now = LocalDateTime.now();
        String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
        String prefix = "GRN/" + year + "/";

        // Get last GRN number for this year
        String lastGrnNumber = grnRepository.findAll().stream()
                .map(GoodsReceipt::getGrnNumber)
                .filter(num -> num.startsWith(prefix))
                .max(String::compareTo)
                .orElse(prefix + "00000");

        // Extract number part and increment
        String numberPart = lastGrnNumber.substring(prefix.length());
        int nextNumber = Integer.parseInt(numberPart) + 1;

        return prefix + String.format("%05d", nextNumber);
    }

    /**
     * Map entity to full response DTO
     */
    private GRNResponse mapToResponse(GoodsReceipt grn) {
        return GRNResponse.builder()
                .id(grn.getId())
                .grnNumber(grn.getGrnNumber())
                .receiptDate(grn.getReceiptDate())
                .indentId(grn.getIndentId())
                .indentDetailsId(grn.getIndentDetailsId())
                .receivedQuantity(grn.getReceivedQuantity())
                .issuedQuantity(grn.getIssuedQuantity())
                .balanceInventory(grn.getBalanceInventory())
                .openingQuantity(grn.getOpeningQuantity())
                .requestedQuantity(grn.getRequestedQuantity())
                .balanceQuantityStores(grn.getBalanceQuantityStores())
                .rate(grn.getRate())
                .amount(grn.getAmount())
                .vendorName(grn.getVendorName())
                .comments(grn.getComments())
                .status(grn.getStatus())
                .statusName(GRNResponse.getStatusName(grn.getStatus()))
                .createdBy(grn.getCreatedBy())
                .createdDate(grn.getCreatedDate())
                .createdRemarks(grn.getCreatedRemarks())
                .approvedBy(grn.getApprovedBy())
                .approvedDate(grn.getApprovedDate())
                .approvedRemarks(grn.getApprovedRemarks())
                .finalApprovedBy(grn.getFinalApprovedBy())
                .finalApprovedDate(grn.getFinalApprovedDate())
                .finalApprovedRemarks(grn.getFinalApprovedRemarks())
                .storedBy(grn.getStoredBy())
                .storedDate(grn.getStoredDate())
                .storedRemarks(grn.getStoredRemarks())
                .build();
    }

    /**
     * Map entity to summary response DTO
     */
    private GRNSummaryResponse mapToSummaryResponse(GoodsReceipt grn) {
        return GRNSummaryResponse.builder()
                .id(grn.getId())
                .grnNumber(grn.getGrnNumber())
                .receiptDate(grn.getReceiptDate())
                .vendorName(grn.getVendorName())
                .receivedQuantity(grn.getReceivedQuantity())
                .amount(grn.getAmount())
                .status(grn.getStatus())
                .statusName(GRNResponse.getStatusName(grn.getStatus()))
                .balanceInventory(grn.getBalanceInventory())
                .build();
    }

    /**
     * C.2 CRITICAL FIX: Validate GRN received quantity against PO ordered quantity
     * Ensures: cumulative GRN qty <= PO ordered qty for the same indent detail
     * 
     * MIGRATION FIX (Dec 2025): Also validates indent procurement status = 5
     * (Procurement Approved)
     * Legacy behavior: GRN only allowed when procurementStatus = 7 (PO_CREATED)
     */
    private void validateGRNQuantityAgainstPO(Integer indentDetailsId, BigDecimal receivedQuantity) {
        // Find PO details for this indent detail
        List<PurchaseOrderDetail> poDetails = poDetailRepository.findByIndentDetailId(indentDetailsId);

        if (poDetails.isEmpty()) {
            throw new BadRequestException(
                    "Cannot create GRN: No Purchase Order found for indent detail ID: " + indentDetailsId +
                            ". A PO must be created and approved before goods can be received.");
        }

        // MIGRATION FIX: Verify indent has PO created (procurement status = 5 =
        // Procurement Approved)
        PurchaseOrderDetail firstPODetail = poDetails.get(0);
        Integer indentId = firstPODetail.getPurchaseOrder().getIndentId();
        IndentDetail indentDetail = indentDetailRepository.findById(indentDetailsId)
                .orElseThrow(() -> new ResourceNotFoundException("Indent detail not found: " + indentDetailsId));
        Indent indent = indentDetail.getIndent();

        if (indent.getProcurementStatus() == null || indent.getProcurementStatus().getId() != 5) {
            throw new BadRequestException(
                    "Cannot create GRN: Indent procurement status must be 'Procurement Approved' (status=5). " +
                            "Current status: "
                            + (indent.getProcurementStatus() != null ? indent.getProcurementStatus().getName()
                                    : "null"));
        }

        log.info("GRN precondition validated: Indent {} has valid procurement status", indentId);

        // Calculate total PO ordered quantity for this indent detail
        BigDecimal totalPOQuantity = poDetails.stream()
                .map(PurchaseOrderDetail::getQuantity)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total already received quantity (from existing GRNs)
        List<GoodsReceipt> existingGRNs = grnRepository.findByIndentId(
                poDetails.get(0).getPurchaseOrder().getIndentId());

        BigDecimal totalAlreadyReceived = existingGRNs.stream()
                .filter(grn -> grn.getIndentDetailsId().equals(indentDetailsId))
                .filter(grn -> grn.getStatus() != 6) // Exclude rejected GRNs
                .map(GoodsReceipt::getReceivedQuantity)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate remaining receivable quantity
        BigDecimal remainingQuantity = totalPOQuantity.subtract(totalAlreadyReceived);

        if (receivedQuantity.compareTo(remainingQuantity) > 0) {
            throw new BadRequestException(
                    String.format("GRN quantity (%.2f) exceeds remaining PO quantity (%.2f). " +
                            "PO total: %.2f, Already received: %.2f",
                            receivedQuantity, remainingQuantity, totalPOQuantity, totalAlreadyReceived));
        }

        log.info("GRN quantity validation passed: received={}, remaining PO qty={}",
                receivedQuantity, remainingQuantity);
    }
}
