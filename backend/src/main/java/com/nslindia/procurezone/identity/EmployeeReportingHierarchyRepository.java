package com.nslindia.procurezone.identity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for employee reporting hierarchy operations.
 * Provides queries for determining approval chains.
 */
@Repository
public interface EmployeeReportingHierarchyRepository extends JpaRepository<EmployeeReportingHierarchy, Integer> {

    /**
     * Find the reporting manager (supervisor) for a given employee.
     * This is used for L1 (RM) approval routing.
     *
     * @param subordinateEmployeeNumber the employee number of the subordinate
     * @return Optional containing the supervisor employee if found
     */
    @Query("SELECT h.supervisor FROM EmployeeReportingHierarchy h " +
            "WHERE h.subordinate.employeeNumber = :employeeNumber AND h.status = 1")
    Optional<Employee> findSupervisorBySubordinateEmployeeNumber(@Param("employeeNumber") Integer employeeNumber);

    /**
     * Find all direct reports (subordinates) for a given supervisor.
     * Useful for managers to see pending approvals from their team.
     *
     * @param supervisorEmployeeNumber the employee number of the supervisor
     * @return List of subordinate employees
     */
    @Query("SELECT h.subordinate FROM EmployeeReportingHierarchy h " +
            "WHERE h.supervisor.employeeNumber = :employeeNumber AND h.status = 1")
    List<Employee> findSubordinatesBySupervisorEmployeeNumber(@Param("employeeNumber") Integer employeeNumber);

    /**
     * Check if an employee is a subordinate of a given supervisor.
     * Used for authorization checks in approval workflows.
     *
     * @param subordinateEmployeeNumber the employee number to check
     * @param supervisorEmployeeNumber  the supervisor employee number
     * @return true if reporting relationship exists
     */
    @Query("SELECT COUNT(h) > 0 FROM EmployeeReportingHierarchy h " +
            "WHERE h.subordinate.employeeNumber = :subordinateEmployeeNumber " +
            "AND h.supervisor.employeeNumber = :supervisorEmployeeNumber " +
            "AND h.status = 1")
    boolean isSubordinateOf(@Param("subordinateEmployeeNumber") Integer subordinateEmployeeNumber,
            @Param("supervisorEmployeeNumber") Integer supervisorEmployeeNumber);

    /**
     * Find all hierarchy records for a subordinate.
     * An employee might have multiple reporting relationships in matrix
     * organizations.
     *
     * @param subordinateEmployeeNumber the employee number of the subordinate
     * @return List of hierarchy records
     */
    @Query("SELECT h FROM EmployeeReportingHierarchy h " +
            "WHERE h.subordinate.employeeNumber = :employeeNumber AND h.status = 1")
    List<EmployeeReportingHierarchy> findAllBySubordinateEmployeeNumber(
            @Param("employeeNumber") Integer employeeNumber);

    /**
     * Find all hierarchy records for a supervisor.
     *
     * @param supervisorEmployeeNumber the employee number of the supervisor
     * @return List of hierarchy records
     */
    @Query("SELECT h FROM EmployeeReportingHierarchy h " +
            "WHERE h.supervisor.employeeNumber = :employeeNumber AND h.status = 1")
    List<EmployeeReportingHierarchy> findAllBySupervisorEmployeeNumber(@Param("employeeNumber") Integer employeeNumber);

    /**
     * Get the full reporting chain (all levels up) for an employee.
     * This recursive query finds all supervisors up the chain.
     * Note: MySQL 8.0+ supports CTEs for this query.
     *
     * @param empNumber the starting employee number
     * @return List of supervisor employee numbers in order (immediate first)
     */
    @Query(value = """
            WITH RECURSIVE hierarchy AS (
                SELECT report_sub, report_sup, 1 as level
                FROM tbl_map_emp_reporting
                WHERE report_sub = :empNumber AND report_status = 1
                UNION ALL
                SELECT h2.report_sub, h2.report_sup, hierarchy.level + 1
                FROM tbl_map_emp_reporting h2
                INNER JOIN hierarchy ON hierarchy.report_sup = h2.report_sub
                WHERE h2.report_status = 1 AND hierarchy.level < 10
            )
            SELECT DISTINCT report_sup FROM hierarchy ORDER BY level
            """, nativeQuery = true)
    List<Integer> findReportingChain(@Param("empNumber") Integer empNumber);

    /**
     * Get count of subordinates for a supervisor.
     * Useful for dashboard statistics.
     *
     * @param supervisorEmployeeNumber the supervisor employee number
     * @return count of direct reports
     */
    @Query("SELECT COUNT(h) FROM EmployeeReportingHierarchy h " +
            "WHERE h.supervisor.employeeNumber = :employeeNumber AND h.status = 1")
    long countSubordinates(@Param("employeeNumber") Integer employeeNumber);
}
