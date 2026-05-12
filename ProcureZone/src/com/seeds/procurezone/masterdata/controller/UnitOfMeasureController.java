package com.seeds.procurezone.masterdata.controller;

import com.seeds.procurezone.masterdata.dto.CreateUnitOfMeasureRequest;
import com.seeds.procurezone.masterdata.dto.UnitOfMeasureResponse;
import com.seeds.procurezone.masterdata.dto.UpdateUnitOfMeasureRequest;
import com.seeds.procurezone.masterdata.service.UnitOfMeasureService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Unit of Measure management endpoints.
 * 
 * <p>
 * This controller provides comprehensive CRUD operations for units of measure:
 * </p>
 * <ul>
 * <li>GET /api/v1/unit-of-measures - List all units of measure (paginated)</li>
 * <li>GET /api/v1/unit-of-measures/active - List only active units of
 * measure</li>
 * <li>GET /api/v1/unit-of-measures/search - Search units of measure by code or
 * name</li>
 * <li>GET /api/v1/unit-of-measures/{id} - Get specific unit of measure by
 * ID</li>
 * <li>POST /api/v1/unit-of-measures - Create new unit of measure (ADMIN/MANAGER
 * only)</li>
 * <li>PUT /api/v1/unit-of-measures/{id} - Update existing unit of measure
 * (ADMIN/MANAGER only)</li>
 * <li>DELETE /api/v1/unit-of-measures/{id} - Delete unit of measure (ADMIN
 * only)</li>
 * </ul>
 * 
 * <p>
 * <b>Common Units of Measure:</b>
 * </p>
 * <ul>
 * <li>KG - Kilogram</li>
 * <li>LITRE - Litre</li>
 * <li>PCS - Pieces</li>
 * <li>METER - Meter</li>
 * <li>BOX - Box</li>
 * <li>DOZEN - Dozen</li>
 * <li>PACKET - Packet</li>
 * </ul>
 * 
 * <p>
 * <b>Authorization:</b>
 * </p>
 * <ul>
 * <li>GET endpoints: Any authenticated user</li>
 * <li>POST/PUT endpoints: ADMIN or MANAGER role required</li>
 * <li>DELETE endpoint: ADMIN role required</li>
 * </ul>
 * 
 * @author ProcureZone Development Team
 * @version 1.0
 * @since 2025-10-13
 */
@RestController
@RequestMapping("/api/v1/unit-of-measures")
@Validated
public class UnitOfMeasureController {

    private static final Logger logger = LoggerFactory.getLogger(UnitOfMeasureController.class);

    private final UnitOfMeasureService unitOfMeasureService;

    /**
     * Constructor with dependency injection.
     * 
     * @param unitOfMeasureService Service for unit of measure business logic
     */
    public UnitOfMeasureController(UnitOfMeasureService unitOfMeasureService) {
        this.unitOfMeasureService = unitOfMeasureService;
    }

