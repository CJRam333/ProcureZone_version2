-- Capture the SPECIFIC company chosen at creation, per line item.
--
-- In the material dropdown a material appears once per company that stocks it; the user picks one
-- material+company combination per line. That chosen company was captured on the frontend but never
-- sent/persisted, so the detail-page "Company" column and the list hover-preview had to fall back to
-- listing EVERY company associated with the material. These columns store the single selected company
-- so new records show exactly the company that was chosen.
--
-- Nullable and NOT back-filled: rows created before this change have no captured company; the app
-- falls back to the multi-company resolver (findCompanyNamesByMaterial) for those legacy rows.
-- Plain INT (no FK) — consistent with tbl_issue_note_details.issue_note_material, which is also an
-- unconstrained id column, and avoids a migration failure on any legacy row referencing a purged company.

ALTER TABLE tbl_indent_details
    ADD COLUMN indent_details_company INT NULL
    AFTER indent_details_material;

ALTER TABLE tbl_issue_note_details
    ADD COLUMN issue_note_details_company INT NULL
    AFTER issue_note_material;
