-- =====================================================
-- Migration: V27__create_employee_reporting_hierarchy
-- Purpose : Seed employee reporting hierarchy
-- NOTE    : Table tbl_map_emp_reporting already exists
-- =====================================================

-- Insert reporting hierarchy (idempotent & safe)
INSERT IGNORE INTO tbl_map_emp_reporting
(
    report_sub,
    report_sup,
    report_start,
    report_end,
    report_status,
    report_lmu,
    report_lmd
)
VALUES
    -- Level 1: Report to CTO (emp_number = 1)
    (2, 1, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (3, 1, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (4, 1, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (5, 1, CURDATE(), '2099-12-31', 1, 1, CURDATE()),

    -- Level 2
    (6, 4, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (7, 1, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (8, 1, CURDATE(), '2099-12-31', 1, 1, CURDATE()),

    -- Level 3
    (9, 8, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (10, 8, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (11, 7, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (12, 7, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (13, 4, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (14, 4, CURDATE(), '2099-12-31', 1, 1, CURDATE()),

    -- Level 4
    (15, 4, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (16, 5, CURDATE(), '2099-12-31', 1, 1, CURDATE()),

    -- Level 5
    (17, 6, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (18, 6, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (19, 13, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (20, 3, CURDATE(), '2099-12-31', 1, 1, CURDATE()),

    -- Auditors / Viewers
    (21, 7, CURDATE(), '2099-12-31', 1, 1, CURDATE()),
    (22, 1, CURDATE(), '2099-12-31', 1, 1, CURDATE());

-- =====================================================
-- End of V27
-- =====================================================
