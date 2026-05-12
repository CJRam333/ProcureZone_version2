-- =======================================================
-- B.1 FIX: Create PO Amendment tracking table
-- Tracks all amendments made to Purchase Orders after vendor confirmation
-- =======================================================

CREATE TABLE IF NOT EXISTS tbl_po_amendments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    po_id INT NOT NULL,
    amendment_version INT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    original_value TEXT,
    amended_value TEXT,
    amendment_reason TEXT,
    amended_by INT NOT NULL,
    amended_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_by INT,
    approved_date DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',

-- Foreign key constraints
CONSTRAINT fk_po_amendment_po FOREIGN KEY (po_id) REFERENCES tbl_purchase_orders (id) ON DELETE CASCADE,
CONSTRAINT fk_po_amendment_amended_by FOREIGN KEY (amended_by) REFERENCES tbl_employees (id),
CONSTRAINT fk_po_amendment_approved_by FOREIGN KEY (approved_by) REFERENCES tbl_employees (id),

-- Indexes for performance
INDEX idx_po_amendments_po_id (po_id),
    INDEX idx_po_amendments_version (po_id, amendment_version),
    INDEX idx_po_amendments_date (amended_date),
    INDEX idx_po_amendments_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comment describing the table
ALTER TABLE tbl_po_amendments COMMENT = 'B.1 FIX: Tracks PO amendments after vendor confirmation for audit trail';