# Quantity-Edit During Approval — Investigation (Pass 3 prep)

**Date:** 2026-07-24
**Status:** Investigation only. No code changed.
**Scope:** How legacy handled RM / DeptHead / Procurement / Stores line-item quantity editing
with (or without) an audit trail; what the current Spring Boot + React system has; and a clean
target design.

---

## TL;DR — the three surprises

1. **Legacy already preserved per-stage quantities for indents** in three separate columns
   (`indent_details_qty`, `indent_details_rm_qty`, `indent_details_dept_qty`). The "single qty
   column overwritten each stage" worst-case does **not** apply to indents.
2. **Procurement never edits quantity** in legacy — there is no PO-quantity field or column. The
   PO is always cut at the DeptHead quantity. So an example like *"Requested 10 → RM 8 → DeptHead 8
   → PO 5"* was **impossible** in legacy; the most it supported was *10 → 8 → 8*.
3. **The current system already contains a complete, correct per-stage indent quantity-edit
   feature — but it is dark code.** `l1Approve`/`l2Approve` accept per-line adjustments, validate
   monotonic decrease, preserve the original, and stamp editor+timestamp. The React approve button,
   however, calls a *different* endpoint (`/approvals/indents/{id}/approve`) that takes **remarks
   only** and writes no quantity. The feature is built and unreachable.

The opposite worst-case **does** apply to legacy **issue notes**: at the RM stage the requested-qty
input is freely editable and writes back onto `issue_note_details_requested_quantity` **in place**,
with no separate RM-approved column and no visible history — the requester's original number is
silently destroyed.

---

## STEP 1 — Legacy behaviour: who can edit line-item quantity, at which stage?

Evidence: JSP form fields (editable `<input>`/`<html:text>` vs read-only `<s:property>`), the
Struts action mappings, and the Hibernate `.hbm.xml` mappings. Compiled `.class` business logic was
not decompiled; behaviour is inferred from the form contract + mappings.

### Indents (`WEB-INF/jsp/indent/`, config `.../seeds/indent/strutsConfig/indent-config.xml`)

| Stage | Editable? | Field submitted → column | Original preserved? | Editor + timestamp |
|---|---|---|---|---|
| **RM** (`seeds_indent_request_rm.jsp` → `saveRmIndentRequest`) | **YES** | `rmQty` → `indent_details_rm_qty` (input pre-filled from original qty). "Quantity Requested" shown read-only. | YES — original stays in `indent_details_qty` | Master-level only: `indent_approvedby` + `indent_approvedby_date`. Line row only has the generic `indent_details_lmu`/`_lmd` (overwritten). |
| **DeptHead / L2** (`seeds_indent_request_dept_head.jsp` → `saveFinalIndentRequest`) | **YES** | `deptQty` → `indent_details_dept_qty` (pre-filled from RM qty, falling back to original). Requested + RM qty shown read-only. | YES — original + RM qty untouched | Master-level: `indent_final_approvedby` + `indent_final_date`. Line row: generic `lmu`/`lmd` only. |
| **Procurement** (`seeds_indent_request_procurement.jsp` → `saveProcurementIndentRequest`) | **NO (qty)** | Quantity columns all read-only. Only **`pricing` → `indent_details_pricing`** is editable per line. Sub-stages (Quotations/Negotiation/PO Released/Cash Buy) are values of a single `indentStatusId` dropdown on one form — none exposes a qty input. | n/a (qty not touched) | Master: `indent_procurementby` + `indent_procurement_status`; per-event rows in `tbl_indent_procurement_logs` (status, remarks, user, date) — **no quantity logged**. |

Procurement sub-stage JS just toggles which header fields show (status 7 = PO Released reveals PO
Number + Delivery Date + Pricing; status 9 = Cash Buy reveals Delivery Date + Pricing, hides PO
Number). No quantity anywhere in procurement.

### Issue notes (`WEB-INF/jsp/issuenote/`, config `.../seeds/issue/strutsConfig/IssueNote-config.xml`)

> Note: a parallel `WEB-INF/jsp/goodsreceipt/` issue-note tree exists but is **dead** — the config
> routes exclusively to `issuenote/`, and the `goodsreceipt/` filenames don't even match the config.

