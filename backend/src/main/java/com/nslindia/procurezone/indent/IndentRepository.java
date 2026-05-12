package com.nslindia.procurezone.indent;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

/**
 * Repository for Indent entity.
 * Provides CRUD operations and custom queries for indent management.
 * F.1 FIX: Added EntityGraph annotations to prevent N+1 query issues.
 */
@Repository
public interface IndentRepository extends JpaRepository<Indent, Integer> {

        /**
         * Find indent by indent number
         */
        Indent findByIndentNumber(String indentNumber);

        /**
         * F.1 FIX: Override findAll with EntityGraph to fetch related entities in
         * single query
         */
        @Override
        @NonNull
        @EntityGraph(attributePaths = { "company", "department", "plant", "section", "employee", "status",
                        "createdBy" })
        Page<Indent> findAll(@NonNull Pageable pageable);

        /**
         * Find indents by status with EntityGraph
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        Page<Indent> findByStatusId(Integer statusId, Pageable pageable);

        /**
         * Find indents by company
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        Page<Indent> findByCompanyId(Integer companyId, Pageable pageable);

        /**
         * Find indents by department with EntityGraph
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        Page<Indent> findByDepartmentId(Integer deptId, Pageable pageable);

        /**
         * Find indents by employee (creator)
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        Page<Indent> findByEmployeeEmployeeNumber(Integer empNumber, Pageable pageable);

        /**
         * Find indents by created by
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        Page<Indent> findByCreatedByEmployeeNumber(Integer empNumber, Pageable pageable);

        /**
         * Find indents created between dates
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        @Query("SELECT i FROM Indent i WHERE i.indentDate >= :startDate AND i.indentDate <= :endDate")
        Page<Indent> findByIndentDateBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Search indents by number or comments with EntityGraph
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        @Query("SELECT i FROM Indent i WHERE " +
                        "LOWER(i.indentNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(i.comments) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<Indent> searchIndents(@Param("searchTerm") String searchTerm, Pageable pageable);

        /**
         * Find pending indents for approval (submitted but not approved)
         */
        @Query("SELECT i FROM Indent i WHERE i.status.id = 2 AND i.department.id = :deptId")
        List<Indent> findPendingApprovalsByDepartment(@Param("deptId") Integer deptId);

        /**
         * Find indents by year
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        Page<Indent> findByIndentYear(String year, Pageable pageable);

        /**
         * Find indents by status and department
         */
        @Query("SELECT i FROM Indent i WHERE i.status.id = :statusId AND i.department.id = :deptId")
        List<Indent> findByStatusIdAndDepartmentId(@Param("statusId") Integer statusId,
                        @Param("deptId") Integer deptId);

        /**
         * Find indents by status and employee numbers (for L1 approval queue)
         * Returns indents created by employees in the given list
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status", "approvedBy" })
        @Query("SELECT i FROM Indent i WHERE i.status.id = :statusId AND i.employee.employeeNumber IN :employeeNumbers")
        List<Indent> findByStatusIdAndEmployeeEmployeeNumberIn(@Param("statusId") Integer statusId,
                        @Param("employeeNumbers") List<Integer> employeeNumbers);

        /**
         * A.1 FIX: Find indents by plant ID for cross-plant isolation
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        Page<Indent> findByPlantId(Integer plantId, Pageable pageable);

        /**
         * A.1 FIX: Find indents by plant IDs (for users with multiple plant access)
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        @Query("SELECT i FROM Indent i WHERE i.plant.id IN :plantIds")
        Page<Indent> findByPlantIdIn(@Param("plantIds") List<Integer> plantIds, Pageable pageable);

        /**
         * A.2 FIX: Find indents by department ID only
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        @Query("SELECT i FROM Indent i WHERE i.department.id = :departmentId")
        Page<Indent> findByDepartmentIdScoped(@Param("departmentId") Integer departmentId, Pageable pageable);

        /**
         * A.2 FIX: Find indents by plant IDs AND department ID (for DEPTHEAD with plant
         * access)
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status" })
        @Query("SELECT i FROM Indent i WHERE i.plant.id IN :plantIds AND i.department.id = :departmentId")
        Page<Indent> findByPlantIdInAndDepartmentId(@Param("plantIds") List<Integer> plantIds,
                        @Param("departmentId") Integer departmentId, Pageable pageable);

        /**
         * Count indents by status
         */
        long countByStatusId(Integer statusId);

        /**
         * Count indents by employee
         */
        long countByEmployeeEmployeeNumber(Integer empNumber);

        /**
         * Get the maximum indent sequence number for a given year
         */
        @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.indentNumber, LENGTH(i.indentNumber) - 4, 5) AS integer)), 0) FROM Indent i WHERE i.indentYear = :year")
        Integer getMaxSequenceForYear(@Param("year") String year);
}
