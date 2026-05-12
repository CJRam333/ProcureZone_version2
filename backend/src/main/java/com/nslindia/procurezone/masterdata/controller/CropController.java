package com.nslindia.procurezone.masterdata.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nslindia.procurezone.masterdata.CropType;
import com.nslindia.procurezone.masterdata.repository.CropTypeRepository;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;

/**
 * REST Controller for Crop Type master data.
 * Maps the pz_crop_type table to the /api/v1/crops endpoint.
 * The frontend uses field names (cropCode, cropName, etc.) which are
 * translated from the underlying DB columns (division_code, diivision_name, etc.).
 */
@RestController
@RequestMapping("/api/v1/crops")
public class CropController {

    private static final Logger logger = LoggerFactory.getLogger(CropController.class);

    private final CropTypeRepository cropTypeRepository;

    public CropController(CropTypeRepository cropTypeRepository) {
        this.cropTypeRepository = cropTypeRepository;
    }

    /**
     * List crops with optional search, type filter, and pagination.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Map<String, Object>>> listCrops(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cropType,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("GET /api/v1/crops - search: {}, cropType: {}, page: {}", search, cropType, page);

        Pageable pageable = PageRequest.of(page, size, Sort.by("divisionName").ascending());
        Page<CropType> result;

        if (search != null && !search.isBlank()) {
            if (isActive != null && isActive) {
                result = cropTypeRepository.searchByNameAndStatus(search, 1, pageable);
            } else {
                result = cropTypeRepository.searchByName(search, pageable);
            }
        } else if (isActive != null && isActive) {
            result = cropTypeRepository.findByStatus(1, pageable);
        } else {
            result = cropTypeRepository.findAll(pageable);
        }

        // Map entity fields to the frontend's expected field names
        Page<Map<String, Object>> response = result.map(this::toFrontendDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Get crop by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCropById(@PathVariable Integer id) {
        CropType crop = cropTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with id: " + id));
        return ResponseEntity.ok(toFrontendDto(crop));
    }

    /**
     * Create a new crop.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<Map<String, Object>> createCrop(@RequestBody Map<String, Object> body) {
        logger.info("POST /api/v1/crops - Creating crop: {}", body.get("cropName"));

        CropType crop = new CropType();
        crop.setDivisionName((String) body.getOrDefault("cropName", ""));
        crop.setDivisionCode(body.get("cropCode") != null ? parseIntOrHash(body.get("cropCode")) : null);
        crop.setDivisionId(body.get("divisionId") != null ? (Integer) body.get("divisionId") : null);
        crop.setStatus(Boolean.TRUE.equals(body.get("isActive")) ? 1 : 0);

        CropType saved = cropTypeRepository.save(crop);
        return ResponseEntity.status(HttpStatus.CREATED).body(toFrontendDto(saved));
    }

    /**
     * Update a crop.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'PLANTMANAGER')")
    public ResponseEntity<Map<String, Object>> updateCrop(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {

        logger.info("PUT /api/v1/crops/{} - Updating crop", id);

        CropType crop = cropTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with id: " + id));

        if (body.containsKey("cropName")) {
            crop.setDivisionName((String) body.get("cropName"));
        }
        if (body.containsKey("cropCode")) {
            crop.setDivisionCode(parseIntOrHash(body.get("cropCode")));
        }
        if (body.containsKey("isActive")) {
            crop.setStatus(Boolean.TRUE.equals(body.get("isActive")) ? 1 : 0);
        }

        CropType saved = cropTypeRepository.save(crop);
        return ResponseEntity.ok(toFrontendDto(saved));
    }

    /**
     * Delete a crop.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteCrop(@PathVariable Integer id) {
        logger.info("DELETE /api/v1/crops/{}", id);
        CropType crop = cropTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with id: " + id));
        cropTypeRepository.delete(crop);
        return ResponseEntity.noContent().build();
    }

    /**
     * Map CropType entity to the frontend's expected field names.
     */
    private Map<String, Object> toFrontendDto(CropType entity) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", entity.getId());
        dto.put("cropCode", entity.getDivisionCode() != null ? String.valueOf(entity.getDivisionCode()) : "");
        dto.put("cropName", entity.getDivisionName() != null ? entity.getDivisionName() : "");
        dto.put("cropType", "Crops");
        dto.put("cropGroup", entity.getDivisionId() != null ? "Group " + entity.getDivisionId() : "General");
        dto.put("description", "");
        dto.put("season", "");
        dto.put("isActive", entity.getStatus() != null && entity.getStatus() == 1);
        dto.put("createdAt", "");
        dto.put("updatedAt", "");
        return dto;
    }

    /**
     * Safely parse an integer from a string or number value.
     */
    private Integer parseIntOrHash(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            // Use hashCode for non-numeric crop codes
            return Math.abs(value.toString().hashCode()) % 100000;
        }
    }
}
