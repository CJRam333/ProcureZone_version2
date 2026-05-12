package com.nslindia.procurezone.service;

import com.nslindia.procurezone.dto.CreateEmployeeReportingRequest;
import com.nslindia.procurezone.dto.EmployeeReportingResponse;
import com.nslindia.procurezone.dto.OrgChartNode;
import com.nslindia.procurezone.dto.UpdateEmployeeReportingRequest;
import com.nslindia.procurezone.entity.EmployeeReporting;
import com.nslindia.procurezone.repository.EmployeeReportingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeReportingService {

    private final EmployeeReportingRepository employeeReportingRepository;
    private static final int MAX_HIERARCHY_DEPTH = 20; // Prevent infinite loops

    @Transactional
    public EmployeeReportingResponse createReportingRelationship(CreateEmployeeReportingRequest request) {
        // Validation: subordinate cannot be same as supervisor
        if (request.getSubordinateEmployeeNumber().equals(request.getSupervisorEmployeeNumber())) {
            throw new RuntimeException("Employee cannot report to themselves");
        }

        // Check for duplicate active relationship
        if (employeeReportingRepository.existsBySubordinateEmployeeNumberAndSupervisorEmployeeNumberAndStatus(
                request.getSubordinateEmployeeNumber(), request.getSupervisorEmployeeNumber(), 1)) {
            throw new RuntimeException("This reporting relationship already exists");
        }

        // Validate no circular reference
        if (wouldCreateCircularReference(request.getSubordinateEmployeeNumber(),
                request.getSupervisorEmployeeNumber())) {
            throw new RuntimeException("This would create a circular reporting structure");
        }

        EmployeeReporting reporting = new EmployeeReporting();
        reporting.setSubordinateEmployeeNumber(request.getSubordinateEmployeeNumber());
        reporting.setSupervisorEmployeeNumber(request.getSupervisorEmployeeNumber());
        reporting.setEffectiveDate(request.getEffectiveDate() != null ? request.getEffectiveDate() : LocalDate.now());
        reporting.setEndDate(LocalDate.of(2099, 12, 31)); // far-future: report_end column is NOT NULL
        reporting.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        reporting.setLastModifiedDate(LocalDate.now());
        reporting.setLastModifiedBy(getCurrentUserId());

        EmployeeReporting saved = employeeReportingRepository.save(reporting);
        return new EmployeeReportingResponse(saved);
    }

    public Page<EmployeeReportingResponse> getAllReportingRelationships(Pageable pageable) {
        return employeeReportingRepository.findAll(pageable)
                .map(EmployeeReportingResponse::new);
    }

    public Page<EmployeeReportingResponse> getActiveReportingRelationships(Pageable pageable) {
        return employeeReportingRepository.findByStatus(1, pageable)
                .map(EmployeeReportingResponse::new);
    }

    public EmployeeReportingResponse getReportingRelationshipById(Integer id) {
        EmployeeReporting reporting = employeeReportingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporting relationship not found with id: " + id));
        return new EmployeeReportingResponse(reporting);
    }

    public List<EmployeeReportingResponse> getSubordinates(Integer supervisorId) {
        return employeeReportingRepository.findActiveSubordinates(supervisorId)
                .stream()
                .map(EmployeeReportingResponse::new)
                .collect(Collectors.toList());
    }

    public EmployeeReportingResponse getSupervisor(Integer subordinateId) {
        EmployeeReporting reporting = employeeReportingRepository.findActiveSupervisor(subordinateId)
                .orElseThrow(() -> new RuntimeException("No active supervisor found for employee: " + subordinateId));
        return new EmployeeReportingResponse(reporting);
    }

    public List<Integer> getSubordinateNumbers(Integer supervisorId) {
        return employeeReportingRepository.findSubordinateNumbers(supervisorId);
    }

    /**
     * Get the complete management chain from employee to top-level manager (CEO)
     * Returns list ordered from employee to highest manager
     */
    public List<Integer> getManagerChain(Integer employeeId) {
        List<Integer> chain = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Integer currentEmployee = employeeId;
        int depth = 0;

        while (currentEmployee != null && depth < MAX_HIERARCHY_DEPTH) {
            // Detect circular reference
            if (visited.contains(currentEmployee)) {
                throw new RuntimeException("Circular reference detected in reporting hierarchy");
            }
            visited.add(currentEmployee);
            chain.add(currentEmployee);

            // Get supervisor
            Optional<Integer> supervisor = employeeReportingRepository.findSupervisorNumber(currentEmployee);
            currentEmployee = supervisor.orElse(null);
            depth++;
        }

        if (depth >= MAX_HIERARCHY_DEPTH) {
            throw new RuntimeException("Maximum hierarchy depth exceeded - possible circular reference");
        }

        return chain;
    }

    /**
     * Get all subordinates recursively (full tree below employee)
     * Returns flattened list of all subordinate IDs
     */
    public List<Integer> getAllSubordinates(Integer supervisorId) {
        List<Integer> allSubordinates = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        collectSubordinatesRecursive(supervisorId, allSubordinates, visited, 0);
        return allSubordinates;
    }

    private void collectSubordinatesRecursive(Integer supervisorId, List<Integer> result,
            Set<Integer> visited, int depth) {
        if (depth >= MAX_HIERARCHY_DEPTH) {
            throw new RuntimeException("Maximum hierarchy depth exceeded");
        }

        if (visited.contains(supervisorId)) {
            throw new RuntimeException("Circular reference detected in organization structure");
        }
        visited.add(supervisorId);

        List<Integer> directSubordinates = employeeReportingRepository.findSubordinateNumbers(supervisorId);
        result.addAll(directSubordinates);

        for (Integer subordinate : directSubordinates) {
            collectSubordinatesRecursive(subordinate, result, visited, depth + 1);
        }
    }

    /**
     * Get organizational chart as a tree structure
     */
    public OrgChartNode getOrgChart(Integer rootEmployeeId) {
        Set<Integer> visited = new HashSet<>();
        return buildOrgChartRecursive(rootEmployeeId, null, 0, visited);
    }

    private OrgChartNode buildOrgChartRecursive(Integer employeeId, Integer supervisorId,
            int level, Set<Integer> visited) {
        if (level >= MAX_HIERARCHY_DEPTH) {
            throw new RuntimeException("Maximum hierarchy depth exceeded");
        }

        if (visited.contains(employeeId)) {
            throw new RuntimeException("Circular reference detected at employee: " + employeeId);
        }
        visited.add(employeeId);

        OrgChartNode node = new OrgChartNode(employeeId, supervisorId, level);
        List<Integer> subordinateIds = employeeReportingRepository.findSubordinateNumbers(employeeId);

        for (Integer subordinateId : subordinateIds) {
            OrgChartNode childNode = buildOrgChartRecursive(subordinateId, employeeId, level + 1, visited);
            node.addSubordinate(childNode);
        }

        return node;
    }

    /**
     * Get approval chain (managers who can approve for this employee)
     * Returns list of manager IDs in order from direct supervisor to top
     */
    public List<Integer> getApprovalChain(Integer employeeId) {
        List<Integer> chain = getManagerChain(employeeId);
        // Remove the employee themselves (first in list)
        if (!chain.isEmpty() && chain.get(0).equals(employeeId)) {
            chain.remove(0);
        }
        return chain;
    }

    @Transactional
    public EmployeeReportingResponse updateReportingRelationship(Integer id, UpdateEmployeeReportingRequest request) {
        EmployeeReporting reporting = employeeReportingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporting relationship not found with id: " + id));

        if (request.getSupervisorEmployeeNumber() != null) {
            // Validate no circular reference with new supervisor
            if (wouldCreateCircularReference(reporting.getSubordinateEmployeeNumber(),
                    request.getSupervisorEmployeeNumber())) {
                throw new RuntimeException("This would create a circular reporting structure");
            }
            reporting.setSupervisorEmployeeNumber(request.getSupervisorEmployeeNumber());
        }

        if (request.getEffectiveDate() != null) {
            reporting.setEffectiveDate(request.getEffectiveDate());
        }

        if (request.getStatus() != null) {
            reporting.setStatus(request.getStatus());
        }

        reporting.setLastModifiedDate(LocalDate.now());
        reporting.setLastModifiedBy(getCurrentUserId());

        EmployeeReporting updated = employeeReportingRepository.save(reporting);
        return new EmployeeReportingResponse(updated);
    }

    @Transactional
    public void deleteReportingRelationship(Integer id) {
        EmployeeReporting reporting = employeeReportingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporting relationship not found with id: " + id));

        reporting.setStatus(0);
        reporting.setLastModifiedDate(LocalDate.now());
        reporting.setLastModifiedBy(getCurrentUserId());

        employeeReportingRepository.save(reporting);
    }

    public boolean hasSupervisor(Integer employeeId) {
        return employeeReportingRepository.hasSupervisor(employeeId);
    }

    public boolean isSupervisor(Integer employeeId) {
        return employeeReportingRepository.isSupervisor(employeeId);
    }

    public long countSubordinates(Integer supervisorId) {
        return employeeReportingRepository.countActiveSubordinates(supervisorId);
    }

    /**
     * Check if assigning subordinateId to report to newSupervisorId would create
     * circular reference
     */
    private boolean wouldCreateCircularReference(Integer subordinateId, Integer newSupervisorId) {
        try {
            // If newSupervisor is anywhere in subordinate's downline, it's circular
            List<Integer> subordinatesDownline = getAllSubordinates(subordinateId);
            return subordinatesDownline.contains(newSupervisorId);
        } catch (Exception e) {
            // If we can't determine, be safe and reject
            return true;
        }
    }

    private Integer getCurrentUserId() {
        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof com.nslindia.procurezone.security.UserPrincipal) {
                    return ((com.nslindia.procurezone.security.UserPrincipal) principal).employeeNumber();
                }
            }
            return 1; // Fallback for system operations
        } catch (Exception e) {
            return 1; // Fallback on error
        }
    }
}