| Stage | Editable? | Field submitted → column | Original preserved? | Editor + timestamp |
|---|---|---|---|---|
| **RM approval** (`seeds_issue_note_request_rm.jsp` → `saveRmIssueNoteRequest`) | **YES** | `reqqty` → **`issue_note_details_requested_quantity`**, edited **in place**. "Balance in Stores" read-only. No separate RM-approved column. | **NO** — RM overwrites the requested qty column directly. The only column that might still hold the original (`issue_note_details_quantity`) is never surfaced in any UI. | Line row: single `issue_note_details_lmu`/`_lmd` (overwritten). No RM-specific fields. |
| **Stores confirmation / issue** (`seeds_issue_note_request_stores.jsp` → `saveStoresIssueNoteRequest`) | **YES (issued qty)** | `issqty` → **`issue_note_details_issued_quantity`** (defaults to requested; JS blocks issue > requested and issue > balance). "Requested Quantity" is **read-only** here. | YES at this stage — requested qty read-only; issued goes to its own column. | Line row: single `lmu`/`lmd` (overwritten). |

---

## STEP 2 — Legacy database schema for edited quantities

Column names live only in the Hibernate mappings under `WEB-INF/classes/pojo/` (no `.sql` dump,
no embedded SQL). Catalog `seeds_indent`.

### `tbl_indent_details` (`TblIndentDetails.hbm.xml`)

- `indent_details_qty` — **original requested qty** (NOT NULL, requester)
- `indent_details_rm_qty` — **RM-adjusted qty** (nullable)
- `indent_details_dept_qty` — **DeptHead/L2-adjusted qty** (nullable)
- `indent_details_pricing` — procurement pricing (nullable) — *not a quantity*
- `indent_details_stock_aval` — available stock
- plus: `indent_details_id` (PK), `indent_id`, `indent_details_material`, `indent_details_umo`,
  `indent_details_purpose`, `indent_details_vendor`, `indent_details_status`,
  `indent_details_lmu` (last-modified user — overwritten each stage),
  `indent_details_lmd` (last-modified date — overwritten each stage)

**Verdict:** three per-stage quantity columns, one per stage. **No procurement/PO quantity column.**
Effective final approved qty = `indent_details_dept_qty`. **No per-stage editor/timestamp on the
line** — only one overwritten `lmu`/`lmd` pair, so you cannot tell from the row who set rm_qty vs
dept_qty, or when.

### `tbl_issue_note_details` (`TblIssueNoteDetails.hbm.xml`)

- `issue_note_details_quantity` — creation/original qty (generic) — **never shown in any live form**
- `issue_note_details_requested_quantity` — requester's ask; **edited by RM in place**
- `issue_note_details_issued_quantity` — **actual issued by Stores**
- `issue_note_details_quantity_stores` — balance/stock in stores
- `issue_note_details_opening_quantity` — opening stock
- `issue_note_details_balance_inventory` — balance inventory
- plus PK/FKs/status and single `issue_note_details_lmu` / `issue_note_details_lmd`

**Verdict:** multiple per-stage columns exist, **but the RM stage overwrites
`requested_quantity` in place** (no dedicated RM-approved column), so the RM edit is *not* audit-
preserving. Stores writes a distinct `issued_quantity`. No per-stage editor/timestamp on the line.

---

## STEP 3 — Legacy UI: how were edited quantities displayed?

**No true history and no change indicator anywhere.** The `*_view.jsp` pages render each stage's
column as a plain `<s:property>` cell:

- **Indents:** the RM/DeptHead/Procurement view JSPs show three side-by-side columns —
  *Quantity Requested | Rm Quantity Requested | Dept. Head Quantity Requested* (procurement view
  adds read-only Pricing). A viewer can see `10 / 8 / 8` across columns, but there is **no
  arrow-style `10 → 8` trail, no colour, no tooltip, no "changed" flag** — a reduced qty looks
  identical to an unchanged one. The requester's own view (`seeds_indent_request_self_view.jsp`)
  shows only a single "Quantity Requested" column — the requester never sees the adjustments.
- **Issue notes:** the view JSPs show only the current values (requested + stores balance; the
  stores view adds opening/issued/balance). No before/after, and `issue_note_details_quantity` is
  literally commented out of the stores view.

So legacy's "audit" is at best a set of parallel columns, with no editor identity, no per-stage
timestamp, and (for issue-note RM) not even value preservation.

---

## STEP 4 — Current new-system state

### Entities (map the same legacy tables)

