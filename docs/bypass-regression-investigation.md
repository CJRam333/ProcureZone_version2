# Investigation — DeptHead bypass + Supervisor self-approval SQL crash

**Date:** 2026-07-27
**Status:** Investigation only. No fix implemented (Part 4 of the task).
**Method:** traced actual code + `git blame` (per docs/DEVELOPMENT_RULES.md — not from memory).

---

## Symptom A — DeptHead-raised indent lands at "Pending RM Approval" instead of skipping to Procurement

### The bypass logic (STEP 1)
`IndentService.submitIndent()` (lines ~745-768) is the only skip logic:
```java
boolean isDeptHead   = hasRoleByCode(currentUser.getEmpNumber(), "Department Head");
boolean hasSupervisor = employeeReportingRepository.hasSupervisor(currentUser.getEmpNumber());
if (isDeptHead && !hasSupervisor) {
    // copy qty -> rmQuantity
    indent.setApprovedBy(currentUser);
    indent.setApprovedByDate(now);
    indent.setApprovedStatus(ref(IndentStatus, 3));   // <-- ONLY approvedStatus is set
    indent.setRemarks("L1 Auto-approved (submitter is DEPTHEAD)");
}
```
Status column is set to 2 (Submitted) just above. **`finalStatus` and `procurementStatus` are left at 1.**

### Two distinct defects here
1. **Incomplete status-setting (definite code bug).** Even when the bypass fires it sets only
   `approvedStatus=3`, leaving `finalStatus=1`, `procurementStatus=1`. `deriveDisplayStatus(3,1,1)` =
   **"RM Approved"** (Dept-Head queue) — NOT Procurement. To land at Procurement the three-column
   state must be **approvedStatus=3, finalStatus=4, procurementStatus=4** (`deriveDisplayStatus(3,4,4)`
   = "Dept. Head Approved" → procurement stage). So the bypass, as written, can never reach
   Procurement — it stops one stage short.
2. **Detection likely not firing (explains the reported "Pending RM Approval").** The observed status
   is "Pending RM Approval" = `approvedStatus=1`, i.e. the `if` block did NOT run. That means
   `isDeptHead == false` OR `hasSupervisor == true`. `hasRoleByCode(empNumber, "Department Head")`
   (IndentService:2157) matches case-insensitively against `role.getCode()`. The frontend "you are a
   Dept Head → goes to Procurement" popup keys off the **normalized** role `DEPTHEAD`, but this
   backend check needs the raw role **code** to equal `"Department Head"`. If the role's code column
   is not literally "Department Head" (e.g. `DEPTHEAD`/`DEPARTMENT_HEAD`), `isDeptHead=false` and the
   bypass silently no-ops → indent stays at Pending RM Approval — exactly the symptom.

### Git blame (STEP 2)
The bypass block is from **`822e9c10` (CJRam_NSL, 2026-05-12)** — long before Pass 3 (`1d40759`,
Jul 25), the contamination fix (`119fdc9`, Jul 26) or Root-Cause-A/B (`aac36ef`, Jul 27). So the
incomplete-status bug is **old, not introduced by recent commits**. If this "used to work," the most
likely regression vector is the **role-code/normalization** changes around 2026-07-01/02
(`adcd7d0b`/`86da7be` role fixes) altering what `role.getCode()` returns, so `hasRoleByCode("Department Head")`
stopped matching. **Needs a data check (business owner):**
`SELECT role_id, role_code FROM tbl_role_master WHERE role_code LIKE '%epart%';` — confirm the exact
code string the DeptHead role carries.

---

## Symptom B — Supervisor self-approval throws the recursive-CTE SQL error

### The crashing query (STEP 3)
`EmployeeReportingHierarchyRepository.findReportingChain(empNumber)` (native, lines 86-99):
```sql
WITH RECURSIVE hierarchy AS (
    SELECT report_sub, report_sup, 1 as level
    FROM tbl_map_emp_reporting
    WHERE report_sub = :empNumber AND report_status = 1
    UNION ALL
    SELECT h2.report_sub, h2.report_sup, hierarchy.level + 1
    FROM tbl_map_emp_reporting h2
    INNER JOIN hierarchy ON hierarchy.report_sup = h2.report_sub
    WHERE h2.report_status = 1 AND hierarchy.level < 10
)
SELECT DISTINCT report_sup FROM hierarchy ORDER BY level   -- <-- BUG
```
`SELECT DISTINCT report_sup ... ORDER BY level`: `level` is not in the SELECT list, and combining
`DISTINCT` with `ORDER BY` on a non-selected column is rejected under `ONLY_FULL_GROUP_BY`/strict SQL
mode → *"Expression #1 of ORDER BY clause is not in SELECT list, references column 'hierarchy.level'
which is not in SELECT list; this is incompatible with DISTINCT."*

