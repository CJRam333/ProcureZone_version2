# Project Stabilization Log

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
