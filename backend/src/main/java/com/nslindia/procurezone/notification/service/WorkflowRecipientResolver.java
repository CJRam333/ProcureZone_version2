package com.nslindia.procurezone.notification.service;

import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.identity.ReportingHierarchyService;
import com.nslindia.procurezone.repository.EmployeeRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resolves the workflow-email recipients ("route to next actor, CC previous actor").
 *
 * <p>This is a thin REUSE layer over logic that already backs the approval queues — it does NOT
 * introduce a second source of truth. RM/DeptHead come from {@link ReportingHierarchyService}
 * (the same reporting-hierarchy the L1/L2 queues use); Procurement comes from
 * {@code EmployeeRoleMappingRepository.findEmployeeNumbersByRole} (the same role table). Shared by
 * IndentService and IssueNoteService so both route identically.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowRecipientResolver {

    /** role_id of the Procurement role (a.k.a. Stores group) in tbl_emp_roles. */
    public static final int ROLE_PROCUREMENT = 5;

    private final ReportingHierarchyService reportingHierarchyService;
    private final EmployeeRepository employeeRepository;
    private final EmployeeRoleMappingRepository employeeRoleMappingRepository;

    /**
     * Email of an employee's reporting manager (RM / L1 approver) — same lookup as the L1 queue.
     */
    public Optional<String> reportingManagerEmail(Integer empNumber) {
        if (empNumber == null) {
            return Optional.empty();
        }
        return reportingHierarchyService.getReportingManager(empNumber)
                .map(Employee::getEmail)
                .filter(e -> e != null && !e.isBlank());
    }

    /**
     * Email of the Department Head above a given RM — the RM's OWN reporting manager (one level up
     * the same chain). This matches the hierarchy-scoped L2 queue: after an RM (the creator's
     * supervisor) approves, the next actor is that RM's supervisor.
     */
    public Optional<String> deptHeadEmailAboveRm(Integer rmEmpNumber) {
        return reportingManagerEmail(rmEmpNumber);
    }

    /**
     * All active Procurement-role (role_id = 5) employee emails — global, matching the procurement
     * queue's global (non-department) scope. Empty list if none are configured (caller decides).
     */
    public List<String> procurementEmails() {
        List<String> emails = employeeRoleMappingRepository.findEmployeeNumbersByRole(ROLE_PROCUREMENT).stream()
                .map(employeeRepository::findById)
                .flatMap(Optional::stream)
                .map(Employee::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .distinct()
                .collect(Collectors.toList());
        if (emails.isEmpty()) {
            log.warn("No Procurement-role (role_id={}) recipients with a valid email were found", ROLE_PROCUREMENT);
        }
        return emails;
    }
}
