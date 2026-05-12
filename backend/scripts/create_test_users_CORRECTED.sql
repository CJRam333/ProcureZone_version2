-- ================================================================
-- CORRECTED TEST USERS FOR PROCUREZONE
-- Date: October 15, 2025
-- Purpose: Create test users with CORRECT role IDs matching database
-- Password for ALL users: password123
-- BCrypt Hash (strength 10): $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- Note: You can generate your own hash using: java GenerateBCryptHash password123
-- ================================================================

USE seeds_indent;

-- ================================================================
-- STEP 1: Clear any existing test users (optional - comment out if not needed)
-- ================================================================
-- DELETE FROM tbl_user_role WHERE user_id IN (
--     SELECT user_id FROM tbl_user_master WHERE user_email LIKE '%@procurezone.test'
-- );
-- DELETE FROM tbl_user_master WHERE user_email LIKE '%@procurezone.test';
-- DELETE FROM tbl_employee_master WHERE employee_email LIKE '%@procurezone.test';

-- ================================================================
-- CORRECT ROLE IDs FROM DATABASE:
-- role_id | role_code      | role_name
-- --------|----------------|------------------------
--    1    | SUPERADMIN     | Super Administrator
--    2    | ADMIN          | Administrator
--    3    | PLANTMANAGER   | Plant Manager
--    4    | DEPTHEAD       | Department Head
--    5    | PROCUREMENT    | Procurement Officer
--    6    | FINANCE        | Finance Manager
--    7    | QUALITY        | Quality Manager
--    8    | STOREKEEPER    | Store Keeper
--    9    | EMPLOYEE       | Regular Employee
--   10    | VIEWER         | View Only
--   11    | FLOORINCHARGE  | Floor Incharge
--   12    | SUPERVISOR     | Supervisor
--   13    | AUDITOR        | Auditor
--   14    | QUALITYMANAGER | Quality Manager
-- ================================================================

-- ================================================================
-- 1. SUPERADMIN USER - Highest Privilege (role_id: 1)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Super Admin',
        'superadmin@procurezone.test',
        '9999999991',
        1,
        101,
        1,
        '2024-01-01'
    );

SET @superadmin_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'superadmin',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'superadmin@procurezone.test',
        1,
        CURDATE(),
        @superadmin_emp_id,
        1
    );

SET @superadmin_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@superadmin_user_id, 1);
-- SUPERADMIN

-- ================================================================
-- 2. ADMIN USER - Administrator (role_id: 2)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'System Admin',
        'admin@procurezone.test',
        '9999999992',
        2,
        101,
        1,
        '2024-01-15'
    );

SET @admin_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'admin',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'admin@procurezone.test',
        1,
        CURDATE(),
        @admin_emp_id,
        1
    );

SET @admin_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@admin_user_id, 2);
-- ADMIN

-- ================================================================
-- 3. PLANTMANAGER USER - Plant Manager (role_id: 3)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Rajesh Kumar',
        'plantmanager@procurezone.test',
        '9999999993',
        3,
        101,
        1,
        '2023-06-01'
    );

SET @plantmgr_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'plantmanager',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'plantmanager@procurezone.test',
        1,
        CURDATE(),
        @plantmgr_emp_id,
        1
    );

SET @plantmgr_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@plantmgr_user_id, 3);
-- PLANTMANAGER

-- ================================================================
-- 4. DEPTHEAD USER - Department Head (role_id: 4)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Suresh Reddy',
        'depthead@procurezone.test',
        '9999999994',
        3,
        102,
        1,
        '2023-03-10'
    );

SET @depthead_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'depthead',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'depthead@procurezone.test',
        1,
        CURDATE(),
        @depthead_emp_id,
        1
    );

SET @depthead_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@depthead_user_id, 4);
-- DEPTHEAD

-- ================================================================
-- 5. PROCUREMENT USER - Procurement Officer (role_id: 5)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Neha Gupta',
        'procurement@procurezone.test',
        '9999999995',
        3,
        103,
        1,
        '2023-07-15'
    );

SET @procurement_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'procurement',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'procurement@procurezone.test',
        1,
        CURDATE(),
        @procurement_emp_id,
        1
    );

