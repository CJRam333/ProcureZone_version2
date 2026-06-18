-- =====================================================================
-- Correction Script: V_CORRECTION_001__restore_reference_data.sql
-- Date drafted   : 2026-06-16
-- Status         : PENDING REVIEW — DO NOT EXECUTE without approval
-- Author         : Migration Audit, 2026-06-16
--
-- Purpose:
--   Restore tbl_indent_status names for IDs 1–6, which were overwritten
--   by the original V41__fix_indent_status_names.sql migration before
--   the UPDATE statements were removed from that file.
--
-- Evidence:
--   V41 flyway_schema_history row: version=41, success=1, checksum=-1395102742
--   Current tbl_indent_status (queried 2026-06-16 against seeds_indent):
--     1 = Draft       (was: Pending)
--     2 = Submitted   (was: Rejected)
--     3 = Dept Head Approved (was: Approved)
--     4 = Rejected    (was: Final Approved)
--     5 = Proc. In Progress (was: Quotations Collected)
--     6 = PO Created  (was: Negotiation Done)
--     7–12: correct, unchanged
--
-- Design rules:
--   1. Every UPDATE is GUARDED — the WHERE clause checks both the ID
--      and the CURRENT (wrong) value. Re-running the script after it
--      succeeds is a safe no-op (0 rows affected per statement).
--   2. No DELETEs. No inserts to rows that already exist.
--   3. No business/transaction table is touched.
--   4. This script is intentionally NOT a Flyway migration (no V prefix
--      with a version number) to avoid altering flyway_schema_history.
--      Execute manually via mysql CLI or MySQL Workbench after approval.
--
-- Pre-flight verification (run before applying):
--   SELECT indent_status_id, indent_status_name
--   FROM tbl_indent_status
--   ORDER BY indent_status_id;
--   Expected output: IDs 1–6 show the CORRUPTED Spring labels.
--
-- Post-apply verification (run after applying):
--   SELECT indent_status_id, indent_status_name
--   FROM tbl_indent_status
--   ORDER BY indent_status_id;
--   Expected output: IDs 1–6 show the LEGACY correct names below.
-- =====================================================================

-- Safety: abort on error (for clients that support it)
-- MySQL CLI: run with --abort-source-on-error

-- Disable safe-update mode for this session (needed for UPDATE without PK in WHERE)
SET SQL_SAFE_UPDATES = 0;

-- -----------------------------------------------------------------------
-- ID 1: Pending  (currently stored as: Draft)
-- -----------------------------------------------------------------------
UPDATE tbl_indent_status
SET    indent_status_name = 'Pending'
WHERE  indent_status_id   = 1
  AND  indent_status_name = 'Draft';

-- -----------------------------------------------------------------------
-- ID 2: Rejected  (currently stored as: Submitted)
--
-- NOTE: In the Spring workflow, status ID 2 = "Submitted/RM Approval Pending".
-- In the legacy system, ID 2 = "Rejected". This is the root of the ID
-- semantics collision. The legacy DB value is authoritative; Spring-side
-- labels are overridden via SPRING_STATUS_LABELS in IndentService.java.
-- -----------------------------------------------------------------------
UPDATE tbl_indent_status
SET    indent_status_name = 'Rejected'
WHERE  indent_status_id   = 2
  AND  indent_status_name = 'Submitted';

-- -----------------------------------------------------------------------
-- ID 3: Approved  (currently stored as: Dept Head Approved)
-- -----------------------------------------------------------------------
UPDATE tbl_indent_status
SET    indent_status_name = 'Approved'
WHERE  indent_status_id   = 3
  AND  indent_status_name = 'Dept Head Approved';

-- -----------------------------------------------------------------------
-- ID 4: Final Approved  (currently stored as: Rejected)
-- -----------------------------------------------------------------------
UPDATE tbl_indent_status
SET    indent_status_name = 'Final Approved'
WHERE  indent_status_id   = 4
  AND  indent_status_name = 'Rejected';

-- -----------------------------------------------------------------------
-- ID 5: Quotations Collected  (currently stored as: Proc. In Progress)
-- -----------------------------------------------------------------------
UPDATE tbl_indent_status
SET    indent_status_name = 'Quotations Collected'
WHERE  indent_status_id   = 5
  AND  indent_status_name = 'Proc. In Progress';

-- -----------------------------------------------------------------------
-- ID 6: Negotiation Done  (currently stored as: PO Created)
-- -----------------------------------------------------------------------
UPDATE tbl_indent_status
SET    indent_status_name = 'Negotiation Done'
WHERE  indent_status_id   = 6
  AND  indent_status_name = 'PO Created';

-- Re-enable safe-update mode
SET SQL_SAFE_UPDATES = 1;

-- -----------------------------------------------------------------------
-- Verification query (always run after applying)
-- -----------------------------------------------------------------------
SELECT indent_status_id, indent_status_name
FROM   tbl_indent_status
ORDER  BY indent_status_id;

-- Expected result after successful application:
--  1  Pending
--  2  Rejected
--  3  Approved
--  4  Final Approved
--  5  Quotations Collected
--  6  Negotiation Done
--  7  PO Released
--  8  Hold
--  9  Cash Buy
-- 10  Goods Receipt
-- 11  Goods Issued
-- 12  DEO Order Pending

-- =====================================================================
-- End of V_CORRECTION_001
-- =====================================================================
