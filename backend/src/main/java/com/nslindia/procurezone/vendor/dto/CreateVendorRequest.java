package com.nslindia.procurezone.vendor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request DTO for creating a new vendor.
 */
public record CreateVendorRequest(
        @NotBlank(message = "Vendor code is required") @Size(max = 50) String vendorCode,

        @NotBlank(message = "Vendor name is required") @Size(max = 200) String vendorName,

        @Size(max = 50) String vendorType,

        @Size(max = 100) String contactPerson,

        @Size(max = 20) String contactPhone,

        @Email(message = "Invalid email format") @Size(max = 100) String contactEmail,

        @Size(max = 200) String addressLine1,

        @Size(max = 200) String addressLine2,

        @Size(max = 100) String city,

        @Size(max = 100) String state,

        @Size(max = 100) String country,

        @Size(max = 20) String pincode,

        @Size(max = 50) String gstNumber,

        @Size(max = 20) String panNumber,

        @Size(max = 100) String paymentTerms,

        Integer creditPeriodDays,

        String remarks,

        LocalDate registrationDate) {
}
