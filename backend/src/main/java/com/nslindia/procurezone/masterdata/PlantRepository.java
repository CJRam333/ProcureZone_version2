package com.nslindia.procurezone.masterdata;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Plant entity.
 */
@Repository
public interface PlantRepository extends JpaRepository<Plant, Integer> {

    Optional<Plant> findByCode(String code);

    Page<Plant> findByStatus(Integer status, Pageable pageable);

    Page<Plant> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
            String code, String name, Pageable pageable);

    Page<Plant> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseAndStatus(
            String code, String name, Integer status, Pageable pageable);

    boolean existsByCode(String code);
}