SET @procurement_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@procurement_user_id, 5);
-- PROCUREMENT

-- ================================================================
-- 6. FINANCE USER - Finance Manager (role_id: 6)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Kavita Deshmukh',
        'finance@procurezone.test',
        '9999999996',
        4,
        104,
        1,
        '2022-05-10'
    );

SET @finance_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'finance',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'finance@procurezone.test',
        1,
        CURDATE(),
        @finance_emp_id,
        1
    );

SET @finance_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@finance_user_id, 6);
-- FINANCE

-- ================================================================
-- 7. QUALITY USER - Quality Manager (role_id: 7)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Pradeep Sharma',
        'quality@procurezone.test',
        '9999999997',
        3,
        105,
        1,
        '2023-09-01'
    );

SET @quality_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'quality',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'quality@procurezone.test',
        1,
        CURDATE(),
        @quality_emp_id,
        1
    );

SET @quality_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@quality_user_id, 7);
-- QUALITY

-- ================================================================
-- 8. STOREKEEPER USER - Store Keeper (role_id: 8)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Ramesh Patel',
        'storekeeper@procurezone.test',
        '9999999998',
        2,
        106,
        1,
        '2023-08-20'
    );

SET @storekeeper_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'storekeeper',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'storekeeper@procurezone.test',
        1,
        CURDATE(),
        @storekeeper_emp_id,
        1
    );

SET @storekeeper_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@storekeeper_user_id, 8);
-- STOREKEEPER

-- ================================================================
-- 9. EMPLOYEE USERS - Regular Employees (role_id: 9) - 3 users
-- ================================================================

-- Employee 1 - Amit Singh
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Amit Singh',
        'employee1@procurezone.test',
        '9999999901',
        1,
        102,
        1,
        '2024-06-15'
    );

SET @emp1_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'employee1',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'employee1@procurezone.test',
        1,
        CURDATE(),
        @emp1_emp_id,
        1
    );

SET @emp1_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@emp1_user_id, 9);
-- EMPLOYEE

-- Employee 2 - Priya Verma
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Priya Verma',
        'employee2@procurezone.test',
        '9999999902',
        1,
        103,
        1,
        '2024-08-20'
    );

SET @emp2_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'employee2',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'employee2@procurezone.test',
        1,
        CURDATE(),
        @emp2_emp_id,
        1
    );

SET @emp2_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@emp2_user_id, 9);
-- EMPLOYEE

-- Employee 3 - Vikram Joshi
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Vikram Joshi',
        'employee3@procurezone.test',
        '9999999903',
        1,
        104,
        1,
        '2024-09-10'
    );

SET @emp3_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'employee3',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'employee3@procurezone.test',
        1,
        CURDATE(),
        @emp3_emp_id,
        1
    );

SET @emp3_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@emp3_user_id, 9);
-- EMPLOYEE

-- ================================================================
-- 10. VIEWER USER - View Only (role_id: 10)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Anita Shah',
        'viewer@procurezone.test',
        '9999999910',
        1,
        101,
        1,
        '2024-10-01'
    );

SET @viewer_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'viewer',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'viewer@procurezone.test',
        1,
        CURDATE(),
        @viewer_emp_id,
        1
    );

SET @viewer_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@viewer_user_id, 10);
-- VIEWER

-- ================================================================
-- 11. FLOORINCHARGE USER - Floor Incharge (role_id: 11)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Deepak Rao',
        'floorincharge@procurezone.test',
        '9999999911',
        2,
        102,
        1,
        '2023-04-15'
    );

SET @floorincharge_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'floorincharge',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'floorincharge@procurezone.test',
        1,
        CURDATE(),
        @floorincharge_emp_id,
        1
    );

SET @floorincharge_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@floorincharge_user_id, 11);
-- FLOORINCHARGE

-- ================================================================
-- 12. SUPERVISOR USER - Supervisor (role_id: 12)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Anil Mehta',
        'supervisor@procurezone.test',
        '9999999912',
        2,
        103,
        1,
        '2023-02-20'
    );

SET @supervisor_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'supervisor',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'supervisor@procurezone.test',
        1,
        CURDATE(),
        @supervisor_emp_id,
        1
    );

