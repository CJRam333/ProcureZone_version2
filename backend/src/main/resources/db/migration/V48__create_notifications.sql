-- V48: Create notification table for in-app approval flow notifications

CREATE TABLE IF NOT EXISTS tbl_notifications (
    notif_id          INT NOT NULL AUTO_INCREMENT,
    notif_recipient   INT NOT NULL,
    notif_type        VARCHAR(50) NOT NULL,
    notif_title       VARCHAR(200) NOT NULL,
    notif_message     TEXT,
    notif_entity_type VARCHAR(50),
    notif_entity_id   INT,
    notif_link        VARCHAR(500),
    notif_is_read     TINYINT(1) NOT NULL DEFAULT 0,
    notif_created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notif_read_at     DATETIME DEFAULT NULL,
    PRIMARY KEY (notif_id),
    INDEX idx_notif_recipient     (notif_recipient),
    INDEX idx_notif_unread        (notif_recipient, notif_is_read),
    INDEX idx_notif_created       (notif_created_at),
    CONSTRAINT fk_notif_emp FOREIGN KEY (notif_recipient)
        REFERENCES tbl_emp_master (emp_number)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
