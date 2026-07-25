-- Pass 3 — Quantity editing during approval + audit trail.
--
-- Adds:
--   1. tbl_indent_details_qty_audit    — one row per RM/DeptHead line-quantity change on an indent
--   2. issue_note_details_rm_qty       — RM-adjusted qty for issue notes (the requester's original,
--                                        issue_note_details_quantity, is NEVER overwritten)
--   3. tbl_issue_note_details_qty_audit — one row per RM line-quantity change on an issue note
--
-- Audit rows are written ONLY when a stage actually changes a line quantity; an approval that
-- leaves quantities untouched writes no audit rows. The audit tables are the authoritative edit
-- history (the details rows keep only a single last-modified stamp).
--
-- FK targets confirmed against the entities: tbl_indent_details PK = indent_details_id,
-- tbl_issue_note_details PK = issue_note_details_id.

-- 1a — Indent line-quantity audit log ---------------------------------------------------------
CREATE TABLE tbl_indent_details_qty_audit (
    audit_id      INT AUTO_INCREMENT PRIMARY KEY,
    detail_id     INT            NOT NULL,
    stage         VARCHAR(20)    NOT NULL,          -- 'RM' or 'DEPTHEAD'
    old_quantity  DECIMAL(18,2),                    -- value before this edit (null if none captured)
    new_quantity  DECIMAL(18,2)  NOT NULL,          -- value this stage set
    edited_by     INT            NOT NULL,          -- emp_number of the editor
    edited_at     DATETIME       NOT NULL,
    INDEX idx_ind_qty_audit_detail (detail_id),
    CONSTRAINT fk_ind_qty_audit_detail FOREIGN KEY (detail_id)
        REFERENCES tbl_indent_details (indent_details_id) ON DELETE CASCADE
);

-- 1b — Issue-note RM-adjusted quantity column + audit log --------------------------------------
-- Placed AFTER issue_note_details_quantity (the requester's original in the new schema; the legacy
-- issue_note_details_requested_quantity column is not mapped by the app). Positional only.
ALTER TABLE tbl_issue_note_details
    ADD COLUMN issue_note_details_rm_qty DECIMAL(18,2) NULL
    AFTER issue_note_details_quantity;

CREATE TABLE tbl_issue_note_details_qty_audit (
    audit_id      INT AUTO_INCREMENT PRIMARY KEY,
    detail_id     INT            NOT NULL,
    stage         VARCHAR(20)    NOT NULL,          -- 'RM' (issue notes have a single edit stage)
    old_quantity  DECIMAL(18,2),
    new_quantity  DECIMAL(18,2)  NOT NULL,
    edited_by     INT            NOT NULL,
    edited_at     DATETIME       NOT NULL,
    INDEX idx_in_qty_audit_detail (detail_id),
    CONSTRAINT fk_in_qty_audit_detail FOREIGN KEY (detail_id)
        REFERENCES tbl_issue_note_details (issue_note_details_id) ON DELETE CASCADE
);
