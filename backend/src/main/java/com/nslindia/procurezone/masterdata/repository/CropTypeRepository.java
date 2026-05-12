package com.nslindia.procurezone.masterdata.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nslindia.procurezone.masterdata.CropType;

@Repository
public interface CropTypeRepository extends JpaRepository<CropType, Integer> {

    Page<CropType> findByStatus(Integer status, Pageable pageable);

    @Query("SELECT c FROM CropType c WHERE LOWER(c.divisionName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<CropType> searchByName(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM CropType c WHERE LOWER(c.divisionName) LIKE LOWER(CONCAT('%', :search, '%')) AND c.status = :status")
    Page<CropType> searchByNameAndStatus(@Param("search") String search, @Param("status") Integer status, Pageable pageable);
}
