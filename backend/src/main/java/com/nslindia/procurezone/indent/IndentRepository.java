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
         * single query. Includes workflow columns for displayStatus derivation.
         */
        @Override
        @NonNull
        @EntityGraph(attributePaths = { "company", "department", "plant", "section", "employee", "status",
                        "createdBy", "approvedStatus", "finalStatus", "procurementStatus" })
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
         * Find indents by status only — used when no department filter is applied
         */
        @Query("SELECT i FROM Indent i WHERE i.status.id = :statusId")
        List<Indent> findByStatusIdOnly(@Param("statusId") Integer statusId);

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
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status",
                        "approvedStatus", "finalStatus", "procurementStatus" })
        @Query("SELECT i FROM Indent i WHERE i.plant.id IN :plantIds")
        Page<Indent> findByPlantIdIn(@Param("plantIds") List<Integer> plantIds, Pageable pageable);

        /**
         * A.2 FIX: Find indents by department ID only
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status",
                        "approvedStatus", "finalStatus", "procurementStatus" })
        @Query("SELECT i FROM Indent i WHERE i.department.id = :departmentId")
        Page<Indent> findByDepartmentIdScoped(@Param("departmentId") Integer departmentId, Pageable pageable);

        /**
         * A.2 FIX: Find indents by plant IDs AND department ID (for DEPTHEAD with plant
         * access)
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status",
                        "approvedStatus", "finalStatus", "procurementStatus" })
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
         * Get the indent_no of the most recently created indent (by indent_id DESC).
         * Matches legacy getIndentNo1(): ORDER BY indentId DESC LIMIT 1.
         */
        @Query("SELECT i.indentNumber FROM Indent i ORDER BY i.id DESC")
        List<String> findLatestIndentNumbers(Pageable pageable);

        /**
         * Get the MAX value across all purely numeric indent_no values.
         * Fallback when the most recent record has a non-numeric indent_no
         * (e.g., mis-generated alphanumeric records from a previous deployment).
         */
        @Query(value = "SELECT MAX(CAST(indent_no AS UNSIGNED)) FROM tbl_indent_master WHERE indent_no REGEXP '^[0-9]+$'", nativeQuery = true)
        Long findMaxNumericIndentNumber();

        // -----------------------------------------------------------------------
        // Three-column workflow queue queries (Step 3e)
        // indent_status = 1 → active (soft-delete flag); never 0 (deleted)
        // -----------------------------------------------------------------------

        /**
         * RM Queue: approvedStatus=1 (Pending), active, for specific employees.
         * Replaces findByStatusIdAndEmployeeEmployeeNumberIn for L1 queue.
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status", "approvedBy",
                        "approvedStatus", "finalStatus", "procurementStatus" })
        @Query("SELECT i FROM Indent i WHERE i.approvedStatus.id = 1 AND i.status.id = 1 AND i.employee.employeeNumber IN :employeeNumbers")
        List<Indent> findRmQueueForEmployees(@Param("employeeNumbers") List<Integer> employeeNumbers);

        /**
         * Dept Head Queue: approvedStatus=3 (RM Approved), finalStatus=1 (Pending), active.
         */
        @Query("SELECT i FROM Indent i WHERE i.approvedStatus.id = 3 AND i.finalStatus.id = 1 AND i.status.id = 1 AND i.department.id = :deptId")
        List<Indent> findDeptHeadQueueByDepartment(@Param("deptId") Integer deptId);

        /**
         * Dept Head Queue — no department filter (for SUPERADMIN/ADMIN approvers).
         */
        @Query("SELECT i FROM Indent i WHERE i.approvedStatus.id = 3 AND i.finalStatus.id = 1 AND i.status.id = 1")
        List<Indent> findDeptHeadQueue();

        /**
         * Procurement Queue: approvedStatus=3, finalStatus=4 (Dept. Head Approved),
         * procurementStatus=4, active.
         */
        @Query("SELECT i FROM Indent i WHERE i.approvedStatus.id = 3 AND i.finalStatus.id = 4 AND i.procurementStatus.id = 4 AND i.status.id = 1")
        List<Indent> findProcurementQueue();

        /**
         * Goods Receipt Queue: procurementStatus=7 (PO Released), active.
         */
        @Query("SELECT i FROM Indent i WHERE i.procurementStatus.id = 7 AND i.status.id = 1")
        List<Indent> findGoodsReceiptQueue();

        /**
         * Combined multi-filter query for the list endpoint.
         * All params are optional; null means "no filter on this field".
         * Includes workflow columns for displayStatus derivation.
         * Three workflow-column params (approvedStatusId, finalStatusId, procurementStatusId)
         * enable filtering by display status label from the frontend.
         */
        @EntityGraph(attributePaths = { "company", "department", "plant", "employee", "status",
                        "approvedStatus", "finalStatus", "procurementStatus" })
        @Query("SELECT i FROM Indent i WHERE " +
                        "(:search IS NULL OR LOWER(i.indentNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.comments) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "(:statusId IS NULL OR i.status.id = :statusId) AND " +
                        "(:departmentId IS NULL OR i.department.id = :departmentId) AND " +
                        "(:plantId IS NULL OR i.plant.id = :plantId) AND " +
                        "(:companyId IS NULL OR i.company.id = :companyId) AND " +
                        "(:fromDate IS NULL OR i.indentDate >= :fromDate) AND " +
                        "(:toDate IS NULL OR i.indentDate <= :toDate) AND " +
                        "(:approvedStatusId IS NULL OR i.approvedStatus.id = :approvedStatusId) AND " +
                        "(:finalStatusId IS NULL OR i.finalStatus.id = :finalStatusId) AND " +
                        "(:procurementStatusId IS NULL OR i.procurementStatus.id = :procurementStatusId)")
        Page<Indent> filterIndents(
                        @Param("search") String search,
                        @Param("statusId") Integer statusId,
                        @Param("departmentId") Integer departmentId,
                        @Param("plantId") Integer plantId,
                        @Param("companyId") Integer companyId,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate,
                        @Param("approvedStatusId") Integer approvedStatusId,
                        @Param("finalStatusId") Integer finalStatusId,
                        @Param("procurementStatusId") Integer procurementStatusId,
                        Pageable pageable);
}
