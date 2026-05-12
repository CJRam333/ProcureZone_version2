package com.nslindia.procurezone.mapping.repository;

import com.nslindia.procurezone.mapping.entity.CompanyPlantMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CompanyPlantMaterial entity
 * Provides data access methods for plant-material mappings
 * 
 * Purpose: Manage material inventory at manufacturing plants
 * Use Case: Production planning, plant inventory management
 */
@Repository
public interface CompanyPlantMaterialRepository extends JpaRepository<CompanyPlantMaterial, Integer> {

        /**
         * Find all active mappings with pagination
         * 
         * @param pageable Pagination parameters
         * @return Page of active plant-material mappings
         */
        @Query("SELECT cpm FROM CompanyPlantMaterial cpm WHERE cpm.status = 1")
        Page<CompanyPlantMaterial> findAllActive(Pageable pageable);

        /**
         * Find all active mappings (no pagination)
         * Use with caution for large datasets
         * 
         * @return List of all active mappings
         */
        @Query("SELECT cpm FROM CompanyPlantMaterial cpm WHERE cpm.status = 1")
        List<CompanyPlantMaterial> findAllActiveList();

        /**
         * Find specific mapping by company, plant, and material
         * This combination should be unique (composite business key)
         * 
         * @param companyId  Company identifier
         * @param plantId    Plant identifier
         * @param materialId Material identifier
         * @return Optional containing mapping if found
         */
        Optional<CompanyPlantMaterial> findByCompanyIdAndPlantIdAndMaterialId(
                        Integer companyId,
                        Integer plantId,
                        Integer materialId);

        /**
         * Check if mapping exists (more efficient than loading entity)
         * Use Case: Duplicate prevention during creation
         * 
         * @param companyId  Company identifier
         * @param plantId    Plant identifier
         * @param materialId Material identifier
         * @return true if mapping exists
         */
        boolean existsByCompanyIdAndPlantIdAndMaterialId(
                        Integer companyId,
                        Integer plantId,
                        Integer materialId);

        /**
         * Find all mappings for a company with specific status
         * Use Case: Company-wide plant material catalog
         * 
         * @param companyId Company identifier
         * @param status    Status filter (1=Active, 0=Inactive)
         * @param pageable  Pagination parameters
         * @return Page of mappings for company
         */
        Page<CompanyPlantMaterial> findByCompanyIdAndStatus(
                        Integer companyId,
                        Integer status,
                        Pageable pageable);

        /**
         * Find all materials at a specific plant
         * Use Case: Plant inventory view, production planning
         * 
         * @param plantId  Plant identifier
         * @param status   Status filter
         * @param pageable Pagination parameters
         * @return Page of materials at plant
         */
        Page<CompanyPlantMaterial> findByPlantIdAndStatus(
                        Integer plantId,
                        Integer status,
                        Pageable pageable);

        /**
         * Find all plants where a material is stored
         * Use Case: Material distribution across plants, inter-plant transfers
         * 
         * @param materialId Material identifier
         * @param status     Status filter
         * @param pageable   Pagination parameters
         * @return Page of plants with this material
         */
        Page<CompanyPlantMaterial> findByMaterialIdAndStatus(
                        Integer materialId,
                        Integer status,
                        Pageable pageable);

        /**
         * Find all materials available at a specific plant for a company
         * Use Case: Production order creation, material requisition from plant
         * 
         * @param companyId Company identifier
         * @param plantId   Plant identifier
         * @return List of active material mappings at plant
         */
        @Query("SELECT cpm FROM CompanyPlantMaterial cpm " +
                        "WHERE cpm.companyId = :companyId " +
                        "AND cpm.plantId = :plantId " +
                        "AND cpm.status = 1")
        List<CompanyPlantMaterial> findMaterialsByCompanyAndPlant(
                        @Param("companyId") Integer companyId,
                        @Param("plantId") Integer plantId);

        /**
         * Find all plants where a material is available for a company
         * Use Case: Check material availability across plants, production scheduling
         * 
         * @param companyId  Company identifier
         * @param materialId Material identifier
         * @return List of plants with this material
         */
        @Query("SELECT cpm FROM CompanyPlantMaterial cpm " +
                        "WHERE cpm.companyId = :companyId " +
                        "AND cpm.materialId = :materialId " +
                        "AND cpm.status = 1")
        List<CompanyPlantMaterial> findPlantsByCompanyAndMaterial(
                        @Param("companyId") Integer companyId,
                        @Param("materialId") Integer materialId);

        /**
         * Find all mappings where stock is low (below a threshold)
         * Since reorder level is not in the current schema, returns mappings with
         * low stock quantity (quantity < 10 as a default threshold)
         * 
         * Use Case: Automated procurement alerts, production risk monitoring
         * 
         * @return List of mappings needing replenishment
         */
        @Query("SELECT cpm FROM CompanyPlantMaterial cpm " +
                        "WHERE cpm.status = 1 " +
                        "AND cpm.quantityStores < 10 " +
                        "ORDER BY cpm.quantityStores ASC")
        List<CompanyPlantMaterial> findMappingsNeedingReorder();

        /**
         * Find mappings needing reorder at specific plant
         * Since reorder level is not in the current schema, uses quantity < 10
         * threshold
         * 
         * @param plantId Plant identifier
         * @return List of materials needing reorder at plant
         */
        @Query("SELECT cpm FROM CompanyPlantMaterial cpm " +
                        "WHERE cpm.plantId = :plantId " +
                        "AND cpm.status = 1 " +
                        "AND cpm.quantityStores < 10 " +
                        "ORDER BY cpm.quantityStores ASC")
        List<CompanyPlantMaterial> findMappingsNeedingReorderAtPlant(
                        @Param("plantId") Integer plantId);

        /**
         * Count total active mappings
         * Use Case: Dashboard statistics, monitoring
         * 
         * @return Count of active plant-material mappings
         */
        @Query("SELECT COUNT(cpm) FROM CompanyPlantMaterial cpm WHERE cpm.status = 1")
        long countActiveMappings();

        /**
         * Count materials at specific plant
         * Use Case: Plant capacity analysis
         * 
         * @param plantId Plant identifier
         * @param status  Status filter
         * @return Count of materials at plant
         */
        long countByPlantIdAndStatus(Integer plantId, Integer status);

        /**
         * Count unique materials per company across all plants
         * Use Case: Material diversity metrics, procurement efficiency
         * 
         * @param companyId Company identifier
         * @return Count of distinct materials
         */
        @Query("SELECT COUNT(DISTINCT cpm.materialId) FROM CompanyPlantMaterial cpm " +
                        "WHERE cpm.companyId = :companyId AND cpm.status = 1")
        long countDistinctMaterialsByCompany(@Param("companyId") Integer companyId);

        /**
         * Find mappings with low stock (below threshold quantity)
         * Since reorder level is not in the current schema, uses a fixed quantity
         * threshold
         * 
         * @param threshold Quantity threshold (mappings below this are considered low
         *                  stock)
         * @return List of materials with low stock
         */
        @Query("SELECT cpm FROM CompanyPlantMaterial cpm " +
                        "WHERE cpm.status = 1 " +
                        "AND cpm.quantityStores <= :threshold " +
                        "ORDER BY cpm.quantityStores ASC")
        List<CompanyPlantMaterial> findLowStockMappings(
                        @Param("threshold") Double threshold);
}
