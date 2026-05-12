package com.nslindia.procurezone.indent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for IndentDetail entity.
 * Provides CRUD operations for indent line items.
 */
@Repository
public interface IndentDetailRepository extends JpaRepository<IndentDetail, Integer> {

    /**
     * Find all details for a specific indent
     */
    List<IndentDetail> findByIndentId(Integer indentId);

    /**
     * Find details by material
     */
    List<IndentDetail> findByMaterialId(Integer materialId);

    /**
     * Find details by indent and material
     */
    IndentDetail findByIndentIdAndMaterialId(Integer indentId, Integer materialId);

    /**
     * Count details for a specific indent
     */
    long countByIndentId(Integer indentId);

    /**
     * Find details by status
     */
    List<IndentDetail> findByStatus(Integer status);

    /**
     * Get total quantity for a material across all indents
     */
    @Query("SELECT SUM(id.quantity) FROM IndentDetail id WHERE id.material.id = :materialId AND id.indent.status.id IN :statusIds")
    Double getTotalQuantityByMaterialAndStatuses(@Param("materialId") Integer materialId,
            @Param("statusIds") List<Integer> statusIds);
}
