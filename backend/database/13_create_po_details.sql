-- ============================================================================
-- Purchase Order Details Table Migration
-- Creates table for storing purchase order line items
-- ============================================================================

USE seeds_indent;

-- Create Purchase Order Details Table
CREATE TABLE IF NOT EXISTS tbl_purchase_order_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id INT NOT NULL,
    line_number INT NOT NULL,
    indent_detail_id INT NULL,
    material_id INT NOT NULL,
    material_code VARCHAR(100) NOT NULL,
    material_name VARCHAR(255) NOT NULL,
    material_description TEXT NULL,
    quantity DECIMAL(20, 2) NOT NULL,
    unit_of_measure VARCHAR(50) NOT NULL,
    unit_price DECIMAL(20, 2) NOT NULL,
    tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
    discount_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
    line_total DECIMAL(20, 2) NOT NULL,
    received_quantity DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
    pending_quantity DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
    rejected_quantity DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
    delivery_status INT NOT NULL DEFAULT 1 COMMENT '1=Pending, 2=Partially Delivered, 3=Fully Delivered, 4=Cancelled',
    expected_delivery_date DATE NULL,
    actual_delivery_date DATE NULL,
    notes TEXT NULL,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME NULL,
    INDEX idx_purchase_order_id (purchase_order_id),
    INDEX idx_line_number (line_number),
    INDEX idx_material_id (material_id),
    INDEX idx_delivery_status (delivery_status),
    INDEX idx_expected_delivery (expected_delivery_date),
    UNIQUE KEY uk_po_line (
        purchase_order_id,
        line_number
    ),
    FOREIGN KEY (purchase_order_id) REFERENCES tbl_purchase_orders (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Purchase Order Line Items Table';

-- ============================================================================
-- Sample Data for PO Details
-- ============================================================================

-- PO1 Details (Draft)
INSERT INTO
    tbl_purchase_order_details (
        purchase_order_id,
        line_number,
        material_id,
        material_code,
        material_name,
        material_description,
        quantity,
        unit_of_measure,
        unit_price,
        tax_rate,
        tax_amount,
        discount_rate,
        discount_amount,
        line_total,
        pending_quantity,
        delivery_status,
        expected_delivery_date,
        created_date
    )
VALUES (
        1,
        1,
        1,
        'MAT001',
        'Steel Rods - 10mm',
        'High grade steel rods',
        100.00,
        'KG',
        500.00,
        18.00,
        9000.00,
        5.00,
        2500.00,
        56500.00,
        100.00,
        1,
        '2025-02-15',
        '2025-01-15 10:00:00'
    ),
    (
        1,
        2,
        2,
        'MAT002',
        'Cement - OPC 53 Grade',
        'Ordinary Portland Cement',
        50.00,
        'BAG',
        400.00,
        18.00,
        3600.00,
        5.00,
        1000.00,
        22600.00,
        50.00,
        1,
        '2025-02-15',
        '2025-01-15 10:00:00'
    ),
    (
        1,
        3,
        3,
        'MAT003',
        'Paint - White Emulsion',
        'Premium quality wall paint',
        20.00,
        'LTR',
        800.00,
        18.00,
        2880.00,
        5.00,
        800.00,
        16880.00,
        20.00,
        1,
        '2025-02-15',
        '2025-01-15 10:00:00'
    );

-- PO2 Details (Sent to Vendor)
INSERT INTO
    tbl_purchase_order_details (
        purchase_order_id,
        line_number,
        material_id,
        material_code,
        material_name,
        material_description,
        quantity,
        unit_of_measure,
        unit_price,
        tax_rate,
        tax_amount,
        discount_rate,
        discount_amount,
        line_total,
        pending_quantity,
        delivery_status,
        expected_delivery_date,
        created_date
    )
VALUES (
        2,
        1,
        4,
        'MAT004',
        'Electrical Wire - 2.5 sq mm',
        'Copper electrical wire',
        500.00,
        'MTR',
        25.00,
        18.00,
        2250.00,
        4.00,
        500.00,
        14250.00,
        500.00,
        1,
        '2025-02-28',
        '2025-01-20 14:30:00'
    ),
    (
        2,
        2,
        5,
        'MAT005',
        'PVC Pipes - 4 inch',
        'Heavy duty PVC pipes',
        200.00,
        'MTR',
        120.00,
        18.00,
        4320.00,
        4.00,
        960.00,
        27360.00,
        200.00,
        1,
        '2025-02-28',
        '2025-01-20 14:30:00'
    ),
    (
        2,
        3,
        6,
        'MAT006',
        'Plywood - 18mm',
        'Marine grade plywood',
        100.00,
        'SHT',
        1200.00,
        18.00,
        21600.00,
        4.00,
        4800.00,
        136800.00,
        100.00,
        1,
        '2025-02-28',
        '2025-01-20 14:30:00'
    );

-- PO3 Details (Fully Received and Closed)
INSERT INTO
    tbl_purchase_order_details (
        purchase_order_id,
        line_number,
        material_id,
        material_code,
        material_name,
        material_description,
        quantity,
        unit_of_measure,
        unit_price,
        tax_rate,
        tax_amount,
        discount_rate,
        discount_amount,
        line_total,
        received_quantity,
        pending_quantity,
        delivery_status,
        expected_delivery_date,
        actual_delivery_date,
        created_date,
        last_modified_date
    )
VALUES (
        3,
        1,
        7,
        'MAT007',
        'Sand - River Sand',
        'Washed river sand',
        10.00,
        'TON',
        5000.00,
        18.00,
        9000.00,
        5.00,
        2500.00,
        56500.00,
        10.00,
        0.00,
        3,
        '2025-02-05',
        '2025-02-04',
        '2025-01-10 09:00:00',
        '2025-02-04 14:00:00'
    ),
    (
        3,
        2,
        8,
        'MAT008',
        'Bricks - Red Clay',
        'Standard size clay bricks',
        5000.00,
        'PCS',
        8.00,
        18.00,
        7200.00,
        5.00,
        2000.00,
        45200.00,
        5000.00,
        0.00,
        3,
        '2025-02-05',
        '2025-02-04',
        '2025-01-10 09:00:00',
        '2025-02-04 14:00:00'
    ),
    (
        3,
        3,
        9,
        'MAT009',
        'Marble - White Italian',
        'Premium white marble tiles',
        50.00,
        'SQ FT',
        600.00,
        18.00,
        5400.00,
        5.00,
        1500.00,
        33900.00,
        50.00,
        0.00,
        3,
        '2025-02-05',
        '2025-02-04',
        '2025-01-10 09:00:00',
        '2025-02-04 14:00:00'
    );

-- PO4 Details (Partially Received)
INSERT INTO
    tbl_purchase_order_details (
        purchase_order_id,
        line_number,
        material_id,
        material_code,
        material_name,
        material_description,
        quantity,
        unit_of_measure,
        unit_price,
        tax_rate,
        tax_amount,
        discount_rate,
        discount_amount,
        line_total,
        received_quantity,
        pending_quantity,
        delivery_status,
        expected_delivery_date,
        created_date,
        last_modified_date
    )
VALUES (
        4,
        1,
        10,
        'MAT010',
        'Steel Beams - I-Section',
        'Structural steel I-beams',
        20.00,
        'MTR',
        3000.00,
        18.00,
        10800.00,
        5.00,
        3000.00,
        67800.00,
        10.00,
        10.00,
        2,
        '2025-03-01',
        '2025-01-25 16:00:00',
        '2025-02-15 11:00:00'
    ),
    (
        4,
        2,
        11,
        'MAT011',
        'Glass Panels - 10mm',
        'Tempered safety glass',
        30.00,
        'SQ MTR',
        1500.00,
        18.00,
        8100.00,
        5.00,
        2250.00,
        50850.00,
        20.00,
        10.00,
        2,
        '2025-03-01',
        '2025-01-25 16:00:00',
        '2025-02-15 11:00:00'
    ),
    (
        4,
        3,
        12,
        'MAT012',
        'Aluminum Frames',
        'Powder coated aluminum window frames',
        40.00,
        'MTR',
        800.00,
        18.00,
        5760.00,
        5.00,
        1600.00,
        36160.00,
        40.00,
        0.00,
        3,
        '2025-03-01',
        '2025-01-25 16:00:00',
        '2025-02-10 15:00:00'
    );

-- PO5 Details (Cancelled)
INSERT INTO
    tbl_purchase_order_details (
        purchase_order_id,
        line_number,
        material_id,
        material_code,
        material_name,
        material_description,
        quantity,
        unit_of_measure,
        unit_price,
        tax_rate,
        tax_amount,
        discount_rate,
        discount_amount,
        line_total,
        pending_quantity,
        delivery_status,
        expected_delivery_date,
        created_date
    )
VALUES (
        5,
        1,
        13,
        'MAT013',
        'Tiles - Ceramic Floor',
        'Glazed ceramic floor tiles',
        200.00,
        'SQ FT',
        150.00,
        18.00,
        5400.00,
        5.00,
        1500.00,
        33900.00,
        0.00,
        4,
        '2025-02-20',
        '2025-01-12 11:00:00'
    ),
    (
        5,
        2,
        14,
        'MAT014',
        'Grout - Tile Joint Filler',
        'Waterproof tile grout',
        50.00,
        'KG',
        200.00,
        18.00,
        1800.00,
        5.00,
        500.00,
        11300.00,
        0.00,
        4,
        '2025-02-20',
        '2025-01-12 11:00:00'
    );

-- ============================================================================
-- Verification Queries
-- ============================================================================

-- PO Details with delivery status
SELECT
    po.po_number,
    pod.line_number,
    pod.material_name,
    pod.quantity,
    pod.received_quantity,
    pod.pending_quantity,
    pod.unit_of_measure,
    pod.line_total,
    CASE pod.delivery_status
        WHEN 1 THEN 'Pending'
        WHEN 2 THEN 'Partially Delivered'
        WHEN 3 THEN 'Fully Delivered'
        WHEN 4 THEN 'Cancelled'
    END as delivery_status
FROM
    tbl_purchase_order_details pod
    INNER JOIN tbl_purchase_orders po ON pod.purchase_order_id = po.id
ORDER BY po.po_number, pod.line_number;

-- PO Summary with line item counts
SELECT
    po.po_number,
    COUNT(pod.id) as total_line_items,
    SUM(pod.line_total) as total_value,
    SUM(
        CASE
            WHEN pod.delivery_status = 3 THEN 1
            ELSE 0
        END
    ) as fully_delivered_items,
    SUM(
        CASE
            WHEN pod.delivery_status = 2 THEN 1
            ELSE 0
        END
    ) as partially_delivered_items,
    SUM(
        CASE
            WHEN pod.delivery_status = 1 THEN 1
            ELSE 0
        END
    ) as pending_items
FROM
    tbl_purchase_orders po
    LEFT JOIN tbl_purchase_order_details pod ON po.id = pod.purchase_order_id
GROUP BY
    po.id,
    po.po_number
ORDER BY po.po_number;