**`IndentDetail`** (`backend/.../indent/IndentDetail.java`) — `tbl_indent_details`
- `quantity` → `indent_details_qty` (NOT NULL) — original requested
- `rmQuantity` → `indent_details_rm_qty` (nullable) — RM/L1 adjusted
- `deptQuantity` → `indent_details_dept_qty` (nullable) — DeptHead/L2 adjusted
- `stockAvailable` → `indent_details_stock_aval`
- line audit: `lastModifiedBy` (`indent_details_lmu`), `lastModifiedDate` (`indent_details_lmd`) —
  single last-modified, **not per-stage**

**`IssueNoteDetails`** (`backend/.../issuenote/IssueNoteDetails.java`) — `tbl_issue_note_details`
- `quantity` → `issue_note_details_quantity` (NOT NULL)
- `quantityStores` → `issue_note_details_quantity_stores` (nullable) — **dead after creation**
- `rate`/`amount` → pricing
- line audit: `lastModifiedBy`/`lastModifiedDate` — single last-modified
- Note: the new entity did **not** map the legacy `requested_quantity` / `issued_quantity` columns —
  it collapsed the issue-note quantity model to `quantity` (+ unused `quantityStores`).

### Item DTOs
- **`IndentDetailResponse`** exposes `quantity`, `rmQuantity`, `deptQuantity` — all three surfaced.
- **`IssueNoteDetailsDTO`** exposes only `quantity`, `rate`, `amount` (`quantityStores` not exposed).

### Approval endpoints — do they accept edited quantities?

**Indents — capability EXISTS but is unwired:**
- `L1ApprovalRequest` carries `List<LineItemAdjustment>` = `{detailId, rmQuantity (@Positive
  @NotNull), rmRemarks}`. `IndentService.l1Approve` validates `rmQuantity <= quantity`, writes
  `rmQuantity`, preserves `quantity`, stamps line `lastModifiedBy/Date`; if none supplied, copies
  `quantity → rmQuantity`. Wired to `POST /api/v1/indents/{id}/l1-approve`.
- `L2ApprovalRequest` carries `{detailId, deptQuantity, deptRemarks}`. `l2Approve` validates
  `deptQuantity <= rmQuantity`, writes `deptQuantity`, preserves prior, stamps editor/time. Wired to
  `POST /api/v1/indents/{id}/l2-approve`.
- `ProcurementUpdateRequest` carries **no line items** (only `procurementSubStatus, poNumber,
  deliveryDate, remarks`); `updateProcurementStatus` writes no quantity.
- **The UI does not call any of these.** The React approve button hits the smart-router
  `ApprovalController` `POST /api/v1/approvals/indents/{id}/approve?remarks=…`, which routes to
  `approveIndent`/`finalApproveIndent`/`procurementApproveIndent` — **remarks only, no body, no
  quantity write.** (`ApproveIndentRequest` on the client even declares an optional
  `items?: {itemId, approvedQuantity}[]`, but the api client never populates or sends it.)

**Issue notes — no capability at any layer:**
- `ApproveIssueNoteRequest` (rm-approve) and `IssueGoodsRequest` (issue) carry **only `remarks`**.
- `IssueNoteService.rmApprove` writes status/approver/remarks; `issueGoods` sets status/storesBy/
  remarks, validates stock against `quantity`, but **writes no quantity column** (and by design does
  not even decrement stock). `quantityStores` is set only at creation.

### Frontend — editable during approval?
- **`IndentDetailPage.tsx`**: items table renders qty as read-only `<td>{qty}</td>`; columns are
  #, Material, Description, Company, UOM, Qty, Est. Value, Remarks — **no rm/dept columns, no
  history, no inputs**. Approve action sends `{ remarks }` only.
- **`IssueNoteDetailPage.tsx`**: renders `{item.quantity}` read-only; RM-approve and the issue modal
  send/show remarks + read-only items. **No editable qty, no requested-vs-approved history.**

**Net:** Indents = full per-stage edit built server-side (original preserved, monotonic validation,
single last-modified stamp) but **dark**; audit is coarse (one `lmu`/`lmd`, no per-stage who/when).
Issue notes = **not implemented** anywhere.

---

## STEP 5 — Design proposal (clean target; NOT implemented)

Goal: RM, DeptHead, and Procurement can each adjust line-item quantities **at their own stage only**,
the original request is never lost, every change records **who / when / why**, and the detail page
shows a clean per-item history. Same shape for issue notes (RM adjust + Stores issued qty).

### 1. Schema

