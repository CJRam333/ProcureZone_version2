-- =====================================================
-- PLANT INDENT TEST DATA SEEDING
-- Seeds test data for plant-specific indent tables
-- Uses INSERT IGNORE to skip existing records
-- =====================================================
-- FK References from existing data:
-- Plants: 301, 302, 303, 304, 305
-- Companies: 1, 2, 3
-- Employees: emp_number 1-25
-- Departments: 101, 102, 103, etc.
-- Roles: 1-14
-- NOTE: pz_tbl_indent_details FK references pz_tbl_indent_masterb (not pz_tbl_indent_master!)

-- Seed Crop Types (skip if exists)
INSERT IGNORE INTO
    pz_crop_type (
        crop_type_name,
        crop_type_desc,
        status
    )
VALUES (
        'Cotton',
        'Cotton varieties',
        1
    ),
    (
        'Maize',
        'Maize/Corn varieties',
        1
    ),
    (
        'Rice',
        'Rice/Paddy varieties',
        1
    ),
    (
        'Soybean',
        'Soybean varieties',
        1
    ),
    ('Wheat', 'Wheat varieties', 1),
    (
        'Vegetables',
        'Vegetable seeds',
        1
    ),
    (
        'Oilseeds',
        'Oil seed varieties',
        1
    );

-- Seed Crop Group (skip if exists)
INSERT IGNORE INTO
    pz_crop_group (
        crop_code,
        crop_name,
        crop_status,
        crop_lmd,
        crop_lmu
    )
VALUES (
        'BG1',
        'BG1 Cotton',
        1,
        CURDATE(),
        1
    ),
    (
        'BG2',
        'BG2 Cotton',
        1,
        CURDATE(),
        1
    ),
    (
        'HT',
        'Herbicide Tolerant',
        1,
        CURDATE(),
        1
    ),
    (
        'CONV',
        'Conventional',
        1,
        CURDATE(),
        1
    ),
    (
        'HYB',
        'Hybrid',
        1,
        CURDATE(),
        1
    ),
    (
        'OPV',
        'Open Pollinated Variety',
        1,
        CURDATE(),
        1
    );

-- Seed Process Packing options
INSERT IGNORE INTO
    pz_tbl_proccess_packing (
        col_name,
        col_desc,
        col_status
    )
VALUES (
        'PKG-450',
        '450 gram seed packet',
        1
    ),
    (
        'PKG-900',
        '900 gram seed packet',
        1
    ),
    (
        'PKG-1KG',
        '1 kilogram seed packet',
        1
    ),
    (
        'PKG-5KG',
        '5 kilogram bulk packet',
        1
    ),
    (
        'PKG-10KG',
        '10 kilogram bulk packet',
        1
    ),
    (
        'PKG-25KG',
        '25 kilogram jumbo packet',
        1
    ),
    (
        'BULK',
        'Bulk/Loose packing',
        1
    );

-- Seed Line Codes (Production lines) - using valid plant IDs: 301, 302, 303
INSERT IGNORE INTO
    pz_tbl_line_code (
        line_code,
        line_desc,
        plant_id,
        line_type,
        line_status
    )
VALUES (
        'LINE-01',
        'Production Line 1',
        301,
        'PRODUCTION',
        1
    ),
    (
        'LINE-02',
        'Production Line 2',
        301,
        'PRODUCTION',
        1
    ),
    (
        'LINE-03',
        'Production Line 3',
        301,
        'PRODUCTION',
        1
    ),
    (
        'LINE-04',
        'Production Line 4',
        302,
        'PRODUCTION',
        1
    ),
    (
        'LINE-05',
        'QC Testing Line',
        302,
        'QC',
        1
    ),
    (
        'LINE-06',
        'R&D Line',
        303,
        'RND',
        1
    );

-- Seed UOM Master for Plant
INSERT IGNORE INTO
    pz_umo_master (umo_name, umo_status)
VALUES ('Kilogram', 1),
    ('Gram', 1),
    ('Metric Ton', 1),
    ('Bag', 1),
    ('Packet', 1),
    ('Quintal', 1);

-- Seed Storage Locations - using valid plant IDs: 301, 302, 303
INSERT IGNORE INTO
    pz_plant_storage_locations (
        plant_code,
        storage_code,
        storage_desc,
        storage_type,
        status
    )
VALUES (
        301,
        'WH-01',
        'Primary storage warehouse',
        'WAREHOUSE',
        1
    ),
    (
        301,
        'WH-02',
        'Temperature controlled storage',
        'COLD',
        1
    ),
    (
        302,
        'WH-03',
        'Temperature controlled storage',
        'COLD',
        1
    ),
    (
        302,
        'WH-04',
        'Processing and packing storage',
        'PROCESSING',
        1
    ),
    (
        303,
        'WH-05',
        'Quality control sample storage',
        'QC',
        1
    ),
    (
        303,
        'WH-06',
        'Research storage',
        'RND',
        1
    );

-- Seed SAP Material Masters - plant as INT 301, 302, 303
INSERT IGNORE INTO
    pz_sap_material_masters (
        material_code,
        material_desc,
        uom,
        material_type,
        plant,
        materialstatus
    )
