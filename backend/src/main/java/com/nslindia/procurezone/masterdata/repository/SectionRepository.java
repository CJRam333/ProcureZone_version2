package com.nslindia.procurezone.masterdata.repository;

import com.nslindia.procurezone.masterdata.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Integer> {

    Optional<Section> findByCode(String code);

    boolean existsByCode(String code);

    Page<Section> findByStatus(Integer status, Pageable pageable);

    @Query("SELECT s FROM Section s WHERE " +
            "(LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Section> searchByCodeOrName(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT s FROM Section s WHERE " +
            "(LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "s.status = :status")
    Page<Section> searchByCodeOrNameAndStatus(
            @Param("searchTerm") String searchTerm,
            @Param("status") Integer status,
            Pageable pageable);
}
