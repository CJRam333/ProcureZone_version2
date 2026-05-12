package com.nslindia.procurezone.plantindent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Plant Indent Detail operations.
 * 
 * @author NSL India
 * @version 1.0
 */
@Repository
public interface PlantIndentDetailRepository extends JpaRepository<PlantIndentDetail, Integer> {

    /**
     * Find details by plant indent ID
     */
    List<PlantIndentDetail> findByPlantIndentId(Integer plantIndentId);

    /**
     * Find details by material ID
     */
    List<PlantIndentDetail> findByMaterialId(Integer materialId);

    /**
     * Find details by lot number
     */
    List<PlantIndentDetail> findByLotNumber(String lotNumber);

    /**
     * Find details by storage location
     */
    List<PlantIndentDetail> findByStorageLocation(String storageLocation);

    /**
     * Find details with QC parameters set (for QC inspection queue)
     */
    @Query("SELECT d FROM PlantIndentDetail d WHERE d.plantIndent.id = :indentId " +
            "AND (d.stl IS NOT NULL OR d.odv IS NOT NULL OR d.got IS NOT NULL OR d.elisa IS NOT NULL)")
    List<PlantIndentDetail> findByPlantIndentIdWithQCParams(@Param("indentId") Integer indentId);

    /**
     * Count details by plant indent
     */
    long countByPlantIndentId(Integer plantIndentId);

    /**
     * Delete all details by plant indent ID
     */
    void deleteByPlantIndentId(Integer plantIndentId);
}
