package com.nslindia.procurezone.plantindent.repository;

import com.nslindia.procurezone.plantindent.entity.PlantStorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantStorageLocationRepository extends JpaRepository<PlantStorageLocation, Integer> {
    List<PlantStorageLocation> findByPlantCodeAndStatus(Integer plantCode, Integer status);

    List<PlantStorageLocation> findByStatus(Integer status);
}