    /**
     * Get all units of measure with pagination and sorting.
     * 
     * <p>
     * <b>Default pagination:</b> 20 units of measure per page, sorted by name
     * ascending.
     * </p>
     * 
     * @param pageable Pagination and sorting parameters (optional)
     * @return ResponseEntity containing paginated list of units of measure
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UnitOfMeasureResponse>> getAllUnitsOfMeasure(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/unit-of-measures - Fetching all units of measure with pagination");

        Page<UnitOfMeasureResponse> unitsOfMeasure = unitOfMeasureService.getAllUnitsOfMeasure(pageable);

        logger.info("Successfully retrieved {} units of measure (page {} of {})",
                unitsOfMeasure.getNumberOfElements(),
                unitsOfMeasure.getNumber() + 1,
                unitsOfMeasure.getTotalPages());

        return ResponseEntity.ok(unitsOfMeasure);
    }

    /**
     * Get only active units of measure with pagination and sorting.
     * 
     * <p>
     * <b>Default pagination:</b> 20 units of measure per page, sorted by name
     * ascending.
     * </p>
     * 
     * @param pageable Pagination and sorting parameters (optional)
     * @return ResponseEntity containing paginated list of active units of measure
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UnitOfMeasureResponse>> getActiveUnitsOfMeasure(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("GET /api/v1/unit-of-measures/active - Fetching active units of measure");

        Page<UnitOfMeasureResponse> unitsOfMeasure = unitOfMeasureService.getActiveUnitsOfMeasure(pageable);

        logger.info("Successfully retrieved {} active units of measure", unitsOfMeasure.getNumberOfElements());

        return ResponseEntity.ok(unitsOfMeasure);
    }

    /**
     * Search units of measure by code or name with optional status filter.
     * Search is case-insensitive and uses partial matching.
     * 
     * <p>
     * <b>Example usage:</b>
     * </p>
     * <ul>
     * <li>/api/v1/unit-of-measures/search?searchTerm=KG - Find units containing
     * "KG"</li>
     * <li>/api/v1/unit-of-measures/search?searchTerm=LITRE&activeOnly=true - Active
     * units only</li>
     * </ul>
     * 
     * @param searchTerm The term to search for in code or name
     * @param activeOnly If true, only returns active units of measure (default:
     *                   false)
     * @param page       Page number (default: 0)
     * @param size       Page size (default: 20)
     * @return ResponseEntity containing paginated search results
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UnitOfMeasureResponse>> searchUnitsOfMeasure(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        logger.info("GET /api/v1/unit-of-measures/search - Searching with term: '{}', activeOnly: {}",
                searchTerm, activeOnly);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<UnitOfMeasureResponse> unitsOfMeasure = unitOfMeasureService.searchUnitsOfMeasure(searchTerm, activeOnly,
                pageable);

        logger.info("Search returned {} units of measure", unitsOfMeasure.getNumberOfElements());

        return ResponseEntity.ok(unitsOfMeasure);
    }

    /**
     * Get a specific unit of measure by its ID.
     * 
     * @param id The ID of the unit of measure
     * @return ResponseEntity containing the unit of measure details
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UnitOfMeasureResponse> getUnitOfMeasureById(@PathVariable Integer id) {
        logger.info("GET /api/v1/unit-of-measures/{} - Fetching unit of measure by ID", id);

        UnitOfMeasureResponse unitOfMeasure = unitOfMeasureService.getUnitOfMeasureById(id);

        logger.info("Successfully retrieved unit of measure: {}", unitOfMeasure.code());

        return ResponseEntity.ok(unitOfMeasure);
    }

    /**
     * Create a new unit of measure.
     * 
     * <p>
     * <b>Required roles:</b> ADMIN or MANAGER
     * </p>
     * 
     * <p>
     * <b>Request body example:</b>
     * </p>
     * 
     * <pre>
     * {
     *   "code": "KG",
     *   "name": "Kilogram",
     *   "status": 1
     * }
     * </pre>
     * 
     * @param request     The create request containing unit of measure details
     * @param userDetails The authenticated user details
     * @return ResponseEntity containing the created unit of measure (HTTP 201)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UnitOfMeasureResponse> createUnitOfMeasure(
            @Valid @RequestBody CreateUnitOfMeasureRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("POST /api/v1/unit-of-measures - Creating new unit of measure: {}", request.code());

        UnitOfMeasureResponse createdUnitOfMeasure = unitOfMeasureService.createUnitOfMeasure(request,
                userDetails.getUsername());

        logger.info("Successfully created unit of measure with ID: {}", createdUnitOfMeasure.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUnitOfMeasure);
    }

    /**
     * Update an existing unit of measure.
     * 
     * <p>
     * <b>Required roles:</b> ADMIN or MANAGER
     * </p>
     * 
     * <p>
     * <b>Request body example:</b>
     * </p>
     * 
     * <pre>
     * {
     *   "code": "KG",
     *   "name": "Kilogram (Updated)",
     *   "status": 1
     * }
     * </pre>
     * 
     * @param id          The ID of the unit of measure to update
     * @param request     The update request containing new unit of measure details
     * @param userDetails The authenticated user details
     * @return ResponseEntity containing the updated unit of measure
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UnitOfMeasureResponse> updateUnitOfMeasure(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUnitOfMeasureRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("PUT /api/v1/unit-of-measures/{} - Updating unit of measure", id);

        UnitOfMeasureResponse updatedUnitOfMeasure = unitOfMeasureService.updateUnitOfMeasure(id, request,
                userDetails.getUsername());

        logger.info("Successfully updated unit of measure ID: {}", id);

        return ResponseEntity.ok(updatedUnitOfMeasure);
    }

    /**
     * Delete a unit of measure (soft delete).
     * 
     * <p>
     * <b>Required roles:</b> ADMIN only
     * </p>
     * 
     * <p>
     * This is a soft delete operation. The unit of measure is not physically
     * removed
     * from the database, but its status is set to 0 (inactive).
     * </p>
     * 
     * @param id          The ID of the unit of measure to delete
     * @param userDetails The authenticated user details
     * @return ResponseEntity with no content (HTTP 204)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUnitOfMeasure(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("DELETE /api/v1/unit-of-measures/{} - Deleting unit of measure", id);

        unitOfMeasureService.deleteUnitOfMeasure(id, userDetails.getUsername());

        logger.info("Successfully deleted unit of measure ID: {}", id);

        return ResponseEntity.noContent().build();
    }
}
