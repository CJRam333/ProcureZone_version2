-- V34__add_issue_note_comments.sql
-- Add missing issue_note_comments column to tbl_issue_note based on JPA entity definition

ALTER TABLE tbl_issue_note
ADD COLUMN issue_note_comments TEXT;
