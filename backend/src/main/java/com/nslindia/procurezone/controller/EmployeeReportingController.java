package com.nslindia.procurezone.controller;

import com.nslindia.procurezone.dto.CreateEmployeeReportingRequest;
import com.nslindia.procurezone.dto.EmployeeReportingResponse;
import com.nslindia.procurezone.dto.OrgChartNode;
import com.nslindia.procurezone.dto.UpdateEmployeeReportingRequest;
import com.nslindia.procurezone.service.EmployeeReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employee-reporting")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'USER', 'DEPTHEAD', 'PLANTMANAGER', 'AUDITOR')")
public class EmployeeReportingController {

    private final EmployeeReportingService employeeReportingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createReportingRelationship(
            @Valid @RequestBody CreateEmployeeReportingRequest request) {
        try {
            EmployeeReportingResponse response = employeeReportingService.createReportingRelationship(request);
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Reporting relationship created successfully");
            responseMap.put("data", response);
            return new ResponseEntity<>(responseMap, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllReportingRelationships(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<EmployeeReportingResponse> reportingRelationships = employeeReportingService
                    .getAllReportingRelationships(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", reportingRelationships.getContent());
            response.put("currentPage", reportingRelationships.getNumber());
            response.put("totalItems", reportingRelationships.getTotalElements());
            response.put("totalPages", reportingRelationships.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveReportingRelationships(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Page<EmployeeReportingResponse> reportingRelationships = employeeReportingService
                    .getActiveReportingRelationships(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", reportingRelationships.getContent());
            response.put("currentPage", reportingRelationships.getNumber());
            response.put("totalItems", reportingRelationships.getTotalElements());
            response.put("totalPages", reportingRelationships.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReportingRelationshipById(@PathVariable Integer id) {
        try {
            EmployeeReportingResponse reportingRelationship = employeeReportingService.getReportingRelationshipById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", reportingRelationship);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/employee/{employeeId}/subordinates")
    public ResponseEntity<Map<String, Object>> getSubordinates(@PathVariable Integer employeeId) {
        try {
            List<EmployeeReportingResponse> subordinates = employeeReportingService.getSubordinates(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", subordinates);
            response.put("count", subordinates.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employee/{employeeId}/supervisor")
    public ResponseEntity<Map<String, Object>> getSupervisor(@PathVariable Integer employeeId) {
        try {
            EmployeeReportingResponse supervisor = employeeReportingService.getSupervisor(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", supervisor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/employee/{employeeId}/subordinate-numbers")
    public ResponseEntity<Map<String, Object>> getSubordinateNumbers(@PathVariable Integer employeeId) {
        try {
            List<Integer> subordinateNumbers = employeeReportingService.getSubordinateNumbers(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", subordinateNumbers);
            response.put("count", subordinateNumbers.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employee/{employeeId}/manager-chain")
    public ResponseEntity<Map<String, Object>> getManagerChain(@PathVariable Integer employeeId) {
        try {
            List<Integer> managerChain = employeeReportingService.getManagerChain(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", managerChain);
            response.put("count", managerChain.size());
            response.put("levels", managerChain.size() - 1); // Exclude employee themselves
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employee/{employeeId}/all-subordinates")
    public ResponseEntity<Map<String, Object>> getAllSubordinates(@PathVariable Integer employeeId) {
        try {
            List<Integer> allSubordinates = employeeReportingService.getAllSubordinates(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", allSubordinates);
            response.put("count", allSubordinates.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employee/{employeeId}/org-chart")
    public ResponseEntity<Map<String, Object>> getOrgChart(@PathVariable Integer employeeId) {
        try {
            OrgChartNode orgChart = employeeReportingService.getOrgChart(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orgChart);
            response.put("totalSubordinates", orgChart.getTotalDescendantCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employee/{employeeId}/approval-chain")
    public ResponseEntity<Map<String, Object>> getApprovalChain(@PathVariable Integer employeeId) {
        try {
            List<Integer> approvalChain = employeeReportingService.getApprovalChain(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", approvalChain);
            response.put("count", approvalChain.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employee/{employeeId}/count-subordinates")
    public ResponseEntity<Map<String, Object>> countSubordinates(@PathVariable Integer employeeId) {
        try {
            long count = employeeReportingService.countSubordinates(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employee/{employeeId}/has-supervisor")
    public ResponseEntity<Map<String, Object>> hasSupervisor(@PathVariable Integer employeeId) {
        try {
            boolean hasSupervisor = employeeReportingService.hasSupervisor(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasSupervisor", hasSupervisor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employee/{employeeId}/is-supervisor")
    public ResponseEntity<Map<String, Object>> isSupervisor(@PathVariable Integer employeeId) {
        try {
            boolean isSupervisor = employeeReportingService.isSupervisor(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isSupervisor", isSupervisor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateReportingRelationship(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEmployeeReportingRequest request) {
        try {
            EmployeeReportingResponse updated = employeeReportingService.updateReportingRelationship(id, request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reporting relationship updated successfully");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteReportingRelationship(@PathVariable Integer id) {
        try {
            employeeReportingService.deleteReportingRelationship(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reporting relationship deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
    }
}
