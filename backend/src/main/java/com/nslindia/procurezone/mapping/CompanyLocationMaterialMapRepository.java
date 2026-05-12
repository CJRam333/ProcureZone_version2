package com.nslindia.procurezone.mapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Company-Location-Material Mapping
 * 
 * @author NSL India
 * @version 1.0
 */
@Repository
public interface CompanyLocationMaterialMapRepository extends JpaRepository<CompanyLocationMaterialMap, Integer> {

    /** Find all active mappings (paginated) — used by the list endpoint */
    org.springframework.data.domain.Page<CompanyLocationMaterialMap> findByIsActiveTrue(
            org.springframework.data.domain.Pageable pageable);

    /**
     * Find mapping by company, location, and material
     */
    @Query("SELECT clm FROM CompanyLocationMaterialMap clm " +
            "WHERE clm.company.id = :companyId " +
            "AND clm.location.id = :locationId " +
            "AND clm.material.id = :materialId")
    Optional<CompanyLocationMaterialMap> findByCompanyAndLocationAndMaterial(
            @Param("companyId") Integer companyId,
            @Param("locationId") Integer locationId,
            @Param("materialId") Integer materialId);

    /**
     * Find all active mappings for a location
     */
    @Query("SELECT clm FROM CompanyLocationMaterialMap clm " +
            "WHERE clm.location.id = :locationId " +
            "AND clm.isActive = true")
    List<CompanyLocationMaterialMap> findActiveByLocation(@Param("locationId") Integer locationId);

    /**
     * Find all active mappings for a company
     */
    @Query("SELECT clm FROM CompanyLocationMaterialMap clm " +
            "WHERE clm.company.id = :companyId " +
            "AND clm.isActive = true")
    List<CompanyLocationMaterialMap> findActiveByCompany(@Param("companyId") Integer companyId);

    /**
     * Find all materials available at a specific location
     */
    @Query("SELECT clm FROM CompanyLocationMaterialMap clm " +
            "WHERE clm.company.id = :companyId " +
            "AND clm.location.id = :locationId " +
            "AND clm.isActive = true")
    List<CompanyLocationMaterialMap> findMaterialsByCompanyAndLocation(
            @Param("companyId") Integer companyId,
            @Param("locationId") Integer locationId);

    /**
     * Check if material is available at location
     */
    @Query("SELECT CASE WHEN COUNT(clm) > 0 THEN true ELSE false END " +
            "FROM CompanyLocationMaterialMap clm " +
            "WHERE clm.company.id = :companyId " +
            "AND clm.location.id = :locationId " +
            "AND clm.material.id = :materialId " +
            "AND clm.isActive = true")
    boolean isMaterialAvailableAtLocation(
            @Param("companyId") Integer companyId,
            @Param("locationId") Integer locationId,
            @Param("materialId") Integer materialId);

    /**
     * Find materials below reorder level
     */
    @Query("SELECT clm FROM CompanyLocationMaterialMap clm " +
            "WHERE clm.location.id = :locationId " +
            "AND clm.isActive = true " +
            "ORDER BY clm.reorderLevel DESC")
    List<CompanyLocationMaterialMap> findMaterialsForReorder(@Param("locationId") Integer locationId);
}
