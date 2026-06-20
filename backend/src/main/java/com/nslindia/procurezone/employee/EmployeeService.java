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

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.security.UserPrincipal;
import com.nslindia.procurezone.employee.dto.AssignRoleRequest;
import com.nslindia.procurezone.employee.dto.CreateEmployeeRequest;
import com.nslindia.procurezone.employee.dto.EmployeeResponse;
import com.nslindia.procurezone.employee.dto.EmployeeStatisticsResponse;
import com.nslindia.procurezone.employee.dto.EmployeeSummaryResponse;
import com.nslindia.procurezone.employee.dto.UpdateEmployeeRequest;
import com.nslindia.procurezone.entity.EmployeeReporting;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.identity.EmployeeRole;
import com.nslindia.procurezone.identity.EmployeeRoleRepository;
import com.nslindia.procurezone.identity.Role;
import com.nslindia.procurezone.identity.RoleRepository;
import com.nslindia.procurezone.identity.UserAccount;
import com.nslindia.procurezone.identity.UserRepository;
import com.nslindia.procurezone.entity.CompanyEmployee;
import com.nslindia.procurezone.masterdata.Company;
import com.nslindia.procurezone.masterdata.Department;
import com.nslindia.procurezone.masterdata.Location;
import com.nslindia.procurezone.masterdata.repository.CompanyRepository;
import com.nslindia.procurezone.masterdata.repository.DepartmentRepository;
import com.nslindia.procurezone.masterdata.LocationRepository;
import com.nslindia.procurezone.repository.CompanyEmployeeRepository;
import com.nslindia.procurezone.repository.EmployeeReportingRepository;
import org.springframework.data.domain.PageRequest;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeReportingRepository employeeReportingRepository;
    private final CompanyEmployeeRepository companyEmployeeRepository;
    private final CompanyRepository companyRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmployeeRoleRepository employeeRoleRepository,
            DepartmentRepository departmentRepository,
            LocationRepository locationRepository,
            PasswordEncoder passwordEncoder,
            EmployeeReportingRepository employeeReportingRepository,
            CompanyEmployeeRepository companyEmployeeRepository,
            CompanyRepository companyRepository) {

        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeRoleRepository = employeeRoleRepository;
        this.departmentRepository = departmentRepository;
        this.locationRepository = locationRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeReportingRepository = employeeReportingRepository;
        this.companyEmployeeRepository = companyEmployeeRepository;
        this.companyRepository = companyRepository;
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
                !locationRepository.existsById(request.locationId())) {
            throw new ResourceNotFoundException("Location", "id", request.locationId());
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

        // Store MD5 password in emp_password for local (Non-LDAP) authentication.
        // LDAP users: store empty string — authentication is handled externally.
        // This ensures emp_password is never null (NOT NULL column constraint).
        if (StringUtils.hasText(request.password())) {
            String md5 = DigestUtils.md5DigestAsHex(
                    request.password().getBytes(StandardCharsets.UTF_8));
            employee.setLegacyPasswordHash(md5);
        } else {
            employee.setLegacyPasswordHash(""); // LDAP — no local password
        }

        employee = employeeRepository.save(employee);

        // Persist company mapping in tbl_map_company_emp
        if (request.companyId() != null && request.companyId() > 0) {
            CompanyEmployee mapping = new CompanyEmployee();
            mapping.setCompanyId(request.companyId());
            mapping.setEmployeeNumber(employee.getEmployeeNumber());
            mapping.setStatus(1);
            mapping.setLastModifiedDate(LocalDate.now());
            mapping.setLastModifiedBy(employee.getEmployeeNumber());
            companyEmployeeRepository.save(mapping);
        }

        // Create UserAccount for backwards compatibility (tbl_user_master — unused for auth).
        if (StringUtils.hasText(request.password())) {
            UserAccount user = new UserAccount();
            user.setUsername(request.email());
            user.setPasswordHash(passwordEncoder.encode(request.password()));
            user.setEmployee(employee);
            user.setStatus(1);
            user.setLastModifiedDate(LocalDate.now());
            user.setLastModifiedUser(employee.getEmployeeNumber());
            userRepository.save(user);
        }

        // Assign roles to tbl_map_emp_roles
        if (request.roleIds() != null) {
            boolean cuIsSuperAdmin = isCurrentUserSuperAdmin();
            for (Integer roleId : request.roleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
                if ("Super Admin".equals(role.getCode()) && !cuIsSuperAdmin) {
                    throw new AccessDeniedException("Only SUPERADMIN can assign the Super Admin role");
                }
                EmployeeRole er = new EmployeeRole();
                er.setEmployee(employee);
                er.setRole(role);
                er.setStatus(1);
                er.setLastModifiedDate(LocalDateTime.now());
                er.setLastModifiedUser(createdBy);
                employeeRoleRepository.save(er);
            }
        }

        // Create reporting relationship in tbl_map_emp_reporting
        if (request.reportingManagerId() != null && request.reportingManagerId() > 0) {
            EmployeeReporting reporting = new EmployeeReporting();
            reporting.setSubordinateEmployeeNumber(employee.getEmployeeNumber());
            reporting.setSupervisorEmployeeNumber(request.reportingManagerId());
            reporting.setEffectiveDate(LocalDate.now());
            reporting.setEndDate(LocalDate.of(2099, 12, 31));
            reporting.setStatus(1);
            reporting.setLastModifiedDate(LocalDate.now());
            reporting.setLastModifiedBy(employee.getEmployeeNumber());
            employeeReportingRepository.save(reporting);
        }

        return mapToEmployeeResponse(employee);
    }

    // ===== BACKWARD COMPATIBILITY FOR CONTROLLERS =====

    public Page<EmployeeSummaryResponse> getAllEmployees(
            Integer status, String search, Pageable pageable) {
        return getAllEmployees(status, search, null, null, pageable);
    }

    public Page<EmployeeSummaryResponse> getAllEmployees(
            Integer status, String search, Integer departmentId, Integer locationId, Pageable pageable) {
        if (StringUtils.hasText(search) || status != null || departmentId != null || locationId != null) {
            String searchTerm = StringUtils.hasText(search) ? search : null;
            return employeeRepository.filterEmployees(searchTerm, status, departmentId, locationId, pageable)
                    .map(this::mapToSummaryResponse);
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
            String name = locId != null ? locationRepository.findById(locId).map(Location::getName).orElse("Unknown") : "Unknown";
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

        if (request.fullName() != null)    employee.setFullName(request.fullName());
        if (request.designation() != null) employee.setDesignation(request.designation());
        if (request.costCenter() != null)  employee.setCostCenter(request.costCenter());
        if (request.departmentId() != null) employee.setDepartmentId(request.departmentId());
        if (request.locationId() != null)  employee.setLocationId(request.locationId());
        if (request.plant() != null)       employee.setPlant(request.plant());
        // email: update emp_email if provided (LDAP/Non-LDAP identifier change)
        if (request.email() != null && !request.email().isBlank()) {
            employee.setEmail(request.email());
        }
        employee.setLastModifiedDate(LocalDate.now());

        employee = employeeRepository.save(employee);
        final Integer savedEmpNumber = employee.getEmployeeNumber();

        // Update company mapping: null = no change; 0 = remove; positive = replace.
        if (request.companyId() != null) {
            companyEmployeeRepository.findCompaniesByEmployee(
                    savedEmpNumber, PageRequest.of(0, 100))
                .forEach(ce -> {
                    ce.setStatus(0);
                    ce.setLastModifiedDate(LocalDate.now());
                    ce.setLastModifiedBy(savedEmpNumber);
                    companyEmployeeRepository.save(ce);
                });
            if (request.companyId() > 0) {
                CompanyEmployee mapping = new CompanyEmployee();
                mapping.setCompanyId(request.companyId());
                mapping.setEmployeeNumber(employee.getEmployeeNumber());
                mapping.setStatus(1);
                mapping.setLastModifiedDate(LocalDate.now());
                mapping.setLastModifiedBy(employee.getEmployeeNumber());
                companyEmployeeRepository.save(mapping);
            }
        }

        // Role replace-all (Option A): null = no change, empty = remove all, list = replace.
        if (request.roleIds() != null) {
            List<EmployeeRole> existing = employeeRoleRepository
                    .findByEmployee_EmployeeNumber(employee.getEmployeeNumber());
            employeeRoleRepository.deleteAll(existing);

            boolean cuIsSuperAdmin = isCurrentUserSuperAdmin();
            for (Integer roleId : request.roleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
                if ("Super Admin".equals(role.getCode()) && !cuIsSuperAdmin) {
                    throw new AccessDeniedException("Only SUPERADMIN can assign the Super Admin role");
                }
                EmployeeRole er = new EmployeeRole();
                er.setEmployee(employee);
                er.setRole(role);
                er.setStatus(1);
                er.setLastModifiedDate(LocalDateTime.now());
                er.setLastModifiedUser(modifiedBy);
                employeeRoleRepository.save(er);
            }
        }

        // Reporting manager update: null = no change, 0 = remove, positive = set/replace.
        if (request.reportingManagerId() != null) {
            // Deactivate any existing active reporting relationship for this employee
            employeeReportingRepository.findActiveSupervisor(employee.getEmployeeNumber())
                    .ifPresent(existing -> {
                        existing.setStatus(0);
                        existing.setLastModifiedDate(LocalDate.now());
                        employeeReportingRepository.save(existing);
                    });

            // Create new relationship if a valid manager is provided
            if (request.reportingManagerId() > 0) {
                EmployeeReporting reporting = new EmployeeReporting();
                reporting.setSubordinateEmployeeNumber(employee.getEmployeeNumber());
                reporting.setSupervisorEmployeeNumber(request.reportingManagerId());
                reporting.setEffectiveDate(LocalDate.now());
                reporting.setEndDate(LocalDate.of(2099, 12, 31));
                reporting.setStatus(1);
                reporting.setLastModifiedDate(LocalDate.now());
                reporting.setLastModifiedBy(employee.getEmployeeNumber());
                employeeReportingRepository.save(reporting);
            }
        }

        return mapToEmployeeResponse(employee);
    }

    /* ================= READ ================= */

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return mapToEmployeeResponse(employee);
    }

    /* ================= HELPERS ================= */

    private boolean isCurrentUserSuperAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal cu)) return false;
        return cu.roles().contains("SUPERADMIN");
    }

    /* ================= MAPPERS ================= */

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {

        Department department = employee.getDepartmentId() != null
                ? departmentRepository.findById(employee.getDepartmentId()).orElse(null)
                : null;

        Location location = employee.getLocationId() != null
                ? locationRepository.findById(employee.getLocationId()).orElse(null)
                : null;

        // Company lookup via tbl_map_company_emp junction table
        Integer companyId = null;
        String companyName = null;
        var companyPage = companyEmployeeRepository.findCompaniesByEmployee(
                employee.getEmployeeNumber(), PageRequest.of(0, 1));
        if (!companyPage.isEmpty()) {
            companyId = companyPage.getContent().get(0).getCompanyId();
            Company company = companyRepository.findById(companyId).orElse(null);
            if (company != null) companyName = company.getName();
        }

        Set<EmployeeResponse.EmployeeRoleInfo> roles = employee.getEmployeeRoles().stream()
                .map(er -> new EmployeeResponse.EmployeeRoleInfo(
                        er.getRole().getId(),
                        er.getRole().getCode(),
                        er.getRole().getName(),
                        er.getStatus()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Look up reporting manager via tbl_map_emp_reporting
        Integer reportingManagerId = employeeReportingRepository
                .findSupervisorNumber(employee.getEmployeeNumber()).orElse(null);
        String reportingManagerName = reportingManagerId != null
                ? employeeRepository.findById(reportingManagerId)
                        .map(Employee::getFullName).orElse(null)
                : null;

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
                location != null ? location.getName() : null,
                companyId,
                companyName,
                roles,
                reportingManagerId,
                reportingManagerName);
    }

    private EmployeeSummaryResponse mapToSummaryResponse(Employee employee) {

        Department department = employee.getDepartmentId() != null
                ? departmentRepository.findById(employee.getDepartmentId()).orElse(null)
                : null;

        Location location = employee.getLocationId() != null
                ? locationRepository.findById(employee.getLocationId()).orElse(null)
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
                location != null ? location.getName() : null,
                (int) activeRoles);
    }

    /* ================= PASSWORD RESET ================= */

    public void resetPassword(Integer employeeId, String newPassword) {
        UserAccount userAccount = userRepository.findByEmployee_EmployeeNumber(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user account found for employee ID: " + employeeId));
        userAccount.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccount.setLastModifiedDate(java.time.LocalDate.now());
        userRepository.save(userAccount);
    }
}