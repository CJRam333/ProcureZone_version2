-- =====================================================
-- Database Migration Script: Create Vendors Table
-- Version: 11
-- Module: Vendor Management
-- Description: Creates tbl_vendors table with proper indexes and constraints
-- =====================================================
use seeds_indent;
-- Create tbl_vendors table
CREATE TABLE IF NOT EXISTS tbl_vendors (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vendor_code VARCHAR(50) NOT NULL UNIQUE,
    vendor_name VARCHAR(200) NOT NULL,
    vendor_type VARCHAR(50),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    address_line1 VARCHAR(200),
    address_line2 VARCHAR(200),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    pincode VARCHAR(20),
    gst_number VARCHAR(50) UNIQUE,
    pan_number VARCHAR(20),
    payment_terms VARCHAR(100),
    credit_period_days INT,
    rating DECIMAL(3, 2) DEFAULT 0.00,
    total_orders INT DEFAULT 0,
    total_order_value DECIMAL(15, 2) DEFAULT 0.00,
    on_time_delivery_rate DECIMAL(5, 2) DEFAULT 0.00,
    quality_rating DECIMAL(3, 2) DEFAULT 0.00,
    status INT NOT NULL DEFAULT 1 COMMENT '1=Active, 2=Inactive, 3=Blacklisted',
    remarks TEXT,
    registration_date DATE,
    last_order_date DATE,
    created_by INT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_by INT,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_vendor_code (vendor_code),
    INDEX idx_vendor_name (vendor_name),
    INDEX idx_gst_number (gst_number),
    INDEX idx_status (status),
    INDEX idx_city (city),
    INDEX idx_rating (rating DESC)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Vendor/Supplier master data table';

-- Insert sample vendor data
INSERT INTO
    tbl_vendors (
        vendor_code,
        vendor_name,
        vendor_type,
        contact_person,
        contact_phone,
        contact_email,
        address_line1,
        city,
        state,
        country,
        pincode,
        gst_number,
        pan_number,
        payment_terms,
        credit_period_days,
        rating,
        status,
        registration_date,
        created_by
    )
VALUES
    -- Active vendors
    (
        'VEN-2025-001',
        'ABC Industrial Supplies Pvt Ltd',
        'Manufacturer',
        'Rajesh Kumar',
        '9876543210',
        'rajesh@abcindustrial.com',
        'Plot No. 45, Industrial Area Phase-1',
        'Pune',
        'Maharashtra',
        'India',
        '411001',
        '27AAACA1234F1Z5',
        'AAACA1234F',
        'Net 30 Days',
        30,
        4.50,
        1,
        '2024-01-15',
        1
    ),
    (
        'VEN-2025-002',
        'XYZ Electronics & Components',
        'Distributor',
        'Priya Sharma',
        '9876543211',
        'priya@xyzelec.com',
        'Tower B, Tech Park',
        'Bangalore',
        'Karnataka',
        'India',
        '560001',
        '29BBBCB5678G2Z6',
        'BBBCB5678G',
        'Net 45 Days',
        45,
        4.75,
        1,
        '2024-02-10',
        1
    ),
    (
        'VEN-2025-003',
        'Global Trading Corporation',
        'Trader',
        'Amit Patel',
        '9876543212',
        'amit@globaltrading.com',
        'Street 7, Commercial Complex',
        'Ahmedabad',
        'Gujarat',
        'India',
        '380001',
        '24CCCDC9012H3Z7',
        'CCCDC9012H',
        'Net 60 Days',
        60,
        4.25,
        1,
        '2024-03-05',
        1
    ),
    (
        'VEN-2025-004',
        'Metro Office Supplies',
        'Distributor',
        'Sunita Reddy',
        '9876543213',
        'sunita@metrooffice.com',
        'Building 12, Business Hub',
        'Hyderabad',
        'Telangana',
        'India',
        '500001',
        '36DDDED3456I4Z8',
        'DDDED3456I',
        'Net 30 Days',
        30,
        4.60,
        1,
        '2024-04-20',
        1
    ),
    (
        'VEN-2025-005',
        'Precision Engineering Works',
        'Manufacturer',
        'Vikram Singh',
        '9876543214',
        'vikram@precision.com',
        'Sector 18, Industrial Estate',
        'Noida',
        'Uttar Pradesh',
        'India',
        '201301',
        '09EEEEF7890J5Z9',
        'EEEEF7890J',
        'Net 45 Days',
        45,
        4.80,
        1,
        '2024-05-12',
        1
    ),

-- Inactive vendor
(
    'VEN-2024-999',
    'Old Supplies Co',
    'Trader',
    'Legacy Contact',
    '9876500000',
    'legacy@oldsupplies.com',
    'Old Address',
    'Mumbai',
    'Maharashtra',
    'India',
    '400001',
    '27FFFGG1234K6Z1',
    'FFFGG1234K',
    'Net 30 Days',
    30,
    3.50,
    2,
    '2023-01-01',
    1
),

-- Test vendor for blacklisting
(
    'VEN-2024-998',
    'Unreliable Traders',
    'Trader',
    'Bad Vendor',
    '9876500001',
    'bad@unreliable.com',
    'Bad Address',
    'Delhi',
    'Delhi',
    'India',
    '110001',
    '07GGGHH5678L7Z2',
    'GGGHH5678L',
    'Net 30 Days',
    30,
    1.50,
    3,
    '2023-06-01',
    1
);

-- Update statistics for active vendors (simulated order history)
UPDATE tbl_vendors
SET
    total_orders = 25,
    total_order_value = 2500000.00,
    on_time_delivery_rate = 95.00,
    quality_rating = 4.60,
    last_order_date = '2025-01-10'
WHERE
    vendor_code = 'VEN-2025-001';

UPDATE tbl_vendors
SET
    total_orders = 30,
    total_order_value = 3200000.00,
    on_time_delivery_rate = 97.50,
    quality_rating = 4.80,
    last_order_date = '2025-01-12'
WHERE
    vendor_code = 'VEN-2025-002';

UPDATE tbl_vendors
SET
    total_orders = 18,
    total_order_value = 1800000.00,
    on_time_delivery_rate = 92.00,
    quality_rating = 4.30,
    last_order_date = '2025-01-08'
WHERE
    vendor_code = 'VEN-2025-003';

UPDATE tbl_vendors
SET
    total_orders = 22,
    total_order_value = 2100000.00,
    on_time_delivery_rate = 94.50,
    quality_rating = 4.70,
    last_order_date = '2025-01-11'
WHERE
    vendor_code = 'VEN-2025-004';

UPDATE tbl_vendors
SET
    total_orders = 35,
    total_order_value = 4500000.00,
    on_time_delivery_rate = 98.00,
    quality_rating = 4.85,
    last_order_date = '2025-01-13'
WHERE
    vendor_code = 'VEN-2025-005';

-- Verification query
SELECT
    id,
    vendor_code,
    vendor_name,
    vendor_type,
    city,
    status,
    rating,
    total_orders,
    CASE status
        WHEN 1 THEN 'Active'
        WHEN 2 THEN 'Inactive'
        WHEN 3 THEN 'Blacklisted'
    END as status_name
FROM tbl_vendors
ORDER BY vendor_code;

-- =====================================================
-- End of Migration Script
-- =====================================================