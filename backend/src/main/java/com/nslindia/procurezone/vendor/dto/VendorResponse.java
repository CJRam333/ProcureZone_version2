package com.nslindia.procurezone.vendor.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for vendor details.
 */
public record VendorResponse(
        Integer id,
        String vendorCode,
        String vendorName,
        String vendorType,
        String contactPerson,
        String contactPhone,
        String contactEmail,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String pincode,
        String gstNumber,
        String panNumber,
        String paymentTerms,
        Integer creditPeriodDays,
        Double rating,
        Integer totalOrders,
        Double totalOrderValue,
        Double onTimeDeliveryRate,
        Double qualityRating,
        Integer status,
        String statusName,
        String remarks,
        LocalDate registrationDate,
        LocalDate lastOrderDate,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate) {
}
