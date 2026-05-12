package com.nslindia.procurezone.masterdata.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nslindia.procurezone.masterdata.Company;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Company entity operations.
 * Provides CRUD operations and custom queries for company master data.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    /**
     * Find company by unique code.
     * 
     * @param code the company code
     * @return Optional containing the company if found
     */
    Optional<Company> findByCode(String code);

    /**
     * Find all active companies.
     * 
     * @param status the status (1 for active)
     * @return list of active companies
     */
    List<Company> findByStatus(Integer status);

    /**
     * Find all active companies with pagination.
     * 
     * @param status   the status (1 for active)
     * @param pageable pagination information
     * @return page of active companies
     */
    Page<Company> findByStatus(Integer status, Pageable pageable);

    /**
     * Search companies by code or name containing the search term
     * (case-insensitive).
     * 
     * @param searchTerm the term to search for
     * @param pageable   pagination information
     * @return page of matching companies
     */
    @Query("SELECT c FROM Company c WHERE " +
            "LOWER(c.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Company> searchByCodeOrName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search active companies by code or name containing the search term.
     * 
     * @param searchTerm the term to search for
     * @param status     the status filter
     * @param pageable   pagination information
     * @return page of matching active companies
     */
    @Query("SELECT c FROM Company c WHERE c.status = :status AND " +
            "(LOWER(c.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Company> searchByCodeOrNameAndStatus(
            @Param("searchTerm") String searchTerm,
            @Param("status") Integer status,
            Pageable pageable);

    /**
     * Check if a company exists with the given code.
     * 
     * @param code the company code
     * @return true if exists, false otherwise
     */
    boolean existsByCode(String code);

    /**
     * Check if a company exists with the given code, excluding a specific id.
     * Used for update validation to allow same code for same entity.
     * 
     * @param code the company code
     * @param id   the company id to exclude
     * @return true if exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Company c " +
            "WHERE c.code = :code AND c.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Integer id);
}
