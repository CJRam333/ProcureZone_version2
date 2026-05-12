package com.nslindia.procurezone.masterdata.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nslindia.procurezone.masterdata.Department;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Department entity operations.
 * Provides CRUD operations and custom queries for department master data.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    /**
     * Find department by unique code.
     * 
     * @param code the department code
     * @return Optional containing the department if found
     */
    Optional<Department> findByCode(String code);

    /**
     * Find all active departments.
     * 
     * @param status the status (1 for active)
     * @return list of active departments
     */
    List<Department> findByStatus(Integer status);

    /**
     * Find all active departments with pagination.
     * 
     * @param status   the status (1 for active)
     * @param pageable pagination information
     * @return page of active departments
     */
    Page<Department> findByStatus(Integer status, Pageable pageable);

    /**
     * Search departments by code or name containing the search term
     * (case-insensitive).
     * 
     * @param searchTerm the term to search for
     * @param pageable   pagination information
     * @return page of matching departments
     */
    @Query("SELECT d FROM Department d WHERE " +
            "LOWER(d.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Department> searchByCodeOrName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search active departments by code or name containing the search term.
     * 
     * @param searchTerm the term to search for
     * @param status     the status filter
     * @param pageable   pagination information
     * @return page of matching active departments
     */
    @Query("SELECT d FROM Department d WHERE d.status = :status AND " +
            "(LOWER(d.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Department> searchByCodeOrNameAndStatus(
            @Param("searchTerm") String searchTerm,
            @Param("status") Integer status,
            Pageable pageable);

    /**
     * Check if a department exists with the given code.
     * 
     * @param code the department code
     * @return true if exists, false otherwise
     */
    boolean existsByCode(String code);

    /**
     * Check if a department exists with the given code, excluding a specific id.
     * Used for update validation to allow same code for same entity.
     * 
     * @param code the department code
     * @param id   the department id to exclude
     * @return true if exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Department d " +
            "WHERE d.code = :code AND d.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Integer id);
}
