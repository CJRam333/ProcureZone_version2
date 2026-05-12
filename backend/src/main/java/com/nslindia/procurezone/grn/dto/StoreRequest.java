package com.nslindia.procurezone.grn.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreRequest(
        @NotBlank(message = "Store remarks are required") String remarks) {
}
