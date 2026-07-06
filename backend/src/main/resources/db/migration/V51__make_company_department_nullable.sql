-- Company/plant/section/location/department are optional, informational employee metadata and
-- must NEVER block creating an indent or issue note. V50 already made plant/section nullable;
-- this makes the remaining geo columns (company, department) nullable too, so creation succeeds
-- for employees who lack any of this data. Values are captured server-side when available and
-- left NULL otherwise.
ALTER TABLE tbl_issue_note   MODIFY COLUMN issue_note_company INT NULL;
ALTER TABLE tbl_issue_note   MODIFY COLUMN issue_note_dept    INT NULL;
ALTER TABLE tbl_indent_master MODIFY COLUMN indent_company    INT NULL;
ALTER TABLE tbl_indent_master MODIFY COLUMN indent_dept       INT NULL;
