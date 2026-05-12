package com.nslindia.procurezone.issuenote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Issue Note Details operations
 * 
 * @author NSL India
 * @version 1.0
 */
@Repository
public interface IssueNoteDetailsRepository extends JpaRepository<IssueNoteDetails, Integer> {

    /**
     * Find all details for a given issue note
     */
    List<IssueNoteDetails> findByIssueNoteId(Integer issueNoteId);

    /**
     * Find details by material
     */
    @Query("SELECT d FROM IssueNoteDetails d WHERE d.materialId = :materialId")
    List<IssueNoteDetails> findByMaterialId(@Param("materialId") Integer materialId);

    /**
     * Get total issued quantity for a material (where status = 6 - Issued)
     */
    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM IssueNoteDetails d " +
            "WHERE d.materialId = :materialId AND d.issueNote.status = 6")
    BigDecimal getTotalIssuedQuantityByMaterial(@Param("materialId") Integer materialId);

    /**
     * Get total issued amount for a material
     */
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM IssueNoteDetails d " +
            "WHERE d.materialId = :materialId AND d.issueNote.status = 6")
    BigDecimal getTotalIssuedAmountByMaterial(@Param("materialId") Integer materialId);

    /**
     * Find details by issue note and material
     */
    @Query("SELECT d FROM IssueNoteDetails d WHERE d.issueNote.id = :issueNoteId AND d.materialId = :materialId")
    List<IssueNoteDetails> findByIssueNoteIdAndMaterialId(
            @Param("issueNoteId") Integer issueNoteId,
            @Param("materialId") Integer materialId);

    /**
     * Delete all details for an issue note
     */
    void deleteByIssueNoteId(Integer issueNoteId);
}
