-- Confirmations is a placeholder module tied to the dormant Plant Indent flow (its one real
-- function, "goods issued", already exists on the Issue Note detail page). Restrict the module to
-- ADMIN/SUPERADMIN only, mirroring V52 for Plant Indent. Real enforcement is also applied at the
-- routes (router.tsx) and sidebar (Sidebar.tsx); there is no dedicated backend controller (the
-- pages call the issue-notes / GRN APIs, which are left untouched). No code deleted — re-enable
-- later by widening these role lists.
UPDATE tbl_module_master
SET module_default_roles = 'ADMIN,SUPERADMIN'
WHERE module_code = 'CONFIRMATIONS';
