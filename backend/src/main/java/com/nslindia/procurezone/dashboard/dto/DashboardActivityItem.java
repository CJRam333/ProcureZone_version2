package com.nslindia.procurezone.dashboard.dto;

import java.time.LocalDateTime;

/**
 * A single row in the dashboard "Latest Activity" feed — a unified view over indents and
 * issue notes visible to the current user. Ordering key is {@link #lastModifiedDate}.
 *
 * @param type             "INDENT" or "ISSUE_NOTE" — drives the frontend detail-link target
 * @param id               document id (used to build the detail-page path)
 * @param documentNumber   human-readable number (indent no. / issue note no.)
 * @param displayStatus    operational status label (from the module's deriveDisplayStatus)
 * @param lastModifiedDate last-modified timestamp (lmd) — the feed's sort key
 * @param creatorName      name of the creator; the frontend hides this column for roles that
 *                         only ever see their own documents
 */
public record DashboardActivityItem(
        String type,
        Integer id,
        String documentNumber,
        String displayStatus,
        LocalDateTime lastModifiedDate,
        String creatorName) {
}
