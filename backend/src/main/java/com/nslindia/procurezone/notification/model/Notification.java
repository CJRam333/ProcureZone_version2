package com.nslindia.procurezone.notification.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notif_id")
    private Integer id;

    @Column(name = "notif_recipient", nullable = false)
    private Integer recipientEmpNumber;

    @Column(name = "notif_type", nullable = false, length = 50)
    private String type;

    @Column(name = "notif_title", nullable = false, length = 200)
    private String title;

    @Column(name = "notif_message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "notif_entity_type", length = 50)
    private String entityType;

    @Column(name = "notif_entity_id")
    private Integer entityId;

    @Column(name = "notif_link", length = 500)
    private String link;

    @Column(name = "notif_is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "notif_created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "notif_read_at")
    private LocalDateTime readAt;
}
