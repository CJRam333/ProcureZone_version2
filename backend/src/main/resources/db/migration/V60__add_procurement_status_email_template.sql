-- V60: Procurement status-change notification template.
-- Legacy had a "Procurement Indent Request" mail coded but disabled (never called). This re-enables
-- that notification: when Procurement moves an approved indent to Quotations Collected / Negotiation
-- Done / PO Released / Hold / Cash Buy, the indent creator is notified.
-- Placeholder syntax is ${...}; Flyway placeholder replacement is disabled (see application.yml).

INSERT INTO tbl_email_template (
    template_code, template_name, template_subject, template_body,
    template_type, template_category, template_description,
    template_placeholders, is_html, template_status, template_lmd, template_lmu
) VALUES
('INDENT_PROCUREMENT_STATUS_CHANGED',
 'Indent Procurement Status Changed',
 'Procurement Update on Indent ${indentNumber} - ${procurementStatus}',
 '<html><body style="font-family:Arial,sans-serif;color:#333;line-height:1.6">
<div style="max-width:600px;margin:0 auto;padding:20px;border:1px solid #ddd;border-radius:5px">
<h2 style="color:#2563eb">Procurement Status Updated</h2>
<p>Dear ${creatorName},</p>
<p>The procurement status of your indent has been updated:</p>
<table style="width:100%;border-collapse:collapse;margin:20px 0">
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Indent No:</strong></td><td style="padding:10px;border:1px solid #ddd">${indentNumber}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Department:</strong></td><td style="padding:10px;border:1px solid #ddd">${department}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>New Status:</strong></td><td style="padding:10px;border:1px solid #ddd">${procurementStatus}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>PO Number:</strong></td><td style="padding:10px;border:1px solid #ddd">${poNumber}</td></tr>
<tr style="background:#f8f9fa"><td style="padding:10px;border:1px solid #ddd"><strong>Updated On:</strong></td><td style="padding:10px;border:1px solid #ddd">${statusDate}</td></tr>
<tr><td style="padding:10px;border:1px solid #ddd"><strong>Remarks:</strong></td><td style="padding:10px;border:1px solid #ddd">${remarks}</td></tr>
</table>
<p>Please log in to ProcureZone to view the full details.</p>
<p style="margin-top:30px">Best regards,<br/>ProcureZone System</p>
</div></body></html>',
 'NOTIFICATION', 'APPROVAL_WORKFLOW',
 'Sent to the indent creator when Procurement changes the sub-status (Quotations/Negotiation/PO Released/Hold/Cash Buy)',
 '["indentNumber","department","creatorName","procurementStatus","poNumber","statusDate","remarks"]',
 1, 1, CURDATE(), 1);
