# Email Notification — Legacy vs. New System Investigation

**Date:** 17 August 2026
**Status:** Investigation only. No code changed. This is the basis for a subsequent build.
**Scope:** How email notifications work in legacy ProcureZone, what exists in the new
Spring Boot system today, the gap between them, and a sequenced recommendation.

**Codebases examined**
- Legacy: `D:\New_ProcureZone\ProcureZone` — a **deployed/compiled WAR** (no `.java` source;
  155 `.class` files under `WEB-INF/classes`). Findings below were obtained by decompiling the
  relevant classes (CFR 0.152). Cited line numbers are from the **decompiled** output; class
  paths are exact. All quoted strings are byte-for-byte from the class constant pools.
- New: `D:\New_ProcureZone\Net-Beans\Net-Beans` — backend under `backend/`, all email routed
  through one class `notification/service/EmailService.java`.

> **Verification honesty (Rule 7).** Nothing below was runtime-verified against a live mail
> server. Legacy facts come from decompiled bytecode; new-system facts from source + git +
> migrations. Where the deployed DB may differ from the repo, it is flagged explicitly.

---

## STEP 1 — Every email trigger point in LEGACY

Legacy has exactly **one** low-level sender, `seeds.indent.mailService.EmailConfiguration`
(`getSendEmail`, `getSendEmailPDF`, `getSendEmailPDF1`), reused by two orchestrators:
`IndentNotification` (indent + plant/Pz workflows) and `IssueNotification` (issue notes, which
imports the same `EmailConfiguration`). Recipients are resolved by `IndentEmail`/`IssueEmail`
via **HQL string-concatenation queries** against employee/reporting/role tables (no hardcoded
addresses). Bodies are **hardcoded HTML strings** built by `IndentMessage`/`IssueMessage`.

A binary string scan of all `.class` and `.jsp` files confirmed only these classes touch
`javax.mail`: `EmailConfiguration`, `IndentNotification`, `IssueNotification`, `IndentAction`,
`IssueNoteAction`, `IssueNoteAction1`, `PlantIndentAction`, `PlantIssueNoteAction`. **No JSP sends
mail; there is no generic `EmailUtil`/`MailUtil`.**

### 1A. Main indent (procurement) workflow — `IndentNotification`

| # | Business event | TO | CC | Subject | Content source |
|---|---|---|---|---|---|
| 1 | **Indent submitted/created** (`IndentAction.saveIndentRequest`) | **RM** (reporting superior of requester) | requester | `New Indent Request-<no>` | hardcoded HTML (`IndentMessage.getNewIndentRequest`) |
| 2 | **RM rejects** (`saveRmIndentRequest`, status==2) | requester | RM | `Rejected Indent Request-<no>` | hardcoded HTML |
| 3 | **RM approves** (`saveRmIndentRequest`, else) | **Dept Head** (superior of the approver) | RM | `Approved Indent Request-<no>` | hardcoded HTML |
| 4 | **Dept Head final-rejects** (`saveFinalIndentRequest`, status==2) | RM | Dept Head's superior | `Final Rejected Indent Request-<no>` | hardcoded HTML |
| 5 | **Dept Head final-approves** (`saveFinalIndentRequest`, else) | **Procurement group** (`roleId=6`) | Dept Head's superior | `Final Approved Indent Request-<no>` | hardcoded HTML **+ PDF attachment** (`getSendEmailPDF1`) |

Dead/unwired: `ProcurementIndentRequestNotification` (a full "Procurement Indent Request" email,
TO=requester+RM, CC=procurement) exists but **is never called** — `saveProcurementIndentRequest()`
contains no send. → Procurement/PO-status-change email was coded but disabled in legacy.

### 1B. Plant / "Pz" (seed-plant inventory) workflow — `IndentNotification`, from `PlantIndentAction`

A parallel indent lifecycle for plant inventory. Five triggers, each routing to the next actor
in the plant chain (superior/inventory-manager/floor roles): **Pz indent submitted**,
**DEO order raised**, **floor-manager inventory issue**, **floor-incharge issue receipt**,
**GRN (goods receipt number)**. Recipients resolved by `getPzRmEmailId` / `getPzInvMgrEmailId`
against `TbPzlIndentMastera`. `NewPzIssueConfirmation`/`NewPzReceiptConfirmation` templates are
defined but **have no caller** (dead).

