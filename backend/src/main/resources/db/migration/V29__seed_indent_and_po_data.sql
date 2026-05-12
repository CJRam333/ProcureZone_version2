-- =====================================================
-- Migration: V29__seed_indent_and_po_data
-- Purpose : Seed sample indent data for workflow & PDF testing
-- NOTE    : Purchase Order tables do NOT exist yet – skipped safely
-- =====================================================

-- -----------------------------------------------------
-- Insert sample indent (safe FK values)
-- -----------------------------------------------------
INSERT IGNORE INTO tbl_indent_master
(
    indent_no,
    indent_year,
    indent_date,
    indent_company,
    indent_dept,
    indent_sec,
    indent_plant,
    indent_emp,
    indent_comments,
    indent_remarks,
    indent_status,
    indent_approved_status,
    indent_final_status,
    indent_createdby,
    indent_lmu,
    indent_lmd
)
VALUES
(
    'IND/2025/0001',
    '2025',
    DATE_FORMAT(CURDATE(), '%Y-%m-%d'),
    1,      -- company
    1,      -- department
    1,      -- section
    1,      -- plant
    1,      -- employee
    'Test indent for workflow testing',
    'Sample indent for API & PDF testing',
    1,
    1,
    1,
    1,
    1,
    CURDATE()
);

-- =====================================================
-- NOTE:
-- tbl_indent_details does NOT exist in current schema
-- tbl_purchase_orders does NOT exist in current schema
-- These will be added in future migrations
-- =====================================================

-- End of V29
