package com.nslindia.procurezone.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for pending indent reminder email data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndentReminderEmailData {
    private String approverName;
    private String approverEmail;
    private int pendingCount;
    private String pendingIndentsTable;
    private int overdueCount;
    private String overdueIndentsTable;
    private double totalPendingValue;
    private int oldestPendingDays;
}
