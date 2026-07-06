# Indent Workflow — Investigation Reference

**Date:** 2026-07-01  
**Scope:** Legacy `IndentAction.java` (1,827 lines) + relevant JSPs + new Spring Boot implementation  
**Purpose:** Authoritative reference for porting and gap analysis. Read-only investigation, no implementation.

---

## Table of Contents

1. [Legacy Session Variables & Role Codes](#1-legacy-session-variables--role-codes)
2. [Legacy Status Integer Mapping (Three-Column Model)](#2-legacy-status-integer-mapping-three-column-model)
3. [Investigation 1 — saveIndentRequest(): Role Branching at Creation](#3-investigation-1--saveindentrequest-role-branching-at-creation)
4. [Investigation 2 — List Visibility Methods per Role](#4-investigation-2--list-visibility-methods-per-role)
5. [Investigation 3 — DeptHead Creating Their Own Indent](#5-investigation-3--depthead-creating-their-own-indent)
6. [Investigation 4 — New System (Spring Boot)](#6-investigation-4--new-system-spring-boot)
7. [Investigation 5 — Gap Analysis Table](#7-investigation-5--gap-analysis-table)

---

## 1. Legacy Session Variables & Role Codes

Source: `IndentAction.java` — `saveIndentRequest()` (L947), `saveFinalIndentRequest()` (L1369), `IndentReport()` (L1784).

### Session Keys

| Key | Type | Meaning |
|-----|------|---------|
| `session["empNumber"]` | int | Employee number (PK in `tbl_emp_master`) |
| `session["role"]` | int | Primary role code (see table below) |
| `session["Supervisor"]` | int | Set to 4 if user is a Supervisor or DeptHead; null otherwise |
| `session["Admin"]` | int | Admin flag (used in report filtering) |
| `session["deptId"]` | int | User's department ID |

### Legacy Role Integer Values

Confirmed from `saveIndentRequest()` conditional checks and `IndentReport()` HQL:

| Integer | Meaning | Where confirmed |
|---------|---------|-----------------|
| 3 | Regular employee | `session["role"] == 3` branch in `saveIndentRequest()`, `roleId=3` in `IndentReport()` HQL |
| 4 | Supervisor / DeptHead | `session["Supervisor"] == 4` branch in `saveIndentRequest()`, `roleId=4` in `IndentReport()` HQL |
| 6 | Procurement | `roleId=6` in `getProcurementList1()` HQL |

**Key nuance:** In the legacy system, Supervisor (RM-level) and DeptHead are the **same role code (4)**. The system does not distinguish between "Reporting Manager" and "Department Head" at the session variable level. Both share `session["Supervisor"]==4`.

---

## 2. Legacy Status Integer Mapping (Three-Column Model)

Source: `saveIndentRequest()` (L947-1077), `saveFinalIndentRequest()` (L1369-1462), `getProcurementList()` (L1529), HQL queries throughout `IndentAction.java`.

The legacy system tracks workflow state across **four columns** on `tbl_indent_master`:

### `indent_status` — Record Active Flag (NOT workflow status)
| Value | Meaning |
|-------|---------|
| 1 | Active (record exists and is valid) |
| 0 | Soft-deleted / inactive |

### `indent_approved_status` — L1 / RM Approval
| Value | Meaning |
|-------|---------|
| 1 | Pending RM approval (just created by employee) |
| 2 | Rejected by RM |
| 3 | Approved by RM (or self-approved by DeptHead) |

### `indent_final_status` — L2 / DeptHead Approval
| Value | Meaning |
|-------|---------|
| 1 | Pending DeptHead decision |
| 2 | Rejected by DeptHead |
| 4 | Approved by DeptHead → forwarded to Procurement |

### `indent_procurement_status` — L3 / Procurement Workflow
| Value | Meaning |
|-------|---------|
| 1 | Pending (not yet at Procurement) |
| 4 | Sent to Procurement (DeptHead approved) |
| 5 | Quotations collected |
| 6 | Negotiation done |
| 7 | PO issued |
| 8 | Hold / Procurement rejected |
| 9 | Cash buy / Delivery |

### State Transition Matrix

```
Creation by employee:    approvedStatus=1, finalStatus=1, procurementStatus=1
Creation by DeptHead:    approvedStatus=3, finalStatus=1, procurementStatus=1  (self-approves L1)
RM approves:             approvedStatus=3  (finalStatus unchanged)
RM rejects:              approvedStatus=2  (finalStatus unchanged)
DeptHead approves:       finalStatus=4, procurementStatus=4
DeptHead rejects:        finalStatus=2, procurementStatus=2
Procurement sets status: procurementStatus=5 through 9
```

---

## 3. Investigation 1 — saveIndentRequest(): Role Branching at Creation

Source: `IndentAction.java` L947-1077 (`saveIndentRequest()`).

### Control Flow

```java
// saveIndentRequest() — Struts action mapped to saveIndentRequest.action
if (validation()) {
    getIndentNo1();  // generates sequential indent number

    if (session["Supervisor"] != null && session["Supervisor"] == 4) {
        // ────────────────────────────────────────────────────────────
        // BRANCH A: DeptHead / Supervisor creating their own indent
        // ────────────────────────────────────────────────────────────
        tblIndentMaster.approvedBy        = session["empNumber"]  // SELF
        tblIndentMaster.approvedStatus    = 3   // L1 auto-approved (self)
        tblIndentMaster.approvedByDate    = today
        tblIndentMaster.finalStatus       = 1   // pending DeptHead L2
        tblIndentMaster.procurementStatus = 1   // pending
        tblIndentMaster.indentStatus      = 1   // active
        // line details: setIndentDetailsRmQty(qty[i])  ← copies qty to rmQty
        // sends: in.NewIndentRequestNotification(...)
    }
    else if (session["role"] != null && session["role"] == 3) {
        // ────────────────────────────────────────────────────────────
        // BRANCH B: Regular employee creating indent
        // ────────────────────────────────────────────────────────────
        // approvedBy: NOT set
        tblIndentMaster.approvedStatus    = 1   // pending RM
        tblIndentMaster.finalStatus       = 1   // pending
        tblIndentMaster.procurementStatus = 1   // pending
        tblIndentMaster.indentStatus      = 1   // active
        // line details: rmQty NOT set (left null / will be set by RM)
        // sends: in.NewIndentRequestNotification(...)
    }
    else {
        // No matching role → validation error, returns INPUT
    }
}
```

### Immediate Queue Placement After Creation

| Creator Role | approvedStatus | finalStatus | Goes Into |
|-------------|---------------|------------|-----------|
| Regular employee | 1 (Pending RM) | 1 | RM's `getRmList()` queue immediately |
| Supervisor/DeptHead | 3 (RM Approved) | 1 (Pending DeptHead) | DeptHead's L2 queue immediately |

### Quantity Fields Set at Creation

| Creator Role | `indentDetailsQty` | `indentDetailsRmQty` | `indentDetailsDeptQty` |
|-------------|-------------------|----------------------|----------------------|
| Regular employee | set from form | NOT set | NOT set |
| DeptHead | set from form | **copied from qty** | NOT set |

The DeptHead's `rmQty` is auto-set to `qty` at creation time, confirming self-approval of L1 quantities.

---

## 4. Investigation 2 — List Visibility Methods per Role

Source: `IndentAction.java` L674-703 (`getList()`), L1106-1135 (`getRmList()`), L1285-1320 (`getDeptHeadList()`), L1529-1562 (`getProcurementList()`).

### getList() — Employee Self-List (L674)

```java
// Shows: own submitted indents waiting for RM approval
// Struts action: selfIndentList.action
indentDao.getList(
    "where indentStatus=1 
     and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 
     and tblEmpMasterByIndentCreatedby.empNumber=" + session["empNumber"]
)
// STATUS GATE: approvedStatus=1 (pending RM)
// SCOPE: creator = current user only
// VISIBILITY: shows only your own indents, only before RM acts on them
```

**getList1() — Filtered by Status Tab (L684)**  
Same scope (creator=self), but filters by tab selection:
- Tab 1 (indId=1): `approvedStatus=1` — Pending RM
- Tab 2 (indId=2): `approvedStatus=2 OR finalStatus=2` — Rejected (RM or DeptHead)
- Tab 3 (indId=3): `approvedStatus=3 AND finalStatus=1` — RM Approved, Pending DeptHead
- Tab 4 (indId=4): `approvedStatus=3 AND finalStatus=4` — DeptHead Approved

### getRmList() — Supervisor/RM Approval Queue (L1106)

```java
// Shows: subordinates' indents waiting for RM approval
// Struts action: rmIndentRequestList.action
indentDao.getList(
    "where indentStatus=1 
     and tblIndentStatusByIndentApprovedStatus.indentStatusId=1 
     and tblEmpMasterByIndentCreatedby.empNumber in (
         select tblEmpMasterByReportSub.empNumber 
         from TblMapEmpReporting 
         where tblEmpMasterByReportSup.empNumber=" + session["empNumber"]
     + ")"
)
// STATUS GATE: approvedStatus=1 (pending RM)
// SCOPE: creators must be direct subordinates via TblMapEmpReporting (reportSup=currentUser)
// NOTE: one-level deep only — not recursive
```

**getRmList1() — Filtered by Status Tab (L1116)**  
Same scope (subordinates of currentUser), filters by tab:
- Tab 1 (indId=1): `approvedStatus=1` — Pending approval
- Tab 2 (indId=2): `approvedStatus=2 OR finalStatus=2 OR (approvedStatus=2 AND approvedBy=currentUser)` — Rejected items
- Tab 3 (indId=3): `(approvedStatus=3 AND finalStatus=1 AND creator IN subordinates) OR (approvedStatus=3 AND finalStatus=1 AND creator=currentUser)` — RM-approved, pending DeptHead
- Tab 4 (indId=4): `(approvedStatus=3 AND finalStatus=4 AND creator IN subordinates) OR (approvedStatus=3 AND finalStatus=4 AND creator=currentUser)` — DeptHead-approved

### getDeptHeadList() — DeptHead L2 Queue (L1285)

```java
// Shows: RM-approved indents awaiting DeptHead decision
// Struts action: deptHeadIndentRequestList.action
indentDao.getList(
    "where indentStatus=1 
     and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 
     and tblIndentStatusByIndentFinalStatus.indentStatusId=1 
     and tblEmpMasterByIndentApprovedby.empNumber in (
         select tblEmpMasterByReportSub.empNumber 
         from TblMapEmpReporting 
         where tblEmpMasterByReportSup.empNumber=" + session["empNumber"]
     + ")"
)
// STATUS GATE: approvedStatus=3 (RM approved) AND finalStatus=1 (pending DeptHead)
// SCOPE: approvedBy (the RM who approved at L1) must be a subordinate of currentUser
// LIMITATION: Does NOT show DeptHead's own self-created indents (where approvedBy=self)
```

**getDeptHeadList1() — Filtered by Status Tab (L1296)**  
The filtered version has an important extra OR clause for DeptHead's own indents:
- Tab 3 (indId=3) — Pending DeptHead decision:
  ```
  (approvedStatus=3 AND finalStatus=1 AND approvedBy IN subordinates)
  OR
  (approvedStatus=3 AND finalStatus=1 AND approvedBy=currentUser)
  ```
  The second OR captures DeptHead's own self-created indents (where `approvedBy=self`).

### getProcurementList() — Procurement Global Queue (L1529)

```java
// Shows: all DeptHead-approved indents ready for procurement
// Struts action: procurementIndentList.action
indentDao.getList(
    "where indentStatus=1 
     and tblIndentStatusByIndentApprovedStatus.indentStatusId=3 
     and tblIndentStatusByIndentFinalStatus.indentStatusId=4 
     and tblIndentStatusByIndentProcurementStatus.indentStatusId=4"
)
// STATUS GATE: approvedStatus=3 AND finalStatus=4 AND procurementStatus=4
// SCOPE: GLOBAL — no user or department scoping at all
// All procurement staff see the same queue
```

**getProcurementList1() — Sub-status Tabs (L1539)**  
Filters by procurementStatus (5-9) AND procurement person's roleId=6:
```
procurementStatus=5 AND procurementBy IN (employees with roleId=6)
procurementStatus=6 AND ...
...
```

---

## 5. Investigation 3 — DeptHead Creating Their Own Indent

Source: `IndentAction.java` `saveIndentRequest()` (L947-1077), `getDeptHeadList()` (L1285), `getDeptHeadList1()` (L1296-1320), JSP `seeds_indent_request.jsp` (form action: `saveIndentRequest.action`).

### The Mechanism

A DeptHead creates their own indent using the **same JSP form and action as a regular employee** (`seeds_indent_request.jsp` → `saveIndentRequest.action`). There is **no separate popup dialog or confirmation** — the role differentiation is entirely in the Java action class via the session check.

```java
// In saveIndentRequest() — line 950:
if (session["Supervisor"] != null && session["Supervisor"] == 4) {
    // DeptHead path: auto-set L1 approval to self
    tblIndentMaster.setTblEmpMasterByIndentApprovedby(empMaster);  // approvedBy = SELF
    indentStatus.setIndentStatusId(3);  // approvedStatus = 3 (RM approved)
    tblIndentMaster.setTblIndentStatusByIndentApprovedStatus(indentStatus);
    tblIndentMaster.setIndentApprovedbyDate(utils.DateIn2());  // today
    indentStatus = new TblIndentStatus();
    indentStatus.setIndentStatusId(1);
    tblIndentMaster.setTblIndentStatusByIndentFinalStatus(indentStatus);     // finalStatus = 1
    tblIndentMaster.setTblIndentStatusByIndentProcurementStatus(indentStatus); // procStatus = 1
}
```

The `rmQty` for each line item is set to the requested `qty`:
```java
tblIndentDetails.setIndentDetailsRmQty(qty[i]);  // auto-fill RM qty = original qty
```

### Where the DeptHead's Own Indent Appears

After creation, the indent has:
- `approvedStatus = 3` (self-approved at L1)
- `finalStatus = 1` (pending DeptHead L2)
- `approvedBy = DeptHead_empNumber`

In `getDeptHeadList()` (default view), the query is:
```sql
approvedBy IN (SELECT ... FROM TblMapEmpReporting WHERE reportSup=currentUser)
```
This returns subordinates of the DeptHead. Since the DeptHead is not their own subordinate, **their own self-created indents do NOT appear in the default DeptHead list view**.

In `getDeptHeadList1()` (tab=3 — "Pending Approval"), the query adds:
```sql
OR (approvedStatus=3 AND finalStatus=1 AND approvedBy=currentUser)
```
This second clause captures self-created indents. **DeptHead's own indents only appear in the filtered tab view, not the default list.**

### saveFinalIndentRequest() — Dual-Branch Approval Logic (L1369)

The DeptHead L2 approval action handles two cases:

**Branch 1** — `approvedStatus == 1` (indent was never RM-approved, DeptHead is acting directly):
```java
// Sets both L1 and L2 simultaneously
tblIndentMaster.approvedBy         = currentUser
tblIndentMaster.approvedStatus     = 2 (reject) or 3 (approve)  // sets L1
tblIndentMaster.approvedByDate     = today
tblIndentMaster.finalStatus        = indentStatusId  // 2=reject, 4=approve — sets L2
tblIndentMaster.procurementStatus  = indentStatusId  // same
```

**Branch 2** — `approvedStatus != 1` (already RM-approved, i.e., approvedStatus=3):
```java
// Sets only L2 (L1 was already handled by RM)
tblIndentMaster.finalApprovedBy    = currentUser
tblIndentMaster.finalStatus        = indentStatusId  // 2=reject, 4=approve
tblIndentMaster.procurementStatus  = indentStatusId  // same
```

DeptHead's own self-created indents (approvedStatus=3 at creation) go through **Branch 2**.

### The `seeds_indent_request_dept_head.jsp` Form

This JSP (`form action="saveFinalIndentRequest.action"`) is the **DeptHead approval page** for reviewing an existing indent submitted by a subordinate. It pre-populates from `getFinalIndentDetails()` using the `iId` static field (set when the DeptHead clicks on an indent from their list). It is **not** a creation form — it is a review-and-approve form.

---

## 6. Investigation 4 — New System (Spring Boot)

Source: `IndentService.java`, `IndentController.java`, `ApprovalController.java` (from prior audit session).

### Status Model — Dual Column System

The new system maintains BOTH a primary `indent_status` column AND the three legacy workflow columns for compatibility.

**Primary `indent_status` (workflow progression):**
| Value | Label | Meaning |
|-------|-------|---------|
| 1 | Draft | Just created, not submitted |
| 2 | Submitted | Awaiting L1 RM approval |
| 3 | Dept Head Approved | L1 approved (note: misleading label in code) |
| 4 | Rejected | Generic rejected state |
| 5 | Proc. In Progress | Under procurement |

**Three legacy workflow columns (still used):**
| Column | Values | Meaning |
|--------|--------|---------|
| `indent_approved_status` | 1=Pending, 2=Rejected, 3=Approved | L1/RM |
| `indent_final_status` | 1=Pending, 2=Rejected, 4=Approved | L2/DeptHead |
| `indent_procurement_status` | 1=Pending, 4-11=stages | L3/Procurement |

**Display status derivation** (`deriveDisplayStatus()`, IndentService L80-99):
```
(approvedId=1, finalId=1, procId=1) → "Pending"
(approvedId=2, finalId=1, procId=1) → "RM Rejected"
(approvedId=3, finalId=1, procId=1) → "RM Approved"
(approvedId=3, finalId=2, procId=2) → "Dept. Head Rejected"
(approvedId=2, finalId=2, procId=2) → "Dept. Head Rejected"
(approvedId=3, finalId=4, procId=4) → "Dept. Head Approved"
(approvedId=3, finalId=4, procId=5) → "Quotations Collected"
(approvedId=3, finalId=4, procId=6) → "Negotiation Done"
(approvedId=3, finalId=4, procId=7) → "PO Released"
(approvedId=3, finalId=4, procId=8) → "Hold"
(approvedId=3, finalId=4, procId=9) → "Cash Buy"
(approvedId=3, finalId=4, procId=10) → "Goods Receipt"
(approvedId=3, finalId=4, procId=11) → "Goods Issued"
```

### createIndent() — IndentService L144

- Sets `status=1` (Draft), `approvedStatus=1`, `finalStatus=1`, `procurementStatus=1`
- **No role-based branching at creation time** (comment at L179-184 is explicit)
- DEPTHEAD auto-approval is deferred to `submitIndent()`

### submitIndent() — IndentService L587

```java
// Changes indent_status from 1 (Draft) to 2 (Submitted)
indent.setStatus(IndentStatus.class, 2);

// Auto-approval check — only for DEPTHEAD with no supervisor
boolean isDeptHead = hasRoleByCode(currentUser.getEmpNumber(), "DEPTHEAD");
boolean hasSupervisor = employeeReportingRepository.hasSupervisor(currentUser.getEmpNumber());
if (isDeptHead && !hasSupervisor) {
    // Auto-approve L1
    detail.setRmQuantity(detail.getQuantity());  // copy qty → rmQty
    indent.setApprovedBy(currentUser);
    indent.setApprovedByDate(now);
    indent.setApprovedStatus(IndentStatus.class, 3);  // L1 auto-approved
    indent.setRemarks("L1 Auto-approved (submitter is DEPTHEAD)");
}
```

**Critical difference from legacy:**
| Condition | Legacy | New System |
|-----------|--------|-----------|
| All role-4 users | Auto-skip L1 (always) | Only if DEPTHEAD AND no supervisor in hierarchy |
| Supervisor (RM-level) | Auto-skip L1 | Normal L1 flow |
| DEPTHEAD with a supervisor above them | Auto-skip L1 | Normal L1 flow |

The new system is more precise: a DEPTHEAD who still reports to someone goes through L1 normally.

### filterIndents() — IndentService L259

```java
// Department scoping
boolean isAdminOrSuper = roles.contains("SUPERADMIN") || roles.contains("ADMIN");
if (!isAdminOrSuper && cu.deptId() != null) {
    effectiveDeptId = cu.deptId();  // force to caller's department
}
// SUPERVISOR has deptId in JWT — sees own department's indents
```

### getPendingL1Approvals() — IndentService L1059

```java
// Calls reportingHierarchyService.getApprovableEmployees(empNumber, false)
// → finds all direct subordinates of current user
// Then queries findRmQueueForEmployees(subordinateIds)
// → WHERE approvedStatus=1 AND creator IN (subordinateIds)
```

### getPendingL2Approvals() — IndentService L1087

```java
// ADMIN/SUPERADMIN: findDeptHeadQueue() — all departments
// Others: findDeptHeadQueueByDepartment(deptId) — own dept
// Filter: approvedStatus=3 AND finalStatus=1
```

### getPendingApprovals() — IndentService (patched in prior session)

```java
// SUPERVISOR-only users: routes to getPendingL1Approvals()
// Others (DEPTHEAD/PLANTMANAGER): routes to DeptHead L2 queue
boolean isSupervisorOnly = roles.contains("SUPERVISOR")
    && !roles.contains("DEPTHEAD") && !roles.contains("PLANTMANAGER")
    && !roles.contains("ADMIN") && !roles.contains("SUPERADMIN");
if (isSupervisorOnly) {
    return getPendingL1Approvals(username);
}
```

### IndentController Endpoints

| Method | Path | Auth |
|--------|------|------|
| GET | `/api/v1/indents/meta` | isAuthenticated |
| POST | `/api/v1/indents` | isAuthenticated |
| GET | `/api/v1/indents/{id}` | isAuthenticated |
| GET | `/api/v1/indents` | hasAnyRole(DEPTHEAD, PLANTMANAGER, PROCUREMENT, FLOORINCHARGE, GOODSINCHARGE, VIEWER, ADMIN, SUPERADMIN, USER, SUPERVISOR) |
| GET | `/api/v1/indents/export` | hasAnyRole(SUPERADMIN, ADMIN) |
| GET | `/api/v1/indents/status/{statusId}` | isAuthenticated |
| GET | `/api/v1/indents/employee/{empNumber}` | isAuthenticated |
| GET | `/api/v1/indents/search` | hasAnyRole(same as list) |
| PUT | `/api/v1/indents/{id}` | isAuthenticated |
| DELETE | `/api/v1/indents/{id}` | isAuthenticated |
| POST | `/api/v1/indents/{id}/cancel` | isAuthenticated |
| POST | `/api/v1/indents/{id}/submit` | isAuthenticated |
| POST | `/api/v1/indents/{id}/approve` | hasRole(DEPTHEAD, PLANTMANAGER, ADMIN, SUPERADMIN) |
| POST | `/api/v1/indents/{id}/reject` | hasRole(DEPTHEAD, PLANTMANAGER, ADMIN, SUPERADMIN) |
| POST | `/api/v1/indents/{id}/l1-approve` | isAuthenticated |
| POST | `/api/v1/indents/{id}/l1-reject` | isAuthenticated |
| POST | `/api/v1/indents/{id}/l2-approve` | hasRole(DEPTHEAD, PLANTMANAGER, ADMIN, SUPERADMIN) |
| POST | `/api/v1/indents/{id}/l2-reject` | hasRole(DEPTHEAD, PLANTMANAGER, ADMIN, SUPERADMIN) |
| GET | `/api/v1/indents/pending/l1` | isAuthenticated |
| GET | `/api/v1/indents/pending/l2` | hasRole(DEPTHEAD, PLANTMANAGER, ADMIN, SUPERADMIN) |

### ApprovalController Endpoints

| Method | Path | Auth (post-SUPERVISOR audit) |
|--------|------|------|
| GET | `/api/v1/approvals/pending` | hasAnyRole(SUPERVISOR, DEPTHEAD, PLANTMANAGER, ADMIN, SUPERADMIN) |
| GET | `/api/v1/approvals/pending-for-me` | same |
| POST | `/api/v1/approvals/indents/{id}/approve` | same |
| POST | `/api/v1/approvals/indents/{id}/reject` | same |
| POST | `/api/v1/approvals/indents/{id}/request-info` | same |
| GET | `/api/v1/approvals/department-indents` | same |

**Smart-routing in ApprovalController `/approve`:**  
Routes by `indent_status` value: 2→`approveIndent()`, 3→`finalApproveIndent()`, 5→`procurementApproveIndent()`.

---

## 7. Investigation 5 — Gap Analysis Table

### USER (regular employee, role=3 legacy, ROLE=USER new)

| Scenario | Legacy | New System | Status |
|----------|--------|-----------|--------|
| Create indent | `saveIndentRequest()` → role=3 branch | `POST /api/v1/indents` (isAuthenticated) | Matches |
| Status at creation | approvedStatus=1, finalStatus=1, procStatus=1 | All set to 1 at create; status=1 (Draft) | Matches |
| Submit for approval | Same action completes creation | `POST /api/v1/indents/{id}/submit` → status=2 | Different UX (create-then-submit vs. single-step), same result |
| Own indent list | `getList()` — creator=self, approvedStatus=1 | `GET /api/v1/indents` filtered by dept | Partial — new system shows all dept indents, not only own |
| L1 queue placement | `getRmList()` of their RM | `getPendingL1Approvals()` of their RM | Matches |
| rmQty at creation | Not set (null) | Not set (null; set by RM on L1 approve) | Matches |

### SUPERVISOR (role=4 legacy, ROLE=SUPERVISOR new)

| Scenario | Legacy | New System | Status |
|----------|--------|-----------|--------|
| Create indent | `saveIndentRequest()` → `session["Supervisor"]==4` branch → **auto-skip L1** | `POST /indents` then submit → **normal L1 flow** | **BEHAVIOR CHANGE** — see note |
| Status after creation/submit | approvedStatus=3 (self-approved), finalStatus=1 | approvedStatus=1, status=2 after submit | **Difference** |
| rmQty at creation | **Auto-set = qty** (DeptHead self-approves qty) | Not set until L1 approval by RM | **Difference** |
| View own approval queue | `getRmList()` — subordinates' approvedStatus=1 | `getPendingL1Approvals()` via `getPendingApprovals()` routing | Matches (post-patch) |
| Approve subordinate L1 | `saveRmIndentRequest()` → sets approvedStatus=3 | `POST /approvals/indents/{id}/approve` → smart-routes | Matches |
| Reject subordinate | `saveRmIndentRequest()` → approvedStatus=2 | `POST /approvals/indents/{id}/reject` | Matches |
| Indent list visibility | `getList()` (own) + `getRmList()` (subordinates) | `GET /api/v1/indents` (dept-scoped) | Partial |

**Note on Supervisor auto-skip:** In the legacy system, all role-4 users (Supervisor AND DeptHead are the same role) get their own indents auto-approved at L1. In the new system, `SUPERVISOR` is a separate role from `DEPTHEAD`. A SUPERVISOR creating their own indent goes through normal L1 flow — their indent lands in THEIR OWN reporting manager's queue. This is intentional and architecturally correct: a SUPERVISOR is an RM-level manager, not a DeptHead. They should not self-approve.

**Action required if legacy behavior is needed for Supervisors:** Verify with the business owner whether a SUPERVISOR creating their own indent should auto-skip L1 (legacy) or go through the RM of the supervisor (new). The new behavior is arguably more correct.

### DEPTHEAD (role=4 legacy, ROLE=DEPTHEAD new)

| Scenario | Legacy | New System | Status |
|----------|--------|-----------|--------|
| Create indent | `saveIndentRequest()` → Supervisor==4 branch → auto-skip L1 | `POST /indents` then submit → auto-skip L1 only if no supervisor in hierarchy | Matches with improved nuance |
| Auto-skip condition | `session["Supervisor"]==4` (all role-4 always) | `hasRoleByCode("DEPTHEAD") && !hasSupervisor()` | New is more precise |
| rmQty at creation | Auto-set = qty | Set during auto-approval in submitIndent() | Matches result |
| Own indent in L2 queue | `getDeptHeadList1()` tab3 — via OR clause (approvedBy=self) | `getPendingL2Approvals()` — dept-scoped, approvedStatus=3 — DeptHead's own self-approved included | Matches |
| Default list (getDeptHeadList) | Misses DeptHead's own indents | n/a | Gap in legacy; new system handles correctly |
| L2 approval queue | `getDeptHeadList()` — RM-approved subordinates | `getPendingL2Approvals()` — dept's approvedStatus=3 indents | Matches |
| Approve L2 | `saveFinalIndentRequest()` → finalStatus=4, procStatus=4 | `POST /approvals/indents/{id}/approve` → `finalApproveIndent()` | Matches |
| Reject L2 | `saveFinalIndentRequest()` → finalStatus=2, procStatus=2 | `POST /approvals/indents/{id}/reject` | Matches |
| Branch 1 (direct DeptHead action, approvedStatus still 1) | Sets BOTH approvedStatus and finalStatus | Not explicitly supported — frontend uses L1 queue then L2 queue | Gap — legacy DeptHead can act on pending-RM indents directly; new requires L1 first |

### PROCUREMENT (role=6 legacy, ROLE=PROCUREMENT new)

| Scenario | Legacy | New System | Status |
|----------|--------|-----------|--------|
| Queue | `getProcurementList()` — global, approvedStatus=3, finalStatus=4, procStatus=4 | `GET /api/v1/indents` + ApprovalController | Matches concept |
| Scoping | No user/dept scoping — everyone sees the same global queue | Non-admin scoped to deptId | **Gap — new system dept-scopes, legacy is global** |
| Sub-status progression | procurementStatus=5 through 9, filtered by roleId=6 employees | Separate procurement workflow | Matches concept |
| Assign to procurement person | procurementBy field + procStatus filter | procurementBy equivalent | Matches |

---

## Summary of Open Items

### Confirmed Matches
- Three-column status model (approvedStatus/finalStatus/procurementStatus) — identical values
- RM queue logic: subordinates' approvedStatus=1 indents
- DeptHead L2 queue logic: approvedStatus=3, finalStatus=1 indents
- DeptHead auto-skip: DEPTHEAD creates → approvedStatus=3 self-set
- Email notifications at each transition

### Behavior Differences (Intentional)
1. **SUPERVISOR creating own indent**: Legacy auto-skips L1 (role=4 = same as DeptHead). New system: SUPERVISOR goes through L1 (SUPERVISOR ≠ DEPTHEAD in new system). **Verify with business owner if this is acceptable.**
2. **DeptHead auto-skip condition**: Legacy = always for role-4. New = only if no supervisor in hierarchy. More correct.
3. **Procurement queue scoping**: Legacy = global. New = dept-scoped. New may show fewer indents to each procurement person.
4. **Create vs. Create+Submit**: Legacy creates and submits in one action. New system has explicit two-step (create draft, then submit). UX difference only.

### Gaps (Not Yet Handled in New System)
1. **DeptHead direct action on approvedStatus=1 indents**: Legacy `saveFinalIndentRequest()` Branch 1 allows DeptHead to act on indents still at the RM-pending stage. New system requires L1 approval before DeptHead sees the indent in their L2 queue.
2. **Employee "own indent" filtered list**: Legacy `getList()` shows only creator's own indents. New `filterIndents()` shows all department indents. A creator cannot easily see only their own indents without a creator filter param.
3. **Procurement person assignment and sub-status tabs** (5-9): Legacy has explicit `procurementBy` and tab filtering by roleId=6. New system has a different procurement workflow — mapping needs to be verified.

---

*Produced from direct source inspection. No implementation changes were made. Legacy source: `D:\New_ProcureZone\Net-Beans\Net-Beans\ProcureZone\src\java\seeds\indent\action\IndentAction.java` (1,827 lines). New system source: Spring Boot at `d:\New_ProcureZone\Net-Beans\Net-Beans\backend\src\main\java\com\nslindia\procurezone\indent\`.*
