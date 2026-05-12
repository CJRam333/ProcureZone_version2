package com.nslindia.procurezone.repository;

import com.nslindia.procurezone.entity.CompanyLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for CompanyLocation entity operations
 */
@Repository
public interface CompanyLocationRepository extends JpaRepository<CompanyLocation, Integer> {

        /**
         * Find all active company-location mappings
         */
        @Query("SELECT cl FROM CompanyLocation cl WHERE cl.status = 1")
        Page<CompanyLocation> findAllActive(Pageable pageable);

        /**
         * Find mapping by company and location
         */
        Optional<CompanyLocation> findByCompanyIdAndLocationId(Integer companyId, Integer locationId);

        /**
         * Check if mapping exists for company and location
         */
        boolean existsByCompanyIdAndLocationId(Integer companyId, Integer locationId);

        /**
         * Find all locations for a company
         */
        @Query("SELECT cl FROM CompanyLocation cl WHERE cl.companyId = :companyId AND cl.status = 1")
        Page<CompanyLocation> findLocationsByCompany(@Param("companyId") Integer companyId, Pageable pageable);

        /**
         * Find all companies for a location
         */
        @Query("SELECT cl FROM CompanyLocation cl WHERE cl.locationId = :locationId AND cl.status = 1")
        Page<CompanyLocation> findCompaniesByLocation(@Param("locationId") Integer locationId, Pageable pageable);

        /**
         * Count active mappings for a company
         */
        @Query("SELECT COUNT(cl) FROM CompanyLocation cl WHERE cl.companyId = :companyId AND cl.status = 1")
        long countActiveLocationsByCompany(@Param("companyId") Integer companyId);

        /**
         * Count active mappings for a location
         */
        @Query("SELECT COUNT(cl) FROM CompanyLocation cl WHERE cl.locationId = :locationId AND cl.status = 1")
        long countActiveCompaniesByLocation(@Param("locationId") Integer locationId);

        /**
         * Find all mappings by company (non-paged)
         */
        java.util.List<CompanyLocation> findByCompanyId(Integer companyId);

        /**
         * Find all mappings by company
         */
        Page<CompanyLocation> findByCompanyId(Integer companyId, Pageable pageable);

        /**
         * Find all mappings by location
         */
        Page<CompanyLocation> findByLocationId(Integer locationId, Pageable pageable);

        /**
         * Check if location is accessible to company
         */
        @Query("SELECT CASE WHEN COUNT(cl) > 0 THEN true ELSE false END FROM CompanyLocation cl " +
                        "WHERE cl.companyId = :companyId AND cl.locationId = :locationId AND cl.status = 1")
        boolean isLocationAccessibleToCompany(@Param("companyId") Integer companyId,
                        @Param("locationId") Integer locationId);
}
