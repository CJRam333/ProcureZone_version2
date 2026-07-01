# SUPERVISOR Role Verification Checklist

**Date:** 2026-07-01  
**Commit:** See PROJECT_STABILIZATION_LOG.md for commit hash  
**Scope:** Complete end-to-end audit of every layer for SUPERVISOR role

---

## Investigation Findings (STEP 1)

### 1a — Backend @PreAuthorize

| File | Endpoint | Gap | Fixed |
|------|----------|-----|-------|
| `ApprovalController.java` | `POST /reject` | Missing SUPERVISOR | ✓ |
| `ApprovalController.java` | `POST /request-info` | Missing SUPERVISOR | ✓ |
| `ApprovalController.java` | `GET /department-indents` | Missing SUPERVISOR | ✓ |
| `IndentController.java` | `GET /indents` (list) | Missing SUPERVISOR | ✓ |
| `IndentController.java` | `GET /indents/search` | Missing SUPERVISOR | ✓ |
| `IssueNoteController.java` | `POST /issue-notes` (create) | Missing SUPERVISOR | ✓ |
| `IssueNoteController.java` | `GET /issue-notes` (list) | Missing SUPERVISOR | ✓ |
| `IssueNoteController.java` | `GET /issue-notes/{id}` | Missing SUPERVISOR | ✓ |
| `IssueNoteController.java` | `GET /issue-notes/by-number` | Missing SUPERVISOR | ✓ |
| `IssueNoteController.java` | `POST /{id}/submit` | Missing SUPERVISOR | ✓ |
| `IssueNoteController.java` | `POST /{id}/cancel` | Missing SUPERVISOR | ✓ |
| `IssueNoteController.java` | `GET /by-department/{deptId}` | Missing SUPERVISOR | ✓ |
| `IssueNoteController.java` | `GET /my-issue-notes` | Missing SUPERVISOR | ✓ |
| `PlantIndentController.java` | `POST /plant-indent` (create) | Missing SUPERVISOR | ✓ |
| `PlantIndentController.java` | `GET /plant-indent/*` (6 endpoints) | Missing SUPERVISOR | ✓ |
| `PlantIndentController.java` | `PUT /plant-indent/{id}` | Missing SUPERVISOR | ✓ |
| `PlantIndentController.java` | `POST /{id}/submit` | Missing SUPERVISOR | ✓ |
| `DashboardController.java` | All 10 endpoints | Missing SUPERVISOR | ✓ |

**No gaps found:**  
- `ApprovalController`: `/pending`, `/pending-for-me`, `/approve` — SUPERVISOR already present ✓  
- `IssueNoteController`: `/rm-approve`, `/rm-reject`, `/pending-rm-approval` — SUPERVISOR already present ✓

### 1b — Frontend Route Guards (router.tsx)

| Route | Gap | Fixed |
|-------|-----|-------|
| `/indents/new` | Missing SUPERVISOR | ✓ |
| `/indents/:id/edit` | Missing SUPERVISOR | ✓ |
| `/issue-notes/new` | Missing SUPERVISOR | ✓ |
| `/issue-notes/:id/edit` | Missing SUPERVISOR | ✓ |
| `/plant-indent/new` | Missing SUPERVISOR | ✓ |
| `/plant-indent/:id/edit` | Missing SUPERVISOR | ✓ |
| `/confirmations/issue` | Missing SUPERVISOR | ✓ |
| `/confirmations/receipt` | Missing SUPERVISOR | ✓ |

**Already correct:** `/indents`, `/indents/approvals`, `/indents/:id`, `/issue-notes`, `/issue-notes/approvals`, `/issue-notes/:id`, `/plant-indent`, `/plant-indent/:id`, `/reports/*` ✓

### 1c / 1d — Page-level hasAnyRole checks

