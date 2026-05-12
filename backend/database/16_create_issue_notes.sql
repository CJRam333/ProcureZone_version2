-- =====================================================
-- 16. ISSUE NOTES SEED DATA
-- =====================================================
-- This seeds sample Issue Notes for testing the workflow

-- Create Issue Note table if it doesn't exist
CREATE TABLE IF NOT EXISTS tbl_issue_note (
    issue_note_id INT AUTO_INCREMENT PRIMARY KEY,
    issue_note_no VARCHAR(100) NOT NULL,
    issue_note_date DATETIME NOT NULL,
    issue_note_company INT NOT NULL,
    issue_note_dept INT NOT NULL,
    issue_note_sec INT,
    issue_note_plant INT NOT NULL,
    issue_note_issued_to VARCHAR(200),
    issue_note_purpose TEXT,
    issue_note_comments TEXT,
    issue_note_createdby INT NOT NULL,
    issue_note_approvedby INT,
    issue_note_approvedby_date DATETIME,
    issue_note_storesby INT,
    issue_note_storesby_date DATETIME,
    issue_note_status INT NOT NULL COMMENT '1=Created, 2=Pending Approval, 3=Approved, 4=Rejected, 5=Pending Issue, 6=Issued, 7=Rejected by Stores',
    issue_note_approved_status INT,
    issue_note_storesby_status INT,
    issue_note_lmd DATETIME NOT NULL,
    issue_note_lmu INT NOT NULL,
    UNIQUE KEY uk_issue_note_no (issue_note_no),
    INDEX idx_issue_note_status (issue_note_status),
    INDEX idx_issue_note_company (issue_note_company),
    INDEX idx_issue_note_dept (issue_note_dept)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Create Issue Note Details table if it doesn't exist
CREATE TABLE IF NOT EXISTS tbl_issue_note_details (
    issue_note_details_id INT AUTO_INCREMENT PRIMARY KEY,
    issue_note_id INT NOT NULL,
    issue_note_details_material INT NOT NULL,
    issue_note_details_umo INT NOT NULL,
    issue_note_details_qty DECIMAL(20, 2) NOT NULL,
    issue_note_details_rate DECIMAL(20, 2),
    issue_note_details_amount DECIMAL(20, 2),
    issue_note_details_purpose TEXT,
    issue_note_details_status INT NOT NULL,
    issue_note_details_lmd DATETIME NOT NULL,
    issue_note_details_lmu INT NOT NULL,
    INDEX idx_issue_note_id (issue_note_id),
    INDEX idx_issue_note_details_material (issue_note_details_material),
    INDEX idx_issue_note_details_status (issue_note_details_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Seed sample issue notes for testing workflows
INSERT INTO
    tbl_issue_note (
        issue_note_id,
        issue_note_no,
        issue_note_date,
        issue_note_company,
        issue_note_dept,
        issue_note_sec,
        issue_note_plant,
        issue_note_issued_to,
        issue_note_purpose,
        issue_note_comments,
        issue_note_createdby,
        issue_note_status,
        issue_note_lmd,
        issue_note_lmu
    )
VALUES
    -- Issue Note 1: Status 1 (Created) - ready for submit
    (
        1,
        'ISS-2025-001',
        NOW(),
        1,
        1,
        1,
        1,
        'Deepak Kumar',
        'Regular maintenance supplies',
        'Test issue note for workflow',
        17,
        1,
        NOW(),
        17
    ),
    -- Issue Note 2: Status 2 (Pending Approval) - ready for approve/reject
    (
        2,
        'ISS-2025-002',
        NOW(),
        1,
        2,
        1,
        1,
        'Swati Sharma',
        'Production materials',
        'Test issue note pending approval',
        18,
        2,
        NOW(),
        18
    ),
    -- Issue Note 3: Status 3 (Approved) - ready for issue/reject-stores
    (
        3,
        'ISS-2025-003',
        NOW(),
        1,
        1,
        1,
        1,
        'Maintenance Team',
        'Equipment repair parts',
        'Approved and ready for issue',
        17,
        3,
        NOW(),
        6
    ),
    -- Issue Note 4: Status 6 (Issued) - completed workflow
    (
        4,
        'ISS-2025-004',
        NOW() - INTERVAL 5 DAY,
        1,
        3,
        1,
        1,
        'QC Department',
        'Testing supplies',
        'Already issued - complete',
        19,
        6,
        NOW(),
        15
    )
ON DUPLICATE KEY UPDATE
    issue_note_status = VALUES(issue_note_status);

-- Seed issue note details
INSERT INTO
    tbl_issue_note_details (
        issue_note_details_id,
        issue_note_id,
        issue_note_details_material,
        issue_note_details_umo,
        issue_note_details_qty,
        issue_note_details_rate,
        issue_note_details_amount,
        issue_note_details_purpose,
        issue_note_details_status,
        issue_note_details_lmd,
        issue_note_details_lmu
    )
VALUES
    -- Details for Issue Note 1
    (
        1,
        1,
        1,
        1,
        10.00,
        150.00,
        1500.00,
        'Packing materials',
        1,
        NOW(),
        17
    ),
    (
        2,
        1,
        2,
        1,
        5.00,
        200.00,
        1000.00,
        'Spare parts',
        1,
        NOW(),
        17
    ),
    -- Details for Issue Note 2
    (
        3,
        2,
        3,
        1,
        20.00,
        75.00,
        1500.00,
        'Production consumables',
        1,
        NOW(),
        18
    ),
    -- Details for Issue Note 3
    (
        4,
        3,
        1,
        1,
        15.00,
        150.00,
        2250.00,
        'Maintenance parts',
        1,
        NOW(),
        17
    ),
    (
        5,
        3,
        4,
        1,
        8.00,
        500.00,
        4000.00,
        'Equipment parts',
        1,
        NOW(),
        17
    ),
    -- Details for Issue Note 4
    (
        6,
        4,
        5,
        1,
        25.00,
        50.00,
        1250.00,
        'Testing materials',
        1,
        NOW(),
        19
    )
ON DUPLICATE KEY UPDATE
    issue_note_details_status = VALUES(issue_note_details_status);