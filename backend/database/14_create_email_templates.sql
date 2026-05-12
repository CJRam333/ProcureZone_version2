-- =====================================================
-- Email Templates for ProcureZone
-- This script creates all required email templates
-- Column names: template_code, template_name, template_subject,
--               template_body, template_type, template_category,
--               template_status, template_lmd, template_lmu
-- =====================================================

USE seeds_indent;

-- Clear existing templates to avoid duplicates
DELETE FROM tbl_email_template;

-- =====================================================
-- Workflow Notification Templates
-- =====================================================

-- Indent Submitted for Approval
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'INDENT_SUBMITTED',
        'Indent Submitted for Approval',
        'Indent ${indentNumber} Submitted for Approval',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #2c3e50;">Indent Submitted for Approval</h2><p>Dear ${approverName},</p><p>A new indent has been submitted and requires your approval:</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #3498db; color: white;"><th style="padding: 10px; text-align: left;">Field</th><th style="padding: 10px; text-align: left;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Indent Number</td><td style="padding: 10px; border: 1px solid #ddd;">${indentNumber}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Requested By</td><td style="padding: 10px; border: 1px solid #ddd;">${requestedBy}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Department</td><td style="padding: 10px; border: 1px solid #ddd;">${department}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Total Items</td><td style="padding: 10px; border: 1px solid #ddd;">${itemCount}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Priority</td><td style="padding: 10px; border: 1px solid #ddd;">${priority}</td></tr></table><p style="margin-top: 20px;">Please review and take appropriate action.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'WORKFLOW',
        1,
        NOW(),
        'SYSTEM'
    );

-- Indent Approved
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'INDENT_APPROVED',
        'Indent Approved',
        'Indent ${indentNumber} Has Been Approved',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #27ae60;">Indent Approved</h2><p>Dear ${requestedBy},</p><p>Your indent has been <strong style="color: #27ae60;">approved</strong>.</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #27ae60; color: white;"><th style="padding: 10px; text-align: left;">Field</th><th style="padding: 10px; text-align: left;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Indent Number</td><td style="padding: 10px; border: 1px solid #ddd;">${indentNumber}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Approved By</td><td style="padding: 10px; border: 1px solid #ddd;">${approvedBy}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Approval Date</td><td style="padding: 10px; border: 1px solid #ddd;">${approvalDate}</td></tr></table><p style="margin-top: 20px;">The procurement team will now proceed with purchase order creation.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'WORKFLOW',
        1,
        NOW(),
        'SYSTEM'
    );

-- Indent Rejected
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'INDENT_REJECTED',
        'Indent Rejected',
        'Indent ${indentNumber} Has Been Rejected',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #e74c3c;">Indent Rejected</h2><p>Dear ${requestedBy},</p><p>Your indent has been <strong style="color: #e74c3c;">rejected</strong>.</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #e74c3c; color: white;"><th style="padding: 10px; text-align: left;">Field</th><th style="padding: 10px; text-align: left;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Indent Number</td><td style="padding: 10px; border: 1px solid #ddd;">${indentNumber}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Rejected By</td><td style="padding: 10px; border: 1px solid #ddd;">${rejectedBy}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Rejection Date</td><td style="padding: 10px; border: 1px solid #ddd;">${rejectionDate}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Reason</td><td style="padding: 10px; border: 1px solid #ddd;">${rejectionReason}</td></tr></table><p style="margin-top: 20px;">Please address the concerns and resubmit if necessary.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'WORKFLOW',
        1,
        NOW(),
        'SYSTEM'
    );

-- PO Created
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'PO_CREATED',
        'Purchase Order Created',
        'Purchase Order ${poNumber} Created',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #2c3e50;">Purchase Order Created</h2><p>Dear ${recipientName},</p><p>A new Purchase Order has been created:</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #3498db; color: white;"><th style="padding: 10px; text-align: left;">Field</th><th style="padding: 10px; text-align: left;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">PO Number</td><td style="padding: 10px; border: 1px solid #ddd;">${poNumber}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Vendor</td><td style="padding: 10px; border: 1px solid #ddd;">${vendorName}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Total Amount</td><td style="padding: 10px; border: 1px solid #ddd;">Rs.${totalAmount}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Items</td><td style="padding: 10px; border: 1px solid #ddd;">${itemCount}</td></tr></table><p style="margin-top: 20px;">This PO is awaiting approval.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'WORKFLOW',
        1,
        NOW(),
        'SYSTEM'
    );

