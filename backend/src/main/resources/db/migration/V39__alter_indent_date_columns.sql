-- Migration to resolve Data truncation error on indent date fields
-- Modifying varchar(20) date columns to appropriate date types
ALTER TABLE tbl_indent_master MODIFY indent_date DATETIME;
ALTER TABLE tbl_indent_master MODIFY indent_delivery_date DATE;
ALTER TABLE tbl_indent_master MODIFY indent_approvedby_date DATETIME;
ALTER TABLE tbl_indent_master MODIFY indent_final_date DATETIME;
ALTER TABLE tbl_indent_master MODIFY indent_no VARCHAR(100);
ALTER TABLE tbl_indent_details MODIFY indent_details_lmd DATETIME NOT NULL;