VALUES (
        'SAP-COT-001',
        'BG2 Cotton Premium',
        'KG',
        'SEED',
        301,
        1
    ),
    (
        'SAP-COT-002',
        'BG1 Cotton Standard',
        'KG',
        'SEED',
        301,
        1
    ),
    (
        'SAP-MAZ-001',
        'Hybrid Maize Gold',
        'KG',
        'SEED',
        302,
        1
    ),
    (
        'SAP-MAZ-002',
        'Single Cross Maize',
        'KG',
        'SEED',
        302,
        1
    ),
    (
        'SAP-RIC-001',
        'Basmati Rice Premium',
        'KG',
        'SEED',
        303,
        1
    ),
    (
        'SAP-SOY-001',
        'JS335 Soybean',
        'KG',
        'SEED',
        303,
        1
    ),
    (
        'SAP-VEG-001',
        'Tomato Hybrid F1',
        'GM',
        'SEED',
        301,
        1
    ),
    (
        'SAP-VEG-002',
        'Chilli Premium',
        'GM',
        'SEED',
        302,
        1
    );

-- Seed Indent Order Types
INSERT IGNORE INTO
    pz_indent_ordertype (
        order_type,
        order_desc,
        plant_id,
        order_status
    )
VALUES (
        'PROD',
        'Production batch material request',
        NULL,
        1
    ),
    (
        'QC',
        'Quality control sample request',
        NULL,
        1
    ),
    (
        'RND',
        'Research and development',
        NULL,
        1
    ),
    (
        'TRIAL',
        'Trial production batch',
        NULL,
        1
    ),
    (
        'SAMPLE',
        'Sample material request',
        NULL,
        1
    ),
    (
        'REWORK',
        'Rework/reprocessing order',
        NULL,
        1
    );

-- Seed Employee-Plant Mapping - emp_id references emp_number, plant_id references plant_id, role_id references role_id
INSERT IGNORE INTO
    pz_tbl_emp_plant_map (
        emp_id,
        company_id,
        plant_id,
        role_id,
        status
    )
VALUES (1, 1, 301, 1, 1),
    (2, 1, 301, 2, 1),
    (3, 1, 302, 2, 1),
    (4, 1, 302, 4, 1),
    (5, 1, 303, 5, 1),
    (8, 1, 301, 8, 1),
    (9, 1, 302, 8, 1),
    (10, 1, 303, 10, 1);

-- NOTE: pz_tbl_indent_master is legacy, use pz_tbl_indent_masterb for FK integrity with pz_tbl_indent_details
-- Sample Plant Indent Master Records (pz_tbl_indent_masterb)
INSERT IGNORE INTO
    pz_tbl_indent_masterb (
        indent_no,
        indent_year,
        indent_date,
        indent_company,
        indent_dept,
        indent_plant,
        indent_emp,
        indent_comments,
        indent_delivery_date,
        indent_status,
        indent_lmd,
        indent_lmu,
        indent_crop_type,
        indent_crop,
        indent_linecode,
        indent_linedesc
    )
VALUES (
        'PIND/2024/001',
        '2024',
        NOW(),
        1,
        101,
        301,
        1,
        'Production material',
        DATE_ADD(NOW(), INTERVAL 15 DAY),
        1,
        NOW(),
        1,
        1,
        'Cotton',
        'LINE-01',
        'Production Line 1'
    ),
    (
        'PIND/2024/002',
        '2024',
        NOW(),
        1,
        102,
        302,
        2,
        'QC samples',
        DATE_ADD(NOW(), INTERVAL 30 DAY),
        1,
        NOW(),
        2,
        2,
        'Maize',
        'LINE-05',
        'QC Testing Line'
    ),
    (
        'PIND/2024/003',
        '2024',
        NOW(),
        1,
        101,
        301,
        3,
        'Regular batch',
        DATE_ADD(NOW(), INTERVAL 20 DAY),
        2,
        NOW(),
        3,
        1,
        'Cotton',
        'LINE-02',
        'Production Line 2'
    ),
    (
        'PIND/2024/004',
        '2024',
        NOW(),
        1,
        103,
        303,
        4,
        'R&D trial batch',
        DATE_ADD(NOW(), INTERVAL 45 DAY),
        0,
        NOW(),
        4,
        3,
        'Rice',
        'LINE-06',
        'R&D Line'
    ),
    (
        'PIND/2024/005',
        '2024',
        NOW(),
        1,
        102,
        302,
        5,
        'Approved production',
        DATE_ADD(NOW(), INTERVAL 10 DAY),
        4,
        NOW(),
        5,
        2,
        'Maize',
        'LINE-04',
        'Production Line 4'
    );

-- Sample Plant Indent Detail Records with QC Parameters
-- NOTE: indent_details_lmu FK requires valid emp_number, so we omit it (nullable)
INSERT IGNORE INTO
    pz_tbl_indent_details (
        indent_id,
        indent_details_qty,
        indent_details_rm_qty,
        indent_details_dept_qty,
        indent_details_stock_aval,
        indent_details_purpose,
        indent_details_status,
        indent_details_lmd,
        indent_material,
        indent_mat_desc,
        indent_lot_number,
        indent_storage_loc,
        stl,
        odv,
        elisa,
        MOISTURE,
        PURE_SEED,
        INERT_MATTER,
        GERM_NORMAL,
        BG1,
        BG2,
        HT
    )
