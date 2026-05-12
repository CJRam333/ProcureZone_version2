-- =====================================================
-- Migration   : V38__add_company_plant_material_missing_columns
-- Description : Add map_max_level and map_reorder_level
--               to tbl_map_company_plant_material if missing
-- =====================================================

-- Add map_max_level
DROP PROCEDURE IF EXISTS AddMaxLevelColumn;
DELIMITER //
CREATE PROCEDURE AddMaxLevelColumn()
BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    IF NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS 
                  WHERE TABLE_SCHEMA = DATABASE() 
                  AND TABLE_NAME = 'tbl_map_company_plant_material' 
                  AND COLUMN_NAME = 'map_max_level') THEN
        ALTER TABLE `tbl_map_company_plant_material` ADD COLUMN `map_max_level` decimal(20,2) DEFAULT NULL;
    END IF;
END //
DELIMITER ;
CALL AddMaxLevelColumn();
DROP PROCEDURE AddMaxLevelColumn;

-- Add map_reorder_level
DROP PROCEDURE IF EXISTS AddReorderLevelColumn;
DELIMITER //
CREATE PROCEDURE AddReorderLevelColumn()
BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    IF NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS 
                  WHERE TABLE_SCHEMA = DATABASE() 
                  AND TABLE_NAME = 'tbl_map_company_plant_material' 
                  AND COLUMN_NAME = 'map_reorder_level') THEN
        ALTER TABLE `tbl_map_company_plant_material` ADD COLUMN `map_reorder_level` decimal(20,2) DEFAULT NULL;
    END IF;
END //
DELIMITER ;
CALL AddReorderLevelColumn();
DROP PROCEDURE AddReorderLevelColumn;

-- We also make sure map_quantity_stores exists, just in case it was map_quantity
DROP PROCEDURE IF EXISTS AddQuantityStoresColumn;
DELIMITER //
CREATE PROCEDURE AddQuantityStoresColumn()
BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    IF NOT EXISTS(SELECT 1 FROM information_schema.COLUMNS 
                  WHERE TABLE_SCHEMA = DATABASE() 
                  AND TABLE_NAME = 'tbl_map_company_plant_material' 
                  AND COLUMN_NAME = 'map_quantity_stores') THEN
        IF EXISTS(SELECT 1 FROM information_schema.COLUMNS 
                  WHERE TABLE_SCHEMA = DATABASE() 
                  AND TABLE_NAME = 'tbl_map_company_plant_material' 
                  AND COLUMN_NAME = 'map_quantity') THEN
            ALTER TABLE `tbl_map_company_plant_material` CHANGE `map_quantity` `map_quantity_stores` decimal(20,2) DEFAULT NULL;
        ELSE
            ALTER TABLE `tbl_map_company_plant_material` ADD COLUMN `map_quantity_stores` decimal(20,2) DEFAULT NULL;
        END IF;
    END IF;
END //
DELIMITER ;
CALL AddQuantityStoresColumn();
DROP PROCEDURE AddQuantityStoresColumn;

-- =====================================================
-- End of V38
-- =====================================================
