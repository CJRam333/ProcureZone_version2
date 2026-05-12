package com.nslindia.procurezone.mapping.repository;

import com.nslindia.procurezone.mapping.entity.CompanyLocationMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CompanyLocationMaterial entity.
 * Provides database access methods for managing company-location-material
 * mappings.
 */
@Repository
public interface CompanyLocationMaterialRepository extends JpaRepository<CompanyLocationMaterial, Integer> {

    /**
     * Find all active mappings
     */
    @Query("SELECT clm FROM CompanyLocationMaterial clm WHERE clm.status = 1")
    Page<CompanyLocationMaterial> findAllActive(Pageable pageable);

    /**
     * Find all active mappings (list)
     */
    @Query("SELECT clm FROM CompanyLocationMaterial clm WHERE clm.status = 1")
    List<CompanyLocationMaterial> findAllActiveList();

    /**
     * Find mapping by company, location, and material
     */
    Optional<CompanyLocationMaterial> findByCompanyIdAndLocationIdAndMaterialId(
            Integer companyId, Integer locationId, Integer materialId);

    /**
     * Check if mapping exists
     */
    boolean existsByCompanyIdAndLocationIdAndMaterialId(
            Integer companyId, Integer locationId, Integer materialId);

    /**
     * Find all mappings for a specific company
     */
    Page<CompanyLocationMaterial> findByCompanyIdAndStatus(Integer companyId, Integer status, Pageable pageable);

    /**
     * Find all mappings for a specific location
     */
    Page<CompanyLocationMaterial> findByLocationIdAndStatus(Integer locationId, Integer status, Pageable pageable);

    /**
     * Find all mappings for a specific material
     */
    Page<CompanyLocationMaterial> findByMaterialIdAndStatus(Integer materialId, Integer status, Pageable pageable);

    /**
     * Find all materials at a specific company and location
     */
    @Query("SELECT clm FROM CompanyLocationMaterial clm " +
            "WHERE clm.companyId = :companyId AND clm.locationId = :locationId AND clm.status = 1")
    List<CompanyLocationMaterial> findMaterialsByCompanyAndLocation(
            @Param("companyId") Integer companyId,
            @Param("locationId") Integer locationId);

    /**
     * Find all locations where a material is available for a company
     */
    @Query("SELECT clm FROM CompanyLocationMaterial clm " +
            "WHERE clm.companyId = :companyId AND clm.materialId = :materialId AND clm.status = 1")
    List<CompanyLocationMaterial> findLocationsByCompanyAndMaterial(
            @Param("companyId") Integer companyId,
            @Param("materialId") Integer materialId);

    /**
     * Find mappings that need reordering (stock below reorder level)
     */
    @Query("SELECT clm FROM CompanyLocationMaterial clm " +
            "WHERE clm.status = 1 AND clm.quantity <= clm.reorderLevel AND clm.reorderLevel IS NOT NULL")
    List<CompanyLocationMaterial> findMappingsNeedingReorder();

    /**
     * Find mappings needing reorder at specific location
     */
    @Query("SELECT clm FROM CompanyLocationMaterial clm " +
            "WHERE clm.locationId = :locationId AND clm.status = 1 " +
            "AND clm.quantity <= clm.reorderLevel AND clm.reorderLevel IS NOT NULL")
    List<CompanyLocationMaterial> findMappingsNeedingReorderAtLocation(@Param("locationId") Integer locationId);

    /**
     * Count total active mappings
     */
    @Query("SELECT COUNT(clm) FROM CompanyLocationMaterial clm WHERE clm.status = 1")
    long countActiveMappings();

    /**
     * Count mappings for a specific location
     */
    long countByLocationIdAndStatus(Integer locationId, Integer status);

    /**
     * Count materials available at a company
     */
    @Query("SELECT COUNT(DISTINCT clm.materialId) FROM CompanyLocationMaterial clm " +
            "WHERE clm.companyId = :companyId AND clm.status = 1")
    long countDistinctMaterialsByCompany(@Param("companyId") Integer companyId);
}
