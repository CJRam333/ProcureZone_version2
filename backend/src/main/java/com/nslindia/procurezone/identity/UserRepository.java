package com.nslindia.procurezone.identity;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for UserAccount entity.
 */
@Repository
public interface UserRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<UserAccount> findByEmployee_EmployeeNumber(Integer employeeNumber);

    boolean existsByEmployee_EmployeeNumber(Integer employeeNumber);

    // Status-based queries
    Page<UserAccount> findByStatus(Integer status, Pageable pageable);

    Long countByStatus(Integer status);

    // Search queries
    @Query("SELECT u FROM UserAccount u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.employee.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.employee.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<UserAccount> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT u FROM UserAccount u WHERE u.status = :status AND " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.employee.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<UserAccount> searchUsersByStatus(@Param("searchTerm") String searchTerm,
            @Param("status") Integer status,
            Pageable pageable);
}
