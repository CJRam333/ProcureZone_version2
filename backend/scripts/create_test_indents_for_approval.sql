-- =====================================================
-- Test Data: Indents in "Submitted" Status for Approval Testing
-- =====================================================
-- Created: November 29, 2025
-- Purpose: Create indents in status=2 (Submitted) for approval workflow testing
-- =====================================================

USE seeds_indent;

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. Verify tbl_indent_status has the correct statuses
-- =====================================================
SELECT 'Checking indent status table...' AS Status;

SELECT
    indent_status_id,
    indent_status_name
FROM tbl_indent_status
ORDER BY indent_status_id;

-- =====================================================
-- 2. Create test indents in SUBMITTED status (status_id = 2)
-- =====================================================
-- These can be used to test approval workflows

-- First, let's get the next available indent ID
SET
    @start_id = (
        SELECT COALESCE(MAX(indent_id), 1000) + 1
        FROM tbl_indent_master
    );

-- Indent 1: Submitted by employee for Department Head approval
INSERT INTO
    tbl_indent_master (
        indent_id,
        indent_no,
        indent_year,
        indent_date,
        indent_company,
        indent_dept,
        indent_sec,
        indent_plant,
        indent_emp,
        indent_comments,
        indent_delivery_date,
        indent_createdby,
        indent_status,
        indent_lmd,
        indent_lmu
    )
VALUES (
        @start_id,
        CONCAT(
            'TST-2025-',
            LPAD(@start_id - 1000, 3, '0')
        ),
        '2025',
        NOW(),
        1, -- Company ID
        104, -- Department ID (Production)
        401, -- Section ID
        301, -- Plant ID
        17, -- Employee ID (creator)
        'TEST INDENT: Submitted for approval testing - Please approve or reject',
        DATE_ADD(NOW(), INTERVAL 14 DAY),
        17, -- Created by
        2, -- STATUS = 2 (Submitted) - Ready for approval!
        NOW(),
        17
    );

-- Indent 2: Another submitted indent from different department
INSERT INTO
    tbl_indent_master (
        indent_id,
        indent_no,
        indent_year,
        indent_date,
        indent_company,
        indent_dept,
        indent_sec,
        indent_plant,
        indent_emp,
        indent_comments,
        indent_delivery_date,
        indent_createdby,
        indent_status,
        indent_lmd,
        indent_lmu
    )
VALUES (
        @start_id + 1,
        CONCAT(
            'TST-2025-',
            LPAD(@start_id - 999, 3, '0')
        ),
        '2025',
        NOW(),
        1, -- Company ID
        106, -- Department ID (Quality Control)
        402, -- Section ID
        302, -- Plant ID
        19, -- Employee ID (creator)
        'TEST INDENT: Quality testing materials - Awaiting approval',
        DATE_ADD(NOW(), INTERVAL 21 DAY),
        19, -- Created by
        2, -- STATUS = 2 (Submitted) - Ready for approval!
        NOW(),
        19
    );

-- Indent 3: Urgent indent submitted for quick approval
INSERT INTO
    tbl_indent_master (
        indent_id,
        indent_no,
        indent_year,
        indent_date,
        indent_company,
        indent_dept,
        indent_sec,
        indent_plant,
        indent_emp,
        indent_comments,
        indent_delivery_date,
        indent_createdby,
        indent_status,
        indent_lmd,
        indent_lmu
    )
VALUES (
        @start_id + 2,
        CONCAT(
            'TST-2025-',
            LPAD(@start_id - 998, 3, '0')
        ),
        '2025',
        DATE_SUB(NOW(), INTERVAL 3 DAY), -- Created 3 days ago
        1, -- Company ID
        110, -- Department ID (Maintenance)
        403, -- Section ID
        301, -- Plant ID
        15, -- Employee ID (creator)
        'URGENT: Emergency maintenance supplies needed - Please approve ASAP',
        DATE_ADD(NOW(), INTERVAL 7 DAY),
        15, -- Created by
        2, -- STATUS = 2 (Submitted) - Ready for approval!
        NOW(),
        15
    );