| File | Variable | Gap | Fixed |
|------|----------|-----|-------|
| `IndentDetailPage.tsx` | `canEdit` | Missing SUPERVISOR | ✓ |
| `IndentDetailPage.tsx` | `canSubmit` | Missing SUPERVISOR | ✓ |
| `IssueNoteDetailPage.tsx` | `canEdit` | Missing SUPERVISOR | ✓ |
| `IssueNoteDetailPage.tsx` | `canSubmit` | Missing SUPERVISOR | ✓ |
| `IssueNoteDetailPage.tsx` | `canApprove` | Missing SUPERVISOR | ✓ |
| `IssueNoteApprovalPage.tsx` | `isManager` | Missing SUPERVISOR | ✓ |
| `IndentsListPage.tsx` | edit button guard (L172) | Missing SUPERVISOR | ✓ |
| `IndentsListPage.tsx` | "New Indent" button (L212) | Missing SUPERVISOR | ✓ |
| `DashboardPage.tsx` | "Pending Approvals" stat card | Missing SUPERVISOR (duplicate role bug fixed) | ✓ |
| `DashboardPage.tsx` | "Create Indent" quick action | Missing SUPERVISOR | ✓ |
| `DashboardPage.tsx` | "Create Issue Note" quick action | Missing SUPERVISOR | ✓ |
| `IndentApprovalPage.tsx` | `getApprovalLevel()` | No SUPERVISOR label | ✓ → "Level 1 (RM Review)" |

### 1e — ModuleAccessService
- `isAllowedByRole()` — previously fixed: "ALL" wildcard + case normalization ✓
- SUPERVISOR gets base modules (INDENTS, PLANT_INDENTS, ISSUE_NOTES, REPORTS, CONFIRMATIONS) via `default_roles = 'ALL'` or explicit `SUPERVISOR` entry in `tbl_module_master` ✓

### 1f — RoleNormalizer
- `"Supervisor"` → `"SUPERVISOR"` — confirmed intact ✓

### 1g — filterIndents() dept scoping
- Non-admin roles scoped to their own `deptId` from JWT ✓
- SUPERVISOR has `deptId` in their JWT claim — they see their department's indents ✓

### 1h — create/submit bypass logic
- `createIndent()` uses `isAuthenticated()` — SUPERVISOR can create ✓
- `submitIndent()` auto-approval only fires for DEPTHEAD; SUPERVISOR gets normal L1 flow ✓
- `createIssueNote()` — `@PreAuthorize` fixed; service has no SUPERVISOR-specific exclusion ✓

