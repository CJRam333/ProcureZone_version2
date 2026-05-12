package com.nslindia.procurezone.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for monthly procurement analytics email data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAnalyticsEmailData {
    private String monthYear;
    private int indentsCreated;
    private int indentsApproved;
    private double approvalRate;
    private int avgApprovalDays;
    private int grnCount;
    private double grnTotalValue;
    private double onTimeDeliveryRate;
    private int issueNoteCount;
    private double issuedTotalValue;
    private int totalReceipts;
    private int totalIssues;
    private int netChange;
    private String trendsSection;
    private List<String> recipients;
}
