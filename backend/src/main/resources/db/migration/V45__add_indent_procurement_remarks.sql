-- Add procurement remarks column to tbl_indent_master.
-- indent_po_number and indent_delivery_date already exist.
ALTER TABLE tbl_indent_master
    ADD COLUMN indent_procurement_remarks MEDIUMTEXT NULL
    AFTER indent_procurement_status;
