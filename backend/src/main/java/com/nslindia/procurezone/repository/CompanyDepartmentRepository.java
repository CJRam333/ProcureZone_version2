package com.nslindia.procurezone.repository;

import com.nslindia.procurezone.entity.CompanyDepartment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyDepartmentRepository extends JpaRepository<CompanyDepartment, Integer> {

    @Query("SELECT cd FROM CompanyDepartment cd WHERE cd.status = 1")
    Page<CompanyDepartment> findAllActive(Pageable pageable);

    Optional<CompanyDepartment> findByCompanyIdAndDepartmentId(Integer companyId, Integer departmentId);

    boolean existsByCompanyIdAndDepartmentId(Integer companyId, Integer departmentId);

    @Query("SELECT cd FROM CompanyDepartment cd WHERE cd.companyId = :companyId AND cd.status = 1")
    Page<CompanyDepartment> findDepartmentsByCompany(@Param("companyId") Integer companyId, Pageable pageable);

    @Query("SELECT cd FROM CompanyDepartment cd WHERE cd.departmentId = :departmentId AND cd.status = 1")
    Page<CompanyDepartment> findCompaniesByDepartment(@Param("departmentId") Integer departmentId, Pageable pageable);

    java.util.List<CompanyDepartment> findByCompanyId(Integer companyId);

    Page<CompanyDepartment> findByCompanyId(Integer companyId, Pageable pageable);

    Page<CompanyDepartment> findByDepartmentId(Integer departmentId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(cd) > 0 THEN true ELSE false END FROM CompanyDepartment cd " +
            "WHERE cd.companyId = :companyId AND cd.departmentId = :departmentId AND cd.status = 1")
    boolean isDepartmentAccessibleToCompany(@Param("companyId") Integer companyId,
            @Param("departmentId") Integer departmentId);
}
