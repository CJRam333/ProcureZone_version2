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
import com.nslindia.procurezone.masterdata.Section;
import com.nslindia.procurezone.masterdata.dto.SectionResponse;
import com.nslindia.procurezone.masterdata.dto.CreateSectionRequest;
import com.nslindia.procurezone.masterdata.dto.UpdateSectionRequest;
import com.nslindia.procurezone.masterdata.repository.SectionRepository;

/**
 * Service class for Section master data management.
 * Handles business logic, validation, and audit logging for section operations.
 */
@Service
@Transactional
public class SectionService {

    private static final Logger logger = LoggerFactory.getLogger(SectionService.class);

    private final SectionRepository sectionRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public SectionService(SectionRepository sectionRepository, EmployeeRepository employeeRepository,
            AuditService auditService) {
        this.sectionRepository = sectionRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    /**
     * Get all sections with pagination.
     * 
     * @param pageable pagination information
     * @return page of section responses
     */
    @Transactional(readOnly = true)
    public Page<SectionResponse> getAllSections(Pageable pageable) {
        logger.info("Fetching sections with pagination: {}", pageable);
        return sectionRepository.findAll(pageable)
                .map(SectionResponse::from);
    }

    /**
     * Get all active sections with pagination.
     * 
     * @param pageable pagination information
     * @return page of active section responses
     */
    @Transactional(readOnly = true)
    public Page<SectionResponse> getActiveSections(Pageable pageable) {
        logger.info("Fetching active sections with pagination: {}", pageable);
        return sectionRepository.findByStatus(1, pageable)
                .map(SectionResponse::from);
    }

    /**
     * Search sections by code or name.
     * 
     * @param searchTerm the search term
     * @param activeOnly filter for active sections only
     * @param pageable   pagination information
     * @return page of matching section responses
     */
    @Transactional(readOnly = true)
    public Page<SectionResponse> searchSections(String searchTerm, Boolean activeOnly, Pageable pageable) {
        logger.info("Searching sections with term: '{}', activeOnly: {}", searchTerm, activeOnly);

        if (searchTerm == null || searchTerm.isBlank()) {
            return activeOnly != null && activeOnly
                    ? getActiveSections(pageable)
                    : getAllSections(pageable);
        }

        Page<Section> sections = activeOnly != null && activeOnly
                ? sectionRepository.searchByCodeOrNameAndStatus(searchTerm, 1, pageable)
                : sectionRepository.searchByCodeOrName(searchTerm, pageable);

        return sections.map(SectionResponse::from);
    }

    /**
     * Get section by ID.
     * 
     * @param id the section ID
     * @return the section response
     * @throws ResourceNotFoundException if section not found
     */
    @Transactional(readOnly = true)
    public SectionResponse getSectionById(Integer id) {
        logger.info("Fetching section with id: {}", id);
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
        return SectionResponse.from(section);
    }

    /**
     * Create a new section.
     * 
     * @param request  the create request
     * @param username the username of the user creating the section
     * @return the created section response
     * @throws BadRequestException        if validation fails
     * @throws DuplicateResourceException if section code already exists
     */
    public SectionResponse createSection(CreateSectionRequest request, String username) {
        logger.info("Creating new section with code: {} by user: {}", request.code(), username);

        // Validate status
        if (!request.isValidStatus()) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Check for duplicate code
        if (sectionRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Section with code '" + request.code() + "' already exists");
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Create section entity
        Section section = new Section();
        section.setCode(request.code());
        section.setName(request.name());
        section.setStatus(request.status());
        section.setLastModifiedDate(LocalDate.now());
        section.setLastModifiedBy(currentUser.getEmpNumber());

        // Save section
        Section savedSection = sectionRepository.save(section);
        logger.info("Section created successfully with id: {}", savedSection.getId());

        // Audit log
        auditService.logEntityChange("CREATE", "Section", savedSection.getId(),
                currentUser.getEmpNumber(), username, "Created section: " + savedSection.getName());

        return SectionResponse.from(savedSection);
    }

    /**
     * Update an existing section.
     * 
     * @param id       the section ID
     * @param request  the update request
     * @param username the username of the user updating the section
     * @return the updated section response
     * @throws ResourceNotFoundException  if section not found
     * @throws BadRequestException        if validation fails
     * @throws DuplicateResourceException if new code already exists
     */
    public SectionResponse updateSection(Integer id, UpdateSectionRequest request, String username) {
        logger.info("Updating section with id: {} by user: {}", id, username);

        // Find existing section
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));

        // Validate status if provided
        if (!request.isValidStatus()) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Check for duplicate code if code is being changed
        if (request.code() != null && !request.code().equals(section.getCode())) {
            if (sectionRepository.existsByCode(request.code())) {
                throw new DuplicateResourceException("Section with code '" + request.code() + "' already exists");
            }
            section.setCode(request.code());
        }

        // Update fields if provided
        if (request.name() != null) {
            section.setName(request.name());
        }
        if (request.status() != null) {
            section.setStatus(request.status());
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        section.setLastModifiedDate(LocalDate.now());
        section.setLastModifiedBy(currentUser.getEmpNumber());

        // Save section
        Section savedSection = sectionRepository.save(section);
        logger.info("Section updated successfully with id: {}", savedSection.getId());

        // Audit log
        auditService.logEntityChange("UPDATE", "Section", savedSection.getId(),
                currentUser.getEmpNumber(), username, "Updated section: " + savedSection.getName());

        return SectionResponse.from(savedSection);
    }

    /**
     * Delete a section by ID.
     * 
     * @param id       the section ID
     * @param username the username of the user deleting the section
     * @throws ResourceNotFoundException if section not found
     */
    public void deleteSection(Integer id, String username) {
        logger.info("Deleting section with id: {} by user: {}", id, username);

        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        sectionRepository.delete(section);
        logger.info("Section deleted successfully with id: {}", id);

        // Audit log
        auditService.logEntityChange("DELETE", "Section", id,
                currentUser.getEmpNumber(), username, "Deleted section: " + section.getName());
    }
}
