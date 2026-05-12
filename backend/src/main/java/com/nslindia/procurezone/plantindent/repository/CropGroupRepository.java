package com.nslindia.procurezone.plantindent.repository;

import com.nslindia.procurezone.plantindent.entity.CropGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropGroupRepository extends JpaRepository<CropGroup, Integer> {
    List<CropGroup> findByStatus(Integer status);

    Optional<CropGroup> findByCropCodeAndStatus(String cropCode, Integer status);
}
