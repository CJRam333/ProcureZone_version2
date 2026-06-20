CREATE TABLE IF NOT EXISTS tbl_module_master (
    module_code VARCHAR(50) NOT NULL,
    module_name VARCHAR(100) NOT NULL,
    module_status TINYINT(1) NOT NULL DEFAULT 1,
    module_is_future TINYINT(1) NOT NULL DEFAULT 0,
    module_default_roles VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (module_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO tbl_module_master VALUES
('INDENTS',          'Indents',          1, 0, 'ALL'),
('PLANT_INDENTS',    'Plant Indents',    1, 0, 'ALL'),
('ISSUE_NOTES',      'Issue Notes',      1, 0, 'ALL'),
('REPORTS',          'Reports',          1, 0, 'ALL'),
('CONFIRMATIONS',    'Confirmations',    1, 0, 'ALL'),
('MASTERS',          'Master Data',      1, 0, 'ADMIN,SUPERADMIN,MASTER_DATA_ADMIN'),
('AUDIT_LOGS',       'Audit Logs',       1, 0, 'ADMIN,SUPERADMIN,ROLE_VIEWER'),
('ADMINISTRATION',   'Administration',   1, 0, 'SUPERADMIN'),
('PURCHASE_ORDERS',  'Purchase Orders',  0, 1, 'ADMIN,SUPERADMIN'),
('GRN',              'Goods Receipt',    0, 1, 'ADMIN,SUPERADMIN'),
('QUALITY_CONTROL',  'Quality Control',  0, 1, 'ADMIN,SUPERADMIN'),
('INVENTORY',        'Inventory',        0, 1, 'ADMIN,SUPERADMIN'),
('ANALYTICS',        'Analytics',        0, 1, 'ADMIN,SUPERADMIN'),
('VENDOR_MASTER',    'Vendor Master',    0, 1, 'ADMIN,SUPERADMIN');
