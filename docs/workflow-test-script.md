# ProcureZone Workflow Test Script

Manual end-to-end test script for the Indent and Issue Note approval workflows.

---

## Prerequisites

| Requirement | Value |
|---|---|
| URL | `http://localhost:5173` (dev) or production |
| DB | `seeds_indent` with at least one company, department, plant, section, and material in `tbl_pz_map_company_plant_material` |
| Users | One each of: USER, DEPTHEAD, PROCUREMENT, STORES roles |

---

## Indent Workflow

### Test 1 — Create and Submit

1. Log in as **USER**.
2. Navigate to **Indents → New Indent**.
3. Verify: employee name, financial year, date, and next indent number are pre-filled (read-only banner).
4. Select Company, Department, Plant.
5. Add at least one line item — type ≥ 2 chars in the material search, confirm dropdown appears, select a material.
6. Click **Save as Draft** — verify the indent appears in the list with status "Draft".
7. Open the draft and click **Save & Submit** — verify status changes to "Submitted".

### Test 2 — L1 Approval (DEPTHEAD)

1. Log in as **DEPTHEAD**.
2. Navigate to **Approvals → Pending Indents**.
3. Find the submitted indent.
4. Click **Approve** — verify status changes to "Dept. Head Approved".

### Test 3 — L2 Approval (PROCUREMENT)

1. Log in as **PROCUREMENT**.
2. Navigate to **Approvals → Pending Indents (L2)**.
3. Find the approved indent.
4. Click **Final Approve** — verify status changes to "Procurement In Progress".

### Test 4 — Rejection Flow

1. Repeat Test 1 steps 1–7.
2. Log in as **DEPTHEAD**, reject the indent with a reason.
3. Log back in as **USER** — verify status shows "Rejected" with the rejection reason.

---

## Issue Note Workflow

### Test 5 — Create and Submit

1. Log in as **USER**.
2. Navigate to **Issue Notes → New Issue Note**.
3. Verify: employee name, financial year, date, and next issue note number are pre-filled.
4. Select Company, Plant, Department.
5. Add at least one material (type ≥ 2 chars to search).
6. Fill **Issued To**, click **Save & Submit**.
7. Verify status "Submitted".

### Test 6 — Stores Issue

1. Log in as **STORES**.
2. Navigate to **Issue Notes → Pending Issue**.
3. Find the submitted note.
4. Click **Issue** — verify status changes to "Issued".

---

## Logout / Session Tests

### Test 7 — Multi-user Session Isolation

1. Log in as User A, navigate to `/indents`.
2. Log out.
3. Log in as User B — verify the browser lands on `/dashboard`, NOT `/indents`.
4. Verify User A's data is not visible under User B's session.

### Test 8 — Token Expiry

1. Log in, then wait for the JWT to expire (default 1 h) or manually clear `localStorage.accessToken`.
2. Attempt any authenticated navigation — verify redirect to `/login`.

---

## Profile Page

### Test 9 — Profile Information

1. Log in as any user.
2. Navigate to **Profile**.
3. Verify: Employee Code, Email, Department, Company, Location all display correctly.
4. Change password with correct current password — verify success toast.
5. Attempt change password with wrong current password — verify error message.
