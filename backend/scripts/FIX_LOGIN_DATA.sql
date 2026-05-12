-- =====================================================
-- FIX LOGIN DATA FOR PROCUREZONE
-- Run this script to fix all user login issues
-- =====================================================

USE seeds_indent;

-- =====================================================
-- DISABLE SAFE MODE (Required for MySQL Workbench)
-- =====================================================
SET SQL_SAFE_UPDATES = 0;

-- =====================================================
-- STEP 1: CHECK CURRENT STATE
-- =====================================================
SELECT 'Current User Accounts:' as Info;

SELECT
    u.user_id,
    u.user_name,
    u.emp_number,
    u.user_status,
    e.emp_name,
    e.emp_status,
    CASE
        WHEN u.user_password LIKE '$2a$%' THEN 'BCrypt'
        WHEN LENGTH(u.user_password) = 32 THEN 'MD5'
        ELSE 'Unknown'
    END as pwd_type
FROM
    tbl_user_master u
    LEFT JOIN tbl_emp_master e ON u.emp_number = e.emp_number
ORDER BY u.user_id;

-- =====================================================
-- STEP 2: ENSURE ALL EMPLOYEES ARE ACTIVE
-- =====================================================
UPDATE tbl_emp_master SET emp_status = 1 WHERE emp_status = 0;

-- =====================================================
-- STEP 3: ENSURE ALL USERS ARE ACTIVE
-- =====================================================
UPDATE tbl_user_master SET user_status = 1 WHERE user_status = 0;

-- =====================================================
-- STEP 4: UPDATE ALL PASSWORDS TO BCRYPT
-- Password will be: password123
-- BCrypt hash for 'password123': $2a$10$N4vl3iEJQWjHBjBQV8zOt.jFQ3sYQJk3y0m.O/hQH4k4eL9X8Q7tC
-- =====================================================
UPDATE tbl_user_master
SET
    user_password = '$2a$10$N4vl3iEJQWjHBjBQV8zOt.jFQ3sYQJk3y0m.O/hQH4k4eL9X8Q7tC',
    user_lmd = CURDATE()
WHERE
    user_password NOT LIKE '$2a$%';

-- =====================================================
-- STEP 5: CREATE MISSING USER ACCOUNTS FOR KEY EMPLOYEES
-- =====================================================

-- Check for employees without user accounts
SELECT 'Employees WITHOUT user accounts:' as Info;

SELECT e.emp_number, e.emp_code, e.emp_name, e.emp_email
FROM
    tbl_emp_master e
    LEFT JOIN tbl_user_master u ON e.emp_number = u.emp_number
WHERE
    u.user_id IS NULL
    AND e.emp_number <= 25;

-- Create user accounts for employees 1-25 if missing
INSERT INTO
    tbl_user_master (
        user_name,
        user_password,
        user_status,
        user_lmd,
        user_lmu,
        emp_number
    )
SELECT
    LOWER(
        REPLACE (
                SUBSTRING_INDEX(e.emp_email, '@', 1),
                '.',
                '_'
            )
    ) as user_name,
    '$2a$10$N4vl3iEJQWjHBjBQV8zOt.jFQ3sYQJk3y0m.O/hQH4k4eL9X8Q7tC' as user_password,
    1 as user_status,
    CURDATE() as user_lmd,
    1 as user_lmu,
    e.emp_number
FROM
    tbl_emp_master e
    LEFT JOIN tbl_user_master u ON e.emp_number = u.emp_number
WHERE
    u.user_id IS NULL
    AND e.emp_number <= 25;

-- =====================================================
-- STEP 6: ENSURE EMPLOYEE ROLES ARE SET
-- =====================================================

-- Check existing roles
SELECT 'Existing Roles:' as Info;

SELECT role_id, role_name FROM tbl_roles_master;

-- Check employee role mappings
SELECT 'Employee Role Mappings:' as Info;

SELECT er.emp_number, e.emp_name, r.role_name
FROM
    tbl_emp_roles er
    JOIN tbl_emp_master e ON er.emp_number = e.emp_number
    JOIN tbl_roles_master r ON er.role_id = r.role_id
WHERE
    e.emp_number <= 25
ORDER BY er.emp_number;

-- Insert missing role mappings for key employees
INSERT IGNORE INTO
    tbl_emp_roles (
        emp_number,
        role_id,
        emp_roles_status,
        emp_roles_lmd,
        emp_roles_lmu
    )
SELECT
    e.emp_number,
    CASE
        WHEN e.emp_designation LIKE '%Admin%' THEN 1
        WHEN e.emp_designation LIKE '%Manager%' THEN 2
        WHEN e.emp_designation LIKE '%Head%' THEN 2
        WHEN e.emp_designation LIKE '%Officer%' THEN 3
        WHEN e.emp_designation LIKE '%Executive%' THEN 3
        ELSE 3
    END as role_id,
    1 as status,
    CURDATE() as lmd,
    1 as lmu
FROM
    tbl_emp_master e
    LEFT JOIN tbl_emp_roles er ON e.emp_number = er.emp_number
WHERE
    er.emp_roles_id IS NULL
    AND e.emp_number <= 25;

-- =====================================================
-- STEP 7: FINAL VERIFICATION
-- =====================================================
SELECT 'FINAL VERIFICATION - Users that can login:' as Info;

SELECT
    u.user_id,
    u.user_name,
    u.user_status as user_active,
    e.emp_name,
    e.emp_status as emp_active,
    e.emp_designation,
    GROUP_CONCAT(r.role_name) as roles,
    'password123' as login_password
FROM
    tbl_user_master u
    JOIN tbl_emp_master e ON u.emp_number = e.emp_number
    LEFT JOIN tbl_emp_roles er ON e.emp_number = er.emp_number
    LEFT JOIN tbl_roles_master r ON er.role_id = r.role_id
WHERE
    u.user_status = 1
    AND e.emp_status = 1
GROUP BY
    u.user_id,
    u.user_name,
    u.user_status,
    e.emp_name,
    e.emp_status,
    e.emp_designation
ORDER BY u.user_id;

-- =====================================================
-- LOGIN CREDENTIALS SUMMARY
-- =====================================================
SELECT '=============================================' as Info;

SELECT 'LOGIN CREDENTIALS (All passwords: password123)' as Info;

SELECT '=============================================' as Info;

SELECT
    u.user_name as Username,
    'password123' as Password,
    e.emp_name as EmployeeName,
    e.emp_designation as Designation
FROM
    tbl_user_master u
    JOIN tbl_emp_master e ON u.emp_number = e.emp_number
WHERE
    u.user_status = 1
    AND e.emp_status = 1
    AND e.emp_number <= 25
ORDER BY u.user_id;

-- =====================================================
-- RE-ENABLE SAFE MODE
-- =====================================================
SET SQL_SAFE_UPDATES = 1;

SELECT 'Script completed successfully!' as Result;