### 1i — getPendingApprovals() routing
- **BUG FIXED:** `getPendingApprovals()` previously always returned the DeptHead L2 queue
- SUPERVISOR is now routed to `getPendingL1Approvals()` (their subordinates' submitted indents)
- Detection: SUPERVISOR role present AND no DEPTHEAD/PLANTMANAGER/ADMIN/SUPERADMIN

### Sidebar.tsx
| Nav item | Gap | Fixed |
|----------|-----|-------|
| Issue Notes | Missing SUPERVISOR | ✓ |
| Confirmations | Missing SUPERVISOR | ✓ |
| Reports group | Missing SUPERVISOR | ✓ |

---

## SQL Verification Queries (Run by Business Owner)

Run these on the production database to confirm module access is configured correctly for the Supervisor role.

```sql
-- 1. Confirm tbl_roles_master has Supervisor
SELECT role_id, role_code, role_name
FROM tbl_roles_master
WHERE role_code = 'Supervisor';

-- 2. Confirm tbl_module_master default_roles covers SUPERVISOR
-- Expect: default_roles includes 'ALL' or contains 'SUPERVISOR' for base modules
SELECT module_code, module_name, default_roles, module_status, is_future
FROM tbl_module_master
WHERE module_code IN ('INDENTS', 'PLANT_INDENTS', 'ISSUE_NOTES', 'REPORTS', 'CONFIRMATIONS')
ORDER BY module_code;

-- 3. Confirm Venki (emp_number=143) has Supervisor role active
SELECT er.emp_number, r.role_code, r.role_name, er.status
FROM tbl_employee_roles er
JOIN tbl_roles_master r ON er.role_id = r.role_id
WHERE er.emp_number = 143 AND er.status = 1;

-- 4. Check if Venki has any tbl_emp_module_access overrides (custom module grants)
SELECT ema.module_code, ema.is_enabled, ema.granted_by, ema.granted_at
FROM tbl_emp_module_access ema
WHERE ema.emp_number = 143
ORDER BY ema.module_code;

-- 5. Confirm reporting hierarchy — Venki's subordinates (to verify L1 queue will have items)
SELECT emp_number, reporting_manager_emp_number
FROM tbl_employee_reporting
WHERE reporting_manager_emp_number = 143 AND status = 1;
```

---

## End-to-End SUPERVISOR Flow (Post-Fix)

### Login
- Venki logs in → `RoleNormalizer` maps `"Supervisor"` → `"SUPERVISOR"` → JWT `ROLE_SUPERVISOR` ✓
- Module API returns base 5 modules (INDENTS, PLANT_INDENTS, ISSUE_NOTES, REPORTS, CONFIRMATIONS) ✓
- Sidebar shows: Indents, Plant Indent, Issue Notes, Confirmations, Reports ✓

### Create & Submit Indent
1. Navigate to `/indents/new` → ProtectedRoute passes ✓
2. `POST /api/v1/indents` (isAuthenticated) → creates draft ✓
3. `POST /api/v1/indents/{id}/submit` → status 2 (Submitted), routed to SUPERVISOR's own RM ✓
   - (Auto-approval skipped — SUPERVISOR is not DEPTHEAD)

### View Approval Queue
1. Navigate to `/indents/approvals` → ProtectedRoute passes ✓
2. `GET /api/v1/approvals/pending` → `getPendingApprovals()` detects SUPERVISOR → routes to `getPendingL1Approvals()` ✓
3. Returns subordinates' submitted indents (status=2, approvedStatus=1) ✓
4. Page header shows "Level 1 (RM Review)" ✓

### Approve Indent
1. Click approve → `POST /api/v1/approvals/indents/{id}/approve` → SUPERVISOR in `@PreAuthorize` ✓
2. Smart-routing: status=2 → `approveIndent()` ✓
3. Hierarchy check: `canApproveFor(indentCreatorEmpNo, supervisorEmpNo)` ✓
4. Status → 3 (Dept Head Approved) ✓

### Reject Indent
1. Click reject → `POST /api/v1/approvals/indents/{id}/reject` → SUPERVISOR now in `@PreAuthorize` ✓

### Issue Notes
1. Create issue note → `POST /api/v1/issue-notes` → SUPERVISOR allowed ✓
2. Submit → `POST /{id}/submit` → SUPERVISOR allowed ✓
3. Approve (RM stage) → `POST /{id}/rm-approve` → SUPERVISOR always had this ✓
4. View list → `GET /api/v1/issue-notes` → SUPERVISOR allowed ✓

---

## Files Changed in This Commit

### Backend
- `ApprovalController.java` — `/reject`, `/request-info`, `/department-indents` + SUPERVISOR
- `IndentController.java` — list, search + SUPERVISOR
- `IssueNoteController.java` — create, list, get, by-number, submit, cancel, by-department, my-issue-notes + SUPERVISOR
- `PlantIndentController.java` — create, all GETs, update, submit + SUPERVISOR
- `DashboardController.java` — all 10 endpoints + SUPERVISOR
- `IndentService.java` — `getPendingApprovals()` routes SUPERVISOR to L1 queue

### Frontend
- `router.tsx` — 8 routes + SUPERVISOR
- `IndentDetailPage.tsx` — `canEdit`, `canSubmit` + SUPERVISOR
- `IssueNoteDetailPage.tsx` — `canEdit`, `canSubmit`, `canApprove` + SUPERVISOR
- `IssueNoteApprovalPage.tsx` — `isManager` + SUPERVISOR
- `IndentsListPage.tsx` — edit button + "New Indent" button + SUPERVISOR
- `DashboardPage.tsx` — stat card + 2 quick actions + SUPERVISOR
- `IndentApprovalPage.tsx` — `getApprovalLevel()` maps SUPERVISOR → "Level 1 (RM Review)"
- `Sidebar.tsx` — Issue Notes, Confirmations, Reports group + SUPERVISOR
