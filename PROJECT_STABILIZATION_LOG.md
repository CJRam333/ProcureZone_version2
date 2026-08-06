# Project Stabilization Log

---

## 2026-07-15

### Startup Failure — findActiveSupervisors ORDER BY referenced non-existent field `reportStart`

The 2026-07-14 reporting queries used `ORDER BY er.reportStart DESC, er.id DESC`, but the
`EmployeeReporting` entity has no `reportStart` field — the `report_start` column maps to
**`effectiveDate`**. Hibernate validates all `@Query` at startup, so this `PathElementException`
prevented the whole app from booting.

**STEP 1 — `EmployeeReporting` fields ↔ columns:**
`report_id`→`id` (PK), `report_sub`→`subordinateEmployeeNumber`, `report_sup`→`supervisorEmployeeNumber`,
`report_start`→**`effectiveDate`**, `report_end`→`endDate`, `report_status`→`status`,
`report_lmd`→`lastModifiedDate`, `report_lmu`→`lastModifiedBy`. (`er.id` in the ORDER BY was already
correct; only `reportStart` was wrong.)

**STEP 2 — fix.** Both new queries changed `ORDER BY er.reportStart DESC, er.id DESC` →
`ORDER BY er.effectiveDate DESC, er.id DESC`:
- `findActiveSupervisorRows(subordinateId)`
- `findActiveSupervisors(subordinateId)`

**STEP 3 — full `@Query` audit (43 repositories).** Rather than eyeball 43 files, verified
mechanically by booting the context (STEP 4): Spring Data validates every declared `@Query` at
startup (that is exactly what failed originally), so a successful boot proves all query property
paths resolve. Reasoning cross-check: any `@Query` present before the last successful prod boot is
already validated; only queries added/changed since could newly break startup. The only such
additions were the 2026-07-14 reporting queries (the `reportStart` ones, now fixed) — the 2026-07-10
`findAllByCompanyAndPlantAndMaterial` and `StockImportHistory` queries had already booted in prod
(the SAP import ran), and the context boot below re-confirms them all.

**STEP 4 — startup verified (not just compiled).** Ran the H2-backed `@SpringBootTest`
`AuthControllerIntegrationTest`: the **context started cleanly** — EntityManagerFactory + all Spring
Data repositories initialized, security chain and dispatcher servlet came up, requests executed
(`Tests run: 2`). No `PathElementException` / "Could not resolve attribute" — so every `@Query`
validated. `mvn compile` alone would NOT catch this; the context boot does.

**Unrelated pre-existing test failure (flagged, not fixed — out of scope):** the same run had one
assertion failure — `login_withValidCredentials` (`legacy.user@nsl.com`, contains `@`) now routes to
the LDAP branch (added 2026-07-09), and `LdapAuthService` hits `tbl_ldap_config`, which the H2 test
schema doesn't create → 401. This is a test-environment gap (H2 lacks `tbl_ldap_config`) surfaced by
the `@`-routing, not a query/startup issue and not from this change. Deploy uses `-DskipTests`, so it
doesn't block release. Follow-up: seed `tbl_ldap_config` in the H2 test schema or use a non-`@` test
user / mock `LdapAuthService` in that test.

**Build:** `mvn compile` clean; context boots (queries validated) via `AuthControllerIntegrationTest`.
Backend-only — no frontend change.

---

### Enforce Single Reporting Manager — deactivate-then-insert + defensive supervisor lookup

Business decision: exactly one active reporting manager per employee. Duplicate data already cleaned
in prod+dev. This makes the code enforce it so duplicates can't reappear, and stops the employee-detail
"Query did not return a unique result: 2 results" throw.

**PART 1 — reassignment never creates a duplicate active row.** Added a shared
`EmployeeReportingRepository.deactivateActiveSupervisors(subordinateId)` default method that soft-deletes
ALL current active rows (`status=0`, `report_end=today`, `lmd=today`; history preserved). Applied
deactivate-then-insert to **every** reporting insert path:
- `EmployeeService.createEmployee` — before: inserted with no deactivation. After: `deactivateActiveSupervisors()` then insert.
- `EmployeeService.updateEmployee` — before: deactivated via single-result `findActiveSupervisor(...).ifPresent(...)` (which itself threw on a duplicate). After: `deactivateActiveSupervisors()` (all rows) then insert.
- `EmployeeReportingService.createReportingRelationship` (the reporting-management screen's POST `/employee-reporting`) — before: only checked exact (sub,sup) duplicate + circular ref, then inserted → assigning a *different* manager created a 2nd active row. After: `deactivateActiveSupervisors()` then insert.
- (Confirmed the only other `save()`s — `EmployeeReportingService` update/delete-by-id — edit one existing row in place; they don't create a new active row.)

**PART 2 — supervisor lookup back to single-result, but never throws.** Replaced the throwing
single-result methods with deterministically-ordered List methods (`ORDER BY report_start DESC, id DESC`):
- Removed `Optional<Integer> findSupervisorNumber` → added `List<Integer> findActiveSupervisors`.
- Removed `Optional<EmployeeReporting> findActiveSupervisor` → added `List<EmployeeReporting> findActiveSupervisorRows`.
- Every call site takes `.stream().findFirst()` — returns the single current supervisor, deterministically
  picks the latest if a stray duplicate ever slips through, never throws. **Call sites updated (4):**
  `EmployeeService.mapToEmployeeResponse` (read), `EmployeeReportingService.getSupervisor` (read),
  `EmployeeReportingService.getManagerChain` (hierarchy walk), and `EmployeeService.updateEmployee`
  deactivation (now via the shared helper). No `findSupervisorNumber`/`findActiveSupervisor` references remain.

**PART 3 — employee detail.** `EmployeeResponse` already carries a single `reportingManagerId` +
`reportingManagerName`; `mapToEmployeeResponse` resolves them via the defensive lookup (first active).
Frontend `EmployeeDetailPage` already shows one "Reporting Manager" field. No change needed.

**PART 4 — access-denied symptom.** The `moduleaccess` package does not use the reporting repository at
all; supervisor-scoped visibility uses `findSubordinateNumbers` (List, already safe) and `hasSupervisor`
(boolean COUNT, safe). The only place that threw was employee-detail `mapToEmployeeResponse` — so the
"access denied" for those 21 employees was that endpoint's 500 surfacing, not a separate module-access
break. With the throw removed and data cleaned, the path is safe.

**PART 5 — reporting UI.** `ReportingHierarchyPage` (mappings) posts `{subordinate, supervisor}` to
`/employee-reporting` → `createReportingRelationship`, which now deactivate-then-inserts — so assigning a
manager via that screen enforces single-supervisor. It's a per-relationship editor; no frontend change
required (backend enforcement covers all entry points).

**Build:** backend `mvn compile` clean; `tsc -b && vite build` clean. Backend-only — frontend bundle
unchanged (`index-Dvex2nF9.js`).

---

### Collation Mismatch — Real Fix at Connection Level (V56 table-conversion was WRONG, reverted)

**Correction to 2026-07-10 (V56):** production `information_schema` confirmed all four tables AND the
`material_code`/`name`/`description` columns are **already `utf8mb4 / utf8mb4_0900_ai_ci`**. There is
NO latin1 table. The V56 blanket table-conversion fixed a non-existent problem — **deleted** the
migration (`V56__convert_material_tables_to_utf8mb4.sql`); it was flagged-to-hold on 07-10 and never
run in prod.

**Real source of `Illegal mix of collations (latin1_swedish_ci,IMPLICIT) and (utf8mb4_0900_ai_ci,COERCIBLE)`:**
the **connection/session**, not stored data. The MySQL server's session default
`collation_connection` is `latin1_swedish_ci`, so the JDBC driver sent string literals as latin1;
comparing a latin1 literal to a utf8mb4 column (`=` in SAP import `findByCode`, `LIKE` in Inventory
search) throws. The latin1 side is IMPLICIT = the connection default, which is why utf8mb4 columns
still failed.

**Diagnostics (owner runs; no DB access here) — expected to confirm:**
- STEP 1: `SHOW VARIABLES LIKE 'collation%'` / `'character_set%'` — expect `collation_connection =
  latin1_swedish_ci` (the culprit). If it already shows utf8mb4, re-examine per-column collations.
- STEP 4: per-column `collation_name` on `tbl_company_master`/`tbl_plant_master` — expect utf8mb4
  (tables confirmed utf8mb4); if any individual `comp_name`/`plant_name` column overrides to latin1,
  that column needs a targeted `MODIFY COLUMN … utf8mb4` (not applied yet — pending that result).

**Fix applied (STEP 3 — connection level).** Pinned the connection to utf8mb4 in `application.yml`:
```
jdbc:mysql://127.0.0.1:3306/seeds_indent?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&socketTimeout=30000&connectTimeout=10000&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_0900_ai_ci
```
Added `useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_0900_ai_ci`. This forces
every connection to send/compare string literals as utf8mb4, removing the latin1 side of the mismatch
for both the SAP import and Inventory search. `application-prod.yml` has no datasource URL — the
**business owner must add the same params to the server's prod datasource URL**.

**Migration numbering note:** V56 is retired/deleted. To avoid any Flyway checksum conflict (in case
V56 was applied in a dev environment), the **next migration should be V57**, not a reused V56. If V56
was ever applied anywhere, run `flyway repair` to drop its history row.

**Build:** backend `mvn compile` clean. Config + migration-deletion only — no Java/frontend change.

**Verify after deploy (with the prod URL param added + restart):** `SHOW VARIABLES` shows
`collation_connection = utf8mb4_0900_ai_ci`; SAP import of `BPW-TIPBOX-200ΜL` and Inventory search
both work with no collation error.

---

## 2026-07-10

### Collation Mismatch (latin1 vs utf8mb4) Breaking SAP Import + Inventory Search — V56 convert

Both failed with `Illegal mix of collations (latin1_swedish_ci,IMPLICIT) and
(utf8mb4_0900_ai_ci,COERCIBLE)`: legacy tables are `latin1_swedish_ci`, but the JDBC connection and
string literals are `utf8mb4_0900_ai_ci`, so any `=`/`LIKE` between a latin1 column and a utf8mb4
value throws (worse for non-latin1 chars like the Greek Mu in `BPW-TIPBOX-200ΜL`, which latin1 can't
even represent).

**STEP 1 — diagnosis (owner runs the `information_schema` queries; can't from here).** The failing
comparisons are: SAP import `findByCode` on material/company/plant codes (`=`), and the Inventory
search `LIKE` on material code/name/description. Those live in `tbl_material_master`,
`tbl_company_master`, `tbl_plant_master`; `tbl_map_company_plant_material` is joined but only on int
ids (no string comparison today).

**STEP 2 — migration V56** (next after V55) converts the compared tables to
`utf8mb4 / utf8mb4_0900_ai_ci`:
- `tbl_material_master` — Inventory `LIKE` + import `findByCode(material)` — **required** (the actual
  failing table for both).
- `tbl_company_master`, `tbl_plant_master` — import `findByCode(code)` equality — **required** for the
  import path.
- `tbl_map_company_plant_material` — the stock table joined in both paths (joins on int ids only, no
  string comparison today); converted for consistency so future string filters can't reintroduce the
  mismatch. (Converting an already-utf8mb4 table is a harmless no-op, so V56 is safe even if STEP 1
  shows some are already utf8mb4.)

**Cautions checked & documented in the migration:**
1. **Data integrity / double-encoding:** `CONVERT TO CHARACTER SET utf8mb4` is lossless for *genuine*
   latin1 data, but *mangles* any column already holding UTF-8 bytes mislabelled as latin1. The
   migration includes a `HEX(...) REGEXP '[^ -~]'` check to run first, and the binary round-trip
   alternative (VARCHAR→VARBINARY→VARCHAR utf8mb4) if double-encoding is found. Most codes are ASCII
   and unaffected. Flagged for owner verification before prod run.
2. **Index key length:** utf8mb4 = up to 4 bytes/char; InnoDB prefix limit 3072 bytes (MySQL 8
   default row format). The relevant varchars are ≤255 chars (≤1020 bytes) — safe. Only an indexed
   varchar > ~768 chars would be at risk; none in these tables.

**STEP 3 — JDBC charset.** `application.yml` datasource URL:
`jdbc:mysql://127.0.0.1:3306/seeds_indent?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&socketTimeout=30000&connectTimeout=10000`
— **no** `useUnicode`/`characterEncoding`/`connectionCollation` params, so Connector/J 8 uses its
default utf8mb4 connection charset. That default is correct and is exactly the target end-state
(utf8mb4 tables + utf8mb4 connection); the table conversion is the real fix, so **no URL change** was
made. (`application-prod.yml` has no datasource override.)

**Build:** backend `mvn compile` clean. SQL-migration-only — no Java/frontend change; bundle unchanged.

**Verify after deploy:** V56 applies in `flyway_schema_history`; re-drop the failing
`Material(<date>).CSV` → the `BPW-TIPBOX-200ΜL` row imports (no collation error); Inventory search by
material name/code returns results without the mix-of-collations error.

---

### SAP Import — duplicate-row tolerance + per-row transaction isolation (3 bad rows no longer revert 562)

The event-driven import updated 562 rows but the whole batch rolled back at commit:
`NonUniqueResultException: Query did not return a unique result: 11 results` on 3 rows.

**STEP 1 — where the 11 came from.** Every lookup in the import path returns a single-result
`Optional` and therefore throws on >1: `companyRepository.findByCode`, `plantRepository.findByCode`,
**`materialRepository.findByCode`**, and `companyPlantMaterialMapRepository.findByCompanyAndPlantAndMaterial`.
Since production/dev confirmed **zero duplicate (map_comp, map_plant, map_material) triples**, the
mapping lookup cannot return 11 — so the 11 is a **duplicate material code** in `tbl_material_master`:
the import auto-creates a Material keyed only by code (`findOrCreateMaterial`), and over runs (and
within-file repeats not yet flushed) duplicate codes accumulate, so `findByCode` returns 11.
(Owner can confirm: `SELECT COUNT(*) FROM tbl_material_master WHERE material_code='<failed code>'`.)
Fixed both the material lookup AND the mapping lookup, plus per-row isolation as defence-in-depth.

**STEP 2 — duplicate-tolerant lookups.**
- `MaterialRepository.findFirstByCodeOrderByIdAsc(code)` (new) — first match, never throws;
  `findOrCreateMaterial` now uses it.
- `CompanyPlantMaterialMapRepository.findAllByCompanyAndPlantAndMaterial(...)` (new, List; the
  existing Optional method is kept for `CompanyPlantMaterialService`). `updateCompanyPlantMaterialMapping`
  now: **empty** → INSERT; **exactly one** → UPDATE; **more than one** → UPDATE the primary
  (lowest `map_id`) row's `map_quantity_stores`, **WARN** naming company+plant+material and the
  duplicate count, and do **not** throw (extras left for a separate cleanup task).

**STEP 3 — per-row transaction isolation.** `importMaterials` is no longer `@Transactional`; it
loops and calls `self.importSingleRow(row, result)` through a `@Lazy` self-reference so the proxy
applies `@Transactional(propagation = REQUIRES_NEW)` on `importSingleRow`. Each row commits in its
own transaction; a row that throws rolls back only itself, the orchestrator catches it, logs the
material id + reason, and continues. A failing row can no longer mark the batch rollback-only — 562
good rows commit, 3 bad rows are logged. (Chose per-row REQUIRES_NEW over batch-error-collection
because, in JPA, one exception inside a shared transaction marks it rollback-only and poisons the
commit regardless of catching — collection cannot save the good rows; separate transactions can.)

**STEP 4 — partial success in history.** Added `StockImportHistory.STATUS_PARTIAL`. The watcher now
records: `failures==0` → **SUCCESS**; some persisted + some failed → **PARTIAL** (rows_updated =
persisted count, message `"N updated, M failed: …"`); nothing persisted → **FAILED**. A file with
SUCCESS **or PARTIAL** counts as processed and is skipped on re-run (`alreadyProcessed()`), so it's
not blindly re-imported, while the failures stay visible in `tbl_stock_import_history`. FAILED
(nothing persisted, e.g. unreadable file) still retries on the next event/restart.

**Build:** backend `mvn compile` clean. Backend-only change — no frontend build, bundle unchanged.

**Verify after deploy:** re-drop the failing `Material(<date>).CSV`; expect
`SAP import PARTIAL: file=…, rowsUpdated=562, rowsFailed=3` (or SUCCESS if the material-code dupes are
also cleaned), a WARN per duplicate triple (if any remain), and 562 refreshed
`tbl_map_company_plant_material` quantities that persist (no rollback). Duplicate material codes /
duplicate mapping rows remain a **separate data-cleanup task**.

---

### Event-Driven SAP Stock Import — WatchService, filename-tracked idempotency, authoritative table

Replaced the broken scheduled SAP stock import (wrong path, wrong table) with an event-driven
filesystem watcher that writes the authoritative stock table.

**PART 5 — import now writes the authoritative table (the core fix).**
`MaterialImportService.updateCompanyPlantMaterialMapping()`:
- **Before:** upserted `CompanyPlantMaterial` → **`tbl_pz_map_company_plant_material`** (which nothing
  reads for stock) via `CompanyPlantMaterialRepository`.
- **After:** upserts `CompanyPlantMaterialMap` → **`tbl_map_company_plant_material`** (`map_quantity_stores`)
  via `CompanyPlantMaterialMapRepository.findByCompanyAndPlantAndMaterial(...)` — match on
  company+plant+material, UPDATE `quantity` (absolute overwrite) if present else INSERT with the
  company/plant/material entity refs. This is the same table the dropdown, Inventory view and
  issue-note stock check read.

**PART 1 — `SapMaterialFileWatcher` (`integration/sap/watcher`), replaces the cron job.**
- `@EventListener(ApplicationReadyEvent)` starts a **daemon thread** running a Java `WatchService`
  registered on `sap.csv.import.path` (default **`/home/issuenote/issue`**, local XFS) for
  `ENTRY_CREATE` + `ENTRY_MODIFY`. `@PreDestroy` closes it.
- **Strict filename filter:** regex `^Material\(\d{4}-\d{2}-\d{2}\)\.CSV$` — matches ONLY
  `Material(YYYY-MM-DD).CSV`; every other file in the directory (Plant_Indent…, Quality_Info…,
  PRD-*.CSV) is ignored (DEBUG-logged).
- The old `SapMaterialImportJob`'s three `@Scheduled` triggers (10:21/10:43/11:55) were **disabled**
  (annotations removed, code retained + deprecation note); the `@Scheduled` import was dropped.

**PART 2 — stability check.** Before importing, `waitUntilStable()` samples `file.length()`,
sleeps 3 s, re-samples; proceeds only when size is unchanged (and > 0), capped at 10 retries. A file
still growing after the cap is left for the next event/restart (not recorded) — avoids importing a
half-copied file.

**PART 3 — idempotency (migration V55).** New `tbl_stock_import_history` (`file_name` UNIQUE,
`imported_at`, `rows_updated`, `status`, `message`) + `StockImportHistory` entity +
`StockImportHistoryRepository`. Before importing: skip if `existsByFileNameAndStatus(name,'SUCCESS')`.
After: `recordResult()` **upserts by filename** (so a retried FAILED row updates in place rather than
violating the UNIQUE constraint) with SUCCESS+rows or FAILED+message. A FAILED file is thus retried
on the next event/restart rather than being marked done.

**PART 4 — startup scan (latest-only).** On `ApplicationReadyEvent`, before starting the watch, it
lists matching files and imports **only the newest** (max filename; the zero-padded ISO date makes
lexical order == chronological) **if not already SUCCESS**. Rationale: each import is an **absolute
overwrite** of stock, so the most recent file is current truth and supersedes older unprocessed ones
— importing only the latest is sufficient, correct, and avoids applying a stale snapshot over a
newer one. (Known minor edge: a *back-dated* file dropped after a newer import would still be
imported by the live watcher; real files arrive current — flagged, not guarded, to keep logic simple.)

**PART 6 — last-import marker.** No new table — `StockImportHistoryRepository.findLastSuccessfulImportAt()`
= `MAX(imported_at) WHERE status='SUCCESS'`. That is the "last stock import" anchor the upcoming
reconciliation feature needs, for free.

**PART 7 — logging.** SUCCESS → INFO (`file`, `rowsUpdated`, timestamp); already-processed → DEBUG;
failure → ERROR with reason; non-Material events → DEBUG; still-changing file → WARN.

**Config:** added `sap.csv.import.path` (default `/home/issuenote/issue`) and `sap.csv.watch.enabled`
(default true) to `application.yml`, env-overridable (`SAP_CSV_IMPORT_PATH`, `SAP_CSV_WATCH_ENABLED`).

**Build:** backend `mvn compile` clean; `tsc -b && vite build` clean. Migration **V55**. Backend-only —
frontend bundle unchanged (`index-Dvex2nF9.js`).

**Verify after deploy:** drop a real `Material(<today>).CSV` into `/home/issuenote/issue`; the log
should show `SAP import SUCCESS: file=…, rowsUpdated=N`; confirm `tbl_map_company_plant_material`
quantities refreshed and a row appears in `tbl_stock_import_history`. Ensure the app user can read
that directory.

---

## 2026-07-09

### Issue-Note Stock Check Reads Real Stock + Read-Only Inventory Module

**PART 1 — issue-note confirmation "Insufficient stock" bug.**
`IssueNoteService.issueGoods()` validated stock via `inventoryService.isSufficientStock()` →
`getAvailableStock()` → `inventoryRepository.findByMaterialIdAndPlantId` → **`tbl_inventory_balance`,
which is EMPTY in production** → always 0 → every issue failed "Insufficient stock … Available: 0".
- **Before:** available read from `tbl_inventory_balance` (empty).
- **After:** available read from `tbl_map_company_plant_material.map_quantity_stores` via
  `companyPlantMaterialMapRepository.sumQuantityByMaterial(materialId)` — the SAME source and
  aggregation the material dropdown / issue-note creation use (`SUM(map_quantity_stores)` per
  material across company/plant, status 0/1). Matched on **material id only** — robust to the
  now-optional (nullable) issue-note plant/company, and consistent with how creation reads stock.
  Material 227 now sees 466, not 0.
- **Decrement (Step 4) — reported + decision flagged, NOT implemented.** The old path also called
  `inventoryService.deductStock()`, which reads/writes `tbl_inventory_balance` and
  `orElseThrow(InsufficientStockException("No inventory found …"))` when the row is absent — i.e.
  once the validation was fixed it would have become the *next* blocker. Since physical inventory
  is managed in **SAP** and `map_quantity_stores` decrement-on-issue is a pending business decision,
  the `deductStock` call was **removed** from the issue path: issuing now validates against real
  stock and records the issue **without decrementing** any stock. No decrement to `map_quantity_stores`
  was implemented. **Decision needed:** should issuing decrement `map_quantity_stores`, or is stock
  authoritative in SAP and the app read-only for stock? Flagged here for the business owner.

**PART 2 — read-only Inventory module (built new alongside the existing balance pages).**
- **Existing state:** `InventoryController` (`/api/v1/inventory`) + `InventoryListPage` read the empty
  `tbl_inventory_balance` and are scoped to management roles (FLOORINCHARGE/GOODSINCHARGE/…); the
  sidebar Inventory item was `future: true` (hidden). Those balance/adjust/history pages were **left
  intact** (separate management concern).
- **Backend (new, read-only):** `GET /api/v1/inventory/stock-view?search=&page=&size=` →
  `InventoryService.getStockView()` returns a `Page<MaterialDropdownResponse>` built from the proven
  `searchForDropdownAllCompanies(search)` query (real `map_quantity_stores`, LEFT JOIN from Material
  so zero-stock materials still appear, one row per material+company+plant), paginated in memory
  (catalogue is bounded; avoids a fragile paginated GROUP BY + count). `@PreAuthorize` =
  USER/SUPERVISOR/DEPTHEAD/ADMIN/SUPERADMIN/PROCUREMENT. No POST/PUT/DELETE added.
- **Frontend (new):** `InventoryStockPage` at `/inventory` (index) — search + paginated table
  (Code, Name, Description, Company, Plant, Available Stock), no create/edit/delete. Reuses
  `MaterialDropdownItem` shape via `inventoryApi.stockView`. The existing adjust/history/movements
  sub-routes keep their management-role guards.
- **Sidebar:** Inventory item de-`future`d; roles set to the six operational roles.
- **Migration V54** (next after V53): activates the seeded-hidden module —
  `module_status=1, module_is_future=0, module_default_roles='USER,SUPERVISOR,DEPTHEAD,ADMIN,SUPERADMIN,PROCUREMENT'`
  for `module_code='INVENTORY'`.

> **Migration number note:** the task guessed "likely V53", but V53 was already used (Confirmations
> restriction, same day) — so this is **V54**.

**Build:** backend `mvn compile` clean; `tsc -b && vite build` clean. Migration **V54**. New bundle
hash **`index-Dvex2nF9.js`**.

---

### Restrict Confirmations Module to ADMIN/SUPERADMIN (placeholder tied to dormant Plant Indent)

Confirmations is a placeholder view over Issue Notes + GRN; its one real action ("goods issued")
already exists on the Issue Note detail page, and it's functionally tied to the now-dormant Plant
Indent flow. Locked to ADMIN/SUPERADMIN at every layer — **no code deleted**, re-enablable by
widening the role lists (same treatment as Plant Indent in V52).

**PART 1 — Sidebar (`Sidebar.tsx`).** Confirmations nav item:
- Before: `['SUPERADMIN','ADMIN','ISSUECONFIRM','RECEIPTCONFIRM','DEPTHEAD','USER','SUPERVISOR']`
- After: `['ADMIN','SUPERADMIN']`

**PART 2 — Routes (`router.tsx`).**

| Route | Before | After |
|---|---|---|
| `confirmations` (index) | plain `<Navigate to="/confirmations/issue">` (no guard) | unchanged — forwards to the now-restricted `issue` route, so a non-admin is redirected then blocked |
| `confirmations/issue` | `['SUPERADMIN','ADMIN','ISSUECONFIRM','RECEIPTCONFIRM','DEPTHEAD','SUPERVISOR']` | `['ADMIN','SUPERADMIN']` |
| `confirmations/receipt` | `['SUPERADMIN','ADMIN','ISSUECONFIRM','RECEIPTCONFIRM','DEPTHEAD','USER','SUPERVISOR']` | `['ADMIN','SUPERADMIN']` |

**PART 3 — Backend.** Confirmed there is **no** `ConfirmationController`/service/entity
(`find -iname "*confirmation*"` → none; no `/api/v1/confirmation*` mapping). The pages call the
existing `issueNotesApi` and `grnApi`, whose endpoint authorizations serve other working features
and were **left untouched**. Nothing to restrict on the backend.

**PART 4 — Module access (migration V53, next after V52).**
`UPDATE tbl_module_master SET module_default_roles = 'ADMIN,SUPERADMIN' WHERE module_code = 'CONFIRMATIONS'`.

> Same `module_default_roles` format caveat noted for V52 applies: value uses role codes
> (`ADMIN,SUPERADMIN`) while V46 wrote display names. Real enforcement is the route guards +
> sidebar `roles` (normalized codes), so the restriction holds regardless.

**Build:** backend `mvn compile` clean; `tsc -b && vite build` clean. Migration **V53**. New bundle
hash **`index-DUpeefxL.js`**.

---

### LDAP path never reached — login() entry/branch tracing (ordering was already correct)

Zero `LDAP-DIAG` lines appear after an `@`-login, so `LdapAuthService.authenticate()` seemed never
called. **Important finding on reading the actual code:** the local password check is ALREADY inside
the `else` of `if (username.contains("@"))` — it does **not** run for `@` users. So the STEP-4
hypothesis ("local check runs before/regardless of the branch") does **not** match the current code;
the branch ordering is already correct and was **not** changed. Fabricating a reorder would have
masked the real cause.

**Actual current `login()` order (unchanged):** trim username → (guard: blank) → load employee by
`emp_email` (throws `InvalidCredentialsException` HERE if not found) → active check → **branch**:
`@` → `ldapAuthService.authenticate()`; else → local `verifyPassword`. → roles/JWT/response.

**Why no `LDAP-DIAG` lines — two candidates the new logs will disambiguate:**
1. **Employee lookup fails before the branch** (most likely): `findByEmailIgnoreCase('janakirama.c@ashaagrisciences.com')`
   returns empty → throws at the `orElseThrow` (~7 ms, no network) — the branch is never reached.
   This happens if no `tbl_emp_master` row has that exact `emp_email`.
2. **`authenticate()` returned at its pre-`step1` guard** (empty password) — previously produced no
   log at all because the guard `return false` came before any logging.

**Diagnostics added (WARN, no passwords):**
- `login()`: `LDAP-DIAG login() ENTRY` (username, containsAt, passwordPresent); a `NO employee row`
  line inside the `orElseThrow`; an `employee loaded empNo=… active=…` line; a `pre-branch` line; and
  `entering LDAP branch` as the first line of the `@` branch.
- `authenticate()`: an `ENTRY` line **before** the guard, plus a `guard rejected` line — so an
  empty-password/no-`@` early return is now visible instead of silent.

**How to read the next attempt's log (grep `LDAP-DIAG`):**
- `ENTRY` then `NO employee row` → cause #1: the email isn't in `tbl_emp_master.emp_email` (verify the
  stored value for emp 4 exactly matches the login email).
- `ENTRY` → `employee loaded` → `pre-branch` → `entering LDAP branch` → `authenticate() ENTRY` →
  `guard rejected` → the login password isn't reaching the server (frontend field / DTO).
- `entering LDAP branch` → `authenticate() ENTRY` → `step1…step6` → shows the real LDAP stage
  (bind/search/user-bind) as built in the prior entry.

**Ordering fix:** none required — current ordering already matches the task's "correct flow" (load
employee → fail if missing/inactive → branch, local check only in the non-`@` path).

**Build:** backend `mvn compile` clean; `tsc -b && vite build` clean. Backend-only — frontend bundle
unchanged (`index-Xk4l49XY.js`). Once the cause is confirmed and fixed, drop the `LDAP-DIAG` WARN
lines to DEBUG.

---

### LDAP Login Diagnostics — stage logging + config_princ DN whitespace normalization

`janakirama.c@ashaagrisciences.com` fails with the generic "Invalid username or password", but the
`ldapsearch` CLI with the same user + service account succeeds — so the server/bind/search/user-bind
all work; something in the Java path differs. Added server-side-only diagnostics (client still gets
the generic error) and applied the most likely fix.

**Stage logging (WARN level, so it shows without changing log config; no passwords ever logged):**
`LdapAuthService.authenticate()` now logs, in order:
- step1: email → extracted domain
- step2: whether a config row was found (logs `config.id` + url, never pwd) / blank-N-A / incomplete
- step3: the constructed service-bind DN + url + `auth=simple`
- step4: service-bind succeeded
- step5: whether the `(mail=…)` search returned an entry, and the DN found (or NO entry)
- step6: user-bind succeeded → authenticated
- FAIL branches log `e.getClass().getName()` + `e.getMessage()` for both `AuthenticationException`
  (bind) and `NamingException` (communication/DN-parse/timeout), tagged `LDAP-DIAG FAIL`.

**DN whitespace normalization (applied — strong suspected root cause):** `config_princ` rows carry
spaces after commas, e.g. `ou=people, dc=ashaagrisciences, dc=com`. `ldapsearch` tolerates/normalizes
these; JNDI's RFC-2253 DN parser can reject or misparse them. Added
`normalizeDn(dn) = dn.replaceAll(",\\s+", ",").trim()`, applied to **both** the service-bind DN and
the `(mail=…)` search base. The service-bind `config_user` is also trimmed. A WARN line logs the
before/after when normalization changes the value, so the log confirms whether this was the issue.

Diagnostic-only otherwise: no change to routing, to what the API returns, or to the local login path.
The step logs will show definitively whether the failure was service-bind, search-returns-nothing, or
a DN-parse `NamingException` — if normalization already fixed it, step6 will log success.

**Build:** backend `mvn compile` clean; `tsc -b && vite build` clean. Backend-only change — frontend
bundle unchanged (`index-Xk4l49XY.js`).

**Next:** reproduce the login, then read `app.log` for the `LDAP-DIAG` lines to confirm the stage and
exception. Once confirmed working, the WARN-level `LDAP-DIAG` lines should be dropped to DEBUG.

---

### LDAP Authentication — 18 domains, plain JNDI, wired into login (local login unchanged)

Built LDAP (directory) authentication for `@`-containing logins. Local (non-`@`) login is
**completely untouched** — LDAP is a new branch only.

**Tested facts this is built on (verified against the live servers — do not deviate):**
- Per-domain config in `tbl_ldap_config` (`config_url`, `config_user`, `config_pwd`, `config_princ`).
- Servers are **Zimbra OpenLDAP (inetOrgPerson), NOT Active Directory**.
- User DN: `uid=<email-localpart>,<config_princ>` (e.g. `uid=janakirama.c,ou=people,dc=ashaagrisciences,dc=com`).
- Service-bind DN: `uid=<config_user>,<config_princ>` (uid=, confirmed working).
- Users searchable by `mail=`; uid = email local-part.
- Flow: service-bind → search `(mail=<username>)` → get user DN → bind as user DN with typed
  password → success = authenticated.

**PART 1 — dependency:** none added. `javax.naming` (JNDI, `com.sun.jndi.ldap.LdapCtxFactory`) is
JDK built-in. Spring LDAP intentionally NOT added (confirmed absent in pom).

**PART 2 — `auth.ldap.LdapConfig` + `LdapConfigRepository`:** read-only entity over
`tbl_ldap_config` (id, domine, url, user, pwd, princ; lmd/lmu ignored). Repo exposes
`findByDomine(String)`.

**PART 3 — `auth.ldap.LdapAuthService.authenticate(email, password)`:**
- **Service-bind DN:** `"uid=" + config.getUser() + "," + config.getPrinc()`.
- **User-bind DN:** the absolute DN returned by the `(mail=<email>)` subtree search
  (`SearchResult.getNameInNamespace()`) — not hand-built — then a second `InitialDirContext` binds
  as that DN with the typed password.
- Env: `simple` auth, `com.sun.jndi.ldap.connect.timeout=5000` + `read.timeout=5000` so a bad
  server can never hang login; both contexts closed in `finally`.
- **Malformed domain-6 (`barracudanslgroup@nslgroup.in`):** `lookupConfig` tries exact
  `findByDomine` first; on miss, scans all rows and matches on the part **after `@`** in the stored
  `config_domine`, so a malformed value is still reachable. If still no match → clean failure.
- **`config_pwd = 'N/A'` / blank:** detected before any bind — returns failure
  ("LDAP not configured for this domain"), never binds with the literal "N/A". Incomplete
  url/user/princ rows also fail cleanly.
- Returns a plain `boolean`. Every failure mode (no config, N/A, user-not-found, wrong password,
  unreachable/timeout) returns `false` — indistinguishable to the caller; only server-side
  debug/warn logs carry detail. No stack trace or credential leaves the method.

**PART 4 — `AuthService.login()` routing:** at the password-verification step,
`if (username.contains("@"))` → `ldapAuthService.authenticate(...)`; else → the **unchanged** local
`passwordService.verifyPassword` path (verbatim, moved into the `else`). The employee is loaded by
`emp_email` and active-checked *before* this branch, so an LDAP user with no `tbl_emp_master` row
fails at lookup with the same generic `InvalidCredentialsException`. On LDAP success the SAME JWT is
issued via the identical downstream code (roles, `RoleNormalizer`, companyIds, `AuthenticatedUser`)
— LDAP only verifies the password; the employee record supplies all authorization.

**PART 5 — safety:** no-employee-row → generic fail; timeouts prevent hangs; each domain is
independent (a failed/unreachable domain returns false for that login only — other domains and
local login are unaffected); API surfaces only "invalid credentials", no detail.

**Build:** backend `mvn compile` clean; `tsc -b && vite build` clean. **No frontend change** —
LDAP is backend-only, so the bundle hash is unchanged (`index-Xk4l49XY.js`); the login form already
posts username/password.

**Business-owner verification after deploy:** log in with an `@`-email whose domain has a valid
(non-N/A) `tbl_ldap_config` row **and** a matching `tbl_emp_master.emp_email`; confirm local
(non-`@`) logins still work unchanged. Ensure the app server (172.16.9.158) can reach each
`config_url` host:port (see `docs/ldap-investigation.md` section 5).

---

### Restrict Plant Indent Module to ADMIN/SUPERADMIN (not in active use)

Plant Indent is not currently used. Locked down to ADMIN/SUPERADMIN at every layer — **no code
deleted**, so it can be re-enabled later by widening the role lists.

**PART 1 — Sidebar (`Sidebar.tsx`).** The Plant Indent nav item had **no `roles` array** (visible
to anyone with module access, gated only by `moduleCode: 'PLANT_INDENTS'`). Added
`roles: ['ADMIN', 'SUPERADMIN']`.

**PART 2 — Routes (`router.tsx`).** All four Plant Indent routes tightened:

| Route | Before | After |
|---|---|---|
| `plant-indent` (index) | `['SUPERADMIN','ADMIN','PLANTMANAGER','FLOORINCHARGE','SUPERVISOR']` | `['ADMIN','SUPERADMIN']` |
| `plant-indent/new` | `['SUPERADMIN','ADMIN','PLANTMANAGER','SUPERVISOR']` | `['ADMIN','SUPERADMIN']` |
| `plant-indent/:id` | `['SUPERADMIN','ADMIN','PLANTMANAGER','FLOORINCHARGE','SUPERVISOR']` | `['ADMIN','SUPERADMIN']` |
| `plant-indent/:id/edit` | `['SUPERADMIN','ADMIN','PLANTMANAGER','SUPERVISOR']` | `['ADMIN','SUPERADMIN']` |

**PART 3 — Backend (`PlantIndentController.java`) — the real enforcement.** All **24**
`@PreAuthorize` annotations were normalized to `hasAnyRole('SUPERADMIN', 'ADMIN')`. Previous values
varied widely and included PLANTMANAGER, FLOORINCHARGE, USER, VIEWER, SUPERVISOR, QUALITYMANAGER,
DEPTHEAD across CRUD, workflow (submit / deo-approve / manager-approve / start-processing /
complete / resubmit) and queue/dashboard endpoints. Every one is now ADMIN/SUPERADMIN only.

**PART 4 — Module access (migration V52, next after V51).**
`UPDATE tbl_module_master SET module_default_roles = 'ADMIN,SUPERADMIN' WHERE module_code = 'PLANT_INDENTS'`.
(Confirmed table/column names against V43/V46: `tbl_module_master.module_default_roles`,
`module_code`.)

> **Note on `module_default_roles` format:** used the task's literal `'ADMIN,SUPERADMIN'` (role
> codes). An earlier migration (V46) wrote display names (`'Super Admin,Admin,ROLE_VIEWER'`) into
> this same column, so the format the module-access check expects is inconsistent in history. Real
> enforcement here is the backend `@PreAuthorize` + route guards + sidebar `roles` (all use the
> normalized `ADMIN`/`SUPERADMIN` codes), so the module restriction holds regardless; if the
> business owner finds the module still seeds for other roles, this value may need to be
> `'Super Admin,Admin'` to match V46's convention.

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. Migration **V52**. New bundle hash
**`index-Xk4l49XY.js`**.