### The call path (which method + what it's for)
`IndentService.l1Approve()` (line ~825) → `ReportingHierarchyService.canApproveFor(creator, approver)`
→ `hierarchyRepository.findReportingChain(creator)` (ReportingHierarchyService:79). It's the
"can this approver approve this indent" authority check (finding the raiser's upward supervisor
chain). When a **Supervisor approves their own** indent, `canApproveFor(self, self)`: `isSubordinateOf(self,self)`
is false, so it falls through to `findReportingChain(self)` → the CTE runs → **crash**.

### Why it's a regression now (likely Pass 3)
Before Pass 3, the detail-page Approve button hit the remarks-only smart-route
`/approvals/indents/{id}/approve` → `approveIndent(...)`, which does **not** call `canApproveFor`
/`findReportingChain`. **Pass 3 (`1d40759`) rewired the Approve button to call `l1Approve`/`l2Approve`
directly** (to carry quantity edits) — and `l1Approve` DOES call `canApproveFor` → `findReportingChain`.
So Pass 3 newly exercises a **latent** CTE bug that the old path never reached. (The CTE SQL itself is
older; Pass 3 didn't change the query — it changed which endpoint the button calls.)

### Secondary issue on the same path (flag, not the crash)
Even after the SQL is fixed, `canApproveFor(self, self)` returns **false** (an employee is not their
own subordinate and not in their own chain) → `l1Approve` would then throw *"You are not authorized to
L1 approve this indent."* A top-of-chain Supervisor legitimately IS the RM for their own indent, so
the authority check needs an explicit "raiser is the top-level RM / no supervisor above them → may
self-approve" allowance. Otherwise fixing only the SQL turns a crash into an authorization error.

---

## STEP 4 — Do A and B share a root cause?
**No — adjacent, not shared.** They are different code paths:
- A lives in `submitIndent()`'s DeptHead auto-approval (three-column status setting). It never touches
  the reporting-chain CTE.
- B lives in `l1Approve()` → `canApproveFor()` → `findReportingChain()` (the CTE), plus the
  self-approval authority gap.

They share only a *theme* ("the raiser is also an approver at this level, needing correct routing"),
and they surface together because a DeptHead whose bypass fails to route to Procurement, and a
Supervisor who must self-approve, are both "raiser == approver" cases. But the fixes are independent.

---

## STEP 5 — Fix plan (NOT implemented)

### B1 — the SQL crash (`findReportingChain`)
Replace the `DISTINCT + ORDER BY level` with an `ONLY_FULL_GROUP_BY`-safe form. Preferred:
```sql
SELECT report_sup FROM hierarchy GROUP BY report_sup ORDER BY MIN(level)
```
(dedupes `report_sup`, orders nearest-supervisor-first; `report_sup` is grouped, `MIN(level)` is an
aggregate — strict-mode safe). Alternative: drop `DISTINCT`, `SELECT report_sup, level ... ORDER BY level`,
and de-duplicate in Java preserving order. Location: `EmployeeReportingHierarchyRepository` line 97.

### B2 — self-approval authority (same path, needed so B1 doesn't become a 403)
In `ReportingHierarchyService.canApproveFor(...)` (or the l1Approve gate), allow the case where
`approver == employee` AND the approver is a top-level RM (e.g. `!hasSupervisor(approver)` or has the
SUPERVISOR/RM role and sits at the top of their own chain). Confirm the intended business rule with
the owner before implementing.

### A — DeptHead bypass to Procurement (`submitIndent`)
1. **Fire reliably:** replace `hasRoleByCode(empNumber, "Department Head")` with a check against the
   caller's normalized roles (e.g. the `UserPrincipal.roles()` set contains `DEPTHEAD`), matching how
   the frontend popup decides — so backend detection can't drift from the role-code string. (Confirm
   the actual role code via the SQL above; if it's a code mismatch, this is the whole fix for
   "lands at Pending RM Approval".)
2. **Route to Procurement, not the Dept-Head queue:** when the bypass fires, set the full three-column
   state so `deriveDisplayStatus` yields "Dept. Head Approved" (procurement stage):
   ```java
   indent.setApprovedStatus(ref(IndentStatus, 3));   // RM auto-approved
   indent.setFinalStatus(ref(IndentStatus, 4));      // Dept-Head auto-approved   <-- ADD
   indent.setProcurementStatus(ref(IndentStatus, 4)); // arrived at procurement    <-- ADD
   indent.setFinalApprovedBy(currentUser);           // record who auto-approved L2 <-- ADD
   indent.setFinalApprovedDate(now);
   ```
   Location: `IndentService.submitIndent()` lines ~750-768.

### Sequencing suggestion
B1 (SQL) is the highest-priority, lowest-risk, isolated fix (a data-leak-free one-query change that
unblocks Supervisor self-approval from crashing). B2 and A involve business-rule confirmation
(self-approval allowance; the DeptHead role-code check) and should be confirmed with the owner first.

*(All are read-only findings — no code changed in this task.)*
