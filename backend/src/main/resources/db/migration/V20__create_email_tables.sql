-- =====================================================
-- Email Template and Email Log Tables
-- Migration: V20_create_email_tables.sql
-- Purpose: Support for email notifications and reporting
-- =====================================================

-- =====================================================
-- Email Template Table
-- Stores reusable email templates for notifications
-- =====================================================
CREATE TABLE IF NOT EXISTS tbl_email_template (
    template_id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    template_code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique template identifier (e.g., SAP_IMPORT_SUCCESS)',
    template_name VARCHAR(200) NOT NULL COMMENT 'Human-readable template name',
    template_subject VARCHAR(500) NOT NULL COMMENT 'Email subject line (supports placeholders)',
    template_body TEXT NOT NULL COMMENT 'Email body content (HTML supported)',
    template_type VARCHAR(20) NOT NULL DEFAULT 'NOTIFICATION' COMMENT 'Template type: NOTIFICATION, REPORT, ALERT',
    template_category VARCHAR(50) COMMENT 'Category: SAP_IMPORT, REPORT_DISTRIBUTION, SYSTEM_ALERT',
    template_description TEXT COMMENT 'Description of template purpose and usage',
    template_placeholders TEXT COMMENT 'JSON array of supported placeholders with descriptions',
    is_html TINYINT(1) DEFAULT 1 COMMENT '1=HTML content, 0=Plain text',
    template_status INT NOT NULL DEFAULT 1 COMMENT '1=Active, 0=Inactive',
    template_lmd DATE NOT NULL COMMENT 'Last modified date',
    template_lmu VARCHAR(100) NOT NULL COMMENT 'Last modified user ID (FK to tbl_emp_master.emp_id)',
    INDEX idx_template_code (template_code),
    INDEX idx_template_type (template_type),
    INDEX idx_template_status (template_status),
    INDEX idx_template_category (template_category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Email templates for system notifications and reports';

-- =====================================================
-- Email Log Table
-- Tracks all emails sent by the system
-- =====================================================
CREATE TABLE IF NOT EXISTS tbl_email_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    log_template_id INT COMMENT 'FK to tbl_email_template.template_id (NULL if no template used)',
    log_to VARCHAR(1000) NOT NULL COMMENT 'Recipient email addresses (comma-separated)',
    log_cc VARCHAR(1000) COMMENT 'CC email addresses (comma-separated)',
    log_bcc VARCHAR(1000) COMMENT 'BCC email addresses (comma-separated)',
    log_subject VARCHAR(500) NOT NULL COMMENT 'Email subject',
    log_body TEXT COMMENT 'Email body content (truncated for large emails)',
    log_sent_date DATETIME NOT NULL COMMENT 'Timestamp when email was sent/attempted',
    log_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, SENT, FAILED, QUEUED',
    log_error_message TEXT COMMENT 'Error details if sending failed',
    log_retry_count INT DEFAULT 0 COMMENT 'Number of retry attempts',
    log_sent_by INT COMMENT 'User ID who triggered email (FK to tbl_emp_master.emp_number)',
    log_reference_type VARCHAR(50) COMMENT 'Reference entity type: SAP_IMPORT, REPORT, INDENT, etc.',
    log_reference_id VARCHAR(100) COMMENT 'Reference entity ID',
    has_attachments TINYINT(1) DEFAULT 0 COMMENT '1=Has attachments, 0=No attachments',
    attachment_info TEXT COMMENT 'JSON array of attachment details (name, size, type)',
    INDEX idx_log_status (log_status),
    INDEX idx_log_sent_date (log_sent_date),
    INDEX idx_log_template (log_template_id),
    INDEX idx_log_reference (
        log_reference_type,
        log_reference_id
    ),
    INDEX idx_log_sent_by (log_sent_by),
    CONSTRAINT fk_email_log_template FOREIGN KEY (log_template_id) REFERENCES tbl_email_template (template_id) ON DELETE SET NULL,
    CONSTRAINT fk_email_log_user FOREIGN KEY (log_sent_by) REFERENCES tbl_emp_master (emp_number) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Log of all emails sent by the system';

-- =====================================================
-- Insert Default Email Templates
-- =====================================================

-- Template: SAP Import Success
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_description,
        template_placeholders,
        is_html,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'SAP_IMPORT_SUCCESS',
        'SAP Material Import - Success Notification',
        'SAP Material Import Completed Successfully - {filename}',
        '<html><body><h2>SAP Material Import - Success</h2><p>Dear Team,</p><p>The SAP Material Import has completed successfully.</p><table border="1" cellpadding="5"><tr><td><strong>Filename:</strong></td><td>{filename}</td></tr><tr><td><strong>Import Date:</strong></td><td>{importDate}</td></tr><tr><td><strong>Total Rows:</strong></td><td>{totalRows}</td></tr><tr><td><strong>New Materials:</strong></td><td>{successfulInserts}</td></tr><tr><td><strong>Updated Materials:</strong></td><td>{successfulUpdates}</td></tr><tr><td><strong>Failures:</strong></td><td>{failures}</td></tr></table><p>Best regards,<br/>ProcureZone System</p></body></html>',
        'NOTIFICATION',
        'SAP_IMPORT',
        'Email notification sent when SAP material import completes successfully',
        '["filename", "importDate", "totalRows", "successfulInserts", "successfulUpdates", "failures"]',
        1,
        1,
        CURDATE(),
        1
    );

-- Template: SAP Import Failure
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_description,
        template_placeholders,
        is_html,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'SAP_IMPORT_FAILURE',
        'SAP Material Import - Failure Alert',
        'ALERT: SAP Material Import Failed - {filename}',
        '<html><body><h2 style="color: red;">SAP Material Import - FAILED</h2><p>Dear Team,</p><p><strong>The SAP Material Import has encountered errors and requires immediate attention.</strong></p><table border="1" cellpadding="5"><tr><td><strong>Filename:</strong></td><td>{filename}</td></tr><tr><td><strong>Import Date:</strong></td><td>{importDate}</td></tr><tr><td><strong>Total Rows:</strong></td><td>{totalRows}</td></tr><tr><td><strong>Failed Rows:</strong></td><td>{failures}</td></tr></table><h3>Error Details:</h3><pre style="background: #f5f5f5; padding: 10px;">{errorDetails}</pre><p><strong>Action Required:</strong> Please review the error details and reprocess the file.</p><p>Best regards,<br/>ProcureZone System</p></body></html>',
        'ALERT',
        'SAP_IMPORT',
        'Alert email sent when SAP material import fails or has significant errors',
        '["filename", "importDate", "totalRows", "failures", "errorDetails"]',
        1,
        1,
        CURDATE(),
        1
    );

