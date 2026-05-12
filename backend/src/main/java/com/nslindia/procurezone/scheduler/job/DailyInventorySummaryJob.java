package com.nslindia.procurezone.scheduler.job;

import com.nslindia.procurezone.domain.inventory.Inventory;
import com.nslindia.procurezone.domain.inventory.InventoryTransaction;
import com.nslindia.procurezone.notification.dto.DailySummaryEmailData;
import com.nslindia.procurezone.notification.service.EmailService;
import com.nslindia.procurezone.repository.inventory.InventoryRepository;
import com.nslindia.procurezone.repository.inventory.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Scheduled job for daily inventory summary report
 * Runs every day at 6:00 PM to generate summary
 * 
 * @author NSL India
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyInventorySummaryJob implements Job {

        private final InventoryRepository inventoryRepository;
        private final InventoryTransactionRepository transactionRepository;
        private final EmailService emailService;

        @Override
        public void execute(JobExecutionContext context) {
                log.info("=== Daily Inventory Summary Job Started at {} ===", LocalDateTime.now());

                try {
                        LocalDate today = LocalDate.now();
                        LocalDateTime startOfDay = today.atStartOfDay();
                        LocalDateTime endOfDay = today.atTime(23, 59, 59);

                        // 1. Get overall inventory statistics
                        List<Inventory> allInventory = inventoryRepository.findAll();
                        long totalItems = allInventory.size();
                        long activeItems = allInventory.stream()
                                        .filter(inv -> inv.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0)
                                        .count();
                        long zeroStockItems = totalItems - activeItems;

                        BigDecimal totalInventoryValue = allInventory.stream()
                                        .map(Inventory::getTotalValue)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        log.info("=== INVENTORY OVERVIEW ===");
                        log.info("Total inventory items: {}", totalItems);
                        log.info("Active items (stock > 0): {}", activeItems);
                        log.info("Zero stock items: {}", zeroStockItems);
                        log.info("Total inventory value: Rs. {}", totalInventoryValue);

                        // 2. Get low stock and critical stock statistics
                        List<Inventory> lowStockItems = inventoryRepository.findLowStockItems();
                        List<Inventory> criticalStockItems = inventoryRepository.findCriticalStockItems();

                        log.warn("=== STOCK ALERT SUMMARY ===");
                        log.warn("Items below reorder level: {}", lowStockItems.size());
                        log.warn("Items below minimum level (CRITICAL): {}", criticalStockItems.size());

                        // 3. Get today's transactions
                        List<InventoryTransaction> todayTransactions = transactionRepository
                                        .findByDateRange(startOfDay, endOfDay);

                        long receipts = todayTransactions.stream()
                                        .filter(t -> "RECEIPT".equalsIgnoreCase(t.getTransactionType())
                                                        || "GRN".equalsIgnoreCase(t.getTransactionType()))
                                        .count();

                        long issues = todayTransactions.stream()
                                        .filter(t -> "ISSUE".equalsIgnoreCase(t.getTransactionType())
                                                        || "ISSUE_NOTE".equalsIgnoreCase(t.getTransactionType()))
                                        .count();

                        long adjustments = todayTransactions.stream()
                                        .filter(t -> "ADJUSTMENT".equalsIgnoreCase(t.getTransactionType()))
                                        .count();

                        BigDecimal totalReceiptQty = todayTransactions.stream()
                                        .filter(t -> "RECEIPT".equalsIgnoreCase(t.getTransactionType())
                                                        || "GRN".equalsIgnoreCase(t.getTransactionType()))
                                        .map(InventoryTransaction::getQuantity)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal totalIssueQty = todayTransactions.stream()
                                        .filter(t -> "ISSUE".equalsIgnoreCase(t.getTransactionType())
                                                        || "ISSUE_NOTE".equalsIgnoreCase(t.getTransactionType()))
                                        .map(InventoryTransaction::getQuantity)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        log.info("=== TODAY'S TRANSACTIONS ({}) ===", today);
                        log.info("Total transactions: {}", todayTransactions.size());
                        log.info("Receipts (GRN): {} transactions, Total Qty: {}", receipts, totalReceiptQty);
                        log.info("Issues: {} transactions, Total Qty: {}", issues, totalIssueQty);
                        log.info("Adjustments: {} transactions", adjustments);

                        // 4. Calculate net movement
                        BigDecimal netMovement = totalReceiptQty.subtract(totalIssueQty);
                        log.info("Net inventory movement: {} ({})",
                                        netMovement,
                                        netMovement.compareTo(BigDecimal.ZERO) >= 0 ? "Increase" : "Decrease");

                        // 5. Top 10 materials by value
                        List<Inventory> topValueMaterials = allInventory.stream()
                                        .sorted((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()))
                                        .limit(10)
                                        .toList();

                        log.info("=== TOP 10 MATERIALS BY VALUE ===");
                        topValueMaterials.forEach(inv -> log.info("Material: {}, Stock: {}, Value: Rs. {}",
                                        inv.getMaterial().getName(),
                                        inv.getCurrentBalance(),
                                        inv.getTotalValue()));

                        // 6. Generate summary
                        log.info("=== DAILY SUMMARY ===");
                        log.info("Date: {}", today);
                        log.info("Total Inventory Value: Rs. {}", totalInventoryValue);
                        log.info("Active Items: {} / {}", activeItems, totalItems);
                        log.info("Items Needing Attention: {} (Low: {}, Critical: {})",
                                        lowStockItems.size(),
                                        lowStockItems.size() - criticalStockItems.size(),
                                        criticalStockItems.size());
                        log.info("Today's Activity: {} receipts, {} issues, {} adjustments",
                                        receipts, issues, adjustments);

                        // 7. Send email report
                        String topMaterialsTable = buildTopMaterialsTable(topValueMaterials);

                        DailySummaryEmailData emailData = DailySummaryEmailData.builder()
                                        .totalMaterials((int) totalItems)
                                        .totalInventoryValue(totalInventoryValue.doubleValue())
                                        .lowStockCount(lowStockItems.size())
                                        .criticalStockCount(criticalStockItems.size())
                                        .receiptsCount((int) receipts)
                                        .receiptsValue(0.0) // Value calculation requires additional query
                                        .issuesCount((int) issues)
                                        .issuesValue(0.0)
                                        .adjustmentsCount((int) adjustments)
                                        .netMovement(netMovement.doubleValue())
                                        .topMaterialsTable(topMaterialsTable)
                                        .recipients(new ArrayList<>())
                                        .build();

                        boolean emailSent = emailService.sendDailyInventorySummary(emailData);
                        if (emailSent) {
                                log.info("Daily summary email sent successfully");
                        } else {
                                log.warn("Failed to send daily summary email");
                        }

                        log.info("=== Daily Inventory Summary Job Completed Successfully ===");

                } catch (Exception e) {
                        log.error("Error in Daily Inventory Summary Job", e);
                        log.error("Job failed at: {}", LocalDateTime.now());
                }
        }

        /**
         * Build HTML table for top materials by value
         */
        private String buildTopMaterialsTable(List<Inventory> materials) {
                List<String> headers = List.of("Material", "Stock", "Value (Rs.)");
                List<List<String>> rows = new ArrayList<>();

                for (Inventory inv : materials) {
                        rows.add(List.of(
                                        inv.getMaterial().getName(),
                                        inv.getCurrentBalance().toString(),
                                        inv.getTotalValue().toString()));
                }

                return emailService.buildHtmlTable(headers, rows);
        }
}
