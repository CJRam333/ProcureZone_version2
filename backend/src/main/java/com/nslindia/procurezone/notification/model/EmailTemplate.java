package com.nslindia.procurezone.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing email templates
 * Supports dynamic email content with placeholders
 */
@Entity
@Table(name = "tbl_email_template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Integer id;

    @Column(name = "template_code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "template_name", nullable = false, length = 100)
    private String name;

    @Column(name = "template_subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "template_body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "template_type", length = 20)
    private String type; // HTML, TEXT

    @Column(name = "template_category", length = 50)
    private String category; // NOTIFICATION, REPORT, ALERT

    @Column(name = "template_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "template_status")
    private Integer status; // 1=Active, 0=Inactive

    @Column(name = "template_placeholders", columnDefinition = "TEXT")
    private String placeholders;

    @Column(name = "is_html")
    private Boolean isHtml;

    @Column(name = "template_lmd")
    private LocalDateTime lastModifiedDate;

    @Column(name = "template_lmu", length = 100)
    private String lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        lastModifiedDate = LocalDateTime.now();
        if (status == null) {
            status = 1;
        }
        if (type == null) {
            type = "HTML";
        }
        if (isHtml == null) {
            isHtml = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
