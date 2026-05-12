package com.nslindia.procurezone.notification.service;

import com.nslindia.procurezone.po.PurchaseOrder;
import com.nslindia.procurezone.vendor.Vendor;
import com.nslindia.procurezone.vendor.VendorRepository;
import com.nslindia.procurezone.notification.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for triggering email notifications on purchase order events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PONotificationService {

    private final EmailService emailService;
    private final VendorRepository vendorRepository;

    /**
     * Send notification when PO is created
     */
    public void sendPOCreatedNotification(PurchaseOrder po, String vendorEmail, String procurementEmail) {
        log.info("Sending PO created notification for PO: {}", po.getPoNumber());

        // Lookup vendor by vendorId
        Vendor vendor = vendorRepository.findById(po.getVendorId())
                .orElse(null);
        String vendorName = vendor != null ? vendor.getVendorName() : "N/A";

        Map<String, Object> variables = new HashMap<>();
        variables.put("poNumber", po.getPoNumber());
        variables.put("vendorName", vendorName);
        variables.put("totalAmount", po.getTotalAmount());
        variables.put("deliveryDate", po.getDeliveryDate() != null ? po.getDeliveryDate().toString() : "N/A");

        // Email to vendor
        EmailRequest vendorEmailRequest = EmailRequest.builder()
                .subject("New Purchase Order - " + po.getPoNumber())
                .body(buildPOVendorEmail(variables))
                .isHtml(true)
                .build();
        vendorEmailRequest.addToRecipient(vendorEmail);
        emailService.sendEmail(vendorEmailRequest);

        // Email to procurement team
        EmailRequest procEmail = EmailRequest.builder()
                .subject("Purchase Order Created - " + po.getPoNumber())
                .body(buildPOProcurementEmail(variables))
                .isHtml(true)
                .build();
        procEmail.addToRecipient(procurementEmail);
        emailService.sendEmail(procEmail);
    }

    /**
     * Send notification when PO is approved
     */
    public void sendPOApprovedNotification(PurchaseOrder po, String vendorEmail) {
        log.info("Sending PO approved notification for PO: {}", po.getPoNumber());

        // Lookup vendor by vendorId
        Vendor vendor = vendorRepository.findById(po.getVendorId())
                .orElse(null);
        String vendorName = vendor != null ? vendor.getVendorName() : "N/A";

        Map<String, Object> variables = new HashMap<>();
        variables.put("poNumber", po.getPoNumber());
        variables.put("vendorName", vendorName);

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("Purchase Order Approved - " + po.getPoNumber())
                .body(buildPOApprovedEmail(variables))
                .isHtml(true)
                .build();

        emailRequest.addToRecipient(vendorEmail);
        emailService.sendEmail(emailRequest);
    }

    /**
     * Send delivery reminder notification
     */
    public void sendDeliveryReminderNotification(PurchaseOrder po, String vendorEmail, int daysUntilDelivery) {
        log.info("Sending delivery reminder for PO: {}, days until delivery: {}", po.getPoNumber(), daysUntilDelivery);

        // Lookup vendor by vendorId
        Vendor vendor = vendorRepository.findById(po.getVendorId())
                .orElse(null);
        String vendorName = vendor != null ? vendor.getVendorName() : "N/A";

        Map<String, Object> variables = new HashMap<>();
        variables.put("poNumber", po.getPoNumber());
        variables.put("vendorName", vendorName);
        variables.put("deliveryDate", po.getDeliveryDate() != null ? po.getDeliveryDate().toString() : "N/A");
        variables.put("daysRemaining", String.valueOf(daysUntilDelivery));

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("Delivery Reminder - PO " + po.getPoNumber() + " due in " + daysUntilDelivery + " days")
                .body(buildDeliveryReminderEmail(variables))
                .isHtml(true)
                .build();

        emailRequest.addToRecipient(vendorEmail);
        emailService.sendEmail(emailRequest);
    }

    // ============================================================================
    // EMAIL TEMPLATES
    // ============================================================================

    private String buildPOVendorEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #2563eb;">New Purchase Order</h2>
                                <p>Dear %s,</p>
                                <p>We are pleased to inform you that a new purchase order has been issued:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>PO Number:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Total Amount:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">₹ %s</td>
                                    </tr>
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Delivery Date:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                </table>
                                <p>Please confirm receipt and arrange for timely delivery.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>Procurement Team - NSL India</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("vendorName"),
                vars.get("poNumber"),
                vars.get("totalAmount"),
                vars.get("deliveryDate"));
    }

    private String buildPOProcurementEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #2563eb;">Purchase Order Created</h2>
                                <p>A new purchase order has been created in the system:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>PO Number:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Vendor:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Total Amount:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">₹ %s</td>
                                    </tr>
                                </table>
                                <p>Please track the delivery progress.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>ProcureZone System</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("poNumber"),
                vars.get("vendorName"),
                vars.get("totalAmount"));
    }

    private String buildPOApprovedEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #10b981;">✓ Purchase Order Approved</h2>
                                <p>Dear %s,</p>
                                <p>Your purchase order has been approved and is now confirmed:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>PO Number:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                </table>
                                <p>Please proceed with the delivery as per the terms.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>Procurement Team - NSL India</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("vendorName"),
                vars.get("poNumber"));
    }

    private String buildDeliveryReminderEmail(Map<String, Object> vars) {
        return String.format(
                """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                                <h2 style="color: #f59e0b;">⏰ Delivery Reminder</h2>
                                <p>Dear %s,</p>
                                <p>This is a reminder that the delivery date for the following PO is approaching:</p>
                                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>PO Number:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Delivery Date:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                    </tr>
                                    <tr style="background-color: #f8f9fa;">
                                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Days Remaining:</strong></td>
                                        <td style="padding: 10px; border: 1px solid #ddd;">%s days</td>
                                    </tr>
                                </table>
                                <p>Please ensure timely delivery to avoid any delays.</p>
                                <p style="margin-top: 30px;">Best regards,<br/>Procurement Team - NSL India</p>
                            </div>
                        </body>
                        </html>
                        """,
                vars.get("vendorName"),
                vars.get("poNumber"),
                vars.get("deliveryDate"),
                vars.get("daysRemaining"));
    }
}
