-- Fix "Illegal mix of collations (latin1_swedish_ci,IMPLICIT) and (utf8mb4_0900_ai_ci,COERCIBLE)".
-- The legacy tables use latin1_swedish_ci; the JDBC connection (Connector/J 8 default) and string
-- literals are utf8mb4_0900_ai_ci. Any = or LIKE between a latin1 column and a utf8mb4 value throws
-- (e.g. SAP import findByCode('BPW-TIPBOX-200ΜL') with a Greek Mu, and the Inventory search LIKE on
-- material code/name/description). Converting the compared tables to utf8mb4 makes the comparisons
-- legal and lets non-latin1 characters be stored.
--
-- Scope — only tables whose string columns are compared to utf8mb4 literals in the failing paths:
--   tbl_material_master              — Inventory search LIKE + SAP import findByCode(material)  [REQUIRED]
--   tbl_company_master               — SAP import findByCode(company code) equality             [REQUIRED]
--   tbl_plant_master                 — SAP import findByCode(plant code) equality               [REQUIRED]
--   tbl_map_company_plant_material   — the stock table joined in both paths (joins are on int ids,
--                                      no string comparison today) — converted for consistency so
--                                      future string filters/joins can't reintroduce the mismatch.
-- Converting a table that is ALREADY utf8mb4 is a harmless no-op, so this is safe to run even if
-- STEP 1 shows some of these are already utf8mb4.
--
-- CAUTIONS (verify before running in production):
--   1. Data integrity: `CONVERT TO CHARACTER SET utf8mb4` reinterprets stored bytes from latin1 to
--      utf8mb4. For GENUINE latin1 data this is lossless (utf8mb4 is a superset). BUT if any column
--      already holds UTF-8 bytes mislabelled as latin1 ("double-encoded"), this CONVERT will mangle
--      them. Check a few special-character rows first, e.g.:
--        SELECT material_id, HEX(material_code) FROM tbl_material_master WHERE material_code REGEXP '[^ -~]';
--      If those bytes are already valid UTF-8 sequences, use the binary round-trip instead
--      (MODIFY the column to VARBINARY, then MODIFY back to VARCHAR ... CHARACTER SET utf8mb4) rather
--      than CONVERT TO CHARACTER SET. Most material codes are ASCII and unaffected either way.
--   2. Index key length: utf8mb4 uses up to 4 bytes/char. InnoDB's index key-prefix limit is 3072
--      bytes (DYNAMIC/COMPRESSED row format, MySQL 8 default). Only an indexed VARCHAR longer than
--      ~768 chars could exceed it; material_code/name/description are <=255, so 255*4=1020 bytes — well
--      under the limit. Verify no index exists on a varchar > 768 in these tables before running.

ALTER TABLE tbl_material_master            CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE tbl_company_master             CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE tbl_plant_master               CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE tbl_map_company_plant_material CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
