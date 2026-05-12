package com.nslindia.procurezone.repository;

import com.nslindia.procurezone.entity.CompanyEmployee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyEmployeeRepository extends JpaRepository<CompanyEmployee, Integer> {

    @Query("SELECT ce FROM CompanyEmployee ce WHERE ce.status = 1")
    Page<CompanyEmployee> findAllActive(Pageable pageable);

    Optional<CompanyEmployee> findByCompanyIdAndEmployeeNumber(Integer companyId, Integer employeeNumber);

    boolean existsByCompanyIdAndEmployeeNumber(Integer companyId, Integer employeeNumber);

    @Query("SELECT ce FROM CompanyEmployee ce WHERE ce.companyId = :companyId AND ce.status = 1")
    Page<CompanyEmployee> findEmployeesByCompany(@Param("companyId") Integer companyId, Pageable pageable);

    @Query("SELECT ce FROM CompanyEmployee ce WHERE ce.employeeNumber = :employeeNumber AND ce.status = 1")
    Page<CompanyEmployee> findCompaniesByEmployee(@Param("employeeNumber") Integer employeeNumber, Pageable pageable);

    @Query("SELECT COUNT(ce) FROM CompanyEmployee ce WHERE ce.companyId = :companyId AND ce.status = 1")
    long countActiveEmployeesByCompany(@Param("companyId") Integer companyId);

    Page<CompanyEmployee> findByCompanyId(Integer companyId, Pageable pageable);

    Page<CompanyEmployee> findByEmployeeNumber(Integer employeeNumber, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(ce) > 0 THEN true ELSE false END FROM CompanyEmployee ce " +
            "WHERE ce.companyId = :companyId AND ce.employeeNumber = :employeeNumber AND ce.status = 1")
    boolean isEmployeeAssignedToCompany(@Param("companyId") Integer companyId,
            @Param("employeeNumber") Integer employeeNumber);

    @Query("SELECT ce.employeeNumber FROM CompanyEmployee ce WHERE ce.companyId = :companyId AND ce.status = 1")
    List<Integer> findActiveEmployeeNumbersByCompany(@Param("companyId") Integer companyId);
}
