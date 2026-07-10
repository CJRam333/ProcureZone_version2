# SAP CSV Import & Stock Reconciliation — Investigation

**Date:** 2026-07-09
**Scope:** Investigation only. No changes. Determines the design of a stock-reconciliation feature.

## Headline finding (read first)

**The daily SAP CSV import does NOT write the table the app reads for stock.**
- The CSV import (`SapMaterialImportJob` → `MaterialImportService`) writes
  **`tbl_pz_map_company_plant_material`** (entity `mapping.entity.CompanyPlantMaterial`).
- Everything that reads stock — the material dropdown, the new read-only Inventory view, and the
  issue-note stock check — reads **`tbl_map_company_plant_material`** (entity
  `mapping.CompanyPlantMaterialMap`, `map_quantity_stores`).
- These are **two different tables** (`tbl_pz_map_...` vs `tbl_map_...`). So today the CSV import
  updates a table nobody reads for stock, and the real-stock table (`tbl_map_company_plant_material`,
  the 669-row legacy table with material 227=466) is **not** refreshed by the import at all.

Any reconciliation design must resolve this mismatch first — otherwise "reset the tally on import"
would anchor to a table (`tbl_pz_map_...`) that is disconnected from what users see.

---

## STEP 1 — The CSV import job

`scheduler/job/SapMaterialImportJob.java` (`@Component`, `@Scheduled`):
- **Three triggers, all Asia/Kolkata, all run the same `executeImport(...)`:**
  - `0 21 10 * * ?` → **10:21 AM** (Trigger 1)
  - `0 43 10 * * ?` → **10:43 AM** ("Material Quantity Insert")
  - `0 55 11 * * ?` → **11:55 AM** (Trigger 2)
  - (Gated by `sap.csv.import.enabled`, default true.)
- **File:** `Material(YYYY-MM-DD).CSV` (ISO date) in `${sap.csv.import.path}` = **`/opt/tomcat/uploads/issue`**.
- **File-not-found:** logs `WARN "CSV file not found: …"` and `return`s — **skips silently**, no error, no retry.
- **On success:** archives the file to `${sap.csv.archive.path}` = `/opt/tomcat/uploads/issue/archive`
  as `Material(date)_processed_<timestamp>.CSV`.

`integration/sap/service/MaterialImportService.java` (what it writes):
- Per CSV row: looks up Company by code, Plant by code; finds Material by code or **creates** it
  (code used as name+desc, legacy behavior); then upserts the mapping.
- **Target table/column:** `CompanyPlantMaterial` → **`tbl_pz_map_company_plant_material`**,
  column **`map_quantity_stores`** (via `setQuantityStores`).
- **UPDATE vs REPLACE vs INSERT:** per-row **upsert** — finds the mapping by
  (company, plant, material); if present **UPDATE**s `map_quantity_stores`, else **INSERT**s a new
  row. It does **not** delete/replace rows absent from the CSV. `setQuantityStores(quantity)` is an
  **absolute overwrite** (sets stock to the CSV value, not additive).
- **Import timestamp:** none dedicated. Only `map_lmd` (a `LocalDate`, per row) is stamped. The
  `MaterialImportResult` (rows, inserts, updates, errors) is returned in memory, **not persisted**.
  → **This job has no run-log / "last imported" marker.**

**Second, separate SAP path (not the CSV stock import):** `sap/scheduler/SapImportScheduler.java`
runs `0 30 1 * * ?` (**1:30 AM**) → `SapImportService` → writes the plant-indent SAP material master
(`SapScheduleMaterialRepository`) and **does persist a run-log** (`SapImportLog`, with `completedAt`,
`getLatestSuccessfulImport()`). This is a **different** feed (plant-indent material master), not the
`map_quantity_stores` stock table — but it's the only place in the codebase that already records a
reliable "last successful import" timestamp.

**Net: three material/stock stores are in play**
| Store | Written by | Has run-log? | Read for stock by app? |
|---|---|---|---|
| `tbl_map_company_plant_material` (legacy, real stock) | *(nothing in new app)* | — | **Yes** (dropdown, inventory view, issue check) |
| `tbl_pz_map_company_plant_material` | CSV job (10:21/10:43/11:55) | No | No |
| plant-indent SAP master (`SapMaterialMaster`) | 1:30 AM `SapImportService` | **Yes** (`SapImportLog`) | No |

---

## STEP 2 — File location & schedule (summary)
- **Directory scanned:** `/opt/tomcat/uploads/issue` (`sap.csv.import.path`).
- **Filename pattern:** `Material(YYYY-MM-DD).CSV` using **today's** date.
- **Times:** 10:21 AM, 10:43 AM, 11:55 AM IST (three triggers) for the CSV stock import; plus a
  separate 1:30 AM job for the plant-indent SAP master. (The log line you saw, "Trigger 1 (10:21 AM)",
  is the first of the three.)

