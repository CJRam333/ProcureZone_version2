package com.nslindia.procurezone.vendor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Vendor entity.
 */
@Repository
public interface VendorRepository extends JpaRepository<Vendor, Integer> {

    /**
     * Find vendor by vendor code
     */
    Optional<Vendor> findByVendorCode(String vendorCode);

    /**
     * Find vendors by status
     */
    Page<Vendor> findByStatus(Integer status, Pageable pageable);

    /**
     * Find active vendors
     */
    @Query("SELECT v FROM Vendor v WHERE v.status = 1 ORDER BY v.vendorName")
    List<Vendor> findActiveVendors();

    /**
     * Search vendors by name, code, or contact
     */
    @Query("SELECT v FROM Vendor v WHERE " +
            "LOWER(v.vendorName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.vendorCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.contactPerson) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.city) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Vendor> searchVendors(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find vendors by type
     */
    Page<Vendor> findByVendorType(String vendorType, Pageable pageable);

    /**
     * Find vendors by city
     */
    Page<Vendor> findByCity(String city, Pageable pageable);

    /**
     * Find top rated vendors
     */
    @Query("SELECT v FROM Vendor v WHERE v.rating >= :minRating AND v.status = 1 ORDER BY v.rating DESC")
    List<Vendor> findTopRatedVendors(@Param("minRating") Double minRating);

    /**
     * Count vendors by status
     */
    long countByStatus(Integer status);

    /**
     * Check if vendor code exists
     */
    boolean existsByVendorCode(String vendorCode);

    /**
     * Check if GST number exists
     */
    boolean existsByGstNumber(String gstNumber);

    /**
     * Find vendors with orders
     */
    @Query("SELECT v FROM Vendor v WHERE v.totalOrders > 0 ORDER BY v.totalOrders DESC")
    List<Vendor> findVendorsWithOrders();
}
