package com.nslindia.procurezone.indent.dto;

import com.nslindia.procurezone.common.dto.QuantityEditDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for indent detail response.
 *
 * <p>Quantity model (Pass 3): {@code quantity} is the requester's ORIGINAL, {@code rmQuantity} the
 * L1 adjustment, {@code deptQuantity} the L2 adjustment (both null until that stage edits).
 * {@code currentEffectiveQuantity} = deptQuantity ?? rmQuantity ?? quantity. {@code quantityHistory}
 * is the per-edit trail from the audit table (empty when the line was never adjusted).
 */
public record IndentDetailResponse(
        Integer id,
        Integer materialId,
        String materialCode,
        String materialName,
        Integer unitOfMeasureId,
        String unitOfMeasureCode,
        String unitOfMeasureName,
        BigDecimal quantity,
        BigDecimal rmQuantity,
        BigDecimal deptQuantity,
        BigDecimal currentEffectiveQuantity,
        BigDecimal stockAvailable,
        BigDecimal pricing,
        String purpose,
        String vendor,
        Integer status,
        // The specific company id captured for this line at creation (null for legacy rows). Lets the
        // edit form round-trip the selection so it isn't lost on save.
        Integer companyId,
        String companies,
        List<QuantityEditDTO> quantityHistory,
        // Current authoritative stock for the material, aggregated across all companies/plants from
        // tbl_map_company_plant_material.map_quantity_stores (same source as the material dropdown /
        // Inventory). Distinct from stockAvailable, which is the value captured at indent creation.
        BigDecimal currentStock) {
}
