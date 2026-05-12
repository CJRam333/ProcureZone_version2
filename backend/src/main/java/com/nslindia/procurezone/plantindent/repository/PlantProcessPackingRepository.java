package com.nslindia.procurezone.plantindent.repository;

import com.nslindia.procurezone.plantindent.entity.PlantProcessPacking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantProcessPackingRepository extends JpaRepository<PlantProcessPacking, Integer> {
    List<PlantProcessPacking> findByStatus(Integer status);
}
