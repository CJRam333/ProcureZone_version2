package com.nslindia.procurezone.notification.service;

import com.nslindia.procurezone.indent.Indent;
import com.nslindia.procurezone.notification.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for triggering email notifications on indent events
 * Automatically sends emails when indents are submitted, approved, or rejected
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IndentNotificationService {

    private final EmailService emailService;

    /**
     * Send notification when indent is submitted for approval
     */
    public void sendIndentSubmittedNotification(Indent indent, String approverEmail) {
        log.info("Sending indent submitted notification for indent: {}", indent.getIndentNumber());

        Map<String, Object> variables = new HashMap<>();
        variables.put("indentNo", indent.getIndentNumber());
        variables.put("department", indent.getDepartment() != null ? indent.getDepartment().getName() : "N/A");
        variables.put("requiredBy", indent.getDeliveryDate() != null ? indent.getDeliveryDate().toString() : "N/A");
        variables.put("approverName", "Approver");

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("New Indent Pending Your Approval - " + indent.getIndentNumber())
                .body(buildIndentSubmittedEmail(variables))
                .isHtml(true)
                .build();

        emailRequest.addToRecipient(approverEmail);

        emailService.sendEmail(emailRequest);
    }

    /**
     * Send notification when indent is approved
     */
    public void sendIndentApprovedNotification(Indent indent, String approverName, String level) {
        log.info("Sending indent approved notification for indent: {}", indent.getIndentNumber());

        String creatorEmail = getCreatorEmail(indent);
        if (creatorEmail == null) {
            log.warn("Cannot send approval notification - creator email not found");
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("indentNo", indent.getIndentNumber());
        variables.put("approverName", approverName);
        variables.put("level", level);
        variables.put("approvedDate", java.time.LocalDateTime.now().toString());

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("Indent Approved - " + indent.getIndentNumber())
                .body(buildIndentApprovedEmail(variables))
                .isHtml(true)
                .build();

        emailRequest.addToRecipient(creatorEmail);

        emailService.sendEmail(emailRequest);
    }

    /**
     * Send notification when indent is rejected
     */
    public void sendIndentRejectedNotification(Indent indent, String rejectorName,
            String reason, String level) {
        log.info("Sending indent rejected notification for indent: {}", indent.getIndentNumber());

        String creatorEmail = getCreatorEmail(indent);
        if (creatorEmail == null) {
            log.warn("Cannot send rejection notification - creator email not found");
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("indentNo", indent.getIndentNumber());
        variables.put("rejectorName", rejectorName);
        variables.put("level", level);
        variables.put("reason", reason != null ? reason : "No reason provided");
        variables.put("rejectedDate", java.time.LocalDateTime.now().toString());

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("Indent Rejected - " + indent.getIndentNumber())
                .body(buildIndentRejectedEmail(variables))
                .isHtml(true)
                .build();

        emailRequest.addToRecipient(creatorEmail);

        emailService.sendEmail(emailRequest);
    }

    /**
     * Send notification when PO is created from indent
     */
    public void sendPOCreatedNotification(Indent indent, String poNumber) {
        log.info("Sending PO created notification for indent: {}", indent.getIndentNumber());

        String creatorEmail = getCreatorEmail(indent);
        if (creatorEmail == null) {
            log.warn("Cannot send PO created notification - creator email not found");
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("indentNo", indent.getIndentNumber());
        variables.put("poNumber", poNumber);
        variables.put("createdDate", java.time.LocalDateTime.now().toString());

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("Purchase Order Created - " + poNumber + " for Indent " + indent.getIndentNumber())
                .body(buildPOCreatedEmail(variables))
                .isHtml(true)
                .build();

        emailRequest.addToRecipient(creatorEmail);

        emailService.sendEmail(emailRequest);
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private String getCreatorEmail(Indent indent) {
        if (indent.getCreatedBy() != null) {
            return indent.getCreatedBy().getEmail();
        }
        return null;
    }

    private String getPriorityLabel(Integer priority) {
        if (priority == null)
            return "Normal";
        return switch (priority) {
            case 1 -> "High";
            case 2 -> "Medium";
            default -> "Normal";
        };
    }

    // ============================================================================
    // EMAIL TEMPLATES
    // ============================================================================

    private String buildIndentSubmittedEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #2563eb;">New Indent Pending Your Approval</h2>
                                <p>Dear %s,</p>
                                <p>A new indent has been submitted and requires your approval:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Indent No:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Department:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Required By:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Priority:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                </table>
                                <p>Please log in to the ProcureZone system to review and approve this indent.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>ProcureZone System</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("approverName"),
                vars.get("indentNo"),
                vars.get("department"),
                vars.get("requiredBy"),
                vars.get("priority"));
    }

    private String buildIndentApprovedEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #10b981;">✓ Indent Approved</h2>
                                <p>Your indent has been approved:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Indent No:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Approved By:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s (%s)</td>
                                    </tr>
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Approved Date:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                </table>
                                <p>Your indent will now proceed to the next stage.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>ProcureZone System</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("indentNo"),
                vars.get("approverName"),
                vars.get("level"),
                vars.get("approvedDate"));
    }

    private String buildIndentRejectedEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #ef4444;">✗ Indent Rejected</h2>
                                <p>Your indent has been rejected:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Indent No:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Rejected By:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s (%s)</td>
                                    </tr>
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Rejected Date:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Reason:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                </table>
                                <p>Please review the reason for rejection and make necessary corrections.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>ProcureZone System</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("indentNo"),
                vars.get("rejectorName"),
                vars.get("level"),
                vars.get("rejectedDate"),
                vars.get("reason"));
    }

    private String buildPOCreatedEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #2563eb;">Purchase Order Created</h2>
                                <p>A Purchase Order has been created for your indent:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Indent No:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>PO Number:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Created Date:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                </table>
                                <p>The procurement process for your requested materials is now underway.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>ProcureZone System</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("indentNo"),
                vars.get("poNumber"),
                vars.get("createdDate"));
    }
}
