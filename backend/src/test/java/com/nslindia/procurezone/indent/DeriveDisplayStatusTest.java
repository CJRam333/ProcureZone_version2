package com.nslindia.procurezone.indent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Verifies deriveDisplayStatus() against every (approved, final, procurement)
 * combination observed in production data (2026-07-03 audit). None of these may
 * fall through to the "In Progress" fallback.
 */
class DeriveDisplayStatusTest {

    private static String derive(Integer a, Integer f, Integer p) {
        return IndentService.deriveDisplayStatus(a, f, p);
    }

    @Test
    void allProductionCombinationsMapToRealLabels() {
        // combination -> expected label (counts from production audit in comments)
        assertEquals("PO Released",          derive(3, 4, 7));  // 562
        assertEquals("Hold",                 derive(3, 4, 8));  // 39
        assertEquals("Cash Buy",             derive(3, 4, 9));  // 34
        assertEquals("RM Rejected",          derive(2, 1, 1));  // 23
        assertEquals("Dept. Head Approved",  derive(3, 4, 4));  // 11
        assertEquals("Dept. Head Rejected",  derive(3, 2, 2));  // 10
        assertEquals("RM Approved",          derive(3, 1, 1));  // 10
        assertEquals("Negotiation Done",     derive(3, 5, 6));  // legacy edge
        assertEquals("RM Rejected",          derive(2, 2, 2));  // legacy edge
        assertEquals("Quotations Collected", derive(3, 4, 5));
        assertEquals("RM Approved",          derive(4, 1, 1));  // legacy edge
        assertEquals("Negotiation Done",     derive(3, 4, 6));
        assertEquals("Pending RM Approval",  derive(1, 1, 1));
    }

    @Test
    void preRepairCorruptionTriplesStillDisplayCorrectly() {
        // Rows produced by the pre-2026-07-02 l2Approve()/finalApproveIndent() bugs,
        // present until the owner runs docs/fix-l2-approved-indents.sql
        assertEquals("Dept. Head Approved", derive(3, 3, 1));
        assertEquals("In Procurement",      derive(3, 5, 1));
    }

    @Test
    void remainingMatrixEntriesPreserved() {
        assertEquals("Goods Receipt", derive(3, 4, 10));
        assertEquals("Goods Issued",  derive(3, 4, 11));
        assertEquals("Pending",       derive(null, null, null));
        assertEquals("RM Approved",   derive(3, null, null)); // null final = awaiting Dept Head
        assertEquals("Dept. Head Approved", derive(3, 4, null));
        assertEquals("Dept. Head Approved", derive(3, 4, 1)); // proc <= 4 = arrived at procurement
    }

    @Test
    void inProgressFallbackIsRareAndNeverFiresForKnownData() {
        Integer[][] production = {
                {3, 4, 7}, {3, 4, 8}, {3, 4, 9}, {2, 1, 1}, {3, 4, 4}, {3, 2, 2},
                {3, 1, 1}, {3, 5, 6}, {2, 2, 2}, {3, 4, 5}, {4, 1, 1}, {3, 4, 6}, {1, 1, 1},
        };
        for (Integer[] c : production) {
            assertNotEquals("In Progress", derive(c[0], c[1], c[2]),
                    "combination (" + c[0] + "," + c[1] + "," + c[2] + ") fell through to In Progress");
        }
    }
}
