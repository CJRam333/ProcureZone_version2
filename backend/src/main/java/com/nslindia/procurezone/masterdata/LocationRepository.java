package com.nslindia.procurezone.masterdata;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Location entity.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {

    Optional<Location> findByCode(String code);

    Page<Location> findByStatus(Integer status, Pageable pageable);

    Page<Location> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
            String code, String name, Pageable pageable);

    Page<Location> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseAndStatus(
            String code, String name, Integer status, Pageable pageable);

    boolean existsByCode(String code);
}
