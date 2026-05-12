-- V36: Add missing columns to tbl_issue_note_details
-- These columns are expected by the IssueNoteDetails JPA entity but do not exist in the legacy database.

ALTER TABLE tbl_issue_note_details ADD COLUMN issue_note_details_umo INT NULL;

ALTER TABLE tbl_issue_note_details ADD COLUMN issue_note_details_rate DECIMAL(20,2) NULL;

ALTER TABLE tbl_issue_note_details ADD COLUMN issue_note_details_amount DECIMAL(20,2) NULL;

ALTER TABLE tbl_issue_note_details ADD COLUMN issue_note_details_purpose TEXT NULL;
