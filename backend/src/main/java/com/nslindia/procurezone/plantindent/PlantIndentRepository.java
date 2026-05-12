package com.nslindia.procurezone.plantindent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Plant Indent operations with 8-status workflow support.
 * Maps to pz_tbl_indent_masterb table.
 * 
 * @author NSL India
 * @version 2.0
 */
@Repository
public interface PlantIndentRepository extends JpaRepository<PlantIndent, Integer> {

    /**
     * Find by indent number
     */
    Optional<PlantIndent> findByIndentNumber(String indentNumber);

    /**
     * Find by plant with pagination
     */
    Page<PlantIndent> findByPlantId(Integer plantId, Pageable pageable);

    /**
     * Find by employee ID
     */
    Page<PlantIndent> findByEmployeeId(Integer employeeId, Pageable pageable);

    /**
     * Find by crop type ID
     */
    Page<PlantIndent> findByCropTypeId(Integer cropTypeId, Pageable pageable);

    /**
     * Find by batch number
     */
    List<PlantIndent> findByBatchNumber(String batchNumber);

    /**
     * Search plant indents by indent number or remarks
     */
    @Query("SELECT pi FROM PlantIndent pi WHERE " +
            "LOWER(pi.indentNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(pi.remarks) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(pi.cropName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<PlantIndent> search(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find by plant and batch number
     */
    List<PlantIndent> findByPlantIdAndBatchNumber(Integer plantId, String batchNumber);

    /**
     * Count by plant
     */
    long countByPlantId(Integer plantId);

    /**
     * Get max batch number for a plant (returns count for numbering)
     */
    @Query("SELECT COUNT(pi) FROM PlantIndent pi WHERE pi.plantId = :plantId")
    Integer getMaxBatchNumberForPlant(@Param("plantId") Integer plantId);

    // ========== 8-STATUS WORKFLOW QUERIES ==========

    /**
     * Find by status with pagination
     */
    Page<PlantIndent> findByStatus(Integer status, Pageable pageable);

    /**
     * Find by plant and status
     */
    Page<PlantIndent> findByPlantIdAndStatus(Integer plantId, Integer status, Pageable pageable);

    /**
     * Find by plant and multiple statuses
     */
    Page<PlantIndent> findByPlantIdAndStatusIn(Integer plantId, List<Integer> statuses, Pageable pageable);

    /**
     * DEO/QM Queue: Status = 1 (Pending DEO Review)
     */
    @Query("SELECT pi FROM PlantIndent pi WHERE pi.status = 1 AND pi.plantId = :plantId")
    Page<PlantIndent> findDeoQueueByPlant(@Param("plantId") Integer plantId, Pageable pageable);

    /**
     * Manager Queue: Status = 2 (Pending Manager Approval)
     */
    @Query("SELECT pi FROM PlantIndent pi WHERE pi.status = 2 AND pi.plantId = :plantId")
    Page<PlantIndent> findManagerQueueByPlant(@Param("plantId") Integer plantId, Pageable pageable);

    /**
     * Processing Queue: Status = 3, 6 (Approved or Processing)
     */
    @Query("SELECT pi FROM PlantIndent pi WHERE pi.status IN (3, 6) AND pi.plantId = :plantId")
    Page<PlantIndent> findProcessingQueueByPlant(@Param("plantId") Integer plantId, Pageable pageable);

    /**
     * Rejected Items: Status = 4, 5
     */
    @Query("SELECT pi FROM PlantIndent pi WHERE pi.status IN (4, 5) AND pi.plantId = :plantId")
    Page<PlantIndent> findRejectedByPlant(@Param("plantId") Integer plantId, Pageable pageable);

    /**
     * My Drafts: Status = 0 for specific employee
     */
    Page<PlantIndent> findByEmployeeIdAndStatus(Integer employeeId, Integer status, Pageable pageable);

    /**
     * Count by status for dashboard
     */
    long countByStatus(Integer status);

    /**
     * Count by plant and status
     */
    long countByPlantIdAndStatus(Integer plantId, Integer status);

    /**
     * Dashboard counts for a plant
     */
    @Query("SELECT pi.status, COUNT(pi) FROM PlantIndent pi WHERE pi.plantId = :plantId GROUP BY pi.status")
    List<Object[]> getStatusCountsByPlant(@Param("plantId") Integer plantId);
}