Keep the existing per-stage value columns (they already exist and are correct):
- Indents: `indent_details_qty` (original), `indent_details_rm_qty`, `indent_details_dept_qty`.
  **Add** `indent_details_po_qty` (nullable) so Procurement can cut a PO quantity below the DeptHead
  quantity (legacy could not; the task's `→ PO 5` example requires this). Effective final qty =
  `po_qty ?? dept_qty ?? rm_qty ?? qty`.
- Issue notes: introduce a real two-column model — `issue_note_details_rm_qty` (RM-approved,
  **new**, so the original `quantity` is never overwritten) and reuse/repurpose an issued-qty column
  (`issue_note_details_issued_quantity`, present in the legacy table but unmapped today) for Stores.

For true who/when/why history, prefer a **dedicated line-item audit table** over piling per-stage
`*_by`/`*_date` columns onto the details row (the single `lmu`/`lmd` is the root cause of today's
lost audit):

```
tbl_indent_detail_qty_log        (mirror: tbl_issue_note_detail_qty_log)
  id                PK
  indent_details_id FK
  stage             enum: RM | DEPT_HEAD | PROCUREMENT   (RM | STORES for issue notes)
  old_qty           decimal
  new_qty           decimal
  changed_by        FK emp_number
  changed_at        datetime
  reason            varchar(500)   -- required when new_qty != old_qty
```

One migration (next Flyway `V58+`) to add `indent_details_po_qty`, the issue-note `rm_qty` column,
and the two log tables. `AuditService` already exists and can persist the log rows, or the new
tables can be written directly by the approval methods.

### 2. API

Extend the **actually-used** approval path (either switch the UI to `/l1-approve`,`/l2-approve` — the
already-built endpoints — or teach `ApprovalController` to forward a body). Each approve carries:

```jsonc
{ "remarks": "…",
  "items": [ { "detailId": 123, "quantity": 8, "reason": "excess vs need" } ] }   // reason required iff changed
```

- RM approve → writes `rm_qty` + a `stage=RM` log row.
- DeptHead approve → writes `dept_qty` + a `stage=DEPT_HEAD` log row.
- Procurement (new PO-qty step) → writes `po_qty` + a `stage=PROCUREMENT` log row.
- Issue-note RM approve → writes `rm_qty`; Stores issue → writes issued qty. Both log.

Reuse the existing `LineItemAdjustment` DTO shape; add `reason`. Server validates each item belongs
to the document and the monotonic rule below.

### 3. Permission model (stage-locked, no retroactive edits)

- A stage's actor may edit **only that stage's column**, and **only while the document is in that
  stage** (`approvedStatus`/`finalStatus`/`procurementStatus` gate). Enforced server-side, not just
  in the UI.
- Monotonic non-increase (extends today's `l1Approve`/`l2Approve` checks):
  `qty ≥ rm_qty ≥ dept_qty ≥ po_qty`. An approver can reduce or keep, never increase beyond the
  prior stage.
- No approver can edit a column for a stage that has passed (their status has advanced) or a stage
  that hasn't been reached. ADMIN/SUPERADMIN override, if allowed, must itself be logged.

### 4. Frontend — editable during your turn, read-only after

- On the detail page, when the current user is the approver for the current stage (reuse the
  existing `canApprove` stage detection in `IndentDetailPage`), render the current stage's quantity
  cell as a numeric `<input>` (pre-filled from the prior stage's value, `max` = prior value) plus an
  inline **reason** field that becomes required once the value differs. Submit collects the
  `items[]` and posts them with the approve action.
- Otherwise the quantity column is read-only.

### 5. Audit display (per-item history)

Render a compact per-line progression from the log table:

```
Material X:  Requested 10  →  RM 8 (A. Rao, 12 Jul)  →  DeptHead 8  →  PO 5 (S. Iyer, 14 Jul)
```

- Show only the stages that occurred; a reduced value is visually flagged (muted strike on the
  prior value or a down-arrow badge) with the reason in a tooltip. This is the piece legacy never
  had — the columns existed but were never shown as a trail, with no editor/time and no reason.

---

## Recommended sequencing for Pass 3 (when implementation is authorised)

1. **Cheapest high-value win:** wire the existing indent `l1Approve`/`l2Approve` capability to the
   UI (it is already built and correct). This alone restores RM/DeptHead quantity editing with
   original preservation.
2. Add the per-stage audit log table + reason field (fixes the "who/when/why" gap that exists even
   in the dark code).
3. Add procurement PO-qty (`indent_details_po_qty`) + step.
4. Build the issue-note RM/Stores quantity model (currently absent) to match.
5. Add the audit-history display to both detail pages.

*(All DB migrations to be run by the business owner on the server per the standing no-DB-access
constraint.)*
