package com.nslindia.procurezone.employee;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.employee.dto.AssignRoleRequest;
import com.nslindia.procurezone.employee.dto.CreateEmployeeRequest;
import com.nslindia.procurezone.employee.dto.EmployeeResponse;
import com.nslindia.procurezone.employee.dto.EmployeeStatisticsResponse;
import com.nslindia.procurezone.employee.dto.EmployeeSummaryResponse;
import com.nslindia.procurezone.employee.dto.UpdateEmployeeRequest;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.identity.EmployeeRole;
import com.nslindia.procurezone.identity.EmployeeRoleRepository;
import com.nslindia.procurezone.identity.Role;
import com.nslindia.procurezone.identity.RoleRepository;
import com.nslindia.procurezone.identity.UserAccount;
import com.nslindia.procurezone.identity.UserRepository;
import com.nslindia.procurezone.masterdata.Department;
import com.nslindia.procurezone.entity.CompanyLocation;
import com.nslindia.procurezone.masterdata.repository.DepartmentRepository;
import com.nslindia.procurezone.repository.CompanyLocationRepository;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyLocationRepository companyLocationRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmployeeRoleRepository employeeRoleRepository,
            DepartmentRepository departmentRepository,
            CompanyLocationRepository companyLocationRepository,
            PasswordEncoder passwordEncoder) {

        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeRoleRepository = employeeRoleRepository;
        this.departmentRepository = departmentRepository;
        this.companyLocationRepository = companyLocationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* ================= CREATE ================= */

    public EmployeeResponse createEmployee(CreateEmployeeRequest request, String createdBy) {

        if (employeeRepository.existsByEmployeeId(request.employeeId())) {
            throw new BadRequestException("Employee ID already exists: " + request.employeeId());
        }
        if (employeeRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists: " + request.email());
        }

        if (request.departmentId() != null &&
                !departmentRepository.existsById(request.departmentId())) {
            throw new ResourceNotFoundException("Department", "id", request.departmentId());
        }

        if (request.locationId() != null &&
                !companyLocationRepository.existsById(request.locationId())) {
            throw new ResourceNotFoundException("CompanyLocation", "id", request.locationId());
        }

        Employee employee = new Employee();
        employee.setEmployeeId(request.employeeId());
        employee.setFullName(request.fullName());
        employee.setEmail(request.email());
        employee.setJoinDate(request.joinDate());
        employee.setDesignation(request.designation());
        employee.setCostCenter(request.costCenter());
        employee.setDepartmentId(request.departmentId());
        employee.setLocationId(request.locationId());
        employee.setPlant(request.plant());
        employee.setStatus(1);
        employee.setLastModifiedDate(LocalDate.now());

        employee = employeeRepository.save(employee);

        UserAccount user = new UserAccount();
        user.setUsername(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmployee(employee);
        user.setStatus(1);
        user.setLastModifiedDate(LocalDate.now());
        user.setLastModifiedUser(employee.getEmployeeNumber());

        userRepository.save(user);

        if (request.roleIds() != null) {
            for (Integer roleId : request.roleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

                EmployeeRole er = new EmployeeRole();
                er.setEmployee(employee);
                er.setRole(role);
                er.setStatus(1);
                /*
                 * er.setAssignedDate(LocalDate.now());
                 * er.setAssignedBy(createdBy);
                 */
                er.setLastModifiedDate(LocalDateTime.now());
                er.setLastModifiedUser(createdBy);

                employeeRoleRepository.save(er);
            }
        }

        return mapToEmployeeResponse(employee);
    }

    // ===== BACKWARD COMPATIBILITY FOR CONTROLLERS =====

    public Page<EmployeeSummaryResponse> getAllEmployees(
            Integer status, Pageable pageable) {
        if (status != null) {
            return employeeRepository.findByStatus(status, pageable).map(this::mapToSummaryResponse);
        }
        return employeeRepository.findAll(pageable).map(this::mapToSummaryResponse);
    }

    public Page<EmployeeSummaryResponse> searchEmployees(
            String term, Integer status, Pageable pageable) {
        if (status != null) {
            return employeeRepository.searchEmployeesByStatus(term, status, pageable).map(this::mapToSummaryResponse);
        }
        return employeeRepository.searchEmployees(term, pageable).map(this::mapToSummaryResponse);
    }

    public Page<EmployeeSummaryResponse> getAvailableForUser(Integer currentEmployeeId, Pageable pageable) {
        return employeeRepository.findAvailableForUser(1, currentEmployeeId, pageable).map(this::mapToSummaryResponse);
    }

    public void assignRole(
            Integer employeeId,
            AssignRoleRequest request,
            String assignedBy) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        
        Integer roleId = request.roleId();
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        boolean alreadyAssigned = employeeRoleRepository.existsByEmployeeAndRoleAndStatus(employee, role, 1);
        if (!alreadyAssigned) {
            EmployeeRole er = new EmployeeRole();
            er.setEmployee(employee);
            er.setRole(role);
            er.setStatus(1);
            er.setLastModifiedDate(LocalDateTime.now());
            er.setLastModifiedUser(assignedBy);
            employeeRoleRepository.save(er);
        }
    }

    public void removeRole(
            Integer employeeId,
            Integer roleId,
            String removedBy) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        employeeRoleRepository.findByEmployeeAndRoleAndStatus(employee, role, 1)
                .ifPresent(er -> employeeRoleRepository.delete(er));
    }

    public void activateEmployee(Integer id, String user) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employee.activate();
        employeeRepository.save(employee);
    }

    public void deactivateEmployee(Integer id, String user) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employee.deactivate();
        employeeRepository.save(employee);
    }

    public Set<EmployeeResponse.EmployeeRoleInfo> getEmployeeRoles(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return employee.getEmployeeRoles().stream()
                .map(er -> new EmployeeResponse.EmployeeRoleInfo(
                        er.getRole().getId(),
                        er.getRole().getCode(),
                        er.getRole().getName(),
                        er.getStatus()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Page<EmployeeSummaryResponse> getEmployeesByDepartment(
            Integer deptId, Pageable pageable) {
        return employeeRepository.findByDepartmentId(deptId, pageable).map(this::mapToSummaryResponse);
    }

    public EmployeeStatisticsResponse getEmployeeStatistics() {
        long total = employeeRepository.count();
        long active = employeeRepository.countByStatus(1);
        long inactive = employeeRepository.countByStatus(0);

        Map<String, Integer> byDept = new HashMap<>();
        for (Object[] row : employeeRepository.countEmployeesByDepartment()) {
            Integer deptId = (Integer) row[0];
            Long count = (Long) row[1];
            String name = deptId != null ? departmentRepository.findById(deptId).map(Department::getName).orElse("Unknown") : "Unknown";
            byDept.put(name, count.intValue());
        }

        Map<String, Integer> byLoc = new HashMap<>();
        for (Object[] row : employeeRepository.countEmployeesByLocation()) {
            Integer locId = (Integer) row[0];
            Long count = (Long) row[1];
            String name = locId != null ? companyLocationRepository.findById(locId).map(l -> String.valueOf(l.getLocationId())).orElse("Unknown") : "Unknown";
            byLoc.put(name, count.intValue());
        }

        Map<String, Integer> byDesig = new HashMap<>();
        for (Object[] row : employeeRepository.countEmployeesByDesignation()) {
            String desig = (String) row[0];
            Long count = (Long) row[1];
            byDesig.put(desig != null ? desig : "Unknown", count.intValue());
        }

        return new EmployeeStatisticsResponse(total, active, inactive, byDept, byLoc, byDesig);
    }

    // ===== BACKWARD COMPATIBILITY FOR CONTROLLER =====
    public EmployeeResponse updateEmployee(
            Integer id,
            UpdateEmployeeRequest request,
            String modifiedBy) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        employee.setFullName(request.fullName());
        employee.setDesignation(request.designation());
        employee.setCostCenter(request.costCenter());
        employee.setDepartmentId(request.departmentId());
        employee.setLocationId(request.locationId());
        employee.setPlant(request.plant());
        employee.setLastModifiedDate(LocalDate.now());

        employee = employeeRepository.save(employee);
        return mapToEmployeeResponse(employee);
    }

    /* ================= READ ================= */

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return mapToEmployeeResponse(employee);
    }

    /* ================= MAPPERS ================= */

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {

        Department department = employee.getDepartmentId() != null
                ? departmentRepository.findById(employee.getDepartmentId()).orElse(null)
                : null;

        CompanyLocation location = employee.getLocationId() != null
                ? companyLocationRepository.findById(employee.getLocationId()).orElse(null)
                : null;

        Set<EmployeeResponse.EmployeeRoleInfo> roles = employee.getEmployeeRoles().stream()
                .map(er -> new EmployeeResponse.EmployeeRoleInfo(
                        er.getRole().getId(),
                        er.getRole().getCode(),
                        er.getRole().getName(),
                        er.getStatus()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new EmployeeResponse(
                employee.getEmployeeNumber(),
                employee.getEmployeeId(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getJoinDate(),
                employee.getDesignation(),
                employee.getCostCenter(),
                employee.getPlant(),
                employee.getStatus(),
                employee.isActive() ? "Active" : "Inactive",
                employee.getLastModifiedDate(),
                department != null ? department.getId() : null,
                department != null ? department.getName() : null,
                location != null ? location.getId() : null,
                location != null ? String.valueOf(location.getLocationId()) : null,
                null,
                roles);
    }

    private EmployeeSummaryResponse mapToSummaryResponse(Employee employee) {

        Department department = employee.getDepartmentId() != null
                ? departmentRepository.findById(employee.getDepartmentId()).orElse(null)
                : null;

        CompanyLocation location = employee.getLocationId() != null
                ? companyLocationRepository.findById(employee.getLocationId()).orElse(null)
                : null;

        long activeRoles = employee.getEmployeeRoles().stream()
                .filter(er -> er.getStatus() == 1)
                .count();

        return new EmployeeSummaryResponse(
                employee.getEmployeeNumber(),
                employee.getEmployeeId(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getDesignation(),
                employee.getStatus(),
                employee.isActive() ? "Active" : "Inactive",
                department != null ? department.getName() : null,
                location != null ? String.valueOf(location.getLocationId()) : null,
                (int) activeRoles);
    }
}