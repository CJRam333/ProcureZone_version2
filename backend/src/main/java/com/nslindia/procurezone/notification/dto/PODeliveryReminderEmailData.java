package com.nslindia.procurezone.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for PO delivery reminder email data
 * E.1 FIX: Used by PODeliveryReminderJob
 * 
 * @author NSL India
 * @version 1.0
 */
@Data
@Builder
public class PODeliveryReminderEmailData {

    /**
     * Number of POs approaching delivery deadline (within 3 days)
     */
    private int upcomingDeliveryCount;

    /**
     * HTML table of upcoming deliveries
     */
    private String upcomingDeliveriesTable;

    /**
     * Number of overdue POs
     */
    private int overdueCount;

    /**
     * HTML table of overdue POs
     */
    private String overduePOsTable;

    /**
     * Total value of overdue POs
     */
    private double overdueValue;

    /**
     * Maximum days overdue
     */
    private int maxDaysOverdue;

    /**
     * List of recipients for this email
     */
    private List<String> recipients;

    /**
     * Vendor name (if sending vendor-specific reminder)
     */
    private String vendorName;

    /**
     * Vendor email
     */
    private String vendorEmail;
}
