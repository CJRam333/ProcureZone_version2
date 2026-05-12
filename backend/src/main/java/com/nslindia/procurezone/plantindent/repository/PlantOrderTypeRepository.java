package com.nslindia.procurezone.plantindent.repository;

import com.nslindia.procurezone.plantindent.entity.PlantOrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantOrderTypeRepository extends JpaRepository<PlantOrderType, Integer> {
    List<PlantOrderType> findByPlantIdAndStatus(Integer plantId, Integer status);

    List<PlantOrderType> findByStatus(Integer status);
}
