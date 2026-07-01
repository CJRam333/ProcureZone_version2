# Project Stabilization Log

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
