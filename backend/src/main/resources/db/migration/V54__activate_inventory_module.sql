-- Activate the Inventory module as a read-only stock view for all operational roles.
-- V43 seeded it hidden/future (module_status=0, module_is_future=1, roles 'ADMIN,SUPERADMIN').
-- The new read-only view (GET /api/v1/inventory/stock-view) reads real stock from
-- tbl_map_company_plant_material.map_quantity_stores — not the empty tbl_inventory_balance.
UPDATE tbl_module_master
SET module_status = 1,
    module_is_future = 0,
    module_default_roles = 'USER,SUPERVISOR,DEPTHEAD,ADMIN,SUPERADMIN,PROCUREMENT'
WHERE module_code = 'INVENTORY';
