-- =====================================================
-- 15. EMPLOYEE REPORTING TABLE
-- =====================================================
-- This table stores the employee reporting hierarchy
-- Used by the Employee Reporting module

-- Create table if it doesn't exist
CREATE TABLE IF NOT EXISTS tbl_map_emp_reporting (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    report_sub INT NOT NULL COMMENT 'Subordinate employee number',
    report_sup INT NOT NULL COMMENT 'Supervisor employee number',
    report_effective_date DATE,
    report_status INT NOT NULL DEFAULT 1 COMMENT '1=Active, 0=Inactive',
    report_lmd DATE NOT NULL COMMENT 'Last modified date',
    report_lmu INT NOT NULL COMMENT 'Last modified user',
    UNIQUE KEY uk_reporting (report_sub, report_sup),
    INDEX idx_reporting_sub (report_sub),
    INDEX idx_reporting_sup (report_sup),
    INDEX idx_reporting_status (report_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Seed sample data for testing
INSERT INTO
    tbl_map_emp_reporting (
        report_id,
        report_sub,
        report_sup,
        report_status,
        report_lmd,
        report_lmu
    )
VALUES
    -- Level 1: Report to CTO (Rajesh Kumar - emp 1)
    (1, 2, 1, 1, '2025-10-10', 1), -- Priya reports to Rajesh
    (2, 3, 1, 1, '2025-10-10', 1), -- Amit reports to Rajesh
    (3, 4, 1, 1, '2025-10-10', 1), -- Suresh reports to Rajesh
    (4, 5, 1, 1, '2025-10-10', 1), -- Kavita reports to Rajesh
    -- Level 2: Department Heads report to Managers
    (5, 6, 4, 1, '2025-10-10', 1), -- Vikram reports to Suresh (Plant Manager)
    (6, 7, 1, 1, '2025-10-10', 1), -- Anjali (Finance Head) reports to CTO
    (7, 8, 1, 1, '2025-10-10', 1), -- Rahul (Procurement Head) reports to CTO
    -- Level 3: Officers report to Heads
    (8, 9, 8, 1, '2025-10-10', 1), -- Neha reports to Rahul (Procurement Head)
    (9, 10, 8, 1, '2025-10-10', 1), -- Arjun reports to Rahul
    (10, 11, 7, 1, '2025-10-10', 1), -- Pooja reports to Anjali (Finance Head)
    (11, 12, 7, 1, '2025-10-10', 1), -- Karan reports to Anjali
    (12, 13, 4, 1, '2025-10-10', 1), -- Meera reports to Suresh (Plant Manager)
    (13, 14, 4, 1, '2025-10-10', 1), -- Sanjay reports to Suresh
    -- Level 4: Store Keepers report to Plant Managers
    (14, 15, 4, 1, '2025-10-10', 1), -- Ravi reports to Suresh
    (15, 16, 5, 1, '2025-10-10', 1), -- Lakshmi reports to Kavita
    -- Level 5: Regular Employees report to Department Heads
    (16, 17, 6, 1, '2025-10-10', 1), -- Deepak reports to Vikram
    (17, 18, 6, 1, '2025-10-10', 1), -- Swati reports to Vikram
    (
        18,
        19,
        13,
        1,
        '2025-10-10',
        1
    ), -- Anil reports to Meera (QC Manager)
    (19, 20, 3, 1, '2025-10-10', 1), -- Divya reports to Amit (Admin Manager)
    -- Viewers/Auditors
    (20, 21, 7, 1, '2025-10-10', 1), -- Manoj reports to Anjali (Finance Head)
    (21, 22, 1, 1, '2025-10-10', 1) -- Sneha reports to CTO
ON DUPLICATE KEY UPDATE
    report_status = VALUES(report_status);