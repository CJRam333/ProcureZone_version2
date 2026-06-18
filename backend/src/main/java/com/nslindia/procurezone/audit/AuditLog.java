package com.nslindia.procurezone.audit;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing audit log entries for tracking system activities.
 * Stores information about user actions, security events, and system
 * operations.
 */
@Entity
@Table(name = "tbl_audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Integer id;

    @Column(name = "audit_action", nullable = false, length = 100)
    private String action;

    @Column(name = "audit_entity_type", length = 100)
    private String entityType;

    @Column(name = "audit_entity_id", length = 100)
    private String entityId;

    @Column(name = "audit_user_id")
    private Integer userId;

    @Column(name = "audit_username", length = 100)
    private String username;

    @Column(name = "audit_ip_address", length = 45)
    private String ipAddress;

    @Column(name = "audit_timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "audit_details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "audit_status", length = 50)
    private String status;

    protected AuditLog() {
    }

    private AuditLog(Builder builder) {
        this.action = builder.action;
        this.entityType = builder.entityType;
        this.entityId = builder.entityId;
        this.userId = builder.userId;
        this.username = builder.username;
        this.ipAddress = builder.ipAddress;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.details = builder.details;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public Integer getUserId() {
        return userId;
    }

    @JsonProperty("performedBy")
    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @JsonProperty("performedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }

    public String getStatus() {
        return status;
    }

    public static class Builder {
        private String action;
        private String entityType;
        private String entityId;
        private Integer userId;
        private String username;
        private String ipAddress;
        private Instant timestamp;
        private String details;
        private String status;

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder userId(Integer userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(this);
        }
    }
}
