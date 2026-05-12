package com.nslindia.procurezone.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for data cleanup summary email data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataCleanupEmailData {
    private int archivedTransactionCount;
    private int expiredTokenCount;
    private int purgedLogCount;
    private double spaceReclaimed;
    private String cleanupStatus;
    private String errorSection;
}
