package com.nslindia.procurezone.sap.repository;

import com.nslindia.procurezone.sap.entity.SapScheduleMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SAP Scheduled Material Master
 * Handles all data access for SAP import materials
 * 
 * @author NSL India
 * @version 1.0
 */
@Repository
public interface SapScheduleMaterialRepository extends JpaRepository<SapScheduleMaterial, Integer> {

    // ==================== QUERY METHODS ====================

    /**
     * Find materials by plant code
     */
    List<SapScheduleMaterial> findByPlantCode(Integer plantCode);

    /**
     * Find materials by company code
     */
    List<SapScheduleMaterial> findByCompanyCode(Integer companyCode);

    /**
     * Find materials by plant and storage location
     */
    List<SapScheduleMaterial> findByPlantCodeAndStorageLocation(Integer plantCode, String storageLocation);

    /**
     * Find material by plant, material code, and batch
     */
    Optional<SapScheduleMaterial> findByPlantCodeAndMaterialCodeAndBatch(
            Integer plantCode, String materialCode, String batch);

    /**
     * Find materials by material code
     */
    List<SapScheduleMaterial> findByMaterialCode(String materialCode);

    /**
     * Find materials by material code pattern (like search)
     */
    @Query("SELECT s FROM SapScheduleMaterial s WHERE s.materialCode LIKE :pattern OR s.materialDesc LIKE :pattern")
    List<SapScheduleMaterial> searchByMaterialCodeOrDesc(@Param("pattern") String pattern);

    /**
     * Find materials by material type
     */
    List<SapScheduleMaterial> findByMaterialType(String materialType);

    /**
     * Find materials by material group
     */
    List<SapScheduleMaterial> findByMaterialGroup(Integer materialGroup);

    /**
     * Find materials with quantity > 0
     */
    @Query("SELECT s FROM SapScheduleMaterial s WHERE s.quantity > 0 AND s.plantCode = :plantCode")
    List<SapScheduleMaterial> findAvailableMaterialsByPlant(@Param("plantCode") Integer plantCode);

    /**
     * Get available quantity for a material at a plant location
     */
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM SapScheduleMaterial s " +
            "WHERE s.plantCode = :plantCode AND s.materialCode = :materialCode AND s.storageLocation = :storageLocation")
    BigDecimal getAvailableQuantity(
            @Param("plantCode") Integer plantCode,
            @Param("materialCode") String materialCode,
            @Param("storageLocation") String storageLocation);

    /**
     * Get total quantity by material across all locations in a plant
     */
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM SapScheduleMaterial s " +
            "WHERE s.plantCode = :plantCode AND s.materialCode = :materialCode")
    BigDecimal getTotalQuantityInPlant(@Param("plantCode") Integer plantCode,
            @Param("materialCode") String materialCode);

    /**
     * Find materials by QC status (for seed materials)
     */
    @Query("SELECT s FROM SapScheduleMaterial s WHERE s.plantCode = :plantCode " +
            "AND (s.germNormal IS NOT NULL OR s.moisture IS NOT NULL)")
    List<SapScheduleMaterial> findMaterialsWithQcData(@Param("plantCode") Integer plantCode);

    // ==================== BATCH OPERATIONS ====================

    /**
     * Truncate all data from SAP material master
     * Used before fresh import
     */
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE pz_schedule_sap_material_master", nativeQuery = true)
    void truncateTable();

    /**
     * Delete materials by plant code
     * For plant-specific reimport
     */
    @Modifying
    @Transactional
    void deleteByPlantCode(Integer plantCode);

    /**
     * Delete materials by company code
     */
    @Modifying
    @Transactional
    void deleteByCompanyCode(Integer companyCode);

    // ==================== COUNT METHODS ====================

    /**
     * Count materials by plant
     */
    long countByPlantCode(Integer plantCode);

    /**
     * Count materials by company
     */
    long countByCompanyCode(Integer companyCode);

    /**
     * Count total records
     */
    @Query("SELECT COUNT(s) FROM SapScheduleMaterial s")
    long countAllRecords();

    // ==================== AGGREGATION QUERIES ====================

    /**
     * Get distinct plant codes
     */
    @Query("SELECT DISTINCT s.plantCode FROM SapScheduleMaterial s WHERE s.plantCode IS NOT NULL ORDER BY s.plantCode")
    List<Integer> findDistinctPlantCodes();

    /**
     * Get distinct company codes
     */
    @Query("SELECT DISTINCT s.companyCode FROM SapScheduleMaterial s WHERE s.companyCode IS NOT NULL ORDER BY s.companyCode")
    List<Integer> findDistinctCompanyCodes();

    /**
     * Get distinct storage locations for a plant
     */
    @Query("SELECT DISTINCT s.storageLocation FROM SapScheduleMaterial s " +
            "WHERE s.plantCode = :plantCode AND s.storageLocation IS NOT NULL ORDER BY s.storageLocation")
    List<String> findStorageLocationsByPlant(@Param("plantCode") Integer plantCode);

    /**
     * Get distinct material groups
     */
    @Query("SELECT DISTINCT s.materialGroup FROM SapScheduleMaterial s WHERE s.materialGroup IS NOT NULL ORDER BY s.materialGroup")
    List<Integer> findDistinctMaterialGroups();
}
