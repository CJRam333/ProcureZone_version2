package com.nslindia.procurezone.auth.service;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.nslindia.procurezone.audit.AuditService;
import com.nslindia.procurezone.auth.dto.AuthenticatedUser;
import com.nslindia.procurezone.auth.dto.LoginRequest;
import com.nslindia.procurezone.auth.dto.LoginResponse;
import com.nslindia.procurezone.auth.dto.LogoutResponse;
import com.nslindia.procurezone.auth.exception.InactiveUserException;
import com.nslindia.procurezone.auth.exception.InvalidCredentialsException;
import com.nslindia.procurezone.common.web.RequestUtils;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRole;
import com.nslindia.procurezone.identity.Role;
import com.nslindia.procurezone.identity.UserAccount;
import com.nslindia.procurezone.identity.UserAccountRepository;
import com.nslindia.procurezone.security.JwtService;
import com.nslindia.procurezone.security.PasswordService;
import com.nslindia.procurezone.security.PasswordService.PasswordVerificationResult;
import com.nslindia.procurezone.security.TokenBlacklistService;
import com.nslindia.procurezone.security.UserPrincipal;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuditService auditService;

    public AuthService(UserAccountRepository userAccountRepository, PasswordService passwordService,
            JwtService jwtService, TokenBlacklistService tokenBlacklistService, AuditService auditService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.auditService = auditService;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        String ipAddress = RequestUtils.getCurrentClientIpAddress();

        if (!StringUtils.hasText(username)) {
            auditService.logAuthentication(username, false, ipAddress);
            throw new InvalidCredentialsException();
        }

        try {
            UserAccount userAccount = userAccountRepository.findUserForLogin(username)
                    .orElseThrow(() -> {
                        auditService.logAuthentication(username, false, ipAddress);
                        return new InvalidCredentialsException();
                    });

            Employee employee = userAccount.getEmployee();
            if (employee == null || !userAccount.isActive() || !employee.isActive()) {
                auditService.logAuthentication(username, false, ipAddress);
                throw new InactiveUserException();
            }

            String storedHash = resolveStoredHash(userAccount);
            PasswordVerificationResult verification = passwordService.verifyPassword(request.password(), storedHash);
            if (!verification.successful()) {
                auditService.logAuthentication(username, false, ipAddress);
                log.warn("Invalid password for user: {}", username);
                throw new InvalidCredentialsException();
            }
            if (verification.legacyMatch()) {
                log.debug("Legacy MD5 password match detected for user {}", username);
            }

            Set<Role> activeRoles = employee.getEmployeeRoles().stream()
                    .filter(er -> er.isActive())
                    .map(EmployeeRole::getRole)
                    .filter(Objects::nonNull)
                    .filter(role -> role.isActive())
                    .collect(Collectors.toSet());

            Set<String> roleCodes = activeRoles.stream()
                    .map(Role::getCode)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            boolean canView = activeRoles.stream().anyMatch(r -> "1".equals(r.getCanView()) || "true".equalsIgnoreCase(r.getCanView()));
            boolean canAdd = activeRoles.stream().anyMatch(r -> "1".equals(r.getCanAdd()) || "true".equalsIgnoreCase(r.getCanAdd()));
            boolean canEdit = activeRoles.stream().anyMatch(r -> "1".equals(r.getCanEdit()) || "true".equalsIgnoreCase(r.getCanEdit()));
            boolean canDelete = activeRoles.stream().anyMatch(r -> "1".equals(r.getCanDelete()) || "true".equalsIgnoreCase(r.getCanDelete()));

            UserPrincipal principal = new UserPrincipal(
                    userAccount.getId(),
                    employee.getEmployeeNumber(),
                    employee.getEmployeeId(),
                    userAccount.getUsername(),
                    employee.getFullName(),
                    employee.getEmail(),
                    roleCodes,
                    canView,
                    canAdd,
                    canEdit,
                    canDelete);

            JwtService.AccessToken accessToken = jwtService.generateAccessToken(principal);

            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userAccount.getId(),
                    employee.getEmployeeNumber(),
                    employee.getEmployeeId(),
                    employee.getFullName(),
                    employee.getEmail(),
                    roleCodes,
                    canView,
                    canAdd,
                    canEdit,
                    canDelete);

            // Log successful login
            auditService.logAuthentication(username, true, ipAddress);
            log.info("Successful login for user: {} from IP: {}", username, ipAddress);

            return LoginResponse.bearer(accessToken.token(), accessToken.expiresAt(), authenticatedUser);
        } catch (InvalidCredentialsException | InactiveUserException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", username, e);
            auditService.logAuthentication(username, false, ipAddress);
            throw new InvalidCredentialsException();
        }
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

    private String resolveStoredHash(UserAccount userAccount) {
        if (StringUtils.hasText(userAccount.getPasswordHash())) {
            return userAccount.getPasswordHash();
        }
        Employee employee = userAccount.getEmployee();
        if (employee != null && StringUtils.hasText(employee.getLegacyPasswordHash())) {
            return employee.getLegacyPasswordHash();
        }
        return null;
    }
}
