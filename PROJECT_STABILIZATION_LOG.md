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
