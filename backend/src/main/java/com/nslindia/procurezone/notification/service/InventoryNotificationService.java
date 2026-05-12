package com.nslindia.procurezone.notification.service;

import com.nslindia.procurezone.notification.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for triggering email notifications on inventory events
 * Sends alerts for low stock, out of stock, and other inventory issues
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryNotificationService {

    private final EmailService emailService;
    private static final String STORES_EMAIL = "stores@nslindia.com";
    private static final String PROCUREMENT_EMAIL = "procurement@nslindia.com";

    /**
     * Send low stock alert notification
     */
    public void sendLowStockAlert(String materialCode, String materialName,
            Double currentQty, Double reorderLevel, String uom) {
        log.info("Sending low stock alert for material: {}", materialCode);

        Map<String, Object> variables = new HashMap<>();
        variables.put("materialCode", materialCode);
        variables.put("materialName", materialName);
        variables.put("currentQty", String.valueOf(currentQty));
        variables.put("reorderLevel", String.valueOf(reorderLevel));
        variables.put("uom", uom);
        variables.put("shortfall", String.valueOf(reorderLevel - currentQty));

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("⚠️ Low Stock Alert - " + materialCode + " - " + materialName)
                .body(buildLowStockEmail(variables))
                .isHtml(true)
                .build();

        emailRequest.addToRecipient(STORES_EMAIL);
        emailRequest.addCcRecipient(PROCUREMENT_EMAIL);

        emailService.sendEmail(emailRequest);
    }

    /**
     * Send critical stock alert (below minimum level)
     */
    public void sendCriticalStockAlert(String materialCode, String materialName,
            Double currentQty, Double minLevel, String uom) {
        log.info("Sending critical stock alert for material: {}", materialCode);

        Map<String, Object> variables = new HashMap<>();
        variables.put("materialCode", materialCode);
        variables.put("materialName", materialName);
        variables.put("currentQty", String.valueOf(currentQty));
        variables.put("minLevel", String.valueOf(minLevel));
        variables.put("uom", uom);

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("🚨 CRITICAL STOCK ALERT - " + materialCode + " - " + materialName)
                .body(buildCriticalStockEmail(variables))
                .isHtml(true)
                .build();

        emailRequest.addToRecipient(STORES_EMAIL);
        emailRequest.addToRecipient(PROCUREMENT_EMAIL);

        emailService.sendEmail(emailRequest);
    }

    /**
     * Send GRN received notification
     */
    public void sendGRNReceivedNotification(String grnNumber, String poNumber,
            String vendorName, String recipientEmail) {
        log.info("Sending GRN received notification for GRN: {}", grnNumber);

        Map<String, Object> variables = new HashMap<>();
        variables.put("grnNumber", grnNumber);
        variables.put("poNumber", poNumber);
        variables.put("vendorName", vendorName);
        variables.put("receivedDate", java.time.LocalDateTime.now().toString());

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("Material Received - GRN " + grnNumber)
                .body(buildGRNReceivedEmail(variables))
                .isHtml(true)
                .build();

        emailRequest.addToRecipient(recipientEmail);
        emailRequest.addCcRecipient(STORES_EMAIL);

        emailService.sendEmail(emailRequest);
    }

    /**
     * Send issue note approved notification
     */
    public void sendIssueNoteApprovedNotification(String issueNoteNo, String requesterEmail,
            List<String> materials) {
        log.info("Sending issue note approved notification for: {}", issueNoteNo);

        Map<String, Object> variables = new HashMap<>();
        variables.put("issueNoteNo", issueNoteNo);
        variables.put("materials", String.join(", ", materials));
        variables.put("approvedDate", java.time.LocalDateTime.now().toString());

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("Material Issue Approved - " + issueNoteNo)
                .body(buildIssueNoteApprovedEmail(variables))
                .isHtml(true)
                .build();

        emailRequest.addToRecipient(requesterEmail);

        emailService.sendEmail(emailRequest);
    }

    /**
     * Send daily stock summary to stores team
     */
    public void sendDailyStockSummary(int totalMaterials, int lowStockItems,
            int criticalStockItems, Double totalValue) {
        log.info("Sending daily stock summary");

        Map<String, Object> variables = new HashMap<>();
        variables.put("totalMaterials", String.valueOf(totalMaterials));
        variables.put("lowStockItems", String.valueOf(lowStockItems));
        variables.put("criticalStockItems", String.valueOf(criticalStockItems));
        variables.put("totalValue", String.format("%.2f", totalValue));
        variables.put("reportDate", java.time.LocalDate.now().toString());

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("Daily Inventory Summary - " + java.time.LocalDate.now())
                .body(buildDailyStockSummaryEmail(variables))
                .isHtml(true)
                .build();

        emailRequest.addToRecipient(STORES_EMAIL);

        emailService.sendEmail(emailRequest);
    }

    // ============================================================================
    // EMAIL TEMPLATES
    // ============================================================================

    private String buildLowStockEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #f59e0b;">⚠️ Low Stock Alert</h2>
                                <p>The following material has reached its reorder level:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #fef3c7;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Material Code:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Material Name:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr style="background-color: #fef3c7;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Current Quantity:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s %s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Reorder Level:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s %s</td>
                                    </tr>
                                    <tr style="background-color: #fef3c7;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Shortfall:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s %s</td>
                                    </tr>
                                </table>
                                <p><strong>Action Required:</strong> Please initiate procurement to replenish stock.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>ProcureZone System</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("materialCode"),
                vars.get("materialName"),
                vars.get("currentQty"), vars.get("uom"),
                vars.get("reorderLevel"), vars.get("uom"),
                vars.get("shortfall"), vars.get("uom"));
    }

    private String buildCriticalStockEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 2px solid #ef4444; border-radius: 5px; background-color: #fef2f2;">
                                <h2 style="color: #dc2626;">🚨 CRITICAL STOCK ALERT</h2>
                                <p><strong style="color: #dc2626;">URGENT:</strong> The following material is below minimum stock level:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #fee2e2;">
                                        <td style="padding: 10px; border: 1px solid #dc2626;"><strong>Material Code:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #dc2626;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #dc2626;"><strong>Material Name:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #dc2626;">%s</td>
                                    </tr>
                                    <tr style="background-color: #fee2e2;">
                                        <td style="padding: 10px; border: 1px solid #dc2626;"><strong>Current Quantity:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #dc2626;">%s %s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #dc2626;"><strong>Minimum Level:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #dc2626;">%s %s</td>
                                    </tr>
                                </table>
                                <p><strong style="color: #dc2626;">IMMEDIATE ACTION REQUIRED:</strong> Expedite procurement to avoid stockout.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>ProcureZone System</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("materialCode"),
                vars.get("materialName"),
                vars.get("currentQty"), vars.get("uom"),
                vars.get("minLevel"), vars.get("uom"));
    }

    private String buildGRNReceivedEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #10b981;">✓ Material Received</h2>
                                <p>Materials have been received and recorded:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>GRN Number:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>PO Number:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Vendor:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Received Date:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                </table>
                                <p>Your requested materials are now available in the stores.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>Stores Department</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("grnNumber"),
                vars.get("poNumber"),
                vars.get("vendorName"),
                vars.get("receivedDate"));
    }

    private String buildIssueNoteApprovedEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #10b981;">✓ Material Issue Approved</h2>
                                <p>Your material issue request has been approved:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Issue Note No:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Materials:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Approved Date:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                </table>
                                <p>Please collect the materials from the stores.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>Stores Department</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("issueNoteNo"),
                vars.get("materials"),
                vars.get("approvedDate"));
    }

    private String buildDailyStockSummaryEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #2563eb;">📊 Daily Inventory Summary</h2>
                                <p>Report Date: %s</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Total Materials:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Low Stock Items:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd; color: #f59e0b;">%s</td>
                                    </tr>
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Critical Stock Items:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd; color: #dc2626;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Total Inventory Value:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">₹ %s</td>
                                    </tr>
                                </table>
                                <p>Please review and take necessary actions for low/critical stock items.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>ProcureZone System</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("reportDate"),
                vars.get("totalMaterials"),
                vars.get("lowStockItems"),
                vars.get("criticalStockItems"),
                vars.get("totalValue"));
    }
}
