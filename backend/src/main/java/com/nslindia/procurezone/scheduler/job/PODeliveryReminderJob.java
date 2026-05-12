package com.nslindia.procurezone.scheduler.job;

import com.nslindia.procurezone.notification.dto.PODeliveryReminderEmailData;
import com.nslindia.procurezone.notification.service.EmailService;
import com.nslindia.procurezone.po.PORepository;
import com.nslindia.procurezone.po.PurchaseOrder;
import com.nslindia.procurezone.vendor.Vendor;
import com.nslindia.procurezone.vendor.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * E.1 FIX: Scheduled job for PO delivery reminders
 * Runs daily at 9:00 AM to remind procurement team about:
 * - Upcoming deliveries (within 3 days)
 * - Overdue POs (past expected delivery date)
 * 
 * @author NSL India
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PODeliveryReminderJob implements Job {

    private final PORepository poRepository;
    private final VendorRepository vendorRepository;
    private final EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("=== PO Delivery Reminder Job Started at {} ===", LocalDateTime.now());

        try {
            LocalDate today = LocalDate.now();
            LocalDate threeDaysLater = today.plusDays(3);

            // 1. Find overdue POs (status 4=Sent to Vendor, 5=Partially Received)
            Page<PurchaseOrder> overduePOsPage = poRepository.findOverduePOs(today, PageRequest.of(0, 1000));
            List<PurchaseOrder> overduePOs = overduePOsPage.getContent();

            // 2. Find POs with upcoming deliveries (within 3 days)
            Page<PurchaseOrder> pendingDeliveriesPage = poRepository.findPendingDeliveries(PageRequest.of(0, 1000));
            List<PurchaseOrder> upcomingDeliveries = pendingDeliveriesPage.getContent().stream()
                    .filter(po -> po.getExpectedDeliveryDate() != null
                            && !po.getExpectedDeliveryDate().isBefore(today)
                            && !po.getExpectedDeliveryDate().isAfter(threeDaysLater))
                    .toList();

            log.info("Found {} overdue POs and {} upcoming deliveries", overduePOs.size(), upcomingDeliveries.size());

            if (overduePOs.isEmpty() && upcomingDeliveries.isEmpty()) {
                log.info("No overdue or upcoming deliveries. Job completed.");
                return;
            }

            // 3. Log and analyze overdue POs
            if (!overduePOs.isEmpty()) {
                logOverduePOs(overduePOs, today);
            }

            // 4. Log upcoming deliveries
            if (!upcomingDeliveries.isEmpty()) {
                logUpcomingDeliveries(upcomingDeliveries, today);
            }

            // 5. Group by vendor for vendor-specific notifications
            Map<Integer, List<PurchaseOrder>> overdueByVendor = groupByVendor(overduePOs);
            Map<Integer, List<PurchaseOrder>> upcomingByVendor = groupByVendor(upcomingDeliveries);

            // 6. Send summary email to procurement team
            sendProcurementTeamSummary(overduePOs, upcomingDeliveries, today);

            // 7. Send vendor-specific reminders (optional, based on config)
            sendVendorReminders(overdueByVendor, upcomingByVendor, today);

            log.info("=== PO Delivery Reminder Job Completed Successfully ===");

        } catch (Exception e) {
            log.error("Error in PO Delivery Reminder Job", e);
            log.error("Job failed at: {}", LocalDateTime.now());
        }
    }

    private void logOverduePOs(List<PurchaseOrder> overduePOs, LocalDate today) {
        log.error("=== OVERDUE PURCHASE ORDERS ===");
        log.error("Found {} PO(s) past expected delivery date", overduePOs.size());

        double totalOverdueValue = 0.0;
        int maxDaysOverdue = 0;

        for (PurchaseOrder po : overduePOs) {
            long daysOverdue = ChronoUnit.DAYS.between(po.getExpectedDeliveryDate(), today);
            maxDaysOverdue = Math.max(maxDaysOverdue, (int) daysOverdue);
            totalOverdueValue += po.getNetAmount() != null ? po.getNetAmount().doubleValue() : 0;

            String vendorName = getVendorName(po.getVendorId());
            log.error("OVERDUE - PO#: {}, Vendor: {}, Expected: {}, Days Overdue: {}, Value: {}",
                    po.getPoNumber(),
                    vendorName,
                    po.getExpectedDeliveryDate(),
                    daysOverdue,
                    po.getNetAmount());
        }

        log.error("Total Overdue Value: {} | Max Days Overdue: {}", totalOverdueValue, maxDaysOverdue);
    }

    private void logUpcomingDeliveries(List<PurchaseOrder> upcomingDeliveries, LocalDate today) {
        log.warn("=== UPCOMING DELIVERIES (Next 3 Days) ===");
        log.warn("Found {} PO(s) with deliveries due within 3 days", upcomingDeliveries.size());

        for (PurchaseOrder po : upcomingDeliveries) {
            long daysUntil = ChronoUnit.DAYS.between(today, po.getExpectedDeliveryDate());
            String vendorName = getVendorName(po.getVendorId());

            log.warn("UPCOMING - PO#: {}, Vendor: {}, Expected: {}, Days Until: {}",
                    po.getPoNumber(),
                    vendorName,
                    po.getExpectedDeliveryDate(),
                    daysUntil);
        }
    }

    private Map<Integer, List<PurchaseOrder>> groupByVendor(List<PurchaseOrder> pos) {
        return pos.stream()
                .filter(po -> po.getVendorId() != null)
                .collect(Collectors.groupingBy(PurchaseOrder::getVendorId));
    }

    private void sendProcurementTeamSummary(List<PurchaseOrder> overduePOs,
            List<PurchaseOrder> upcomingDeliveries,
            LocalDate today) {

        // Calculate summary values
        double overdueValue = overduePOs.stream()
                .mapToDouble(po -> po.getNetAmount() != null ? po.getNetAmount().doubleValue() : 0)
                .sum();

        int maxDaysOverdue = overduePOs.stream()
                .filter(po -> po.getExpectedDeliveryDate() != null)
                .mapToInt(po -> (int) ChronoUnit.DAYS.between(po.getExpectedDeliveryDate(), today))
                .max()
                .orElse(0);

        // Build HTML tables
        String overduePOsTable = buildOverduePOsTable(overduePOs, today);
        String upcomingTable = buildUpcomingDeliveriesTable(upcomingDeliveries, today);

        PODeliveryReminderEmailData emailData = PODeliveryReminderEmailData.builder()
                .overdueCount(overduePOs.size())
                .overduePOsTable(overduePOsTable)
                .overdueValue(overdueValue)
                .maxDaysOverdue(maxDaysOverdue)
                .upcomingDeliveryCount(upcomingDeliveries.size())
                .upcomingDeliveriesTable(upcomingTable)
                .build();

        boolean sent = emailService.sendPODeliveryReminder(emailData);
        if (sent) {
            log.info("PO delivery reminder sent to procurement team");
        } else {
            log.warn("Failed to send PO delivery reminder to procurement team");
        }
    }

    private void sendVendorReminders(Map<Integer, List<PurchaseOrder>> overdueByVendor,
            Map<Integer, List<PurchaseOrder>> upcomingByVendor,
            LocalDate today) {

        // Combine vendor IDs from both maps
        Map<Integer, List<PurchaseOrder>> allVendorPOs = new HashMap<>(overdueByVendor);
        upcomingByVendor.forEach((vendorId, pos) -> allVendorPOs.merge(vendorId, pos, (existing, newList) -> {
            List<PurchaseOrder> combined = new ArrayList<>(existing);
            combined.addAll(newList);
            return combined;
        }));

        for (Map.Entry<Integer, List<PurchaseOrder>> entry : allVendorPOs.entrySet()) {
            Integer vendorId = entry.getKey();
            List<PurchaseOrder> vendorPOs = entry.getValue();

            // Get vendor details
            Vendor vendor = vendorRepository.findById(vendorId).orElse(null);
            if (vendor == null || vendor.getContactEmail() == null) {
                log.warn("No email found for vendor ID: {}", vendorId);
                continue;
            }

            // Separate overdue and upcoming for this vendor
            List<PurchaseOrder> vendorOverdue = vendorPOs.stream()
                    .filter(po -> po.getExpectedDeliveryDate() != null
                            && po.getExpectedDeliveryDate().isBefore(today))
                    .toList();

            List<PurchaseOrder> vendorUpcoming = vendorPOs.stream()
                    .filter(po -> po.getExpectedDeliveryDate() != null
                            && !po.getExpectedDeliveryDate().isBefore(today))
                    .toList();

            // Only send if there are overdue POs (don't spam for upcoming)
            if (vendorOverdue.isEmpty()) {
                continue;
            }

            double overdueValue = vendorOverdue.stream()
                    .mapToDouble(po -> po.getNetAmount() != null ? po.getNetAmount().doubleValue() : 0)
                    .sum();

            int maxDaysOverdue = vendorOverdue.stream()
                    .filter(po -> po.getExpectedDeliveryDate() != null)
                    .mapToInt(po -> (int) ChronoUnit.DAYS.between(po.getExpectedDeliveryDate(), today))
                    .max()
                    .orElse(0);

            PODeliveryReminderEmailData emailData = PODeliveryReminderEmailData.builder()
                    .vendorName(vendor.getVendorName())
                    .vendorEmail(vendor.getContactEmail())
                    .overdueCount(vendorOverdue.size())
                    .overduePOsTable(buildOverduePOsTable(vendorOverdue, today))
                    .overdueValue(overdueValue)
                    .maxDaysOverdue(maxDaysOverdue)
                    .upcomingDeliveryCount(vendorUpcoming.size())
                    .upcomingDeliveriesTable(buildUpcomingDeliveriesTable(vendorUpcoming, today))
                    .recipients(List.of(vendor.getContactEmail()))
                    .build();

            boolean sent = emailService.sendPODeliveryReminder(emailData);
            if (sent) {
                log.info("PO delivery reminder sent to vendor: {} ({})", vendor.getVendorName(),
                        vendor.getContactEmail());
            } else {
                log.warn("Failed to send PO delivery reminder to vendor: {}", vendor.getVendorName());
            }
        }
    }

    private String buildOverduePOsTable(List<PurchaseOrder> pos, LocalDate today) {
        if (pos.isEmpty()) {
            return "<p>No overdue POs.</p>";
        }

        List<String> headers = List.of("PO #", "Vendor", "Expected Date", "Days Overdue", "Value", "Status");
        List<List<String>> rows = new ArrayList<>();

        for (PurchaseOrder po : pos) {
            long daysOverdue = po.getExpectedDeliveryDate() != null
                    ? ChronoUnit.DAYS.between(po.getExpectedDeliveryDate(), today)
                    : 0;

            rows.add(List.of(
                    po.getPoNumber() != null ? po.getPoNumber() : "-",
                    getVendorName(po.getVendorId()),
                    po.getExpectedDeliveryDate() != null ? po.getExpectedDeliveryDate().toString() : "-",
                    String.valueOf(daysOverdue),
                    po.getNetAmount() != null ? String.format("%.2f", po.getNetAmount()) : "-",
                    getStatusName(po.getPoStatus())));
        }

        return emailService.buildHtmlTable(headers, rows);
    }

    private String buildUpcomingDeliveriesTable(List<PurchaseOrder> pos, LocalDate today) {
        if (pos.isEmpty()) {
            return "<p>No upcoming deliveries.</p>";
        }

        List<String> headers = List.of("PO #", "Vendor", "Expected Date", "Days Until", "Value");
        List<List<String>> rows = new ArrayList<>();

        for (PurchaseOrder po : pos) {
            long daysUntil = po.getExpectedDeliveryDate() != null
                    ? ChronoUnit.DAYS.between(today, po.getExpectedDeliveryDate())
                    : 0;

            rows.add(List.of(
                    po.getPoNumber() != null ? po.getPoNumber() : "-",
                    getVendorName(po.getVendorId()),
                    po.getExpectedDeliveryDate() != null ? po.getExpectedDeliveryDate().toString() : "-",
                    String.valueOf(daysUntil),
                    po.getNetAmount() != null ? String.format("%.2f", po.getNetAmount()) : "-"));
        }

        return emailService.buildHtmlTable(headers, rows);
    }

    private String getVendorName(Integer vendorId) {
        if (vendorId == null) {
            return "Unknown";
        }
        return vendorRepository.findById(vendorId)
                .map(Vendor::getVendorName)
                .orElse("Unknown");
    }

    private String getStatusName(Integer status) {
        if (status == null)
            return "Unknown";
        return switch (status) {
            case 1 -> "Draft";
            case 2 -> "Submitted";
            case 3 -> "Approved";
            case 4 -> "Sent to Vendor";
            case 5 -> "Partially Received";
            case 6 -> "Fully Received";
            case 7 -> "Cancelled";
            case 8 -> "Closed";
            default -> "Unknown";
        };
    }
}
