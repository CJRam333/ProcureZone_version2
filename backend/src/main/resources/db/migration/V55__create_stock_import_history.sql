-- Tracks each SAP stock CSV file processed by the event-driven importer, keyed by filename, so a
-- file is imported exactly once (idempotency). MAX(imported_at) WHERE status='SUCCESS' also serves
-- as the "last stock import" marker for the upcoming reconciliation feature.
CREATE TABLE IF NOT EXISTS tbl_stock_import_history (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  file_name     VARCHAR(255) NOT NULL UNIQUE,
  imported_at   DATETIME NOT NULL,
  rows_updated  INT,
  status        VARCHAR(20) NOT NULL,   -- SUCCESS / FAILED
  message       VARCHAR(500)
);
