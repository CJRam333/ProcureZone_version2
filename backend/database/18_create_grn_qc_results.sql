-- GRN QC Results Table Migration
-- This table stores quality control inspection results for Goods Receipt Notes
-- Created for the ProcureZone migration project

CREATE TABLE IF NOT EXISTS tbl_grn_qc_results (
    qc_id INT AUTO_INCREMENT PRIMARY KEY,
    grn_id INT NOT NULL,
    inspector_id INT NOT NULL,
    inspection_date DATETIME NOT NULL,
    qc_status INT NOT NULL DEFAULT 1 COMMENT '1=Pending, 2=Passed, 3=Failed, 4=Conditional',

-- Physical Parameters
moisture DECIMAL(10, 2) NULL,
pure_seed DECIMAL(10, 2) NULL,
inert_matter DECIMAL(10, 2) NULL,
ocs_count DECIMAL(10, 2) NULL COMMENT 'Other Crop Seeds count',
weed_seed_count DECIMAL(10, 2) NULL,
grain DECIMAL(10, 2) NULL,
black_seeds DECIMAL(10, 2) NULL,
pinhole_seed DECIMAL(10, 2) NULL,
bulk_density DECIMAL(10, 2) NULL,
thsw DECIMAL(10, 2) NULL COMMENT 'Thousand Seed Weight',

-- Germination Parameters
cold_vigour_germ_normal DECIMAL(10, 2) NULL,
first_count_normal DECIMAL(10, 2) NULL,
germ_normal DECIMAL(10, 2) NULL COMMENT 'Normal Germination',
fet_normal DECIMAL(10, 2) NULL,
soil_count_days DECIMAL(10, 2) NULL,
aav_germ_normal DECIMAL(10, 2) NULL,

-- GOT Parameters (Genetic Observation Testing)
got DECIMAL(10, 2) NULL,
got_gp DECIMAL(10, 2) NULL COMMENT 'Got General Plant',
got_female DECIMAL(10, 2) NULL,
got_others DECIMAL(10, 2) NULL,

-- Trait Markers
bg1 VARCHAR(50) NULL COMMENT 'Bt Gene Marker 1',
bg2 VARCHAR(50) NULL COMMENT 'Bt Gene Marker 2',
ht VARCHAR(50) NULL COMMENT 'Herbicide Tolerance',
fqr VARCHAR(50) NULL COMMENT 'Fiber Quality Rating',

-- Disease/Pest Tests
elisa VARCHAR(50) NULL COMMENT 'ELISA Test Result',
stl VARCHAR(50) NULL COMMENT 'Seed Treatment Level',
odv VARCHAR(50) NULL COMMENT 'ODV Test',
odv_res VARCHAR(50) NULL COMMENT 'ODV Result',

-- Additional QC Fields
q1 VARCHAR(100) NULL,
q2 VARCHAR(100) NULL,
q3 VARCHAR(100) NULL,
q4 VARCHAR(100) NULL,
q5 VARCHAR(100) NULL,
q6 VARCHAR(100) NULL,
q7 VARCHAR(100) NULL,
q8 VARCHAR(100) NULL,
q9 VARCHAR(100) NULL,

-- Overall Assessment
overall_remarks TEXT NULL,
pass_fail_remarks TEXT NULL,
corrective_action TEXT NULL,
re_inspection_required TINYINT(1) DEFAULT 0,
re_inspection_date DATETIME NULL,

-- Audit Fields
created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
created_by INT NOT NULL,
updated_date DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
updated_by INT NULL,

-- Foreign Keys
CONSTRAINT fk_grn_qc_grn FOREIGN KEY (grn_id) REFERENCES tbl_goods_receipt (goods_receipt_id),
CONSTRAINT fk_grn_qc_inspector FOREIGN KEY (inspector_id) REFERENCES tbl_emp_master (emp_number),
CONSTRAINT fk_grn_qc_created FOREIGN KEY (created_by) REFERENCES tbl_emp_master (emp_number),
CONSTRAINT fk_grn_qc_updated FOREIGN KEY (updated_by) REFERENCES tbl_emp_master (emp_number),

-- Indexes
INDEX idx_grn_qc_grn_id (grn_id),
    INDEX idx_grn_qc_status (qc_status),
    INDEX idx_grn_qc_inspector (inspector_id),
    INDEX idx_grn_qc_date (inspection_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Quality Control inspection results for Goods Receipt Notes';

-- Seed some QC statuses for reference
-- 1 = Pending
-- 2 = Passed
-- 3 = Failed
-- 4 = Conditional (needs re-inspection)