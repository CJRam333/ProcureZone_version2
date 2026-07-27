package com.nslindia.procurezone.identity;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing employee reporting hierarchies.
 * Provides methods for determining approval chains and authorization.
 * 
 * Key concepts:
 * - L1 Approval (RM/Section Head): Immediate supervisor in reporting hierarchy
 * - L2 Approval (Dept Head): Department head based on employee's department
 */
@Service
@Transactional(readOnly = true)
public class ReportingHierarchyService {

    private static final Logger logger = LoggerFactory.getLogger(ReportingHierarchyService.class);

    private final EmployeeReportingHierarchyRepository hierarchyRepository;
    private final EmployeeRepository employeeRepository;

    public ReportingHierarchyService(
            EmployeeReportingHierarchyRepository hierarchyRepository,
            EmployeeRepository employeeRepository) {
        this.hierarchyRepository = hierarchyRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get the immediate reporting manager (supervisor) for an employee.
     * This is used for L1 approval routing.
     *
     * @param empNumber the employee number
     * @return Optional containing the supervisor if found
     */
    public Optional<Employee> getReportingManager(Integer empNumber) {
        logger.debug("Finding reporting manager for employee: {}", empNumber);
        return hierarchyRepository.findSupervisorBySubordinateEmployeeNumber(empNumber);
    }

    /**
     * Get all direct reports for a supervisor.
     *
     * @param supervisorEmpNumber the supervisor's employee number
     * @return List of subordinate employees
     */
    public List<Employee> getDirectReports(Integer supervisorEmpNumber) {
        logger.debug("Finding direct reports for supervisor: {}", supervisorEmpNumber);
        return hierarchyRepository.findSubordinatesBySupervisorEmployeeNumber(supervisorEmpNumber);
    }

    /**
     * Check if the approver can approve for the given employee.
     * An approver can approve if:
     * 1. They are the direct supervisor of the employee
     * 2. They are anywhere in the employee's reporting chain
     * 3. They have DEPTHEAD role and the employee is in their department
     *
     * @param employeeEmpNumber the employee who needs approval
     * @param approverEmpNumber the potential approver
     * @return true if the approver can approve for this employee
     */
    public boolean canApproveFor(Integer employeeEmpNumber, Integer approverEmpNumber) {
        logger.debug("Checking if emp {} can approve for emp {}", approverEmpNumber, employeeEmpNumber);

        // Self-approval: an approver acting on THEIR OWN indent (employee == approver) may approve it
        // at L1 only if they are themselves an RM (have direct reports) — i.e. they ARE the approval
        // authority for their own requests (the Supervisor/Dept-Head-is-RM case). A regular employee
        // with no reports still routes to their supervisor. The equality guard means this branch can
        // NEVER authorise approving someone else's indent.
        if (employeeEmpNumber != null && employeeEmpNumber.equals(approverEmpNumber)) {
            boolean isRm = hierarchyRepository.countSubordinates(approverEmpNumber) > 0;
            logger.debug("Self-approval for emp {}: isRm(hasSubordinates)={}", approverEmpNumber, isRm);
            return isRm;
        }

        // Check direct reporting relationship
        if (hierarchyRepository.isSubordinateOf(employeeEmpNumber, approverEmpNumber)) {
            logger.debug("Direct reporting relationship found");
            return true;
        }

        // Check if approver is in the reporting chain
        List<Integer> chain = hierarchyRepository.findReportingChain(employeeEmpNumber);
        if (chain.contains(approverEmpNumber)) {
            logger.debug("Approver {} found in reporting chain of {}", approverEmpNumber, employeeEmpNumber);
            return true;
        }

        logger.debug("No approval authority found for emp {} over emp {}", approverEmpNumber, employeeEmpNumber);
        return false;
    }

    /**
     * Get the full reporting chain for an employee (all supervisors up to the top).
     *
     * @param empNumber the starting employee number
     * @return List of supervisor employee numbers in order (immediate first)
     */
    public List<Integer> getReportingChain(Integer empNumber) {
        logger.debug("Getting full reporting chain for employee: {}", empNumber);
        return hierarchyRepository.findReportingChain(empNumber);
    }

    /**
     * Get the next approver in the chain for a given approval level.
     * 
     * @param empNumber    the employee who needs approval
     * @param currentLevel current approval level (0=none, 1=L1, 2=L2, etc.)
     * @return Optional containing the next approver
     */
    public Optional<Employee> getNextApprover(Integer empNumber, int currentLevel) {
        logger.debug("Finding next approver for emp {} at level {}", empNumber, currentLevel);

        List<Integer> chain = hierarchyRepository.findReportingChain(empNumber);

        if (chain.isEmpty()) {
            logger.warn("No reporting chain found for employee: {}", empNumber);
            return Optional.empty();
        }

        if (currentLevel >= chain.size()) {
            logger.debug("No more approvers in chain for employee: {}", empNumber);
            return Optional.empty();
        }

        Integer nextApproverEmpNumber = chain.get(currentLevel);
        return employeeRepository.findByEmployeeNumber(nextApproverEmpNumber);
    }

    /**
     * Get all employees that a supervisor can approve for.
     * This includes direct reports and potentially indirect reports.
     *
     * @param supervisorEmpNumber the supervisor's employee number
     * @param includeIndirect     whether to include indirect reports
     * @return List of employee numbers that can be approved
     */
    public List<Integer> getApprovableEmployees(Integer supervisorEmpNumber, boolean includeIndirect) {
        logger.debug("Finding approvable employees for supervisor: {}", supervisorEmpNumber);

        List<Employee> directReports = hierarchyRepository
                .findSubordinatesBySupervisorEmployeeNumber(supervisorEmpNumber);

        return directReports.stream()
                .map(Employee::getEmpNumber)
                .toList();
    }

    /**
     * Check if an employee has any direct reports.
     *
     * @param empNumber the employee number to check
     * @return true if the employee has subordinates
     */
    public boolean hasDirectReports(Integer empNumber) {
        return hierarchyRepository.countSubordinates(empNumber) > 0;
    }

    /**
     * Get the count of direct reports for an employee.
     *
     * @param empNumber the employee number
     * @return count of direct reports
     */
    public long getDirectReportCount(Integer empNumber) {
        return hierarchyRepository.countSubordinates(empNumber);
    }
}
