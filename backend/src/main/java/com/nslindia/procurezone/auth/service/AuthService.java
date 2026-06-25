package com.nslindia.procurezone.auth.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.auth.dto.AuthenticatedUser;
import com.nslindia.procurezone.repository.CompanyEmployeeRepository;
import com.nslindia.procurezone.auth.dto.LoginRequest;
import com.nslindia.procurezone.auth.dto.LoginResponse;
import com.nslindia.procurezone.auth.dto.LogoutResponse;
import com.nslindia.procurezone.auth.exception.InactiveUserException;
import com.nslindia.procurezone.auth.exception.InvalidCredentialsException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.common.web.RequestUtils;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.identity.EmployeeRole;
import com.nslindia.procurezone.identity.Role;
import com.nslindia.procurezone.security.JwtService;
import com.nslindia.procurezone.security.PasswordService;
import com.nslindia.procurezone.security.PasswordService.PasswordVerificationResult;
import com.nslindia.procurezone.security.TokenBlacklistService;
import com.nslindia.procurezone.security.UserPrincipal;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final EmployeeRepository employeeRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuditService auditService;
    private final CompanyEmployeeRepository companyEmployeeRepository;

    public AuthService(EmployeeRepository employeeRepository, PasswordService passwordService,
            JwtService jwtService, TokenBlacklistService tokenBlacklistService, AuditService auditService,
            CompanyEmployeeRepository companyEmployeeRepository) {
        this.employeeRepository = employeeRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.auditService = auditService;
        this.companyEmployeeRepository = companyEmployeeRepository;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        String ipAddress = RequestUtils.getCurrentClientIpAddress();

        if (!StringUtils.hasText(username)) {
            auditService.logAuthentication(username, false, ipAddress);
            throw new InvalidCredentialsException();
        }

        try {
            // Load Employee directly by email (case-insensitive).
            // Source: tbl_emp_master.emp_email  (replaces tbl_user_master.user_name lookup)
            Employee employee = employeeRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> {
                        auditService.logAuthentication(username, false, ipAddress);
                        return new InvalidCredentialsException();
                    });

            // Single status check — employee active status only.
            // Source: tbl_emp_master.emp_status == 1
            if (!employee.isActive()) {
                auditService.logAuthentication(username, false, ipAddress);
                throw new InactiveUserException();
            }

            // Verify password against emp_password (MD5).
            // Source: tbl_emp_master.emp_password
            // PasswordService.verifyPassword() detects MD5 vs BCrypt automatically.
            String storedHash = employee.getLegacyPasswordHash();
            PasswordVerificationResult verification = passwordService.verifyPassword(request.password(), storedHash);
            if (!verification.successful()) {
                auditService.logAuthentication(username, false, ipAddress);
                log.warn("Invalid password for employee: {}", username);
                throw new InvalidCredentialsException();
            }
            if (verification.legacyMatch()) {
                log.debug("MD5 password match for employee {}", username);
            }

            // Role aggregation — identical logic, source unchanged: tbl_map_emp_roles + tbl_roles_master
            Set<Role> activeRoles = employee.getEmployeeRoles().stream()
                    .filter(er -> er.isActive())
                    .map(EmployeeRole::getRole)
                    .filter(Objects::nonNull)
                    .filter(role -> role.isActive())
                    .collect(Collectors.toSet());

            Set<String> roleCodes = activeRoles.stream()
                    .map(Role::getCode)
                    .map(com.nslindia.procurezone.security.RoleNormalizer::normalize)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            boolean canView   = activeRoles.stream().anyMatch(r -> "1".equals(r.getCanView())   || "true".equalsIgnoreCase(r.getCanView()));
            boolean canAdd    = activeRoles.stream().anyMatch(r -> "1".equals(r.getCanAdd())    || "true".equalsIgnoreCase(r.getCanAdd()));
            boolean canEdit   = activeRoles.stream().anyMatch(r -> "1".equals(r.getCanEdit())   || "true".equalsIgnoreCase(r.getCanEdit()));
            boolean canDelete = activeRoles.stream().anyMatch(r -> "1".equals(r.getCanDelete()) || "true".equalsIgnoreCase(r.getCanDelete()));

            // userId = emp_number as Long.
            // Replaces tbl_user_master.user_id. JWT "uid" claim carries emp_number.
            Long userId = employee.getEmployeeNumber().longValue();

            // Load company IDs from tbl_map_company_emp for JWT claim
            List<Integer> companyIds = companyEmployeeRepository
                    .findCompanyIdsByEmpNumber(employee.getEmployeeNumber());

            UserPrincipal principal = new UserPrincipal(
                    userId,
                    employee.getEmployeeNumber(),
                    employee.getEmployeeId(),
                    employee.getEmail(),        // username = emp_email
                    employee.getFullName(),
                    employee.getEmail(),
                    roleCodes,
                    canView,
                    canAdd,
                    canEdit,
                    canDelete,
                    employee.getDepartmentId(),
                    employee.getLocationId(),
                    companyIds);

            JwtService.AccessToken accessToken = jwtService.generateAccessToken(principal);

            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    employee.getEmployeeNumber(),
                    employee.getEmployeeId(),
                    employee.getFullName(),
                    employee.getEmail(),
                    roleCodes,
                    canView,
                    canAdd,
                    canEdit,
                    canDelete);

            auditService.logAuthentication(username, true, ipAddress);
            log.info("Successful login for employee: {} from IP: {}", username, ipAddress);

            return LoginResponse.bearer(accessToken.token(), accessToken.expiresAt(), authenticatedUser);
        } catch (InvalidCredentialsException | InactiveUserException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for employee: {}", username, e);
            auditService.logAuthentication(username, false, ipAddress);
            throw new InvalidCredentialsException();
        }
    }

    /**
     * Change the password for the currently authenticated employee.
     * Verifies current password (MD5 or BCrypt), then re-encodes new password as BCrypt
     * and stores it in tbl_emp_master.emp_password.
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + email));

        PasswordVerificationResult verification = passwordService.verifyPassword(
                currentPassword, employee.getLegacyPasswordHash());
        if (!verification.successful()) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        employee.setLegacyPasswordHash(passwordService.encodePassword(newPassword));
        employeeRepository.save(employee);
        log.info("Password changed successfully for employee: {}", email);
    }

    /**
     * Logout user by blacklisting their JWT token.
     * 
     * @param token    the JWT token to blacklist
     * @param userId   the user ID logging out
     * @param username the username of the user logging out (for logging)
     * @return logout response
     */
    public LogoutResponse logout(String token, Integer userId, String username) {
        String ipAddress = RequestUtils.getCurrentClientIpAddress();

        // Parse token to get expiration time
        jwtService.parseAccessToken(token).ifPresent(payload -> {
            tokenBlacklistService.blacklistToken(token, payload.expiresAt());
            log.info("User {} logged out successfully from IP: {}", username, ipAddress);
        });

        // Log logout event
        auditService.logLogout(username, userId, ipAddress);

        return LogoutResponse.success();
    }

}