VALUES
    -- Use the indent_id values from pz_tbl_indent_masterb (typically starting from 1)
    (
        1,
        500.00,
        500.00,
        500.00,
        1200.00,
        'Production batch',
        1,
        NOW(),
        'SAP-COT-001',
        'BG2 Cotton Premium',
        'LOT-2024-001',
        'WH-01',
        'PASS',
        'NEGATIVE',
        'NEGATIVE',
        '8.50',
        '97.00',
        '1.50',
        '85.00',
        'POSITIVE',
        'POSITIVE',
        'NEGATIVE'
    ),
    (
        1,
        300.00,
        300.00,
        300.00,
        800.00,
        'Production batch',
        1,
        NOW(),
        'SAP-COT-002',
        'BG1 Cotton Standard',
        'LOT-2024-002',
        'WH-01',
        'PASS',
        'NEGATIVE',
        'NEGATIVE',
        '9.00',
        '96.50',
        '2.00',
        '82.00',
        'POSITIVE',
        'NEGATIVE',
        'NEGATIVE'
    ),
    (
        2,
        100.00,
        100.00,
        100.00,
        500.00,
        'QC testing',
        1,
        NOW(),
        'SAP-MAZ-001',
        'Hybrid Maize Gold',
        'LOT-2024-003',
        'WH-03',
        'PASS',
        'NEGATIVE',
        'NEGATIVE',
        '12.00',
        '98.00',
        '1.00',
        '90.00',
        NULL,
        NULL,
        NULL
    ),
    (
        3,
        1000.00,
        800.00,
        800.00,
        1500.00,
        'Regular batch',
        2,
        NOW(),
        'SAP-COT-001',
        'BG2 Cotton Premium',
        'LOT-2024-004',
        'WH-01',
        'PASS',
        'NEGATIVE',
        'NEGATIVE',
        '8.80',
        '97.20',
        '1.30',
        '84.00',
        'POSITIVE',
        'POSITIVE',
        'NEGATIVE'
    ),
    (
        3,
        50.00,
        50.00,
        50.00,
        200.00,
        'Vegetable seeds',
        1,
        NOW(),
        'SAP-VEG-001',
        'Tomato Hybrid F1',
        'LOT-2024-005',
        'WH-02',
        'PASS',
        'NEGATIVE',
        'NEGATIVE',
        '6.00',
        '99.00',
        '0.50',
        '92.00',
        NULL,
        NULL,
        NULL
    ),
    (
        4,
        200.00,
        NULL,
        NULL,
        350.00,
        'R&D trial',
        0,
        NOW(),
        'SAP-RIC-001',
        'Basmati Rice Premium',
        NULL,
        'WH-05',
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL
    ),
    (
        5,
        750.00,
        750.00,
        750.00,
        900.00,
        'Approved order',
        4,
        NOW(),
        'SAP-MAZ-002',
        'Single Cross Maize',
        'LOT-2024-006',
        'WH-04',
        'PASS',
        'NEGATIVE',
        'NEGATIVE',
        '11.50',
        '97.50',
        '1.50',
        '88.00',
        NULL,
        NULL,
        NULL
    ),
    (
        5,
        400.00,
        400.00,
        400.00,
        600.00,
        'Soybean order',
        4,
        NOW(),
        'SAP-SOY-001',
        'JS335 Soybean',
        'LOT-2024-007',
        'WH-04',
        'PASS',
        'NEGATIVE',
        'NEGATIVE',
        '10.00',
        '98.50',
        '1.00',
        '86.00',
        NULL,
        NULL,
        NULL
    );

-- Verification queries
SELECT 'pz_crop_type' AS table_name, COUNT(*) AS row_count
FROM pz_crop_type
UNION ALL
SELECT 'pz_crop_group', COUNT(*)
FROM pz_crop_group
UNION ALL
SELECT 'pz_tbl_proccess_packing', COUNT(*)
FROM pz_tbl_proccess_packing
UNION ALL
SELECT 'pz_tbl_line_code', COUNT(*)
FROM pz_tbl_line_code
UNION ALL
SELECT 'pz_umo_master', COUNT(*)
FROM pz_umo_master
UNION ALL
SELECT 'pz_plant_storage_locations', COUNT(*)
FROM pz_plant_storage_locations
UNION ALL
SELECT 'pz_sap_material_masters', COUNT(*)
FROM pz_sap_material_masters
UNION ALL
SELECT 'pz_indent_ordertype', COUNT(*)
FROM pz_indent_ordertype
UNION ALL
SELECT 'pz_tbl_emp_plant_map', COUNT(*)
FROM pz_tbl_emp_plant_map
UNION ALL
SELECT 'pz_tbl_indent_masterb', COUNT(*)
FROM pz_tbl_indent_masterb
UNION ALL
SELECT 'pz_tbl_indent_details', COUNT(*)
FROM pz_tbl_indent_details;