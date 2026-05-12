-- =====================================================
-- Migration: V28__create_company_plant_material_table
-- Purpose : Seed company-plant-material inventory data
-- NOTE    : Table tbl_pz_map_company_plant_material already exists
-- =====================================================

-- Insert sample inventory data (safe & idempotent)
INSERT IGNORE INTO tbl_pz_map_company_plant_material
(
    map_comp,
    map_plant,
    map_material,
    map_quantity_stores,
    map_status,
    map_lmu,
    map_lmd
)
SELECT
    1 AS map_comp,
    1 AS map_plant,
    m.material_id AS map_material,
    FLOOR(RAND() * 1000) + 50 AS map_quantity_stores,
    1 AS map_status,
    1 AS map_lmu,
    CURDATE() AS map_lmd
FROM tbl_material_master m
WHERE m.material_status = 1
LIMIT 10;

-- =====================================================
-- End of V28
-- =====================================================
