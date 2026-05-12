package com.nslindia.procurezone.vendor;

import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.po.PORepository;
import com.nslindia.procurezone.vendor.dto.CreateVendorRequest;
import com.nslindia.procurezone.vendor.dto.UpdateVendorRequest;
import com.nslindia.procurezone.vendor.dto.VendorPerformanceResponse;
import com.nslindia.procurezone.vendor.dto.VendorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service for managing vendors.
 */
@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final PORepository poRepository;
    private final AuditService auditService;

    public VendorService(VendorRepository vendorRepository, PORepository poRepository, AuditService auditService) {
        this.vendorRepository = vendorRepository;
        this.poRepository = poRepository;
        this.auditService = auditService;
    }

    /**
     * Creates a new vendor.
     */
    @Transactional
    public VendorResponse createVendor(CreateVendorRequest request, Integer createdByEmpId) {
        // Validate vendor code uniqueness
        if (vendorRepository.existsByVendorCode(request.vendorCode())) {
            throw new BadRequestException("Vendor code already exists: " + request.vendorCode());
        }

        // Validate GST number uniqueness if provided
        if (request.gstNumber() != null && !request.gstNumber().isBlank()) {
            if (vendorRepository.existsByGstNumber(request.gstNumber())) {
                throw new BadRequestException("GST number already exists: " + request.gstNumber());
            }
        }

        // Create vendor entity
        Vendor vendor = new Vendor();
        vendor.setVendorCode(request.vendorCode());
        vendor.setVendorName(request.vendorName());
        vendor.setVendorType(request.vendorType());
        vendor.setContactPerson(request.contactPerson());
        vendor.setContactPhone(request.contactPhone());
        vendor.setContactEmail(request.contactEmail());
        vendor.setAddressLine1(request.addressLine1());
        vendor.setAddressLine2(request.addressLine2());
        vendor.setCity(request.city());
        vendor.setState(request.state());
        vendor.setCountry(request.country());
        vendor.setPincode(request.pincode());
        vendor.setGstNumber(request.gstNumber());
        vendor.setPanNumber(request.panNumber());
        vendor.setPaymentTerms(request.paymentTerms());
        vendor.setCreditPeriodDays(request.creditPeriodDays());
        vendor.setRemarks(request.remarks());
        vendor.setRegistrationDate(request.registrationDate() != null ? request.registrationDate() : LocalDate.now());

        // Set defaults
        vendor.setStatus(1); // Active
        vendor.setRating(0.0);
        vendor.setTotalOrders(0);
        vendor.setTotalOrderValue(0.0);
        vendor.setOnTimeDeliveryRate(0.0);
        vendor.setQualityRating(0.0);
        vendor.setCreatedBy(createdByEmpId);
        vendor.setCreatedDate(LocalDateTime.now());

        Vendor savedVendor = vendorRepository.save(vendor);

        // Audit log
        auditService.logEntityChange(
                "CREATE",
                "Vendor",
                savedVendor.getId(),
                createdByEmpId,
                null,
                "Vendor " + savedVendor.getVendorCode() + " created");

        return mapToResponse(savedVendor);
    }

    /**
     * Updates an existing vendor.
     */
    @Transactional
    public VendorResponse updateVendor(Integer vendorId, UpdateVendorRequest request, Integer modifiedByEmpId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + vendorId));

        // Validate GST number uniqueness if changed
        if (request.gstNumber() != null && !request.gstNumber().isBlank()) {
            if (!request.gstNumber().equals(vendor.getGstNumber())
                    && vendorRepository.existsByGstNumber(request.gstNumber())) {
                throw new BadRequestException("GST number already exists: " + request.gstNumber());
            }
        }

        // Update fields (only if provided)
        if (request.vendorName() != null)
            vendor.setVendorName(request.vendorName());
        if (request.vendorType() != null)
            vendor.setVendorType(request.vendorType());
        if (request.contactPerson() != null)
            vendor.setContactPerson(request.contactPerson());
        if (request.contactPhone() != null)
            vendor.setContactPhone(request.contactPhone());
        if (request.contactEmail() != null)
            vendor.setContactEmail(request.contactEmail());
        if (request.addressLine1() != null)
            vendor.setAddressLine1(request.addressLine1());
        if (request.addressLine2() != null)
            vendor.setAddressLine2(request.addressLine2());
        if (request.city() != null)
            vendor.setCity(request.city());
        if (request.state() != null)
            vendor.setState(request.state());
        if (request.country() != null)
            vendor.setCountry(request.country());
        if (request.pincode() != null)
            vendor.setPincode(request.pincode());
        if (request.gstNumber() != null)
            vendor.setGstNumber(request.gstNumber());
        if (request.panNumber() != null)
            vendor.setPanNumber(request.panNumber());
        if (request.paymentTerms() != null)
            vendor.setPaymentTerms(request.paymentTerms());
        if (request.creditPeriodDays() != null)
            vendor.setCreditPeriodDays(request.creditPeriodDays());
        if (request.status() != null)
            vendor.setStatus(request.status());
        if (request.remarks() != null)
            vendor.setRemarks(request.remarks());

        vendor.setLastModifiedBy(modifiedByEmpId);
        vendor.setLastModifiedDate(LocalDateTime.now());

        Vendor updatedVendor = vendorRepository.save(vendor);

        // Audit log
        auditService.logEntityChange(
                "UPDATE",
                "Vendor",
                updatedVendor.getId(),
                modifiedByEmpId,
                null,
                "Vendor " + updatedVendor.getVendorCode() + " updated");

        return mapToResponse(updatedVendor);
    }

    /**
     * Soft deletes a vendor by setting status to Inactive.
     * Validates that vendor has no active purchase orders before deletion.
     */
    @Transactional
    public void deleteVendor(Integer vendorId, Integer deletedByEmpId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + vendorId));

        // Check if vendor has active purchase orders (status 1-5: Draft to Partially
        // Received)
        long activePOCount = poRepository.findByVendorId(vendorId, PageRequest.of(0, 1000)).getContent().stream()
                .filter(po -> po.getPoStatus() >= 1 && po.getPoStatus() <= 5)
                .count();

        if (activePOCount > 0) {
            throw new BadRequestException(
                    "Cannot delete vendor '" + vendor.getVendorName() + "' - has " + activePOCount
                            + " active purchase order(s). \" +\n                \"Please complete or cancel all active POs before deleting this vendor.");
        }

        vendor.setStatus(2);
        vendor.setLastModifiedBy(deletedByEmpId);
        vendor.setLastModifiedDate(LocalDateTime.now());
        vendorRepository.save(vendor);

        // Audit log
        auditService.logEntityChange(
                "DELETE",
                "Vendor",
                vendor.getId(),
                deletedByEmpId,
                null,
                "Vendor " + vendor.getVendorCode() + " deleted (soft delete)");
    }

    /**
     * Gets a vendor by ID.
     */
    @Transactional(readOnly = true)
    public VendorResponse getVendorById(Integer vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + vendorId));
        return mapToResponse(vendor);
    }

    /**
     * Lists all vendors with pagination.
     */
    @Transactional(readOnly = true)
    public Page<VendorResponse> listVendors(Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<Vendor> vendors;
        if (status != null) {
            vendors = vendorRepository.findByStatus(status, pageable);
        } else {
            vendors = vendorRepository.findAll(pageable);
        }

        return vendors.map(this::mapToResponse);
    }

    /**
     * Lists all active vendors.
     */
    @Transactional(readOnly = true)
    public Page<VendorResponse> listActiveVendors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "vendorName"));
        return vendorRepository.findByStatus(1, pageable).map(this::mapToResponse);
    }

    /**
     * Searches vendors by keyword.
     */
    @Transactional(readOnly = true)
    public Page<VendorResponse> searchVendors(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "vendorName"));
        return vendorRepository.searchVendors(keyword, pageable).map(this::mapToResponse);
    }

    /**
     * Updates vendor rating.
     */
    @Transactional
    public VendorResponse updateVendorRating(Integer vendorId, Double rating, Integer modifiedByEmpId) {
        if (rating < 0.0 || rating > 5.0) {
            throw new BadRequestException("Rating must be between 0.0 and 5.0");
        }

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + vendorId));

        vendor.setRating(rating);
        vendor.setLastModifiedBy(modifiedByEmpId);
        vendor.setLastModifiedDate(LocalDateTime.now());
        Vendor updatedVendor = vendorRepository.save(vendor);

        // Audit log
        auditService.logEntityChange(
                "UPDATE",
                "Vendor",
                vendor.getId(),
                modifiedByEmpId,
                null,
                "Vendor " + vendor.getVendorCode() + " rating updated to " + rating);

        return mapToResponse(updatedVendor);
    }

    /**
     * Gets vendor performance metrics.
     */
    @Transactional(readOnly = true)
    public VendorPerformanceResponse getVendorPerformance(Integer vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + vendorId));

        // Calculate days active since registration
        long daysActive = 0;
        if (vendor.getRegistrationDate() != null) {
            daysActive = ChronoUnit.DAYS.between(vendor.getRegistrationDate(), LocalDate.now());
        }

        // Calculate average order value
        double avgOrderValue = 0.0;
        if (vendor.getTotalOrders() != null && vendor.getTotalOrders() > 0) {
            avgOrderValue = vendor.getTotalOrderValue() / vendor.getTotalOrders();
        }

        // Fetch actual order statistics from purchase orders
        Page<com.nslindia.procurezone.po.PurchaseOrder> vendorPOs = poRepository.findByVendorId(vendorId,
                PageRequest.of(0, 1000));
        int completedOrders = (int) vendorPOs.getContent().stream()
                .filter(po -> po.getPoStatus() == 6 || po.getPoStatus() == 8).count(); // Fully Received or Closed
        int pendingOrders = (int) vendorPOs.getContent().stream()
                .filter(po -> po.getPoStatus() >= 1 && po.getPoStatus() <= 5).count(); // Draft to Partially Received
        int rejectedOrders = (int) vendorPOs.getContent().stream().filter(po -> po.getPoStatus() == 7).count(); // Cancelled

        return new VendorPerformanceResponse(
                vendor.getId(),
                vendor.getVendorCode(),
                vendor.getVendorName(),
                vendor.getRating(),
                vendor.getTotalOrders(),
                vendor.getTotalOrderValue(),
                vendor.getOnTimeDeliveryRate(),
                vendor.getQualityRating(),
                completedOrders,
                pendingOrders,
                rejectedOrders,
                avgOrderValue,
                (int) daysActive);
    }

    /**
     * Maps Vendor entity to VendorResponse DTO.
     */
    private VendorResponse mapToResponse(Vendor vendor) {
        String statusName = switch (vendor.getStatus()) {
            case 1 -> "Active";
            case 2 -> "Inactive";
            case 3 -> "Blacklisted";
            default -> "Unknown";
        };

        return new VendorResponse(
                vendor.getId(),
                vendor.getVendorCode(),
                vendor.getVendorName(),
                vendor.getVendorType(),
                vendor.getContactPerson(),
                vendor.getContactPhone(),
                vendor.getContactEmail(),
                vendor.getAddressLine1(),
                vendor.getAddressLine2(),
                vendor.getCity(),
                vendor.getState(),
                vendor.getCountry(),
                vendor.getPincode(),
                vendor.getGstNumber(),
                vendor.getPanNumber(),
                vendor.getPaymentTerms(),
                vendor.getCreditPeriodDays(),
                vendor.getRating(),
                vendor.getTotalOrders(),
                vendor.getTotalOrderValue(),
                vendor.getOnTimeDeliveryRate(),
                vendor.getQualityRating(),
                vendor.getStatus(),
                statusName,
                vendor.getRemarks(),
                vendor.getRegistrationDate(),
                vendor.getLastOrderDate(),
                vendor.getCreatedDate(),
                vendor.getLastModifiedDate());
    }
}