-- PO Approved
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'PO_APPROVED',
        'Purchase Order Approved',
        'Purchase Order ${poNumber} Has Been Approved',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #27ae60;">Purchase Order Approved</h2><p>Dear ${recipientName},</p><p>Purchase Order has been <strong style="color: #27ae60;">approved</strong>.</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #27ae60; color: white;"><th style="padding: 10px; text-align: left;">Field</th><th style="padding: 10px; text-align: left;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">PO Number</td><td style="padding: 10px; border: 1px solid #ddd;">${poNumber}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Approved By</td><td style="padding: 10px; border: 1px solid #ddd;">${approvedBy}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Approval Date</td><td style="padding: 10px; border: 1px solid #ddd;">${approvalDate}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Total Amount</td><td style="padding: 10px; border: 1px solid #ddd;">Rs.${totalAmount}</td></tr></table><p style="margin-top: 20px;">The PO is now ready to be sent to the vendor.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'WORKFLOW',
        1,
        NOW(),
        'SYSTEM'
    );

-- PO Sent to Vendor (KEY TEMPLATE FOR VENDOR NOTIFICATION)
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'PO_SENT_TO_VENDOR',
        'Purchase Order Sent to Vendor',
        'New Purchase Order ${poNumber} from NSL India',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #2c3e50;">New Purchase Order</h2><p>Dear ${vendorName},</p><p>We are pleased to send you the following Purchase Order:</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #3498db; color: white;"><th style="padding: 10px; text-align: left;">Field</th><th style="padding: 10px; text-align: left;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">PO Number</td><td style="padding: 10px; border: 1px solid #ddd;"><strong>${poNumber}</strong></td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">PO Date</td><td style="padding: 10px; border: 1px solid #ddd;">${poDate}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Total Items</td><td style="padding: 10px; border: 1px solid #ddd;">${itemCount}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Order Value</td><td style="padding: 10px; border: 1px solid #ddd;"><strong>Rs.${totalAmount}</strong></td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Expected Delivery</td><td style="padding: 10px; border: 1px solid #ddd;">${expectedDeliveryDate}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Priority</td><td style="padding: 10px; border: 1px solid #ddd;">${priority}</td></tr></table><p style="margin-top: 20px;">Please acknowledge receipt of this order and confirm the delivery schedule.</p><p>For any queries, please contact our procurement team.</p><p style="margin-top: 30px;">Best Regards,<br><strong>NSL India Procurement Team</strong><br>ProcureZone System</p></body></html>',
        'HTML',
        'WORKFLOW',
        1,
        NOW(),
        'SYSTEM'
    );

-- GRN Created
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'GRN_CREATED',
        'Goods Receipt Note Created',
        'GRN ${grnNumber} Created for PO ${poNumber}',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #2c3e50;">Goods Receipt Note Created</h2><p>Dear ${recipientName},</p><p>A Goods Receipt Note has been created:</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #3498db; color: white;"><th style="padding: 10px; text-align: left;">Field</th><th style="padding: 10px; text-align: left;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">GRN Number</td><td style="padding: 10px; border: 1px solid #ddd;">${grnNumber}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">PO Number</td><td style="padding: 10px; border: 1px solid #ddd;">${poNumber}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Vendor</td><td style="padding: 10px; border: 1px solid #ddd;">${vendorName}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Receipt Date</td><td style="padding: 10px; border: 1px solid #ddd;">${receiptDate}</td></tr></table><p style="margin-top: 20px;">This GRN is awaiting quality inspection and approval.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'WORKFLOW',
        1,
        NOW(),
        'SYSTEM'
    );

