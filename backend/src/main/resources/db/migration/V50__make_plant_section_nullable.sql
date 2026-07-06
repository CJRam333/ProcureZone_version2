-- Company/department/plant are now captured server-side from the creating employee's record,
-- and the plant/section dropdowns were removed from the indent & issue note creation forms.
-- There is no reliable employee→plant-id or employee→section mapping (emp_location is the best
-- proxy for plant, and may be null), so these columns must accept NULL or creation would fail.
ALTER TABLE tbl_issue_note MODIFY COLUMN issue_note_plant INT NULL;
ALTER TABLE tbl_issue_note MODIFY COLUMN issue_note_sec   INT NULL;
ALTER TABLE tbl_indent_master MODIFY COLUMN indent_plant INT NULL;
ALTER TABLE tbl_indent_master MODIFY COLUMN indent_sec   INT NULL;