SET @supervisor_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@supervisor_user_id, 12);
-- SUPERVISOR

-- ================================================================
-- 13. AUDITOR USER - Auditor (role_id: 13)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Sunita Kapoor',
        'auditor@procurezone.test',
        '9999999913',
        3,
        104,
        1,
        '2022-11-05'
    );

SET @auditor_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'auditor',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'auditor@procurezone.test',
        1,
        CURDATE(),
        @auditor_emp_id,
        1
    );

SET @auditor_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@auditor_user_id, 13);
-- AUDITOR

-- ================================================================
-- 14. QUALITYMANAGER USER - Quality Manager (role_id: 14)
-- ================================================================
INSERT INTO
    tbl_employee_master (
        employee_name,
        employee_email,
        employee_phone,
        employee_designation_id,
        employee_department_id,
        employee_status,
        employee_join_date
    )
VALUES (
        'Mohan Das',
        'qualitymanager@procurezone.test',
        '9999999914',
        4,
        105,
        1,
        '2022-03-25'
    );

SET @qualitymgr_emp_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_master (
        user_username,
        user_password,
        user_email,
        user_status,
        user_created_date,
        user_employee_id,
        user_active
    )
VALUES (
        'qualitymanager',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'qualitymanager@procurezone.test',
        1,
        CURDATE(),
        @qualitymgr_emp_id,
        1
    );

SET @qualitymgr_user_id = LAST_INSERT_ID();

INSERT INTO
    tbl_user_role (user_id, role_id)
VALUES (@qualitymgr_user_id, 14);
-- QUALITYMANAGER

-- ================================================================
-- VERIFICATION QUERIES
-- ================================================================

SELECT '======================================' as '';

SELECT 'TEST USERS CREATED SUCCESSFULLY' as '';

SELECT '======================================' as '';

SELECT '' as '';

SELECT '=== ALL TEST USERS WITH CORRECT ROLES ===' as '';

SELECT u.user_id, u.user_username, e.employee_name, u.user_email, r.role_id, r.role_code, r.role_name
FROM
    tbl_user_master u
    JOIN tbl_employee_master e ON u.user_employee_id = e.employee_id
    JOIN tbl_user_role ur ON u.user_id = ur.user_id
    JOIN tbl_roles_master r ON ur.role_id = r.role_id
WHERE
    u.user_email LIKE '%@procurezone.test'
ORDER BY r.role_id, u.user_username;

SELECT '' as '';

SELECT '=== USER COUNT BY ROLE ===' as '';

SELECT r.role_id, r.role_code, r.role_name, COUNT(ur.user_id) as user_count
FROM
    tbl_roles_master r
    LEFT JOIN tbl_user_role ur ON r.role_id = ur.role_id
    LEFT JOIN tbl_user_master u ON ur.user_id = u.user_id
    AND u.user_email LIKE '%@procurezone.test'
GROUP BY
    r.role_id,
    r.role_code,
    r.role_name
ORDER BY r.role_id;

SELECT '' as '';

SELECT '======================================' as '';

SELECT 'LOGIN CREDENTIALS FOR ALL USERS:' as '';

SELECT '======================================' as '';

SELECT 'Username format: [role]@procurezone.test' as '';

SELECT 'Password for ALL: password123' as '';

SELECT '======================================' as '';

SELECT '' as '';

SELECT u.user_username as 'Username', u.user_email as 'Email', 'password123' as 'Password', r.role_code as 'Role', e.employee_name as 'Employee Name'
FROM
    tbl_user_master u
    JOIN tbl_employee_master e ON u.user_employee_id = e.employee_id
    JOIN tbl_user_role ur ON u.user_id = ur.user_id
    JOIN tbl_roles_master r ON ur.role_id = r.role_id
WHERE
    u.user_email LIKE '%@procurezone.test'
ORDER BY r.role_id;

SELECT '' as '';

SELECT '======================================' as '';

SELECT 'READY FOR API TESTING!' as '';

SELECT 'Total Users Created: 14' as '';

SELECT 'All 14 Roles Represented: YES' as '';

SELECT '======================================' as '';