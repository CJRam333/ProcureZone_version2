package com.nslindia.procurezone.plantindent.repository;

import com.nslindia.procurezone.plantindent.entity.SapMaterialMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SapMaterialMasterRepository extends JpaRepository<SapMaterialMaster, Integer> {
    List<SapMaterialMaster> findByPlantAndStatus(Integer plant, Integer status);

    Optional<SapMaterialMaster> findByMaterialCodeAndStatus(String materialCode, Integer status);

    @Query("SELECT s FROM SapMaterialMaster s WHERE s.status = :status AND " +
            "(LOWER(s.materialCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.materialDescription) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<SapMaterialMaster> searchMaterials(@Param("search") String search, @Param("status") Integer status);
}
