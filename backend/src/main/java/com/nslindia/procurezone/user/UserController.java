package com.nslindia.procurezone.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nslindia.procurezone.security.UserPrincipal;
import com.nslindia.procurezone.user.dto.ChangePasswordRequest;
import com.nslindia.procurezone.user.dto.CreateUserRequest;
import com.nslindia.procurezone.user.dto.ResetPasswordRequest;
import com.nslindia.procurezone.user.dto.UpdateProfileRequest;
import com.nslindia.procurezone.user.dto.UserResponse;
import com.nslindia.procurezone.user.dto.UserSummaryResponse;

import jakarta.validation.Valid;

/**
 * REST controller for managing user accounts.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/v1/users - Create new user account.
     * Roles: ADMIN, SUPERADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        UserResponse response = userService.createUser(request, currentUser.employeeNumber());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/users/{id} - Update user profile.
     * Roles: ADMIN, SUPERADMIN, or the user themselves
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        UserResponse response = userService.updateProfile(id, request, currentUser.employeeNumber());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/users/{id}/change-password - Change password.
     * Roles: The user themselves or ADMIN/SUPERADMIN
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();

        // Non-admin users can only change their own password
        if (!currentUser.userId().equals(id) &&
                !authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                a.getAuthority().equals("ROLE_SUPERADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.changePassword(id, request);

        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/v1/users/reset-password - Reset user password (admin function).
     * Roles: ADMIN, SUPERADMIN
     */
    @PostMapping("/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        userService.resetPassword(request, currentUser.employeeNumber());

        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/v1/users/{id} - Get user profile by ID.
     * Roles: ADMIN, SUPERADMIN, VIEWER, or the user themselves
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'VIEWER') or #id == authentication.principal.userId")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/users - List all users with pagination and filtering.
     * Roles: ADMIN, SUPERADMIN, VIEWER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'VIEWER')")
    public ResponseEntity<Page<UserSummaryResponse>> getAllUsers(
            @RequestParam(required = false) Integer status,
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<UserSummaryResponse> response = userService.getAllUsers(status, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/users/{id}/lock - Lock user account.
     * Roles: ADMIN, SUPERADMIN
     */
    @PostMapping("/{id}/lock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> lockAccount(
            @PathVariable Long id,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        userService.lockAccount(id, currentUser.employeeNumber());

        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/v1/users/{id}/unlock - Unlock user account.
     * Roles: ADMIN, SUPERADMIN
     */
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> unlockAccount(
            @PathVariable Long id,
            Authentication authentication) {

        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        userService.unlockAccount(id, currentUser.employeeNumber());

        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/v1/users/{id} - Delete user account.
     * Roles: ADMIN, SUPERADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
