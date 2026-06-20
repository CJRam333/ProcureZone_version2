-- Seed default module access rows for all existing active employees.
-- Rows are computed from each employee's active role(s) in tbl_map_emp_roles.
-- The GROUP BY + MAX(CASE) handles employees with multiple roles correctly:
--   any qualifying role enables the module.
-- INSERT IGNORE skips employees who already have a custom override row.

INSERT IGNORE INTO tbl_map_emp_module_access
    (access_emp, access_module, access_enabled, access_granted_by)
SELECT
    e.emp_number,
    m.module_code,
    MAX(CASE
        WHEN m.module_code IN ('INDENTS','PLANT_INDENTS','ISSUE_NOTES','REPORTS','CONFIRMATIONS') THEN 1
        WHEN m.module_code = 'MASTERS'        AND r.role_code IN ('Super Admin','Admin') THEN 1
        WHEN m.module_code = 'AUDIT_LOGS'     AND r.role_code IN ('Super Admin','Admin') THEN 1
        WHEN m.module_code = 'ADMINISTRATION' AND r.role_code  = 'Super Admin'           THEN 1
        ELSE 0
    END) AS access_enabled,
    1 AS access_granted_by
FROM tbl_emp_master e
CROSS JOIN tbl_module_master m
LEFT JOIN tbl_map_emp_roles er
    ON er.emp_number = e.emp_number AND er.emp_roles_status = 1
LEFT JOIN tbl_roles_master r
    ON r.role_id = er.role_id
WHERE e.emp_status = 1
  AND m.module_is_future = 0
  AND NOT EXISTS (
      SELECT 1 FROM tbl_map_emp_module_access
      WHERE access_emp    = e.emp_number
        AND access_module = m.module_code
  )
GROUP BY e.emp_number, m.module_code;