-- =====================================================
-- 3. Add indent details (line items) for each test indent
-- =====================================================
SET
    @detail_start_id = (
        SELECT COALESCE(MAX(indent_details_id), 2000) + 1
        FROM tbl_indent_details
    );

-- Details for Indent 1
INSERT INTO
    tbl_indent_details (
        indent_details_id,
        indent_id,
        indent_details_material,
        indent_details_umo,
        indent_details_qty,
        indent_details_purpose,
        indent_details_status,
        indent_details_lmd,
        indent_details_lmu
    )
VALUES (
        @detail_start_id,
        @start_id,
        501,
        1,
        1000.00,
        'Corn seeds for winter crop',
        2,
        NOW(),
        17
    ),
    (
        @detail_start_id + 1,
        @start_id,
        505,
        2,
        50.00,
        'NPK Fertilizer',
        2,
        NOW(),
        17
    );

-- Details for Indent 2
INSERT INTO
    tbl_indent_details (
        indent_details_id,
        indent_id,
        indent_details_material,
        indent_details_umo,
        indent_details_qty,
        indent_details_purpose,
        indent_details_status,
        indent_details_lmd,
        indent_details_lmu
    )
VALUES (
        @detail_start_id + 2,
        @start_id + 1,
        509,
        3,
        200.00,
        'Testing equipment',
        2,
        NOW(),
        19
    ),
    (
        @detail_start_id + 3,
        @start_id + 1,
        510,
        3,
        100.00,
        'Lab supplies',
        2,
        NOW(),
        19
    );

-- Details for Indent 3 (Urgent)
INSERT INTO
    tbl_indent_details (
        indent_details_id,
        indent_id,
        indent_details_material,
        indent_details_umo,
        indent_details_qty,
        indent_details_purpose,
        indent_details_status,
        indent_details_lmd,
        indent_details_lmu
    )
VALUES (
        @detail_start_id + 4,
        @start_id + 2,
        507,
        5,
        25.00,
        'Emergency maintenance parts',
        2,
        NOW(),
        15
    );

-- =====================================================
-- 4. Add procurement logs for submission
-- =====================================================
SET
    @log_start_id = (
        SELECT COALESCE(
                MAX(indent_procurement_id), 3000
            ) + 1
        FROM tbl_indent_procurement_logs
    );

INSERT INTO
    tbl_indent_procurement_logs (
        indent_procurement_id,
        indent_procurement_indent,
        indent_procurement_emp,
        indent_procurement_comments,
        indent_procurement_date,
        indent_procurement_lmd,
        indent_procurement_lmu
    )
VALUES (
        @log_start_id,
        @start_id,
        17,
        'Indent submitted for approval',
        NOW(),
        NOW(),
        17
    ),
    (
        @log_start_id + 1,
        @start_id + 1,
        19,
        'Indent submitted for approval',
        NOW(),
        NOW(),
        19
    ),
    (
        @log_start_id + 2,
        @start_id + 2,
        15,
        'URGENT: Indent submitted for approval',
        DATE_SUB(NOW(), INTERVAL 3 DAY),
        NOW(),
        15
    );

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 5. Verification
-- =====================================================
SELECT 'Test indents created successfully!' AS Status;

SELECT 'INDENTS IN SUBMITTED STATUS (Ready for Approval Testing):' AS Info;

SELECT
    i.indent_id,
    i.indent_no,
    i.indent_date,
    s.indent_status_name AS status,
    d.dept_name AS department,
    e.emp_firstname AS created_by,
    i.indent_comments
FROM
    tbl_indent_master i
    JOIN tbl_indent_status s ON i.indent_status = s.indent_status_id
    LEFT JOIN tbl_dept_master d ON i.indent_dept = d.dept_id
    LEFT JOIN tbl_emp_master e ON i.indent_createdby = e.emp_number
WHERE
    i.indent_status = 2
ORDER BY i.indent_date DESC
LIMIT 10;

SELECT CONCAT(
        'Created ', COUNT(*), ' indents in SUBMITTED status. ', 'Use API: POST /api/v1/indents/{id}/approve to test approval workflow.'
    ) AS Summary
FROM tbl_indent_master
WHERE
    indent_status = 2;