-- =====================================================
-- Migration: V30__create_sap_integration_tables
-- Purpose : SAP Material CSV Import + Import Job Logging
-- Notes   : Matches legacy SAP CSV import structure
-- =====================================================

-- -----------------------------------------------------
-- SAP Scheduled Material Master (CSV Import Table)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS pz_schedule_sap_material_master (
    sap_material_id INT AUTO_INCREMENT PRIMARY KEY,
    company_code INT,
    plant_code INT,
    storage_location VARCHAR(50),
    material_code VARCHAR(100),
    material_desc VARCHAR(500),
    material_uom VARCHAR(20),
    batch VARCHAR(100),
    quantity DECIMAL(20,4) DEFAULT 0,
    material_type VARCHAR(50),
    material_group INT,
    material_group_desc VARCHAR(200),
    variety_type VARCHAR(50),
    variety VARCHAR(100),
    crop_type VARCHAR(50),
    crop_group VARCHAR(50),
    stl VARCHAR(50),
    odv VARCHAR(50),
    got VARCHAR(50),
    elisa VARCHAR(50),
    status INT DEFAULT 1,
    mat_code_id INT,

    -- QC Parameters
    sdcls VARCHAR(50),
    stats VARCHAR(50),
    skipd VARCHAR(50),
    inspdt VARCHAR(50),
    moisture DECIMAL(10,4),
    pure_seed DECIMAL(10,4),
    inert_matter DECIMAL(10,4),
    ocs_count DECIMAL(10,4),
    weed_seed_count DECIMAL(10,4),
    grain DECIMAL(10,4),
    black_seeds DECIMAL(10,4),
    pinhole_seeds DECIMAL(10,4),
    odv_res VARCHAR(50),
    bulk_density DECIMAL(10,4),
    thsw DECIMAL(10,4),
    cold_vigour_germ_normal DECIMAL(10,4),
    first_count_normal DECIMAL(10,4),
    germ_normal DECIMAL(10,4),
    fet_normal DECIMAL(10,4),
    soil_count_days DECIMAL(10,4),
    aav_germ_normal DECIMAL(10,4),
    got_gp DECIMAL(10,4),
    got_female DECIMAL(10,4),
    got_others DECIMAL(10,4),
    bg1 VARCHAR(50),
    bg2 VARCHAR(50),
    ht VARCHAR(50),
    fqr VARCHAR(50),
    stp_one VARCHAR(50),

    lmd DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_sap_company_code (company_code),
    INDEX idx_sap_plant_code (plant_code),
    INDEX idx_sap_material_code (material_code),
    INDEX idx_sap_batch (batch),
    INDEX idx_sap_storage_location (storage_location),
    INDEX idx_sap_plant_material (plant_code, material_code)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='SAP Material Master imported from scheduled CSV jobs';

-- -----------------------------------------------------
-- SAP Import Job Log
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS sap_import_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    import_type VARCHAR(50) NOT NULL COMMENT 'SCHEDULED | MANUAL | API',
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    records_processed INT DEFAULT 0,
    records_inserted INT DEFAULT 0,
    records_updated INT DEFAULT 0,
    records_failed INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'STARTED' COMMENT 'STARTED | COMPLETED | FAILED',
    error_message TEXT,
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,
    duration_seconds BIGINT,
    triggered_by VARCHAR(100),
    plant_id INT,

    INDEX idx_import_status (status),
    INDEX idx_import_started_at (started_at),
    INDEX idx_import_type (import_type)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='SAP CSV Import Job History and Execution Metrics';

-- =====================================================
-- End of V30
-- =====================================================
