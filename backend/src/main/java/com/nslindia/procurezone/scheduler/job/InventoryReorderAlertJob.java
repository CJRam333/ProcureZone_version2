package com.nslindia.procurezone.scheduler.job;

import com.nslindia.procurezone.domain.inventory.Inventory;
import com.nslindia.procurezone.notification.dto.InventoryAlertEmailData;
import com.nslindia.procurezone.notification.service.EmailService;
import com.nslindia.procurezone.repository.inventory.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Scheduled job for inventory reorder level alerts
 * Runs every day at 9:00 AM to check low stock items
 * 
 * @author NSL India
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReorderAlertJob implements Job {

    private final InventoryRepository inventoryRepository;
    private final EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("=== Inventory Reorder Alert Job Started at {} ===", LocalDateTime.now());

        try {
            // 1. Query low stock items (below reorder level)
            List<Inventory> lowStockItems = inventoryRepository.findLowStockItems();

            // 2. Query critical stock items (below minimum level)
            List<Inventory> criticalStockItems = inventoryRepository.findCriticalStockItems();

            if (lowStockItems.isEmpty() && criticalStockItems.isEmpty()) {
                log.info("No materials require reordering at this time");
                log.info("=== Inventory Reorder Alert Job Completed - No Action Needed ===");
                return;
            }

            log.warn("Found {} materials below reorder level", lowStockItems.size());
            log.warn("Found {} materials below minimum level (CRITICAL)", criticalStockItems.size());

            // 3. Log critical items first (urgent attention required)
            if (!criticalStockItems.isEmpty()) {
                log.error("=== CRITICAL STOCK LEVELS (Below Minimum) ===");
                criticalStockItems.forEach(item -> log.error("URGENT - Material: {} (ID: {}), Plant: {} (ID: {}), " +
                        "Current: {}, Available: {}, Min Level: {}, Reorder Level: {}, Max Level: {}",
                        item.getMaterial().getName(),
                        item.getMaterial().getId(),
                        item.getPlant().getName(),
                        item.getPlant().getId(),
                        item.getCurrentBalance(),
                        item.getAvailableQuantity(),
                        item.getMinLevel(),
                        item.getReorderLevel(),
                        item.getMaxLevel()));
            }

            // 4. Log warning items (below reorder level but above minimum)
            List<Inventory> warningItems = lowStockItems.stream()
                    .filter(item -> !criticalStockItems.contains(item))
                    .toList();

            if (!warningItems.isEmpty()) {
                log.warn("=== WARNING STOCK LEVELS (Below Reorder Level) ===");
                warningItems.forEach(item -> {
                    BigDecimal recommendedOrder = item.getMaxLevel() != null
                            ? item.getMaxLevel().subtract(item.getAvailableQuantity())
                            : BigDecimal.ZERO;

                    log.warn("Material: {} (ID: {}), Plant: {} (ID: {}), " +
                            "Current: {}, Available: {}, Reorder Level: {}, Recommended Order Qty: {}",
                            item.getMaterial().getName(),
                            item.getMaterial().getId(),
                            item.getPlant().getName(),
                            item.getPlant().getId(),
                            item.getCurrentBalance(),
                            item.getAvailableQuantity(),
                            item.getReorderLevel(),
                            recommendedOrder);
                });
            }

            // 5. Generate summary report
            log.info("=== REORDER ALERT SUMMARY ===");
            log.info("Total materials needing attention: {}", lowStockItems.size());
            log.info("Critical items (below minimum): {}", criticalStockItems.size());
            log.info("Warning items (below reorder level): {}", warningItems.size());

            // 6. Calculate total value at risk
            BigDecimal criticalValue = criticalStockItems.stream()
                    .map(Inventory::getTotalValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalValue = lowStockItems.stream()
                    .map(Inventory::getTotalValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("Total inventory value at risk: Rs. {}", totalValue);
            log.info("Critical inventory value: Rs. {}", criticalValue);

            // 7. Send email notification
            String criticalTable = buildInventoryTable(criticalStockItems, "CRITICAL");
            String warningTable = buildInventoryTable(warningItems, "WARNING");

            InventoryAlertEmailData emailData = InventoryAlertEmailData.builder()
                    .criticalCount(criticalStockItems.size())
                    .lowStockCount(warningItems.size())
                    .criticalItemsTable(criticalTable)
                    .lowStockItemsTable(warningTable)
                    .totalValueAtRisk(totalValue.doubleValue())
                    .recipients(new ArrayList<>()) // Uses default recipients
                    .build();

            boolean emailSent = emailService.sendInventoryReorderAlert(emailData);
            if (emailSent) {
                log.info("Email notification sent successfully to procurement team");
            } else {
                log.warn("Failed to send email notification - check email service logs");
            }

            log.info("=== Inventory Reorder Alert Job Completed Successfully ===");

        } catch (Exception e) {
            log.error("Error in Inventory Reorder Alert Job", e);
            log.error("Job failed at: {}", LocalDateTime.now());
            // Don't throw - let the job complete so it runs again tomorrow
        }
    }

    /**
     * Build HTML table for inventory items
     */
    private String buildInventoryTable(List<Inventory> items, String type) {
        if (items.isEmpty()) {
            return "<p>No " + type.toLowerCase() + " items found.</p>";
        }

        List<String> headers = List.of("Material", "Plant", "Current Qty", "Available Qty",
                "Reorder Level", "Recommended Order");
        List<List<String>> rows = new ArrayList<>();

        for (Inventory item : items) {
            BigDecimal recommendedOrder = item.getMaxLevel() != null
                    ? item.getMaxLevel().subtract(item.getAvailableQuantity())
                    : BigDecimal.ZERO;

            rows.add(List.of(
                    item.getMaterial().getName(),
                    item.getPlant().getName(),
                    item.getCurrentBalance().toString(),
                    item.getAvailableQuantity().toString(),
                    item.getReorderLevel() != null ? item.getReorderLevel().toString() : "-",
                    recommendedOrder.toString()));
        }

        return emailService.buildHtmlTable(headers, rows);
    }
}
