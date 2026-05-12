package com.nslindia.procurezone.notification.service;

import com.nslindia.procurezone.notification.dto.DailySummaryEmailData;
import com.nslindia.procurezone.notification.dto.DataCleanupEmailData;
import com.nslindia.procurezone.notification.dto.EmailAttachment;
import com.nslindia.procurezone.notification.dto.EmailRequest;
import com.nslindia.procurezone.notification.dto.IndentReminderEmailData;
import com.nslindia.procurezone.notification.dto.InventoryAlertEmailData;
import com.nslindia.procurezone.notification.dto.InventoryReconciliationEmailData;
import com.nslindia.procurezone.notification.dto.MonthlyAnalyticsEmailData;
import com.nslindia.procurezone.notification.dto.PODeliveryReminderEmailData;
import com.nslindia.procurezone.notification.model.EmailLog;
import com.nslindia.procurezone.notification.model.EmailTemplate;
import com.nslindia.procurezone.notification.repository.EmailLogRepository;
import com.nslindia.procurezone.notification.repository.EmailTemplateRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for sending emails with template support
 * Provides HTML email, attachments, and logging functionality
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailLogRepository emailLogRepository;

    // Default recipient list for system notifications (can be configured)
    private static final String PROCUREMENT_TEAM_EMAIL = "procurement@nslindia.com";
    private static final String INVENTORY_TEAM_EMAIL = "inventory@nslindia.com";
    private static final String ADMIN_EMAIL = "admin@nslindia.com";

    /**
     * Send email using template code
     */
    @Transactional
    public boolean sendEmailFromTemplate(String templateCode, Map<String, Object> variables,
            String toAddress) {
        EmailTemplate template = emailTemplateRepository.findByCode(templateCode)
                .orElseThrow(() -> new IllegalArgumentException("Email template not found: " + templateCode));

        String subject = replacePlaceholders(template.getSubject(), variables);
        String body = replacePlaceholders(template.getBody(), variables);

        EmailRequest request = EmailRequest.builder()
                .subject(subject)
                .body(body)
                .templateCode(templateCode)
                .isHtml("HTML".equalsIgnoreCase(template.getType()))
                .build();

        request.addToRecipient(toAddress);

        return sendEmail(request);
    }

    /**
     * Send email with multiple recipients and attachments
     */
    @Transactional
    public boolean sendEmail(EmailRequest request) {
        EmailLog emailLog = null;

        try {
            // Create email log entry
            if (request.isLogEmail()) {
                emailLog = createEmailLog(request);
                emailLogRepository.save(emailLog);
            }

            // Create MIME message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set recipients
            if (request.getTo() != null && !request.getTo().isEmpty()) {
                helper.setTo(request.getTo().toArray(new String[0]));
            } else {
                throw new IllegalArgumentException("At least one TO recipient is required");
            }

            if (request.getCc() != null && !request.getCc().isEmpty()) {
                helper.setCc(request.getCc().toArray(new String[0]));
            }

            if (request.getBcc() != null && !request.getBcc().isEmpty()) {
                helper.setBcc(request.getBcc().toArray(new String[0]));
            }

            // Set subject and body
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), request.isHtml());

            // Add attachments
            if (request.getAttachments() != null) {
                for (EmailAttachment attachment : request.getAttachments()) {
                    helper.addAttachment(
                            attachment.getFilename(),
                            new ByteArrayResource(attachment.getContent()),
                            attachment.getContentType());
                }
            }

            // Send email
            mailSender.send(message);

            // Update log as sent
            if (emailLog != null) {
                emailLog.setStatus("SENT");
                emailLog.setSentDate(LocalDateTime.now());
                emailLogRepository.save(emailLog);
            }

            log.info("Email sent successfully to: {}", request.getTo());
            return true;

        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage(), e);

            // Update log as failed
            if (emailLog != null) {
                emailLog.setStatus("FAILED");
                emailLog.setErrorMessage(e.getMessage());
                emailLogRepository.save(emailLog);
            }

            return false;
        } catch (Exception e) {
            log.error("Unexpected error sending email: {}", e.getMessage(), e);

            if (emailLog != null) {
                emailLog.setStatus("FAILED");
                emailLog.setErrorMessage(e.getMessage());
                emailLogRepository.save(emailLog);
            }

            return false;
        }
    }

    /**
     * Replace placeholders in template with actual values
     * Supports ${variable} syntax
     */
    private String replacePlaceholders(String template, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);

        while (matcher.find()) {
            String placeholder = matcher.group(0); // ${variable}
            String variableName = matcher.group(1); // variable

            Object value = variables.get(variableName);
            if (value != null) {
                result = result.replace(placeholder, value.toString());
            }
        }

        return result;
    }

    /**
     * Create email log entry from request
     */
    private EmailLog createEmailLog(EmailRequest request) {
        return EmailLog.builder()
                .toAddress(request.getTo() != null ? String.join(", ", request.getTo()) : null)
                .ccAddress(request.getCc() != null ? String.join(", ", request.getCc()) : null)
                .bccAddress(request.getBcc() != null ? String.join(", ", request.getBcc()) : null)
                .subject(request.getSubject())
                .body(request.getBody())
                .templateCode(request.getTemplateCode())
                .status("PENDING")
                .triggeredBy("SYSTEM")
                .build();
    }

    /**
     * Retry failed emails
     */
    @Transactional
    public void retryFailedEmails() {
        var failedEmails = emailLogRepository.findFailedEmailsForRetry();

        log.info("Retrying {} failed emails", failedEmails.size());

        for (EmailLog emailLog : failedEmails) {
            try {
                EmailRequest request = EmailRequest.builder()
                        .subject(emailLog.getSubject())
                        .body(emailLog.getBody())
                        .isHtml(true)
                        .logEmail(false) // Don't create new log entry
                        .build();

                String[] toAddresses = emailLog.getToAddress().split(",\\s*");
                for (String address : toAddresses) {
                    request.addToRecipient(address.trim());
                }

                boolean sent = sendEmail(request);

                if (sent) {
                    emailLog.setStatus("SENT");
                    emailLog.setSentDate(LocalDateTime.now());
                    log.info("Successfully retried email to: {}", emailLog.getToAddress());
                } else {
                    emailLog.setRetryCount(emailLog.getRetryCount() + 1);
                }

                emailLogRepository.save(emailLog);

            } catch (Exception e) {
                log.error("Error retrying email {}: {}", emailLog.getId(), e.getMessage());
                emailLog.setRetryCount(emailLog.getRetryCount() + 1);
                emailLog.setErrorMessage(e.getMessage());
                emailLogRepository.save(emailLog);
            }
        }
    }

    // =====================================================
    // Scheduled Job Email Methods (using DTOs)
    // =====================================================

    /**
     * Send inventory reorder alert email
     */
    public boolean sendInventoryReorderAlert(InventoryAlertEmailData data) {
        Map<String, Object> variables = Map.of(
                "criticalCount", String.valueOf(data.getCriticalCount()),
                "lowStockCount", String.valueOf(data.getLowStockCount()),
                "criticalItemsTable", data.getCriticalItemsTable(),
                "lowStockItemsTable", data.getLowStockItemsTable(),
                "totalValueAtRisk", formatCurrency(data.getTotalValueAtRisk()),
                "generatedDate", formatDateTime(LocalDateTime.now()));

        List<String> recipients = data.getRecipients();
        if (recipients == null || recipients.isEmpty()) {
            recipients = List.of(PROCUREMENT_TEAM_EMAIL, INVENTORY_TEAM_EMAIL);
        }
        return sendToMultipleRecipients("INVENTORY_REORDER_ALERT", variables, recipients);
    }

    /**
     * E.1 FIX: Send PO delivery reminder email
     */
    public boolean sendPODeliveryReminder(PODeliveryReminderEmailData data) {
        Map<String, Object> variables = Map.ofEntries(
                Map.entry("upcomingCount", String.valueOf(data.getUpcomingDeliveryCount())),
                Map.entry("upcomingDeliveriesTable", data.getUpcomingDeliveriesTable() != null
                        ? data.getUpcomingDeliveriesTable()
                        : "<p>No upcoming deliveries.</p>"),
                Map.entry("overdueCount", String.valueOf(data.getOverdueCount())),
                Map.entry("overduePOsTable", data.getOverduePOsTable() != null
                        ? data.getOverduePOsTable()
                        : "<p>No overdue POs.</p>"),
                Map.entry("overdueValue", formatCurrency(data.getOverdueValue())),
                Map.entry("maxDaysOverdue", String.valueOf(data.getMaxDaysOverdue())),
                Map.entry("generatedDate", formatDateTime(LocalDateTime.now())),
                Map.entry("vendorName", data.getVendorName() != null ? data.getVendorName() : "Procurement Team"));

        List<String> recipients = data.getRecipients();
        if (recipients == null || recipients.isEmpty()) {
            recipients = List.of(PROCUREMENT_TEAM_EMAIL);
        }
        return sendToMultipleRecipients("PO_DELIVERY_REMINDER", variables, recipients);
    }

    /**
     * Send pending indent reminder to specific approver
     */
    public boolean sendPendingIndentReminder(IndentReminderEmailData data) {
        Map<String, Object> variables = Map.of(
                "approverName", data.getApproverName(),
                "pendingCount", String.valueOf(data.getPendingCount()),
                "pendingIndentsTable", data.getPendingIndentsTable(),
                "overdueCount", String.valueOf(data.getOverdueCount()),
                "overdueIndentsTable", data.getOverdueIndentsTable(),
                "totalPendingValue", formatCurrency(data.getTotalPendingValue()),
                "oldestPendingDays", String.valueOf(data.getOldestPendingDays()),
                "approvalLink", "http://procurezone.nslindia.com/indents/pending");

        return sendToSingleRecipient("PENDING_INDENT_REMINDER", variables, data.getApproverEmail());
    }

    /**
     * Send daily inventory summary email
     */
    public boolean sendDailyInventorySummary(DailySummaryEmailData data) {
        Map<String, Object> variables = Map.ofEntries(
                Map.entry("reportDate", formatDate(LocalDateTime.now())),
                Map.entry("totalMaterials", String.valueOf(data.getTotalMaterials())),
                Map.entry("totalInventoryValue", formatCurrency(data.getTotalInventoryValue())),
                Map.entry("lowStockCount", String.valueOf(data.getLowStockCount())),
                Map.entry("criticalStockCount", String.valueOf(data.getCriticalStockCount())),
                Map.entry("receiptsCount", String.valueOf(data.getReceiptsCount())),
                Map.entry("receiptsValue", formatCurrency(data.getReceiptsValue())),
                Map.entry("issuesCount", String.valueOf(data.getIssuesCount())),
                Map.entry("issuesValue", formatCurrency(data.getIssuesValue())),
                Map.entry("adjustmentsCount", String.valueOf(data.getAdjustmentsCount())),
                Map.entry("netMovement", formatCurrency(data.getNetMovement())),
                Map.entry("netMovementColor", data.getNetMovement() >= 0 ? "#5cb85c" : "#d9534f"),
                Map.entry("topMaterialsTable", data.getTopMaterialsTable()));

        List<String> recipients = data.getRecipients();
        if (recipients == null || recipients.isEmpty()) {
            recipients = List.of(INVENTORY_TEAM_EMAIL);
        }
        return sendToMultipleRecipients("DAILY_INVENTORY_SUMMARY", variables, recipients);
    }

    /**
     * Send monthly procurement analytics email
     */
    public boolean sendMonthlyProcurementAnalytics(MonthlyAnalyticsEmailData data) {
        Map<String, Object> variables = Map.ofEntries(
                Map.entry("monthYear", data.getMonthYear()),
                Map.entry("indentsCreated", String.valueOf(data.getIndentsCreated())),
                Map.entry("indentsApproved", String.valueOf(data.getIndentsApproved())),
                Map.entry("approvalRate", String.format("%.1f", data.getApprovalRate())),
                Map.entry("avgApprovalDays", String.valueOf(data.getAvgApprovalDays())),
                Map.entry("grnCount", String.valueOf(data.getGrnCount())),
                Map.entry("grnTotalValue", formatCurrency(data.getGrnTotalValue())),
                Map.entry("onTimeDeliveryRate", String.format("%.1f", data.getOnTimeDeliveryRate())),
                Map.entry("issueNoteCount", String.valueOf(data.getIssueNoteCount())),
                Map.entry("issuedTotalValue", formatCurrency(data.getIssuedTotalValue())),
                Map.entry("totalReceipts", String.valueOf(data.getTotalReceipts())),
                Map.entry("totalIssues", String.valueOf(data.getTotalIssues())),
                Map.entry("netChange", String.valueOf(data.getNetChange())),
                Map.entry("trendsSection", data.getTrendsSection() != null ? data.getTrendsSection() : ""));

        List<String> recipients = data.getRecipients();
        if (recipients == null || recipients.isEmpty()) {
            recipients = List.of(ADMIN_EMAIL, PROCUREMENT_TEAM_EMAIL);
        }
        return sendToMultipleRecipients("MONTHLY_PROCUREMENT_ANALYTICS", variables, recipients);
    }

    /**
     * Send data cleanup summary email
     */
    public boolean sendDataCleanupSummary(DataCleanupEmailData data) {
        Map<String, Object> variables = Map.of(
                "cleanupDate", formatDateTime(LocalDateTime.now()),
                "archivedTransactionCount", String.valueOf(data.getArchivedTransactionCount()),
                "expiredTokenCount", String.valueOf(data.getExpiredTokenCount()),
                "purgedLogCount", String.valueOf(data.getPurgedLogCount()),
                "spaceReclaimed", String.format("%.2f", data.getSpaceReclaimed()),
                "statusColor", "SUCCESS".equals(data.getCleanupStatus()) ? "#5cb85c" : "#d9534f",
                "cleanupStatus", data.getCleanupStatus(),
                "errorSection", data.getErrorSection() != null ? data.getErrorSection() : "");

        return sendToSingleRecipient("DATA_CLEANUP_SUMMARY", variables, ADMIN_EMAIL);
    }

    /**
     * E.2 FIX: Send inventory reconciliation report email
     */
    public boolean sendInventoryReconciliationReport(InventoryReconciliationEmailData data) {
        Map<String, Object> variables = Map.ofEntries(
                Map.entry("reportPeriod",
                        data.getReportPeriod() != null ? data.getReportPeriod() : formatDate(LocalDateTime.now())),
                Map.entry("totalMaterialsChecked", String.valueOf(data.getTotalMaterialsChecked())),
                Map.entry("discrepancyCount", String.valueOf(data.getDiscrepancyCount())),
                Map.entry("discrepanciesTable", data.getDiscrepanciesTable() != null
                        ? data.getDiscrepanciesTable()
                        : "<p>No discrepancies found.</p>"),
                Map.entry("totalQuantityVariance", String.format("%.3f", data.getTotalQuantityVariance())),
                Map.entry("totalValueVariance", formatCurrency(data.getTotalValueVariance())),
                Map.entry("negativeBalanceCount", String.valueOf(data.getNegativeBalanceCount())),
                Map.entry("negativeBalanceTable", data.getNegativeBalanceTable() != null
                        ? data.getNegativeBalanceTable()
                        : "<p>No negative balances.</p>"),
                Map.entry("orphanedTransactionCount", String.valueOf(data.getOrphanedTransactionCount())),
                Map.entry("statusColor", "PASS".equals(data.getReconciliationStatus()) ? "#5cb85c" : "#d9534f"),
                Map.entry("reconciliationStatus",
                        data.getReconciliationStatus() != null ? data.getReconciliationStatus() : "UNKNOWN"),
                Map.entry("errorSection", data.getErrorSection() != null ? data.getErrorSection() : ""),
                Map.entry("generatedDate", formatDateTime(LocalDateTime.now())));

        List<String> recipients = data.getRecipients();
        if (recipients == null || recipients.isEmpty()) {
            recipients = List.of(INVENTORY_TEAM_EMAIL, ADMIN_EMAIL);
        }
        return sendToMultipleRecipients("INVENTORY_RECONCILIATION_REPORT", variables, recipients);
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * Send email to multiple recipients using template
     */
    private boolean sendToMultipleRecipients(String templateCode, Map<String, Object> variables,
            List<String> recipients) {

        EmailTemplate template = emailTemplateRepository.findByCode(templateCode)
                .orElse(null);

        if (template == null) {
            log.warn("Email template not found: {}. Skipping email.", templateCode);
            return false;
        }

        String subject = replacePlaceholders(template.getSubject(), variables);
        String body = replacePlaceholders(template.getBody(), variables);

        EmailRequest request = EmailRequest.builder()
                .subject(subject)
                .body(body)
                .templateCode(templateCode)
                .isHtml("HTML".equalsIgnoreCase(template.getType()))
                .build();

        for (String recipient : recipients) {
            request.addToRecipient(recipient);
        }

        return sendEmailInternal(request);
    }

    /**
     * Send email to single recipient using template
     */
    private boolean sendToSingleRecipient(String templateCode, Map<String, Object> variables,
            String recipient) {
        return sendToMultipleRecipients(templateCode, variables, List.of(recipient));
    }

    /**
     * Internal email sending without transactional annotation to avoid
     * self-invocation issues
     */
    private boolean sendEmailInternal(EmailRequest request) {
        EmailLog emailLog = null;

        try {
            if (request.isLogEmail()) {
                emailLog = createEmailLog(request);
                emailLogRepository.save(emailLog);
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (request.getTo() != null && !request.getTo().isEmpty()) {
                helper.setTo(request.getTo().toArray(new String[0]));
            } else {
                throw new IllegalArgumentException("At least one TO recipient is required");
            }

            if (request.getCc() != null && !request.getCc().isEmpty()) {
                helper.setCc(request.getCc().toArray(new String[0]));
            }

            if (request.getBcc() != null && !request.getBcc().isEmpty()) {
                helper.setBcc(request.getBcc().toArray(new String[0]));
            }

            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), request.isHtml());

            if (request.getAttachments() != null) {
                for (EmailAttachment attachment : request.getAttachments()) {
                    helper.addAttachment(attachment.getFilename(),
                            new ByteArrayResource(attachment.getContent()),
                            attachment.getContentType());
                }
            }

            mailSender.send(message);

            if (emailLog != null) {
                emailLog.setStatus("SENT");
                emailLog.setSentDate(LocalDateTime.now());
                emailLogRepository.save(emailLog);
            }

            log.info("Email sent successfully to: {}", request.getTo());
            return true;

        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
            updateEmailLogAsFailed(emailLog, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error sending email: {}", e.getMessage(), e);
            updateEmailLogAsFailed(emailLog, e.getMessage());
            return false;
        }
    }

    private void updateEmailLogAsFailed(EmailLog emailLog, String errorMessage) {
        if (emailLog != null) {
            emailLog.setStatus("FAILED");
            emailLog.setErrorMessage(errorMessage);
            emailLogRepository.save(emailLog);
        }
    }

    /**
     * Build HTML table from list of items
     */
    public String buildHtmlTable(List<String> headers, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border=\"1\" cellpadding=\"8\" style=\"border-collapse: collapse;\">");

        // Header row
        sb.append("<tr style=\"background-color: #f5f5f5;\">");
        for (String header : headers) {
            sb.append("<th>").append(header).append("</th>");
        }
        sb.append("</tr>");

        // Data rows
        for (List<String> row : rows) {
            sb.append("<tr>");
            for (String cell : row) {
                sb.append("<td>").append(cell != null ? cell : "-").append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</table>");
        return sb.toString();
    }

    private String formatCurrency(double amount) {
        return String.format("%,.2f", amount);
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));
    }
}
