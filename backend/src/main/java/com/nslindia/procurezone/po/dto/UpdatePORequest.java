package com.nslindia.procurezone.po.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request DTO for updating Purchase Order
 * Only updatable fields are included
 */
public record UpdatePORequest(
        @JsonProperty("deliveryDate") LocalDate deliveryDate,

        @JsonProperty("deliveryAddress") @Size(max = 1000, message = "Delivery address must not exceed 1000 characters") String deliveryAddress,

        @JsonProperty("paymentTerms") @Size(max = 100, message = "Payment terms must not exceed 100 characters") String paymentTerms,

        @JsonProperty("termsConditions") @Size(max = 5000, message = "Terms and conditions must not exceed 5000 characters") String termsConditions,

        @JsonProperty("notes") @Size(max = 5000, message = "Notes must not exceed 5000 characters") String notes,

        @JsonProperty("priority") @Size(max = 20, message = "Priority must not exceed 20 characters") String priority) {
}
