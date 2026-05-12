package com.nslindia.procurezone.mapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Company-Plant-Material Mapping
 * 
 * @author NSL India
 * @version 1.0
 */
@Repository
public interface CompanyPlantMaterialMapRepository extends JpaRepository<CompanyPlantMaterialMap, Integer> {

        /**
         * Find mapping by company, plant, and material
         */
        @Query("SELECT cpm FROM CompanyPlantMaterialMap cpm " +
                        "WHERE cpm.company.id = :companyId " +
                        "AND cpm.plant.id = :plantId " +
                        "AND cpm.material.id = :materialId")
        Optional<CompanyPlantMaterialMap> findByCompanyAndPlantAndMaterial(
                        @Param("companyId") Integer companyId,
                        @Param("plantId") Integer plantId,
                        @Param("materialId") Integer materialId);

        /**
         * Find all active mappings for a plant
         */
        @Query("SELECT cpm FROM CompanyPlantMaterialMap cpm " +
                        "WHERE cpm.plant.id = :plantId " +
                        "AND cpm.status = 1")
        List<CompanyPlantMaterialMap> findActiveByPlant(@Param("plantId") Integer plantId);

        /**
         * Find all active mappings for a company
         */
        @Query("SELECT cpm FROM CompanyPlantMaterialMap cpm " +
                        "WHERE cpm.company.id = :companyId " +
                        "AND cpm.status = 1")
        List<CompanyPlantMaterialMap> findActiveByCompany(@Param("companyId") Integer companyId);

        /**
         * Find all materials available at a specific plant
         */
        @Query("SELECT cpm FROM CompanyPlantMaterialMap cpm " +
                        "WHERE cpm.company.id = :companyId " +
                        "AND cpm.plant.id = :plantId " +
                        "AND cpm.status = 1")
        List<CompanyPlantMaterialMap> findMaterialsByCompanyAndPlant(
                        @Param("companyId") Integer companyId,
                        @Param("plantId") Integer plantId);

        /**
         * Check if material is available at plant
         */
        @Query("SELECT CASE WHEN COUNT(cpm) > 0 THEN true ELSE false END " +
                        "FROM CompanyPlantMaterialMap cpm " +
                        "WHERE cpm.company.id = :companyId " +
                        "AND cpm.plant.id = :plantId " +
                        "AND cpm.material.id = :materialId " +
                        "AND cpm.status = 1")
        boolean isMaterialAvailableAtPlant(
                        @Param("companyId") Integer companyId,
                        @Param("plantId") Integer plantId,
                        @Param("materialId") Integer materialId);

        /**
         * Find materials for reorder
         */
        @Query("SELECT cpm FROM CompanyPlantMaterialMap cpm " +
                        "WHERE cpm.plant.id = :plantId " +
                        "AND cpm.status = 1 " +
                        "ORDER BY cpm.quantity ASC")
        List<CompanyPlantMaterialMap> findMaterialsForReorder(@Param("plantId") Integer plantId);
}
