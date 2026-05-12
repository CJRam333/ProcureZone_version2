-- V22__create_inventory_tables.sql
-- Inventory Management Tables
-- Author: NSL India
-- Date: November 7, 2025

-- =====================================================
-- Table: tbl_inventory_balance
-- Purpose: Track current stock balance for each material at each plant
-- =====================================================

CREATE TABLE IF NOT EXISTS tbl_inventory_balance (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    material_id INT NOT NULL,
    plant_id INT NOT NULL,
    company_id INT NOT NULL,
    umo_id INT NOT NULL,

-- Stock quantities
opening_balance DECIMAL(15, 3) NOT NULL DEFAULT 0.000,
current_balance DECIMAL(15, 3) NOT NULL DEFAULT 0.000,
reserved_quantity DECIMAL(15, 3) NOT NULL DEFAULT 0.000,
available_quantity DECIMAL(15, 3) NOT NULL DEFAULT 0.000,

-- Stock levels for alerts
reorder_level DECIMAL(15, 3) NULL,
max_level DECIMAL(15, 3) NULL,
min_level DECIMAL(15, 3) NULL,

-- Valuation
avg_rate DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
total_value DECIMAL(15, 2) NOT NULL DEFAULT 0.00,

-- Audit fields
last_receipt_date DATETIME NULL,
last_issue_date DATETIME NULL,
last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

-- Foreign keys
FOREIGN KEY (material_id) REFERENCES tbl_material_master (material_id),
FOREIGN KEY (plant_id) REFERENCES tbl_plant_master (plant_id),
FOREIGN KEY (company_id) REFERENCES tbl_company_master (comp_id),
FOREIGN KEY (umo_id) REFERENCES tbl_umo_master (umo_id),

-- Unique constraint: one inventory record per material-plant-company combination
UNIQUE KEY uk_inventory_material_plant (
    material_id,
    plant_id,
    company_id
),

-- Indexes for performance
INDEX idx_inventory_material (material_id),
    INDEX idx_inventory_plant (plant_id),
    INDEX idx_inventory_company (company_id),
    INDEX idx_inventory_low_stock (available_quantity, reorder_level),
    INDEX idx_inventory_last_updated (last_updated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory balance tracking';

-- =====================================================
-- Table: tbl_inventory_transaction
-- Purpose: Complete audit trail of all inventory movements
-- =====================================================

CREATE TABLE IF NOT EXISTS tbl_inventory_transaction (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id INT NOT NULL,
    material_id INT NOT NULL,
    plant_id INT NOT NULL,
    company_id INT NOT NULL,
    umo_id INT NOT NULL,

-- Transaction details
transaction_type VARCHAR(50) NOT NULL COMMENT 'GRN, ISSUE_NOTE, ADJUSTMENT, OPENING_BALANCE, TRANSFER',
transaction_direction VARCHAR(10) NOT NULL COMMENT 'IN or OUT',
quantity DECIMAL(15, 3) NOT NULL,
rate DECIMAL(15, 2) NULL,
amount DECIMAL(15, 2) NULL,

-- Balance tracking
before_balance DECIMAL(15, 3) NOT NULL,
after_balance DECIMAL(15, 3) NOT NULL,

-- Reference to source document
reference_type VARCHAR(50) NULL,
reference_number VARCHAR(100) NULL,
reference_id INT NULL,

-- Additional details
remarks VARCHAR(500) NULL,
created_by INT NULL COMMENT 'FK to tbl_emp_master.emp_number',

-- Timestamps
transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

-- Foreign keys
FOREIGN KEY (inventory_id) REFERENCES tbl_inventory_balance (inventory_id),
FOREIGN KEY (material_id) REFERENCES tbl_material_master (material_id),
FOREIGN KEY (plant_id) REFERENCES tbl_plant_master (plant_id),
FOREIGN KEY (company_id) REFERENCES tbl_company_master (comp_id),
FOREIGN KEY (umo_id) REFERENCES tbl_umo_master (umo_id),
FOREIGN KEY (created_by) REFERENCES tbl_emp_master (emp_number),

-- Indexes for querying
INDEX idx_transaction_inventory (inventory_id),
    INDEX idx_transaction_material (material_id),
    INDEX idx_transaction_plant (plant_id),
    INDEX idx_transaction_company (company_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_transaction_direction (transaction_direction),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_transaction_reference (reference_type, reference_number),
    INDEX idx_transaction_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory transaction history';

-- =====================================================
-- Insert sample opening balances for testing
-- =====================================================

-- Commented out - will be populated via API or SAP import
-- INSERT INTO tbl_inventory_balance VALUES...

-- =====================================================
-- Comments and documentation
-- =====================================================

-- tbl_inventory_balance: Maintains current stock position
--   - One record per Material-Plant-Company combination
--   - available_quantity = current_balance - reserved_quantity
--   - Alerts triggered when available_quantity < reorder_level
--   - avg_rate calculated using weighted average method

-- tbl_inventory_transaction: Complete audit trail
--   - Records every IN/OUT movement
--   - Links to source documents (GRN, Issue Note, etc.)
--   - Tracks before/after balance for reconciliation
--   - Supports multiple transaction types

-- Transaction Types:
--   - GRN: Goods Receipt Note (stock IN)
--   - ISSUE_NOTE: Material issue (stock OUT)
--   - ADJUSTMENT: Manual stock correction (IN or OUT)
--   - OPENING_BALANCE: Initial stock setup (IN)
--   - TRANSFER: Stock transfer between plants (IN or OUT)

-- Integration Points:
--   1. GRN approval → Add stock via InventoryService.addStock()
--   2. Issue Note approval → Deduct stock via InventoryService.deductStock()
--   3. Stock Adjustment → Manual adjustment via InventoryService.adjustStock()
--   4. SAP Import → Bulk update via scheduled jobs