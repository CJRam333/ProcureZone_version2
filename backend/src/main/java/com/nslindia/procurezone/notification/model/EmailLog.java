package com.nslindia.procurezone.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for tracking email send history
 * Logs all emails sent from the system
 */
@Entity
@Table(name = "tbl_email_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "log_to", nullable = false, length = 500)
    private String toAddress;

    @Column(name = "log_cc", length = 500)
    private String ccAddress;

    @Column(name = "log_bcc", length = 500)
    private String bccAddress;

    @Column(name = "log_subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "log_body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "log_template_code", length = 50)
    private String templateCode;

    @Column(name = "log_status", length = 20)
    private String status; // SENT, FAILED, PENDING

    @Column(name = "log_error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "log_sent_date")
    private LocalDateTime sentDate;

    @Column(name = "log_retry_count")
    private Integer retryCount;

    @Column(name = "log_triggered_by")
    private String triggeredBy; // USER_ID or SYSTEM

    @Column(name = "log_created_date")
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (retryCount == null) {
            retryCount = 0;
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}