---

## STEP 3 — `tbl_inventory_balance` structure
Empty in production. Cannot `DESCRIBE` from here (no DB access) — **business owner please run:**
```sql
DESCRIBE tbl_inventory_balance;
```
From the `domain/inventory/Inventory` entity the columns are: `inventory_id` (PK), material/plant/
company/uom FKs, `opening_balance`, `current_balance`, `reserved_quantity`, `available_quantity`,
`reorder_level`, `max_level`, `min_level`, `avg_rate`, `total_value`, `last_receipt_date`,
`last_issue_date`, `last_updated`, `created_at`, `updated_at`. It's a full per-material-per-plant
balance table (heavier than a simple "issued tally" needs).

---

## STEP 4 — How issues are recorded
`IssueNoteService.issueGoods()` (after the 2026-07-09 fix) writes:
- `tbl_issue_note`: `status=8`, `issue_note_storesby_status=11` (Goods Issued), `issue_note_storesby`
  (who), **`issue_note_storesby_date`** (when).
- `tbl_issue_note_details`: one row per material with **`issue_note_details_quantity`**.
- It **no longer writes `tbl_inventory_balance`** (the `deductStock` call was removed) and does **not**
  decrement `map_quantity_stores`.

**Queryable by date?** Yes. "Issued since <T>" per material =
`SUM(d.issue_note_details_quantity)` for issue notes where `storesByStatus = 11` and
`storesByDate >= T`, grouped by `issue_note_material`. No new per-issue record is needed — the issue
note + details already are the ledger.

---

## STEP 5 — Reconciliation feasibility & recommendation

**Goal (as I understand it):** show a *live* available stock = (last SAP import snapshot) − (quantity
issued via ProcureZone since that import).