> The new system has **deactivated the plant-indent / confirmations modules** (migrations V52,
> V57). So unless the business wants them back, the entire 1B set is **out of scope** for the new
> build. **Flag for confirmation.**

### 1C. Issue note (stores requisition) workflow — `IssueNotification`

| Business event | TO | CC | Subject |
|---|---|---|---|
| **Issue note submitted** | **Stores** (`roleId=6`) if session role==4, else **RM** | requester | `New Issue Note Request-<no>` |
| **RM approves** | **Stores** (`roleId=6`) | RM **and** requester | `Approved Issue Note Request-<no>` |
| **RM rejects** | requester | RM | `Rejected Issue Note Request-<no>` |
| **Stores rejects** | requester (+RM if present) | Stores | `Stores Rejected Issue Note Request-<no>` |
| **Stores issues goods** | requester | Stores | `Stores Goods Issued Note Request-<no>` |

### 1D. Recipient-resolution semantics (the important part)

Legacy follows a **"route-to-the-next-actor, CC the previous actor"** model:
- **Requester** = `empEmail` of the indent/issue creator.
- **RM** = the reporting **superior** of the requester (`TblMapEmpReporting`).
- **Final RM / Dept Head** = the superior of *whoever approved* (chain climbs one more level).
- **Procurement / Stores** = **role-based, hardcoded `roleId=6`** (all active employees with that role).
- Issue-note RM lookup **excludes `roleId=5`** employees.

Role **names** for `roleId=5`/`6` could not be confirmed — the provided SQL dump lacks
`tbl_roles_master` (mapped in `pojo/TblRolesMaster.hbm.xml`, columns `role_id/role_code/role_name/role_view`). **Needs the live roles table.**

### 1E. Data interpolated into legacy emails
Indent header (year, date, employee+id, company, indent no, dept, section, plant, comments),
approver/date/remarks on approval mails, PO number + delivery date on procurement mail; per-line
material+desc, UMO, purpose, vendor, quantity (requested/RM/dept-head), available stock. Issue
notes add issue-note no and per-line requested/opening/issued/stores quantities. Every mail
hardcodes an app link `http://procurezone.nslgroup.in/ProcureZone/` and the footer
*"This is a Computer-genarated e-mail, please do not reply to this message."*

