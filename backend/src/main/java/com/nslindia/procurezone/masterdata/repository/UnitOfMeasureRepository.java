package com.nslindia.procurezone.masterdata.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nslindia.procurezone.masterdata.UnitOfMeasure;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UnitOfMeasure entity operations.
 * Provides CRUD operations and custom queries for UOM master data.
 */
@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Integer> {

    /**
     * Find unit of measure by unique code.
     * 
     * @param code the UOM code
     * @return Optional containing the UOM if found
     */
    Optional<UnitOfMeasure> findByCode(String code);

    /**
     * Find all active units of measure.
     * 
     * @param status the status (1 for active)
     * @return list of active UOMs
     */
    List<UnitOfMeasure> findByStatus(Integer status);

    /**
     * Find all active units of measure with pagination.
     * 
     * @param status   the status (1 for active)
     * @param pageable pagination information
     * @return page of active UOMs
     */
    Page<UnitOfMeasure> findByStatus(Integer status, Pageable pageable);

    /**
     * Search units of measure by code or name containing the search term
     * (case-insensitive).
     * 
     * @param searchTerm the term to search for
     * @param pageable   pagination information
     * @return page of matching UOMs
     */
    @Query("SELECT u FROM UnitOfMeasure u WHERE " +
            "LOWER(u.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<UnitOfMeasure> searchByCodeOrName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search active units of measure by code or name containing the search term.
     * 
     * @param searchTerm the term to search for
     * @param status     the status filter
     * @param pageable   pagination information
     * @return page of matching active UOMs
     */
    @Query("SELECT u FROM UnitOfMeasure u WHERE u.status = :status AND " +
            "(LOWER(u.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<UnitOfMeasure> searchByCodeOrNameAndStatus(
            @Param("searchTerm") String searchTerm,
            @Param("status") Integer status,
            Pageable pageable);

    /**
     * Check if a unit of measure exists with the given code.
     * 
     * @param code the UOM code
     * @return true if exists, false otherwise
     */
    boolean existsByCode(String code);

    /**
     * Check if a unit of measure exists with the given code, excluding a specific
     * id.
     * Used for update validation to allow same code for same entity.
     * 
     * @param code the UOM code
     * @param id   the UOM id to exclude
     * @return true if exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UnitOfMeasure u " +
            "WHERE u.code = :code AND u.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Integer id);
}