---

## 2026-07-06

### Stores Buttons Visible to PROCUREMENT + "Created By" Column for Elevated Roles

**PART 1 — stores Goods Issued / Reject (Stores) buttons not showing.** The status check was
correct and read the unwrapped fields fine; the bug was the **role list**. Before/after:

```
// before
const canIssue = approvedStatusId === 3 && storesByStatusId === 1
  && hasAnyRole(['SUPERADMIN', 'ADMIN', 'ISSUECONFIRM']);
// after
const canIssue = approvedStatusId === 3 && storesByStatusId === 1
  && hasAnyRole(['SUPERADMIN', 'ADMIN', 'ISSUECONFIRM', 'PROCUREMENT']);
```

PROCUREMENT fills the stores role in this deployment but was absent from the check, so the
stores/procurement user saw the note with no action buttons. Also added PROCUREMENT to the matching
backend `@PreAuthorize` on `POST /issue-notes/{id}/issue` and `/reject-stores` — otherwise the
now-visible buttons would 403 on click. Endpoints confirmed wired: Green "Goods Issued" →
`issueNotesApi.issue` → `/issue` → storesByStatus=11; Red "Reject (Stores)" →
`issueNotesApi.storesReject` → `/reject-stores` (with reason) → storesByStatus=2. Both frontend
methods already unwrap the `{success,data}` envelope (returning `response.data.data`).

