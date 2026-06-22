package com.nslindia.procurezone.issuenote;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for Issue Note operations
 * 
 * @author NSL India
 * @version 1.0
 */
@Repository
public interface IssueNoteRepository extends JpaRepository<IssueNote, Integer> {

        /**
         * Find issue note by unique issue note number
         */
        Optional<IssueNote> findByIssueNoteNumber(String issueNoteNumber);

        /**
         * Find last issue note ordered by ID (for number generation)
         */
        Optional<IssueNote> findTopByOrderByIdDesc();

        /**
         * Find by status with pagination
         */
        Page<IssueNote> findByStatus(Integer status, Pageable pageable);

        /**
         * Find by company with pagination
         */
        @Query("SELECT i FROM IssueNote i WHERE i.companyId = :companyId")
        Page<IssueNote> findByCompanyId(@Param("companyId") Integer companyId, Pageable pageable);

        /**
         * Find by department with pagination
         */
        @Query("SELECT i FROM IssueNote i WHERE i.departmentId = :deptId")
        Page<IssueNote> findByDepartmentId(@Param("deptId") Integer deptId, Pageable pageable);

        /**
         * Find by plant with pagination
         */
        @Query("SELECT i FROM IssueNote i WHERE i.plantId = :plantId")
        Page<IssueNote> findByPlantId(@Param("plantId") Integer plantId, Pageable pageable);

        /**
         * Find by created by with pagination
         */
        @Query("SELECT i FROM IssueNote i WHERE i.createdBy = :empId")
        Page<IssueNote> findByCreatedBy(@Param("empId") Integer empId, Pageable pageable);

        /**
         * Find pending RM approval issue notes (status = 2)
         */
        @Query("SELECT i FROM IssueNote i WHERE i.status = 2")
        Page<IssueNote> findPendingRmApproval(Pageable pageable);

        /**
         * Find pending manager approval issue notes (status = 3)
         */
        @Query("SELECT i FROM IssueNote i WHERE i.status = 3")
        Page<IssueNote> findPendingApproval(Pageable pageable);

        /**
         * Find pending issue (status = 3 RM Approved — goes directly to stores, no Manager stage)
         */
        @Query("SELECT i FROM IssueNote i WHERE i.status = 3")
        Page<IssueNote> findPendingIssue(Pageable pageable);

        /**
         * Find by date range
         */
        @Query("SELECT i FROM IssueNote i WHERE i.issueDate BETWEEN :startDate AND :endDate")
        Page<IssueNote> findByDateRange(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Count by status
         */
        @Query("SELECT COUNT(i) FROM IssueNote i WHERE i.status = :status")
        Long countByStatus(@Param("status") Integer status);

        /**
         * Get total issued value (sum of all line items where status = 8 Issued)
         */
        @Query("SELECT COALESCE(SUM(d.amount), 0) FROM IssueNoteDetails d WHERE d.issueNote.status = 8")
        BigDecimal getTotalIssuedValue();

        /**
         * Search issue notes by number or issued to
         */
        @Query("SELECT i FROM IssueNote i WHERE " +
                        "LOWER(i.issueNoteNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(i.issuedTo) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<IssueNote> searchIssueNotes(@Param("searchTerm") String searchTerm, Pageable pageable);

        /**
         * Find by company and status
         */
        @Query("SELECT i FROM IssueNote i WHERE i.companyId = :companyId AND i.status = :status")
        Page<IssueNote> findByCompanyIdAndStatus(
                        @Param("companyId") Integer companyId,
                        @Param("status") Integer status,
                        Pageable pageable);

        /**
         * Find by department and status
         */
        @Query("SELECT i FROM IssueNote i WHERE i.departmentId = :deptId AND i.status = :status")
        Page<IssueNote> findByDepartmentIdAndStatus(
                        @Param("deptId") Integer deptId,
                        @Param("status") Integer status,
                        Pageable pageable);

        /**
         * Count issue notes by date range
         */
        @Query("SELECT COUNT(i) FROM IssueNote i WHERE i.issueDate BETWEEN :startDate AND :endDate")
        Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
