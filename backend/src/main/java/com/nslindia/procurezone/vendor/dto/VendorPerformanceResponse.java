package com.nslindia.procurezone.vendor.dto;

/**
 * Response DTO for vendor performance metrics.
 */
public record VendorPerformanceResponse(
        Integer vendorId,
        String vendorCode,
        String vendorName,
        Double overallRating,
        Integer totalOrders,
        Double totalOrderValue,
        Double onTimeDeliveryRate,
        Double qualityRating,
        Integer completedOrders,
        Integer pendingOrders,
        Integer rejectedOrders,
        Double averageOrderValue,
        Integer daysActiveSinceRegistration) {
}
