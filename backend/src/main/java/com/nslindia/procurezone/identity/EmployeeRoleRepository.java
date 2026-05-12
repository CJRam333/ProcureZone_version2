package com.nslindia.procurezone.identity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for EmployeeRole entity.
 */
@Repository
public interface EmployeeRoleRepository extends JpaRepository<EmployeeRole, Long> {

    List<EmployeeRole> findByEmployee_EmployeeNumber(Integer employeeNumber);

    List<EmployeeRole> findByEmployee_EmployeeNumberAndStatus(Integer employeeNumber, Integer status);

    boolean existsByEmployeeAndRoleAndStatus(Employee employee, Role role, Integer status);

    Optional<EmployeeRole> findByEmployeeAndRoleAndStatus(Employee employee, Role role, Integer status);
}
