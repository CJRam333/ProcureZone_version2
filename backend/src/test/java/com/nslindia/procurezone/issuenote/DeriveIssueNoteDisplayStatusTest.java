package com.nslindia.procurezone.issuenote;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Verifies deriveIssueNoteDisplayStatus() against every
 * (issue_note_approved_status, issue_note_storesby_status) combination observed
 * in production data. None of these may fall through to "In Progress".
 */
class DeriveIssueNoteDisplayStatusTest {

    private static String derive(Integer approved, Integer stores) {
        return IssueNoteService.deriveIssueNoteDisplayStatus(approved, stores);
    }

    @Test
    void allProductionCombinationsMapToRealLabels() {
        // combination -> expected label (production counts in comments)
        assertEquals("Goods Issued",        derive(3, 11)); // 2,213
        assertEquals("Stores Rejected",     derive(3, 2));  // 45
        assertEquals("RM Rejected",         derive(2, 1));  // 26
        assertEquals("RM Approved",         derive(3, 1));  // 3 — awaiting stores
        assertEquals("Pending RM Approval", derive(1, 1));  // 2
    }

    @Test
    void nullColumnsAreSafe() {
        // Pre-fix new-app rows never set the two columns at creation
        assertEquals("Pending RM Approval", derive(null, null));
        assertEquals("RM Approved",         derive(3, null));
        assertEquals("RM Rejected",         derive(2, null)); // stores column irrelevant after RM rejection
    }

    @Test
    void inProgressFallbackNeverFiresForKnownData() {
        Integer[][] production = { {3, 11}, {3, 2}, {2, 1}, {3, 1}, {1, 1} };
        for (Integer[] c : production) {
            assertNotEquals("In Progress", derive(c[0], c[1]),
                    "combination (" + c[0] + "," + c[1] + ") fell through to In Progress");
        }
    }
}
