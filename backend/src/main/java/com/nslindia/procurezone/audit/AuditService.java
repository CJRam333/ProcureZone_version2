package com.nslindia.procurezone.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for creating and managing audit log entries.
 * All audit operations are asynchronous to avoid blocking main application
 * flow.
 */
@Service
@Transactional
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Log authentication attempt (login).
     * 
     * @param username  username attempting to log in
     * @param success   whether login was successful
     * @param ipAddress IP address of the request
     */
    @Async
    public void logAuthentication(String username, boolean success, String ipAddress) {
        try {
            String action = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
            String status = success ? "SUCCESS" : "FAILED";
            String details = success
                    ? String.format("User %s logged in successfully", username)
                    : String.format("Failed login attempt for user %s", username);

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType("USER")
                    .userId(0) // Use 0 for authentication events (no user context yet)
                    .username(username)
                    .ipAddress(ipAddress)
                    .details(details)
                    .status(status)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Logged authentication attempt for user: {}, success: {}", username, success);
        } catch (Exception e) {
            log.error("Failed to log authentication for user: {}", username, e);
        }
    }

    /**
     * Log logout event.
     * 
     * @param username  username logging out
     * @param userId    user ID logging out
     * @param ipAddress IP address of the request
     */
    @Async
    public void logLogout(String username, Integer userId, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action("LOGOUT")
                    .entityType("USER")
                    .userId(userId)
                    .username(username)
                    .ipAddress(ipAddress)
                    .details(String.format("User %s logged out", username))
                    .status("SUCCESS")
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Logged logout for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to log logout for user: {}", username, e);
        }
    }

    /**
     * Log entity change (create, update, delete).
     * 
     * @param action     action performed (CREATE, UPDATE, DELETE)
     * @param entityType type of entity (INDENT, MATERIAL, etc.)
     * @param entityId   ID of the entity
     * @param userId     user ID who performed the action
     * @param username   username who performed the action
     * @param details    additional details about the change
     */
    @Async
    public void logEntityChange(String action, String entityType, Integer entityId,
            Integer userId, String username, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId != null ? String.valueOf(entityId) : null)
                    .userId(userId)
                    .username(username)
                    .details(details)
                    .status("SUCCESS")
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Logged {} action on {} {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to log entity change for {} {}", entityType, entityId, e);
        }
    }

    /**
     * Log security event (token validation failure, suspicious activity, etc.).
     * 
     * @param eventType type of security event
     * @param details   details about the event
     * @param username  username involved (if available)
     * @param ipAddress IP address involved
     */
    @Async
    public void logSecurityEvent(String eventType, String details, String username, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(eventType)
                    .entityType("SECURITY_EVENT")
                    .username(username)
                    .ipAddress(ipAddress)
                    .details(details)
                    .status("WARNING")
                    .build();

            auditLogRepository.save(auditLog);
            log.warn("Security event logged: {} - {}", eventType, details);
        } catch (Exception e) {
            log.error("Failed to log security event: {}", eventType, e);
        }
    }

    /**
     * Log general system event.
     * 
     * @param action  action/event name
     * @param details event details
     * @param status  status (SUCCESS, FAILED, INFO)
     */
    @Async
    public void logSystemEvent(String action, String details, String status) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType("SYSTEM")
                    .details(details)
                    .status(status)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("System event logged: {}", action);
        } catch (Exception e) {
            log.error("Failed to log system event: {}", action, e);
        }
    }

    // ============== Unit of Measure Audit Methods ==============

    /**
     * Log unit of measure creation.
     * 
     * @param unitOfMeasureId ID of the created unit of measure
     * @param code            Code of the unit of measure
     * @param userId          ID of the user who created the unit of measure
     * @param username        Username of the user who created the unit of measure
     */
    @Async
    public void logUnitOfMeasureCreated(Integer unitOfMeasureId, String code, Integer userId, String username) {
        try {
            String details = String.format("Unit of measure created: %s (ID: %d)", code, unitOfMeasureId);
            logEntityChange("CREATE", "UnitOfMeasure", unitOfMeasureId, userId, username, details);
            log.debug("Logged unit of measure creation: {} by user: {}", code, username);
        } catch (Exception e) {
            log.error("Failed to log unit of measure creation for code: {}", code, e);
        }
    }

    /**
     * Log unit of measure update.
     * 
     * @param unitOfMeasureId ID of the updated unit of measure
     * @param code            Code of the unit of measure
     * @param oldDetails      Old values before update
     * @param newDetails      New values after update
     * @param userId          ID of the user who updated the unit of measure
     * @param username        Username of the user who updated the unit of measure
     */
    @Async
    public void logUnitOfMeasureUpdated(Integer unitOfMeasureId, String code, String oldDetails,
            String newDetails, Integer userId, String username) {
        try {
            String details = String.format("Unit of measure %s (ID: %d) updated. Before: [%s]. After: [%s]",
                    code, unitOfMeasureId, oldDetails, newDetails);
            logEntityChange("UPDATE", "UnitOfMeasure", unitOfMeasureId, userId, username, details);
            log.debug("Logged unit of measure update: {} by user: {}", code, username);
        } catch (Exception e) {
            log.error("Failed to log unit of measure update for code: {}", code, e);
        }
    }

    /**
     * Log unit of measure deletion (soft delete).
     * 
     * @param unitOfMeasureId ID of the deleted unit of measure
     * @param code            Code of the unit of measure
     * @param userId          ID of the user who deleted the unit of measure
     * @param username        Username of the user who deleted the unit of measure
     */
    @Async
    public void logUnitOfMeasureDeleted(Integer unitOfMeasureId, String code, Integer userId, String username) {
        try {
            String details = String.format("Unit of measure deleted (soft): %s (ID: %d)", code, unitOfMeasureId);
            logEntityChange("DELETE", "UnitOfMeasure", unitOfMeasureId, userId, username, details);
            log.debug("Logged unit of measure deletion: {} by user: {}", code, username);
        } catch (Exception e) {
            log.error("Failed to log unit of measure deletion for code: {}", code, e);
        }
    }
}
