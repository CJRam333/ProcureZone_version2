package com.nslindia.procurezone.masterdata.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.DuplicateResourceException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.masterdata.Location;
import com.nslindia.procurezone.masterdata.LocationRepository;
import com.nslindia.procurezone.masterdata.dto.CreateLocationRequest;
import com.nslindia.procurezone.masterdata.dto.LocationResponse;
import com.nslindia.procurezone.masterdata.dto.UpdateLocationRequest;

/**
 * Service for managing locations.
 */
@Service
@Transactional
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository locationRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public LocationService(LocationRepository locationRepository,
            EmployeeRepository employeeRepository,
            AuditService auditService) {
        this.locationRepository = locationRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    /**
     * Get all locations with pagination.
     */
    @Transactional(readOnly = true)
    public Page<LocationResponse> getAllLocations(Pageable pageable) {
        logger.debug("Fetching all locations with pagination: {}", pageable);
        return locationRepository.findAll(pageable).map(LocationResponse::from);
    }

    /**
     * Get only active locations with pagination.
     */
    @Transactional(readOnly = true)
    public Page<LocationResponse> getActiveLocations(Pageable pageable) {
        logger.debug("Fetching active locations with pagination: {}", pageable);
        return locationRepository.findByStatus(1, pageable).map(LocationResponse::from);
    }

    /**
     * Search locations by code or name.
     */
    @Transactional(readOnly = true)
    public Page<LocationResponse> searchLocations(String searchTerm, Boolean activeOnly, Pageable pageable) {
        logger.debug("Searching locations with term: {}, activeOnly: {}", searchTerm, activeOnly);

        if (activeOnly != null && activeOnly) {
            return locationRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseAndStatus(
                    searchTerm, searchTerm, 1, pageable).map(LocationResponse::from);
        } else {
            return locationRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                    searchTerm, searchTerm, pageable).map(LocationResponse::from);
        }
    }

    /**
     * Get a location by ID.
     */
    @Transactional(readOnly = true)
    public LocationResponse getLocationById(Integer id) {
        logger.debug("Fetching location with id: {}", id);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
        return LocationResponse.from(location);
    }

    /**
     * Create a new location.
     */
    public LocationResponse createLocation(CreateLocationRequest request, String username) {
        logger.info("Creating new location with code: {} by user: {}", request.code(), username);

        // Validate status
        if (request.status() != 0 && request.status() != 1) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Check for duplicate code
        if (locationRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Location with code '" + request.code() + "' already exists");
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Create new location
        Location location = new Location();
        location.setCode(request.code());
        location.setName(request.name());
        location.setStatus(request.status());
        location.setLastModifiedDate(LocalDate.now());
        location.setLastModifiedUser(currentUser.getEmpNumber());

        // Save location
        Location savedLocation = locationRepository.save(location);
        logger.info("Location created successfully with id: {}", savedLocation.getId());

        // Audit log
        auditService.logEntityChange(
                "CREATE",
                "Location",
                savedLocation.getId(),
                currentUser.getEmpNumber(),
                username,
                String.format("Created location: %s - %s", savedLocation.getCode(), savedLocation.getName()));

        return LocationResponse.from(savedLocation);
    }

    /**
     * Update an existing location.
     */
    public LocationResponse updateLocation(Integer id, UpdateLocationRequest request, String username) {
        logger.info("Updating location with id: {} by user: {}", id, username);

        // Validate status
        if (request.status() != 0 && request.status() != 1) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Find existing location
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Store old values for audit
        String oldDetails = String.format("Code: %s, Name: %s, Status: %d",
                location.getCode(), location.getName(), location.getStatus());

        // Update location (code cannot be changed)
        location.setName(request.name());
        location.setStatus(request.status());
        location.setLastModifiedDate(LocalDate.now());
        location.setLastModifiedUser(currentUser.getEmpNumber());

        // Save updated location
        Location updatedLocation = locationRepository.save(location);
        logger.info("Location updated successfully with id: {}", updatedLocation.getId());

        // Audit log
        String newDetails = String.format("Code: %s, Name: %s, Status: %d",
                updatedLocation.getCode(), updatedLocation.getName(), updatedLocation.getStatus());
        auditService.logEntityChange(
                "UPDATE",
                "Location",
                updatedLocation.getId(),
                currentUser.getEmpNumber(),
                username,
                String.format("Updated location from [%s] to [%s]", oldDetails, newDetails));

        return LocationResponse.from(updatedLocation);
    }

    /**
     * Delete a location by ID.
     */
    public void deleteLocation(Integer id, String username) {
        logger.info("Deleting location with id: {} by user: {}", id, username);

        // Find existing location
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Store values for audit
        String locationDetails = String.format("Code: %s, Name: %s, Status: %d",
                location.getCode(), location.getName(), location.getStatus());

        // Delete location
        locationRepository.delete(location);
        logger.info("Location deleted successfully with id: {}", id);

        // Audit log
        auditService.logEntityChange(
                "DELETE",
                "Location",
                id,
                currentUser.getEmpNumber(),
                username,
                String.format("Deleted location: %s", locationDetails));
    }
}
