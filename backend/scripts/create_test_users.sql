-- ========================================
-- Create Test Users for Complete Workflow Testing
-- Date: October 13, 2025
-- ========================================

USE seeds_indent;

-- ========================================
-- SMALL LEVEL USERS (Junior Staff)
-- ========================================

-- Junior Employee 1 - Production Assistant
INSERT INTO tbl_employee_master (employee_name, employee_email, employee_phone, employee_designation_id, employee_department_id, employee_status, employee_join_date)
VALUES ('Amit Singh', 'amit.singh@nslindia.com', '9876543210', 1, 101, 1, '2023-06-15');

SET @amit_emp_id = LAST_INSERT_ID();

INSERT INTO tbl_user_master (user_username, user_password, user_email, user_status, user_created_date, user_employee_id, user_active)
VALUES ('amit.singh', '$2a$10$rQx8K9X8K9X8K9X8K9X8KehR5h5h5h5h5h5h5h5h5h5h5h5', 'amit.singh@nslindia.com', 1, CURDATE(), @amit_emp_id, 1);

SET @amit_user_id = LAST_INSERT_ID();

INSERT INTO tbl_user_role (user_id, role_id) VALUES (@amit_user_id, 3); -- EMPLOYEE role

-- Junior Employee 2 - Store Clerk
INSERT INTO tbl_employee_master (employee_name, employee_email, employee_phone, employee_designation_id, employee_department_id, employee_status, employee_join_date)
VALUES ('Priya Sharma', 'priya.sharma@nslindia.com', '9876543211', 1, 102, 1, '2023-08-20');

SET @priya_emp_id = LAST_INSERT_ID();

INSERT INTO tbl_user_master (user_username, user_password, user_email, user_status, user_created_date, user_employee_id, user_active)
VALUES ('priya.sharma', '$2a$10$rQx8K9X8K9X8K9X8K9X8KehR5h5h5h5h5h5h5h5h5h5h5h5', 'priya.sharma@nslindia.com', 1, CURDATE(), @priya_emp_id, 1);

SET @priya_user_id = LAST_INSERT_ID();

INSERT INTO tbl_user_role (user_id, role_id) VALUES (@priya_user_id, 3); -- EMPLOYEE role

-- ========================================
-- MIDDLE LEVEL USERS (Managers/Supervisors)
-- ========================================

-- Manager 1 - Department Head
INSERT INTO tbl_employee_master (employee_name, employee_email, employee_phone, employee_designation_id, employee_department_id, employee_status, employee_join_date)
VALUES ('Suresh Reddy', 'suresh.reddy@nslindia.com', '9876543212', 3, 101, 1, '2020-03-10');

SET @suresh_emp_id = LAST_INSERT_ID();

INSERT INTO tbl_user_master (user_username, user_password, user_email, user_status, user_created_date, user_employee_id, user_active)
VALUES ('suresh.reddy', '$2a$10$rQx8K9X8K9X8K9X8K9X8KehR5h5h5h5h5h5h5h5h5h5h5h5', 'suresh.reddy@nslindia.com', 1, CURDATE(), @suresh_emp_id, 1);

SET @suresh_user_id = LAST_INSERT_ID();

INSERT INTO tbl_user_role (user_id, role_id) VALUES (@suresh_user_id, 2); -- APPROVER role
INSERT INTO tbl_user_role (user_id, role_id) VALUES (@suresh_user_id, 3); -- EMPLOYEE role

-- Manager 2 - Procurement Officer
INSERT INTO tbl_employee_master (employee_name, employee_email, employee_phone, employee_designation_id, employee_department_id, employee_status, employee_join_date)
VALUES ('Neha Gupta', 'neha.gupta@nslindia.com', '9876543213', 3, 103, 1, '2019-07-15');

SET @neha_emp_id = LAST_INSERT_ID();

INSERT INTO tbl_user_master (user_username, user_password, user_email, user_email, user_status, user_created_date, user_employee_id, user_active)
VALUES ('neha.gupta', '$2a$10$rQx8K9X8K9X8K9X8K9X8KehR5h5h5h5h5h5h5h5h5h5h5h5', 'neha.gupta@nslindia.com', 1, CURDATE(), @neha_emp_id, 1);

SET @neha_user_id = LAST_INSERT_ID();

INSERT INTO tbl_user_role (user_id, role_id) VALUES (@neha_user_id, 2); -- APPROVER role
INSERT INTO tbl_user_role (user_id, role_id) VALUES (@neha_user_id, 3); -- EMPLOYEE role

-- ========================================
-- HIGH LEVEL USERS (Senior Management)
-- ========================================

