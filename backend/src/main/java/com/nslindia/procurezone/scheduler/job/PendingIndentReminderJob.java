package com.nslindia.procurezone.scheduler.job;

import com.nslindia.procurezone.indent.Indent;
import com.nslindia.procurezone.indent.IndentRepository;
import com.nslindia.procurezone.notification.dto.IndentReminderEmailData;
import com.nslindia.procurezone.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scheduled job for pending indent reminders
 * Runs every weekday at 10:00 AM to remind approvers
 * 
 * @author NSL India
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PendingIndentReminderJob implements Job {

    private final IndentRepository indentRepository;
    private final EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("=== Pending Indent Reminder Job Started at {} ===", LocalDateTime.now());

        try {
            final String UNKNOWN = "Unknown";

            // 1. Query for pending indents (status = 2 means SUBMITTED/PENDING)
            List<Indent> allIndents = indentRepository.findAll();
            List<Indent> pendingIndents = allIndents.stream()
                    .filter(indent -> indent.getStatus() != null && indent.getStatus().getId() == 2)
                    .toList();

            if (pendingIndents.isEmpty()) {
                log.info("No pending indents found for approval");
                log.info("=== Pending Indent Reminder Job Completed - No Action Needed ===");
                return;
            }

            log.warn("Found {} pending indents requiring approval", pendingIndents.size());

            // 2. Group by department
            Map<String, List<Indent>> pendingByDepartment = pendingIndents.stream()
                    .collect(Collectors.groupingBy(indent -> indent.getDepartment() != null
                            ? indent.getDepartment().getName()
                            : "Unknown Department"));

            // 3. Log summary by department
            logDepartmentSummary(pendingByDepartment, UNKNOWN);

            // 4. Group by approver (if approvedBy is set)
            logApproverSummary(pendingIndents);

            // 5. Identify and log overdue indents
            int overdueCount = logOverdueIndents(pendingIndents, UNKNOWN);

            // 6. Generate summary
            log.info("=== REMINDER SUMMARY ===");
            log.info("Total pending indents: {}", pendingIndents.size());
            log.info("Departments with pending indents: {}", pendingByDepartment.size());
            log.info("Overdue indents (> 3 days): {}", overdueCount);

            // 7. Send email reminders to approvers
            sendApproverReminders(pendingByDepartment, overdueCount);

            log.info("=== Pending Indent Reminder Job Completed Successfully ===");

        } catch (Exception e) {
            log.error("Error in Pending Indent Reminder Job", e);
            log.error("Job failed at: {}", LocalDateTime.now());
        }
    }

    private void logDepartmentSummary(Map<String, List<Indent>> pendingByDepartment, String unknown) {
        log.warn("=== PENDING INDENTS BY DEPARTMENT ===");
        pendingByDepartment.forEach((deptName, indents) -> {
            log.warn("Department: {} - {} pending indent(s)", deptName, indents.size());
            indents.forEach(indent -> log.warn("  - Indent #: {}, Date: {}, Created by: {}, Plant: {}",
                    indent.getIndentNumber(),
                    indent.getIndentDate(),
                    indent.getCreatedBy() != null ? indent.getCreatedBy().getFullName() : unknown,
                    indent.getPlant() != null ? indent.getPlant().getName() : unknown));
        });
    }

    private void logApproverSummary(List<Indent> pendingIndents) {
        Map<String, Long> pendingByApprover = pendingIndents.stream()
                .filter(indent -> indent.getApprovedBy() != null)
                .collect(Collectors.groupingBy(
                        indent -> indent.getApprovedBy().getFullName(),
                        Collectors.counting()));

        if (!pendingByApprover.isEmpty()) {
            log.info("=== PENDING INDENTS BY APPROVER ===");
            pendingByApprover.forEach(
                    (approverName, count) -> log.info("Approver: {} - {} pending indent(s)", approverName, count));
        }
    }

    private int logOverdueIndents(List<Indent> pendingIndents, String unknown) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<Indent> overdueIndents = pendingIndents.stream()
                .filter(indent -> indent.getIndentDate() != null
                        && indent.getIndentDate().isBefore(threeDaysAgo))
                .toList();

        if (!overdueIndents.isEmpty()) {
            log.error("=== OVERDUE PENDING INDENTS (> 3 days) ===");
            log.error("Found {} overdue indent(s) requiring urgent attention", overdueIndents.size());
            overdueIndents.forEach(indent -> log.error("OVERDUE - Indent #: {}, Days Pending: {}, Department: {}",
                    indent.getIndentNumber(),
                    java.time.temporal.ChronoUnit.DAYS.between(indent.getIndentDate(), LocalDateTime.now()),
                    indent.getDepartment() != null ? indent.getDepartment().getName() : unknown));
        }
        return overdueIndents.size();
    }

    /**
     * Send email reminders to approvers with pending indents
     */
    private void sendApproverReminders(Map<String, List<Indent>> pendingByDepartment, int overdueCount) {
        int emailsSent = 0;
        int emailsFailed = 0;

        for (Map.Entry<String, List<Indent>> entry : pendingByDepartment.entrySet()) {
            List<Indent> deptIndents = entry.getValue();
            if (deptIndents.isEmpty())
                continue;

            // Find the approver (department head)
            Indent firstIndent = deptIndents.get(0);
            if (firstIndent.getDepartment() == null)
                continue;

            // Get approver info (for now, we use department head logic)
            String approverName = entry.getKey();
            String approverEmail = getApproverEmail(firstIndent);

            if (approverEmail == null) {
                log.warn("No approver email found for department: {}", entry.getKey());
                continue;
            }

            // Build tables
            String pendingTable = buildPendingIndentsTable(deptIndents);
            String overdueTable = buildOverdueIndentsTable(deptIndents);

            // Calculate oldest pending days
            int oldestDays = deptIndents.stream()
                    .filter(i -> i.getIndentDate() != null)
                    .mapToInt(i -> (int) ChronoUnit.DAYS.between(i.getIndentDate(), LocalDateTime.now()))
                    .max()
                    .orElse(0);

            // Calculate total pending value (sum of all indent details)
            double totalValue = 0.0; // Value calculation depends on indent details

            IndentReminderEmailData emailData = IndentReminderEmailData.builder()
                    .approverName(approverName)
                    .approverEmail(approverEmail)
                    .pendingCount(deptIndents.size())
                    .pendingIndentsTable(pendingTable)
                    .overdueCount(overdueCount)
                    .overdueIndentsTable(overdueTable)
                    .totalPendingValue(totalValue)
                    .oldestPendingDays(oldestDays)
                    .build();

            boolean sent = emailService.sendPendingIndentReminder(emailData);
            if (sent) {
                emailsSent++;
                log.info("Reminder email sent to: {}", approverEmail);
            } else {
                emailsFailed++;
                log.warn("Failed to send reminder to: {}", approverEmail);
            }
        }

        log.info("Email reminders summary: {} sent, {} failed", emailsSent, emailsFailed);
    }

    private String getApproverEmail(Indent indent) {
        // Try to get approver email from department head or created by's reporting
        // manager
        if (indent.getCreatedBy() != null && indent.getCreatedBy().getEmail() != null) {
            // In production, this should be the reporting manager's email
            // For now, we'll use a placeholder approach
            return null; // Will use default recipients
        }
        return null;
    }

    private String buildPendingIndentsTable(List<Indent> indents) {
        List<String> headers = List.of("Indent #", "Date", "Created By", "Plant", "Status");
        List<List<String>> rows = new ArrayList<>();

        for (Indent indent : indents) {
            rows.add(List.of(
                    indent.getIndentNumber() != null ? indent.getIndentNumber() : "-",
                    indent.getIndentDate() != null ? indent.getIndentDate().toLocalDate().toString() : "-",
                    indent.getCreatedBy() != null ? indent.getCreatedBy().getFullName() : "Unknown",
                    indent.getPlant() != null ? indent.getPlant().getName() : "Unknown",
                    "Pending Approval"));
        }

        return emailService.buildHtmlTable(headers, rows);
    }

    private String buildOverdueIndentsTable(List<Indent> indents) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<Indent> overdueIndents = indents.stream()
                .filter(i -> i.getIndentDate() != null && i.getIndentDate().isBefore(threeDaysAgo))
                .toList();

        if (overdueIndents.isEmpty()) {
            return "<p>No overdue indents.</p>";
        }

        List<String> headers = List.of("Indent #", "Date", "Days Pending", "Created By");
        List<List<String>> rows = new ArrayList<>();

        for (Indent indent : overdueIndents) {
            long daysPending = ChronoUnit.DAYS.between(indent.getIndentDate(), LocalDateTime.now());
            rows.add(List.of(
                    indent.getIndentNumber() != null ? indent.getIndentNumber() : "-",
                    indent.getIndentDate() != null ? indent.getIndentDate().toLocalDate().toString() : "-",
                    String.valueOf(daysPending),
                    indent.getCreatedBy() != null ? indent.getCreatedBy().getFullName() : "Unknown"));
        }

        return emailService.buildHtmlTable(headers, rows);
    }
}