1. **Can we hook the import to reset the issued tally?** Yes — cleanly, at the end of
   `MaterialImportService.importMaterials(...)` (it's `@Transactional`), or in the job's
   `executeImport` right after `result.isSuccess()`. That's the natural reset point.
   **But** (headline): the CSV import updates `tbl_pz_map_company_plant_material`, while stock is read
   from `tbl_map_company_plant_material`. Before any reconciliation is meaningful, decide/redirect so
   the import snapshot and the read source are the **same** table.

2. **Reliable "last import timestamp"?** Not for the CSV stock job — it persists nothing. Options:
   (a) reuse the existing `SapImportLog` pattern (only populated by the 1:30 AM master job today), or
   (b) create a tiny marker (one row: `last_stock_import_at`). Recommend **(b)** a dedicated marker
   written by the CSV job on success — simplest and unambiguous for stock.

3. **Storage for the issued tally — recommended: don't store a tally at all; compute on the fly.**
   Since issues are already a dated ledger (STEP 4), the live view can be:
   `available = snapshot_stock(material) − SUM(issued since last_stock_import_at)`.
   This needs only a **last-import timestamp marker** — no tally table to maintain, no decrement to
   keep in sync, and it self-corrects each import. If a materialized tally is preferred for
   performance, a **new small table** (`material_id, company_id, plant_id, issued_qty, reset_at`) is
   cleaner than repurposing the heavy `tbl_inventory_balance`.

**Recommended design (for a later task):**
- Resolve the table mismatch: point the CSV import at `tbl_map_company_plant_material` (the read
  source) — or point the reads at the pz table — so import and reads agree. *(Decision needed.)*
- Add a `last_stock_import_at` marker, set by the CSV job on success.
- Compute live available stock on the fly = imported snapshot − Σ(issued since marker), reusing the
  issue-note details ledger. No decrement of `map_quantity_stores` on issue (consistent with the
  2026-07-09 decision that SAP is authoritative for physical stock).

**Open decisions for the business owner:**
1. Which table is authoritative for stock the app reads — `tbl_map_company_plant_material` (current
   reads, 669 rows) or `tbl_pz_map_company_plant_material` (current CSV import target)? They must be
   unified.
2. Should the daily CSV actually be landing in `/opt/tomcat/uploads/issue` and updating the
   read-source table? (Today it updates the pz table and, if the file is absent, silently skips.)
3. Confirm `DESCRIBE tbl_inventory_balance` and whether it should be retired or repurposed.

---

# Addendum (2026-07-10) — Legacy authoritative table & import path confirmed

Decompiled the legacy issue-note SAP import (`javap` on the compiled classes; no `.java` source ships).

## STEP 1 — Authoritative stock table: `tbl_map_company_plant_material`

Legacy import `seeds/issue/mailService/SapCsvImport` writes stock via:
- pojo **`pojo.TblMapCompanyPlantMaterial`** → mapped (hbm.xml) to table
  **`tbl_map_company_plant_material`** (catalog `seeds_indent`), column **`map_quantity_stores`**.
- Calls `setMapQuantityStores(...)` then `CompPlantMaterialDaoImpl.save(...)` — per-row upsert
  (looks up Company/Plant/Material by code, updates the mapping's quantity).
- The `Pz` pojo (`pojo.TblPzMapCompanyPlantMaterial` → `tbl_pz_map_company_plant_material`) is a
  **separate** table used by the plant-indent SAP feed, **not** the issue-note stock import.

The issue-note dropdown/stock reads use the same `tbl_map_company_plant_material` (the new system was
already pointed there — 669 rows, material 227=466). **End-to-end, legacy is
`tbl_map_company_plant_material`.** Confirmed authoritative.

## STEP 2 — Legacy import file path
`SapCsvImport` builds the path literally as **`/home/issuenote/issue/Material(<date>).CSV`**
(string constants `"/home/issuenote/issue/Material("` + date + `").CSV"`). This matches the real
production path the business owner confirmed (`/home/issuenote/issue/Material`). Date is the run
date; the earlier new-system log line `Material(2026-07-01).CSV` shows ISO `yyyy-MM-dd` — confirm the
exact separator against a real production file.

## STEP 3 — New system's `SapMaterialImportJob` diverges on all three axes
| Axis | Legacy (authoritative) | New `SapMaterialImportJob` | Divergent? |
|---|---|---|---|
| **File path** | `/home/issuenote/issue/Material(<date>).CSV` | `/opt/tomcat/uploads/issue/Material(<date>).CSV` | **Yes** — never finds the prod file |
| **Target table** | `tbl_map_company_plant_material` | `tbl_pz_map_company_plant_material` | **Yes** — writes the wrong (pz) table |
| **Schedule** | active legacy `CronScheduler` = `0 0 3 * * ?` + `0 0 5 * * ?` (**3 AM & 5 AM**) | `0 21 10`, `0 43 10`, `0 55 11` (10:21/10:43/11:55 AM) | **Yes** |

Schedule note: the compiled legacy code shows **3 AM & 5 AM** (a "2AM" backup variant shows 1 AM;
none show exactly 2/5/9 AM). The business owner's stated **2/5/9 AM** is a requirement to confirm,
not something the legacy binary contains — flagging rather than asserting.

## STEP 4 — Dev-vs-prod discrepancy: theory holds
`tbl_pz_map_company_plant_material` empty in prod, populated in dev — consistent with the code:
- In **production** the new job looks in `/opt/tomcat/uploads/issue`, the file lands in
  `/home/issuenote/issue` → "CSV file not found" WARN → returns without writing → the pz table it
  targets stays **empty**. (And even if it found the file, it would write the *wrong* table.)
- **Dev** rows in the pz table were almost certainly a manual/test seed or an old run with a file
  placed at the dev path — not the daily production import. Confirmed plausible from code alone.

## STEP 5 — Corrected design facts
1. **Authoritative stock table:** `tbl_map_company_plant_material` (`map_quantity_stores`).
2. **Correct import path:** `/home/issuenote/issue/Material(<date>).CSV`.
3. **New job divergences to fix:** (a) path → `/home/issuenote/issue`; (b) target table →
   `tbl_map_company_plant_material` (i.e. import via the `CompanyPlantMaterialMap` entity /
   `map_quantity_stores`, the same one everything reads — not the `CompanyPlantMaterial`/pz entity);
   (c) schedule → confirm 2/5/9 AM with the owner (legacy binary is 3 AM & 5 AM).
4. **What must change** so the import writes the same table everything reads, from the correct path,
   on the intended schedule:
   - Point `SapMaterialImportJob`/`MaterialImportService` at
     **`tbl_map_company_plant_material`** (write `CompanyPlantMaterialMap.quantity` /
     `map_quantity_stores`) instead of `tbl_pz_map_company_plant_material`.
   - Change `sap.csv.import.path` default to **`/home/issuenote/issue`** (or set the prop in
     `application-prod.yml`).
   - Set the three `@Scheduled` crons to the agreed times (e.g. `0 0 2`, `0 0 5`, `0 0 9` if 2/5/9 AM
     is confirmed; legacy binary uses 3 AM & 5 AM).
   - This also fixes the reconciliation prerequisite from the first investigation: once the import
     writes the read-source table, "reset issued tally on import" becomes meaningful, and the
     `last_stock_import_at` marker should be stamped by this job on success.

**Still investigation-only — nothing implemented.** Decisions for the owner: confirm the 2/5/9 AM
schedule, and confirm production drops the file at `/home/issuenote/issue/Material(<date>).CSV`.
