package com.nslindia.procurezone.moduleaccess;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpModuleAccessRepository extends JpaRepository<EmpModuleAccess, Integer> {

    List<EmpModuleAccess> findByEmpNumber(Integer empNumber);

    Optional<EmpModuleAccess> findByEmpNumberAndModuleCode(Integer empNumber, String moduleCode);

    @Query("SELECT e.moduleCode FROM EmpModuleAccess e WHERE e.empNumber = :empNumber AND e.enabled = true")
    List<String> findEnabledModuleCodesByEmpNumber(@Param("empNumber") Integer empNumber);

    boolean existsByEmpNumberAndModuleCode(Integer empNumber, String moduleCode);
}
