package com.nslindia.procurezone.identity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    /* ===== BASIC ===== */

    Optional<Employee> findByEmail(String email);

    /**
     * Authentication login query — case-insensitive email lookup with eager role fetch.
     * Mirrors the join-fetch pattern previously used in UserAccountRepository.findUserForLogin().
     */
    @Query("""
                SELECT DISTINCT e FROM Employee e
                LEFT JOIN FETCH e.employeeRoles er
                LEFT JOIN FETCH er.role r
                WHERE LOWER(e.email) = LOWER(:email)
            """)
    Optional<Employee> findByEmailIgnoreCase(@Param("email") String email);

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByEmployeeNumber(Integer employeeNumber);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    /* ===== STATUS ===== */

    Page<Employee> findByStatus(Integer status, Pageable pageable);

    List<Employee> findByStatus(Integer status);

    Long countByStatus(Integer status);

    /* ===== DEPARTMENT & LOCATION (SAFE MODE) ===== */

    Page<Employee> findByDepartmentId(Integer departmentId, Pageable pageable);

    Page<Employee> findByLocationId(Integer locationId, Pageable pageable);

    List<Employee> findByDepartmentIdAndStatus(Integer departmentId, Integer status);

    List<Employee> findByLocationIdAndStatus(Integer locationId, Integer status);

    /* ===== SEARCH ===== */

    @Query("""
        SELECT e FROM Employee e
        WHERE
            LOWER(e.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(e.designation) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    """)
    Page<Employee> searchEmployees(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("""
        SELECT e FROM Employee e
        WHERE e.status = :status AND (
            LOWER(e.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        )
    """)
    Page<Employee> searchEmployeesByStatus(
            @Param("searchTerm") String searchTerm,
            @Param("status") Integer status,
            Pageable pageable);

    /* ===== STATISTICS (SAFE MODE) ===== */

    @Query("""
        SELECT e.departmentId, COUNT(e)
        FROM Employee e
        WHERE e.status = 1
        GROUP BY e.departmentId
    """)
    List<Object[]> countEmployeesByDepartment();

    @Query("""
        SELECT e.locationId, COUNT(e)
        FROM Employee e
        WHERE e.status = 1
        GROUP BY e.locationId
    """)
    List<Object[]> countEmployeesByLocation();

    @Query("""
        SELECT e.designation, COUNT(e)
        FROM Employee e
        WHERE e.status = 1
        GROUP BY e.designation
    """)
    List<Object[]> countEmployeesByDesignation();

    /* ===== EMPLOYEE NUMBER ===== */

    @Query("""
        SELECT e FROM Employee e
        WHERE e.employeeNumber IN :employeeNumbers
        AND e.status = 1
    """)
    List<Employee> findByEmployeeNumberIn(
            @Param("employeeNumbers") List<Integer> employeeNumbers);

    @Query("""
        SELECT e FROM Employee e
        WHERE e.status = :status
        AND (
            e.employeeNumber NOT IN (
                SELECT u.employee.employeeNumber FROM UserAccount u WHERE u.employee IS NOT NULL
            )
            OR (:currentEmployeeId IS NOT NULL AND e.employeeNumber = :currentEmployeeId)
        )
    """)
    Page<Employee> findAvailableForUser(
            @Param("status") Integer status,
            @Param("currentEmployeeId") Integer currentEmployeeId,
            Pageable pageable);
}
