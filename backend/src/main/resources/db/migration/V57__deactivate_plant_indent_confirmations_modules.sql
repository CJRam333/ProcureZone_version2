-- Plant Indent and Confirmations are dormant/placeholder modules, now hidden from every role in the
-- UI (Sidebar hidden flag + routes redirect to /dashboard). Mark them inactive at the module-access
-- layer too so nothing re-surfaces them. No code deleted — re-enable by setting module_status = 1.
UPDATE tbl_module_master
SET module_status = 0
WHERE module_code IN ('PLANT_INDENTS', 'CONFIRMATIONS');
