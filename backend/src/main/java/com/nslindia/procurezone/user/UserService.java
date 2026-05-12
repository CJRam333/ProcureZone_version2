package com.nslindia.procurezone.user;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nslindia.procurezone.common.exception.BadRequestException;
import com.nslindia.procurezone.common.exception.ResourceNotFoundException;
import com.nslindia.procurezone.identity.Employee;
import com.nslindia.procurezone.identity.EmployeeRepository;
import com.nslindia.procurezone.identity.UserAccount;
import com.nslindia.procurezone.identity.UserRepository;
import com.nslindia.procurezone.security.PasswordService;
import com.nslindia.procurezone.user.dto.ChangePasswordRequest;
import com.nslindia.procurezone.user.dto.CreateUserRequest;
import com.nslindia.procurezone.user.dto.ResetPasswordRequest;
import com.nslindia.procurezone.user.dto.UpdateProfileRequest;
import com.nslindia.procurezone.user.dto.UserResponse;
import com.nslindia.procurezone.user.dto.UserSummaryResponse;

/**
 * Service for managing user accounts.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;

    public UserService(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            PasswordService passwordService) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordService = passwordService;
    }

    /**
     * Creates a new user account.
     */
    public UserResponse createUser(CreateUserRequest request, Integer createdBy) {
        // Validate username uniqueness
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already exists: " + request.username());
        }

        // Validate employee exists
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Employee", "id", request.employeeId()));

        // Check if employee already has a user account
        if (userRepository.existsByEmployee_EmployeeNumber(request.employeeId())) {
            throw new BadRequestException("User account already exists for this employee");
        }

        // Create user account
        UserAccount user = new UserAccount();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmployee(employee);
        user.setStatus(1); // Active
        user.setLastModifiedDate(LocalDate.now());
        user.setLastModifiedUser(createdBy);

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    /**
     * Updates user profile.
     */
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request, Integer modifiedBy) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update username if provided
        if (request.username() != null) {
            // Check username uniqueness
            if (!request.username().equals(user.getUsername()) &&
                    userRepository.existsByUsername(request.username())) {
                throw new BadRequestException("Username already exists: " + request.username());
            }
            user.setUsername(request.username());
        }

        user.setLastModifiedDate(LocalDate.now());
        user.setLastModifiedUser(modifiedBy);

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    /**
     * Changes user password.
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify current password (supports both MD5 legacy and BCrypt)
        PasswordService.PasswordVerificationResult verification = passwordService
                .verifyPassword(request.currentPassword(), user.getPasswordHash());

        if (!verification.successful()) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Verify new password and confirm password match
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Update password with BCrypt
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setLastModifiedDate(LocalDate.now());
        user.setLastModifiedUser(user.getEmployee().getEmployeeNumber());

        userRepository.save(user);
    }

    /**
     * Resets user password (admin function).
     */
    public void resetPassword(ResetPasswordRequest request, Integer resetBy) {
        UserAccount user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setLastModifiedDate(LocalDate.now());
        user.setLastModifiedUser(resetBy);

        userRepository.save(user);
    }

    /**
     * Gets user profile by ID.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return mapToUserResponse(user);
    }

    /**
     * Gets all users with optional status filter and pagination.
     */
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> getAllUsers(Integer status, Pageable pageable) {
        Page<UserAccount> users;
        if (status != null) {
            users = userRepository.findByStatus(status, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::mapToSummaryResponse);
    }

    /**
     * Locks a user account (sets status to 0).
     */
    public void lockAccount(Long userId, Integer lockedBy) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setStatus(0); // Locked/Inactive
        user.setLastModifiedDate(LocalDate.now());
        user.setLastModifiedUser(lockedBy);

        userRepository.save(user);
    }

    /**
     * Unlocks a user account (sets status to 1).
     */
    public void unlockAccount(Long userId, Integer unlockedBy) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setStatus(1); // Active
        user.setLastModifiedDate(LocalDate.now());
        user.setLastModifiedUser(unlockedBy);

        userRepository.save(user);
    }

    /**
     * Deletes a user account.
     */
    public void deleteUser(Long userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userRepository.delete(user);
    }

    /**
     * Maps UserAccount entity to UserResponse DTO.
     */
    private UserResponse mapToUserResponse(UserAccount user) {
        Employee employee = user.getEmployee();

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                employee.getEmployeeNumber(),
                employee.getEmployeeId(),
                employee.getFullName(),
                employee.getEmail(),
                user.getStatus(),
                user.isActive() ? "Active" : "Locked",
                user.getLastLoginIp(),
                user.getLastModifiedDate());
    }

    /**
     * Maps UserAccount entity to UserSummaryResponse DTO.
     */
    private UserSummaryResponse mapToSummaryResponse(UserAccount user) {
        Employee employee = user.getEmployee();

        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                employee.getEmployeeNumber(),
                employee.getFullName(),
                employee.getEmail(),
                user.getStatus(),
                user.isActive() ? "Active" : "Locked");
    }
}
