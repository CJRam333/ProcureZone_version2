package com.nslindia.procurezone.plantindent.repository;

import com.nslindia.procurezone.plantindent.entity.PlantLineCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantLineCodeRepository extends JpaRepository<PlantLineCode, Integer> {
    List<PlantLineCode> findByPlantIdAndStatus(Integer plantId, Integer status);

    List<PlantLineCode> findByStatus(Integer status);
}
