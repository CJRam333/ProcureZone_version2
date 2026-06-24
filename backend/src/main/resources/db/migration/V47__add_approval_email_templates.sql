-- V47: Add workflow approval/rejection email templates
-- Covers indent and issue note lifecycle notification points

INSERT INTO tbl_email_template (
    template_code, template_name, template_subject, template_body,
    template_type, template_category, template_description,
    template_placeholders, is_html, template_status, template_lmd, template_lmu
) VALUES

-- INDENT: Created / Submitted for approval (notifies RM approver)
('INDENT_CREATED',
 'Indent Submitted for Approval',
 'New Indent Pending Approval - ${indentNumber}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#2563eb">New Indent Pending Your Approval</h2>
<p>A new indent has been submitted and requires your approval:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Indent No:</strong></td><td style="padding:10px;border:1px solid #ddd">${indentNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Requested By:</strong></td><td style="padding:10px;border:1px solid #ddd">${creatorName}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Delivery Date:</strong></td><td style="padding:10px;border:1px solid #ddd">${deliveryDate}</td></tr>
</table>
<p>Please log in to ProcureZone to review and act on this indent.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent to RM approver when an indent is submitted',
 '["indentNumber","department","creatorName","deliveryDate"]',
 1, 1, CURDATE(), 'SYSTEM'),

-- INDENT: RM (L1) Approved
('INDENT_L1_APPROVED',
 'Indent RM Approved',
 'Indent Approved by RM - ${indentNumber}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#10b981">&#10003; Indent Approved by Reporting Manager</h2>
<p>Dear ${creatorName},</p>
<p>Your indent has been approved by the Reporting Manager:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Indent No:</strong></td><td style="padding:10px;border:1px solid #ddd">${indentNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Approved By:</strong></td><td style="padding:10px;border:1px solid #ddd">${approverName}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Approval Date:</strong></td><td style="padding:10px;border:1px solid #ddd">${approvalDate}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Remarks:</strong></td><td style="padding:10px;border:1px solid #ddd">${remarks}</td></tr>
</table>
<p>Your indent will now proceed to the Department Head for final approval.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent to indent creator when RM approves (L1)',
 '["indentNumber","department","creatorName","approverName","approvalDate","remarks"]',
 1, 1, CURDATE(), 'SYSTEM'),

-- INDENT: RM (L1) Rejected
('INDENT_L1_REJECTED',
 'Indent RM Rejected',
 'Indent Rejected by RM - ${indentNumber}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#ef4444">&#10007; Indent Rejected by Reporting Manager</h2>
<p>Dear ${creatorName},</p>
<p>Your indent has been rejected by the Reporting Manager:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Indent No:</strong></td><td style="padding:10px;border:1px solid #ddd">${indentNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Rejected By:</strong></td><td style="padding:10px;border:1px solid #ddd">${rejectorName}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Rejection Date:</strong></td><td style="padding:10px;border:1px solid #ddd">${rejectionDate}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Reason:</strong></td><td style="padding:10px;border:1px solid #ddd">${reason}</td></tr>
</table>
<p>Please review the remarks and resubmit with corrections if needed.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent to indent creator when RM rejects (L1)',
 '["indentNumber","department","creatorName","rejectorName","rejectionDate","reason"]',
 1, 1, CURDATE(), 'SYSTEM'),

-- INDENT: Department Head (L2) Approved
('INDENT_L2_APPROVED',
 'Indent Final Approved',
 'Indent Final Approval Granted - ${indentNumber}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#10b981">&#10003; Indent Approved by Department Head</h2>
<p>Dear ${creatorName},</p>
<p>Your indent has received final approval from the Department Head:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Indent No:</strong></td><td style="padding:10px;border:1px solid #ddd">${indentNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Approved By:</strong></td><td style="padding:10px;border:1px solid #ddd">${approverName}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Approval Date:</strong></td><td style="padding:10px;border:1px solid #ddd">${approvalDate}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Remarks:</strong></td><td style="padding:10px;border:1px solid #ddd">${remarks}</td></tr>
</table>
<p>Your indent is now forwarded to the Procurement team for processing.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent to indent creator when Dept Head gives final approval (L2)',
 '["indentNumber","department","creatorName","approverName","approvalDate","remarks"]',
 1, 1, CURDATE(), 'SYSTEM'),

-- INDENT: Department Head (L2) Rejected
('INDENT_L2_REJECTED',
 'Indent Final Rejected',
 'Indent Rejected by Department Head - ${indentNumber}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#ef4444">&#10007; Indent Rejected by Department Head</h2>
<p>Dear ${creatorName},</p>
<p>Your indent has been rejected by the Department Head:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Indent No:</strong></td><td style="padding:10px;border:1px solid #ddd">${indentNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Rejected By:</strong></td><td style="padding:10px;border:1px solid #ddd">${rejectorName}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Rejection Date:</strong></td><td style="padding:10px;border:1px solid #ddd">${rejectionDate}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Reason:</strong></td><td style="padding:10px;border:1px solid #ddd">${reason}</td></tr>
</table>
<p>Please review the remarks and make necessary corrections before resubmitting.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent to indent creator when Dept Head rejects (L2)',
 '["indentNumber","department","creatorName","rejectorName","rejectionDate","reason"]',
 1, 1, CURDATE(), 'SYSTEM'),

-- INDENT: Cancelled
('INDENT_CANCELLED',
 'Indent Cancelled',
 'Indent Cancelled - ${indentNumber}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#6b7280">Indent Cancelled</h2>
<p>Dear ${creatorName},</p>
<p>The following indent has been cancelled:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Indent No:</strong></td><td style="padding:10px;border:1px solid #ddd">${indentNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Cancelled By:</strong></td><td style="padding:10px;border:1px solid #ddd">${cancelledByName}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Cancellation Date:</strong></td><td style="padding:10px;border:1px solid #ddd">${cancellationDate}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Reason:</strong></td><td style="padding:10px;border:1px solid #ddd">${reason}</td></tr>
</table>
<p>If you believe this is an error, please contact your supervisor or raise a new indent.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent to indent creator when an indent is cancelled',
 '["indentNumber","department","creatorName","cancelledByName","cancellationDate","reason"]',
 1, 1, CURDATE(), 'SYSTEM'),

-- ISSUE NOTE: Created
('ISSUE_NOTE_CREATED',
 'Issue Note Created',
 'Issue Note Submitted for RM Approval - ${issueNoteNumber}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#2563eb">New Issue Note Pending Approval</h2>
<p>An issue note has been submitted and requires your approval:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Issue Note No:</strong></td><td style="padding:10px;border:1px solid #ddd">${issueNoteNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Issued To:</strong></td><td style="padding:10px;border:1px solid #ddd">${issuedTo}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Created By:</strong></td><td style="padding:10px;border:1px solid #ddd">${createdBy}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Issue Date:</strong></td><td style="padding:10px;border:1px solid #ddd">${issueDate}</td></tr>
</table>
<p>Please log in to ProcureZone to review and approve this issue note.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent when an issue note is created and awaiting RM approval',
 '["issueNoteNumber","department","issuedTo","createdBy","issueDate"]',
 1, 1, CURDATE(), 'SYSTEM'),

-- ISSUE NOTE: RM Approved
('ISSUE_NOTE_RM_APPROVED',
 'Issue Note RM Approved',
 'Issue Note Approved - ${issueNoteNumber}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#10b981">&#10003; Issue Note Approved by RM</h2>
<p>Your issue note has been approved by the Reporting Manager:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Issue Note No:</strong></td><td style="padding:10px;border:1px solid #ddd">${issueNoteNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Approved By:</strong></td><td style="padding:10px;border:1px solid #ddd">${approverName}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Approval Date:</strong></td><td style="padding:10px;border:1px solid #ddd">${approvalDate}</td></tr>
</table>
<p>The issue note will now proceed to Stores for goods issuance.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent to issue note creator when RM approves',
 '["issueNoteNumber","department","approverName","approvalDate"]',
 1, 1, CURDATE(), 'SYSTEM'),

-- ISSUE NOTE: RM Rejected
('ISSUE_NOTE_RM_REJECTED',
 'Issue Note RM Rejected',
 'Issue Note Rejected by RM - ${issueNoteNumber}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#ef4444">&#10007; Issue Note Rejected by RM</h2>
<p>Your issue note has been rejected by the Reporting Manager:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Issue Note No:</strong></td><td style="padding:10px;border:1px solid #ddd">${issueNoteNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Rejected By:</strong></td><td style="padding:10px;border:1px solid #ddd">${rejectorName}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Rejection Date:</strong></td><td style="padding:10px;border:1px solid #ddd">${rejectionDate}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Reason:</strong></td><td style="padding:10px;border:1px solid #ddd">${reason}</td></tr>
</table>
<p>Please review the feedback and create a corrected issue note if needed.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent to issue note creator when RM rejects',
 '["issueNoteNumber","department","rejectorName","rejectionDate","reason"]',
 1, 1, CURDATE(), 'SYSTEM'),

-- ISSUE NOTE: Goods Issued by Stores
('ISSUE_NOTE_ISSUED',
 'Issue Note Goods Issued',
 'Goods Issued - ${issueNoteNumber}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#10b981">&#10003; Goods Issued from Stores</h2>
<p>The materials in your issue note have been issued from Stores:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Issue Note No:</strong></td><td style="padding:10px;border:1px solid #ddd">${issueNoteNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Issued By:</strong></td><td style="padding:10px;border:1px solid #ddd">${issuedByName}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Issue Date:</strong></td><td style="padding:10px;border:1px solid #ddd">${issueDate}</td></tr>
</table>
<p>Please collect the materials from the Stores as per this issue note.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent to issue note creator when stores issues the goods',
 '["issueNoteNumber","department","issuedByName","issueDate"]',
 1, 1, CURDATE(), 'SYSTEM'),

-- ISSUE NOTE: Rejected by Stores
('ISSUE_NOTE_STORES_REJECTED',
 'Issue Note Rejected by Stores',
 'Issue Note Rejected by Stores - ${issueNoteNumber}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#ef4444">&#10007; Issue Note Rejected by Stores</h2>
<p>Your issue note has been rejected by the Stores department:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Issue Note No:</strong></td><td style="padding:10px;border:1px solid #ddd">${issueNoteNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Rejected By:</strong></td><td style="padding:10px;border:1px solid #ddd">${rejectorName}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Rejection Date:</strong></td><td style="padding:10px;border:1px solid #ddd">${rejectionDate}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Reason:</strong></td><td style="padding:10px;border:1px solid #ddd">${reason}</td></tr>
</table>
<p>Please contact the Stores department or your supervisor for further guidance.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent to issue note creator when Stores rejects the issue',
 '["issueNoteNumber","department","rejectorName","rejectionDate","reason"]',
 1, 1, CURDATE(), 'SYSTEM');
