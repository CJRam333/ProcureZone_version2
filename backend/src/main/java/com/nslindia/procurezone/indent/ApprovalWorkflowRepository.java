package com.nslindia.procurezone.indent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ApprovalWorkflow entity.
 * Manages approval workflow history for indents.
 */
@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Integer> {

    /**
     * Get approval history for an indent, ordered by action date
     */
    @Query("SELECT aw FROM ApprovalWorkflow aw " +
            "WHERE aw.indent.id = :indentId " +
            "ORDER BY aw.actionDate ASC")
    List<ApprovalWorkflow> findByIndentIdOrderByActionDateAsc(@Param("indentId") Integer indentId);

    /**
     * Get the latest workflow entry for an indent
     */
    @Query("SELECT aw FROM ApprovalWorkflow aw " +
            "WHERE aw.indent.id = :indentId " +
            "ORDER BY aw.actionDate DESC")
    List<ApprovalWorkflow> findLatestByIndentId(@Param("indentId") Integer indentId);
}
