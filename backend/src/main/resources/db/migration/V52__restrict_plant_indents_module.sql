-- Plant Indent is not in active use. Restrict the module to ADMIN/SUPERADMIN only across every
-- layer; this migration covers the module-access default so the module is not seeded/shown for
-- other roles. Real enforcement is also applied at the API (PlantIndentController @PreAuthorize),
-- routes (router.tsx) and sidebar (Sidebar.tsx). No data or code is deleted — re-enable later by
-- widening these role lists.
UPDATE tbl_module_master
SET module_default_roles = 'ADMIN,SUPERADMIN'
WHERE module_code = 'PLANT_INDENTS';
