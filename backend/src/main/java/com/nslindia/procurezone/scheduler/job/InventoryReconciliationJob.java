package com.nslindia.procurezone.scheduler.job;

import com.nslindia.procurezone.domain.inventory.Inventory;
import com.nslindia.procurezone.domain.inventory.InventoryTransaction;
import com.nslindia.procurezone.notification.dto.InventoryReconciliationEmailData;
import com.nslindia.procurezone.notification.service.EmailService;
import com.nslindia.procurezone.repository.inventory.InventoryRepository;
import com.nslindia.procurezone.repository.inventory.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * E.2 FIX: Scheduled job for Inventory Reconciliation
 * Runs weekly (Sunday at 2:00 AM) to verify inventory balances against
 * transactions.
 * 
 * Checks:
 * 1. Opening balance + receipts - issues = current balance
 * 2. No negative inventory balances
 * 3. All transactions have valid inventory references
 * 4. No orphaned transactions
 * 
 * @author NSL India
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReconciliationJob implements Job {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("=== Inventory Reconciliation Job Started at {} ===", LocalDateTime.now());

        ReconciliationResult result = new ReconciliationResult();

        try {
            // 1. Get all inventory records
            List<Inventory> allInventory = inventoryRepository.findAll();
            result.totalMaterialsChecked = allInventory.size();
            log.info("Checking {} inventory records", result.totalMaterialsChecked);

            // 2. Calculate date range for transactions (last 30 days or since opening)
            LocalDateTime startOfMonth = LocalDateTime.now()
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endDate = LocalDateTime.now();

            // 3. Check each inventory record
            for (Inventory inventory : allInventory) {
                checkInventoryRecord(inventory, startOfMonth, endDate, result);
            }

            // 4. Check for orphaned transactions
            checkOrphanedTransactions(result);

            // 5. Log summary
            logReconciliationSummary(result);

            // 6. Send email report
            sendReconciliationReport(result);

            log.info("=== Inventory Reconciliation Job Completed Successfully ===");

        } catch (Exception e) {
            log.error("Error in Inventory Reconciliation Job", e);
            result.errorMessage = e.getMessage();
            sendErrorReport(result, e);
        }
    }

    private void checkInventoryRecord(Inventory inventory, LocalDateTime startDate,
            LocalDateTime endDate, ReconciliationResult result) {
        try {
            // Get material name for logging
            String materialName = inventory.getMaterial() != null
                    ? inventory.getMaterial().getName()
                    : "Unknown";
            String plantName = inventory.getPlant() != null
                    ? inventory.getPlant().getName()
                    : "Unknown";

            // 1. Check for negative balance
            if (inventory.getCurrentBalance() != null &&
                    inventory.getCurrentBalance().compareTo(BigDecimal.ZERO) < 0) {

                result.negativeBalanceItems.add(new NegativeBalanceItem(
                        inventory.getId(),
                        materialName,
                        plantName,
                        inventory.getCurrentBalance()));
                log.warn("NEGATIVE BALANCE - Material: {}, Plant: {}, Balance: {}",
                        materialName, plantName, inventory.getCurrentBalance());
            }

            // 2. Get transactions for this inventory
            List<InventoryTransaction> transactions = transactionRepository
                    .findByInventory_Id(inventory.getId());

            // 3. Calculate expected balance from transactions
            BigDecimal calculatedReceipts = BigDecimal.ZERO;
            BigDecimal calculatedIssues = BigDecimal.ZERO;

            for (InventoryTransaction txn : transactions) {
                if ("IN".equalsIgnoreCase(txn.getDirection())) {
                    calculatedReceipts = calculatedReceipts.add(
                            txn.getQuantity() != null ? txn.getQuantity() : BigDecimal.ZERO);
                } else if ("OUT".equalsIgnoreCase(txn.getDirection())) {
                    calculatedIssues = calculatedIssues.add(
                            txn.getQuantity() != null ? txn.getQuantity() : BigDecimal.ZERO);
                }
            }

            // 4. Expected balance = opening + receipts - issues
            BigDecimal openingBalance = inventory.getOpeningBalance() != null
                    ? inventory.getOpeningBalance()
                    : BigDecimal.ZERO;
            BigDecimal expectedBalance = openingBalance.add(calculatedReceipts).subtract(calculatedIssues);
            BigDecimal currentBalance = inventory.getCurrentBalance() != null
                    ? inventory.getCurrentBalance()
                    : BigDecimal.ZERO;

            // 5. Check for variance
            BigDecimal variance = currentBalance.subtract(expectedBalance).abs();
            BigDecimal threshold = new BigDecimal("0.001"); // Small threshold for rounding

            if (variance.compareTo(threshold) > 0) {
                BigDecimal valueVariance = variance.multiply(
                        inventory.getAvgRate() != null ? inventory.getAvgRate() : BigDecimal.ZERO);

                result.discrepancies.add(new DiscrepancyItem(
                        inventory.getId(),
                        materialName,
                        plantName,
                        expectedBalance,
                        currentBalance,
                        variance,
                        valueVariance));

                result.totalQuantityVariance = result.totalQuantityVariance.add(variance);
                result.totalValueVariance = result.totalValueVariance.add(valueVariance);

                log.warn("DISCREPANCY - Material: {}, Plant: {}, Expected: {}, Actual: {}, Variance: {}",
                        materialName, plantName, expectedBalance, currentBalance, variance);
            }

        } catch (Exception e) {
            log.error("Error checking inventory ID {}: {}", inventory.getId(), e.getMessage());
        }
    }

    private void checkOrphanedTransactions(ReconciliationResult result) {
        log.info("Checking for orphaned transactions...");

        // Get all transactions
        List<InventoryTransaction> allTransactions = transactionRepository.findAll();

        // Track inventory IDs that exist
        Map<Integer, Boolean> inventoryExists = new HashMap<>();
        List<Inventory> allInventory = inventoryRepository.findAll();
        for (Inventory inv : allInventory) {
            inventoryExists.put(inv.getId(), true);
        }

        // Check each transaction
        for (InventoryTransaction txn : allTransactions) {
            Integer invId = txn.getInventory() != null ? txn.getInventory().getId() : null;
            if (invId != null && !inventoryExists.containsKey(invId)) {
                result.orphanedTransactionCount++;
                log.warn("ORPHANED TRANSACTION - ID: {}, Inventory ID: {} (not found)",
                        txn.getId(), invId);
            }
        }

        if (result.orphanedTransactionCount > 0) {
            log.error("Found {} orphaned transactions", result.orphanedTransactionCount);
        }
    }

    private void logReconciliationSummary(ReconciliationResult result) {
        log.info("=== RECONCILIATION SUMMARY ===");
        log.info("Total materials checked: {}", result.totalMaterialsChecked);
        log.info("Discrepancies found: {}", result.discrepancies.size());
        log.info("Negative balance items: {}", result.negativeBalanceItems.size());
        log.info("Orphaned transactions: {}", result.orphanedTransactionCount);
        log.info("Total quantity variance: {}", result.totalQuantityVariance);
        log.info("Total value variance: {}", result.totalValueVariance);

        if (result.discrepancies.isEmpty() && result.negativeBalanceItems.isEmpty()
                && result.orphanedTransactionCount == 0) {
            log.info("RECONCILIATION STATUS: PASS");
            result.status = "PASS";
        } else {
            log.warn("RECONCILIATION STATUS: ISSUES_FOUND");
            result.status = "ISSUES_FOUND";
        }
    }

    private void sendReconciliationReport(ReconciliationResult result) {
        String reportPeriod = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        String discrepanciesTable = buildDiscrepanciesTable(result.discrepancies);
        String negativeBalanceTable = buildNegativeBalanceTable(result.negativeBalanceItems);

        InventoryReconciliationEmailData emailData = InventoryReconciliationEmailData.builder()
                .reportPeriod(reportPeriod)
                .totalMaterialsChecked(result.totalMaterialsChecked)
                .discrepancyCount(result.discrepancies.size())
                .discrepanciesTable(discrepanciesTable)
                .totalQuantityVariance(result.totalQuantityVariance.doubleValue())
                .totalValueVariance(result.totalValueVariance.doubleValue())
                .negativeBalanceCount(result.negativeBalanceItems.size())
                .negativeBalanceTable(negativeBalanceTable)
                .orphanedTransactionCount(result.orphanedTransactionCount)
                .reconciliationStatus(result.status)
                .build();

        boolean sent = emailService.sendInventoryReconciliationReport(emailData);
        if (sent) {
            log.info("Reconciliation report sent successfully");
        } else {
            log.warn("Failed to send reconciliation report");
        }
    }

    private void sendErrorReport(ReconciliationResult result, Exception e) {
        String reportPeriod = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        String errorSection = String.format("<p style=\"color: red;\"><strong>Error:</strong> %s</p>" +
                "<p>The reconciliation job encountered an error and could not complete.</p>",
                e.getMessage());

        InventoryReconciliationEmailData emailData = InventoryReconciliationEmailData.builder()
                .reportPeriod(reportPeriod)
                .totalMaterialsChecked(result.totalMaterialsChecked)
                .discrepancyCount(0)
                .reconciliationStatus("ERROR")
                .errorSection(errorSection)
                .build();

        emailService.sendInventoryReconciliationReport(emailData);
    }

    private String buildDiscrepanciesTable(List<DiscrepancyItem> discrepancies) {
        if (discrepancies.isEmpty()) {
            return "<p style=\"color: green;\">No discrepancies found. All inventory balances are correct.</p>";
        }

        List<String> headers = List.of("Material", "Plant", "Expected Qty", "Actual Qty", "Variance", "Value Variance");
        List<List<String>> rows = new ArrayList<>();

        for (DiscrepancyItem item : discrepancies) {
            rows.add(List.of(
                    item.materialName,
                    item.plantName,
                    String.format("%.3f", item.expectedBalance),
                    String.format("%.3f", item.currentBalance),
                    String.format("%.3f", item.variance),
                    String.format("%.2f", item.valueVariance)));
        }

        return emailService.buildHtmlTable(headers, rows);
    }

    private String buildNegativeBalanceTable(List<NegativeBalanceItem> items) {
        if (items.isEmpty()) {
            return "<p style=\"color: green;\">No negative balances found.</p>";
        }

        List<String> headers = List.of("Material", "Plant", "Current Balance");
        List<List<String>> rows = new ArrayList<>();

        for (NegativeBalanceItem item : items) {
            rows.add(List.of(
                    item.materialName,
                    item.plantName,
                    String.format("%.3f", item.balance)));
        }

        return emailService.buildHtmlTable(headers, rows);
    }

    // Inner classes for results
    private static class ReconciliationResult {
        int totalMaterialsChecked = 0;
        List<DiscrepancyItem> discrepancies = new ArrayList<>();
        List<NegativeBalanceItem> negativeBalanceItems = new ArrayList<>();
        int orphanedTransactionCount = 0;
        BigDecimal totalQuantityVariance = BigDecimal.ZERO;
        BigDecimal totalValueVariance = BigDecimal.ZERO;
        String status = "UNKNOWN";
        String errorMessage;
    }

    private record DiscrepancyItem(
            Integer inventoryId,
            String materialName,
            String plantName,
            BigDecimal expectedBalance,
            BigDecimal currentBalance,
            BigDecimal variance,
            BigDecimal valueVariance) {
    }

    private record NegativeBalanceItem(
            Integer inventoryId,
            String materialName,
            String plantName,
            BigDecimal balance) {
    }
}