-- GRN Approved
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'GRN_APPROVED',
        'Goods Receipt Note Approved',
        'GRN ${grnNumber} Has Been Approved',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #27ae60;">GRN Approved</h2><p>Dear ${recipientName},</p><p>The Goods Receipt Note has been <strong style="color: #27ae60;">approved</strong>.</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #27ae60; color: white;"><th style="padding: 10px; text-align: left;">Field</th><th style="padding: 10px; text-align: left;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">GRN Number</td><td style="padding: 10px; border: 1px solid #ddd;">${grnNumber}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">PO Number</td><td style="padding: 10px; border: 1px solid #ddd;">${poNumber}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Approved By</td><td style="padding: 10px; border: 1px solid #ddd;">${approvedBy}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Approval Date</td><td style="padding: 10px; border: 1px solid #ddd;">${approvalDate}</td></tr></table><p style="margin-top: 20px;">Inventory has been updated accordingly.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'WORKFLOW',
        1,
        NOW(),
        'SYSTEM'
    );

-- Issue Note Created
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'ISSUE_NOTE_CREATED',
        'Issue Note Created',
        'Issue Note ${issueNoteNumber} Created',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #2c3e50;">Issue Note Created</h2><p>Dear ${recipientName},</p><p>An Issue Note has been created:</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #3498db; color: white;"><th style="padding: 10px; text-align: left;">Field</th><th style="padding: 10px; text-align: left;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Issue Note Number</td><td style="padding: 10px; border: 1px solid #ddd;">${issueNoteNumber}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Department</td><td style="padding: 10px; border: 1px solid #ddd;">${department}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Requested By</td><td style="padding: 10px; border: 1px solid #ddd;">${requestedBy}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Total Items</td><td style="padding: 10px; border: 1px solid #ddd;">${itemCount}</td></tr></table><p style="margin-top: 20px;">This issue note is awaiting approval.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'WORKFLOW',
        1,
        NOW(),
        'SYSTEM'
    );

-- Issue Note Approved
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'ISSUE_NOTE_APPROVED',
        'Issue Note Approved',
        'Issue Note ${issueNoteNumber} Has Been Approved',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #27ae60;">Issue Note Approved</h2><p>Dear ${recipientName},</p><p>The Issue Note has been <strong style="color: #27ae60;">approved</strong>.</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #27ae60; color: white;"><th style="padding: 10px; text-align: left;">Field</th><th style="padding: 10px; text-align: left;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Issue Note Number</td><td style="padding: 10px; border: 1px solid #ddd;">${issueNoteNumber}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Approved By</td><td style="padding: 10px; border: 1px solid #ddd;">${approvedBy}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Approval Date</td><td style="padding: 10px; border: 1px solid #ddd;">${approvalDate}</td></tr></table><p style="margin-top: 20px;">Materials are ready for issue.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'WORKFLOW',
        1,
        NOW(),
        'SYSTEM'
    );

-- Issue Note Issued
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'ISSUE_NOTE_ISSUED',
        'Materials Issued',
        'Materials Issued - Issue Note ${issueNoteNumber}',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #27ae60;">Materials Issued</h2><p>Dear ${recipientName},</p><p>Materials have been issued against Issue Note:</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #27ae60; color: white;"><th style="padding: 10px; text-align: left;">Field</th><th style="padding: 10px; text-align: left;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Issue Note Number</td><td style="padding: 10px; border: 1px solid #ddd;">${issueNoteNumber}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Issued By</td><td style="padding: 10px; border: 1px solid #ddd;">${issuedBy}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Issue Date</td><td style="padding: 10px; border: 1px solid #ddd;">${issueDate}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Total Items</td><td style="padding: 10px; border: 1px solid #ddd;">${itemCount}</td></tr></table><p style="margin-top: 20px;">Please collect the materials from the stores.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'WORKFLOW',
        1,
        NOW(),
        'SYSTEM'
    );

-- =====================================================
-- Alert Templates
-- =====================================================

-- Inventory Reorder Alert
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'INVENTORY_REORDER_ALERT',
        'Inventory Reorder Alert',
        'Low Stock Alert: ${itemCount} Items Below Reorder Level',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #e74c3c;">Inventory Reorder Alert</h2><p>Dear Inventory Team,</p><p>The following materials have fallen below their reorder levels and require immediate attention:</p><p><strong>Total Items Requiring Reorder: ${itemCount}</strong></p>${itemsTable}<p style="margin-top: 20px; color: #e74c3c;"><strong>Action Required:</strong> Please create purchase requisitions for the above items.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'ALERT',
        1,
        NOW(),
        'SYSTEM'
    );

