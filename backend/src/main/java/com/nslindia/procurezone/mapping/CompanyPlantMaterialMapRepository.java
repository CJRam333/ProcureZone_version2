package com.nslindia.procurezone.mapping;

import com.nslindia.procurezone.masterdata.dto.MaterialDropdownResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

        @Query("SELECT SUM(s.quantity) FROM CompanyPlantMaterialMap s " +
                        "WHERE s.material.id = :materialId AND s.status IN (0, 1)")
        Optional<BigDecimal> sumQuantityByMaterial(@Param("materialId") Integer materialId);

        @Query("""
                        SELECT new com.nslindia.procurezone.masterdata.dto.MaterialDropdownResponse(
                            m.id, m.code, m.name, m.description,
                            co.id, co.name, pl.id, pl.name,
                            COALESCE(SUM(s.quantity), 0))
                        FROM Material m
                        LEFT JOIN CompanyPlantMaterialMap s ON s.material = m AND s.status IN (0, 1)
                        LEFT JOIN s.company co
                        LEFT JOIN s.plant pl
                        WHERE m.status = 1
                        AND (:search = ''
                             OR LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%'))
                             OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%'))
                             OR LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%')))
                        GROUP BY m.id, m.code, m.name, m.description, co.id, co.name, pl.id, pl.name
                        ORDER BY m.code
                        """)
        List<MaterialDropdownResponse> searchForDropdownAllCompanies(@Param("search") String search);

        @Query("""
                        SELECT new com.nslindia.procurezone.masterdata.dto.MaterialDropdownResponse(
                            m.id, m.code, m.name, m.description,
                            co.id, co.name, pl.id, pl.name,
                            COALESCE(SUM(s.quantity), 0))
                        FROM Material m
                        LEFT JOIN CompanyPlantMaterialMap s ON s.material = m AND s.status IN (0, 1)
                        LEFT JOIN s.company co
                        LEFT JOIN s.plant pl
                        WHERE m.status = 1
                        AND (co.id IS NULL OR co.id IN :companyIds)
                        AND (:search = ''
                             OR LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%'))
                             OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%'))
                             OR LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%')))
                        GROUP BY m.id, m.code, m.name, m.description, co.id, co.name, pl.id, pl.name
                        ORDER BY m.code
                        """)
        List<MaterialDropdownResponse> searchForDropdownByCompanies(
                        @Param("search") String search,
                        @Param("companyIds") List<Integer> companyIds);
}
