package com.nslindia.procurezone.plantindent.repository;

import com.nslindia.procurezone.plantindent.entity.EmployeePlantMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeePlantMapRepository extends JpaRepository<EmployeePlantMap, Integer> {
    List<EmployeePlantMap> findByEmployeeIdAndStatus(Integer employeeId, Integer status);

    List<EmployeePlantMap> findByPlantIdAndStatus(Integer plantId, Integer status);

    List<EmployeePlantMap> findByPlantIdAndRoleIdAndStatus(Integer plantId, Integer roleId, Integer status);

    Optional<EmployeePlantMap> findByEmployeeIdAndPlantIdAndStatus(Integer employeeId, Integer plantId, Integer status);

    boolean existsByEmployeeIdAndPlantIdAndStatus(Integer employeeId, Integer plantId, Integer status);
}
