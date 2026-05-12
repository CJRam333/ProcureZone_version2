package com.nslindia.procurezone.repository;

import com.nslindia.procurezone.entity.EmployeeRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRoleMappingRepository extends JpaRepository<EmployeeRole, Integer> {

    Page<EmployeeRole> findByStatus(Integer status, Pageable pageable);

    @Query("SELECT er FROM EmployeeRoleMapping er WHERE er.status = 1")
    Page<EmployeeRole> findAllActive(Pageable pageable);

    @Query("SELECT er FROM EmployeeRoleMapping er WHERE er.employeeNumber = :employeeNumber AND er.status = 1")
    List<EmployeeRole> findActiveRolesByEmployee(@Param("employeeNumber") Integer employeeNumber);

    @Query("SELECT er.roleId FROM EmployeeRoleMapping er WHERE er.employeeNumber = :employeeNumber AND er.status = 1")
    List<Integer> findActiveRoleIdsByEmployee(@Param("employeeNumber") Integer employeeNumber);

    @Query("SELECT er FROM EmployeeRoleMapping er WHERE er.roleId = :roleId AND er.status = 1")
    Page<EmployeeRole> findEmployeesByRole(@Param("roleId") Integer roleId, Pageable pageable);

    @Query("SELECT er.employeeNumber FROM EmployeeRoleMapping er WHERE er.roleId = :roleId AND er.status = 1")
    List<Integer> findEmployeeNumbersByRole(@Param("roleId") Integer roleId);

    Optional<EmployeeRole> findByEmployeeNumberAndRoleId(Integer employeeNumber, Integer roleId);

    boolean existsByEmployeeNumberAndRoleIdAndStatus(Integer employeeNumber, Integer roleId, Integer status);

    @Query("SELECT CASE WHEN COUNT(er) > 0 THEN true ELSE false END FROM EmployeeRoleMapping er " +
            "WHERE er.employeeNumber = :employeeNumber AND er.roleId = :roleId AND er.status = 1")
    boolean hasActiveRole(@Param("employeeNumber") Integer employeeNumber, @Param("roleId") Integer roleId);

    @Query("SELECT COUNT(er) FROM EmployeeRoleMapping er WHERE er.employeeNumber = :employeeNumber AND er.status = 1")
    long countActiveRolesByEmployee(@Param("employeeNumber") Integer employeeNumber);

    @Query("SELECT COUNT(er) FROM EmployeeRoleMapping er WHERE er.roleId = :roleId AND er.status = 1")
    long countEmployeesWithRole(@Param("roleId") Integer roleId);

    Page<EmployeeRole> findByEmployeeNumber(Integer employeeNumber, Pageable pageable);

    Page<EmployeeRole> findByRoleId(Integer roleId, Pageable pageable);
}
