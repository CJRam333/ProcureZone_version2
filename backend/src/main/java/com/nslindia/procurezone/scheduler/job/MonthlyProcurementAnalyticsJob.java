package com.nslindia.procurezone.scheduler.job;

import com.nslindia.procurezone.grn.GoodsReceiptRepository;
import com.nslindia.procurezone.indent.IndentRepository;
import com.nslindia.procurezone.issuenote.IssueNoteRepository;
import com.nslindia.procurezone.notification.dto.MonthlyAnalyticsEmailData;
import com.nslindia.procurezone.notification.service.EmailService;
import com.nslindia.procurezone.repository.inventory.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;

/**
 * Scheduled job for monthly procurement analytics
 * Runs on 1st day of every month at 8:00 AM
 * 
 * @author NSL India
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyProcurementAnalyticsJob implements Job {

        private final IndentRepository indentRepository;
        private final IssueNoteRepository issueNoteRepository;
        private final GoodsReceiptRepository grnRepository;
        private final InventoryTransactionRepository transactionRepository;
        private final EmailService emailService;

        @Override
        public void execute(JobExecutionContext context) {
                log.info("=== Monthly Procurement Analytics Job Started at {} ===", LocalDateTime.now());

                try {
                        // 1. Determine last month's date range
                        YearMonth lastMonth = YearMonth.now().minusMonths(1);
                        LocalDateTime startOfMonth = lastMonth.atDay(1).atStartOfDay();
                        LocalDateTime endOfMonth = lastMonth.atEndOfMonth().atTime(23, 59, 59);

                        log.info("Generating analytics for: {} to {}", startOfMonth.toLocalDate(),
                                        endOfMonth.toLocalDate());

                        // 2. Indent statistics
                        long totalIndents = indentRepository.findAll().stream()
                                        .filter(indent -> indent.getIndentDate() != null
                                                        && !indent.getIndentDate().isBefore(startOfMonth)
                                                        && !indent.getIndentDate().isAfter(endOfMonth))
                                        .count();

                        long approvedIndents = indentRepository.findAll().stream()
                                        .filter(indent -> indent.getIndentDate() != null
                                                        && !indent.getIndentDate().isBefore(startOfMonth)
                                                        && !indent.getIndentDate().isAfter(endOfMonth)
                                                        && indent.getFinalApprovedBy() != null)
                                        .count();

                        log.info("=== INDENT STATISTICS ===");
                        log.info("Total indents created: {}", totalIndents);
                        log.info("Indents approved: {}", approvedIndents);
                        log.info("Approval rate: {}%", totalIndents > 0 ? (approvedIndents * 100 / totalIndents) : 0);

                        // 3. GRN statistics
                        long totalGRNs = grnRepository.findAll().stream()
                                        .filter(grn -> grn.getReceiptDate() != null
                                                        && !grn.getReceiptDate().isBefore(startOfMonth)
                                                        && !grn.getReceiptDate().isAfter(endOfMonth))
                                        .count();

                        log.info("=== GRN STATISTICS ===");
                        log.info("Total GRNs created: {}", totalGRNs);

                        // 4. Issue Note statistics
                        long totalIssueNotes = issueNoteRepository.findAll().stream()
                                        .filter(issue -> issue.getIssueDate() != null
                                                        && !issue.getIssueDate().isBefore(startOfMonth)
                                                        && !issue.getIssueDate().isAfter(endOfMonth))
                                        .count();

                        log.info("=== ISSUE NOTE STATISTICS ===");
                        log.info("Total issue notes created: {}", totalIssueNotes);

                        // 5. Inventory transaction statistics
                        var monthTransactions = transactionRepository.findByDateRange(startOfMonth, endOfMonth);
                        long receipts = monthTransactions.stream()
                                        .filter(t -> "RECEIPT".equalsIgnoreCase(t.getTransactionType())
                                                        || "GRN".equalsIgnoreCase(t.getTransactionType()))
                                        .count();

                        long issues = monthTransactions.stream()
                                        .filter(t -> "ISSUE".equalsIgnoreCase(t.getTransactionType())
                                                        || "ISSUE_NOTE".equalsIgnoreCase(t.getTransactionType()))
                                        .count();

                        log.info("=== INVENTORY TRANSACTION STATISTICS ===");
                        log.info("Total transactions: {}", monthTransactions.size());
                        log.info("Receipt transactions: {}", receipts);
                        log.info("Issue transactions: {}", issues);

                        // 6. Generate comprehensive summary
                        log.info("=== MONTHLY ANALYTICS SUMMARY - {} ===", lastMonth);
                        log.info("Indents: {} created, {} approved ({}% approval rate)",
                                        totalIndents, approvedIndents,
                                        totalIndents > 0 ? (approvedIndents * 100 / totalIndents) : 0);
                        log.info("GRNs: {} received", totalGRNs);
                        log.info("Issue Notes: {} issued", totalIssueNotes);
                        log.info("Inventory Movements: {} total ({} in, {} out)",
                                        monthTransactions.size(), receipts, issues);

                        // 7. Send email report
                        double approvalRate = totalIndents > 0 ? (approvedIndents * 100.0 / totalIndents) : 0;

                        MonthlyAnalyticsEmailData emailData = MonthlyAnalyticsEmailData.builder()
                                        .monthYear(lastMonth.toString())
                                        .indentsCreated((int) totalIndents)
                                        .indentsApproved((int) approvedIndents)
                                        .approvalRate(approvalRate)
                                        .avgApprovalDays(0)
                                        .grnCount((int) totalGRNs)
                                        .grnTotalValue(0.0)
                                        .onTimeDeliveryRate(0.0)
                                        .issueNoteCount((int) totalIssueNotes)
                                        .issuedTotalValue(0.0)
                                        .totalReceipts((int) receipts)
                                        .totalIssues((int) issues)
                                        .netChange((int) (receipts - issues))
                                        .trendsSection(null)
                                        .recipients(new ArrayList<>())
                                        .build();

                        boolean emailSent = emailService.sendMonthlyProcurementAnalytics(emailData);
                        if (emailSent) {
                                log.info("Monthly analytics report sent to management");
                        } else {
                                log.warn("Failed to send monthly analytics email");
                        }

                        log.info("=== Monthly Procurement Analytics Job Completed Successfully ===");

                } catch (Exception e) {
                        log.error("Error in Monthly Procurement Analytics Job", e);
                        log.error("Job failed at: {}", LocalDateTime.now());
                }
        }
}
