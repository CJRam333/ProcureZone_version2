-- =============================================================================
-- Data Repair: DeptHead-approved indents stuck at wrong three-column state
-- =============================================================================
-- Root cause: two code paths both produced invalid (approvedStatus, finalStatus,
-- procurementStatus) triples that did not match any entry in deriveDisplayStatus(),
-- causing indents to show "In Progress" instead of "Dept. Head Approved" and
-- making the entire procurement sub-workflow invisible to PROCUREMENT users.
--
-- Path A — smart-route (ApprovalController /approvals/indents/{id}/approve):
--   finalApproveIndent() set finalStatus=5, never set procurementStatus.
--   Result: (approvedStatus=3, finalStatus=5, procurementStatus=1, status=5)
--
-- Path B — legacy endpoint (/indents/{id}/l2-approve):
--   l2Approve() set finalStatus=3, never set procurementStatus.
--   Result: (approvedStatus=3, finalStatus=3, procurementStatus=1, status=3)
--
-- Both are fixed in IndentService.java (commit in git log).
-- Correct state after DeptHead approval: (approvedStatus=3, finalStatus=4,
-- procurementStatus=4) which maps to "Dept. Head Approved" in the matrix.
--
-- INSTRUCTIONS:
--   1. Run each SELECT first to confirm the rows are correct to fix.
--   2. Verify the indent_no values match indents you know were DeptHead-approved.
--   3. Only then uncomment and run the corresponding UPDATE.
--   4. Run the verification query at the bottom after each UPDATE.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- PATH A: Corrupted by finalApproveIndent() via smart-route
-- These indents have status=5, finalStatus=5, procurementStatus=1
-- ---------------------------------------------------------------------------

SELECT
    indent_id,
    indent_no,
    indent_approved_status,
    indent_final_status,
    indent_procurement_status,
    indent_status
FROM tbl_indent_master
WHERE indent_approved_status   = 3
  AND indent_final_status      = 5
  AND indent_procurement_status = 1
  AND indent_status            = 5;

-- After confirming the rows above are correct, uncomment and run:
-- UPDATE tbl_indent_master
-- SET   indent_final_status       = 4,
--       indent_procurement_status = 4
-- WHERE indent_approved_status    = 3
--   AND indent_final_status       = 5
--   AND indent_procurement_status = 1
--   AND indent_status             = 5;


-- ---------------------------------------------------------------------------
-- PATH B: Corrupted by l2Approve() via legacy /indents/{id}/l2-approve endpoint
-- These indents have status=3, finalStatus=3, procurementStatus=1
-- ---------------------------------------------------------------------------

SELECT
    indent_id,
    indent_no,
    indent_approved_status,
    indent_final_status,
    indent_procurement_status,
    indent_status
FROM tbl_indent_master
WHERE indent_approved_status    = 3
  AND indent_final_status       = 3
  AND indent_procurement_status = 1
  AND indent_status             = 3;

-- After confirming the rows above are correct, uncomment and run:
-- UPDATE tbl_indent_master
-- SET   indent_final_status       = 4,
--       indent_procurement_status = 4
-- WHERE indent_approved_status    = 3
--   AND indent_final_status       = 3
--   AND indent_procurement_status = 1
--   AND indent_status             = 3;


-- ---------------------------------------------------------------------------
-- Verification: after running the UPDATE(s), this should return 0 rows
-- ---------------------------------------------------------------------------
SELECT COUNT(*) AS remaining_corrupted
FROM tbl_indent_master
WHERE indent_approved_status    = 3
  AND indent_procurement_status = 1
  AND indent_final_status      IN (3, 5);

-- And these should now appear in the procurement queue (non-zero after repair):
SELECT
    indent_id,
    indent_no,
    indent_approved_status,
    indent_final_status,
    indent_procurement_status,
    indent_status
FROM tbl_indent_master
WHERE indent_approved_status    = 3
  AND indent_final_status       = 4
  AND indent_procurement_status = 4;