**PART 2 — "Created By" column, elevated roles only.** The list DTO (`IssueNoteSummaryResponse`)
**did not** carry the creator name — it had neither `createdBy` nor a resolved name. Added
`createdBy` + `employeeName` to the DTO and resolved the name in `mapToSummaryResponse` via the same
`resolveEmployeeName(createdBy)` the detail response uses. Frontend: added a "Created By" column to
`IssueNotesListPage`, rendered only when
`hasAnyRole(['SUPERVISOR','DEPTHEAD','PROCUREMENT','ISSUECONFIRM','ADMIN','SUPERADMIN'])` (spread into
the columns array so it's fully absent for a plain USER, who only ever sees their own notes). Null
creator renders as "—".

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. New bundle hash **`index-HSNw8zic.js`**.

---

### Company/Plant/Section/Location Fully Optional — Never Block Creation + Plant≠Location Resolved

Company, plant, section, location and department are optional, informational employee metadata.
Missing any of them must never prevent creating an indent or issue note.

**PART 1 — migration V51 (next number after V50).** Made the remaining NOT NULL geo columns
nullable (V50 already did plant/section):

| Table | Column | Was | Now |
|---|---|---|---|
| tbl_issue_note | issue_note_company | NOT NULL | NULL |
| tbl_issue_note | issue_note_dept | NOT NULL | NULL |
| tbl_indent_master | indent_company | NOT NULL (legacy) | NULL |
| tbl_indent_master | indent_dept | NOT NULL (legacy) | NULL |

Full geo picture across both tables after V50+V51: company, department, plant, section all nullable.
`createdby`, document number, date, status and the audit `lmd/lmu` columns stay NOT NULL (genuinely
required / server-set) — not geo, correctly left alone.

**PART 2 — constraints removed.** `IssueNote` entity: dropped `nullable=false` from
`issue_note_company`, `issue_note_dept`, and a stale `issue_note_plant` (missed in V50's code side).
Indent uses `@JoinColumn` (no entity-level NOT NULL — nothing to change). Request DTOs already had
`@NotNull` removed from company/department/plant/section (and indent employeeId) in the prior commit;
TS request types already optional.

**PART 3 — resilient server-side capture.** `createIssueNote` and `createIndent` now:
company ← first `tbl_map_company_emp` row wrapped in try/catch (missing/error → null, logged, never
throws); department ← `emp_department` else null; plant/section ← null unless a client explicitly
supplies one. Each still honours an explicit request value as a fallback. Creation proceeds no matter
how much employee metadata is absent.

**PART 5 — plant vs location: DISTINCT masters.** `tbl_plant_master` and `tbl_location_master` are
separate tables, and `Employee` carries both `emp_location` (Integer location id) and `emp_plant`
(String name) as distinct fields — so plant ≠ location. **Reverted** the prior commit's
`plant ← emp_location` assignment in both services: writing a location id into a plant column would
have made the detail page's plant-name lookup resolve the wrong master. `*_plant` now stays null
(no employee→plant-id source exists). `emp_location` has no target column on either entity, so
location is not stored. (Business owner can still confirm with the row-count queries, but the code
evidence is conclusive.)

**PART 4 — null geo renders as "—".** Both detail pages now show a clean em-dash for null
company/department/plant/section instead of "N/A". The backend name lookups were already null-safe
(`getXxxId() != null ? repo.findById(...) : null`), so a null id never hits the repository.

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. Migration **V51**. New bundle hash
**`index-BF1ljrKI.js`**.

---

## 2026-07-04

### Issue Note Creation 500 (audit column) + Legacy Number Sequence + RM Approver + Card Removal

**PART 1 — creation 500 `Unknown column 'log_created_date'`.** The issue note was created, but the
post-create email notification saved an `EmailLog` (`tbl_email_log`) whose entity maps
`log_created_date` — a column missing from the deployed table. `EmailService.sendEmailFromTemplate`
was `@Transactional` with default propagation, so it **joined** `createIssueNote`'s transaction; the
failed INSERT marked it rollback-only and reverted the issue note even though the caller caught the
exception. Two fixes: (1) both `sendEmailFromTemplate` and `sendEmail` are now
`@Transactional(propagation = REQUIRES_NEW)` — email/audit logging runs in its own transaction and a
failure can never roll back a business operation again; (2) migration **V49** adds the missing
`log_created_date DATETIME NULL` column so logging actually works. Audit entity: `EmailLog` →
`tbl_email_log`; mismatch was the single missing `log_created_date` column.

**PART 1 Step 4 — issue note number format.** Generator produced `IN/2026/00001`; the real legacy
sequence is a continuous 13-digit numeric (…988, …989, …). Indent generation
(`generateLegacyNumericIndentNumber`) takes the latest `indent_no` (ORDER BY id DESC), `parseLong+1`,
with a MAX-numeric fallback for stray non-numeric rows. Rewrote `generateIssueNoteNumber` to mirror it
exactly — added `findLatestIssueNoteNumbers` + `findMaxNumericIssueNoteNumber` (native
`MAX(CAST(... AS UNSIGNED)) WHERE issue_note_no REGEXP '^[0-9]+$'`) to `IssueNoteRepository`. The
MAX-numeric fallback deliberately ignores the stray `IN/2026/00001` so the sequence resumes from the
real max. Both the `/meta` preview (`previewNextIssueNoteNumber`) and creation call
`generateIssueNoteNumber`, so both are consistent.

**PART 5 — RM approver name not showing.** The previous commit resolved the name from
`issue_note_rm_approvedby` only. Legacy flow was always User→RM→Stores with no manager stage, so
legacy rows recorded the RM in `issue_note_approvedby`, while new-app `rmApprove()` writes
`issue_note_rm_approvedby`. Fixed by coalescing in `mapToResponse`: RM approver id/date =
`rmApprovedBy`/`rmApprovedByDate` if present, else `approvedBy`/`approvedByDate`. Frontend already
reads `rmApprovedByName`/`rmApprovedByDate`; the RM Review stage shows "Approved by {name} on {date}".

**PART 6 — remove creation cards, capture data server-side, make plant/section nullable.**
Confirmed available employee data: `emp_department` (`tbl_emp_master`), `emp_location`
(`tbl_emp_master`), company via `tbl_map_company_emp`. NOT available: section, and no employee→plant-id.
- **Backend:** `createIssueNote` and `createIndent` now resolve company (first of
  `companyEmployeeRepository.findCompanyIdsByEmpNumber`), department (`emp_department`), and plant
  (`emp_location` — best available proxy) server-side, each **falling back to the request value** if
  the employee record can't supply it (older clients keep working). Indent employee = creator when the
  form omits it.
- **Migration V50** makes `issue_note_plant`, `issue_note_sec`, `indent_plant`, `indent_sec` nullable
  (no reliable plant/section source, so creation must tolerate NULL).
- **DTOs:** `@NotNull` removed from company/department/plant (and indent employeeId); TS request types
  made these optional.
- **Frontend:** removed the "Basic Information" card (indent) and "Issue Details" card (issue note)
  entirely — with their company/plant/department/section dropdowns, related master-data queries,
  auto-select effects, and the zod `min(1)` validators. Payloads send these only as an optional
  fallback (never `0`). Added a minimal read-only header line to both creation forms:
  `Indent/Issue Note No: {preview} · Date: {today} · FY: {year}`.

> **Caveat flagged for the business owner:** `plant` is populated from the employee's `emp_location`
> (a location id), because no employee→plant-id mapping exists. If the plant and location masters are
> distinct, the stored `*_plant` value is really a location id and the detail page's plant-name lookup
> may show the wrong name or blank. Confirm whether location==plant in this deployment; if not, we
> should add a dedicated location column rather than reuse plant. Company creation still requires a
> `tbl_map_company_emp` row for every creator (company columns remain NOT NULL) — verify completeness.

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. Migrations added: **V49** (email log
column), **V50** (plant/section nullable). New bundle hash **`index-BN8cCzXC.js`**.

---

### Issue Note Creation Blockers + Material Dropdown Catalogue + Dropdown Scroll + Detail Gaps

**PART 1 — `issue_note_lmd` data-truncation (blocker) + all varchar date columns.**
Root cause: the legacy `tbl_issue_note` / `tbl_issue_note_details` audit-date columns are
`varchar(20)`, but the entities map them as `LocalDateTime`. Binding a `LocalDateTime` sends a
value with fractional seconds (`2026-07-04 07:25:17.738893`, 26+ chars) → MySQL truncation on
INSERT. (Indents are unaffected — their `indent_*` date columns are real `datetime`, proven by
indents creating fine with the identical mapping.) Fix: new
`common/persistence/VarcharDateTimeConverter` (JPA `AttributeConverter<LocalDateTime,String>`)
writing a fixed 19-char `yyyy-MM-dd HH:mm:ss` and reading tolerantly. Applied via `@Convert` to
the audit-date fields **not** used in JPQL range/sort:

| Column | Width | Code wrote (before) | Chars | After |
|---|---|---|---|---|
| `issue_note_lmd` | varchar(20) | `LocalDateTime.now()` → `2026-07-04 07:25:17.738893` | 26+ | `2026-07-04 07:25:17` (19) |
| `issue_note_details_lmd` | varchar(20) | same | 26+ | 19-char |
| `issue_note_storesby_date` | varchar(20) | same (on goods-issue) | 26+ | 19-char |
| `issue_note_approvedby_date` | varchar(20) | same | 26+ | 19-char |
| `issue_note_rm_approvedby_date` | varchar(20) | same (on RM approve) | 26+ | 19-char |
| `issue_note_year` | varchar(45) | `"2026-27"` | 7 | unchanged (fine) |
| `issue_note_created_date` | varchar(20) | never written (String field) | — | unchanged |
| `issue_note_date` | datetime | `LocalDateTime` | — | **left native** (used in `Sort`/`BETWEEN`) |
| `tbl_indent_master.*` dates | datetime | `LocalDateTime` | — | unchanged (real datetime, works) |

The 19-char format also inserts cleanly into a real `datetime` column, so the converter is safe
regardless of the exact column type. `issue_note_lmd` was only the first to fail; `details_lmd`
and `storesby_date` would have blocked the next steps — all fixed in one pass.

**PART 2 — material dropdown limited to ~30-40 for non-USER/ADMIN roles.**
`MaterialController /dropdown` set `adminView = ADMIN||SUPERADMIN` and passed the caller's
`companyIds`; non-admin callers hit `searchForDropdownByCompanies`, whose
`AND (co.id IS NULL OR co.id IN :companyIds)` filter dropped every material mapped only to other
companies. USER appeared full only because its `companyIds` is empty (fell through to the
all-companies branch); SUPERVISOR/DEPTHEAD/PROCUREMENT have mappings → scoped subset. Fix:
`searchMaterialsForDropdown(search)` now always calls `searchForDropdownAllCompanies` — the
catalogue is stock context, not a permission boundary. Controller simplified (dropped
`adminView`/`companyIds`), all authenticated roles get the full list.

**PART 3 — dropdown detached from input on scroll.**
The material search dropdown is `position: fixed` (so it escapes the card's `overflow`). Fixed
elements don't scroll with the page, so it floated away. Fix (applied to both `IndentFormPage`
and `IssueNoteFormPage` — they each have their own inline dropdown, no shared component): store a
ref to the anchor input; a `useEffect` gated on `showMaterialSearch` adds capture-phase `scroll`
+ `resize` listeners that recompute `{top,left,width}` from the input's live bounding rect, so the
dropdown tracks the input continuously and still overlays outside the card.

**PART 4 — issue note detail line items (qty/purpose/total).** No code change needed — the
table already renders material code/name, UOM, quantity, purpose and a total-quantity footer, and
`IssueNoteResponse.IssueNoteDetailResponse` already carries `quantity`/`purpose`. The fields were
blank only because the **response-envelope bug** (fixed in fd5de25) left `issueNote.details`
undefined; with the unwrap deployed they populate. Verified the table + DTO are correct.

**PART 5 — RM approver in flowchart.** Added `rmApprovedBy` / `rmApprovedByName` /
`rmApprovedByDate` to `IssueNoteResponse` (name resolved from `issue_note_rm_approvedby` via the
existing employee lookup). The RM Review stage now reads "Approved by {name} on {date}".

**PART 6 — remove "Issue Details" card & capture server-side: NOT DONE (reported, by design).**
The premise doesn't hold against the schema. `tbl_emp_master` has only `emp_department` (Integer)
and `emp_location` (Integer); there is **no `emp_company`, no `emp_section`, and `emp_plant` is a
`varchar(100)` name — not the Integer plant FK** that `issue_note_plant` (NOT NULL) requires.
Company is only derivable via the company↔employee mapping table (and defaults to the first of
possibly several); section has no employee source at all; plant has no integer source. Removing
the card would break creation (no plant id, possible null company/department). So the card was
**kept** (heading already renamed "Issue Details" in fd5de25) with its pre-filled, working
dropdowns. Recommend the business owner confirm `emp_*` completeness before any future removal;
even then, plant needs a resolvable employee→plant-id source that doesn't currently exist.

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. New bundle hash
**`index-CqjkQo48.js`** (was `index-D3NXf63A.js`).

---

### Issue Note Detail Response Envelope Unwrap + Create-Button Roles + Card Heading

**Root cause of "detail page shows N/A + flowchart shows default data" (looked like a stale
deploy, was not):** rebuilding the committed frontend reproduced the exact deployed bundle hash
(`index-BaXK1G_W.js`), proving the deployed frontend was current — no cache/deploy/duplicate-
component problem. The real bug: `GET /issue-notes/{id}` wraps its body as
`{ success: true, data: {...} }`, but `issueNotesApi.getById()` returned axios `response.data`
(the envelope) instead of `response.data.data` (the issue note). So the detail page read
`issueNote.employeeName` etc. as `undefined` → "N/A", and `issueNote.approvedStatus ?? 1`
defaulted to 1 → the 3-stage flowchart rendered its structure (new labels visible) but every
stage showed its first/pending state. This predates the enrichment work — the old page read
`requestedByName` off the same envelope and got the same blanks, which is why the enrichment
"didn't appear." The list page was unaffected because `GET /issue-notes` returns a raw `Page`.

**FIX 1 — envelope unwrap (`api/issueNotes.ts`):**

Audited every issue note endpoint. Envelope usage:

| Shape | Endpoints |
|---|---|
| `{ success, [message,] data }` (entity nested under `data`) | create, getById, getByNumber, submit, rm-approve, rm-reject, issue, reject-stores, cancel, return |
| Flat `Page<>` (no envelope) | GET `/issue-notes` (list) |
| `{ content, currentPage, totalItems, totalPages }` map | pending-rm-approval, pending-issue, by-department, my-issue-notes |
| `{ success:false, message }` (410 GONE, **no data**) | approve, reject (deprecated manager stage) |

Unwrapped to `response.data.data`: `getById`, `getByNumber`, `create`, `submit`, `issue`,
`return`, `cancel`, `storesReject`, `rmApprove`, `rmReject`. **`create` was also a latent bug** —
the form's "Save & Submit" reads `created.id` to chain the submit call; against the envelope
that was `undefined`, so the second step used a bad id. Left the two deprecated 410 endpoints
(`approve`/`reject`) flat — they carry no `data` and axios throws on 4xx before the return runs.
The `{content,...}` paginated maps are a different shape used by secondary queues and were left
as-is (out of scope; the primary list uses the flat `Page`).

**FIX 2 — create-button + endpoint + route-guard roles aligned** to
`USER, SUPERVISOR, DEPTHEAD, ADMIN, SUPERADMIN`:
- `IssueNotesListPage.tsx` create button: was `['ADMIN', 'ISSUECONFIRM']` (stores staff, wrong —
  they *issue* goods, they don't *raise* notes; USER/SUPERVISOR/DEPTHEAD saw no button at all).
- Backend `POST /issue-notes` `@PreAuthorize`: was `USER, ADMIN, SUPERADMIN, SUPERVISOR` → added
  DEPTHEAD. (ISSUECONFIRM was not present, so nothing to remove.)
- `router.tsx` `/issue-notes/new` **and** `/issue-notes/:id/edit` guards: were
  `SUPERADMIN, ADMIN, USER, SUPERVISOR` → added DEPTHEAD. All three now match exactly.

**FIX 3 — card heading** in `IssueNoteFormPage.tsx`: "Issue Note Information" → "Issue Details"
(the card now only wraps the Company/Plant/Department/Section dropdowns; the employee strip was
removed 2026-07-04 earlier).

**Note (not fixed, out of scope):** no `PUT`/`DELETE` endpoint exists on the controller, so the
frontend `update`/`delete` methods target nothing — the issue note *edit* flow is non-functional
server-side. Flagged for a future task.

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. **New bundle hash
`index-D3NXf63A.js`** (was `index-BaXK1G_W.js`) — confirms the frontend change compiled in.

---

### Issue Note Indent-Parity — Enrichment, Status Display, Flowchart, Visibility, Creation Card

Brings Issue Notes to the same standard as Indents. Flow: **User → RM → Stores** (no Dept
Head stage). Two status columns: `issue_note_approved_status` (1=pending, 2=rejected,
3=approved) × `issue_note_storesby_status` (1=pending, 2=rejected, 11=issued).

**PART 1 — IssueNoteResponse enrichment (prerequisite):**

The DTO returned only raw IDs; the detail page rendered `requestedByName`, `issueNumber`,
`createdAt`, `departmentName` etc. — fields that never existed on the payload, so the page
showed blanks/N-A. Added resolved names: `employeeName`/`employeeNumber` (from
`issue_note_createdby`), `companyName`, `departmentName`, `sectionName`, `plantName`,
`issuedByName` (from `issue_note_storesby`), `displayStatus`, plus `materialCode`/
`materialName`/`uomCode` on line items (the items table had the same blank-fields disease).

**Resolution pattern:** IndentService resolves names through JPA `@ManyToOne` entity
relationships (`indent.getCompany().getName()`). IssueNote stores raw FK integers with no
entity relationships, so the identical outcome is achieved via null-guarded repository
lookups (`companyRepository.findById(...).map(Company::getName)` etc.) in `mapToResponse()`.
Frontend types cleaned: stale aliases (`issueNumber`, `requestedByName`, `createdAt`,
`statusName`, `items`, `remarks`) removed and all consumers rewired to real field names.

**Write-path corruption fix (same disease as the indent l2Approve bug):**

`createIssueNote()` never set the two workflow columns (both NULL) and `rmApprove()`/
`rmReject()` set only the `issue_note_rm_status` audit column — never
`issue_note_approved_status`. Every new-app issue note therefore derived its display from
`(null, null)` and stayed "Pending RM Approval" forever, and the stores queue check
(`approved=3, stores=1`) could never fire for new notes. Fixed:
- `createIssueNote()` → `approvedStatus = 1` (or 3 with DEPTHEAD bypass), `storesByStatus = 1`
- `rmApprove()` → `approvedStatus = 3` (keeps rm_status audit write)
- `rmReject()` → `approvedStatus = 2`
- `issueGoods()` / `rejectByStores()` already set `storesByStatus` 11 / 2 correctly.
Legacy-migrated rows already carry correct values; only new-app rows created before this fix
(if any) may have NULL columns — they display as "Pending RM Approval" (safe default).

**PART 2 — deriveIssueNoteDisplayStatus():** function already existed and covered the
production combinations; label "Pending" renamed to "Pending RM Approval" and visibility
widened to package-private for `DeriveIssueNoteDisplayStatusTest` (new, 3/3 green):
(3,11)→Goods Issued ×2,213 · (3,2)→Stores Rejected ×45 · (2,1)→RM Rejected ×26 ·
(3,1)→RM Approved ×3 · (1,1)→Pending RM Approval ×2 · null-safe.

**PART 3 — Three-stage flowchart** (Submitted → RM Review → Stores) on the detail page,
driven purely by the two-column model, never the Spring status (legacy rows carry status=1
as an active flag). Submitted always green; RM ✓/✗/amber per approvedStatus; Stores
✓ (issued 11) / ✗ (rejected 2) / amber (pending 1, once RM approved). Rejection terminates
the flow; a level-specific rejection alert shows below.

**Detail-page action wiring fix (found during investigation):** the Approve/Reject buttons
called `issueNotesApi.approve`/`reject` → the deprecated manager-stage endpoints which now
return **410 GONE** — RM approval from the detail page was broken. Rewired to
`rmApprove`/`rmReject`, gated on `status=2 AND approvedStatus=1`, label "Approve (RM Review)".

**PART 4 — Role visibility:** already implemented in `getAll()` (USER own; SUPERVISOR own +
subordinates; DEPTHEAD/PLANTMANAGER department; ISSUECONFIRM/PROCUREMENT/ADMIN/SUPERADMIN
global) with `issueDate DESC` ordering, and the list page title was already dynamic. Added the
missing active-row filter to both filter queries. **Deviation from spec:** the task said
"always filter issue_note_status = 1" — implemented as `status <> 0` instead, because the new
app reuses the Spring column for workflow (2=submitted, 3=RM approved, 8=issued…); filtering
=1 would hide every in-flight new-app note. `0` is the cancel/soft-delete value in both eras.

**PART 5 — True-draft gating:** `createIssueNote()` sets Spring `status=1` (Draft; 3 with
DEPTHEAD bypass) and now `(1,1)` two-column state; `submitForApproval()` moves status to 2.
Gate: `isTrueDraft = status===1 && approvedStatus===1 && storesByStatus===1` on detail-page
Submit/Edit and the list-page Edit button (which previously compared integer `status` to the
string `'DRAFT'` — never rendered; same for the Return button vs `'ISSUED'`, now `status===8`).
Residual ambiguity: legacy `(1,1)` rows with status=1 are indistinguishable from new drafts
(2 rows in production) — same accepted trade-off as indents.

**PART 6 — Creation page:** removed the read-only employee strip (Employee, Financial Year,
Date, Issue Note No. preview) — this data is auto-captured at creation and now displays on the
detail page. Also removed the "Issued To" field (no legacy equivalent; investigation 2026-07-04);
backend `@NotBlank` on `issuedTo` relaxed to optional — **DB column retained**. The editable
Company/Plant/Department/Section dropdowns remain (legacy-faithful).

**PART 7 — Colors:** all five issue note labels already present and unique in
`INDENT_STATUS_COLORS`: Pending RM Approval=warning, RM Rejected=danger, RM Approved=info,
Stores Rejected=pink, Goods Issued=dark-green. List-page filter option renamed
"Pending" → "Pending RM Approval".

**PART 8 — Stores buttons:** "Goods Issued" / "Reject (Stores)" preserved with the two-column
gate (`approved=3 AND stores=1`, ISSUECONFIRM/ADMIN/SUPERADMIN) and confirmed against the
enriched response; the issue-confirmation modal now lists items by material code.

**Files changed:**
- `backend/.../issuenote/dto/IssueNoteResponse.java` — enriched (names + displayStatus + line-item codes)
- `backend/.../issuenote/dto/IssueNoteSummaryResponse.java` — + approvedStatus/storesByStatus
- `backend/.../issuenote/dto/CreateIssueNoteRequest.java` — issuedTo optional
- `backend/.../issuenote/IssueNoteService.java` — name resolution, write-path two-column fixes, label rename
- `backend/.../issuenote/IssueNoteRepository.java` — `status <> 0` active filter on both filter queries
- `backend/src/test/.../issuenote/DeriveIssueNoteDisplayStatusTest.java` — new, 3/3 green
- `frontend/src/api/issueNotes.ts` — types match enriched DTO; stale aliases removed
- `frontend/src/pages/issue-notes/IssueNoteDetailPage.tsx` — info card, flowchart, gating, RM wiring
- `frontend/src/pages/issue-notes/IssueNoteFormPage.tsx` — info strip + Issued To removed
- `frontend/src/pages/issue-notes/IssueNotesListPage.tsx` — filter label, edit/return button fixes
- `frontend/src/pages/issue-notes/IssueNoteApprovalPage.tsx` — rewired to real field names

**Build:** `mvn compile` clean; `mvn test -Dtest=DeriveIssueNoteDisplayStatusTest` 3/3 green;
`tsc -b && vite build` clean.

---

## 2026-07-03

### Procurement UI Fixes + Unique Status Colors + Approval Flowchart Correction

**FIX 1 — Approve/Reject buttons removed for procurement stage (`IndentDetailPage.tsx`):**

Previous `canApprove` had a third clause `(awaitingProcurement && hasAnyRole([SUPERADMIN, ADMIN, PROCUREMENT]))`
that showed "Procurement Approve"/"Reject" buttons once an indent reached the procurement stage.
Removed that clause — Approve/Reject now exist only for the two approval stages:
L1 (SUPERVISOR when `approvedStatus=1`) and L2 (DEPTHEAD/PLANTMANAGER when `approvedStatus=3, finalStatus=1`).
PROCUREMENT works exclusively through the Procurement Status Update card. The smart-route
`status=5 → procurementApproveIndent()` branch is now unreachable from the UI by design.

**FIX 2 — Procurement dropdown reflects saved sub-status on load:**

The dropdown was `useState(5)` — always reset to "Quotations Collected" on reload even when the
indent was already at PO Released. Added a `useEffect` that seeds the dropdown from
`indent.procurementStatusId` when it is a valid sub-status (5–9). Value 4 means "arrived at
procurement, no sub-status chosen yet" and keeps the default of 5.

**FIX 3 — Lock after terminal status (PO Released / Cash Buy):**

`isTerminalStatus = procurementStatusId === 7 || === 9`. When terminal, the editable card is
replaced by a read-only summary card: badge + "Procurement complete — PO# XXX, Delivery: DATE".
**Hold (8) is NOT terminal** — procurement can move a held indent to PO Released or Cash Buy later.
The backend (`updateProcurementStatus`) places no restriction on transitions out of any sub-status
(only requires `finalStatus=4`), and no legacy documentation indicates Hold was terminal, so this
matches both. Note: the lock is UI-level only; the backend still accepts updates after 7/9.

**FIX 4 — Unique color per status (`constants/indentStatus.ts` + `styles/main.scss`):**

Before: heavy collisions — `info` shared by 4 statuses, `success` by 5, `danger` by 4, `secondary` by 4.
After: every status in the regular indent workflow has a unique color. Approach chosen:
**custom `.badge.bg-*` CSS classes in main.scss** (purple, orange, teal, cyan, dark-green, maroon,
indigo, olive, pink, brown). Because react-bootstrap `<Badge bg={x}>` renders class `bg-{x}`,
custom suffixes work through the existing `INDENT_STATUS_COLORS` lookup with **zero consumer changes** —
Indent list, Indent detail, Plant Indent list, Issue Notes list, and IndentApprovalPage all pick the
new colors up automatically. Also added `.badge.bg-light { color: #212529 }` so the In Progress
fallback stays legible. Reports were the one gap: `IndentReportPage` (hardcoded `bg="secondary"`)
and `IssueNoteReportPage` (local `statusBadgeColor()` by numeric id) now route through
`INDENT_STATUS_COLORS` first.

Key assignments: Pending=warning, RM Approved=info, RM Rejected=danger, Dept. Head Approved=primary,
Dept. Head Rejected=maroon, Quotations Collected=secondary, Negotiation Done=purple, PO Released=success,
Hold=orange, Cash Buy=teal, Goods Receipt=cyan, Goods Issued=dark-green, Stores Rejected=pink,
In Progress=light. Reuse only where statuses can never co-occur in one view (plant-only 'Rejected'
vs regular-only 'RM Rejected'; 'Completed' vs 'PO Released').

**FIX 5 — Approval flowchart corrected (`IndentDetailPage.tsx`):**

Before: a 3-step timeline (Submitted → Dept Head Approval → Final Approval) driven entirely by the
legacy single `statusId` — wrong stage names and ignored the three-column model.
After: a 4-stage stepper driven by the three-column state:

| Stage | State source | Display |
|---|---|---|
| Submitted | `statusId >= 2` | ✓ done / grey |
| RM Review | `approvedStatus` 1=pending, 3=approved, 2=rejected | ✓ / ✗ / amber Pending + approver name/date |
| Dept Head Review | `finalStatus` 1=pending, 4=approved, 2=rejected (reachable only if RM approved) | ✓ / ✗ / amber Pending + approver name/date |
| Procurement | `procurementStatus` 4=arrived, 5–9=sub-stage (reachable only if DH approved) | sub-stage label; ✓ when 7/9 |

Rejection terminates the flow: RM rejected → Dept Head and Procurement render grey/unreached;
same for Dept Head rejection. Red ✗ icon (FaTimes) marks the rejected stage, and the rejection
alert below now says which level rejected ("Rejected by RM" / "Rejected by Dept Head").
Removed the now-unused `STATUS_PROCUREMENT_APPROVED` constant; added `PROC_SUB_LABELS` map.

**Files changed:**
- `frontend/src/pages/indents/IndentDetailPage.tsx` — FIX 1, 2, 3, 5
- `frontend/src/constants/indentStatus.ts` — FIX 4 color map
- `frontend/src/styles/main.scss` — FIX 4 custom badge classes
- `frontend/src/pages/reports/IndentReportPage.tsx` — FIX 4 reports consistency
- `frontend/src/pages/reports/IssueNoteReportPage.tsx` — FIX 4 reports consistency

**Build:** Backend `mvn compile` clean (no Java changes). Frontend `tsc -b && vite build` clean.

---

## 2026-07-02

### deriveDisplayStatus() Complete Matrix + Flowchart/Submit Decoupled from Spring Status

**Root cause:** `deriveDisplayStatus()` only matched a handful of exact triples; the 562
production PO Released indents `(3,4,7)` and 12 other real combinations fell through to the
`"In Progress"` fallback. Separately, the detail-page flowchart's "Submitted" stage and the
"Submit for Approval" button keyed off the Spring single-column `status` — which migrated
legacy rows carry as `1` (legacy used `indent_status` as an active flag) — so old indents
deep in the workflow showed "Not yet submitted" and a live Submit button.

**FIX 1 — deriveDisplayStatus() rewritten as a hierarchical decision tree** (`IndentService.java`):

RM stage decides first (`approvedStatus`), then Dept Head (`finalStatus`), then procurement
sub-stage (`procurementStatus`). Wildcard matching per level instead of exact-triple matching.
All 13 production combinations verified by unit test (`DeriveDisplayStatusTest`, 4 tests green):

| Triple | Label | Triple | Label |
|---|---|---|---|
| (3,4,7) ×562 | PO Released | (2,2,2) | RM Rejected |
| (3,4,8) ×39 | Hold | (3,4,5) | Quotations Collected |
| (3,4,9) ×34 | Cash Buy | (4,1,1) | RM Approved (legacy edge) |
| (2,1,1) ×23 | RM Rejected | (3,4,6) | Negotiation Done |
| (3,4,4) ×11 | Dept. Head Approved | (1,1,1) | Pending RM Approval |
| (3,2,2) ×10 | Dept. Head Rejected | (3,5,6) | Negotiation Done (legacy edge) |
| (3,1,1) ×10 | RM Approved | | |

Defensive extras beyond the production list: `(3,3,x)` → "Dept. Head Approved" and `(3,5,1)` →
"In Procurement" (pre-repair corruption triples, correct display until the owner runs
`docs/fix-l2-approved-indents.sql`); `(3,4,10/11)` Goods Receipt/Issued preserved;
`(3,4,≤4)` and null procurement → "Dept. Head Approved". `"In Progress"` remains only as a
true fallback. **Accessor confirmed:** the three columns are `IndentStatus` entity references
on `Indent`; the integer is extracted with `.getId()` (null-guarded) at both call sites
(`toIndentResponse`, `toIndentListResponse`). Method visibility changed private→package-private
for the unit test.

New labels "Pending RM Approval" (warning) and "In Procurement" (indigo) added to
`INDENT_STATUS_COLORS`; the list-page filter option "Pending" renamed to match.

**FIX 2 — Flowchart driven purely by the three-column model** (`IndentDetailPage.tsx`):

"Submitted" now always renders green — any persisted indent has entered the workflow; the
Spring status is never consulted. Stage rules: RM green when `approved∈{3,4}`, red when `2`,
amber when `1`; Dept Head green when `final∈{3,4,5}` (legacy ids included), red when `2`,
amber when RM-approved and `final=1`; Procurement reached once Dept Head approved, green when
`proc≥5` with the sub-stage label, amber "Awaiting Procurement" when `proc<5`. Rejection at
either level terminates the flow (later stages grey).

**FIX 3 — Submit (and Edit) button only for true drafts** (`IndentDetailPage.tsx`):

**Finding from `createIndent()`:** drafts are created with `approvedStatus=1, finalStatus=1,
procurementStatus=1` immediately — identical three-column state to a submitted indent. The
draft/submitted distinction lives ONLY in the Spring column (`status` 1=Draft → 2=Submitted via
`submitIndent()`). Therefore the task's literal condition (`approvedStatusId == null || === 0`)
would never be true for ANY indent and would kill the submit path for genuine new-app drafts —
NOT implemented as written. Implemented instead:
`isTrueDraft = status=1 AND approved=1 AND final=1 AND procurement=1` — hides Submit/Edit on
every in-workflow production combination listed above. Residual ambiguity: legacy rows at
exactly `(1,1,1)` with `status=1` are indistinguishable from new-app drafts; those still show
the button (clicking it is benign — sets status=2 and notifies RM). If desired the owner can
bulk-set `indent_status=2` on legacy `(1,1,1)` rows by import-date cutoff. The list page has
no submit button (verified).

**FIX 4 — Single source of truth confirmed:** both `toIndentResponse` and `toIndentListResponse`
call `deriveDisplayStatus`; Indent list, detail, approval page, and IndentReportPage all render
`displayStatus`; CSV/Excel export (`IndentController` lines ~172/212) already uses
`r.displayStatus()` with `statusName` fallback. No page computes labels independently.

**Files changed:**
- `backend/.../indent/IndentService.java` — deriveDisplayStatus rewrite
- `backend/src/test/java/.../indent/DeriveDisplayStatusTest.java` — new: 13-combination verification
- `frontend/src/pages/indents/IndentDetailPage.tsx` — flowchart + isTrueDraft gating
- `frontend/src/pages/indents/IndentsListPage.tsx` — filter label rename
- `frontend/src/constants/indentStatus.ts` — 2 new labels

**Build:** `mvn compile` clean; `mvn test -Dtest=DeriveDisplayStatusTest` 4/4 green;
`tsc -b && vite build` clean.

---

### l2Approve() Status Corruption + PROCUREMENT Sidebar + Procurement Scope

**Root cause — "In Progress" phantom status after DeptHead approval:**

`finalApproveIndent()` (called by the smart-route ApprovalController when status=3) and
`l2Approve()` (called by the legacy `/indents/{id}/l2-approve` endpoint) both produced
invalid three-column triples that fell through `deriveDisplayStatus()` to the "In Progress"
fallback, making DeptHead-approved indents show the wrong status and making the entire
procurement sub-workflow invisible to PROCUREMENT users.

| Method | set status | set finalStatus | set procurementStatus | triple | display |
|---|---|---|---|---|---|
| `finalApproveIndent()` BEFORE | 5 | **5** ← wrong | never set → 1 | (3,5,1) | "In Progress" |
| `finalApproveIndent()` AFTER | 5 | **4** ✓ | **4** ✓ | (3,4,4) | "Dept. Head Approved" |
| `l2Approve()` BEFORE | 3 | **3** ← wrong | never set → 1 | (3,3,1) | "In Progress" |
| `l2Approve()` AFTER | 3 | **4** ✓ | **4** ✓ | (3,4,4) | "Dept. Head Approved" |

The `status=5` in `finalApproveIndent()` is intentionally kept so the smart-router can
route the PROCUREMENT user's subsequent "Procurement Approve" click to `procurementApproveIndent()`.
The display label comes from the three-column triple, not from `status`.

**Root cause — PROCUREMENT sidebar Issue Notes not visible:**

The Issue Notes sidebar item had `roles: [SUPERADMIN, ADMIN, USER, DEPTHEAD, ISSUECONFIRM, SUPERVISOR]`
— PROCUREMENT was absent. The `activeNavItems` filter requires BOTH `hasAnyRole(item.roles)` AND
`hasModuleAccess(item.moduleCode)` to pass. PROCUREMENT failed the first check immediately,
short-circuiting the module check. The router.tsx fix from the previous commit was necessary
but insufficient — route guards prevent 403 on direct URL, but the sidebar never showed the link.

**Sidebar audit — PROCUREMENT presence by nav item:**

| Item | roles restricted? | PROCUREMENT included? | Action |
|---|---|---|---|
| Dashboard | No | Yes (no restriction) | None needed |
| Indents | No (moduleCode only) | Yes (no role gate) | None needed |
| Plant Indent | No (moduleCode only) | Yes (no role gate) | None needed |
| Issue Notes | Yes | **Added** ✓ | Fixed |
| Purchase Orders | Yes | Yes (already present) | None needed |
| Reports group | Yes | No — `[SUPERADMIN, ADMIN, DEPTHEAD, SUPERVISOR]` | Not fixed (separate task) |
| Confirmations | Yes | No — not a procurement workflow item | Not fixed |
| GRN | Yes (future) | No — future item, greyed for all | None needed |
| Quality Control | Yes (future) | No — future item | None needed |

**FIX — PROCUREMENT indent list scoping:**

Removed PROCUREMENT from the `isGlobal` group in `filterIndents()`. Added a dedicated
`isProcurement` branch that forces `approvedStatusId=3` and `finalStatusId=4` as mandatory
filters, ensuring PROCUREMENT only sees indents that have fully cleared the approval chain
and landed in the procurement queue. The user-selected `procurementStatusId` is passed through
so PROCUREMENT can still filter by sub-stage (Quotations Collected, PO Released, etc.).

**Data repair SQL:**

Written to `docs/fix-l2-approved-indents.sql` — covers both corruption paths:
- Path A: `(3, 5, 1)` with `status=5` — from `finalApproveIndent()` smart-route
- Path B: `(3, 3, 1)` with `status=3` — from `l2Approve()` legacy endpoint
Business owner reviews SELECT output, then uncomments and runs the UPDATE for each path.

**Execution path clarification:**

The smart-route `POST /api/v1/approvals/indents/{id}/approve` (ApprovalController) is the
**primary path** used by the current frontend (`indentsApi.approve()`). It routes:
- status=2 → `approveIndent()` (RM L1 approval: sets status=3, approvedStatus=3)
- status=3 → `finalApproveIndent()` ← **real DeptHead approval path**
- status=5 → `procurementApproveIndent()`

`l1Approve()` / `l2Approve()` are reached only via the legacy
`POST /api/v1/indents/{id}/l1-approve` and `/l2-approve` endpoints in IndentController.
Both methods are now also fixed for consistency.

**Files changed:**
- `backend/.../indent/IndentService.java` — `finalApproveIndent()`: finalStatus 5→4, procurementStatus added as 4; `l2Approve()`: finalStatus 3→4, procurementStatus added as 4; `filterIndents()`: PROCUREMENT removed from isGlobal, new isProcurement branch added
- `frontend/src/components/layout/Sidebar.tsx` — Issue Notes roles: PROCUREMENT added
- `docs/fix-l2-approved-indents.sql` — new: manual data repair for production

**Build:** Backend `mvn compile` clean. Frontend `tsc -b && vite build` clean.

---

### Procurement Role — UI Workflow + Route Access + Filter Cleanup

**Scope:** Four-part task completing PROCUREMENT role integration in the frontend: module access via issue-notes routes, procurement status update sub-workflow on indent detail, Stores issue/reject on issue note detail, and filter cleanup.

**Changes:**

#### PART 1 — Issue Notes module: add PROCUREMENT to route guards
- `frontend/src/routes/router.tsx` — Added `'PROCUREMENT'` to both `issue-notes` list (index) and `issue-notes/:id` detail ProtectedRoute roles arrays.
- Backend GET list `@PreAuthorize` already included PROCUREMENT — no backend change needed.

#### PART 2 — Indent detail: procurement sub-status update UI
- `frontend/src/api/indents.ts` — Added `procurementUpdate(id, data)` calling `POST /api/v1/indents/{id}/procurement-update` with `{ procurementSubStatus, poNumber?, deliveryDate?, remarks? }`.
- `frontend/src/pages/indents/IndentDetailPage.tsx`:
  - Added state: `procSubStatus` (default 5), `procPoNumber`, `procDeliveryDate`, `procRemarks`.
  - Added `procurementUpdateMutation`.
  - Added `isProcurementStage = finalStatusId === 4 && procurementStatusIdVal >= 4 && procurementStatusIdVal <= 9` flag.
  - Added `canUpdateProcurement = isProcurementStage && hasAnyRole(['SUPERADMIN', 'ADMIN', 'PROCUREMENT'])`.
  - Rendered inline "Procurement Status Update" card (no modal) with a dropdown (Quotations Collected=5, Negotiation Done=6, PO Released=7, Hold=8, Cash Buy=9) and conditional extra fields: PO Released shows poNumber + deliveryDate; Hold shows remarks; Cash Buy shows deliveryDate.

#### PART 3 — Issue note detail: Goods Issued + Reject (Stores) buttons
- `frontend/src/pages/issue-notes/IssueNoteDetailPage.tsx`:
  - Fixed `canIssue`: was checking flat `status` enum (`APPROVED_BY_MANAGER || PENDING_STORE_ISSUE`). Now uses two-column: `issueNote.approvedStatus === 3 && issueNote.storesByStatus === 1`.
  - Added `showStoresRejectModal`, `storesRejectReason` state.
  - Added `storesRejectMutation` calling `issueNotesApi.storesReject(id, { reason })` → `POST /issue-notes/{id}/reject-stores`.
  - Renamed "Issue Materials" button to "Goods Issued".
  - Added "Reject (Stores)" button alongside "Goods Issued" when `canIssue`.
  - Added Stores Rejection modal with required reason field.

#### PART 4 — Remove Goods Receipt / Goods Issued from indent status filter
- `frontend/src/pages/indents/IndentsListPage.tsx`:
  - Removed `'Goods Receipt'` (procurementStatus=10) and `'Goods Issued'` (procurementStatus=11) from `STATUS_FILTERS` map.
  - Removed corresponding `<option>` elements from the status dropdown.
  - `IndentReportPage.tsx` has no status dropdown — no change needed there.

**Confirmed clean (no change needed):**
- PROCUREMENT already in `filterIndents()` `isGlobal` group in `IndentService.java` ✓
- `issueNotesApi.storesReject()` already implemented in `issueNotes.ts` ✓
- Backend `POST /api/v1/indents/{id}/procurement-update` already exists with correct `@PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")` ✓

**Build:** Clean (`tsc -b && vite build` — 0 TypeScript errors)

**Commit:** (see git log)

---

## 2026-07-01

### SUPERVISOR Role — Complete End-to-End Audit and Fix

**Scope:** Comprehensive audit of every auth layer (backend @PreAuthorize, service logic, frontend route guards, page-level role checks, sidebar) for the SUPERVISOR role. Single-commit closure of all gaps found.

**Root causes fixed:**

1. **Backend @PreAuthorize — 18 endpoint gaps across 5 controllers** — SUPERVISOR was missing from IndentController list/search, ApprovalController reject/request-info/department-indents, IssueNoteController create/list/get/submit/cancel/by-department/my-issue-notes, PlantIndentController create/all GETs/update/submit, and all 10 DashboardController endpoints.

2. **Service logic — wrong approval queue routed to SUPERVISOR** — `IndentService.getPendingApprovals()` always returned the DeptHead L2 queue (indents waiting for final approval); SUPERVISOR needs the L1 RM queue (subordinates' submitted indents). Fixed by adding a role check that routes SUPERVISOR to `getPendingL1Approvals()`.

3. **Frontend route guards — 8 routes missing SUPERVISOR** — `/indents/new`, `/indents/:id/edit`, `/issue-notes/new`, `/issue-notes/:id/edit`, `/plant-indent/new`, `/plant-indent/:id/edit`, `/confirmations/issue`, `/confirmations/receipt`.

4. **Page-level role checks — 12 variables missing SUPERVISOR** — `IndentDetailPage.tsx` canEdit/canSubmit; `IssueNoteDetailPage.tsx` canEdit/canSubmit/canApprove; `IssueNoteApprovalPage.tsx` isManager; `IndentsListPage.tsx` edit button + New Indent button; `DashboardPage.tsx` stat card + 2 quick actions.

5. **IndentApprovalPage `getApprovalLevel()`** — SUPERVISOR now maps to "Level 1 (RM Review)" label.

6. **Sidebar.tsx** — Issue Notes, Confirmations, Reports group all missing SUPERVISOR.

**Files changed:**
- `backend/.../indent/ApprovalController.java` — `/reject`, `/request-info`, `/department-indents` + SUPERVISOR
- `backend/.../indent/IndentController.java` — list, search + SUPERVISOR
- `backend/.../issuenote/IssueNoteController.java` — 8 endpoints + SUPERVISOR
- `backend/.../plantindent/PlantIndentController.java` — create, 6 GETs, update, submit + SUPERVISOR
- `backend/.../dashboard/controller/DashboardController.java` — all 10 endpoints + SUPERVISOR
- `backend/.../indent/IndentService.java` — `getPendingApprovals()` routes SUPERVISOR to L1 queue
- `frontend/src/routes/router.tsx` — 8 routes + SUPERVISOR
- `frontend/src/pages/indents/IndentDetailPage.tsx` — canEdit, canSubmit + SUPERVISOR
- `frontend/src/pages/issue-notes/IssueNoteDetailPage.tsx` — canEdit, canSubmit, canApprove + SUPERVISOR
- `frontend/src/pages/issue-notes/IssueNoteApprovalPage.tsx` — isManager + SUPERVISOR
- `frontend/src/pages/indents/IndentsListPage.tsx` — edit button + create button + SUPERVISOR
- `frontend/src/pages/dashboard/DashboardPage.tsx` — stat card + 2 quick actions + SUPERVISOR
- `frontend/src/pages/indents/IndentApprovalPage.tsx` — getApprovalLevel() → "Level 1 (RM Review)"
- `frontend/src/components/layout/Sidebar.tsx` — Issue Notes, Confirmations, Reports + SUPERVISOR
- `docs/supervisor-role-verification.md` — new: full checklist with SQL verification queries

**Commit:** `1add319`

**Layers confirmed clean (no gaps):**
- RoleNormalizer: `"Supervisor"` → `"SUPERVISOR"` ✓
- JwtAuthenticationFilter: `ROLE_SUPERVISOR` prepended ✓
- ModuleAccessService: "ALL" wildcard + case normalization ✓ (fixed 2026-06-30)
- filterIndents() dept scoping: SUPERVISOR scoped to their deptId ✓
- createIndent() / submitIndent() service logic: no SUPERVISOR-specific exclusions ✓
- ApprovalController /pending, /pending-for-me, /approve: SUPERVISOR already present ✓
- IssueNoteController rm-approve, rm-reject, pending-rm-approval: SUPERVISOR already present ✓

---

## 2026-06-26

### Issue Note Status Labels — Legacy Alignment

**Files changed:**
- `backend/.../issuenote/IssueNoteService.java` — `deriveIssueNoteDisplayStatus()` updated to match legacy JSP labels exactly
- `frontend/src/constants/indentStatus.ts` — Added `'Stores Rejected': 'danger'`
- `frontend/src/pages/issue-notes/IssueNotesListPage.tsx` — Badge color uses `INDENT_STATUS_COLORS` instead of `getStatusVariant()`

**Legacy label mapping fixed:**
- `approvedStatus=null|1` → `"Pending"` (was "Pending RM Approval")
- `approvedStatus=2` → `"RM Rejected"` (was "Rejected by RM")
- `approvedStatus=3, storesByStatus=null|1` → `"RM Approved"` (was "Awaiting Stores Issue")
- `approvedStatus=3, storesByStatus=2` → `"Stores Rejected"` (was "Rejected by Stores")
- `approvedStatus=3, storesByStatus=11` → `"Goods Issued"` (unchanged)

---

## 2026-06-29

### Multi-Fix: Material Dropdown + Issue Note Pagination + Filter Labels + Remove Issued To

**Commit:** `fix: material dropdown data + quantity display | feat: issue note pagination | fix: issue note filter labels | fix: remove issued to column`

#### FIX 1 — Material Dropdown Data (CompanyPlantMaterial column mapping)

**Root cause:** `CompanyPlantMaterial.java` had `@Column(name = "map_quantity")` but the actual DB column is `map_quantity_stores`. Hibernate was reading the wrong column, causing `quantityStores` to always be null.

**File changed:**
- `backend/.../mapping/entity/CompanyPlantMaterial.java` — `@Column(name = "map_quantity_stores", precision = 20, scale = 2)`

#### FIX 2 — Issue Note Pagination

**Root cause:** Controller returned `Map<String, Object>` with key `totalItems`. Frontend `DataTable` reads `totalElements`. Pagination always showed 0 total items.

**Files changed:**
- `backend/.../issuenote/IssueNoteRepository.java` — Added `filterIssueNotes()` JPQL query (approvedStatus + storesByStatus + departmentId + search, all optional)
- `backend/.../issuenote/IssueNoteService.java` — `getAll()` now returns `Page<IssueNoteSummaryResponse>` via `filterIssueNotes()`
- `backend/.../issuenote/IssueNoteController.java` — Returns `ResponseEntity<Page<IssueNoteSummaryResponse>>` directly; params updated to `search`, `approvedStatus`, `storesByStatus`, `departmentId`

#### FIX 3 — Issue Note Status Filter (legacy-aligned composite key)

**Root cause:** Old dropdown used enum strings (DRAFT, PENDING_APPROVAL) — unrelated to the actual two-column workflow (approvedStatus × storesByStatus).

**Files changed:**
- `frontend/src/api/issueNotes.ts` — `IssueNoteSearchParams`: replaced `status?: IssueNoteStatus` with `approvedStatus?: number` + `storesByStatus?: number` (kept `status?: number` for legacy callers)
- `frontend/src/pages/issue-notes/IssueNotesListPage.tsx` — New `decodeStatusFilter()` helper + 5 dropdown options matching legacy labels (Pending, RM Rejected, RM Approved, Goods Issued, Stores Rejected)

#### FIX 4 — Remove "Issued To" Column

**File changed:**
- `frontend/src/pages/issue-notes/IssueNotesListPage.tsx` — Removed `issuedTo` table column

---

### Material Dropdown + Material Master List + Material Detail Page

**Commit:** `fix: material dropdown LEFT JOIN all materials | fix: MaterialResponse align to DB | fix: material list remove non-existent columns | fix: material detail page`

#### FIX 1 — Material Dropdown LEFT JOIN (all 647 materials now appear)

**Root cause:** Dropdown JPQL used `FROM CompanyPlantMaterial cpm JOIN Material m ...` (INNER JOIN). Only 10 materials had a row in `tbl_pz_map_company_plant_material`, so 637 active materials were invisible.

**Fix:** Flipped to `FROM Material m LEFT JOIN CompanyPlantMaterial cpm ON cpm.materialId = m.id LEFT JOIN Company/Plant`. Materials with no mapping return `COALESCE(cpm.quantityStores, 0)`.

Added description to search terms (was only code + name). `searchForDropdownByCompanies` adds `AND (cpm.companyId IS NULL OR cpm.companyId IN :companyIds)` so unmapped materials are visible to non-admin users too.

**File changed:** `backend/.../mapping/repository/CompanyPlantMaterialRepository.java`

#### FIX 2 — MaterialResponse aligned to actual DB columns

**Root cause:** `MaterialResponse.java` had 8 fields but frontend expected ~15 (uomCode, categoryName, hsnCode, etc.). None of those columns exist in `tbl_material_master`.

**Fix:** Rewrote record to match DB exactly: `id`, `materialCode`, `materialName`, `description`, `status`, `statusText`, `isActive` (boolean), `stockQuantity` (from CPM join), `companyName`, `plantName`, `lastModifiedDate`, `lastModifiedBy`. Removed static `from(Material)` factory; replaced with `MaterialService.toResponse(Material)` calling `sumQuantityByMaterial()`.

Added `sumQuantityByMaterial(@materialId)` to `CompanyPlantMaterialRepository` — verified: material_id=1 → 700.00.

**Files changed:** `MaterialResponse.java`, `MaterialService.java`, `CompanyPlantMaterialRepository.java`

#### FIX 3 — Material list page columns fixed

**Root cause:** 8 columns referencing non-existent fields; `isActive` always false (was undefined); stock from a broken separate `inventoryApi` call.

**Fix:** Columns reduced to: Code, Description, Avail. Stock, Status, Actions. Stock from `row.stockQuantity`. Removed `inventoryApi` import entirely.

**File changed:** `frontend/src/pages/materials/MaterialsListPage.tsx`

#### FIX 4 — Material detail page simplified

**Root cause:** Rendered non-DB fields (uomCode, categoryName, etc.) plus a separate `inventoryApi.list({size:100})` call.

**Fix:** Removed all non-existent field references and inventory API call. Shows: status banner (isActive, stockQuantity, lastModifiedDate) + Material Information card + Stock card.

**File changed:** `frontend/src/pages/materials/MaterialDetailPage.tsx`

---

### Material Stock Source + Dropdown Load-All + Remove Phantom Columns + Form Auto-fill Investigation

**Commit:** `fix: material stock from legacy table | fix: dropdown load-all on focus | fix: materials list name column | fix: remove broken inventory re-fetch`

#### FIX 1 — Switch stock source to tbl_map_company_plant_material (legacy, 669 rows)

**Root cause:** `MaterialService` and the dropdown queries read from `tbl_pz_map_company_plant_material` (entity: `CompanyPlantMaterial`, 10 rows). The legacy table `tbl_map_company_plant_material` (entity: `CompanyPlantMaterialMap`, 669 rows, 221 with real stock) already existed in the codebase but was not used for material stock display.

**Fix:** Added `sumQuantityByMaterial`, `searchForDropdownAllCompanies`, `searchForDropdownByCompanies` to `CompanyPlantMaterialMapRepository`. Updated `MaterialService` constructor to inject `CompanyPlantMaterialMapRepository`; switched `toResponse()` and `searchMaterialsForDropdown()` to call the new repository.

JPQL dropdown queries use `LEFT JOIN CompanyPlantMaterialMap s ON s.material = m AND s.status IN (0, 1)` with `LEFT JOIN s.company co / LEFT JOIN s.plant pl` — all materials appear; unmapped ones return null company/plant and 0 stock via `COALESCE(SUM(s.quantity), 0)`.

**Verified:** `sumQuantityByMaterial(1)` → 0.00 from `tbl_map_company_plant_material` (material BCH-1-2-DI-C-500ML has 0 legacy stock, which is correct).

**Files changed:** `CompanyPlantMaterialMapRepository.java`, `MaterialService.java`

#### FIX 2 — Material dropdown loads on focus (no minimum typing required)

**Root cause:** Both form pages had `enabled: materialSearch.length >= 2` in the React Query config. Clicking the material search field showed nothing. Users had to type 2+ characters before any results appeared.

**Fix:** Changed to `enabled: showMaterialSearch` in both pages. Dropdown results load immediately on focus (empty search returns all 647 active materials from backend). Removed "Type at least 2 characters" placeholder.

Also removed `inventoryApi.getStockByMaterialAndPlant()` plant-change `useEffect` from both pages — this call read `tbl_inventory_balance` which has 0 rows, silently wiping all displayed stock quantities whenever plant changed. Stock now comes exclusively from `MaterialDropdownItem.stockQuantity` at selection time.

**Files changed:** `IndentFormPage.tsx`, `IssueNoteFormPage.tsx`

#### FIX 3 — Material list page: add Name column

**Root cause:** "Description" column was rendering `row.materialName || row.description` — it displayed material name but was labelled "Description". The actual `description` field was never shown.

**Fix:** Split into two columns: "Name" (`row.materialName`) and "Description" (`row.description`).

**File changed:** `frontend/src/pages/materials/MaterialsListPage.tsx`

#### Investigation: FIX 5 — Plant auto-fill and legacy Issue Note form fields

**FIX 5a-5b (SKIPPED — employees have no plant FK):**
`tbl_emp_master.emp_location` is a FK to `tbl_location_master` (locations like "Icon", "NSL", "Lab Biotech Main") — NOT to `tbl_plant_master`. `emp_plant` is a VARCHAR text field (e.g., '1002'), not a plant ID. No meta endpoint change made — adding `plantId = getLocationId()` would silently set plant to a location ID (e.g., 13 = "Icon") which doesn't exist as a plant. Plant selection stays manual.

**FIX 5d — Legacy Issue Note fields (report only, no changes):**
Legacy `seeds_issue_note_request.jsp` had: Year, Emp ID, Issue Note No., Emp Name (all read-only), Company, Plant, Department, Section (manual dropdowns), per-item: Material Description dropdown + Requested Qty + Balance Qty (read-only from `getQuantity.action`), header-level Remarks textarea.

Fields added in the new rewrite vs legacy:
- UOM per item — not in legacy
- Purpose per item — not in legacy
- "Issued To" field (required) — not in legacy (is the recipient of materials, not the requester — intentionally added for the new approval workflow, kept)

No fields removed. No changes made to IssueNoteFormPage.

---

## 2026-06-30

### Form Auto-fill + Field Cleanup + Card Restructure

**Commit:** `fix: form auto-fill company/dept/section/issued-to | fix: hide plant if none, auto-select if single | fix: remove delivery date from indent form | fix: isolate comments+purpose into Additional Information card | fix: plant indent form meta-based auto-fill`

#### FIX 1 — IssueNoteFormPage: complete auto-fill from meta

**Root cause:** Meta useEffect only set `departmentId` and `companyId`. `issuedTo`, `sectionId`, and plant auto-fill were missing.

**Fix:**
- Added `setValue('issuedTo', metaData.empName)` inside the meta useEffect — pre-fills the recipient field with the logged-in employee's name.
- Added section useEffect: when `sectionsData` loads, auto-selects first section (`sectionsData.content[0].id`). Matches the pattern already in IndentFormPage.
- Added plant useEffect: if `plantsData.content` has exactly 1 plant, auto-selects it; if 0 plants, sets `showPlant = false` to hide the field.
- Added `showPlant` state (defaults `true`); Plant `<Col>` wrapped in `{showPlant && (...)}`.

**File changed:** `frontend/src/pages/issue-notes/IssueNoteFormPage.tsx`

#### FIX 2 — IndentFormPage: plant auto-fill + showPlant

**Root cause:** Meta useEffect already set `companyId`, `departmentId`, `sectionId` (via separate useEffect). Plant auto-fill was missing.

**Fix:** Added `showPlant` state and plant useEffect (same logic as FIX 1). Plant `<Col>` wrapped in `{showPlant && (...)}`.

**File changed:** `frontend/src/pages/indents/IndentFormPage.tsx`

#### FIX 4 — IndentFormPage: remove Delivery Date field

**Root cause:** Delivery Date field was present on indent creation form but does not exist in the legacy system. Field remains in the schema and backend for DB compatibility.

**Fix:** Removed `<Col>` containing the Delivery Date `<Form.Control type="date">` from the Basic Information card JSX. No backend or schema changes.

**File changed:** `frontend/src/pages/indents/IndentFormPage.tsx`

#### FIX 5 — Isolate Comments + Purpose into separate "Additional Information" card

**Root cause:** Comments (IndentFormPage) and Purpose+Comments (IssueNoteFormPage) were inline inside the header information card, making the card visually cluttered and hard to scan.

**Fix:** Removed Purpose and Comments from the header card in each form. Added a new "Additional Information" card placed below the line-items table and above the action buttons.

- IndentFormPage: new card has `Comments` textarea only.
- IssueNoteFormPage: new card has `Purpose` text input + `Comments` textarea.
- PlantIndentFormPage: existing "Additional Details" card (Comments) moved from above line items to below line items; header renamed to "Additional Information".

**Files changed:** `IndentFormPage.tsx`, `IssueNoteFormPage.tsx`, `PlantIndentFormPage.tsx`

#### FIX 6 — PlantIndentFormPage: meta-based auto-fill

**Root cause:** PlantIndentFormPage did not call any meta endpoint. It used `user.departmentId` and `user.plantId` from the auth context — `user.plantId` is a legacy `VARCHAR` text field (e.g., `'1002'`), not an FK, so it never resolved to a valid plant.

**Fix:**
- Added `indentsApi` import; added meta query (`GET /api/v1/indents/meta`, `enabled: !isEdit`).
- Replaced the user-context useEffect with a meta useEffect that sets `companyId = metaData.defaultCompanyId` and `departmentId = metaData.departmentId`.
- Added plant useEffect (same `showPlant` pattern as other forms).

**File changed:** `frontend/src/pages/plant-indent/PlantIndentFormPage.tsx`

---

### Wave 1 Data-Capture Fixes: availableStock on Indent + quantityStores + Over-Request Guard on Issue Note

**Commit:** `fix: capture availableStock on indent save | fix: capture quantityStores + over-request guard on issue note`

#### FIX 1 — Capture availableStock per line on Indent save

**Root cause:** `stockByIndex` state was already populated from the material dropdown selection (`MaterialDropdownItem.stockQuantity`) and displayed in the UI, but `transformFormData()` did not include it in the API payload. The backend (entity column `indent_details_stock_aval`, DTO field, service setter) was already fully wired.

**Fix:** Changed `data.items.map((item) => ...)` to `data.items.map((item, index) => ...)` in `transformFormData()` and added `stockAvailable: stockByIndex[index] ?? undefined` to each detail object.

**File changed:** `frontend/src/pages/indents/IndentFormPage.tsx`

#### FIX 2 — Capture quantityStores per line on Issue Note save + over-request guard

**Root cause:** `tbl_issue_note_details.issue_note_details_quantity_stores` column had no entity mapping. The create DTO had no field for it. The frontend did not include it in the submit payload. No server-side guard existed.

**Fix:**
- `IssueNoteDetails.java` — added `@Column(name = "issue_note_details_quantity_stores", precision = 20, scale = 2) private BigDecimal quantityStores`
- `CreateIssueNoteRequest.IssueNoteLineItem` — added `BigDecimal quantityStores` to the nested record
- `IssueNoteService.createIssueNote()` — server-side guard: `if (quantityStores != null && quantity > quantityStores) throw new IllegalArgumentException(...)`. Passes `quantityStores` into the entity builder.
- `issueNotes.ts` — added `quantityStores?: number` to `IssueNoteLineItemCreateRequest`
- `IssueNoteFormPage.tsx`:
  - Both `onSubmit` and `handleSaveAndSubmit` payloads now include `quantityStores: stockByIndex[index] ?? undefined`
  - `hasStockViolation` computed from `watchLineItems.some(...)` — true if any line's quantity exceeds its captured stock
  - Inline per-line error "Exceeds available stock (N)" rendered below the Quantity field
  - "Save & Submit for Approval" button disabled when `hasStockViolation`

**Files changed:** `IssueNoteDetails.java`, `CreateIssueNoteRequest.java`, `IssueNoteService.java`, `issueNotes.ts`, `IssueNoteFormPage.tsx`

#### FIX 3 — Label alignment (flag only, no change)

No code change. Issue noted for tracking: label alignment in Issue Note line items is cosmetically different from legacy but not a functional gap.

---

### Approval Workflow Bugs: Supervisor Role + BUG 1 Root Cause

**Commit:** `fix: Supervisor role wired into approval permissions | fix: indent approved_status incorrectly set to 3 at creation`

#### BUG 1 — indent_approved_status = 3 at creation (root cause investigation, no code change)

**Symptom:** Indent 700 has `indent_status = 1` (DRAFT) with `indent_approved_status = 3` (Dept Head Approved). Created 2025-07-16.

**Root cause:** Pre-refactoring `createIndent()` contained a DEPTHEAD bypass that auto-approved the indent at creation time. That bypass was removed in a prior refactor. Current `createIndent()` correctly initializes all three status columns to 1.

**Additional finding — `hasRoleByCode()` bug (non-functional bypass):**
`hasRoleByCode()` (`IndentService.java:1865`) compares the passed string against the raw DB `role_code` column using `equalsIgnoreCase`. The caller in `submitIndent()` passes normalized string `"DEPTHEAD"`, but the DB stores `"Department"`. `"Department".equalsIgnoreCase("DEPTHEAD")` = false. The DEPTHEAD auto-submit bypass in `submitIndent()` is completely non-functional and has never fired since the normalization was introduced.

**No fix applied** — the old data is a legacy artifact; the code path no longer exists. The `hasRoleByCode` bug does not affect any live workflow (the bypass is dead code).

#### BUG 2 — Supervisor role not wired into approval permissions

**Root cause:** `ApprovalController.approveIndent()` `@PreAuthorize` listed only `DEPTHEAD`, `PLANTMANAGER`, `PROCUREMENT`, `ADMIN`, `SUPERADMIN`. `RoleNormalizer` maps DB `"Supervisor"` → `"SUPERVISOR"` → JWT `ROLE_SUPERVISOR`, but `SUPERVISOR` was absent from the allow-list. 10 Supervisor employees could not approve any indent.

**Additional gap:** `approveIndent()` in `IndentService` had no hierarchy check — any DEPTHEAD (or now SUPERVISOR) could approve any indent in the plant, not just those created by subordinates.

**Fix:**

- `ApprovalController.java` — Added `or hasRole('SUPERVISOR')` to `@PreAuthorize` on the `/indents/{id}/approve` endpoint.
- `IndentService.java` — Added hierarchy check inside `approveIndent()`: users without dept-level authority (`"Department"`, `"Plant Manager"`, `"Admin"`, `"Super Admin"` raw DB values) must pass `reportingHierarchyService.canApproveFor(creatorEmpNumber, approverEmpNumber)`. Throws `IllegalStateException` if not in the reporting chain.
- `IndentDetailPage.tsx` — `canApprove` now includes `'SUPERVISOR'` for `status=2` (Submitted). Approve button label changed from `'Approve (Dept Head)'` to `'Approve (RM Review)'`.
- `router.tsx` — `IndentApprovalPage` and `IndentDetailPage` `ProtectedRoute` both include `'SUPERVISOR'`.

**Files changed:** `ApprovalController.java`, `IndentService.java`, `IndentDetailPage.tsx`, `router.tsx`

---

### Supervisor Approval Permission Mismatch — Phase 2 Fix

**Commit:** `fix: resolve Supervisor approval permission mismatch — GET /approvals/pending endpoints missing SUPERVISOR role`

#### Root Cause

The previous fix (Phase C) added `hasRole('SUPERVISOR')` to the POST `/indents/{id}/approve` endpoint, but not to the GET endpoints that load the approval queue. When a Supervisor navigates to `IndentApprovalPage`, it immediately calls `GET /api/v1/approvals/pending` (via `indentsApi.getPendingApproval()`). That endpoint's `@PreAuthorize` listed only `DEPTHEAD`, `PLANTMANAGER`, `ADMIN`, `SUPERADMIN` — no `SUPERVISOR`. Spring Security threw `AccessDeniedException`, caught by `RestExceptionHandler.handleAccessDenied()`, which returns the generic "You do not have permission to perform this action". The approve POST endpoint was already correct; the Supervisor could never reach it because the page load itself failed.

**Role chain confirmed correct** (no mismatch):
- `RoleNormalizer`: DB `"Supervisor"` → `"SUPERVISOR"`
- JWT claim `"roles"`: `["SUPERVISOR"]`
- `JwtAuthenticationFilter` → `UserPrincipal.authorities()`: prepends `"ROLE_"` → `ROLE_SUPERVISOR`
- `hasRole('SUPERVISOR')` checks for `ROLE_SUPERVISOR` → match ✓

The fix is purely an incomplete `@PreAuthorize` on the data-loading endpoints, not a role-string mismatch.

#### Fix

- `ApprovalController.java` — Added `or hasRole('SUPERVISOR')` to `@PreAuthorize` on both:
  - `GET /api/v1/approvals/pending`
  - `GET /api/v1/approvals/pending-for-me`

No service-layer changes required. The `approveIndent()` hierarchy check (added in Phase C) continues to enforce that Supervisors can only approve indents from their direct reporting chain.

**File changed:** `ApprovalController.java`

---

### Module Access "ALL" Wildcard + Route Guard + Safety Fallback

**Commit:** `fix: ALL wildcard not recognized in module role matching | fix: SUPERVISOR missing from indents route guard | fix: empty allowedModules safety fallback`

#### Root Cause

`tbl_module_master.module_default_roles` stores the literal string `"ALL"` for 5 base modules (INDENTS, ISSUE_NOTES, PLANT_INDENTS, REPORTS, CONFIRMATIONS) — meaning every authenticated user should have access. `ModuleAccessService.isAllowedByRole()` was checking `"ALL".equals(defaultRoles)` as a whole-string exact match BEFORE splitting, which fails if the value has trailing whitespace, is lowercase, or appears alongside other roles (e.g. `"ALL,SUPERVISOR"`). Additionally, the role comparison after splitting was not normalizing case, so `defaultRoles` tokens like `"Supervisor"` would never match the normalized JWT role `"SUPERVISOR"`.

Effect on system: every role that isn't SUPERADMIN received `allowedModules: []` at login because none of the modules matched. `hasModuleAccess()` then returned `false` for every module code, collapsing the entire sidebar to empty. Users (notably Venki after role change to Supervisor) saw a blank sidebar and effectively no navigation.

#### FIX 1 — isAllowedByRole() case-normalization + in-set ALL wildcard

`ModuleAccessService.isAllowedByRole()` rewritten to:
1. Split `defaultRoles` on commas and `.toUpperCase()` every token
2. Check `allowed.contains("ALL")` AFTER splitting — handles `"ALL"`, `"all"`, `"ALL,SUPERVISOR"`, `" ALL "` all correctly
3. Normalize `userRoles` stream to uppercase before matching — handles mixed-case DB role codes vs normalized JWT roles

This single change fixes module access for ALL roles in the system, not just Supervisor. Any future role is automatically granted access to modules marked "ALL" without a DB update.

**File changed:** `ModuleAccessService.java`

#### FIX 2 — SUPERVISOR added to route guards

Every `ProtectedRoute` in `router.tsx` was audited. SUPERVISOR added to routes that logically include reporting managers:

| Route | Before | SUPERVISOR added? |
|---|---|---|
| `/indents` list | `USER, DEPTHEAD, PROCUREMENT` | ✅ Yes |
| `/indents/approvals` | already had SUPERVISOR | — already done |
| `/indents/:id` | already had SUPERVISOR | — already done |
| `/issue-notes` list | `USER, ISSUECONFIRM, DEPTHEAD` | ✅ Yes |
| `/issue-notes/approvals` | `DEPTHEAD, ISSUECONFIRM` | ✅ Yes |
| `/issue-notes/:id` | `USER, ISSUECONFIRM, DEPTHEAD` | ✅ Yes |
| `/plant-indent` list | `PLANTMANAGER, FLOORINCHARGE` | ✅ Yes |
| `/plant-indent/:id` | `PLANTMANAGER, FLOORINCHARGE` | ✅ Yes |
| `/reports` (all 3 sub-routes) | `ADMIN, DEPTHEAD` | ✅ Yes |
| `/plant-indent/new`, `:id/edit` | `PLANTMANAGER` only | ✗ Supervisor shouldn't create/edit |
| `/issue-notes/new`, `:id/edit` | `USER` only | ✗ Supervisor shouldn't create/edit |
| All other routes (PO, GRN, masters, admin) | role-specific lists | ✗ Not relevant to Supervisor |

**File changed:** `router.tsx`

#### FIX 3 — Safety fallback for empty allowedModules

`AuthContext.hasModuleAccess()` updated: if `allowedModules` is empty (zero-length array, not null), grant the 5 base modules (`INDENTS`, `PLANT_INDENTS`, `ISSUE_NOTES`, `REPORTS`, `CONFIRMATIONS`) as a fallback. This prevents any future module misconfiguration from completely locking out a logged-in user. Fix 1 resolves the actual root cause; this remains as defense-in-depth.

**File changed:** `AuthContext.tsx`

---

## 2026-07-01 (second entry)

### Role-Based Indent and Issue Note Visibility + DEPTHEAD Confirmation Popup

**Commit:** `adcd7d0`

**Scope:** End-to-end visibility fix for the Indents list and Issue Notes list — each role now sees only the records it is entitled to. Accompanying UX: DEPTHEAD/PLANTMANAGER get an explicit confirmation dialog before submitting an indent, because their submit bypasses RM approval and goes directly to Procurement.

---

#### PART 1 + PART 2 — filterIndents() role-based routing (IndentService + IndentRepository)

**Root cause:** `IndentService.filterIndents()` had a single block: if not ADMIN/SUPERADMIN, force `effectiveDeptId = cu.deptId()`. This incorrectly dept-scoped USER (should see only own indents), SUPERVISOR (should see own + subordinates), and PROCUREMENT (should see global, not their dept).

**Fix — IndentService.filterIndents():** Replaced the flat dept-scope block with a priority chain:

| Role | Visibility |
|------|-----------|
| ADMIN, SUPERADMIN, PROCUREMENT | Global — no scope restriction |
| DEPTHEAD, PLANTMANAGER | All indents in their department (`cu.deptId()`) |
| SUPERVISOR | Own indents + direct subordinates (`findSubordinateNumbers()`) |
| USER (default) | Own indents only (`cu.employeeNumber()`) |

DEPTHEAD/PLANTMANAGER continue to call the existing `filterIndents()` with `departmentId = cu.deptId()`. SUPERVISOR and USER call the new `filterIndentsForCreators()` with a list of employee numbers. ADMIN/SUPERADMIN/PROCUREMENT fall through to the unscoped `filterIndents()`.

**Fix — IndentRepository:** Added `filterIndentsForCreators()` — same JPQL as `filterIndents()` except `i.employee.employeeNumber IN :empNumbers` replaces the `departmentId` clause. `@EntityGraph` identical to `filterIndents()`.

**PART 4 note (verify only, no change):** `submitIndent()` auto-skip condition is `hasRoleByCode(empNum, "DEPTHEAD") && !hasSupervisor(empNum)`. SUPERVISOR role is NOT present — SUPERVISOR submits through the normal L1 flow. Confirmed correct, no change made.

**Files changed:**
- `backend/.../indent/IndentRepository.java` — added `filterIndentsForCreators()`
- `backend/.../indent/IndentService.java` — replaced dept-scope block in `filterIndents()` with role-priority chain

---

#### PART 3 — DEPTHEAD confirmation popup (IndentFormPage)

**Context:** When a DEPTHEAD or PLANTMANAGER submits an indent, `submitIndent()` auto-approves L1, routing directly to the Procurement queue. The user had no indication of this bypass.

**Fix:** "Save & Submit" now triggers a two-step flow for DEPTHEAD/PLANTMANAGER:

1. `handleSaveAndSubmit()` detects `isDeptHead` → stores form data in `pendingSubmitData`, opens `showDeptHeadConfirm` modal.
2. `ConfirmDialog` renders with: title "Direct to Procurement", message "You are a Department Head. Your indent will skip RM approval and go directly to Procurement. Do you agree?", confirm "Yes, Submit Directly", cancel "Cancel", variant "warning".
3. On confirm → `handleDeptHeadConfirm()` calls `doSaveAndSubmit()`.

**Component used:** Existing `ConfirmDialog` at `frontend/src/components/common/ConfirmDialog.tsx`.

**File changed:** `frontend/src/pages/indents/IndentFormPage.tsx`

---

#### PART 5 — Same visibility fix applied to Issue Notes (IssueNoteService + IssueNoteRepository)

**Root cause:** `IssueNoteService.getAll()` had no role-based scoping — all roles saw all issue notes.

**Fix — IssueNoteService.getAll():** Same role priority chain as Indents. ISSUECONFIRM added to the global group (alongside ADMIN/SUPERADMIN/PROCUREMENT).

Injected `EmployeeReportingRepository` into `IssueNoteService` (previously not present).

**Fix — IssueNoteRepository:** Added `filterIssueNotesForCreators()` — same JPQL as `filterIssueNotes()` except `i.createdBy IN :empNumbers` (IssueNote uses a raw Integer column, not an Employee relation).

**Note:** `filterIssueNotes()` already existed (added 2026-06-29). Only `filterIssueNotesForCreators()` was new.

**Files changed:**
- `backend/.../issuenote/IssueNoteRepository.java` — added `filterIssueNotesForCreators()`
- `backend/.../issuenote/IssueNoteService.java` — added `EmployeeReportingRepository` field + role-priority chain in `getAll()`

---

#### PART 6 — Role-aware page titles (IndentsListPage, IssueNotesListPage)

| Role | Indents title | Issue Notes title |
|------|--------------|-------------------|
| ADMIN / SUPERADMIN / PROCUREMENT | All Indents | All Issue Notes |
| DEPTHEAD / PLANTMANAGER | Department Indents | Department Issue Notes |
| SUPERVISOR | My Team Indents | My Team Issue Notes |
| USER (default) | My Indents | My Issue Notes |

**Files changed:**
- `frontend/src/pages/indents/IndentsListPage.tsx`
- `frontend/src/pages/issue-notes/IssueNotesListPage.tsx`

---

**Build results:**
- `mvn compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — built in 32.61s, 0 errors (pre-existing chunk-size warning unrelated to this change)

---

## 2026-07-01 (third entry)

### USER→RM→DEPTHEAD Approval Workflow — 5 Root-Cause Bugs Fixed

**Commit:** `3d14c07`

**Scope:** After the visibility fix (commit `adcd7d0`), testing revealed the complete approval workflow was broken for all roles. Five distinct root-cause bugs prevented indents from moving through the pipeline.

**Correction to prior log entry (2026-06-30, "Approval Workflow Bugs"):**
> "The `hasRoleByCode` bug does not affect any live workflow (the bypass is dead code)."

This was **WRONG**. The bypass WAS needed for DEPTHEAD to skip RM approval on submit. It was silently broken (never firing), which is why DEPTHEADs saw their indents going to RM instead of Procurement directly.

---

#### BUG 1 — USER sees "Direct to Procurement" popup (Frontend)

**File:** `frontend/src/contexts/AuthContext.tsx`

**Root cause:** `hasAnyRole()` had a shortcut `if (user.roles.includes('SUPERADMIN') || user.canView) return true`. If the "User" DB role has `can_view = "1"`, every USER employee has `user.canView = true`, causing them to pass ANY role check — including `hasAnyRole(['DEPTHEAD', 'PLANTMANAGER'])` which guards the popup.

**Fix:** Removed `|| user.canView`. SUPERADMIN shortcut kept; role membership check is now the only path.

---

#### BUG 2 — DEPTHEAD bypass never fires (Backend)

**File:** `backend/.../indent/IndentService.java` `submitIndent()`

**Root cause:** `hasRoleByCode(currentUser.getEmpNumber(), "DEPTHEAD")` always returned `false`. `hasRoleByCode()` compares the passed string against the raw DB `role_code` column using `equalsIgnoreCase`. DB stores `"Department"`, not `"DEPTHEAD"`. `"Department".equalsIgnoreCase("DEPTHEAD") = false`.

**Fix:** Changed to `hasRoleByCode(currentUser.getEmpNumber(), "Department")`.

---

#### BUG 3 — RM-approved indent never reaches DeptHead queue (Backend)

**File:** `backend/.../indent/IndentService.java` `l1Approve()`

**Root cause:** `l1Approve()` set `approvedStatus = IndentStatus(2)` with comment "L1 Approved marker". DeptHead queue JPQL (`findDeptHeadQueue`, `findDeptHeadQueueByDepartment`) requires `approvedStatus.id = 3`. Value `2` never matched the queue filter.

**Fix:** Changed to `approvedStatus = IndentStatus(3)` — consistent with the auto-bypass in `submitIndent()` which already correctly sets `approvedStatus = 3`.

---

#### BUG 4 — ALL approval queues permanently empty (Backend)

**File:** `backend/.../indent/IndentRepository.java`

**Root cause:** All 5 queue queries contained `AND i.status.id = 1`. But `submitIndent()` changes `status` from 1 (Draft) to 2 (Submitted). After submission, every indent has `status.id = 2`, so no indent ever satisfies `status.id = 1`. All approval queues (RM, DeptHead, Procurement, GoodsReceipt) returned empty for every submitted indent.

This is the root cause of "SUPERVISOR approval page is empty" and "DEPTHEAD approval queue is empty".

**Fix:** Removed `AND i.status.id = 1` from all five queries:
- `findRmQueueForEmployees` — RM (SUPERVISOR) approval queue
- `findDeptHeadQueueByDepartment` — DeptHead dept-scoped queue
- `findDeptHeadQueue` — DeptHead global queue (ADMIN/SUPERADMIN)
- `findProcurementQueue` — Procurement queue
- `findGoodsReceiptQueue` — Goods Receipt queue

The workflow stage is already uniquely determined by the three workflow columns (`approvedStatus`, `finalStatus`, `procurementStatus`); the `status` column filter adds no meaningful constraint and actively breaks every queue.

---

#### BUG 5 — DEPTHEAD list only shows own indents (Backend)

**Files:** `backend/.../indent/IndentService.java` `filterIndents()` + `getPendingL2Approvals()` + `getPendingApprovals()`

**Root cause:** DEPTHEAD scope used `cu.deptId()` (from `emp_department` FK on the logged-in user's employee record). Indents from subordinate employees have a `department.id` FK on the indent itself. If `emp_department` values are not consistently populated across test employees (or point to different rows), the dept-based filter silently misses all subordinates' indents.

**Fix:** Replaced dept-based approach with 2-level hierarchy expansion for DEPTHEAD/PLANTMANAGER:
1. Own employee number
2. Direct reports (`employeeReportingRepository.findSubordinateNumbers(own)`)
3. Their direct reports (one more level)

Now calls `filterIndentsForCreators(empNumbers, ...)` — same query used by SUPERVISOR, just with a wider employee list.

**Same fix applied to approval queues:**
- Added `findDeptHeadQueueForCreators(List<Integer> empNumbers)` to `IndentRepository` — `approvedStatus=3 AND finalStatus=1 AND employee.employeeNumber IN :empNumbers`
- `getPendingL2Approvals()`: replaced `findDeptHeadQueueByDepartment(deptId)` with `findDeptHeadQueueForCreators(hierEmpNumbers)`
- `getPendingApprovals()`: added DEPTHEAD branch that routes to `getPendingL2Approvals()` (hierarchy-based) before falling through to the old global queue (which remains correct for ADMIN/SUPERADMIN)

---

**Files changed:**
- `frontend/src/contexts/AuthContext.tsx` — Bug 1
- `backend/.../indent/IndentService.java` — Bugs 2, 3, 5
- `backend/.../indent/IndentRepository.java` — Bugs 4, 5

**Build results:**
- `mvn compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors

---

## 2026-07-01 (fourth entry)

### Role Code Mismatch Audit — `"Department Head"` Never Mapped in RoleNormalizer

**Commit:** `86da7be`

**Root cause (system-wide):**
`tbl_roles_master.role_code` stores `"Department Head"` (with a space). `RoleNormalizer.java` had `Map.entry("Department", "DEPTHEAD")` — a dead mapping whose key never matched any real DB row. The fallback (`rawCode.replaceAll("\\s+","").toUpperCase()`) produced `"DEPARTMENTHEAD"`. Every `@PreAuthorize("hasRole('DEPTHEAD')")`, `hasAnyRole(['DEPTHEAD'])`, and module access check for Department Head silently failed system-wide since RoleNormalizer was introduced.

---

#### FIX 1 — RoleNormalizer.java

**Before:**
```
"Department"  → "DEPTHEAD"   ← dead (no DB row has code "Department")
```

**After:**
```
"Department Head" → "DEPTHEAD"   ← primary (actual DB role_code)
"Department"      → "DEPTHEAD"   ← legacy alias (kept for safety)
```

Both keys map to `"DEPTHEAD"`. Keeping `"Department"` means any cached JWTs that were issued using the old (broken) fallback still work after expiry. New logins pick up `"Department Head"` → `"DEPTHEAD"`.

**Complete mapping table (after fix):**

| DB `role_code`   | Normalized JWT role  |
|------------------|----------------------|
| Super Admin      | SUPERADMIN           |
| Admin            | ADMIN                |
| User             | USER                 |
| Supervisor       | SUPERVISOR           |
| Department Head  | DEPTHEAD (primary)   |
| Department       | DEPTHEAD (alias)     |
| Procurement      | PROCUREMENT          |
| Plant Manager    | PLANTMANAGER         |
| FloorIncharge    | FLOORINCHARGE        |
| DataEntry        | DATAENTRYOPERATOR    |
| GoodsIncharge    | GOODSINCHARGE        |
| GRNIncharge      | GRNINCHARGE          |
| IssueConfirm     | ISSUECONFIRM         |
| ReceiptConfirm   | RECEIPTCONFIRM       |
| QualityManager   | QUALITYMANAGER       |

---

#### FIX 2 — hasRoleByCode() callers (IndentService.java)

`hasRoleByCode()` queries raw DB `role_code` via `er.getRole().getCode()` with `equalsIgnoreCase`. All callers passing `"Department"` were changed to `"Department Head"`:

- `submitIndent()` DEPTHEAD bypass: `hasRoleByCode(empNum, "Department Head")`
- `approveIndent()` `hasDeptLevelAuth` check: `hasRoleByCode(empNum, "Department Head")`

Note: `"Plant Manager"`, `"Admin"`, `"Super Admin"` are correct (DB values match).

---

#### FIX 3 — ModuleAccessService.isAllowedByRole()

**Before:** Uppercase-only comparison on `module_default_roles` tokens.
```
"Department Head" → toUpperCase → "DEPARTMENT HEAD"  ≠  "DEPTHEAD"  → no match
```

**After:** Tokens normalized through `RoleNormalizer.normalize()` before comparing.
```
"Department Head" → normalize → "DEPTHEAD"  ==  "DEPTHEAD"  → match ✓
"ALL"             → "ALL" special case → universal grant ✓
```

JWT roles are already normalized, so no transformation needed on the user side.

---

#### FIX 4 — AuthContext.tsx hasAnyRole()

Added `ADMIN` to the universal bypass alongside `SUPERADMIN`:
```typescript
if (user.roles.includes('SUPERADMIN') || user.roles.includes('ADMIN')) return true;
```
ADMIN should have the same UI pass-through as SUPERADMIN for role-gated elements.

---

#### STEP 5 — canView read logic (verified, no change)

`AuthService.java` already reads `canView` as:
```java
"1".equals(r.getCanView()) || "true".equalsIgnoreCase(r.getCanView())
```
The string `"false"` does not match either condition → evaluates correctly to `false` for Supervisor. No code change needed.

---

**Files changed:**
- `backend/.../security/RoleNormalizer.java` — Fix 1
- `backend/.../indent/IndentService.java` — Fix 2
- `backend/.../moduleaccess/ModuleAccessService.java` — Fix 3
- `frontend/src/contexts/AuthContext.tsx` — Fix 4

**Build results:**
- `mvn compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — built in 34.22s, 0 errors (pre-existing chunk-size warning)

---

## 2026-07-01 (fifth entry)

### IndentDetailPage — Approve/Reject Buttons Not Showing for DEPTHEAD

**Commit:** `7bbe14a`

**Root causes (two, compounding):**

**Root cause A — Cached JWT (re-login required):**
Before commit `86da7be` (RoleNormalizer fix), the `"Department Head"` DB role fell to the fallback normalizer → `"DEPARTMENTHEAD"`. Any DEPTHEAD user who was already logged in has a cached JWT with `"DEPARTMENTHEAD"`, so `hasAnyRole(['DEPTHEAD'])` returns `false`. Fix: user must log out and log back in to receive a new JWT with `"DEPTHEAD"`. This is not a code change — it is a session invalidation requirement.

**Root cause B — canApprove used the wrong workflow signal (code bug):**
`canApprove` checked `statusId === STATUS_SUBMITTED (2)` for the DEPTHEAD condition. The main `status` column stays at `2` through BOTH the RM-pending stage (approvedStatus=1) AND the RM-approved stage (approvedStatus=3). This meant:
- DEPTHEAD saw the Approve button when indent was still waiting for RM (wrong — RM hasn't acted yet)
- SUPERVISOR was grouped with DEPTHEAD at `statusId===2`, so SUPERVISOR also saw the button on RM-approved indents that should only show for DEPTHEAD

**Fix — use `approvedStatusId` and `finalStatusId` from `IndentResponse`:**

`IndentResponse` already returns `approvedStatusId`, `finalStatusId`, and `procurementStatusId`. The fix reads those fields to detect the exact workflow stage, then gates each role to its correct stage:

| Stage | Condition | Roles that can act |
|-------|-----------|-------------------|
| L1 RM Review | `approvedStatusId=1` (pending RM) | SUPERVISOR, ADMIN, SUPERADMIN |
| L2 Dept Head | `approvedStatusId=3 AND finalStatusId=1` | DEPTHEAD, PLANTMANAGER, ADMIN, SUPERADMIN |
| Procurement | `finalStatusId=4 AND procurementStatusId=4` | PROCUREMENT, ADMIN, SUPERADMIN |

The approve button label was also corrected: L1 → "Approve (RM Review)", L2 → "Approve (Dept Head)", Procurement → "Procurement Approve".

**File changed:** `frontend/src/pages/indents/IndentDetailPage.tsx`

**Build results:**
- `tsc -b` — clean, 0 errors

---

## 2026-07-17 — Pass 1: Small UI Fixes

Four independent UI fixes shipped in one commit. No feature code deleted — dormant modules are hidden, not removed, and all backend endpoints remain intact.

### FIX 1 — Scroll to top on every route navigation

New `ScrollToTop` component (standard React Router pattern: `useLocation()` + `window.scrollTo(0, 0)` on `pathname` change). The app scroll container is the window (MainLayout.css defines no `overflow` container), so `window.scrollTo` is sufficient.

- **New:** `frontend/src/components/ScrollToTop.tsx`
- **Mounted in:** `frontend/src/components/layout/MainLayout.tsx` — first child of `.app-container`. MainLayout stays mounted across route changes and lives inside router context (`createBrowserRouter` → `RouterProvider`), so `useLocation()` is valid there.

### FIX 2 — Hide Plant Indent AND Confirmations from every role (code preserved)

Hidden from all roles including ADMIN/SUPERADMIN. Nothing deleted — re-enable by reverting these three changes.

- **Sidebar** (`frontend/src/components/layout/Sidebar.tsx`): added `hidden: true` to both the Plant Indent and Confirmations nav items (existing `roles`/`moduleCode` left intact). The nav filter already honors `!item.hidden`; `hidden: true` is the established hide pattern (precedent: `/masters/users`).
- **Routes** (`frontend/src/routes/router.tsx`): every Plant Indent route (`index`, `new`, `:id`, `:id/edit`) and Confirmations route (`index`, `issue`, `receipt`) now renders `<Navigate to="/dashboard" replace />`. Routes stay registered and page imports remain, so re-enabling is a one-line-per-route revert. (Note: `roles: []` was deliberately NOT used — an empty roles array makes `ProtectedRoute` skip its check and render children as PUBLIC, and a denial shows an "Access Denied" panel rather than redirecting.)
- **Migration** `backend/src/main/resources/db/migration/V57__deactivate_plant_indent_confirmations_modules.sql`: `UPDATE tbl_module_master SET module_status = 0 WHERE module_code IN ('PLANT_INDENTS','CONFIRMATIONS');` — deactivates at the module-access layer so nothing re-surfaces them.
- Backend `@PreAuthorize` annotations left unchanged, as specified.

### FIX 3 — Remove obsolete action buttons

- **Issue Notes list** (`frontend/src/pages/issue-notes/IssueNotesListPage.tsx`): removed the "Process Return" button (`FaUndo`) entirely, plus its now-unused import. The backend return endpoint is untouched.
- **Indents list** (`frontend/src/pages/indents/IndentsListPage.tsx`): the Edit button previously showed whenever Spring `status === 1`. Legacy rows carry `status = 1` as an active flag even at terminal states (PO Released `procurementStatusId=7`, Cash Buy `=9`), so Edit wrongly appeared there. Tightened to a genuine-draft guard: `status === 1 && approvedStatusId === 1 && finalStatusId === 1 && procurementStatusId === 1` (mirrors the detail page's draft gate), which inherently excludes procurement 7/9 and every other advanced stage.
  - **Backend prerequisite:** `IndentListResponse` did not return the three-column ids, so the list rows lacked `procurementStatusId` at runtime. Added `approvedStatusId`, `finalStatusId`, `procurementStatusId` to the record and populated them in `IndentService.toIndentListResponse(...)`. Additive change — no existing consumer affected.

### FIX 4 — Row click opens issue note detail

Mirrored the Indents-list pattern: added `onRowClick={(row) => navigate('/issue-notes/' + row.id)}` to the issue-notes `DataTable`. `DataTable` already applies `cursor: pointer` when `onRowClick` is set. Added `e.stopPropagation()` to the remaining action-column buttons (View, Edit) so they don't also trigger row navigation.

### Files changed

- `frontend/src/components/ScrollToTop.tsx` (new)
- `frontend/src/components/layout/MainLayout.tsx`
- `frontend/src/components/layout/Sidebar.tsx`
- `frontend/src/routes/router.tsx`
- `frontend/src/pages/issue-notes/IssueNotesListPage.tsx`
- `frontend/src/pages/indents/IndentsListPage.tsx`
- `backend/src/main/java/com/nslindia/procurezone/indent/dto/IndentListResponse.java`
- `backend/src/main/java/com/nslindia/procurezone/indent/IndentService.java`
- `backend/src/main/resources/db/migration/V57__deactivate_plant_indent_confirmations_modules.sql` (new)

### Migration

V57 — `V57__deactivate_plant_indent_confirmations_modules.sql` (to be run by the business owner per the standing no-DB-access constraint).

### Build results

- `mvn -DskipTests compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — built in 16.60s, 0 errors (pre-existing >500 kB chunk-size warning only)
- New build hashes: JS `assets/index-DWRGIhPe.js`, CSS `assets/index-C0ZtAKUb.css`

---

## 2026-07-23 — Pass 2: Dashboard Rebuild, List-Page Exports, Detail-Page Restructure

Three structural changes in one commit. No feature code deleted; two pre-existing export
visibility leaks were closed as part of FIX 2.

### FIX 1 — Dashboard rebuild

The dashboard was stripped to exactly three cards, in this order:
1. **Quick Actions** (existing) — promoted to the top.
2. **Latest Activity** (new) — the meaningful addition.
3. **Welcome back** (existing) — moved to the bottom.

**Elements removed** from `DashboardPage.tsx`: the two stat tiles ("Pending Approvals",
"Low Stock Alerts" StatCards), the role-based `DashboardSummary` widget row, the "Procurement
Trends" area chart and "Indent Status" pie chart (all recharts usage on this page), the standalone
"Low Stock Alerts" list card, and the "Recent GRN Activity" table. The `StatCard` component, sample
`trendData`, `statusDistribution`, `COLORS`, the recharts imports, and the `reportsApi`/`grnApi`
low-stock + recent-GRN queries were all deleted from the page. (The `DashboardSummary`/recharts
components themselves remain in the codebase for other pages.)

**Latest Activity — new endpoint:** `GET /api/v1/dashboard/latest-activity?limit=15`,
`@PreAuthorize("isAuthenticated()")` (per-role visibility is enforced inside the service).
Response — JSON array of:

    { "type": "INDENT" | "ISSUE_NOTE", "id": 123, "documentNumber": "...",
      "displayStatus": "...", "lastModifiedDate": "2026-07-23T10:15:00", "creatorName": "..." }

Each row is clickable to `/indents/{id}` or `/issue-notes/{id}`. The "Created By" column shows only
for roles that can see other people's documents (SUPERVISOR/DEPTHEAD/PLANTMANAGER/PROCUREMENT/
ISSUECONFIRM/ADMIN/SUPERADMIN); a plain USER does not see it. Default N = **15 mixed items**;
candidates fetched per type before filtering are capped at **100**.

**Visibility reuse:** `DashboardService.getLatestActivity()` does NOT re-implement scoping — it calls
the exact role-scoped list methods `IndentService.filterIndents(...)` and `IssueNoteService.getAll(...)`,
so the feed always agrees with the module list pages for the current user.

**Terminal-vs-non-terminal window:** applied in-memory after the scoped fetch rather than as a single
SQL predicate, because issue-note lmd (`issue_note_lmd`) is a `varchar(20)` persisted via
`VarcharDateTimeConverter` (mixed legacy formats) and its own contract forbids using it in a JPQL
range/sort. The boolean rule (identical for both types) is:

    include = !isTerminal(row)  ||  (lmd != null && lmd >= NOW() - 3 days)

where, for **indents** (`IndentListResponse` now carries the three workflow ids + lmd):

    isTerminal = approvedStatusId == 2         // RM Rejected
              || finalStatusId    == 2         // Dept. Head Rejected
              || procurementStatusId in (7, 9) // PO Released / Cash Buy

and for **issue notes** (`IssueNoteSummaryResponse` now carries lmd):

    isTerminal = approvedStatus == 2           // RM Rejected
              || storesByStatus in (2, 11)     // Stores Rejected / Goods Issued

In-flight (non-terminal) documents are always included; terminal documents drop off the dashboard
once their lmd is older than 3 days (they remain visible in the module list pages). Candidates are
ordered newest-modified-first (indents by the native `indent_lmd` column; issue notes by
`issue_note_date`), merged, sorted by lmd desc, and capped at N.

Backend: `DashboardActivityItem` DTO (new), `DashboardService.getLatestActivity`,
`DashboardController` endpoint, `lastModifiedDate` added to `IndentListResponse` +
`IssueNoteSummaryResponse` (+ their list mappers). Frontend: `dashboardApi.getLatestActivity`,
`DashboardPage.tsx` rewrite.

### FIX 2 — Export CSV/Excel on operational list pages

**Scope (agreed):** the core transactional + explicitly-named lists. The ~10 master lists, ~6 mapping
pages, admin/audit, and confirmation lists were audited and deferred to a follow-up (see inventory
below) to keep this commit proportionate.

**Two pre-existing visibility leaks fixed:** `IndentService.exportIndents` and
`IssueNoteService.exportAll` previously hit unscoped repository queries directly, so any authorized
caller could export **all** rows regardless of role. Both now route through the role-scoped list
methods (`filterIndents` / `getAll`) with a single large page (50,000-row cap), so an export returns
exactly what the caller sees in the filtered list. Both `/export` endpoints' `@PreAuthorize` were
relaxed from `ADMIN,SUPERADMIN` to the same role set as their list endpoints (if you can view the
list, you can export it), and filenames are now `"<resource>-<yyyy-MM-dd>.<csv|xlsx>"`.

**Endpoints — added or reused** (all format=csv|xlsx, same filter params + same @PreAuthorize as the
matching list endpoint, delegating to the same list service method, so no cross-role leak beyond what
the list itself exposes):
- `/indents/export` — reused; leak fixed, roles relaxed, filename dated.
- `/issue-notes/export` — reused; leak fixed, params aligned to list (search/approvedStatus/
  storesByStatus/departmentId), roles relaxed, now wired to the frontend.
- `/inventory/export`, `/employees/export`, `/pos/export`, `/grn/export`, `/vendors/export`,
  `/materials/export`, `/plant-indents/export` — **new**. Apache POI (poi-ooxml 5.2.5) was already a
  dependency; nothing added. Row cap 50,000.

**Frontend:** new reusable `components/common/ExportButtons.tsx` ("Export CSV" / "Export Excel" with
in-flight spinner, blob object-URL download). Wired into 8 pages with their live filter state:
Issue Notes, Inventory (stock view), Employees, Purchase Orders, GRN, Vendors, Materials
(replaced the old client-side-only CSV), Plant Indents. Indents already had working buttons (left
as-is). Note: downloads use a per-page filename base (the API returns a bare Blob, which carries no
`Content-Disposition`); the backend's dated filename applies to direct endpoint hits.

**Full list-page export inventory (audited):**
- *Already had backend + frontend export:* Indents, Reports (generic ReportsPage, Indent Report,
  Issue Note Report — Excel/CSV/PDF), Inventory Reports (Excel).
- *Export added/wired this pass:* Issue Notes, Inventory stock view, Employees, Purchase Orders,
  GRN, Vendors, Materials, Plant Indents.
- *Audited, deferred to follow-up (no export):* master lists (Companies, Plants, Departments,
  Sections, Locations, UOM, Roles, Users, Crops), mapping pages (Company-Dept, Company-Location,
  Company-Location-Material, Company-Plant-Material, Employee-Role, Reporting-Hierarchy),
  Email Templates, approval queues (Indent/Issue-Note Approval), QC lists (QC had client-side CSV
  only), Receipt/Issue Confirmation. Audit-log and QC-Rejected retain their existing client-side CSV.

### FIX 3 — Detail pages: remove info cards, promote items, add material's company

**Material to company resolution:** line items capture **no** company (both `IndentDetail` and
`IssueNoteDetails` reference only a material; company lives on the parent header). So each line
resolves to **all active companies stocking that material**, comma-separated, via a new
`CompanyPlantMaterialRepository.findCompanyNamesByMaterial(materialId)`
(`DISTINCT co.name ... status = 1 ORDER BY co.name`). Both detail mappers build a
`Map<Integer,String>` cache keyed by materialId so each distinct material hits the DB at most once.
A new `companies` field was added to `IndentDetailResponse` and the nested `IssueNoteDetailResponse`.

**Indent detail (`IndentDetailPage.tsx`):** removed the "Indent Information" card; the essential info
(number, date, creator, status) was already in the header line — comments/remarks moved into a new
"Additional Information" card. Promoted the #Items card directly under the header. Added a "Company"
column (next to Description) bound to `item.companies` ("—" when empty). Preserved: number, date,
creator, status, comments/remarks. Dropped from the visible header: company, department, plant,
section (delivery date still shown in the procurement summary). Approval-status timeline and
procurement/terminal action cards unchanged.

**Issue-note detail (`IssueNoteDetailPage.tsx`):** removed the "Issue Note Information" card; header
line already carried number/date/creator/status (status bar also shows Department/Plant/Total Items);
purpose/comments moved to a new "Additional Information" card. Promoted the Items card under the
header. Added the "Company" column (colspans adjusted). Dropped from the visible header: company,
section (department/plant remain in the status bar; "Issued By (Stores)" remains in the workflow
timeline). Workflow-status timeline and stores action cards/modals unchanged.

### Build results

- `mvn -DskipTests compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — built in ~90s, 0 errors (pre-existing >500 kB chunk-size warning only)
- New build hash: JS `assets/index-ajUyHFG9.js` (CSS unchanged `assets/index-C0ZtAKUb.css`)

---

## 2026-07-24 — Pass 2b: Export Button Visibility/Placement + Detail-Page Cleanup

Four small independent UI fixes in one commit. Frontend only — no backend, DTO, or migration changes.

### FIX 1 — Indent export buttons visible to all list-viewing roles

`IndentsListPage.tsx` gated the Excel/CSV export buttons to admins only.
- **Before:** `{hasAnyRole(['SUPERADMIN', 'ADMIN']) && ( … Excel/CSV … )}`
- **After:** `{hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'DEPTHEAD', 'PROCUREMENT', 'SUPERVISOR']) && ( … )}`
  (the same role set as the page's "New Indent" button — i.e. everyone who can view/act on the list).

**No data leak:** confirmed still true. Pass 2 fixed the export visibility leak so `/indents/export`
routes through the role-scoped `filterIndents` — a USER exports only their own indents, a SUPERVISOR
their team, a DEPTHEAD their department, etc. Making the buttons visible to all roles only lets each
role download the slice it can already see. A code comment to this effect was added at the gate.

### FIX 2 — Issue-note export buttons match the Indent list's placement + style

The Indent list renders export as two inline buttons in the `PageHeader` `actions` (top-right
toolbar), `d-flex gap-2`: **Excel** = `variant="outline-success" size="sm"` and **CSV** =
`variant="outline-secondary" size="sm"`, each with a `<FaFileExport className="me-1" />` icon, sitting
left of the primary "New" button.

`IssueNotesListPage.tsx` previously rendered export via the shared `ExportButtons` component down in
the search/filter toolbar. Updated to match the Indent list **identically**: removed `ExportButtons`
from the filter card, added a `handleExport('xlsx' | 'csv')` mirroring the Indent blob-download
handler, and placed the same two inline buttons (same variants/icon/size/`gap-2` spacing) in the
`PageHeader` `actions`, left of "Create Issue Note". Export is shown to every role that can view the
list; the backend export remains role-scoped via `getAll`. (`FaFileExport` imported; the
`ExportButtons` import dropped from this page — the shared component is still used by the other
operational list pages.)

### FIX 3 — Indent detail: consolidate the redundant card into the Status and Actions Bar

Immediately below the Status and Actions Bar, a second (terminal-summary) card showed a colour
status badge (redundant with the Status field already in the bar), the procurement status, PO#, and
the delivery date.
- **Removed** the redundant status badge entirely.
- **Moved up** into the Status and Actions Bar (as labelled segments alongside Status/Items, shown
  only at the terminal procurement stage — same condition as before, `showTerminalSummary`):
  **Procurement** (`PO Released` / `Cash Buy`, with `— PO# …` when PO Released) and **Delivery**
  (the delivery date, when present).
- **Deleted** the now-empty second `<Card>` block cleanly (the whole `{showTerminalSummary && (…)}`
  card was removed — no ghost padding, no empty div left behind).

### FIX 4 — Remove the "History" tab from both detail pages

Both `IndentDetailPage.tsx` and `IssueNoteDetailPage.tsx` had a redundant History tab (a timeline
duplicating information already conveyed by the indent's **Approval Status** card and the issue
note's **Workflow Status** card).
- Removed the `<Tab eventKey="history">…</Tab>` panel from both (the pages keep a single **Details**
  tab under the existing `<Tabs>`).
- Removed the now-unused `FaHistory` import from both.
- **No history-fetch API call existed** — both History tabs were rendered purely from the
  already-loaded indent/issue-note object (status ids, created/approved names + dates). So nothing
  was removed on the API/service side and there are **no newly-dead backend endpoints**.
- The **Approval Status** flowchart (indent) and **Workflow Status** card (issue note) are untouched.

### Files changed
- `frontend/src/pages/indents/IndentsListPage.tsx` (FIX 1)
- `frontend/src/pages/issue-notes/IssueNotesListPage.tsx` (FIX 2)
- `frontend/src/pages/indents/IndentDetailPage.tsx` (FIX 3 + FIX 4)
- `frontend/src/pages/issue-notes/IssueNoteDetailPage.tsx` (FIX 4)

### Build results
- `mvn -DskipTests compile` — clean, 0 errors (no backend changes this pass)
- `tsc -b` — clean, 0 errors
- `vite build` — built in ~44s, 0 errors (pre-existing >500 kB chunk-size warning only)
- New build hash: JS `assets/index-C1RJ0X0y.js` (CSS unchanged `assets/index-C0ZtAKUb.css`)

---

## 2026-07-25 — Pass 3: Quantity Editing During Approval + Audit Trail

RM (indent L1) and DeptHead (indent L2) can now adjust line-item quantities while approving, and RM
can adjust them on issue notes. Every edit is captured in dedicated audit tables (who + when, no
reason field per business owner). Monotonic-decrease only, enforced server-side. Originals are never
overwritten.

### PART 1 — Migration V58 (`V58__quantity_edit_audit_trail.sql`)

Next number confirmed as V58 (V57 was the last; V56 was previously deleted). Creates:
- `tbl_indent_details_qty_audit` (audit_id, detail_id FK→tbl_indent_details.indent_details_id,
  stage 'RM'|'DEPTHEAD', old_quantity, new_quantity, edited_by, edited_at)
- `tbl_issue_note_details_qty_audit` (same shape, FK→tbl_issue_note_details.issue_note_details_id,
  stage 'RM')
- `issue_note_details_rm_qty DECIMAL(18,2) NULL` on `tbl_issue_note_details`

FK PK targets confirmed against the entities (`indent_details_id`, `issue_note_details_id`).
**Deviation noted:** the spec placed the new column `AFTER issue_note_details_requested_quantity`;
I used `AFTER issue_note_details_quantity` instead — in the *new* system the requester's original
lives in `issue_note_details_quantity` (the entity maps that column; `issue_note_details_requested_quantity`
is a legacy column the app doesn't map), and that column is guaranteed present, so the ALTER can't
fail on a missing anchor. Placement is cosmetic only. (Migration to be run on the server by the
business owner per the standing no-DB-access constraint.)

### PART 2 — Backend wiring + issue-note editing

New audit entities + repos: `IndentDetailQtyAudit` / `IndentDetailQtyAuditRepository`,
`IssueNoteDetailQtyAudit` / `IssueNoteDetailQtyAuditRepository` (each with
`findByDetailIdInOrderByEditedAtAsc` for one-query history). `IssueNoteDetails` gained an
`rmQuantity` field (`issue_note_details_rm_qty`). Shared history DTO
`common/dto/QuantityEditDTO(stage, oldQuantity, newQuantity, editedByName, editedAt)`.

**Indent (`IndentService.l1Approve`, `l2Approve`):** the dark-code adjustment logic was already
present; added (a) an explicit lower-bound check (see PART 3) and (b) audit writes. After setting a
line's `rmQuantity`/`deptQuantity`, an audit row is added **only when the new value differs** from
the previous stage's value — L1 compares against the original `indent_details_qty`; L2 against
`indent_details_rm_qty` (falling back to the original). Rows are collected and `saveAll`ed right
after the indent is saved (`edited_by` = approver emp-number, `edited_at` = now). The single
`indent_details_lmu`/`lmd` columns are kept but the audit table is now the authoritative history.
An approval with no quantity change writes no audit rows.

**Issue note (`IssueNoteService.rmApprove`):** `ApproveIssueNoteRequest` gained an OPTIONAL
`items: [{detailId, rmQuantity}]`. For each item the RM value is written to the new `rmQuantity`
column and an audit row is added if it differs from the requester's original. The original
`issue_note_details_quantity` is **never** overwritten — confirmed no code in the new system
overwrites it (the legacy in-place overwrite does not exist here); `rmApprove` previously touched no
quantity at all. If `items` is null/empty the note is approved with no quantity change and no audit
rows.

**Old smart-route endpoint:** `/approvals/indents/{id}/approve` (remarks-only) **stays** for
backward compatibility; the React UI's primary path now calls the direct `/indents/{id}/l1-approve`
and `/l2-approve` endpoints so quantity edits + audit are recorded.

### PART 2c — Response DTOs

`IndentDetailResponse` now also carries `currentEffectiveQuantity` (= deptQuantity ?? rmQuantity ??
quantity) and `quantityHistory: List<QuantityEditDTO>`. The nested
`IssueNoteResponse.IssueNoteDetailResponse` now carries `rmQuantity`, `currentEffectiveQuantity`
(= rmQuantity ?? quantity), and `quantityHistory`. Both detail mappers build the history in one
query per document (audit rows grouped by detail id, editor emp-numbers resolved to names with a
per-call cache) and compute the effective quantity. `quantity` remains the requester's original.

### PART 3 — Monotonic-decrease validation (server-authoritative)

In the service layer (not relying on the frontend), each edited line must satisfy
`0 <= newQty <= previousStageQty`, else `IllegalArgumentException` with a per-line message
(→ 400, whole approval rejected, nothing partially applied):
- Indent L1: `0 <= rmQuantity <= quantity`
- Indent L2: `0 <= deptQuantity <= (rmQuantity ?? quantity)`
- Issue-note RM: `0 <= rmQuantity <= quantity`

Zero is allowed (an approver may zero out a line). The request DTOs' bean validation was changed
from `@Positive` to `@PositiveOrZero` so zero passes binding.

### PART 4 — Frontend editable items during your own turn

`IndentDetailPage.tsx` / `IssueNoteDetailPage.tsx`: a single `qtyEdits: Record<detailId, number>`
state, seeded from each line's `currentEffectiveQuantity ?? quantity`. Editable state reuses the
pages' EXISTING approval gating — no new role logic:
- Indent L1 turn: `canApprove && approvedStatusId === 1` → edits `rmQuantity`, `max = quantity`.
- Indent L2 turn: `canApprove && approvedStatusId === 3 && finalStatusId === 1` → edits
  `deptQuantity`, `max = rmQuantity ?? quantity`.
- Issue-note RM turn: existing `canRmAct` (status 2, approvedStatus 1, RM role) → edits `rmQuantity`,
  `max = quantity`.
When it is not the user's turn the cell is read-only, showing `currentEffectiveQuantity ?? quantity`.
On submit the approve mutation branches by stage: L1 → `indentsApi.l1Approve`, L2 →
`indentsApi.l2Approve` (fallback to the old remarks-only approve if no stage matches),
issue-note → `issueNotesApi.rmApprove` with `items`. Every editable line is sent; the backend audits
only real changes. Client-side `min/max` is convenience only — the server is authoritative.

### PART 5 — Quantity history display (Option B chosen)

Option B (icon + popover) — fits the existing Bootstrap table better than expandable rows. A small
`FaHistory` button renders next to a line's quantity **only when `quantityHistory.length > 0`**;
clicking opens an `OverlayTrigger`/`Popover` showing `Requested: {original}` then one line per edit
`→ {stage}: {newQuantity} (by {name}, {time})` (oldest-first). Lines never edited show nothing extra.

### Backend service methods modified
- `IndentService.l1Approve`, `IndentService.l2Approve` (validation + audit writes),
  `IndentService.toIndentResponse`/`toIndentDetailResponse` + new `buildIndentQtyHistory`.
- `IssueNoteService.rmApprove` (RM editing + validation + audit), `mapToResponse` + new
  `buildIssueNoteQtyHistory`.

### Build results
- `mvn -DskipTests compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — built in ~52s, 0 errors (pre-existing >500 kB chunk-size warning only)
- New build hash: JS `assets/index-DELmqxcs.js` (CSS unchanged `assets/index-C0ZtAKUb.css`)

---

## 2026-07-26 — Indent List Contamination Fix (DATA-EXPOSURE bug, now closed at source)

**Severity: data exposure.** Under specific unresolved-identity conditions, a non-admin
(USER/SUPERVISOR/DEPTHEAD/PLANTMANAGER) could receive the FULL unscoped indent list (~680 rows)
instead of their scoped subset. Reported as intermittent: correctly scoped right after login, but
all indents on reload/navigation, self-correcting on logout/login. Confirmed single backend instance
(no load balancer / stale replica), so the trigger is an in-process unresolved-identity edge, not a
mixed-build rollout. Fixed structurally so it is now impossible for a non-admin to receive unscoped
data, regardless of the exact trigger.

### Root cause (two layers)
1. **Backend fail-OPEN (the leak source).** `IndentService.filterIndents()` scoped USER/SUPERVISOR/
   DEPTHEAD only when `cu.employeeNumber() != null` and only when the principal was a `UserPrincipal`;
   any other case (null employee number, non-`UserPrincipal`, or no authentication) **fell through to
   the unscoped repository query returning ALL indents.** A silent fail-open.
2. **Frontend cache with no identity + 5-min staleTime (the amplifier).** The indent list query key
   was `['indents', searchParams]` — no user identity — with the global `staleTime: 5 * 60 * 1000`,
   cleared only on logout. So any unscoped payload that landed under that key was served across
   navigation for up to 5 minutes and self-healed on logout (`queryClient.clear()`).

### PART 1 — Backend: fail CLOSED (`IndentService.filterIndents`)
Restructured the visibility block. Behaviour for every valid role is unchanged
(ADMIN/SUPERADMIN → unscoped; PROCUREMENT → approvedStatus=3, finalStatus=4; DEPTHEAD/PLANTMANAGER →
own + 2 levels of reports; SUPERVISOR → own + direct subordinates; USER → own only). Only the edge
cases changed:

- **Before:** non-admin with null `employeeNumber`, a non-`UserPrincipal` principal, or no auth →
  fell through to `indentRepository.filterIndents(...)` **unscoped** (all rows).
- **After:**
  - no authentication / principal is not a `UserPrincipal` → `return Page.empty(pageable)` +
    `logger.error("filterIndents: no UserPrincipal in the security context ... returning EMPTY rather than unscoped data ...")`.
  - authenticated non-admin whose `employeeNumber()` is null → `return Page.empty(pageable)` +
    `logger.error("filterIndents: could not resolve an employee number for a non-admin principal (roles={}) ... returning EMPTY rather than unscoped data.")`.
  - ADMIN/SUPERADMIN unchanged (explicit `isGlobal` branch → unscoped, intended).

The two ERROR logs are deliberate: if the real trigger ever recurs in production it now appears
immediately in app.log (with the roles/authentication context) instead of silently degrading into a
data leak. This is how the true root trigger will be caught if it happens again.

Callers re-verified (Rule 3): `IndentController` list endpoint and `IndentService.exportIndents`
(bulk export) both route through `filterIndents`; both now also fail closed for an unresolved
non-admin identity — export can no longer leak the full dataset either.

### PART 2 — Frontend: identity-aware cache + always revalidate
- `IndentsListPage.tsx`:
  - **Before:** `queryKey: ['indents', searchParams]`, global `staleTime` 5 min.
  - **After:** `queryKey: ['indents', user?.employeeNumber, user?.roles, searchParams]`, plus
    `staleTime: 0` and `refetchOnMount: 'always'` on this query.
- `IssueNotesListPage.tsx` — confirmed it had the SAME latent vulnerability (`['issue-notes', page, pageSize, filters]`, no identity, global 5-min stale). Same fix applied:
  - **After:** `queryKey: ['issue-notes', user?.employeeNumber, user?.roles, page, pageSize, filters]`,
    `staleTime: 0`, `refetchOnMount: 'always'`.
- Identity in the key makes it impossible to serve one identity's result to another; `staleTime: 0` +
  `refetchOnMount: 'always'` force a fresh, correctly-scoped fetch on every mount/navigation rather
  than trusting a stale entry. Together with the backend fix, contamination is now impossible from
  either layer.
- **Clear-cache-on-login:** already satisfied — `AuthContext.login()` calls `clearAllSession()` as its
  first step, and `clearAllSession()` runs `queryClient.clear()`. No change made (would have been a
  redundant duplicate).

### Files changed
- `backend/src/main/java/com/nslindia/procurezone/indent/IndentService.java` — `filterIndents` fail-closed + ERROR logging
- `frontend/src/pages/indents/IndentsListPage.tsx` — identity-aware key + staleTime:0 + refetchOnMount
- `frontend/src/pages/issue-notes/IssueNotesListPage.tsx` — same
- (`AuthContext.tsx` — no change; login already clears the query cache)

### Build results
- `mvn -DskipTests compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — built in ~27s, 0 errors (pre-existing >500 kB chunk-size warning only)
- New build hash: JS `assets/index-BwSFYK0f.js` (CSS unchanged `assets/index-C0ZtAKUb.css`)

### Verification done (Rule 7 — not just "it compiled")
Traced every branch of the rewritten `filterIndents`: valid roles produce identical repository calls
to before (manual line-by-line comparison of the ADMIN/PROCUREMENT/DEPTHEAD/SUPERVISOR/USER paths);
only the two edge cases now return `Page.empty` + ERROR log instead of the unscoped query. Not
runtime-verified against a live DB (no DB access); the ERROR logging is in place precisely so a live
recurrence is observable. Migration N/A (no schema change).

## 2026-07-27 — Root Cause A + B fixes + 6 UI/data fixes

Six independent fixes in one commit (scoped per Rule 4, reported separately). Follows the earlier
investigations that traced these to pre-Pass-3 commits.

### PART 1 — DEPTHEAD denied "New Indent" (symptom 4)
- `frontend/src/routes/router.tsx` `/indents/new` guard:
  - **Before:** `['SUPERADMIN','ADMIN','USER','SUPERVISOR']`
  - **After:** `['SUPERADMIN','ADMIN','USER','SUPERVISOR','DEPTHEAD']`
  - PROCUREMENT intentionally NOT added (business rule: procurement never creates indents).
- **Broader list↔new guard audit (Rule 3):**
  - Indent: list `[SA,A,USER,DEPTHEAD,PROCUREMENT,SUPERVISOR]` vs new (now) `[SA,A,USER,SUPERVISOR,DEPTHEAD]`. Only role that can view-but-not-create = **PROCUREMENT** — intentional. Fixed: DEPTHEAD.
  - Issue Note: list `[SA,A,USER,ISSUECONFIRM,DEPTHEAD,SUPERVISOR,PROCUREMENT]` vs new `[SA,A,USER,SUPERVISOR,DEPTHEAD]` (already had DEPTHEAD). View-but-not-create = ISSUECONFIRM, PROCUREMENT — intentional downstream roles. No change needed.
  - No other gap found.

### PART 2 — DEPTHEAD denied submitting an issue note (symptom 6)
- `backend/.../issuenote/IssueNoteController.java` `/{id}/submit`:
  - **Before:** `@PreAuthorize("hasAnyRole('USER','ADMIN','SUPERADMIN','SUPERVISOR')")`
  - **After:** `@PreAuthorize("hasAnyRole('USER','SUPERVISOR','DEPTHEAD','ADMIN','SUPERADMIN')")` — aligned with the create endpoint (DEPTHEAD could create a draft but then AccessDenied on submit).
- **Broader create↔submit audit (Rule 3):** indent `POST /indents` = `isAuthenticated()` and `/{id}/submit` = `isAuthenticated()` — no narrowing gap for indents (any authenticated creator can submit). No change needed. Issue-note create already includes DEPTHEAD; only submit was the gap.

### PART 3 — Dashboard Quick Actions: add DEPTHEAD, hide card when no actions (symptom 5)
- `frontend/src/pages/dashboard/DashboardPage.tsx`:
  - Added `DEPTHEAD` to both button gates.
    - Create Indent: before `[SA,A,PLANTMANAGER,USER,SUPERVISOR]` → after `[...,'DEPTHEAD']` (derived flag `canCreateIndent`).
    - Create Issue Note: before `[SA,A,PLANTMANAGER,USER,ISSUECONFIRM,SUPERVISOR]` → after `[...,'DEPTHEAD']` (`canCreateIssueNote`).
  - Card render: **Before** the `<Card>` was always rendered (ungated). **After** it renders only when
    `hasAnyQuickAction = canCreateIndent || canCreateIssueNote`. Derived generically (no hardcoded
    "hide for PROCUREMENT"). Confirmed: **PROCUREMENT resolves both flags false → no Quick Actions card at all.**

### PART 4 — Indent detail: remove Est. Value, restructure Quantity into sub-columns
- `frontend/src/pages/indents/IndentDetailPage.tsx` items table:
  - Removed the **"Est. Value (₹)"** column (header + per-row cell). It was frontend display only.
    Confirmed no calculation breaks: the page's `totalValue` reduce is computed independently from
    `item.pricing` and is not rendered (pre-existing dead calc); no visible total-value summary
    depends on the removed column.
  - Replaced the single **"Qty"** column (with its Pass-3 click-popover history icon) with a grouped
    header **"Quantity"** over three sub-columns: **Requested | RM | Dept Head**.
    - Requested = `quantity` (original), always read-only.
    - RM = `rmQuantity ?? quantity`; editable `<input>` only on the L1 turn (`isL1Turn`, max=original), read-only otherwise.
    - Dept Head = `deptQuantity ?? rmQuantity ?? quantity`; editable only on the L2 turn (`isL2Turn`, max=`rmQuantity ?? quantity`), read-only otherwise.
    - On a read-only, already-adjusted value, an **inline tooltip** (react-bootstrap `Tooltip`) shows
      the editor name + timestamp, sourced from the same `quantityHistory` audit data (icon+popover
      replaced by per-sub-column inline tooltip; underlying data unchanged).
  - **Logic unchanged:** the single `qtyEdits` state + `l1Approve`/`l2Approve` submit flow from Pass 3
    is untouched — the editable input simply renders in the RM or Dept Head sub-column depending on
    whose turn it is. This is a display restructure only. Removed the now-unused `historyIcon`/`Popover`/`FaHistory`
    and the dead `isQtyEditable` local.

### PART 5 — Issue note creation: remove duplicate per-line Purpose
- **Decision: column NOT dropped.** Codebase search for the per-line column
  `issue_note_details_purpose` (added in migration V36): written at `IssueNoteService.createIssueNote`,
  **read** at `IssueNoteService.mapToResponse` → exposed as `IssueNoteDetailResponse.purpose`, and
  **displayed** on the issue-note DETAIL page items table ("Purpose" column, `{item.purpose || '-'}`).
  No report/export references. Because it IS used elsewhere (detail display + response DTO), per the
  task rule the column stays (nullable/deprecated).
- What changed (`frontend/src/pages/issue-notes/IssueNoteFormPage.tsx`, frontend only):
  - Removed the per-line **Purpose** input + its `<th>` from the "Materials to Issue" table.
  - Stopped sending per-line `purpose` in both create/save payload builders (`lineItems.map` no longer
    includes `purpose`). The header-level Purpose (Additional Information card, `data.purpose`) is kept.
  - Backend, entity, DTO, migration, and the detail-page display of existing per-line purposes are all
    untouched (existing data still shows; new notes simply leave the per-line purpose null).

### PART 6 — Issue note detail: Company column + remove Plant
- **Company column (empty) — investigated, NO code discrepancy found.** The issue-note path is
  byte-for-byte identical to the working indent path:
  - Both resolve via the same `CompanyPlantMaterialRepository.findCompanyNamesByMaterial(materialId)`
    and the same private `resolveCompaniesForMaterial(...)` (identical bodies in IndentService and
    IssueNoteService).
  - Issue-note `mapToResponse` populates `IssueNoteDetailResponse.companies` at the correct record
    position via `resolveCompaniesForMaterial(d.getMaterialId(), …)`; `d.getMaterialId()` is a valid
    material-master id (its `materialCode`/`materialName` resolve and display correctly on the page).
  - `getById` returns this record DTO; the frontend reads `item.companies` directly (no re-mapping).
  - **Conclusion:** the empty column is NOT a code bug — it is data-driven: the specific issued
    materials have no active (`status = 1`) rows in `tbl_map_company_plant_material`. Per Rule 6/7 I did
    NOT fabricate a code change. **Recommended verification (business owner runs, per no-DB-access
    rule):** `SELECT material_id, company_id, status FROM tbl_map_company_plant_material WHERE material_id IN (<materials on a test issue note>);`
    — if those materials have no status=1 rows, the fix is data (add the mappings), not code.
- **Plant removed from Status and Actions Bar** (`IssueNoteDetailPage.tsx`):
  - **Before:** Status | Plant | Total Items. **After:** Status | Total Items. (Plant remains in the
    data/DB; only removed from this display location.)

### Files changed
- `frontend/src/routes/router.tsx` (P1)
- `backend/.../issuenote/IssueNoteController.java` (P2)
- `frontend/src/pages/dashboard/DashboardPage.tsx` (P3)
- `frontend/src/pages/indents/IndentDetailPage.tsx` (P4)
- `frontend/src/pages/issue-notes/IssueNoteFormPage.tsx` (P5)
- `frontend/src/pages/issue-notes/IssueNoteDetailPage.tsx` (P6b)
- (P6 Company: no file changed — no code discrepancy)

### Build results
- `mvn -DskipTests compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — built in ~20s, 0 errors (pre-existing >500 kB chunk-size warning only)
- New build hash: JS `assets/index-DLlXiRi9.js` (CSS unchanged `assets/index-C0ZtAKUb.css`)

### Verification (Rule 7 — beyond "it compiled")
Traced each change: role gates only widened (no existing access removed); the P4 restructure keeps the
Pass-3 `qtyEdits`/`l1Approve`/`l2Approve` submit path unchanged (verified the editable input still binds
`qtyEdits` and routes to RM at L1 / Dept Head at L2); P3 card correctly hides for a zero-action role.
Not runtime-verified against a live DB (no DB access). P6 Company left as a data check for the owner.

## 2026-07-27 — Part 4 fixes: DeptHead bypass routing + Supervisor self-approval SQL crash

Implements the plan in docs/bypass-regression-investigation.md. Backend-only (no frontend change).

### FIX A — DeptHead bypass now routes to Procurement + uses normalized role check
`IndentService.submitIndent()`, the DEPTHEAD auto-approval block.

**Before:**
```java
boolean isDeptHead = hasRoleByCode(currentUser.getEmpNumber(), "Department Head"); // raw role-code string
boolean hasSupervisor = employeeReportingRepository.hasSupervisor(currentUser.getEmpNumber());
if (isDeptHead && !hasSupervisor) {
    // copy qty -> rmQuantity
    indent.setApprovedBy(currentUser);
    indent.setApprovedByDate(now);
    indent.setApprovedStatus(ref 3);            // ONLY approvedStatus set → lands at "RM Approved"
    indent.setRemarks("L1 Auto-approved (submitter is DEPTHEAD)");
}
```
**After:**
```java
var bypassAuth = SecurityContextHolder.getContext().getAuthentication();
boolean isDeptHead = bypassAuth != null
        && bypassAuth.getPrincipal() instanceof UserPrincipal bp
        && plantSecurityService.isDeptHead(bp);                 // normalized DEPTHEAD (roles() contains "DEPTHEAD")
boolean hasSupervisor = employeeReportingRepository.hasSupervisor(currentUser.getEmpNumber());
if (isDeptHead && !hasSupervisor) {
    // copy qty -> rmQuantity
    indent.setStatus(ref 3);                    // Dept Head Approved
    indent.setApprovedBy(currentUser); setApprovedByDate(now); setApprovedStatus(ref 3);
    indent.setFinalApprovedBy(currentUser); setFinalApprovedDate(now);
    indent.setFinalStatus(ref 4);               // NEW — Dept-Head auto-approved
    indent.setProcurementStatus(ref 4);         // NEW — arrived at Procurement
    indent.setRemarks("L1+L2 auto-approved (submitter is DEPTHEAD) — routed to Procurement");
}
```
Two defects fixed: (1) detection now uses the same normalized-role method (`PlantSecurityService.isDeptHead`,
which checks `principal.roles()` contains `DEPTHEAD`) as the rest of the codebase, instead of the raw
role-code string `"Department Head"` that could silently no-op after JWT normalization; (2) the full
three-column state is now set to **3 / 4 / 4** (approved / final / procurement) so
`deriveDisplayStatus(3,4,4)` = "Dept. Head Approved" (procurement stage), instead of stopping at
`approved=3` → "RM Approved" (Dept-Head queue).

**PLANTMANAGER check:** `submitIndent` has NO PLANTMANAGER bypass (only DEPTHEAD), so this change does
not affect PLANTMANAGER. Flag (not fixed — out of this scope, different method): `approveIndent`'s
`hasDeptLevelAuth` (IndentService ~818) still uses the raw-string `hasRoleByCode("Department Head"/"Plant
Manager"/…)` pattern and has the same latent fragility; recommend migrating it to normalized-role checks
in a follow-up.

### FIX B1 — reporting-chain CTE SQL error
`EmployeeReportingHierarchyRepository.findReportingChain()` final line.
- **Before:** `SELECT DISTINCT report_sup FROM hierarchy ORDER BY level`  ← fails under
  ONLY_FULL_GROUP_BY (`level` not in SELECT list, incompatible with DISTINCT).
- **After:** `SELECT report_sup FROM hierarchy GROUP BY report_sup ORDER BY MIN(level)` — de-duplicates
  `report_sup` and orders nearest-supervisor-first (MIN level), strict-SQL-mode safe. Same result
  semantics (distinct supervisors, immediate first).

### FIX B2 — allow a Supervisor to self-approve their own indent
`ReportingHierarchyService.canApproveFor(employeeEmpNumber, approverEmpNumber)` — added at the top:
```java
if (employeeEmpNumber != null && employeeEmpNumber.equals(approverEmpNumber)) {
    boolean isRm = hierarchyRepository.countSubordinates(approverEmpNumber) > 0;
    return isRm;   // self-approval valid only for an RM (has direct reports)
}
```
- Allows self-approval ONLY when the raiser IS an RM (has direct reports) — the Supervisor/RM case.
- A regular employee (no reports) still returns to the normal path → routes to their supervisor.
- The `employee == approver` equality guard means this can NEVER authorise approving someone else's
  indent. Normal (non-self) approvals skip this branch entirely (unchanged behaviour).

### Verification traces (Rule 7 — reasoned, not just compiled)
- **A — DeptHead (top of chain) raises + submits:** `submitIndent` → isDeptHead=true (principal role
  DEPTHEAD), hasSupervisor=false → bypass block → three-column set to approved=3, final=4,
  procurement=4 → `deriveDisplayStatus(3,4,4)` = "Dept. Head Approved" (procurement stage). Result
  state = **3/4/4**. ✓
- **B — Supervisor raises + self-approves:** indent lands approved=1 (no bypass — not a DeptHead) →
  "Pending RM Approval", Approve shown to self. Approve → `l1Approve` → `canApproveFor(self, self)` →
  self-branch: `countSubordinates(self) > 0` = true (a Supervisor has reports) → returns true →
  approval proceeds. The self-branch returns BEFORE `findReportingChain`, so the CTE isn't even hit
  for self-approval; no SQL exception. ✓
- **C — Supervisor approves a SUBORDINATE (normal, must be unaffected):** `canApproveFor(subordinate,
  supervisor)` → employee ≠ approver → self-branch skipped → existing `isSubordinateOf` / (now-fixed)
  `findReportingChain` path runs unchanged → true. Behaviour identical to before (plus the CTE no
  longer throws for deeper chains). ✓

### Files changed
- `backend/.../indent/IndentService.java` (FIX A)
- `backend/.../identity/EmployeeReportingHierarchyRepository.java` (FIX B1)
- `backend/.../identity/ReportingHierarchyService.java` (FIX B2)

### Build results
- `mvn -DskipTests compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — clean, 0 errors (backend-only change; frontend bundle unchanged: `assets/index-xJFjXSwf.js`)
- Not runtime-verified against a live DB (no DB access); traces above are code-level. Migration N/A.

## 2026-07-27 — B2 refinement: self-approval keyed on SUPERVISOR role, not subordinate count

Business decision: any SUPERVISOR may self-approve their own raised indent regardless of whether they
currently have subordinates assigned. `ReportingHierarchyService.canApproveFor()` self-branch:

- **Before:** `return hierarchyRepository.countSubordinates(approverEmpNumber) > 0;` (a derived signal —
  a SUPERVISOR with zero assigned subordinates would be wrongly denied).
- **After:** reads the normalized principal role (same pattern as FIX A's `isDeptHead`) —
  ```java
  var auth = SecurityContextHolder.getContext().getAuthentication();
  boolean isSupervisor = auth != null
      && auth.getPrincipal() instanceof UserPrincipal cu
      && cu.roles() != null && cu.roles().contains("SUPERVISOR");
  return isSupervisor;
  ```

**Principal/role context:** obtained via `SecurityContextHolder.getContext().getAuthentication()` inside
the method (the class had no principal access before — added here, mirroring FIX A in IndentService).
This is valid because in every caller (`IndentService.l1Approve`, `approveIndent`) `approverEmpNumber`
is the current authenticated user, so the SecurityContext principal IS the approver. If there is no
principal (e.g. a non-web context), it fails closed (returns false).

**Verification traces:**
- A) SUPERVISOR with ZERO subordinates self-approves → `canApproveFor(self,self)` → self-branch →
  `roles().contains("SUPERVISOR")`=true → allowed (previously failed under the count check). ✓
- B) SUPERVISOR WITH subordinates self-approves → same branch, role present → allowed (unchanged). ✓
- C) Plain USER never reaches `canApproveFor(self,self)`: the indent detail page gates the Approve
  button on `canApprove = awaitingL1 && hasAnyRole(['SUPERADMIN','ADMIN','SUPERVISOR'])` (or L2 roles),
  so a USER has no Approve button → never calls `l1Approve` → never hits this branch; their indent
  routes to their real RM. Defense-in-depth: even a direct API call would return false (USER lacks the
  SUPERVISOR role). ✓
- D) SUPERVISOR approving a SUBORDINATE's indent (employee ≠ approver) → self-branch skipped entirely →
  existing `isSubordinateOf` / `findReportingChain` path → unchanged. ✓

Note (flag, not changed): the branch checks SUPERVISOR only, per the business decision. An ADMIN/
SUPERADMIN who lacks the SUPERVISOR role self-approving their own indent would return false here; not
in scope — raise separately if global roles should also self-approve. `countSubordinates` remains used
by `hasSubordinates()` elsewhere (no dead code).

**File changed:** `backend/.../identity/ReportingHierarchyService.java`.
**Build:** `mvn compile` clean, `tsc -b` clean, `vite build` clean (backend-only; bundle unchanged
`assets/index-xJFjXSwf.js`).

## 2026-07-27 — DeptHead bypass (still not firing) + issue-note qty sub-columns + Dept-Head-column blank-until-acted

### PART 1 — Root cause + fix: DeptHead bypass still not firing

**Full trace (runtime path, per Rule 7):**
- STEP 1 — the "Direct to Procurement" popup (`IndentFormPage.tsx:59`) fires on
  `isDeptHead = hasAnyRole(['DEPTHEAD','PLANTMANAGER'])` — role only, **both** DEPTHEAD and PLANTMANAGER,
  no other condition.
- STEP 2 — `PlantSecurityService.isDeptHead(principal)` reads `principal.roles()` (JWT-derived, set at
  login via RoleNormalizer) and matches `"DEPTHEAD"` only. Reads the JWT, not the DB — same source the
  popup uses.
- STEP 3/4 — `submitIndent()` runs on "Save & Submit" (frontend `doSaveAndSubmit` → create → `submit`
  → `POST /indents/{id}/submit` → `submitIndent`). The popup is a **pure frontend confirmation**; it
  sends nothing extra to the backend, so the backend must detect DeptHead status independently.
  `submitIndent` reads the principal from `SecurityContextHolder` on the same request thread — the
  principal IS present and correct.
- STEP 6 — verified the executed path is `submitIndent` (not a create-with-immediate-submit variant);
  Fix A was in the right method.

**Root cause = signal mismatch between popup and backend (they were NOT reading the same signal):**
the popup fires for `DEPTHEAD || PLANTMANAGER` unconditionally, but the backend bypass required
`isDeptHead` (**DEPTHEAD only**) **AND `!hasSupervisor`**. So two classes of account see the popup but
get no bypass → land at "Pending RM Approval":
1. a **PLANTMANAGER** (not covered by the DEPTHEAD-only check), and
2. a **DEPTHEAD who reports to someone** (`hasSupervisor == true`).
Either matches the reported symptom exactly. (Fix A's code-level trace only held for a DEPTHEAD with
no supervisor — the assumption that didn't match the live account.)

**Fix (`IndentService.submitIndent`)** — make the backend read the SAME signal as the popup:
- **Before:** `isDeptHead(DEPTHEAD only) && !hasSupervisor`
- **After:** `principal.roles().contains("DEPTHEAD") || principal.roles().contains("PLANTMANAGER")`,
  with the `!hasSupervisor` gate **removed** — identical to `hasAnyRole(['DEPTHEAD','PLANTMANAGER'])`.
- **⚠ Flagged behaviour change (Rule 10):** removed the `!hasSupervisor` gate and added PLANTMANAGER.
  This makes the backend honour the popup's unconditional promise. If the business actually wants a
  DeptHead-with-a-supervisor to still route through their RM, the popup itself must become conditional
  — flag for confirmation. As-is, popup and backend now agree.
- The bypass already sets the full three-column state 3/4/4 (from the earlier fix) so the indent
  routes to Procurement, and now also seeds `deptQuantity` (see Part 3).

### PART 2 — Issue Note quantity sub-columns (`IssueNoteDetailPage.tsx`)

Replaced the single "Quantity" column with a grouped **"Quantity"** header over two sub-columns
(issue notes have ONE adjustment stage — RM — since the flow is User → RM → Stores, no Dept Head):
- **Requested** = `item.quantity` (original) — always read-only.
- **RM** = editable numeric input when `canRmAct` (the existing RM-turn gate), else read-only
  `rmQuantity ?? original` with an **inline tooltip** (editor + timestamp) when an RM adjustment exists.
Footer now totals Requested and RM (effective). The Pass-3 click-popover history icon was replaced by
the per-column inline tooltip (matching the indent pattern); `Popover`/`FaHistory` imports dropped,
`Tooltip` added. **Submission path unchanged** — the input still binds the same `qtyEdits` state, and
`rmApproveMutation` still sends `items:[{detailId, rmQuantity}]` exactly as built in Pass 3.

### PART 3 — Dept Head quantity column now blank until a Dept Head acts

- STEP 1/2 — the backend mapper was already correct: `IndentDetailResponse.deptQuantity` = raw
  `detail.getDeptQuantity()` (null until an L2 action writes it); `currentEffectiveQuantity` carries the
  `dept ?? rm ?? original` fallback separately. So the bug was **frontend**: the "Dept Head" column
  rendered `deptDisplay = deptQuantity ?? rmQuantity ?? qty` — the same fallback used for effective qty
  — so it showed the RM value before the Dept Head had acted.
- STEP 3 — Fix (`IndentDetailPage.tsx`): the Dept Head column now renders **only `item.deptQuantity`**
  (blank "—" when null); the `deptQuantity ?? rmQuantity ?? qty` fallback is kept solely for the
  effective-quantity input seed, not for this column. RM column keeps `rmQuantity ?? original` (the RM
  starts from the request — unchanged, per spec).
- Backend consistency: the DeptHead/PlantManager submit bypass now also writes `deptQuantity`
  (= rmQuantity) since it IS an auto L2 approval, so a bypassed indent correctly shows a Dept Head
  value. **Confirmed:** an indent not yet acted on by a Dept Head shows the Dept Head column BLANK;
  once a Dept Head approves (adjusting or not) — or the bypass fires — it shows their value.

### Files changed
- `backend/.../indent/IndentService.java` — Part 1 bypass detection + Part 3 deptQuantity seeding
- `frontend/src/pages/indents/IndentDetailPage.tsx` — Part 3 Dept Head column blank-until-acted
- `frontend/src/pages/issue-notes/IssueNoteDetailPage.tsx` — Part 2 Requested/RM sub-columns

### Build results
- `mvn -DskipTests compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — clean, 0 errors (pre-existing >500 kB chunk warning only)
- New build hash: JS `assets/index-CbbcZYnm.js` (CSS unchanged `assets/index-C0ZtAKUb.css`)

### Verification (Rule 7) + data check still owed
- Traced Part 1 end-to-end (above). Part 1's live confirmation still needs the STEP-5 DB check the
  owner runs — but the fix now covers BOTH failure modes (PLANTMANAGER role + DEPTHEAD-with-supervisor)
  regardless of which the specific account hits.
- Part 3 traced: not-acted → deptQuantity null → column blank; l2Approve (adjust or not) writes
  deptQuantity → column shows it; bypass seeds deptQuantity → shows it.
- Not runtime-verified against a live DB (no DB access).

## 2026-07-27 — 8-item batch: search robustness, role/permission independence, quantity limits, supervisor bypass, dropdown fields, password field, UOM validation, detail-page stock columns

### PART 1 — Material search robustness (both creation forms)
Search is DUPLICATED inline in IndentFormPage + IssueNoteFormPage (no shared component); fixed both.
Root cause: NO debounce — the raw input drove the React Query key with `staleTime:30000`, so clearing
mapped to the cached empty-term result (stale results lingered) and rapid clear/retype could settle on
a stale cached response until a blur/refocus reset it. Fix: 300ms `debouncedSearch` (cleared/rescheduled
each keystroke; clearing resets synchronously), query keyed on the debounced term, `keepPreviousData`
to prevent flicker, per-term cache + current-key-only read as the out-of-order guard, and an `isFetching`
"Searching…" indicator.

### PART 2 — Workflow actions require the ACTUAL workflow role, not ADMIN/SUPERADMIN
Business model: module/list/export VISIBILITY stays ADMIN/SUPERADMIN-governed; workflow ACTIONS
(approve/reject, quantity-edit at L1/L2, RM approve) require the real workflow role. Holding ADMIN alone
no longer grants action rights.
- **Frontend gates (detail pages):**
  - IndentDetailPage `canApprove`: `(awaitingL1 && ['SUPERADMIN','ADMIN','SUPERVISOR']) || (awaitingL2 && ['SUPERADMIN','ADMIN','DEPTHEAD','PLANTMANAGER'])` → `(awaitingL1 && ['SUPERVISOR']) || (awaitingL2 && ['DEPTHEAD','PLANTMANAGER'])`. (isL1Turn/isL2Turn — hence the quantity-edit inputs — inherit this.)
  - IssueNoteDetailPage `canRmAct`: `['SUPERADMIN','ADMIN','DEPTHEAD','SUPERVISOR']` → `['DEPTHEAD','SUPERVISOR']`.
  - Left unchanged: `canEdit`/`canSubmit` (draft), `canIssue` (stores) — not L1/L2 approval actions.
- **Backend endpoint @PreAuthorize (ADMIN/SUPERADMIN removed):**
  - IndentController `/{id}/approve`, `/{id}/reject`, `/{id}/l2-approve`, `/{id}/l2-reject`:
    `DEPTHEAD or PLANTMANAGER or ADMIN or SUPERADMIN` → `DEPTHEAD or PLANTMANAGER`.
  - ApprovalController (smart-route) `/indents/{id}/approve`, `/reject`: dropped `ADMIN`/`SUPERADMIN`
    (kept DEPTHEAD/PLANTMANAGER/PROCUREMENT/SUPERVISOR).
  - IssueNoteController `/{id}/rm-approve`, `/{id}/rm-reject`: `SUPERVISOR,DEPTHEAD,ADMIN,SUPERADMIN` → `SUPERVISOR,DEPTHEAD`.
  - `l1-approve`/`l1-reject` stay `isAuthenticated()` — authority is enforced by `canApproveFor` (reporting-chain / SUPERVISOR-self), which already excludes ADMIN.
- **STEP 3 confirmed:** `canApproveFor` self-branch already checks `roles().contains("SUPERVISOR")` (B2) — no ADMIN — unchanged.
- **⚠ FLAGGED TRADEOFF (Rule 7):** an ADMIN/SUPERADMIN who is NOT also a SUPERVISOR/DEPTHEAD/PLANTMANAGER can
  no longer Approve/Reject or edit quantities on ANY indent/issue note — they keep full read/export
  visibility only. This removes the previous admin-override for unblocking stuck approvals. This is the
  intended business tradeoff. (Out of scope, left as-is: `final-approve` / `procurement-approve` — non-L1/L2
  stages — still allow ADMIN; and the deprecated manager-stage endpoints.)

### PART 3 — Supervisor-raised indent skips RM, lands in the Dept-Head queue
`submitIndent()` now reads the raiser's principal roles once and branches:
- DEPTHEAD/PLANTMANAGER → full bypass to Procurement (approved=3, final=4, procurement=4) — unchanged.
- **NEW: SUPERVISOR (and not DEPTHEAD/PLANTMANAGER)** → RM auto-approved only: copy qty→rmQuantity,
  set `approvedStatus=3`, leave `finalStatus=1` and `procurementStatus=1`, status stays 2. deriveDisplayStatus(3,1,1)
  = "RM Approved" → the indent appears in the Dept-Head queue (findDeptHeadQueueForCreators filters approved=3 && final=1).
  deptQuantity stays null (Dept Head hasn't acted → Dept Head column blank).
- **B2 self-approval + CTE-SQL fixes are UNTOUCHED (Rule 10):** `ReportingHierarchyService.canApproveFor`
  (SUPERVISOR self-approval) and `findReportingChain` (GROUP BY report_sup ORDER BY MIN(level)) remain in
  the code exactly as before. New Supervisor-raised indents simply won't reach the self-approval path
  (they're past RM at creation), but the path still works for any pre-existing "Pending RM Approval" indent.

### PART 4 — Removed monotonic quantity limits; sanity cap 0–99999 everywhere
- Backend: `l1Approve`, `l2Approve` (IndentService) and issue-note `rmApprove` — replaced the
  `0 <= qty <= previousStageQty` monotonic check with `0 <= qty <= 99999` (new `MAX_QUANTITY` constant in
  each service). Removed the issue-note creation "exceeds available stock" cap (stock is still validated at
  the Stores/issueGoods stage — flagged). Creation DTOs `IndentDetailRequest.quantity` and
  `CreateIssueNoteRequest.quantity`: `@Positive` → `@PositiveOrZero` + `@DecimalMax("99999")`.
- Frontend: creation-form quantity inputs `min="0.01"` → `min={0} max={99999}`; zod `.min(0).max(99999)`;
  removed the issue-note stock-violation cap. Detail-page approval-stage inputs: `max` → `99999` (both pages).

### PART 5 — Material dropdown: remove Code, add Description
Both forms: dropdown rows changed from `[code] name (desc?) / Company|Plant|Stock` to `name / description /
Company|Plant|Stock` — Code removed, Description added as its own line, company/plant/stock unchanged. Display-only.

### PART 6 — Employee "Temporary Password" reveal toggle
**No change needed.** The non-LDAP create "Temporary Password *" field (EmployeeFormPage) is already a plain
masked `type="password"` with NO show/hide toggle. The eye toggles elsewhere in the file belong to a separate
password-reset section (not this field, not in scope). Reported per Rule 6 rather than touching unrelated UI.

### PART 7 — UOM validation stuck red (Indent creation)
Root cause: the UOM `Form.Select` had a custom `onChange` after `{...register()}` that wrote the value via
`setValue(..., { shouldDirty: true })` WITHOUT `shouldValidate`, so selecting a UOM never re-ran validation —
the "UOM is required" error from a failed submit persisted and the selection appeared not to take. Fix: added
`shouldValidate: true` to the `setValue`, so selection clears the error immediately and the value submits.

### PART 8 — Detail-page Company + current-stock columns
- **8a Indent Company "not showing" — investigated, NO frontend/backend code discrepancy.** Backend
  `toIndentDetailResponse` populates `companies` via the same `resolveCompaniesForMaterial` resolver as issue
  notes; frontend sources `indent.details` and renders `item.companies || '—'` — structurally identical to the
  (working) issue-note page. Conclusion: the empty column is DATA-driven (the tested indent's materials lack
  active rows in tbl_map_company_plant_material), not code. No spurious change made.
- **Available Stock (indent) / Balance in Stores (issue-note):** new backend field per detail line —
  `IndentDetailResponse.currentStock` and `IssueNoteResponse.IssueNoteDetailResponse.storesBalance` — both
  resolved from `CompanyPlantMaterialRepository.sumQuantityByMaterial(materialId)`, the **aggregated** total of
  `map_quantity_stores` across all companies/plants (chosen over per-company breakdown: simpler, reuses the
  existing method, matches the dropdown's total). New columns added to both detail item tables (indent → 10
  cols, issue-note → 8 cols; grouped-Quantity headers + colSpans kept consistent). Frontend types gained
  `currentStock?`/`storesBalance?`.
  - "Reconciled effective available" stock: no such reconciliation feature exists — the figure shown is the
    raw aggregated map_quantity_stores (informational, read-only), as the task specified for that case.

### Files changed (15)
Backend: IndentService, IndentController, ApprovalController, IndentDetailRequest, IndentDetailResponse,
IssueNoteService, IssueNoteController, CreateIssueNoteRequest, IssueNoteResponse.
Frontend: api/indents.ts, api/issueNotes.ts, IndentDetailPage.tsx, IndentFormPage.tsx,
IssueNoteDetailPage.tsx, IssueNoteFormPage.tsx.

### Build results
- `mvn -DskipTests compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — clean, 0 errors (pre-existing >500 kB chunk warning only)
- New build hash: JS `assets/index-1SzRGnd3.js` (CSS unchanged `assets/index-C0ZtAKUb.css`)

### Verification (Rule 7) + flags
- PART 2 tradeoff (ADMIN loses workflow-action rights) and PART 4 issue-note-creation stock-cap removal are
  flagged above as deliberate consequences.
- PART 3: B2 self-approval + CTE fixes confirmed present and untouched.
- PART 8a Company: no code bug — data-driven (needs a DB check of tbl_map_company_plant_material for the tested
  materials).
- Not runtime-verified against a live DB (no DB access); traces/analysis are code-level.

---

## 2026-07-28 — password toggle, items-Company real-data re-trace, items hover-preview, quantity-tooltip cue

### PART 1 (correction) — Show/hide toggle ADDED to the non-LDAP "Temporary Password *" field
Earlier this field was reported as "no toggle needed". Corrected: the create-form Temporary Password
field (EmployeeFormPage, non-LDAP + create only) was a plain `type="password"`. Wrapped it in the EXACT
same pattern already used by the file's reset-password section — `InputGroup` + an `outline-secondary`
`Button` toggling `showTempPassword`, `FaEye`/`FaEyeSlash` icon, `type={showTempPassword ? 'text' : 'password'}`.
Kept react-hook-form `register('password', …)` intact; `InputGroup hasValidation` so the invalid feedback
still renders. Toggles both ways (hidden ⇄ visible).

### PART 2 (re-investigated with the real table) — Items "Company" column now reads the SAME source as the dropdown/Inventory
ROOT CAUSE (found by code trace, not another "data-driven" guess): there are TWO company-plant-material
sources —
  - `CompanyPlantMaterial` → **tbl_pz_map_company_plant_material** (repo `CompanyPlantMaterialRepository`)
  - `CompanyPlantMaterialMap` → **tbl_map_company_plant_material** (repo `CompanyPlantMaterialMapRepository`)
The material-search dropdown and the Inventory page read the **Map** repo/table (tbl_map…, `status IN (0,1)`,
relations `s.material`/`s.company`). But the detail-page **Company** column AND the current-stock/stores-balance
columns (added 2026-07-27) resolved from the **non-Map** repo/table (tbl_pz_map…) — a different, sparsely
populated table. That mismatch is why the Company column flip-flopped empty across earlier rounds.
FIX:
  - Added `findCompanyNamesByMaterial(materialId)` to `CompanyPlantMaterialMapRepository`
    (`SELECT DISTINCT co.name FROM CompanyPlantMaterialMap s JOIN s.company co WHERE s.material.id = :id AND s.status IN (0,1) ORDER BY co.name`) — same table/filter as the dropdown.
  - IndentService: swapped the injected `CompanyPlantMaterialRepository` → `CompanyPlantMaterialMapRepository`
    (only used at the 2 resolver call-sites); `resolveCompaniesForMaterial` and `resolveCurrentStock` now hit
    the Map repo.
  - IssueNoteService: `resolveCompaniesForMaterial` and `storesBalance` now call the already-injected
    `companyPlantMaterialMapRepository`; removed the now-unused `CompanyPlantMaterialRepository` field + import.
Result: detail-page Company (multi-company, comma-separated) and stock now match the dropdown/Inventory exactly.
OWNER VERIFICATION SQL (run on a known multi-company material to confirm the row set the code now returns):
  `SELECT map_material, map_comp, c.comp_name, map_plant, map_quantity_stores, map_status
   FROM tbl_map_company_plant_material m JOIN tbl_company_master c ON c.comp_id = m.map_comp
   WHERE map_material = <id> AND map_status IN (0,1);`

### PART 3 (new feature) — Items hover-preview on the Indent + Issue Note list pages
DTO: the list endpoints did NOT carry per-line data (only `detailsCount`/`lineItemCount`), so a compact
summary field WAS added (per the task's "else add a compact summary field" branch):
  - `IndentListResponse.ItemSummary { materialCode, materialName, uomCode, quantity }` + `List<ItemSummary> items`;
    populated in `toIndentListResponse` from `indent.getDetails()` (material/UOM relations already available in
    the read-only tx).
  - `IssueNoteSummaryResponse.ItemSummary { … }` + `List<ItemSummary> items`; populated in `mapToSummaryResponse`
    with a per-note **batch** `findAllById` for materials + UOMs (avoids a per-line N+1).
Frontend: new shared `components/common/ItemsPreview.tsx` — renders the count + an `FaListUl` icon whose
hover/focus opens a `Popover` listing every line (`code — name`, `qty uom`), scrollable, `stopPropagation` so
the icon click doesn't trigger the row-navigate. Wired into both list pages' existing "Items" column.
Added `items?` to the `Indent`/`IssueNote` list types + `IndentItemSummary`/`IssueNoteItemSummary` and barrel exports.

### PART 4 (UI) — Quantity tooltip trigger: dropped the dotted underline, added a subtle history glyph
Both detail pages' `withEdit(value, edit)` quantity-history tooltip trigger dropped
`textDecoration: 'underline dotted'`; the value is now followed by a small muted `FaHistory` glyph
(`fontSize: 0.7em`), `cursor: help` retained. Hover tooltip (stage / new qty / editor / timestamp) unchanged.

### Files changed (15: 5 backend, 9 frontend edits + 1 new)
BE: CompanyPlantMaterialMapRepository, IndentService, IndentListResponse, IssueNoteService, IssueNoteSummaryResponse.
FE: EmployeeFormPage, IndentsListPage, IssueNotesListPage, IndentDetailPage, IssueNoteDetailPage,
api/indents.ts, api/issueNotes.ts, api/index.ts, components/common/index.ts, + new components/common/ItemsPreview.tsx.

### Build
- `mvn -q -DskipTests compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — clean (pre-existing >500 kB chunk warning only). New JS `assets/index-DJIE7YZX.js`; CSS unchanged `assets/index-C0ZtAKUb.css`.

### Verification (Rule 7) + notes
- PART 2 is a genuine code fix (wrong repo/table), not a data assumption — traced via the dropdown's own JPQL
  (`InventoryService` → `searchForDropdownAllCompanies` on the Map repo). Live-DB confirmation left to the owner
  via the SQL above (no DB access here).
- PART 3 issue-note summary uses a per-note batch lookup (2 queries/note) rather than N+1; indent summary reuses
  the already-loaded detail relations. Acceptable for paginated list sizes; noted for future perf tuning.
- Not runtime-verified against a live DB; analysis/build are code-level.

---

## 2026-07-28 (follow-up) — Items hover-preview: name-only + company + qty-with-UOM, widened popover (no name clipping)

### Popover content — before → after (per line item)
- BEFORE: `<materialCode> — <materialName>` (code redundant with the dropdown; name `text-truncate`
  capped at 220px so long names were clipped) │ right column: `<quantity> <uomCode>`.
- AFTER: line 1 — **material NAME only** (full, `wordBreak: break-word`, wraps, never truncated) on the
  left; **quantity + UOM together** (`10 KG`) on the right. line 2 — **Company/companies** (comma-separated,
  muted, wraps) when present. Material code removed entirely.

### DTO change (`ItemSummary`, both records)
- BEFORE: `ItemSummary(materialCode, materialName, uomCode, quantity)`.
- AFTER:  `ItemSummary(materialName, companies, uomCode, quantity)` — `materialCode` REMOVED, `companies` ADDED.
- `materialCode` was safe to drop: grep confirmed the summary DTO's `materialCode` was consumed ONLY by
  `ItemsPreview.tsx` (the other `materialCode` fields live on unrelated interfaces — `IndentItem`,
  `IssueNoteDetail`, `IssueNoteItem` — untouched). Frontend `IndentItemSummary`/`IssueNoteItemSummary` and
  the shared `ItemsPreviewLine` were updated to match (drop `materialCode`, add `companies?`).
- `companies` is resolved by REUSING the existing `resolveCompaniesForMaterial(materialId, cache)` in each
  service (the Map-repo / tbl_map_company_plant_material resolver fixed earlier today) — no logic duplicated:
  - `IndentService.toIndentListResponse`: added a per-indent `companiesByMaterial` cache; each line's
    company resolved via that resolver.
  - `IssueNoteService.mapToSummaryResponse`: dropped the now-unused `materialCodeById` batch map; added a
    per-note `companiesByMaterial` cache + resolver call. Material name (batch `findAllById`) and UOM lookups
    retained.

### Sizing fix (name never clipped)
- Removed the `text-truncate` + `maxWidth: 220` cap on the name span (that cap WAS the clipping).
- Popover `maxWidth` 360 → **440** (well past Bootstrap's 276px default), body `minWidth: 280`,
  `maxHeight` 260 → 300 with vertical scroll for many lines.
- Name and company use `wordBreak: 'break-word'` → they WRAP to multiple lines rather than truncate, so a
  material name (or long multi-company list) of ANY length is shown in full. This makes the fix
  length-independent — it does not depend on knowing the single longest name.
- ⚠ Rule 7 honesty: I could not query the DB for the literal longest material name (no DB access). The
  wrapping approach is strictly stronger than sizing to one known maximum — no finite name length can clip.
  Owner can eyeball the longest name in a note's preview to confirm the visual.

### Files changed (7)
BE: IndentListResponse (ItemSummary), IssueNoteSummaryResponse (ItemSummary), IndentService
(toIndentListResponse), IssueNoteService (mapToSummaryResponse).
FE: components/common/ItemsPreview.tsx, api/indents.ts (IndentItemSummary), api/issueNotes.ts
(IssueNoteItemSummary). List pages (IndentsListPage/IssueNotesListPage) unchanged — they pass `items`
through and the shapes still match structurally.

### Build
- `mvn -q -DskipTests compile` — clean, 0 errors.
- `tsc -b` — clean, 0 errors.
- `vite build` — clean (pre-existing >500 kB chunk warning only). New JS `assets/index-Bign-_5C.js`;
  CSS unchanged `assets/index-C0ZtAKUb.css`.

### Verification (Rule 7) + notes
- Company reuse traced: both mappers now call the SAME `resolveCompaniesForMaterial` (Map repo) the detail
  page uses — company data is identical to the detail-page Company column and the dropdown/Inventory.
- Perf: the indent list mapper now resolves companies per distinct material (cached per indent) — adds
  company lookups to the list endpoint (issue-note list already resolved companies per note). Acceptable for
  paginated sizes; noted for future tuning.
- Not runtime-verified against a live DB; analysis/build are code-level.

---

## 2026-07-28 (follow-up 2) — Items Company = the SPECIFIC company selected at creation (not all companies) + description in hover popup

### STEP 1-2 — investigation result: PATH 2 (column missing AND value never sent)
Resolved the contradiction in project history definitively:
- **tbl_indent_details entity (IndentDetail):** columns were material, umo, qty, rm_qty, dept_qty, stock_aval,
  pricing, purpose, vendor, status, lmd, lmu — **NO company column.**
- **tbl_issue_note_details entity (IssueNoteDetails):** material, umo, quantity, rm_qty, rate, amount,
  quantity_stores, purpose, status, lmd, lmu — **NO company column.**
- **Create DTOs:** `IndentDetailRequest` and `CreateIssueNoteRequest.IssueNoteLineItem` had no companyId field.
- **Save paths:** `createIndent` / `updateIndent` set stockAvailable but never a company; `createIssueNote`
  builder set materialId/qty/stores but never a company.
- **Frontend:** the dropdown option carries companyId+companyName; `selectMaterial` DOES capture it into
  `itemCompanyMap[index]`, and stock flows through as `stockAvailable`/`quantityStores` — but the chosen
  **companyId was never included in the submit payload** (`transformFormData` / issue-note `lineItems.map`).
- **Verdict:** the "captures materialId+companyId+stockAvailable" note was WRONG about companyId. The selected
  company was captured on the client, discarded before save, and there was no column to hold it. The display
  therefore had to list ALL companies for the material. → PATH 2.

### Fix implemented (PATH 2, full stack)
1. **Migration `V59__line_item_selected_company.sql`** — adds nullable plain-INT columns (no FK, not
   back-filled): `tbl_indent_details.indent_details_company` and `tbl_issue_note_details.issue_note_details_company`.
2. **Entities** — `IndentDetail.companyId` (@Column indent_details_company + getter/setter),
   `IssueNoteDetails.companyId` (@Column issue_note_details_company; Lombok getter/setter + builder).
3. **Create DTOs** — added optional `Integer companyId` to `IndentDetailRequest` and
   `CreateIssueNoteRequest.IssueNoteLineItem` (Jackson-only construction, so backward compatible: old
   payloads deserialize companyId = null).
4. **Save paths** — `createIndent` + `updateIndent` now `detail.setCompanyId(detailReq.companyId())`;
   `createIssueNote` builder `.companyId(lineItem.companyId())`. (Issue notes have no PUT/update endpoint in
   the controller, so no update-path change was needed there.)
5. **Frontend send** — indent `transformFormData` and BOTH issue-note payload builders now send
   `companyId: itemCompanyMap[index]?.companyId ?? undefined` per line. Create-request types gained `companyId?`.
6. **Display — all four locations via one helper per service** (`resolveLineCompany(lineCompanyId, materialId, cache)`):
   if the line has a captured companyId → show THAT single company's name (indent via `entityManager.find(Company)`,
   issue-note via `companyRepository.findById`, both L1-cached in-tx); else **fall back to the existing
   multi-company resolver** `resolveCompaniesForMaterial` so pre-existing rows never show blank.
   - Indent detail Items card — `toIndentDetailResponse`
   - Indent list hover popup — `toIndentListResponse` ItemSummary
   - Issue Note detail Items card — `mapToResponse`
   - Issue Note list hover popup — `mapToSummaryResponse` ItemSummary

### Fallback behavior for pre-existing records
Rows created before V59 have companyId = null → `resolveLineCompany` returns the multi-company list exactly
as before (no visible change for old records). Every NEW indent/issue note created after this change shows the
single company the user selected. Legacy-safe by construction.

### Edit round-trip (prevents silent data-loss — flagged per Rule 9/10)
The indent **update** path does `details.clear()` + rebuild from the payload. Without care, editing a draft
whose lines weren't re-selected would resend companyId = null and wipe the captured company. Fixed by:
- Exposing `companyId` on `IndentDetailResponse` + `IssueNoteResponse.IssueNoteDetailResponse` (and the FE
  `IndentItem`/`IssueNoteDetail` types).
- On edit-load, both forms repopulate `itemCompanyMap` from the loaded line's `companyId` (+ `companies` as the
  name) so an unchanged line resends and preserves its company on save.
(Scoped to company only — I deliberately did NOT change the pre-existing behavior where stockAvailable isn't
repopulated on edit; that's out of scope. Reported, not silently altered — Rule 4.)

### STEP 4 — material description in the hover popup
Added `materialDescription` to both `ItemSummary` records, populated from the SAME Material already read for
the name (indent: `d.getMaterial().getDescription()`; issue-note: added `description` to the existing per-note
batch `findAllById` — no new resolver). Frontend `IndentItemSummary`/`IssueNoteItemSummary`/`ItemsPreviewLine`
gained `materialDescription?`; `ItemsPreview` renders it as an italic muted line under the name, above company.
Popup order now: **Name (+ qty·UOM on the same row) → Description → Company**.

### Files changed (18)
Migration (1): V59__line_item_selected_company.sql.
BE (9): IndentDetail, IssueNoteDetails, IndentDetailRequest, CreateIssueNoteRequest, IndentService,
IssueNoteService, IndentDetailResponse, IssueNoteResponse, IndentListResponse, IssueNoteSummaryResponse.
FE (6): api/indents.ts, api/issueNotes.ts, components/common/ItemsPreview.tsx, IndentFormPage.tsx,
IssueNoteFormPage.tsx (+ the two list pages already pass items through — unchanged).

### Build
- `mvn -q -DskipTests compile` — clean, 0 errors.
- `tsc -b` — clean, 0 errors.
- `vite build` — clean (pre-existing >500 kB chunk warning only). New JS `assets/index-BDHeUhpK.js`;
  CSS unchanged `assets/index-C0ZtAKUb.css`.

### Verification (Rule 7) + notes
- Each changed response DTO has exactly ONE constructor (all updated); grep confirmed no other positional
  callers would break (Rule 3/8).
- `resolveLineCompany` fallback path preserves the prior multi-company display for legacy rows — verified by
  code trace, not a live DB (no DB access).
- ⚠ The V59 migration MUST run before the app serves requests (Flyway auto-applies on startup); the entities
  now map the new columns, so an un-migrated DB would fail with "unknown column". Standard deploy order handles
  this.
- Not runtime-verified against a live DB; analysis/build are code-level.

---

## 2026-08-06 — Description column bug, quantity minimum reverted to >0, dashboard Latest-Activity staleness

### PART 1 — Items-card "Description" column now shows the material DESCRIPTION (was showing the name)
STEP 1 (data flow): `tbl_material_master` / `Material` entity has THREE distinct fields — `material_code`
(code), `material_name` (name), `material_desc` (description). But the detail response DTOs
(`IndentDetailResponse`, `IssueNoteResponse.IssueNoteDetailResponse`) exposed only `materialCode` +
`materialName` — **no materialDescription field at all**.
STEP 2 (rendering): the Items-card "Description" column was bound to `materialName`, so it showed the
material's NAME (which in this data often equals the code, hence the "shows code" report) — never the
`material_desc` description, which wasn't even in the DTO.
- **Fix — before → after:**
  - IndentDetailResponse / IssueNoteDetailResponse: **added `String materialDescription`** (after
    materialName), populated in the mappers from `material.getDescription()`
    (`toIndentDetailResponse`, `mapToResponse`).
  - FE types: `IndentItem.materialDescription` re-documented as the real field (was an "alias for
    materialName" comment); `IssueNoteDetail` **gained `materialDescription?`**.
  - IndentDetailPage Items row: Description cell `{item.materialName || item.materialDescription || 'N/A'}`
    → `{item.materialDescription || item.materialName || 'N/A'}` (prefer description; fall back to name so
    it's never blank).
  - IssueNoteDetailPage Items row: `{item.materialName || 'N/A'}` → `{item.materialDescription || item.materialName || 'N/A'}`.
  - Material Code column (`item.materialCode`) unchanged — it was already correct.
- Rule 8: each detail DTO has exactly ONE constructor (grep-confirmed); both updated.

### PART 2 — Quantity minimum reverted to strictly > 0 everywhere (max stays 99999)
System uses BigDecimal(20,2) quantities (decimals allowed) → frontend min = 0.01, step 0.01.
- **Backend (before → after):**
  - `IndentDetailRequest.quantity`: `@PositiveOrZero` → `@Positive` (message "must be greater than 0"); `@DecimalMax("99999")` kept.
  - `CreateIssueNoteRequest.IssueNoteLineItem.quantity`: `@PositiveOrZero` → `@Positive`; DecimalMax kept.
  - `IndentService.l1Approve` rmQuantity bound: `signum() < 0` → `signum() <= 0` (message "must be greater than 0 and at most 99999").
  - `IndentService.l2Approve` deptQuantity bound: `signum() < 0` → `signum() <= 0`.
  - `IssueNoteService.rmApprove` rmQuantity bound: `signum() < 0` → `signum() <= 0`.
- **Frontend (before → after):**
  - IndentFormPage: input `min={0}` → `min={0.01}`; zod `requestedQuantity.min(0,...)` → `.min(0.01,'Quantity must be greater than 0')`.
  - IssueNoteFormPage: input `min={0}` → `min={0.01}`; zod `quantity.min(0,...)` → `.min(0.01,...)`.
  - IndentDetailPage approval `qtyInput` (shared by L1 & L2): `min={0}` → `min={0.01}`.
  - IssueNoteDetailPage approval RM input: `min={0}` → `min={0.01}`.
- This exactly reverses the 2026-07-27 Part 4 lower-bound change; the 99999 upper bound is untouched.
  Grep confirmed no other quantity `min={0}` / `.min(0` remained (estimatedRate stays `.min(0)` — a rate, not a quantity).

### PART 3 — Dashboard "Latest Activity" staleness (INVESTIGATED + FIXED)
- **STEP 1 (current query):** `queryKey: ['dashboard','latest-activity']` (static, NOT identity-aware),
  `enabled: !!user`, and **no staleTime / refetchOnMount / refetchInterval** → inherits the global 5-minute
  staleTime. A revisit within 5 min serves the cached (stale) feed; a status change (RM/DeptHead
  approve-reject, creation) isn't reflected until that window lapses or a hard reload.
- **STEP 2 (comparison):** the Indent/Issue-Note list-page contamination fix uses
  `['<entity>', user?.employeeNumber, user?.roles, ...] + staleTime:0 + refetchOnMount:'always'`.
  That fix was **never applied to the dashboard query** → this is the same stale-cache class of bug.
  ROOT CAUSE: the dashboard trusted the global 5-min stale cache under a non-identity key.
- **STEP 3 (latency behavior):** requirement is "shown without any delay". The (revised) task resolved the
  ambiguity in favour of BOTH revalidate-on-mount AND short polling — no owner question needed.
- **STEP 4/5 (fix applied) — before → after** on the Latest Activity query:
  - key `['dashboard','latest-activity']` → `['dashboard','latest-activity', user?.employeeNumber, user?.roles]` (identity-aware)
  - added `staleTime: 0`
  - added `refetchOnMount: 'always'` (every visit revalidates)
  - added `refetchInterval: 30000` (an already-open dashboard self-refreshes every 30 s)
  - Safe per new Rule 11: the dashboard is a read-only display; no editable form is bound to `activity`.
- **STEP 6 (backend cache check):** GET /api/v1/dashboard/latest-activity → `DashboardService.getLatestActivity`
  reads LIVE each call (`indentService.filterIndents(...)` + `issueNoteService.getAll(...)`, both hit the
  DB), with **no @Cacheable / in-memory cache / Cache-Control headers**. Confirmed: the backend adds no
  delay; the latency was 100% the frontend stale cache. Dashboard scoping is unchanged (still reuses
  `filterIndents` / `getAll`) — matches list-page visibility as required.

### Rule 11 added to docs/DEVELOPMENT_RULES.md
"Never auto-refresh (refetchInterval) a query that feeds an editable form" — verbatim as specified.
- **refetchInterval audit (all uses):**
  - `DashboardPage.tsx:53` — Latest Activity (read-only display feed). SAFE.
  - `TopNavbar.tsx:45` — notifications unread-count badge (read-only counter). SAFE.
  - No refetchInterval on any creation/edit/form page. Audit passes.

### Files changed (13)
BE (5): IndentDetailResponse, IssueNoteResponse, IndentService, IssueNoteService, IndentDetailRequest,
CreateIssueNoteRequest — (6 actually). FE (6): api/indents.ts, api/issueNotes.ts, IndentDetailPage.tsx,
IssueNoteDetailPage.tsx, IndentFormPage.tsx, IssueNoteFormPage.tsx, DashboardPage.tsx — (7). Docs (1):
DEVELOPMENT_RULES.md (Rule 11).

### Build
- `mvn -q -DskipTests compile` — clean, 0 errors.
- `tsc -b` — clean, 0 errors.
- `vite build` — clean (pre-existing >500 kB chunk warning only). New JS `assets/index-DwBrodvt.js`;
  CSS unchanged `assets/index-C0ZtAKUb.css`.

### Verification (Rule 7) + notes
- PART 1: verified by tracing entity → mapper → DTO → FE binding; the description now flows end-to-end.
  Fallback to name prevents a blank column when material_desc is null.
- PART 2: @Positive rejects 0 and negatives at the DTO boundary; signum()<=0 rejects 0/negative at the
  approval stage; FE min=0.01 + zod .min(0.01) are convenience only (server is authoritative).
- PART 3: root cause is a code-level cache-config gap (traced against the list-page fix), fixed by matching
  that pattern + 30 s polling. Not runtime-verified against a live server (no DB/live access); analysis +
  green builds only.