-- Pending Indent Reminder
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'PENDING_INDENT_REMINDER',
        'Pending Indent Reminder',
        'Reminder: ${indentCount} Indents Pending Approval',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #f39c12;">Pending Indent Reminder</h2><p>Dear ${approverName},</p><p>You have <strong>${indentCount} indent(s)</strong> pending your approval:</p>${indentsTable}<p style="margin-top: 20px;">Please review and take appropriate action at your earliest convenience.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'ALERT',
        1,
        NOW(),
        'SYSTEM'
    );

-- =====================================================
-- Report Templates
-- =====================================================

-- Daily Inventory Summary
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'DAILY_INVENTORY_SUMMARY',
        'Daily Inventory Summary',
        'Daily Inventory Summary - ${reportDate}',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #2c3e50;">Daily Inventory Summary</h2><p>Dear Team,</p><p>Here is the inventory summary for <strong>${reportDate}</strong>:</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #3498db; color: white;"><th style="padding: 10px; text-align: left;">Metric</th><th style="padding: 10px; text-align: right;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Total Materials</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">${totalMaterials}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Items Below Reorder</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right; color: #e74c3c;">${belowReorderCount}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Total Inventory Value</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">Rs.${totalValue}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Items Received Today</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">${receivedToday}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Items Issued Today</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">${issuedToday}</td></tr></table><p style="margin-top: 20px;">For detailed reports, please access the ProcureZone dashboard.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'REPORT',
        1,
        NOW(),
        'SYSTEM'
    );

-- Monthly Procurement Analytics
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'MONTHLY_PROCUREMENT_ANALYTICS',
        'Monthly Procurement Analytics',
        'Monthly Procurement Analytics - ${monthYear}',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #2c3e50;">Monthly Procurement Analytics</h2><p>Dear Management,</p><p>Here is the procurement analytics for <strong>${monthYear}</strong>:</p><h3 style="color: #3498db;">Order Summary</h3><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #3498db; color: white;"><th style="padding: 10px; text-align: left;">Metric</th><th style="padding: 10px; text-align: right;">Value</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Total POs Created</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">${totalPOs}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Total Procurement Value</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">Rs.${totalValue}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Average Order Value</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">Rs.${avgOrderValue}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">On-Time Delivery Rate</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">${onTimeDeliveryRate}%</td></tr></table>${vendorsTable}${categoriesTable}<p style="margin-top: 20px;">For detailed analytics, please access the ProcureZone dashboard.</p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'REPORT',
        1,
        NOW(),
        'SYSTEM'
    );

-- Data Cleanup Summary
INSERT INTO
    tbl_email_template (
        template_code,
        template_name,
        template_subject,
        template_body,
        template_type,
        template_category,
        template_status,
        template_lmd,
        template_lmu
    )
VALUES (
        'DATA_CLEANUP_SUMMARY',
        'Data Cleanup Summary',
        'Data Cleanup Summary - ${cleanupDate}',
        '<html><body style="font-family: Arial, sans-serif; line-height: 1.6;"><h2 style="color: #2c3e50;">Data Cleanup Summary</h2><p>Dear Admin,</p><p>The scheduled data cleanup job completed on <strong>${cleanupDate}</strong>:</p><table style="border-collapse: collapse; width: 100%; max-width: 600px;"><tr style="background-color: #3498db; color: white;"><th style="padding: 10px; text-align: left;">Data Type</th><th style="padding: 10px; text-align: right;">Records Processed</th></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Email Logs Archived</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">${emailLogsArchived}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Audit Logs Archived</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">${auditLogsArchived}</td></tr><tr style="background-color: #f9f9f9;"><td style="padding: 10px; border: 1px solid #ddd;">Temp Files Deleted</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">${tempFilesDeleted}</td></tr><tr><td style="padding: 10px; border: 1px solid #ddd;">Space Recovered</td><td style="padding: 10px; border: 1px solid #ddd; text-align: right;">${spaceRecovered} MB</td></tr></table><p style="margin-top: 20px;">Job Status: <strong style="color: #27ae60;">${status}</strong></p><p>Best Regards,<br>ProcureZone System</p></body></html>',
        'HTML',
        'SYSTEM',
        1,
        NOW(),
        'SYSTEM'
    );

-- Verify templates created
SELECT
    template_id,
    template_code,
    template_name,
    template_category,
    template_status
FROM tbl_email_template
ORDER BY template_id;