package com.nslindia.procurezone.po;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * B.1 FIX: Repository for PO Amendment entity
 */
@Repository
public interface POAmendmentRepository extends JpaRepository<POAmendment, Integer> {

    /**
     * Find all amendments for a specific PO
     */
    List<POAmendment> findByPoIdOrderByAmendedDateDesc(Integer poId);

    /**
     * Find amendments by PO and version
     */
    List<POAmendment> findByPoIdAndAmendmentVersion(Integer poId, Integer amendmentVersion);

    /**
     * Get the latest amendment version for a PO
     */
    @Query("SELECT COALESCE(MAX(a.amendmentVersion), 0) FROM POAmendment a WHERE a.poId = :poId")
    Integer findMaxAmendmentVersionByPoId(@Param("poId") Integer poId);

    /**
     * Find all amendments with pagination
     */
    Page<POAmendment> findByPoId(Integer poId, Pageable pageable);

    /**
     * Find amendments by status
     */
    List<POAmendment> findByPoIdAndStatus(Integer poId, String status);

    /**
     * Check if PO has any amendments
     */
    boolean existsByPoId(Integer poId);

    /**
     * Count amendments for a PO
     */
    long countByPoId(Integer poId);
}
