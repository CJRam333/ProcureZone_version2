-- V35: Add missing columns to tbl_issue_note
-- These columns are expected by the IssueNote JPA entity but do not exist in the legacy database.

ALTER TABLE tbl_issue_note ADD COLUMN issue_note_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE tbl_issue_note ADD COLUMN issue_note_issued_to VARCHAR(200) NULL;

ALTER TABLE tbl_issue_note ADD COLUMN issue_note_purpose TEXT NULL;

ALTER TABLE tbl_issue_note ADD COLUMN issue_note_rm_approvedby INT NULL;

ALTER TABLE tbl_issue_note ADD COLUMN issue_note_rm_approvedby_date DATETIME NULL;

ALTER TABLE tbl_issue_note ADD COLUMN issue_note_rm_approvedby_remarks TEXT NULL;

ALTER TABLE tbl_issue_note ADD COLUMN issue_note_rm_status INT NULL;

ALTER TABLE tbl_issue_note ADD COLUMN issue_note_supervisor_bypass TINYINT(1) NULL DEFAULT 0;
