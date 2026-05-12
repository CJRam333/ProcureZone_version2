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
import com.nslindia.procurezone.masterdata.Department;
import com.nslindia.procurezone.masterdata.dto.DepartmentResponse;
import com.nslindia.procurezone.masterdata.dto.CreateDepartmentRequest;
import com.nslindia.procurezone.masterdata.dto.UpdateDepartmentRequest;
import com.nslindia.procurezone.masterdata.repository.DepartmentRepository;

/**
 * Service class for Department master data management.
 * Handles business logic, validation, and audit logging for department
 * operations.
 */
@Service
@Transactional
public class DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository,
            AuditService auditService) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }

    /**
     * Get all departments with pagination.
     * 
     * @param pageable pagination information
     * @return page of department responses
     */
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getAllDepartments(Pageable pageable) {
        logger.info("Fetching departments with pagination: {}", pageable);
        return departmentRepository.findAll(pageable)
                .map(DepartmentResponse::from);
    }

    /**
     * Get all active departments with pagination.
     * 
     * @param pageable pagination information
     * @return page of active department responses
     */
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getActiveDepartments(Pageable pageable) {
        logger.info("Fetching active departments with pagination: {}", pageable);
        return departmentRepository.findByStatus(1, pageable)
                .map(DepartmentResponse::from);
    }

    /**
     * Search departments by code or name.
     * 
     * @param searchTerm the search term
     * @param activeOnly filter for active departments only
     * @param pageable   pagination information
     * @return page of matching department responses
     */
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> searchDepartments(String searchTerm, Boolean activeOnly, Pageable pageable) {
        logger.info("Searching departments with term: '{}', activeOnly: {}", searchTerm, activeOnly);

        if (searchTerm == null || searchTerm.isBlank()) {
            return activeOnly != null && activeOnly
                    ? getActiveDepartments(pageable)
                    : getAllDepartments(pageable);
        }

        Page<Department> departments = activeOnly != null && activeOnly
                ? departmentRepository.searchByCodeOrNameAndStatus(searchTerm, 1, pageable)
                : departmentRepository.searchByCodeOrName(searchTerm, pageable);

        return departments.map(DepartmentResponse::from);
    }

    /**
     * Get department by ID.
     * 
     * @param id the department ID
     * @return the department response
     * @throws ResourceNotFoundException if department not found
     */
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Integer id) {
        logger.info("Fetching department with id: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return DepartmentResponse.from(department);
    }

    /**
     * Create a new department.
     * 
     * @param request  the create request
     * @param username the username of the user creating the department
     * @return the created department response
     * @throws BadRequestException        if validation fails
     * @throws DuplicateResourceException if department code already exists
     */
    public DepartmentResponse createDepartment(CreateDepartmentRequest request, String username) {
        logger.info("Creating new department with code: {} by user: {}", request.code(), username);

        // Validate status
        if (!request.isValidStatus()) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Check for duplicate code
        if (departmentRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Department with code '" + request.code() + "' already exists");
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Create department entity
        Department department = new Department();
        department.setCode(request.code());
        department.setName(request.name());
        department.setStatus(request.status());
        department.setLastModifiedDate(LocalDate.now());
        department.setLastModifiedBy(currentUser.getEmpNumber());

        // Save department
        Department savedDepartment = departmentRepository.save(department);
        logger.info("Department created successfully with id: {}", savedDepartment.getId());

        // Audit log
        auditService.logEntityChange(
                "CREATE",
                "Department",
                savedDepartment.getId(),
                currentUser.getEmpNumber(),
                username,
                String.format("Created department: %s - %s", savedDepartment.getCode(), savedDepartment.getName()));

        return DepartmentResponse.from(savedDepartment);
    }

    /**
     * Update an existing department.
     * 
     * @param id       the department ID
     * @param request  the update request
     * @param username the username of the user updating the department
     * @return the updated department response
     * @throws ResourceNotFoundException  if department not found
     * @throws BadRequestException        if validation fails
     * @throws DuplicateResourceException if department code already exists for
     *                                    different department
     */
    public DepartmentResponse updateDepartment(Integer id, UpdateDepartmentRequest request, String username) {
        logger.info("Updating department with id: {} by user: {}", id, username);

        // Validate status
        if (!request.isValidStatus()) {
            throw new BadRequestException("Invalid status value. Must be 0 (inactive) or 1 (active)");
        }

        // Find existing department
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Check for duplicate code (excluding current department)
        if (departmentRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new DuplicateResourceException("Department with code '" + request.code() + "' already exists");
        }

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Store old values for audit
        String oldDetails = String.format("Code: %s, Name: %s, Status: %d",
                department.getCode(), department.getName(), department.getStatus());

        // Update department
        department.setCode(request.code());
        department.setName(request.name());
        department.setStatus(request.status());
        department.setLastModifiedDate(LocalDate.now());
        department.setLastModifiedBy(currentUser.getEmpNumber());

        // Save updated department
        Department updatedDepartment = departmentRepository.save(department);
        logger.info("Department updated successfully with id: {}", updatedDepartment.getId());

        // Audit log
        String newDetails = String.format("Code: %s, Name: %s, Status: %d",
                updatedDepartment.getCode(), updatedDepartment.getName(), updatedDepartment.getStatus());
        auditService.logEntityChange(
                "UPDATE",
                "Department",
                updatedDepartment.getId(),
                currentUser.getEmpNumber(),
                username,
                String.format("Updated department from [%s] to [%s]", oldDetails, newDetails));

        return DepartmentResponse.from(updatedDepartment);
    }

    /**
     * Delete a department by ID.
     * Note: This is a hard delete. Consider implementing soft delete if needed.
     * 
     * @param id       the department ID
     * @param username the username of the user deleting the department
     * @throws ResourceNotFoundException if department not found
     */
    public void deleteDepartment(Integer id, String username) {
        logger.info("Deleting department with id: {} by user: {}", id, username);

        // Find existing department
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Get current user
        Employee currentUser = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Store values for audit
        String departmentDetails = String.format("Code: %s, Name: %s, Status: %d",
                department.getCode(), department.getName(), department.getStatus());

        // Delete department
        departmentRepository.delete(department);
        logger.info("Department deleted successfully with id: {}", id);

        // Audit log
        auditService.logEntityChange(
                "DELETE",
                "Department",
                id,
                currentUser.getEmpNumber(),
                username,
                String.format("Deleted department: %s", departmentDetails));
    }
}
