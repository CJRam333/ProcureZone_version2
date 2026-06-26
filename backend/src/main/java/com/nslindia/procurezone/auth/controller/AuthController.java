package com.nslindia.procurezone.auth.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nslindia.procurezone.auth.dto.AuthenticatedUser;
import com.nslindia.procurezone.auth.dto.ChangePasswordRequest;
import com.nslindia.procurezone.auth.dto.LoginRequest;
import com.nslindia.procurezone.auth.dto.LoginResponse;
import com.nslindia.procurezone.auth.dto.LogoutResponse;
import com.nslindia.procurezone.auth.service.AuthService;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.masterdata.LocationRepository;
import com.nslindia.procurezone.masterdata.repository.CompanyRepository;
import com.nslindia.procurezone.masterdata.repository.DepartmentRepository;
import com.nslindia.procurezone.repository.CompanyEmployeeRepository;
import com.nslindia.procurezone.security.UserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private CompanyEmployeeRepository companyEmployeeRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private LocationRepository locationRepository;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.username());
        try {
            LoginResponse response = authService.login(request);
            log.info("Login successful for username: {}", request.username());
            return response;
        } catch (Exception e) {
            log.error("Login failed for username: {} - Error: {}", request.username(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public LogoutResponse logout(Authentication authentication,
            @org.springframework.web.bind.annotation.RequestHeader("Authorization") String authHeader) {
        log.info("Logout request received");
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            log.warn("Logout attempt with no authentication");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        // Extract token from "Bearer <token>"
        String token = authHeader.substring(7);

        log.info("Processing logout for user: {}", principal.username());
        LogoutResponse response = authService.logout(token, principal.userId().intValue(), principal.username());
        log.info("Logout successful for user: {}", principal.username());
        return response;
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @jakarta.validation.Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        authService.changePassword(principal.email(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public AuthenticatedUser me(Authentication authentication) {
        log.debug("Get current user request received");
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            log.warn("Get current user request with no authentication");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        log.debug("Returning current user info for: {}", principal.username());
        String deptName = null;
        String companyName = null;
        String locName = null;
        try {
            if (principal.employeeNumber() != null) {
                var emp = employeeRepository.findByEmployeeNumber(principal.employeeNumber()).orElse(null);
                if (emp != null) {
                    if (emp.getDepartmentId() != null)
                        deptName = departmentRepository.findById(emp.getDepartmentId())
                                .map(d -> d.getName()).orElse(null);
                    if (emp.getLocationId() != null)
                        locName = locationRepository.findById(emp.getLocationId())
                                .map(l -> l.getName()).orElse(null);
                    List<Integer> cids = companyEmployeeRepository.findCompanyIdsByEmpNumber(principal.employeeNumber());
                    if (!cids.isEmpty())
                        companyName = companyRepository.findById(cids.get(0))
                                .map(c -> c.getName()).orElse(null);
                }
            }
        } catch (Exception ex) {
            log.warn("Could not enrich /auth/me with dept/company/location: {}", ex.getMessage());
        }
        return new AuthenticatedUser(
                principal.userId(),
                principal.employeeNumber(),
                principal.employeeId(),
                principal.displayName(),
                principal.email(),
                principal.roles(),
                principal.canView(),
                principal.canAdd(),
                principal.canEdit(),
                principal.canDelete(),
                deptName,
                companyName,
                locName);
    }
}
