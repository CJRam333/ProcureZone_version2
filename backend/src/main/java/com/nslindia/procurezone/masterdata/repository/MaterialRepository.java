package com.nslindia.procurezone.masterdata.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nslindia.procurezone.masterdata.Material;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Material entity operations.
 * Provides CRUD operations and custom queries for material master data.
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {

        /**
         * Find material by unique code.
         * 
         * @param code the material code
         * @return Optional containing the material if found
         */
        Optional<Material> findByCode(String code);

        /**
         * Find all active materials.
         * 
         * @param status the status (1 for active)
         * @return list of active materials
         */
        List<Material> findByStatus(Integer status);

        /**
         * Find all active materials with pagination.
         * 
         * @param status   the status (1 for active)
         * @param pageable pagination information
         * @return page of active materials
         */
        Page<Material> findByStatus(Integer status, Pageable pageable);

        /**
         * Search materials by code, name, or description containing the search term
         * (case-insensitive).
         * 
         * @param searchTerm the term to search for
         * @param pageable   pagination information
         * @return page of matching materials
         */
        @Query("SELECT m FROM Material m WHERE " +
                        "LOWER(m.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        Page<Material> searchByCodeOrNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

        /**
         * Search active materials by code, name, or description containing the search
         * term.
         * 
         * @param searchTerm the term to search for
         * @param status     the status filter
         * @param pageable   pagination information
         * @return page of matching active materials
         */
        @Query("SELECT m FROM Material m WHERE m.status = :status AND " +
                        "(LOWER(m.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
        Page<Material> searchByCodeOrNameOrDescriptionAndStatus(
                        @Param("searchTerm") String searchTerm,
                        @Param("status") Integer status,
                        Pageable pageable);

        /**
         * Check if a material exists with the given code.
         * 
         * @param code the material code
         * @return true if exists, false otherwise
         */
        boolean existsByCode(String code);

        /**
         * Check if a material exists with the given code, excluding a specific id.
         * Used for update validation to allow same code for same entity.
         * 
         * @param code the material code
         * @param id   the material id to exclude
         * @return true if exists, false otherwise
         */
        @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Material m " +
                        "WHERE m.code = :code AND m.id != :id")
        boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Integer id);

        /**
         * Get all material codes (for bulk import duplicate detection).
         * 
         * @return list of all material codes
         */
        @Query("SELECT m.code FROM Material m")
        List<String> findAllCodes();
}
