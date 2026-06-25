package com.nslindia.procurezone.masterdata.dto;

import java.math.BigDecimal;

public record MaterialDropdownResponse(
        Integer materialId,
        String materialCode,
        String materialName,
        String materialDescription,
        Integer companyId,
        String companyName,
        Integer plantId,
        String plantName,
        BigDecimal stockQuantity) {

    public String displayLabel() {
        String desc = (materialDescription != null && !materialDescription.isBlank())
                ? " (" + materialDescription + ")" : "";
        BigDecimal qty = stockQuantity != null ? stockQuantity : BigDecimal.ZERO;
        return String.format("[%s] — %s%s | Company: %s | Stock: %s",
                materialCode, materialName, desc, companyName,
                qty.stripTrailingZeros().toPlainString());
    }
}
