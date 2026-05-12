-- ============================================================================
-- Purchase Orders Table Migration
-- Creates table for storing purchase order header information
-- ============================================================================

USE seeds_indent;

-- Create Purchase Orders Table
CREATE TABLE IF NOT EXISTS tbl_purchase_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    po_date DATE NOT NULL,
    indent_id INT NOT NULL,
    vendor_id INT NOT NULL,
    department_id INT NULL,
    po_status INT NOT NULL DEFAULT 1 COMMENT '1=Draft, 2=Submitted, 3=Approved, 4=Sent to Vendor, 5=Partially Received, 6=Fully Received, 7=Cancelled, 8=Closed',
    total_amount DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
    net_amount DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) DEFAULT 'INR',
    payment_terms VARCHAR(100) NULL,
    delivery_address VARCHAR(1000) NULL,
    delivery_date DATE NULL,
    expected_delivery_date DATE NULL,
    actual_delivery_date DATE NULL,
    terms_conditions TEXT NULL,
    notes TEXT NULL,
    priority VARCHAR(20) DEFAULT 'Medium' COMMENT 'High, Medium, Low',
    approved_by INT NULL,
    approved_date DATETIME NULL,
    sent_to_vendor_by INT NULL,
    sent_to_vendor_date DATETIME NULL,
    cancelled_by INT NULL,
    cancelled_date DATETIME NULL,
    cancellation_reason VARCHAR(500) NULL,
    closed_by INT NULL,
    closed_date DATETIME NULL,
    created_by INT NOT NULL,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by INT NULL,
    last_modified_date DATETIME NULL,
    INDEX idx_po_number (po_number),
    INDEX idx_po_date (po_date),
    INDEX idx_indent_id (indent_id),
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_department_id (department_id),
    INDEX idx_po_status (po_status),
    INDEX idx_created_date (created_date),
    INDEX idx_expected_delivery (expected_delivery_date),
    FOREIGN KEY (vendor_id) REFERENCES tbl_vendors (id),
    CONSTRAINT fk_purchase_order_indent FOREIGN KEY (indent_id) REFERENCES tbl_indent_master (indent_id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Purchase Orders Header Table';

-- ============================================================================
-- Sample Data for Purchase Orders
-- ============================================================================

-- Sample PO 1: Draft PO
INSERT INTO
    tbl_purchase_orders (
        po_number,
        po_date,
        indent_id,
        vendor_id,
        department_id,
        po_status,
        total_amount,
        tax_amount,
        discount_amount,
        net_amount,
        currency,
        payment_terms,
        delivery_address,
        expected_delivery_date,
        priority,
        created_by,
        created_date
    )
VALUES (
        'PO202510001',
        '2025-01-15',
        1,
        1,
        1,
        1,
        100000.00,
        18000.00,
        5000.00,
        113000.00,
        'INR',
        'Net 30 days',
        'Plant - A, Sector 58, Noida, UP - 201301',
        '2025-02-15',
        'High',
        1001,
        '2025-01-15 10:00:00'
    );

-- Sample PO 2: Approved and Sent to Vendor
INSERT INTO
    tbl_purchase_orders (
        po_number,
        po_date,
        indent_id,
        vendor_id,
        department_id,
        po_status,
        total_amount,
        tax_amount,
        discount_amount,
        net_amount,
        currency,
        payment_terms,
        delivery_address,
        expected_delivery_date,
        priority,
        approved_by,
        approved_date,
        sent_to_vendor_by,
        sent_to_vendor_date,
        created_by,
        created_date
    )
VALUES (
        'PO202510002',
        '2025-01-20',
        2,
        2,
        2,
        4,
        250000.00,
        45000.00,
        10000.00,
        285000.00,
        'INR',
        'Net 45 days',
        'Factory - B, Sector 62, Noida, UP - 201309',
        '2025-02-28',
        'High',
        1002,
        '2025-01-21 11:00:00',
        1001,
        '2025-01-22 09:30:00',
        1001,
        '2025-01-20 14:30:00'
    );

-- Sample PO 3: Fully Received and Closed
INSERT INTO
    tbl_purchase_orders (
        po_number,
        po_date,
        indent_id,
        vendor_id,
        department_id,
        po_status,
        total_amount,
        tax_amount,
        discount_amount,
        net_amount,
        currency,
        payment_terms,
        delivery_address,
        delivery_date,
        expected_delivery_date,
        actual_delivery_date,
        priority,
        approved_by,
        approved_date,
        sent_to_vendor_by,
        sent_to_vendor_date,
        closed_by,
        closed_date,
        created_by,
        created_date
    )
VALUES (
        'PO202510003',
        '2025-01-10',
        3,
        3,
        1,
        8,
        150000.00,
        27000.00,
        7500.00,
        169500.00,
        'INR',
        'Net 30 days',
        'Warehouse - C, Greater Noida, UP',
        '2025-02-05',
        '2025-02-05',
        '2025-02-04',
        'Medium',
        1002,
        '2025-01-11 10:00:00',
        1001,
        '2025-01-12 15:00:00',
        1002,
        '2025-02-06 10:00:00',
        1001,
        '2025-01-10 09:00:00'
    );

-- Sample PO 4: Partially Received
INSERT INTO
    tbl_purchase_orders (
        po_number,
        po_date,
        indent_id,
        vendor_id,
        department_id,
        po_status,
        total_amount,
        tax_amount,
        discount_amount,
        net_amount,
        currency,
        payment_terms,
        delivery_address,
        expected_delivery_date,
        priority,
        approved_by,
        approved_date,
        sent_to_vendor_by,
        sent_to_vendor_date,
        created_by,
        created_date
    )
VALUES (
        'PO202510004',
        '2025-01-25',
        4,
        4,
        3,
        5,
        180000.00,
        32400.00,
        9000.00,
        203400.00,
        'INR',
        'Net 30 days',
        'Plant - D, Sector 63, Noida, UP',
        '2025-03-01',
        'Medium',
        1002,
        '2025-01-26 10:00:00',
        1001,
        '2025-01-27 11:00:00',
        1001,
        '2025-01-25 16:00:00'
    );

-- Sample PO 5: Cancelled
INSERT INTO
    tbl_purchase_orders (
        po_number,
        po_date,
        indent_id,
        vendor_id,
        department_id,
        po_status,
        total_amount,
        tax_amount,
        discount_amount,
        net_amount,
        currency,
        payment_terms,
        delivery_address,
        expected_delivery_date,
        priority,
        cancelled_by,
        cancelled_date,
        cancellation_reason,
        created_by,
        created_date
    )
VALUES (
        'PO202510005',
        '2025-01-12',
        5,
        5,
        2,
        7,
        90000.00,
        16200.00,
        4500.00,
        101700.00,
        'INR',
        'Advance payment',
        'Factory - E, Ghaziabad, UP',
        '2025-02-20',
        'Low',
        1002,
        '2025-01-18 14:00:00',
        'Vendor failed to meet quality standards',
        1001,
        '2025-01-12 11:00:00'
    );

-- ============================================================================
-- Verification Queries
-- ============================================================================

-- Count POs by status
SELECT
    po_status,
    CASE po_status
        WHEN 1 THEN 'Draft'
        WHEN 2 THEN 'Submitted'
        WHEN 3 THEN 'Approved'
        WHEN 4 THEN 'Sent to Vendor'
        WHEN 5 THEN 'Partially Received'
        WHEN 6 THEN 'Fully Received'
        WHEN 7 THEN 'Cancelled'
        WHEN 8 THEN 'Closed'
    END as status_name,
    COUNT(*) as count,
    SUM(net_amount) as total_value
FROM tbl_purchase_orders
GROUP BY
    po_status
ORDER BY po_status;

-- List all POs with vendor names
SELECT po.po_number, po.po_date, v.vendor_name, po.po_status, po.net_amount, po.expected_delivery_date
FROM
    tbl_purchase_orders po
    INNER JOIN tbl_vendors v ON po.vendor_id = v.id
ORDER BY po.po_date DESC;