### Legacy events that send **NO** email (confirmed by binary scan)
Employee/user creation, login/**password reset**, LDAP, goods receipt (GRN), and **any
vendor/quotation** email — none exist in legacy. If the new system wants these, they are
**net-new**, with no legacy behavior to port. The SAP-CSV cron classes under `seeds/issue/mailService/`
do **not** send mail (Quartz import jobs only).

### Legacy defects to explicitly NOT replicate
- Impossible recipient guard `if ("".equals(s) && s == null)` (never true).
- **Empty `catch (MessagingException)` blocks — send failures silently swallowed** (violates our Rule 9).
- HQL built by string concatenation (injection-prone).
- Unescaped user input concatenated into HTML bodies (injection/XSS-in-email).
- Malformed From header (`Procure<ezone@nslgroup.co.in>`).

---

## STEP 2 — Legacy template storage & format

**There is none.** No template table (no `tbl_email_template`/`email_template`/`mail_template`),
no `.vm`/`.ftl`/`.html` template files, **no placeholder syntax at all**. Every subject and body
is a hardcoded Java string assembled by `+` concatenation of POJO getters inside `IndentMessage`
and `IssueMessage`. "Substitution" = literal string concatenation; there is no engine and no
`{{}}`/`${}`/`#name#`/`%name%` tokens. Bodies are HTML tables color-coded by event (new=red
`#C7241D`, approved=green `#00cc00`, Pz=blue `#3399ff`), with leftover `System.out.println` debug
lines in the Pz methods.

→ **Implication:** there is no legacy placeholder format to match. The new system's `${...}`
template-table approach is a clean improvement, not a divergence from a legacy standard.

---

## STEP 3 — Legacy SMTP / mail server config

**All hardcoded in Java** (`EmailConfiguration`); no properties/XML config anywhere.

| Setting | Legacy value |
|---|---|
| Host | `172.16.65.66` (internal RFC-1918 LAN relay) |
| Port | `25` (plain SMTP) |
| Auth | username `""`, password `""` — anonymous relay |
| Encryption | none (no STARTTLS/SSL) |
| From | `ezone@nslgroup.co.in` (display name malformed in code) |

→ Internal-only relay, unreachable off-LAN, no credentials. **Business owner must confirm the
current/correct mail server** — legacy values are very likely stale.

---

## STEP 4 — Current NEW-system state

### 4.1 Infrastructure
- **`EmailService`** (`notification/service/EmailService.java`) is the single send path. Key methods:
  `sendEmailFromTemplate(templateCode, Map vars, String toAddress)` (used by all workflow callers,
  **single recipient, no CC**) and `sendEmail(EmailRequest)` (low-level, supports To/Cc/Bcc + attachments).
- **REQUIRES_NEW fix — PRESENT ✅.** Both `sendEmailFromTemplate` and `sendEmail` are
  `@Transactional(propagation = Propagation.REQUIRES_NEW)`, with a comment stating the intent
  (a mail/audit failure can never mark the caller's business transaction rollback-only).
  Corroborated by the stabilization log (2026-07-04, the issue-note-creation 500/rollback bug + V49).
- **Error handling — fail-loud, not swallowed, not propagated ✅.** `MessagingException`/`Exception`
  are caught, logged with full stack trace, the log row is marked `FAILED`, and the method returns
  `false` (no rethrow). Every workflow caller additionally wraps the call in its own try/catch that
  only logs. (Contrast with legacy's empty catch blocks.)
- **`tbl_email_log`** (`EmailLog.java`): one row per attempt, written `PENDING` **before** send,
  updated to `SENT` (with `sentDate`) or `FAILED` (with `errorMessage`) after. `triggeredBy` is
  always `"SYSTEM"`.

### 4.2 Templates
- **`tbl_email_template`** (`EmailTemplate.java`): code, name, subject, body, type, category,
  description, status, placeholders (JSON), is_html, lmd, lmu.
- **Placeholder syntax = `${variable}`**, substituted by regex `\$\{([^}]+)\}` in
  `EmailService.replacePlaceholders`. Null-valued vars are left as literal `${...}` in the output.
- **Migration V47** (`V47__add_approval_email_templates.sql`) seeds the **11 workflow templates**
  and fixed two bugs: (1) **placeholder format** — `${...}` bodies collided with Flyway's own
  placeholder replacement; fixed by a global `FlywayConfigurationCustomizer`
  (`placeholderReplacement(false)`) + `placeholder-replacement: false` in yml; (2) **`template_lmu`
  type** — changed the seeded value from `'SYSTEM'` to numeric `1` (commit "V47 template_lmu is INT
  not VARCHAR"). Both confirmed in git history and the current file.
- Seeded workflow templates: `INDENT_CREATED`, `INDENT_L1_APPROVED`, `INDENT_L1_REJECTED`,
  `INDENT_L2_APPROVED`, `INDENT_L2_REJECTED`, `INDENT_CANCELLED`, `ISSUE_NOTE_CREATED`,
  `ISSUE_NOTE_RM_APPROVED`, `ISSUE_NOTE_RM_REJECTED`, `ISSUE_NOTE_ISSUED`, `ISSUE_NOTE_STORES_REJECTED`.
- The four original V20/V21 templates (`SAP_IMPORT_SUCCESS`, `SAP_IMPORT_FAILURE`,
  `MATERIAL_QUANTITY_REPORT`, `LOW_STOCK_ALERT`) use **single-brace** `{filename}` placeholders that
  the `${...}` regex will **not** render.

### 4.3 Which triggers actually send email today

| Business event | Sends today? | Recipient (new) | Template |
|---|---|---|---|
| Indent submit/create | **NO** | — | `INDENT_CREATED` seeded but **never called in code** |
| Indent L1 (RM) approve | YES | indent **creator** | `INDENT_L1_APPROVED` |
| Indent L1 (RM) reject | YES | creator | `INDENT_L1_REJECTED` |
| Indent L2 (Dept Head) approve | YES | creator | `INDENT_L2_APPROVED` |
| Indent L2 (Dept Head) reject | YES | creator | `INDENT_L2_REJECTED` |
| Indent cancel | YES | creator | `INDENT_CANCELLED` (no legacy equivalent) |
| Indent procurement status update | **NO** | — | — |
| Issue note create/submit | YES* | **creator** (template text addresses the approver) | `ISSUE_NOTE_CREATED` |
| Issue note RM approve | YES | creator | `ISSUE_NOTE_RM_APPROVED` |
| Issue note RM reject | YES | creator | `ISSUE_NOTE_RM_REJECTED` |
| Issue note goods issued | YES | creator | `ISSUE_NOTE_ISSUED` |
| Issue note stores reject | YES | creator | `ISSUE_NOTE_STORES_REJECTED` |
| Issue note return | **NO** | — | — |
| PO sent to vendor | **fails** | vendor email | `PO_SENT_TO_VENDOR` — **template not seeded anywhere** → lookup fails, skipped |
| Employee created / password reset / LDAP / GRN-QC | **NO** | — | (none in legacy either) |
| Scheduled jobs (reorder alert, pending-indent reminder, daily/monthly summaries, cleanup) | **fail** | team/admin | templates seeded only in **`V26…sql.disabled`** → never loaded → lookup fails |
| PO delivery reminder / inventory reconciliation jobs | **NO** | — | job classes exist but **never registered** in the scheduler |

`*` fires but see recipient issue below. **Net:** only the **5 indent + 5 issue-note** events have a
real seeded template and can render. Everything else either isn't wired, isn't scheduled, or fails
template lookup. Three `notification.service.*NotificationService` classes call `sendEmail(...)` but
are **dead code** (no class injects them).

### 4.4 SMTP config (new)
Only in `application.yml` (`spring.mail`): host `${SMTP_HOST:172.16.65.65}`, port `25`,
username/password env-driven with **empty defaults**, `mail.smtp.auth=false`,
`starttls.enable=false`. **No `from`/from-address anywhere** and `EmailService` never calls
`setFrom(...)`. `application-prod.yml` does **not** redefine `spring.mail` (inherits the same
defaults; real prod config is a manually-maintained out-of-repo `/opt/procurezone/application-prod.yml`).
Note the host differs from legacy by one digit: new `…65.65` vs legacy `…65.66`.

### 4.5 Verification evidence — **BUILT-BUT-UNVERIFIED**
No mail tests, no GreenMail/MailHog dependency or harness, no `tbl_email_log` seed/test rows, no
"untested" markers. The stabilization log mentions email only around the rollback bug (V49 +
REQUIRES_NEW) — i.e. it confirmed the **log INSERT** works, never that a real email was **delivered**.
This is the same "built-but-unverified" posture flagged earlier for the notification-bell system.

---

## STEP 5 — Gap analysis

| Legacy trigger | In legacy? | In new? | Recipient matches legacy? | Template matches legacy intent? | Gap |
|---|---|---|---|---|---|
| Indent submitted → RM (CC requester) | ✅ | ❌ (`INDENT_CREATED` seeded, never called) | — | template exists | **MISSING trigger.** No "new indent pending approval" email to the RM. |
| Indent RM approve → Dept Head (CC RM) | ✅ | ⚠️ sends to **creator** only | **NO** — legacy routes to next approver (Dept Head) + CC RM | partial | **Recipient wrong + no CC.** Next approver never notified. |
| Indent RM reject → requester (CC RM) | ✅ | ✅ sends to creator | TO matches; **CC missing** | ✅ | Minor: missing CC to RM. |
| Indent Dept Head approve → Procurement `roleId=6` (+PDF) | ✅ | ⚠️ sends to **creator** only | **NO** | partial | **Procurement group not notified; PDF attachment not implemented.** |
| Indent Dept Head reject → RM (CC superior) | ✅ | ⚠️ sends to **creator** only | **NO** | partial | **Recipient wrong.** |
| Indent cancel | ❌ | ✅ → creator | n/a (new-only) | n/a | New-only; fine. |
| Indent procurement-status change | ⚠️ coded but disabled | ❌ | — | — | Neither active. Confirm if wanted. |
| Issue note submitted → Stores/RM (CC requester) | ✅ | ⚠️ sends to **creator** only | **NO** — legacy routes to Stores or RM | partial | **Approver/stores never notified; recipient inverted.** |
| Issue note RM approve → Stores (CC RM+requester) | ✅ | ⚠️ **creator** only | **NO** | partial | **Stores not notified.** |
| Issue note RM reject → requester (CC RM) | ✅ | ✅ creator | TO matches; CC missing | ✅ | Minor: missing CC. |
| Issue note stores reject → requester (+RM) (CC Stores) | ✅ | ✅ creator | TO matches; CC missing | ✅ | Minor: missing CC. |
| Issue note goods issued → requester (CC Stores) | ✅ | ✅ creator | TO matches; CC missing | ✅ | Minor: missing CC. |
| Plant/Pz workflow (5 triggers) | ✅ | ❌ (modules deactivated V52/V57) | — | — | Out of scope unless plant modules revived. **Confirm.** |
| PO → vendor | ❌ (legacy had it disabled) | ⚠️ wired but template unseeded → **fails** | — | — | **Broken:** seed `PO_SENT_TO_VENDOR` or gate the call. |
| Scheduled reminders/summaries | ❌ | ⚠️ wired but templates in `.disabled` V26 → **fail** | — | — | New-only feature; broken until templates seeded. |
| Employee created / password reset / LDAP / GRN | ❌ | ❌ | — | — | No gap vs legacy (net-new if desired). |

### The single biggest gap
**Recipient model divergence.** Legacy is a *workflow-routing* mail system: each event notifies
**the next actor** (RM → Dept Head → Procurement/Stores) and CCs the previous actor. The new
system sends **every** workflow email to the **indent/issue-note creator only**, with **no CC and
no routing**. Result: in the new system approvers, the procurement group, and stores are **never
emailed** — the emails have lost their operational purpose (telling the next person there's
something waiting for them). The seeded templates' own wording even betrays this
(`ISSUE_NOTE_CREATED` says "requires your approval" but is sent to the creator, not the approver;
`INDENT_CREATED`, meant for the RM, is never sent at all).

### Secondary gaps
1. **`sendEmailFromTemplate` supports only one recipient, no CC** — cannot express legacy's TO+CC.
2. **Broken wired paths:** `PO_SENT_TO_VENDOR` and the 5 scheduled-job templates aren't seeded in
   any active migration → runtime "template not found", silently skipped.
3. **No From address / no auth / plain port 25** → a real relay will likely reject from-less mail.
4. **Built-but-unverified** — no evidence a real email was ever delivered.
5. **Single-brace legacy-style templates** (V20/V21 SAP/inventory) won't render under the `${...}`
   regex (lower priority — not workflow email).
6. **Schema drift** on `tbl_email_log`/`template_lmu` between repo DDL, JPA entity, and the values
   the V47 fix assumes (deployed DB presumably differs). Cleanup item; confirm against live DB.
7. **No PDF attachment** on final-approved indent (legacy attached the indent PDF to procurement).

---

## STEP 6 — Recommendation (NOT implemented)

Sequenced so we **fix what's broken before adding scope**, and gate on business decisions early.

### Phase 0 — Business decisions to unblock (get these answers first)
- **B1 (critical): recipient model.** Replicate legacy's *route-to-next-actor + CC previous* model,
  or intentionally keep the simplified creator-only model? Everything in Phase 2 depends on this.
- **B2: role mapping.** In the new role model, who are "RM", "Dept Head", "Procurement", and
  "Stores"? Legacy used reporting-hierarchy lookups + hardcoded `roleId=6` (procurement/stores) and
  excluded `roleId=5`. We need the equivalent in the new employee/role tables (and the live
  `tbl_roles_master` names).
- **B3: mail server.** Confirm the current relay host/port, whether it requires auth/TLS, and the
  **From address** to use (legacy sent from `ezone@nslgroup.co.in`). Provide credentials if needed.
- **B4: plant/Pz scope.** The plant-indent modules are deactivated (V52/V57). Confirm the 5 plant
  email triggers are intentionally out of scope.
- **B5: procurement-status-change email** was coded-but-disabled in legacy. Want it in the new system?
- **B6: scheduled reminders/summaries & PO-to-vendor** — keep these new-only features (finish them)
  or drop them?

### Phase 1 — Fix what's already broken (no new features)
1. **Add a From address** (config + `helper.setFrom(...)`), and confirm relay auth/TLS per B3.
   Without this, *every* email likely fails at a real relay.
2. **Resolve unseeded-template failures:** either seed `PO_SENT_TO_VENDOR` + the 5 scheduled-job
   templates (promote `V26…disabled` to a real migration) **or** gate/remove those calls until
   wanted (B6). Today they silently no-op.
3. **End-to-end verification harness:** add GreenMail (test) and a manual/staging send against the
   real relay, then actually observe delivery. Until then, treat email as unverified (Rule 7).
4. (Lower priority) Fix or migrate the single-brace V20/V21 SAP/inventory templates to `${...}`.

### Phase 2 — Correct the recipient model (only if B1 = "match legacy")
5. **Extend the template send path** to accept CC and/or multiple recipients (a
   `sendEmailFromTemplate(code, vars, to, cc)` overload on top of the existing multi-recipient
   `sendEmail`). Keep REQUIRES_NEW + fail-loud behavior.
6. **Add recipient-resolution** using the new reporting hierarchy + roles (per B2): requester,
   reporting superior (RM), next-level superior (Dept Head), procurement/stores group.
7. **Re-point each workflow email** to legacy semantics: submit→RM(CC requester);
   RM-approve→Dept Head(CC RM); reject→requester(CC RM); final-approve→procurement group; issue-note
   submit→stores/RM; etc. **Wire `INDENT_CREATED`** (currently never sent).

### Phase 3 — Restore remaining legacy parity
8. **Final-approved indent → procurement group with the indent PDF attached** (legacy `getSendEmailPDF1`).
9. **Procurement-status-change email** if B5 = yes.

### Phase 4 — Net-new (no legacy equivalent; business-driven, optional)
10. Scheduled reminders/summaries/analytics (finish per B6), employee-created / password-reset
    emails (never existed in legacy).

### Explicitly flagged for a human decision
- **B1 recipient model** — the core call; the new system currently notifies the wrong people.
- **B2 role mapping** — cannot faithfully route without the new role↔legacy `roleId 5/6` mapping
  and live `tbl_roles_master` names.
- **B3 mail server + From address + credentials/TLS** — legacy values (`172.16.65.66:25`, anon,
  `ezone@nslgroup.co.in`) are almost certainly stale; new default is `172.16.65.65:25` with no From.
- **B4 plant modules**, **B5 procurement-status email**, **B6 scheduled/PO-vendor features** — scope.
- **Schema drift** (`tbl_email_log` columns, `template_lmu` type) — reconcile repo DDL/entity with
  the deployed DB before relying on either.

### Do-not-replicate (legacy anti-patterns)
Empty catch blocks that swallow send failures, impossible recipient guards, HQL string-concat
(injection), unescaped user input in HTML bodies, malformed From header. The new system already
avoids most of these — keep it that way.

---

## Appendix — key file references

**New system**
- `backend/src/main/java/com/nslindia/procurezone/notification/service/EmailService.java`
- `.../notification/model/EmailLog.java`, `.../notification/model/EmailTemplate.java`
- `backend/src/main/resources/db/migration/V47__add_approval_email_templates.sql`,
  `V49__add_email_log_created_date.sql`, `V20`/`V21__create_email_tables.sql`,
  `V26__add_scheduled_job_email_templates.sql.disabled`
- `backend/src/main/resources/application.yml` (`spring.mail`), `application-prod.yml`
- Workflow callers: `IndentService.java`, `IssueNoteService.java`, `POService.java`

**Legacy (compiled; paths exact, line numbers from decompilation)**
- `WEB-INF/classes/seeds/indent/mailService/EmailConfiguration.class` (SMTP + send)
- `.../indent/mailService/{IndentNotification,IndentEmail,IndentMessage}.class`
- `.../issue/mailService/{IssueNotification,IssueEmail,IssueMessage}.class`
- Callers: `.../seeds/indent/action/IndentAction.class`, `.../seeds/issue/action/IssueNoteAction.class`
  (+`IssueNoteAction1`), `.../plant/indent/action/PlantIndentAction.class`,
  `.../plant/indent/issuenote/action/PlantIssueNoteAction.class`
- `WEB-INF/classes/pojo/TblRolesMaster.hbm.xml`
