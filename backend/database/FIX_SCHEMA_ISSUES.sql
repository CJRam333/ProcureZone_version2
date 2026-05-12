-- ========================================
-- Database Schema Fix Script
-- Date: October 15, 2025
-- Purpose: Fix missing columns causing authentication failures
-- ========================================

USE seeds_indent;

-- ========================================
-- FIX 1: Add missing columns to tbl_map_emp_roles
-- ========================================

-- Add columns one by one with safe checks
SET @dbname = DATABASE();

SET @tablename = 'tbl_map_emp_roles';

-- Add emp_roles_assigned_by
SET
    @col_exists = (
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            TABLE_SCHEMA = @dbname
            AND TABLE_NAME = @tablename
            AND COLUMN_NAME = 'emp_roles_assigned_by'
    );

SET
    @sqlstmt = IF(
        @col_exists = 0,
        'ALTER TABLE tbl_map_emp_roles ADD COLUMN emp_roles_assigned_by INT NULL AFTER emp_roles_status',
        'SELECT "Column emp_roles_assigned_by already exists" AS Info'
    );

PREPARE stmt FROM @sqlstmt;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- Add emp_roles_assigned_date
SET
    @col_exists = (
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            TABLE_SCHEMA = @dbname
            AND TABLE_NAME = @tablename
            AND COLUMN_NAME = 'emp_roles_assigned_date'
    );

SET
    @sqlstmt = IF(
        @col_exists = 0,
        'ALTER TABLE tbl_map_emp_roles ADD COLUMN emp_roles_assigned_date DATETIME NULL AFTER emp_roles_assigned_by',
        'SELECT "Column emp_roles_assigned_date already exists" AS Info'
    );

PREPARE stmt FROM @sqlstmt;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- Add emp_roles_remarks
SET
    @col_exists = (
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            TABLE_SCHEMA = @dbname
            AND TABLE_NAME = @tablename
            AND COLUMN_NAME = 'emp_roles_remarks'
    );

SET
    @sqlstmt = IF(
        @col_exists = 0,
        'ALTER TABLE tbl_map_emp_roles ADD COLUMN emp_roles_remarks TEXT NULL AFTER emp_roles_assigned_date',
        'SELECT "Column emp_roles_remarks already exists" AS Info'
    );

PREPARE stmt FROM @sqlstmt;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- Add emp_roles_removed_by
SET
    @col_exists = (
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            TABLE_SCHEMA = @dbname
            AND TABLE_NAME = @tablename
            AND COLUMN_NAME = 'emp_roles_removed_by'
    );

SET
    @sqlstmt = IF(
        @col_exists = 0,
        'ALTER TABLE tbl_map_emp_roles ADD COLUMN emp_roles_removed_by INT NULL AFTER emp_roles_remarks',
        'SELECT "Column emp_roles_removed_by already exists" AS Info'
    );

PREPARE stmt FROM @sqlstmt;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- Add emp_roles_removed_date
SET
    @col_exists = (
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            TABLE_SCHEMA = @dbname
            AND TABLE_NAME = @tablename
            AND COLUMN_NAME = 'emp_roles_removed_date'
    );

SET
    @sqlstmt = IF(
        @col_exists = 0,
        'ALTER TABLE tbl_map_emp_roles ADD COLUMN emp_roles_removed_date DATETIME NULL AFTER emp_roles_removed_by',
        'SELECT "Column emp_roles_removed_date already exists" AS Info'
    );

PREPARE stmt FROM @sqlstmt;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- ========================================
-- FIX 2: Update tbl_audit_log schema
-- ========================================

SET @tablename = 'tbl_audit_log';

-- Add audit_details
SET
    @col_exists = (
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            TABLE_SCHEMA = @dbname
            AND TABLE_NAME = @tablename
            AND COLUMN_NAME = 'audit_details'
    );

SET
    @sqlstmt = IF(
        @col_exists = 0,
        'ALTER TABLE tbl_audit_log ADD COLUMN audit_details TEXT NULL AFTER audit_action',
        'SELECT "Column audit_details already exists" AS Info'
    );

PREPARE stmt FROM @sqlstmt;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- Add audit_status
SET
    @col_exists = (
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            TABLE_SCHEMA = @dbname
            AND TABLE_NAME = @tablename
            AND COLUMN_NAME = 'audit_status'
    );

SET
    @sqlstmt = IF(
        @col_exists = 0,
        'ALTER TABLE tbl_audit_log ADD COLUMN audit_status VARCHAR(20) NULL DEFAULT "SUCCESS" AFTER audit_details',
        'SELECT "Column audit_status already exists" AS Info'
    );

PREPARE stmt FROM @sqlstmt;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- ========================================
-- VERIFY CHANGES
-- ========================================

-- Show updated structure
SELECT 'tbl_map_emp_roles structure:' AS info;

DESCRIBE tbl_map_emp_roles;

SELECT 'tbl_audit_log structure:' AS info;

DESCRIBE tbl_audit_log;

SELECT '✅ Schema fixes applied successfully!' AS result;