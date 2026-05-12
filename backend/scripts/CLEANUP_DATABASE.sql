-- =====================================================
-- PROCUREZONE DATABASE CLEANUP SCRIPT
-- Generated: December 5, 2025
-- Purpose: Fix incorrect data and remove test records
-- =====================================================

SET SQL_SAFE_UPDATES = 0;

-- =====================================================
-- STEP 1: Fix employee names (derive from email)
-- =====================================================

-- EMP001-EMP025: Fix "Updated Employee Name" with proper names from email
UPDATE tbl_emp_master
SET
    emp_name = 'Rajesh Kumar'
WHERE
    emp_number = 1;

UPDATE tbl_emp_master
SET
    emp_name = 'Priya Sharma'
WHERE
    emp_number = 2;

UPDATE tbl_emp_master
SET
    emp_name = 'Amit Patel'
WHERE
    emp_number = 3;

UPDATE tbl_emp_master
SET
    emp_name = 'Suresh Reddy'
WHERE
    emp_number = 4;

UPDATE tbl_emp_master
SET
    emp_name = 'Kavita Desai'
WHERE
    emp_number = 5;
-- emp_number 6 (Vikram Singh) is already correct
UPDATE tbl_emp_master
SET
    emp_name = 'Anjali Mehta'
WHERE
    emp_number = 7;

UPDATE tbl_emp_master
SET
    emp_name = 'Rahul Joshi'
WHERE
    emp_number = 8;

UPDATE tbl_emp_master
SET
    emp_name = 'Neha Gupta'
WHERE
    emp_number = 9;

UPDATE tbl_emp_master
SET
    emp_name = 'Arjun Nair'
WHERE
    emp_number = 10;

UPDATE tbl_emp_master
SET
    emp_name = 'Pooja Iyer'
WHERE
    emp_number = 11;

UPDATE tbl_emp_master
SET
    emp_name = 'Karan Verma'
WHERE
    emp_number = 12;

UPDATE tbl_emp_master
SET
    emp_name = 'Meera Rao'
WHERE
    emp_number = 13;

UPDATE tbl_emp_master
SET
    emp_name = 'Sanjay Pillai'
WHERE
    emp_number = 14;

UPDATE tbl_emp_master
SET
    emp_name = 'Ravi Chandra'
WHERE
    emp_number = 15;

UPDATE tbl_emp_master
SET
    emp_name = 'Lakshmi Nambiar'
WHERE
    emp_number = 16;

UPDATE tbl_emp_master
SET
    emp_name = 'Deepak Malhotra'
WHERE
    emp_number = 17;

UPDATE tbl_emp_master
SET
    emp_name = 'Swati Bhatt'
WHERE
    emp_number = 18;

UPDATE tbl_emp_master
SET
    emp_name = 'Anil Kapoor'
WHERE
    emp_number = 19;

UPDATE tbl_emp_master
SET
    emp_name = 'Divya Krishnan'
WHERE
    emp_number = 20;

UPDATE tbl_emp_master
SET
    emp_name = 'Manoj Tiwari'
WHERE
    emp_number = 21;

UPDATE tbl_emp_master
SET
    emp_name = 'Sneha Kulkarni'
WHERE
    emp_number = 22;

UPDATE tbl_emp_master
SET
    emp_name = 'Harish Reddy'
WHERE
    emp_number = 23;

UPDATE tbl_emp_master
SET
    emp_name = 'Gayatri Menon'
WHERE
    emp_number = 24;

UPDATE tbl_emp_master
SET
    emp_name = 'Ramesh Sinha'
WHERE
    emp_number = 25;

-- =====================================================
-- STEP 2: Delete test employee role mappings
-- =====================================================
DELETE FROM tbl_map_emp_roles WHERE emp_number >= 30;

-- =====================================================
-- STEP 3: Delete test users from user_master
-- =====================================================
DELETE FROM tbl_user_master WHERE emp_number >= 30;

-- =====================================================
-- STEP 4: Delete test employees from emp_master
-- =====================================================
DELETE FROM tbl_emp_master WHERE emp_number >= 30;

-- =====================================================
-- STEP 5: Fix passwords - ensure all use BCrypt
-- Standard password: password123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- =====================================================

UPDATE tbl_emp_master
SET
    emp_password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE
    emp_password IS NULL
    OR emp_password = ''
    OR emp_password NOT LIKE '$2a$%';

UPDATE tbl_user_master
SET
    user_password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE
    user_password IS NULL
    OR user_password = ''
    OR user_password NOT LIKE '$2a$%';

-- =====================================================
-- STEP 6: Clean up audit logs for deleted test users
-- =====================================================
DELETE FROM tbl_audit_log WHERE audit_user_id >= 30;

-- =====================================================
-- STEP 7: Verify cleanup
-- =====================================================
SELECT 'Employee count after cleanup:' AS info, COUNT(*) AS count
FROM tbl_emp_master;

SELECT 'User count after cleanup:' AS info, COUNT(*) AS count
FROM tbl_user_master;

SELECT 'Role mapping count:' AS info, COUNT(*) AS count
FROM tbl_map_emp_roles;

-- Show cleaned employee list
SELECT
    emp_number,
    emp_id,
    emp_name,
    emp_email,
    emp_designation
FROM tbl_emp_master
ORDER BY emp_number;

SET SQL_SAFE_UPDATES = 1;

-- =====================================================
-- END OF CLEANUP SCRIPT
-- =====================================================