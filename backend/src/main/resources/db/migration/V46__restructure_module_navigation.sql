-- Restructure module master: remove ADMINISTRATION parent group,
-- add EMAIL_TEMPLATES and MATERIAL_IMPORT as standalone modules,
-- update AUDIT_LOGS to include ROLE_VIEWER in defaults.

-- Remove the ADMINISTRATION umbrella module (items are now top-level)
DELETE FROM tbl_map_emp_module_access WHERE access_module = 'ADMINISTRATION';
DELETE FROM tbl_module_master WHERE module_code = 'ADMINISTRATION';

-- Update AUDIT_LOGS default roles to include ROLE_VIEWER
UPDATE tbl_module_master
SET module_default_roles = 'Super Admin,Admin,ROLE_VIEWER'
WHERE module_code = 'AUDIT_LOGS';

-- Add EMAIL_TEMPLATES standalone module
INSERT IGNORE INTO tbl_module_master
    (module_code, module_name, module_description, module_status, module_is_future, module_default_roles, module_lmd, module_lmu)
VALUES
    ('EMAIL_TEMPLATES', 'Email Templates', 'Manage approval workflow email templates', 1, 0, 'Super Admin,Admin', CURDATE(), 1);

-- Add MATERIAL_IMPORT standalone module
INSERT IGNORE INTO tbl_module_master
    (module_code, module_name, module_description, module_status, module_is_future, module_default_roles, module_lmd, module_lmu)
VALUES
    ('MATERIAL_IMPORT', 'Material Import', 'Bulk material data import', 1, 0, 'Super Admin,Admin', CURDATE(), 1);

-- Seed module access for SUPERADMIN and ADMIN employees for the new modules
INSERT IGNORE INTO tbl_map_emp_module_access
    (access_emp, access_module, access_enabled, access_granted_by)
SELECT e.emp_number, m.module_code, 1, 1
FROM tbl_emp_master e
CROSS JOIN tbl_module_master m
LEFT JOIN tbl_map_emp_roles er ON er.emp_number = e.emp_number AND er.emp_roles_status = 1
LEFT JOIN tbl_roles_master r ON r.role_id = er.role_id
WHERE e.emp_status = 1
  AND m.module_code IN ('EMAIL_TEMPLATES', 'MATERIAL_IMPORT')
  AND r.role_code IN ('Super Admin', 'Admin')
  AND NOT EXISTS (
      SELECT 1 FROM tbl_map_emp_module_access
      WHERE access_emp = e.emp_number AND access_module = m.module_code
  )
GROUP BY e.emp_number, m.module_code;

-- Seed AUDIT_LOGS access for ROLE_VIEWER employees who don't already have it
INSERT IGNORE INTO tbl_map_emp_module_access
    (access_emp, access_module, access_enabled, access_granted_by)
SELECT e.emp_number, 'AUDIT_LOGS', 1, 1
FROM tbl_emp_master e
LEFT JOIN tbl_map_emp_roles er ON er.emp_number = e.emp_number AND er.emp_roles_status = 1
LEFT JOIN tbl_roles_master r ON r.role_id = er.role_id
WHERE e.emp_status = 1
  AND r.role_code = 'ROLE_VIEWER'
  AND NOT EXISTS (
      SELECT 1 FROM tbl_map_emp_module_access
      WHERE access_emp = e.emp_number AND access_module = 'AUDIT_LOGS'
  )
GROUP BY e.emp_number;