-- Senior Manager - Plant Manager
INSERT INTO tbl_employee_master (employee_name, employee_email, employee_phone, employee_designation_id, employee_department_id, employee_status, employee_join_date)
VALUES ('Amit Patel', 'amit.patel@nslindia.com', '9876543214', 5, 101, 1, '2018-01-20');

SET @amit_p_emp_id = LAST_INSERT_ID();

INSERT INTO tbl_user_master (user_username, user_password, user_email, user_status, user_created_date, user_employee_id, user_active)
VALUES ('amit.patel', '$2a$10$rQx8K9X8K9X8K9X8K9X8KehR5h5h5h5h5h5h5h5h5h5h5h5', 'amit.patel@nslindia.com', 1, CURDATE(), @amit_p_emp_id, 1);

SET @amit_p_user_id = LAST_INSERT_ID();

INSERT INTO tbl_user_role (user_id, role_id) VALUES (@amit_p_user_id, 1); -- ADMIN role
INSERT INTO tbl_user_role (user_id, role_id) VALUES (@amit_p_user_id, 2); -- APPROVER role
INSERT INTO tbl_user_role (user_id, role_id) VALUES (@amit_p_user_id, 3); -- EMPLOYEE role

-- CFO - Finance Director
INSERT INTO tbl_employee_master (employee_name, employee_email, employee_phone, employee_designation_id, employee_department_id, employee_status, employee_join_date)
VALUES ('Kavita Deshmukh', 'kavita.deshmukh@nslindia.com', '9876543215', 6, 104, 1, '2017-05-10');

SET @kavita_emp_id = LAST_INSERT_ID();

INSERT INTO tbl_user_master (user_username, user_password, user_email, user_status, user_created_date, user_employee_id, user_active)
VALUES ('kavita.deshmukh', '$2a$10$rQx8K9X8K9X8K9X8K9X8KehR5h5h5h5h5h5h5h5h5h5h5h5', 'kavita.deshmukh@nslindia.com', 1, CURDATE(), @kavita_emp_id, 1);

SET @kavita_user_id = LAST_INSERT_ID();

INSERT INTO tbl_user_role (user_id, role_id) VALUES (@kavita_user_id, 1); -- ADMIN role
INSERT INTO tbl_user_role (user_id, role_id) VALUES (@kavita_user_id, 2); -- APPROVER role
INSERT INTO tbl_user_role (user_id, role_id) VALUES (@kavita_user_id, 3); -- EMPLOYEE role

-- ========================================
-- Summary of Created Users
-- ========================================
SELECT 
    '== SMALL LEVEL USERS (Junior Staff) ==' as user_category;

SELECT u.user_username, e.employee_name, e.employee_email, d.dept_name
FROM tbl_user_master u
JOIN tbl_employee_master e ON u.user_employee_id = e.employee_id
JOIN tbl_department_master d ON e.employee_department_id = d.dept_id
WHERE u.user_username IN ('amit.singh', 'priya.sharma');

SELECT 
    '== MIDDLE LEVEL USERS (Managers) ==' as user_category;

SELECT u.user_username, e.employee_name, e.employee_email, d.dept_name
FROM tbl_user_master u
JOIN tbl_employee_master e ON u.user_employee_id = e.employee_id
JOIN tbl_department_master d ON e.employee_department_id = d.dept_id
WHERE u.user_username IN ('suresh.reddy', 'neha.gupta');

SELECT 
    '== HIGH LEVEL USERS (Senior Management) ==' as user_category;

SELECT u.user_username, e.employee_name, e.employee_email, d.dept_name
FROM tbl_user_master u
JOIN tbl_employee_master e ON u.user_employee_id = e.employee_id
JOIN tbl_department_master d ON e.employee_department_id = d.dept_id
WHERE u.user_username IN ('amit.patel', 'kavita.deshmukh');

SELECT 
    '== All Test Users with Roles ==' as summary;

SELECT 
    u.user_username, 
    e.employee_name,
    GROUP_CONCAT(r.role_name SEPARATOR ', ') as roles
FROM tbl_user_master u
JOIN tbl_employee_master e ON u.user_employee_id = e.employee_id
LEFT JOIN tbl_user_role ur ON u.user_id = ur.user_id
LEFT JOIN tbl_role_master r ON ur.role_id = r.role_id
WHERE u.user_username IN ('amit.singh', 'priya.sharma', 'suresh.reddy', 'neha.gupta', 'amit.patel', 'kavita.deshmukh')
GROUP BY u.user_id, u.user_username, e.employee_name
ORDER BY u.user_id;
