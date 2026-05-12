-- V37: Add missing columns to Company-Location-Material Mapping
-- -------------------------------------------------
-- Updating the legacy table to include new fields needed by the modern application
-- while maintaining existing legacy structure.

ALTER TABLE tbl_map_company_location_material
ADD COLUMN min_stock_level DECIMAL(20,2) DEFAULT 0.00,
ADD COLUMN max_stock_level DECIMAL(20,2) DEFAULT 0.00,
ADD COLUMN reorder_level DECIMAL(20,2) DEFAULT 0.00,
ADD COLUMN reorder_quantity DECIMAL(20,2) DEFAULT 0.00,
ADD COLUMN created_by INT NOT NULL DEFAULT 1,
ADD COLUMN created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN remarks VARCHAR(500);
