-- =====================================================
-- Schema Migration Fix for Spring Boot Backend
-- =====================================================
-- Purpose: Align database schema with JPA entity definitions
-- Date: 2025-10-15
-- Issues Fixed:
--   1. audit_user_name vs audit_username column mismatch
--   2. emp_password NOT NULL constraint incompatible with new architecture
-- =====================================================

USE seeds_indent;

-- =====================================================
-- FIX 1: Add audit_username column for AuditLog entity
-- =====================================================
-- The JPA entity expects 'audit_username' but DB has 'audit_user_name'
-- Strategy: Add new column and copy data from existing column

-- Step 1: Check if audit_username column already exists
SET
    @column_exists = (
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            TABLE_SCHEMA = 'seeds_indent'
            AND TABLE_NAME = 'tbl_audit_log'
            AND COLUMN_NAME = 'audit_username'
    );

-- Step 2: Add the new audit_username column only if it doesn't exist
SET
    @sql = IF(
        @column_exists = 0,
        'ALTER TABLE tbl_audit_log ADD COLUMN audit_username VARCHAR(100) COMMENT ''Username - aligned with JPA entity naming'' AFTER audit_user_name',
        'SELECT ''Column audit_username already exists, skipping...'' AS Info'
    );

PREPARE stmt FROM @sql;

EXECUTE stmt;

DEALLOCATE PREPARE stmt;

-- Step 3: Disable safe update mode temporarily (for this session only)
SET SQL_SAFE_UPDATES = 0;

-- Step 4: Copy existing data from audit_user_name to audit_username (only where NULL)
UPDATE tbl_audit_log
SET
    audit_username = audit_user_name
WHERE
    audit_user_name IS NOT NULL
    AND (
        audit_username IS NULL
        OR audit_username = ''
    );

-- Step 5: Re-enable safe update mode
SET SQL_SAFE_UPDATES = 1;

-- Step 6: (Optional) You can drop the old column after verifying the migration
-- Uncomment below line after testing:
-- ALTER TABLE tbl_audit_log DROP COLUMN audit_user_name;

SELECT 'Audit log table fixed: audit_username column ready' AS Status;

-- =====================================================
-- FIX 2: Make emp_password nullable for new architecture
-- =====================================================
-- New design: Passwords stored in tbl_user_account, not tbl_emp_master
-- Legacy records: emp_password contains MD5 hashes (to be kept for backward compatibility)
-- New records: emp_password can be NULL (authentication via tbl_user_account)

-- Step 1: Modify emp_password to allow NULL values
ALTER TABLE tbl_emp_master
MODIFY COLUMN emp_password VARCHAR(500) NULL COMMENT 'Legacy password hash (NULL for new employees using tbl_user_account)';

SELECT 'Employee table fixed: emp_password is now nullable' AS Status;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Verify audit_log table structure
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE
    TABLE_SCHEMA = 'seeds_indent'
    AND TABLE_NAME = 'tbl_audit_log'
    AND COLUMN_NAME IN (
        'audit_user_name',
        'audit_username'
    )
ORDER BY ORDINAL_POSITION;

-- Verify emp_master table structure
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE
    TABLE_SCHEMA = 'seeds_indent'
    AND TABLE_NAME = 'tbl_emp_master'
    AND COLUMN_NAME = 'emp_password';

-- Check if any existing audit logs need migration
SELECT
    COUNT(*) AS total_audit_records,
    SUM(
        CASE
            WHEN audit_username IS NOT NULL THEN 1
            ELSE 0
        END
    ) AS records_with_username,
    SUM(
        CASE
            WHEN audit_user_name IS NOT NULL
            AND audit_username IS NULL THEN 1
            ELSE 0
        END
    ) AS needs_migration
FROM tbl_audit_log;

-- Check existing employee records
SELECT
    COUNT(*) AS total_employees,
    SUM(
        CASE
            WHEN emp_password IS NOT NULL THEN 1
            ELSE 0
        END
    ) AS employees_with_password,
    SUM(
        CASE
            WHEN emp_password IS NULL THEN 1
            ELSE 0
        END
    ) AS employees_without_password
FROM tbl_emp_master;

SELECT '=== MIGRATION COMPLETE ===' AS Status;

SELECT 'All schema issues fixed. Application should now work correctly.' AS Result;