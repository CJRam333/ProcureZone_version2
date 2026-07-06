-- Issue note (and other) creation failed with:
--   java.sql.SQLSyntaxErrorException: Unknown column 'log_created_date' in 'field list'
-- The EmailLog entity (tbl_email_log) maps log_created_date but the deployed table lacks it,
-- so the notification INSERT inside the business transaction failed and rolled everything back.
-- Add the missing column so email/audit logging works. (Rollback isolation is also handled in
-- code via REQUIRES_NEW so a logging failure can never revert a business operation again.)
ALTER TABLE tbl_email_log ADD COLUMN log_created_date DATETIME NULL;