-- Template: Material Quantity Report
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_description,
        template_placeholders,
        is_html,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'MATERIAL_QUANTITY_REPORT',
        'Material Quantity Report Distribution',
        'Material Quantity Report - {reportDate}',
        '<html><body><h2>Material Quantity Report</h2><p>Dear Team,</p><p>Please find attached the Material Quantity Report for {reportDate}.</p><p><strong>Report Summary:</strong></p><table border="1" cellpadding="5"><tr><td><strong>Report Type:</strong></td><td>{reportType}</td></tr><tr><td><strong>Company:</strong></td><td>{companyName}</td></tr><tr><td><strong>Total Records:</strong></td><td>{totalRecords}</td></tr><tr><td><strong>Generated Date:</strong></td><td>{generatedDate}</td></tr></table><p>The detailed report is attached as an Excel file.</p><p>Best regards,<br/>ProcureZone System</p></body></html>',
        'REPORT',
        'REPORT_DISTRIBUTION',
        'Email template for distributing material quantity reports',
        '["reportDate", "reportType", "companyName", "totalRecords", "generatedDate"]',
        1,
        1,
        CURDATE(),
        1
    );

-- Template: Low Stock Alert
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_description,
        template_placeholders,
        is_html,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'LOW_STOCK_ALERT',
        'Low Stock Alert Report',
        'URGENT: Low Stock Alert - {materialCount} Materials Below Reorder Level',
        '<html><body><h2 style="color: orange;">Low Stock Alert</h2><p>Dear Team,</p><p><strong>The following materials are at or below their reorder levels and require immediate procurement action:</strong></p><table border="1" cellpadding="5"><tr><td><strong>Total Materials:</strong></td><td>{materialCount}</td></tr><tr><td><strong>Alert Date:</strong></td><td>{alertDate}</td></tr><tr><td><strong>Critical Items:</strong></td><td>{criticalCount}</td></tr></table><p>Please find the detailed Low Stock Alert Report attached.</p><p><strong>Immediate Action Required:</strong> Review the attached report and initiate procurement for critical materials.</p><p>Best regards,<br/>ProcureZone System</p></body></html>',
        'ALERT',
        'INVENTORY_MANAGEMENT',
        'Alert email for materials below reorder level requiring procurement',
        '["materialCount", "alertDate", "criticalCount"]',
        1,
        1,
        CURDATE(),
        1
    );

-- =====================================================
-- Add Comments for Documentation
-- =====================================================

ALTER TABLE tbl_email_template COMMENT = 'Stores reusable email templates for system notifications and reports. Templates support placeholders for dynamic content.';

ALTER TABLE tbl_email_log COMMENT = 'Complete audit trail of all emails sent by the system. Used for troubleshooting and compliance.';

-- =====================================================
-- Migration Complete
-- =====================================================