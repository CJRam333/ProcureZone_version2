# Project Stabilization Log

---

## 2026-07-07

### LDAP Login Diagnostics — stage logging + config_princ DN whitespace normalization

`janakirama.c@ashaagrisciences.com` fails with the generic "Invalid username or password", but the
`ldapsearch` CLI with the same user + service account succeeds — so the server/bind/search/user-bind
all work; something in the Java path differs. Added server-side-only diagnostics (client still gets
the generic error) and applied the most likely fix.

**Stage logging (WARN level, so it shows without changing log config; no passwords ever logged):**
`LdapAuthService.authenticate()` now logs, in order:
- step1: email → extracted domain
- step2: whether a config row was found (logs `config.id` + url, never pwd) / blank-N-A / incomplete
- step3: the constructed service-bind DN + url + `auth=simple`
- step4: service-bind succeeded
- step5: whether the `(mail=…)` search returned an entry, and the DN found (or NO entry)
- step6: user-bind succeeded → authenticated
- FAIL branches log `e.getClass().getName()` + `e.getMessage()` for both `AuthenticationException`
  (bind) and `NamingException` (communication/DN-parse/timeout), tagged `LDAP-DIAG FAIL`.

**DN whitespace normalization (applied — strong suspected root cause):** `config_princ` rows carry
spaces after commas, e.g. `ou=people, dc=ashaagrisciences, dc=com`. `ldapsearch` tolerates/normalizes
these; JNDI's RFC-2253 DN parser can reject or misparse them. Added
`normalizeDn(dn) = dn.replaceAll(",\\s+", ",").trim()`, applied to **both** the service-bind DN and
the `(mail=…)` search base. The service-bind `config_user` is also trimmed. A WARN line logs the
before/after when normalization changes the value, so the log confirms whether this was the issue.

Diagnostic-only otherwise: no change to routing, to what the API returns, or to the local login path.
The step logs will show definitively whether the failure was service-bind, search-returns-nothing, or
a DN-parse `NamingException` — if normalization already fixed it, step6 will log success.

**Build:** backend `mvn compile` clean; `tsc -b && vite build` clean. Backend-only change — frontend
bundle unchanged (`index-Xk4l49XY.js`).

**Next:** reproduce the login, then read `app.log` for the `LDAP-DIAG` lines to confirm the stage and
exception. Once confirmed working, the WARN-level `LDAP-DIAG` lines should be dropped to DEBUG.

---

### LDAP Authentication — 18 domains, plain JNDI, wired into login (local login unchanged)

Built LDAP (directory) authentication for `@`-containing logins. Local (non-`@`) login is
**completely untouched** — LDAP is a new branch only.

**Tested facts this is built on (verified against the live servers — do not deviate):**
- Per-domain config in `tbl_ldap_config` (`config_url`, `config_user`, `config_pwd`, `config_princ`).
- Servers are **Zimbra OpenLDAP (inetOrgPerson), NOT Active Directory**.
- User DN: `uid=<email-localpart>,<config_princ>` (e.g. `uid=janakirama.c,ou=people,dc=ashaagrisciences,dc=com`).
- Service-bind DN: `uid=<config_user>,<config_princ>` (uid=, confirmed working).
- Users searchable by `mail=`; uid = email local-part.
- Flow: service-bind → search `(mail=<username>)` → get user DN → bind as user DN with typed
  password → success = authenticated.

**PART 1 — dependency:** none added. `javax.naming` (JNDI, `com.sun.jndi.ldap.LdapCtxFactory`) is
JDK built-in. Spring LDAP intentionally NOT added (confirmed absent in pom).

**PART 2 — `auth.ldap.LdapConfig` + `LdapConfigRepository`:** read-only entity over
`tbl_ldap_config` (id, domine, url, user, pwd, princ; lmd/lmu ignored). Repo exposes
`findByDomine(String)`.

**PART 3 — `auth.ldap.LdapAuthService.authenticate(email, password)`:**
- **Service-bind DN:** `"uid=" + config.getUser() + "," + config.getPrinc()`.
- **User-bind DN:** the absolute DN returned by the `(mail=<email>)` subtree search
  (`SearchResult.getNameInNamespace()`) — not hand-built — then a second `InitialDirContext` binds
  as that DN with the typed password.
- Env: `simple` auth, `com.sun.jndi.ldap.connect.timeout=5000` + `read.timeout=5000` so a bad
  server can never hang login; both contexts closed in `finally`.
- **Malformed domain-6 (`barracudanslgroup@nslgroup.in`):** `lookupConfig` tries exact
  `findByDomine` first; on miss, scans all rows and matches on the part **after `@`** in the stored
  `config_domine`, so a malformed value is still reachable. If still no match → clean failure.
- **`config_pwd = 'N/A'` / blank:** detected before any bind — returns failure
  ("LDAP not configured for this domain"), never binds with the literal "N/A". Incomplete
  url/user/princ rows also fail cleanly.
- Returns a plain `boolean`. Every failure mode (no config, N/A, user-not-found, wrong password,
  unreachable/timeout) returns `false` — indistinguishable to the caller; only server-side
  debug/warn logs carry detail. No stack trace or credential leaves the method.

**PART 4 — `AuthService.login()` routing:** at the password-verification step,
`if (username.contains("@"))` → `ldapAuthService.authenticate(...)`; else → the **unchanged** local
`passwordService.verifyPassword` path (verbatim, moved into the `else`). The employee is loaded by
`emp_email` and active-checked *before* this branch, so an LDAP user with no `tbl_emp_master` row
fails at lookup with the same generic `InvalidCredentialsException`. On LDAP success the SAME JWT is
issued via the identical downstream code (roles, `RoleNormalizer`, companyIds, `AuthenticatedUser`)
— LDAP only verifies the password; the employee record supplies all authorization.

**PART 5 — safety:** no-employee-row → generic fail; timeouts prevent hangs; each domain is
independent (a failed/unreachable domain returns false for that login only — other domains and
local login are unaffected); API surfaces only "invalid credentials", no detail.

**Build:** backend `mvn compile` clean; `tsc -b && vite build` clean. **No frontend change** —
LDAP is backend-only, so the bundle hash is unchanged (`index-Xk4l49XY.js`); the login form already
posts username/password.

**Business-owner verification after deploy:** log in with an `@`-email whose domain has a valid
(non-N/A) `tbl_ldap_config` row **and** a matching `tbl_emp_master.emp_email`; confirm local
(non-`@`) logins still work unchanged. Ensure the app server (172.16.9.158) can reach each
`config_url` host:port (see `docs/ldap-investigation.md` section 5).

---

### Restrict Plant Indent Module to ADMIN/SUPERADMIN (not in active use)

Plant Indent is not currently used. Locked down to ADMIN/SUPERADMIN at every layer — **no code
deleted**, so it can be re-enabled later by widening the role lists.

**PART 1 — Sidebar (`Sidebar.tsx`).** The Plant Indent nav item had **no `roles` array** (visible
to anyone with module access, gated only by `moduleCode: 'PLANT_INDENTS'`). Added
`roles: ['ADMIN', 'SUPERADMIN']`.

**PART 2 — Routes (`router.tsx`).** All four Plant Indent routes tightened:

| Route | Before | After |
|---|---|---|
| `plant-indent` (index) | `['SUPERADMIN','ADMIN','PLANTMANAGER','FLOORINCHARGE','SUPERVISOR']` | `['ADMIN','SUPERADMIN']` |
| `plant-indent/new` | `['SUPERADMIN','ADMIN','PLANTMANAGER','SUPERVISOR']` | `['ADMIN','SUPERADMIN']` |
| `plant-indent/:id` | `['SUPERADMIN','ADMIN','PLANTMANAGER','FLOORINCHARGE','SUPERVISOR']` | `['ADMIN','SUPERADMIN']` |
| `plant-indent/:id/edit` | `['SUPERADMIN','ADMIN','PLANTMANAGER','SUPERVISOR']` | `['ADMIN','SUPERADMIN']` |

**PART 3 — Backend (`PlantIndentController.java`) — the real enforcement.** All **24**
`@PreAuthorize` annotations were normalized to `hasAnyRole('SUPERADMIN', 'ADMIN')`. Previous values
varied widely and included PLANTMANAGER, FLOORINCHARGE, USER, VIEWER, SUPERVISOR, QUALITYMANAGER,
DEPTHEAD across CRUD, workflow (submit / deo-approve / manager-approve / start-processing /
complete / resubmit) and queue/dashboard endpoints. Every one is now ADMIN/SUPERADMIN only.

**PART 4 — Module access (migration V52, next after V51).**
`UPDATE tbl_module_master SET module_default_roles = 'ADMIN,SUPERADMIN' WHERE module_code = 'PLANT_INDENTS'`.
(Confirmed table/column names against V43/V46: `tbl_module_master.module_default_roles`,
`module_code`.)

> **Note on `module_default_roles` format:** used the task's literal `'ADMIN,SUPERADMIN'` (role
> codes). An earlier migration (V46) wrote display names (`'Super Admin,Admin,ROLE_VIEWER'`) into
> this same column, so the format the module-access check expects is inconsistent in history. Real
> enforcement here is the backend `@PreAuthorize` + route guards + sidebar `roles` (all use the
> normalized `ADMIN`/`SUPERADMIN` codes), so the module restriction holds regardless; if the
> business owner finds the module still seeds for other roles, this value may need to be
> `'Super Admin,Admin'` to match V46's convention.

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. Migration **V52**. New bundle hash
**`index-Xk4l49XY.js`**.

---

## 2026-07-06

### Stores Buttons Visible to PROCUREMENT + "Created By" Column for Elevated Roles

**PART 1 — stores Goods Issued / Reject (Stores) buttons not showing.** The status check was
correct and read the unwrapped fields fine; the bug was the **role list**. Before/after:

```
// before
const canIssue = approvedStatusId === 3 && storesByStatusId === 1
  && hasAnyRole(['SUPERADMIN', 'ADMIN', 'ISSUECONFIRM']);
// after
const canIssue = approvedStatusId === 3 && storesByStatusId === 1
  && hasAnyRole(['SUPERADMIN', 'ADMIN', 'ISSUECONFIRM', 'PROCUREMENT']);
```

PROCUREMENT fills the stores role in this deployment but was absent from the check, so the
stores/procurement user saw the note with no action buttons. Also added PROCUREMENT to the matching
backend `@PreAuthorize` on `POST /issue-notes/{id}/issue` and `/reject-stores` — otherwise the
now-visible buttons would 403 on click. Endpoints confirmed wired: Green "Goods Issued" →
`issueNotesApi.issue` → `/issue` → storesByStatus=11; Red "Reject (Stores)" →
`issueNotesApi.storesReject` → `/reject-stores` (with reason) → storesByStatus=2. Both frontend
methods already unwrap the `{success,data}` envelope (returning `response.data.data`).

**PART 2 — "Created By" column, elevated roles only.** The list DTO (`IssueNoteSummaryResponse`)
**did not** carry the creator name — it had neither `createdBy` nor a resolved name. Added
`createdBy` + `employeeName` to the DTO and resolved the name in `mapToSummaryResponse` via the same
`resolveEmployeeName(createdBy)` the detail response uses. Frontend: added a "Created By" column to
`IssueNotesListPage`, rendered only when
`hasAnyRole(['SUPERVISOR','DEPTHEAD','PROCUREMENT','ISSUECONFIRM','ADMIN','SUPERADMIN'])` (spread into
the columns array so it's fully absent for a plain USER, who only ever sees their own notes). Null
creator renders as "—".

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. New bundle hash **`index-HSNw8zic.js`**.

---

### Company/Plant/Section/Location Fully Optional — Never Block Creation + Plant≠Location Resolved

Company, plant, section, location and department are optional, informational employee metadata.
Missing any of them must never prevent creating an indent or issue note.

**PART 1 — migration V51 (next number after V50).** Made the remaining NOT NULL geo columns
nullable (V50 already did plant/section):

| Table | Column | Was | Now |
|---|---|---|---|
| tbl_issue_note | issue_note_company | NOT NULL | NULL |
| tbl_issue_note | issue_note_dept | NOT NULL | NULL |
| tbl_indent_master | indent_company | NOT NULL (legacy) | NULL |
| tbl_indent_master | indent_dept | NOT NULL (legacy) | NULL |

Full geo picture across both tables after V50+V51: company, department, plant, section all nullable.
`createdby`, document number, date, status and the audit `lmd/lmu` columns stay NOT NULL (genuinely
required / server-set) — not geo, correctly left alone.

**PART 2 — constraints removed.** `IssueNote` entity: dropped `nullable=false` from
`issue_note_company`, `issue_note_dept`, and a stale `issue_note_plant` (missed in V50's code side).
Indent uses `@JoinColumn` (no entity-level NOT NULL — nothing to change). Request DTOs already had
`@NotNull` removed from company/department/plant/section (and indent employeeId) in the prior commit;
TS request types already optional.

**PART 3 — resilient server-side capture.** `createIssueNote` and `createIndent` now:
company ← first `tbl_map_company_emp` row wrapped in try/catch (missing/error → null, logged, never
throws); department ← `emp_department` else null; plant/section ← null unless a client explicitly
supplies one. Each still honours an explicit request value as a fallback. Creation proceeds no matter
how much employee metadata is absent.

**PART 5 — plant vs location: DISTINCT masters.** `tbl_plant_master` and `tbl_location_master` are
separate tables, and `Employee` carries both `emp_location` (Integer location id) and `emp_plant`
(String name) as distinct fields — so plant ≠ location. **Reverted** the prior commit's
`plant ← emp_location` assignment in both services: writing a location id into a plant column would
have made the detail page's plant-name lookup resolve the wrong master. `*_plant` now stays null
(no employee→plant-id source exists). `emp_location` has no target column on either entity, so
location is not stored. (Business owner can still confirm with the row-count queries, but the code
evidence is conclusive.)

**PART 4 — null geo renders as "—".** Both detail pages now show a clean em-dash for null
company/department/plant/section instead of "N/A". The backend name lookups were already null-safe
(`getXxxId() != null ? repo.findById(...) : null`), so a null id never hits the repository.

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. Migration **V51**. New bundle hash
**`index-BF1ljrKI.js`**.

---

## 2026-07-04

### Issue Note Creation 500 (audit column) + Legacy Number Sequence + RM Approver + Card Removal

**PART 1 — creation 500 `Unknown column 'log_created_date'`.** The issue note was created, but the
post-create email notification saved an `EmailLog` (`tbl_email_log`) whose entity maps
`log_created_date` — a column missing from the deployed table. `EmailService.sendEmailFromTemplate`
was `@Transactional` with default propagation, so it **joined** `createIssueNote`'s transaction; the
failed INSERT marked it rollback-only and reverted the issue note even though the caller caught the
exception. Two fixes: (1) both `sendEmailFromTemplate` and `sendEmail` are now
`@Transactional(propagation = REQUIRES_NEW)` — email/audit logging runs in its own transaction and a
failure can never roll back a business operation again; (2) migration **V49** adds the missing
`log_created_date DATETIME NULL` column so logging actually works. Audit entity: `EmailLog` →
`tbl_email_log`; mismatch was the single missing `log_created_date` column.

**PART 1 Step 4 — issue note number format.** Generator produced `IN/2026/00001`; the real legacy
sequence is a continuous 13-digit numeric (…988, …989, …). Indent generation
(`generateLegacyNumericIndentNumber`) takes the latest `indent_no` (ORDER BY id DESC), `parseLong+1`,
with a MAX-numeric fallback for stray non-numeric rows. Rewrote `generateIssueNoteNumber` to mirror it
exactly — added `findLatestIssueNoteNumbers` + `findMaxNumericIssueNoteNumber` (native
`MAX(CAST(... AS UNSIGNED)) WHERE issue_note_no REGEXP '^[0-9]+$'`) to `IssueNoteRepository`. The
MAX-numeric fallback deliberately ignores the stray `IN/2026/00001` so the sequence resumes from the
real max. Both the `/meta` preview (`previewNextIssueNoteNumber`) and creation call
`generateIssueNoteNumber`, so both are consistent.

**PART 5 — RM approver name not showing.** The previous commit resolved the name from
`issue_note_rm_approvedby` only. Legacy flow was always User→RM→Stores with no manager stage, so
legacy rows recorded the RM in `issue_note_approvedby`, while new-app `rmApprove()` writes
`issue_note_rm_approvedby`. Fixed by coalescing in `mapToResponse`: RM approver id/date =
`rmApprovedBy`/`rmApprovedByDate` if present, else `approvedBy`/`approvedByDate`. Frontend already
reads `rmApprovedByName`/`rmApprovedByDate`; the RM Review stage shows "Approved by {name} on {date}".

**PART 6 — remove creation cards, capture data server-side, make plant/section nullable.**
Confirmed available employee data: `emp_department` (`tbl_emp_master`), `emp_location`
(`tbl_emp_master`), company via `tbl_map_company_emp`. NOT available: section, and no employee→plant-id.
- **Backend:** `createIssueNote` and `createIndent` now resolve company (first of
  `companyEmployeeRepository.findCompanyIdsByEmpNumber`), department (`emp_department`), and plant
  (`emp_location` — best available proxy) server-side, each **falling back to the request value** if
  the employee record can't supply it (older clients keep working). Indent employee = creator when the
  form omits it.
- **Migration V50** makes `issue_note_plant`, `issue_note_sec`, `indent_plant`, `indent_sec` nullable
  (no reliable plant/section source, so creation must tolerate NULL).
- **DTOs:** `@NotNull` removed from company/department/plant (and indent employeeId); TS request types
  made these optional.
- **Frontend:** removed the "Basic Information" card (indent) and "Issue Details" card (issue note)
  entirely — with their company/plant/department/section dropdowns, related master-data queries,
  auto-select effects, and the zod `min(1)` validators. Payloads send these only as an optional
  fallback (never `0`). Added a minimal read-only header line to both creation forms:
  `Indent/Issue Note No: {preview} · Date: {today} · FY: {year}`.

> **Caveat flagged for the business owner:** `plant` is populated from the employee's `emp_location`
> (a location id), because no employee→plant-id mapping exists. If the plant and location masters are
> distinct, the stored `*_plant` value is really a location id and the detail page's plant-name lookup
> may show the wrong name or blank. Confirm whether location==plant in this deployment; if not, we
> should add a dedicated location column rather than reuse plant. Company creation still requires a
> `tbl_map_company_emp` row for every creator (company columns remain NOT NULL) — verify completeness.

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. Migrations added: **V49** (email log
column), **V50** (plant/section nullable). New bundle hash **`index-BN8cCzXC.js`**.

---

### Issue Note Creation Blockers + Material Dropdown Catalogue + Dropdown Scroll + Detail Gaps

**PART 1 — `issue_note_lmd` data-truncation (blocker) + all varchar date columns.**
Root cause: the legacy `tbl_issue_note` / `tbl_issue_note_details` audit-date columns are
`varchar(20)`, but the entities map them as `LocalDateTime`. Binding a `LocalDateTime` sends a
value with fractional seconds (`2026-07-04 07:25:17.738893`, 26+ chars) → MySQL truncation on
INSERT. (Indents are unaffected — their `indent_*` date columns are real `datetime`, proven by
indents creating fine with the identical mapping.) Fix: new
`common/persistence/VarcharDateTimeConverter` (JPA `AttributeConverter<LocalDateTime,String>`)
writing a fixed 19-char `yyyy-MM-dd HH:mm:ss` and reading tolerantly. Applied via `@Convert` to
the audit-date fields **not** used in JPQL range/sort:

| Column | Width | Code wrote (before) | Chars | After |
|---|---|---|---|---|
| `issue_note_lmd` | varchar(20) | `LocalDateTime.now()` → `2026-07-04 07:25:17.738893` | 26+ | `2026-07-04 07:25:17` (19) |
| `issue_note_details_lmd` | varchar(20) | same | 26+ | 19-char |
| `issue_note_storesby_date` | varchar(20) | same (on goods-issue) | 26+ | 19-char |
| `issue_note_approvedby_date` | varchar(20) | same | 26+ | 19-char |
| `issue_note_rm_approvedby_date` | varchar(20) | same (on RM approve) | 26+ | 19-char |
| `issue_note_year` | varchar(45) | `"2026-27"` | 7 | unchanged (fine) |
| `issue_note_created_date` | varchar(20) | never written (String field) | — | unchanged |
| `issue_note_date` | datetime | `LocalDateTime` | — | **left native** (used in `Sort`/`BETWEEN`) |
| `tbl_indent_master.*` dates | datetime | `LocalDateTime` | — | unchanged (real datetime, works) |

The 19-char format also inserts cleanly into a real `datetime` column, so the converter is safe
regardless of the exact column type. `issue_note_lmd` was only the first to fail; `details_lmd`
and `storesby_date` would have blocked the next steps — all fixed in one pass.

**PART 2 — material dropdown limited to ~30-40 for non-USER/ADMIN roles.**
`MaterialController /dropdown` set `adminView = ADMIN||SUPERADMIN` and passed the caller's
`companyIds`; non-admin callers hit `searchForDropdownByCompanies`, whose
`AND (co.id IS NULL OR co.id IN :companyIds)` filter dropped every material mapped only to other
companies. USER appeared full only because its `companyIds` is empty (fell through to the
all-companies branch); SUPERVISOR/DEPTHEAD/PROCUREMENT have mappings → scoped subset. Fix:
`searchMaterialsForDropdown(search)` now always calls `searchForDropdownAllCompanies` — the
catalogue is stock context, not a permission boundary. Controller simplified (dropped
`adminView`/`companyIds`), all authenticated roles get the full list.

**PART 3 — dropdown detached from input on scroll.**
The material search dropdown is `position: fixed` (so it escapes the card's `overflow`). Fixed
elements don't scroll with the page, so it floated away. Fix (applied to both `IndentFormPage`
and `IssueNoteFormPage` — they each have their own inline dropdown, no shared component): store a
ref to the anchor input; a `useEffect` gated on `showMaterialSearch` adds capture-phase `scroll`
+ `resize` listeners that recompute `{top,left,width}` from the input's live bounding rect, so the
dropdown tracks the input continuously and still overlays outside the card.

**PART 4 — issue note detail line items (qty/purpose/total).** No code change needed — the
table already renders material code/name, UOM, quantity, purpose and a total-quantity footer, and
`IssueNoteResponse.IssueNoteDetailResponse` already carries `quantity`/`purpose`. The fields were
blank only because the **response-envelope bug** (fixed in fd5de25) left `issueNote.details`
undefined; with the unwrap deployed they populate. Verified the table + DTO are correct.

**PART 5 — RM approver in flowchart.** Added `rmApprovedBy` / `rmApprovedByName` /
`rmApprovedByDate` to `IssueNoteResponse` (name resolved from `issue_note_rm_approvedby` via the
existing employee lookup). The RM Review stage now reads "Approved by {name} on {date}".

**PART 6 — remove "Issue Details" card & capture server-side: NOT DONE (reported, by design).**
The premise doesn't hold against the schema. `tbl_emp_master` has only `emp_department` (Integer)
and `emp_location` (Integer); there is **no `emp_company`, no `emp_section`, and `emp_plant` is a
`varchar(100)` name — not the Integer plant FK** that `issue_note_plant` (NOT NULL) requires.
Company is only derivable via the company↔employee mapping table (and defaults to the first of
possibly several); section has no employee source at all; plant has no integer source. Removing
the card would break creation (no plant id, possible null company/department). So the card was
**kept** (heading already renamed "Issue Details" in fd5de25) with its pre-filled, working
dropdowns. Recommend the business owner confirm `emp_*` completeness before any future removal;
even then, plant needs a resolvable employee→plant-id source that doesn't currently exist.

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. New bundle hash
**`index-CqjkQo48.js`** (was `index-D3NXf63A.js`).

---

### Issue Note Detail Response Envelope Unwrap + Create-Button Roles + Card Heading

**Root cause of "detail page shows N/A + flowchart shows default data" (looked like a stale
deploy, was not):** rebuilding the committed frontend reproduced the exact deployed bundle hash
(`index-BaXK1G_W.js`), proving the deployed frontend was current — no cache/deploy/duplicate-
component problem. The real bug: `GET /issue-notes/{id}` wraps its body as
`{ success: true, data: {...} }`, but `issueNotesApi.getById()` returned axios `response.data`
(the envelope) instead of `response.data.data` (the issue note). So the detail page read
`issueNote.employeeName` etc. as `undefined` → "N/A", and `issueNote.approvedStatus ?? 1`
defaulted to 1 → the 3-stage flowchart rendered its structure (new labels visible) but every
stage showed its first/pending state. This predates the enrichment work — the old page read
`requestedByName` off the same envelope and got the same blanks, which is why the enrichment
"didn't appear." The list page was unaffected because `GET /issue-notes` returns a raw `Page`.

**FIX 1 — envelope unwrap (`api/issueNotes.ts`):**

Audited every issue note endpoint. Envelope usage:

| Shape | Endpoints |
|---|---|
| `{ success, [message,] data }` (entity nested under `data`) | create, getById, getByNumber, submit, rm-approve, rm-reject, issue, reject-stores, cancel, return |
| Flat `Page<>` (no envelope) | GET `/issue-notes` (list) |
| `{ content, currentPage, totalItems, totalPages }` map | pending-rm-approval, pending-issue, by-department, my-issue-notes |
| `{ success:false, message }` (410 GONE, **no data**) | approve, reject (deprecated manager stage) |

Unwrapped to `response.data.data`: `getById`, `getByNumber`, `create`, `submit`, `issue`,
`return`, `cancel`, `storesReject`, `rmApprove`, `rmReject`. **`create` was also a latent bug** —
the form's "Save & Submit" reads `created.id` to chain the submit call; against the envelope
that was `undefined`, so the second step used a bad id. Left the two deprecated 410 endpoints
(`approve`/`reject`) flat — they carry no `data` and axios throws on 4xx before the return runs.
The `{content,...}` paginated maps are a different shape used by secondary queues and were left
as-is (out of scope; the primary list uses the flat `Page`).

**FIX 2 — create-button + endpoint + route-guard roles aligned** to
`USER, SUPERVISOR, DEPTHEAD, ADMIN, SUPERADMIN`:
- `IssueNotesListPage.tsx` create button: was `['ADMIN', 'ISSUECONFIRM']` (stores staff, wrong —
  they *issue* goods, they don't *raise* notes; USER/SUPERVISOR/DEPTHEAD saw no button at all).
- Backend `POST /issue-notes` `@PreAuthorize`: was `USER, ADMIN, SUPERADMIN, SUPERVISOR` → added
  DEPTHEAD. (ISSUECONFIRM was not present, so nothing to remove.)
- `router.tsx` `/issue-notes/new` **and** `/issue-notes/:id/edit` guards: were
  `SUPERADMIN, ADMIN, USER, SUPERVISOR` → added DEPTHEAD. All three now match exactly.

**FIX 3 — card heading** in `IssueNoteFormPage.tsx`: "Issue Note Information" → "Issue Details"
(the card now only wraps the Company/Plant/Department/Section dropdowns; the employee strip was
removed 2026-07-04 earlier).

**Note (not fixed, out of scope):** no `PUT`/`DELETE` endpoint exists on the controller, so the
frontend `update`/`delete` methods target nothing — the issue note *edit* flow is non-functional
server-side. Flagged for a future task.

**Build:** `mvn compile` clean; `tsc -b && vite build` clean. **New bundle hash
`index-D3NXf63A.js`** (was `index-BaXK1G_W.js`) — confirms the frontend change compiled in.

---

### Issue Note Indent-Parity — Enrichment, Status Display, Flowchart, Visibility, Creation Card

Brings Issue Notes to the same standard as Indents. Flow: **User → RM → Stores** (no Dept
Head stage). Two status columns: `issue_note_approved_status` (1=pending, 2=rejected,
3=approved) × `issue_note_storesby_status` (1=pending, 2=rejected, 11=issued).

**PART 1 — IssueNoteResponse enrichment (prerequisite):**

The DTO returned only raw IDs; the detail page rendered `requestedByName`, `issueNumber`,
`createdAt`, `departmentName` etc. — fields that never existed on the payload, so the page
showed blanks/N-A. Added resolved names: `employeeName`/`employeeNumber` (from
`issue_note_createdby`), `companyName`, `departmentName`, `sectionName`, `plantName`,
`issuedByName` (from `issue_note_storesby`), `displayStatus`, plus `materialCode`/
`materialName`/`uomCode` on line items (the items table had the same blank-fields disease).

**Resolution pattern:** IndentService resolves names through JPA `@ManyToOne` entity
relationships (`indent.getCompany().getName()`). IssueNote stores raw FK integers with no
entity relationships, so the identical outcome is achieved via null-guarded repository
lookups (`companyRepository.findById(...).map(Company::getName)` etc.) in `mapToResponse()`.
Frontend types cleaned: stale aliases (`issueNumber`, `requestedByName`, `createdAt`,
`statusName`, `items`, `remarks`) removed and all consumers rewired to real field names.

**Write-path corruption fix (same disease as the indent l2Approve bug):**

`createIssueNote()` never set the two workflow columns (both NULL) and `rmApprove()`/
`rmReject()` set only the `issue_note_rm_status` audit column — never
`issue_note_approved_status`. Every new-app issue note therefore derived its display from
`(null, null)` and stayed "Pending RM Approval" forever, and the stores queue check
(`approved=3, stores=1`) could never fire for new notes. Fixed:
- `createIssueNote()` → `approvedStatus = 1` (or 3 with DEPTHEAD bypass), `storesByStatus = 1`
- `rmApprove()` → `approvedStatus = 3` (keeps rm_status audit write)
- `rmReject()` → `approvedStatus = 2`
- `issueGoods()` / `rejectByStores()` already set `storesByStatus` 11 / 2 correctly.
Legacy-migrated rows already carry correct values; only new-app rows created before this fix
(if any) may have NULL columns — they display as "Pending RM Approval" (safe default).

**PART 2 — deriveIssueNoteDisplayStatus():** function already existed and covered the
production combinations; label "Pending" renamed to "Pending RM Approval" and visibility
widened to package-private for `DeriveIssueNoteDisplayStatusTest` (new, 3/3 green):
(3,11)→Goods Issued ×2,213 · (3,2)→Stores Rejected ×45 · (2,1)→RM Rejected ×26 ·
(3,1)→RM Approved ×3 · (1,1)→Pending RM Approval ×2 · null-safe.

**PART 3 — Three-stage flowchart** (Submitted → RM Review → Stores) on the detail page,
driven purely by the two-column model, never the Spring status (legacy rows carry status=1
as an active flag). Submitted always green; RM ✓/✗/amber per approvedStatus; Stores
✓ (issued 11) / ✗ (rejected 2) / amber (pending 1, once RM approved). Rejection terminates
the flow; a level-specific rejection alert shows below.

**Detail-page action wiring fix (found during investigation):** the Approve/Reject buttons
called `issueNotesApi.approve`/`reject` → the deprecated manager-stage endpoints which now
return **410 GONE** — RM approval from the detail page was broken. Rewired to
`rmApprove`/`rmReject`, gated on `status=2 AND approvedStatus=1`, label "Approve (RM Review)".

**PART 4 — Role visibility:** already implemented in `getAll()` (USER own; SUPERVISOR own +
subordinates; DEPTHEAD/PLANTMANAGER department; ISSUECONFIRM/PROCUREMENT/ADMIN/SUPERADMIN
global) with `issueDate DESC` ordering, and the list page title was already dynamic. Added the
missing active-row filter to both filter queries. **Deviation from spec:** the task said
"always filter issue_note_status = 1" — implemented as `status <> 0` instead, because the new
app reuses the Spring column for workflow (2=submitted, 3=RM approved, 8=issued…); filtering
=1 would hide every in-flight new-app note. `0` is the cancel/soft-delete value in both eras.

**PART 5 — True-draft gating:** `createIssueNote()` sets Spring `status=1` (Draft; 3 with
DEPTHEAD bypass) and now `(1,1)` two-column state; `submitForApproval()` moves status to 2.
Gate: `isTrueDraft = status===1 && approvedStatus===1 && storesByStatus===1` on detail-page
Submit/Edit and the list-page Edit button (which previously compared integer `status` to the
string `'DRAFT'` — never rendered; same for the Return button vs `'ISSUED'`, now `status===8`).
Residual ambiguity: legacy `(1,1)` rows with status=1 are indistinguishable from new drafts
(2 rows in production) — same accepted trade-off as indents.

**PART 6 — Creation page:** removed the read-only employee strip (Employee, Financial Year,
Date, Issue Note No. preview) — this data is auto-captured at creation and now displays on the
detail page. Also removed the "Issued To" field (no legacy equivalent; investigation 2026-07-04);
backend `@NotBlank` on `issuedTo` relaxed to optional — **DB column retained**. The editable
Company/Plant/Department/Section dropdowns remain (legacy-faithful).

**PART 7 — Colors:** all five issue note labels already present and unique in
`INDENT_STATUS_COLORS`: Pending RM Approval=warning, RM Rejected=danger, RM Approved=info,
Stores Rejected=pink, Goods Issued=dark-green. List-page filter option renamed
"Pending" → "Pending RM Approval".

**PART 8 — Stores buttons:** "Goods Issued" / "Reject (Stores)" preserved with the two-column
gate (`approved=3 AND stores=1`, ISSUECONFIRM/ADMIN/SUPERADMIN) and confirmed against the
enriched response; the issue-confirmation modal now lists items by material code.

**Files changed:**
- `backend/.../issuenote/dto/IssueNoteResponse.java` — enriched (names + displayStatus + line-item codes)
- `backend/.../issuenote/dto/IssueNoteSummaryResponse.java` — + approvedStatus/storesByStatus
- `backend/.../issuenote/dto/CreateIssueNoteRequest.java` — issuedTo optional
- `backend/.../issuenote/IssueNoteService.java` — name resolution, write-path two-column fixes, label rename
- `backend/.../issuenote/IssueNoteRepository.java` — `status <> 0` active filter on both filter queries
- `backend/src/test/.../issuenote/DeriveIssueNoteDisplayStatusTest.java` — new, 3/3 green
- `frontend/src/api/issueNotes.ts` — types match enriched DTO; stale aliases removed
- `frontend/src/pages/issue-notes/IssueNoteDetailPage.tsx` — info card, flowchart, gating, RM wiring
- `frontend/src/pages/issue-notes/IssueNoteFormPage.tsx` — info strip + Issued To removed
- `frontend/src/pages/issue-notes/IssueNotesListPage.tsx` — filter label, edit/return button fixes
- `frontend/src/pages/issue-notes/IssueNoteApprovalPage.tsx` — rewired to real field names

**Build:** `mvn compile` clean; `mvn test -Dtest=DeriveIssueNoteDisplayStatusTest` 3/3 green;
`tsc -b && vite build` clean.

---

## 2026-07-03

### Procurement UI Fixes + Unique Status Colors + Approval Flowchart Correction

**FIX 1 — Approve/Reject buttons removed for procurement stage (`IndentDetailPage.tsx`):**

Previous `canApprove` had a third clause `(awaitingProcurement && hasAnyRole([SUPERADMIN, ADMIN, PROCUREMENT]))`
that showed "Procurement Approve"/"Reject" buttons once an indent reached the procurement stage.
Removed that clause — Approve/Reject now exist only for the two approval stages:
L1 (SUPERVISOR when `approvedStatus=1`) and L2 (DEPTHEAD/PLANTMANAGER when `approvedStatus=3, finalStatus=1`).
PROCUREMENT works exclusively through the Procurement Status Update card. The smart-route
`status=5 → procurementApproveIndent()` branch is now unreachable from the UI by design.

**FIX 2 — Procurement dropdown reflects saved sub-status on load:**

The dropdown was `useState(5)` — always reset to "Quotations Collected" on reload even when the
indent was already at PO Released. Added a `useEffect` that seeds the dropdown from
`indent.procurementStatusId` when it is a valid sub-status (5–9). Value 4 means "arrived at
procurement, no sub-status chosen yet" and keeps the default of 5.

**FIX 3 — Lock after terminal status (PO Released / Cash Buy):**

`isTerminalStatus = procurementStatusId === 7 || === 9`. When terminal, the editable card is
replaced by a read-only summary card: badge + "Procurement complete — PO# XXX, Delivery: DATE".
**Hold (8) is NOT terminal** — procurement can move a held indent to PO Released or Cash Buy later.
The backend (`updateProcurementStatus`) places no restriction on transitions out of any sub-status
(only requires `finalStatus=4`), and no legacy documentation indicates Hold was terminal, so this
matches both. Note: the lock is UI-level only; the backend still accepts updates after 7/9.

**FIX 4 — Unique color per status (`constants/indentStatus.ts` + `styles/main.scss`):**

Before: heavy collisions — `info` shared by 4 statuses, `success` by 5, `danger` by 4, `secondary` by 4.
After: every status in the regular indent workflow has a unique color. Approach chosen:
**custom `.badge.bg-*` CSS classes in main.scss** (purple, orange, teal, cyan, dark-green, maroon,
indigo, olive, pink, brown). Because react-bootstrap `<Badge bg={x}>` renders class `bg-{x}`,
custom suffixes work through the existing `INDENT_STATUS_COLORS` lookup with **zero consumer changes** —
Indent list, Indent detail, Plant Indent list, Issue Notes list, and IndentApprovalPage all pick the
new colors up automatically. Also added `.badge.bg-light { color: #212529 }` so the In Progress
fallback stays legible. Reports were the one gap: `IndentReportPage` (hardcoded `bg="secondary"`)
and `IssueNoteReportPage` (local `statusBadgeColor()` by numeric id) now route through
`INDENT_STATUS_COLORS` first.

Key assignments: Pending=warning, RM Approved=info, RM Rejected=danger, Dept. Head Approved=primary,
Dept. Head Rejected=maroon, Quotations Collected=secondary, Negotiation Done=purple, PO Released=success,
Hold=orange, Cash Buy=teal, Goods Receipt=cyan, Goods Issued=dark-green, Stores Rejected=pink,
In Progress=light. Reuse only where statuses can never co-occur in one view (plant-only 'Rejected'
vs regular-only 'RM Rejected'; 'Completed' vs 'PO Released').

**FIX 5 — Approval flowchart corrected (`IndentDetailPage.tsx`):**

Before: a 3-step timeline (Submitted → Dept Head Approval → Final Approval) driven entirely by the
legacy single `statusId` — wrong stage names and ignored the three-column model.
After: a 4-stage stepper driven by the three-column state:

| Stage | State source | Display |
|---|---|---|
| Submitted | `statusId >= 2` | ✓ done / grey |
| RM Review | `approvedStatus` 1=pending, 3=approved, 2=rejected | ✓ / ✗ / amber Pending + approver name/date |
| Dept Head Review | `finalStatus` 1=pending, 4=approved, 2=rejected (reachable only if RM approved) | ✓ / ✗ / amber Pending + approver name/date |
| Procurement | `procurementStatus` 4=arrived, 5–9=sub-stage (reachable only if DH approved) | sub-stage label; ✓ when 7/9 |

Rejection terminates the flow: RM rejected → Dept Head and Procurement render grey/unreached;
same for Dept Head rejection. Red ✗ icon (FaTimes) marks the rejected stage, and the rejection
alert below now says which level rejected ("Rejected by RM" / "Rejected by Dept Head").
Removed the now-unused `STATUS_PROCUREMENT_APPROVED` constant; added `PROC_SUB_LABELS` map.

**Files changed:**
- `frontend/src/pages/indents/IndentDetailPage.tsx` — FIX 1, 2, 3, 5
- `frontend/src/constants/indentStatus.ts` — FIX 4 color map
- `frontend/src/styles/main.scss` — FIX 4 custom badge classes
- `frontend/src/pages/reports/IndentReportPage.tsx` — FIX 4 reports consistency
- `frontend/src/pages/reports/IssueNoteReportPage.tsx` — FIX 4 reports consistency

**Build:** Backend `mvn compile` clean (no Java changes). Frontend `tsc -b && vite build` clean.

---

## 2026-07-02

### deriveDisplayStatus() Complete Matrix + Flowchart/Submit Decoupled from Spring Status

**Root cause:** `deriveDisplayStatus()` only matched a handful of exact triples; the 562
production PO Released indents `(3,4,7)` and 12 other real combinations fell through to the
`"In Progress"` fallback. Separately, the detail-page flowchart's "Submitted" stage and the
"Submit for Approval" button keyed off the Spring single-column `status` — which migrated
legacy rows carry as `1` (legacy used `indent_status` as an active flag) — so old indents
deep in the workflow showed "Not yet submitted" and a live Submit button.

**FIX 1 — deriveDisplayStatus() rewritten as a hierarchical decision tree** (`IndentService.java`):

RM stage decides first (`approvedStatus`), then Dept Head (`finalStatus`), then procurement
sub-stage (`procurementStatus`). Wildcard matching per level instead of exact-triple matching.
All 13 production combinations verified by unit test (`DeriveDisplayStatusTest`, 4 tests green):

| Triple | Label | Triple | Label |
|---|---|---|---|
| (3,4,7) ×562 | PO Released | (2,2,2) | RM Rejected |
| (3,4,8) ×39 | Hold | (3,4,5) | Quotations Collected |
| (3,4,9) ×34 | Cash Buy | (4,1,1) | RM Approved (legacy edge) |
| (2,1,1) ×23 | RM Rejected | (3,4,6) | Negotiation Done |
| (3,4,4) ×11 | Dept. Head Approved | (1,1,1) | Pending RM Approval |
| (3,2,2) ×10 | Dept. Head Rejected | (3,5,6) | Negotiation Done (legacy edge) |
| (3,1,1) ×10 | RM Approved | | |

Defensive extras beyond the production list: `(3,3,x)` → "Dept. Head Approved" and `(3,5,1)` →
"In Procurement" (pre-repair corruption triples, correct display until the owner runs
`docs/fix-l2-approved-indents.sql`); `(3,4,10/11)` Goods Receipt/Issued preserved;
`(3,4,≤4)` and null procurement → "Dept. Head Approved". `"In Progress"` remains only as a
true fallback. **Accessor confirmed:** the three columns are `IndentStatus` entity references
on `Indent`; the integer is extracted with `.getId()` (null-guarded) at both call sites
(`toIndentResponse`, `toIndentListResponse`). Method visibility changed private→package-private
for the unit test.

New labels "Pending RM Approval" (warning) and "In Procurement" (indigo) added to
`INDENT_STATUS_COLORS`; the list-page filter option "Pending" renamed to match.

**FIX 2 — Flowchart driven purely by the three-column model** (`IndentDetailPage.tsx`):

"Submitted" now always renders green — any persisted indent has entered the workflow; the
Spring status is never consulted. Stage rules: RM green when `approved∈{3,4}`, red when `2`,
amber when `1`; Dept Head green when `final∈{3,4,5}` (legacy ids included), red when `2`,
amber when RM-approved and `final=1`; Procurement reached once Dept Head approved, green when
`proc≥5` with the sub-stage label, amber "Awaiting Procurement" when `proc<5`. Rejection at
either level terminates the flow (later stages grey).

**FIX 3 — Submit (and Edit) button only for true drafts** (`IndentDetailPage.tsx`):

**Finding from `createIndent()`:** drafts are created with `approvedStatus=1, finalStatus=1,
procurementStatus=1` immediately — identical three-column state to a submitted indent. The
draft/submitted distinction lives ONLY in the Spring column (`status` 1=Draft → 2=Submitted via
`submitIndent()`). Therefore the task's literal condition (`approvedStatusId == null || === 0`)
would never be true for ANY indent and would kill the submit path for genuine new-app drafts —
NOT implemented as written. Implemented instead:
`isTrueDraft = status=1 AND approved=1 AND final=1 AND procurement=1` — hides Submit/Edit on
every in-workflow production combination listed above. Residual ambiguity: legacy rows at
exactly `(1,1,1)` with `status=1` are indistinguishable from new-app drafts; those still show
the button (clicking it is benign — sets status=2 and notifies RM). If desired the owner can
bulk-set `indent_status=2` on legacy `(1,1,1)` rows by import-date cutoff. The list page has
no submit button (verified).

**FIX 4 — Single source of truth confirmed:** both `toIndentResponse` and `toIndentListResponse`
call `deriveDisplayStatus`; Indent list, detail, approval page, and IndentReportPage all render
`displayStatus`; CSV/Excel export (`IndentController` lines ~172/212) already uses
`r.displayStatus()` with `statusName` fallback. No page computes labels independently.

**Files changed:**
- `backend/.../indent/IndentService.java` — deriveDisplayStatus rewrite
- `backend/src/test/java/.../indent/DeriveDisplayStatusTest.java` — new: 13-combination verification
- `frontend/src/pages/indents/IndentDetailPage.tsx` — flowchart + isTrueDraft gating
- `frontend/src/pages/indents/IndentsListPage.tsx` — filter label rename
- `frontend/src/constants/indentStatus.ts` — 2 new labels

**Build:** `mvn compile` clean; `mvn test -Dtest=DeriveDisplayStatusTest` 4/4 green;
`tsc -b && vite build` clean.

---

### l2Approve() Status Corruption + PROCUREMENT Sidebar + Procurement Scope

**Root cause — "In Progress" phantom status after DeptHead approval:**

`finalApproveIndent()` (called by the smart-route ApprovalController when status=3) and
`l2Approve()` (called by the legacy `/indents/{id}/l2-approve` endpoint) both produced
invalid three-column triples that fell through `deriveDisplayStatus()` to the "In Progress"
fallback, making DeptHead-approved indents show the wrong status and making the entire
procurement sub-workflow invisible to PROCUREMENT users.

| Method | set status | set finalStatus | set procurementStatus | triple | display |
|---|---|---|---|---|---|
| `finalApproveIndent()` BEFORE | 5 | **5** ← wrong | never set → 1 | (3,5,1) | "In Progress" |
| `finalApproveIndent()` AFTER | 5 | **4** ✓ | **4** ✓ | (3,4,4) | "Dept. Head Approved" |
| `l2Approve()` BEFORE | 3 | **3** ← wrong | never set → 1 | (3,3,1) | "In Progress" |
| `l2Approve()` AFTER | 3 | **4** ✓ | **4** ✓ | (3,4,4) | "Dept. Head Approved" |

The `status=5` in `finalApproveIndent()` is intentionally kept so the smart-router can
route the PROCUREMENT user's subsequent "Procurement Approve" click to `procurementApproveIndent()`.
The display label comes from the three-column triple, not from `status`.

**Root cause — PROCUREMENT sidebar Issue Notes not visible:**

The Issue Notes sidebar item had `roles: [SUPERADMIN, ADMIN, USER, DEPTHEAD, ISSUECONFIRM, SUPERVISOR]`
— PROCUREMENT was absent. The `activeNavItems` filter requires BOTH `hasAnyRole(item.roles)` AND
`hasModuleAccess(item.moduleCode)` to pass. PROCUREMENT failed the first check immediately,
short-circuiting the module check. The router.tsx fix from the previous commit was necessary
but insufficient — route guards prevent 403 on direct URL, but the sidebar never showed the link.

**Sidebar audit — PROCUREMENT presence by nav item:**

| Item | roles restricted? | PROCUREMENT included? | Action |
|---|---|---|---|
| Dashboard | No | Yes (no restriction) | None needed |
| Indents | No (moduleCode only) | Yes (no role gate) | None needed |
| Plant Indent | No (moduleCode only) | Yes (no role gate) | None needed |
| Issue Notes | Yes | **Added** ✓ | Fixed |
| Purchase Orders | Yes | Yes (already present) | None needed |
| Reports group | Yes | No — `[SUPERADMIN, ADMIN, DEPTHEAD, SUPERVISOR]` | Not fixed (separate task) |
| Confirmations | Yes | No — not a procurement workflow item | Not fixed |
| GRN | Yes (future) | No — future item, greyed for all | None needed |
| Quality Control | Yes (future) | No — future item | None needed |

**FIX — PROCUREMENT indent list scoping:**

Removed PROCUREMENT from the `isGlobal` group in `filterIndents()`. Added a dedicated
`isProcurement` branch that forces `approvedStatusId=3` and `finalStatusId=4` as mandatory
filters, ensuring PROCUREMENT only sees indents that have fully cleared the approval chain
and landed in the procurement queue. The user-selected `procurementStatusId` is passed through
so PROCUREMENT can still filter by sub-stage (Quotations Collected, PO Released, etc.).

**Data repair SQL:**

Written to `docs/fix-l2-approved-indents.sql` — covers both corruption paths:
- Path A: `(3, 5, 1)` with `status=5` — from `finalApproveIndent()` smart-route
- Path B: `(3, 3, 1)` with `status=3` — from `l2Approve()` legacy endpoint
Business owner reviews SELECT output, then uncomments and runs the UPDATE for each path.

**Execution path clarification:**

The smart-route `POST /api/v1/approvals/indents/{id}/approve` (ApprovalController) is the
**primary path** used by the current frontend (`indentsApi.approve()`). It routes:
- status=2 → `approveIndent()` (RM L1 approval: sets status=3, approvedStatus=3)
- status=3 → `finalApproveIndent()` ← **real DeptHead approval path**
- status=5 → `procurementApproveIndent()`

`l1Approve()` / `l2Approve()` are reached only via the legacy
`POST /api/v1/indents/{id}/l1-approve` and `/l2-approve` endpoints in IndentController.
Both methods are now also fixed for consistency.

**Files changed:**
- `backend/.../indent/IndentService.java` — `finalApproveIndent()`: finalStatus 5→4, procurementStatus added as 4; `l2Approve()`: finalStatus 3→4, procurementStatus added as 4; `filterIndents()`: PROCUREMENT removed from isGlobal, new isProcurement branch added
- `frontend/src/components/layout/Sidebar.tsx` — Issue Notes roles: PROCUREMENT added
- `docs/fix-l2-approved-indents.sql` — new: manual data repair for production

**Build:** Backend `mvn compile` clean. Frontend `tsc -b && vite build` clean.

---

### Procurement Role — UI Workflow + Route Access + Filter Cleanup

**Scope:** Four-part task completing PROCUREMENT role integration in the frontend: module access via issue-notes routes, procurement status update sub-workflow on indent detail, Stores issue/reject on issue note detail, and filter cleanup.

**Changes:**

#### PART 1 — Issue Notes module: add PROCUREMENT to route guards
- `frontend/src/routes/router.tsx` — Added `'PROCUREMENT'` to both `issue-notes` list (index) and `issue-notes/:id` detail ProtectedRoute roles arrays.
- Backend GET list `@PreAuthorize` already included PROCUREMENT — no backend change needed.

#### PART 2 — Indent detail: procurement sub-status update UI
- `frontend/src/api/indents.ts` — Added `procurementUpdate(id, data)` calling `POST /api/v1/indents/{id}/procurement-update` with `{ procurementSubStatus, poNumber?, deliveryDate?, remarks? }`.
- `frontend/src/pages/indents/IndentDetailPage.tsx`:
  - Added state: `procSubStatus` (default 5), `procPoNumber`, `procDeliveryDate`, `procRemarks`.
  - Added `procurementUpdateMutation`.
  - Added `isProcurementStage = finalStatusId === 4 && procurementStatusIdVal >= 4 && procurementStatusIdVal <= 9` flag.
  - Added `canUpdateProcurement = isProcurementStage && hasAnyRole(['SUPERADMIN', 'ADMIN', 'PROCUREMENT'])`.
  - Rendered inline "Procurement Status Update" card (no modal) with a dropdown (Quotations Collected=5, Negotiation Done=6, PO Released=7, Hold=8, Cash Buy=9) and conditional extra fields: PO Released shows poNumber + deliveryDate; Hold shows remarks; Cash Buy shows deliveryDate.

#### PART 3 — Issue note detail: Goods Issued + Reject (Stores) buttons
- `frontend/src/pages/issue-notes/IssueNoteDetailPage.tsx`:
  - Fixed `canIssue`: was checking flat `status` enum (`APPROVED_BY_MANAGER || PENDING_STORE_ISSUE`). Now uses two-column: `issueNote.approvedStatus === 3 && issueNote.storesByStatus === 1`.
  - Added `showStoresRejectModal`, `storesRejectReason` state.
  - Added `storesRejectMutation` calling `issueNotesApi.storesReject(id, { reason })` → `POST /issue-notes/{id}/reject-stores`.
  - Renamed "Issue Materials" button to "Goods Issued".
  - Added "Reject (Stores)" button alongside "Goods Issued" when `canIssue`.
  - Added Stores Rejection modal with required reason field.

#### PART 4 — Remove Goods Receipt / Goods Issued from indent status filter
- `frontend/src/pages/indents/IndentsListPage.tsx`:
  - Removed `'Goods Receipt'` (procurementStatus=10) and `'Goods Issued'` (procurementStatus=11) from `STATUS_FILTERS` map.
  - Removed corresponding `<option>` elements from the status dropdown.
  - `IndentReportPage.tsx` has no status dropdown — no change needed there.

**Confirmed clean (no change needed):**
- PROCUREMENT already in `filterIndents()` `isGlobal` group in `IndentService.java` ✓
- `issueNotesApi.storesReject()` already implemented in `issueNotes.ts` ✓
- Backend `POST /api/v1/indents/{id}/procurement-update` already exists with correct `@PreAuthorize("hasAnyRole('PROCUREMENT', 'ADMIN', 'SUPERADMIN')")` ✓

**Build:** Clean (`tsc -b && vite build` — 0 TypeScript errors)

**Commit:** (see git log)

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

## 2026-07-01 (fourth entry)

### Role Code Mismatch Audit — `"Department Head"` Never Mapped in RoleNormalizer

**Commit:** `86da7be`

**Root cause (system-wide):**
`tbl_roles_master.role_code` stores `"Department Head"` (with a space). `RoleNormalizer.java` had `Map.entry("Department", "DEPTHEAD")` — a dead mapping whose key never matched any real DB row. The fallback (`rawCode.replaceAll("\\s+","").toUpperCase()`) produced `"DEPARTMENTHEAD"`. Every `@PreAuthorize("hasRole('DEPTHEAD')")`, `hasAnyRole(['DEPTHEAD'])`, and module access check for Department Head silently failed system-wide since RoleNormalizer was introduced.

---

#### FIX 1 — RoleNormalizer.java

**Before:**
```
"Department"  → "DEPTHEAD"   ← dead (no DB row has code "Department")
```

**After:**
```
"Department Head" → "DEPTHEAD"   ← primary (actual DB role_code)
"Department"      → "DEPTHEAD"   ← legacy alias (kept for safety)
```

Both keys map to `"DEPTHEAD"`. Keeping `"Department"` means any cached JWTs that were issued using the old (broken) fallback still work after expiry. New logins pick up `"Department Head"` → `"DEPTHEAD"`.

**Complete mapping table (after fix):**

| DB `role_code`   | Normalized JWT role  |
|------------------|----------------------|
| Super Admin      | SUPERADMIN           |
| Admin            | ADMIN                |
| User             | USER                 |
| Supervisor       | SUPERVISOR           |
| Department Head  | DEPTHEAD (primary)   |
| Department       | DEPTHEAD (alias)     |
| Procurement      | PROCUREMENT          |
| Plant Manager    | PLANTMANAGER         |
| FloorIncharge    | FLOORINCHARGE        |
| DataEntry        | DATAENTRYOPERATOR    |
| GoodsIncharge    | GOODSINCHARGE        |
| GRNIncharge      | GRNINCHARGE          |
| IssueConfirm     | ISSUECONFIRM         |
| ReceiptConfirm   | RECEIPTCONFIRM       |
| QualityManager   | QUALITYMANAGER       |

---

#### FIX 2 — hasRoleByCode() callers (IndentService.java)

`hasRoleByCode()` queries raw DB `role_code` via `er.getRole().getCode()` with `equalsIgnoreCase`. All callers passing `"Department"` were changed to `"Department Head"`:

- `submitIndent()` DEPTHEAD bypass: `hasRoleByCode(empNum, "Department Head")`
- `approveIndent()` `hasDeptLevelAuth` check: `hasRoleByCode(empNum, "Department Head")`

Note: `"Plant Manager"`, `"Admin"`, `"Super Admin"` are correct (DB values match).

---

#### FIX 3 — ModuleAccessService.isAllowedByRole()

**Before:** Uppercase-only comparison on `module_default_roles` tokens.
```
"Department Head" → toUpperCase → "DEPARTMENT HEAD"  ≠  "DEPTHEAD"  → no match
```

**After:** Tokens normalized through `RoleNormalizer.normalize()` before comparing.
```
"Department Head" → normalize → "DEPTHEAD"  ==  "DEPTHEAD"  → match ✓
"ALL"             → "ALL" special case → universal grant ✓
```

JWT roles are already normalized, so no transformation needed on the user side.

---

#### FIX 4 — AuthContext.tsx hasAnyRole()

Added `ADMIN` to the universal bypass alongside `SUPERADMIN`:
```typescript
if (user.roles.includes('SUPERADMIN') || user.roles.includes('ADMIN')) return true;
```
ADMIN should have the same UI pass-through as SUPERADMIN for role-gated elements.

---

#### STEP 5 — canView read logic (verified, no change)

`AuthService.java` already reads `canView` as:
```java
"1".equals(r.getCanView()) || "true".equalsIgnoreCase(r.getCanView())
```
The string `"false"` does not match either condition → evaluates correctly to `false` for Supervisor. No code change needed.

---

**Files changed:**
- `backend/.../security/RoleNormalizer.java` — Fix 1
- `backend/.../indent/IndentService.java` — Fix 2
- `backend/.../moduleaccess/ModuleAccessService.java` — Fix 3
- `frontend/src/contexts/AuthContext.tsx` — Fix 4

**Build results:**
- `mvn compile` — clean, 0 errors
- `tsc -b` — clean, 0 errors
- `vite build` — built in 34.22s, 0 errors (pre-existing chunk-size warning)

---

## 2026-07-01 (fifth entry)

### IndentDetailPage — Approve/Reject Buttons Not Showing for DEPTHEAD

**Commit:** `7bbe14a`

**Root causes (two, compounding):**

**Root cause A — Cached JWT (re-login required):**
Before commit `86da7be` (RoleNormalizer fix), the `"Department Head"` DB role fell to the fallback normalizer → `"DEPARTMENTHEAD"`. Any DEPTHEAD user who was already logged in has a cached JWT with `"DEPARTMENTHEAD"`, so `hasAnyRole(['DEPTHEAD'])` returns `false`. Fix: user must log out and log back in to receive a new JWT with `"DEPTHEAD"`. This is not a code change — it is a session invalidation requirement.

**Root cause B — canApprove used the wrong workflow signal (code bug):**
`canApprove` checked `statusId === STATUS_SUBMITTED (2)` for the DEPTHEAD condition. The main `status` column stays at `2` through BOTH the RM-pending stage (approvedStatus=1) AND the RM-approved stage (approvedStatus=3). This meant:
- DEPTHEAD saw the Approve button when indent was still waiting for RM (wrong — RM hasn't acted yet)
- SUPERVISOR was grouped with DEPTHEAD at `statusId===2`, so SUPERVISOR also saw the button on RM-approved indents that should only show for DEPTHEAD

**Fix — use `approvedStatusId` and `finalStatusId` from `IndentResponse`:**

`IndentResponse` already returns `approvedStatusId`, `finalStatusId`, and `procurementStatusId`. The fix reads those fields to detect the exact workflow stage, then gates each role to its correct stage:

| Stage | Condition | Roles that can act |
|-------|-----------|-------------------|
| L1 RM Review | `approvedStatusId=1` (pending RM) | SUPERVISOR, ADMIN, SUPERADMIN |
| L2 Dept Head | `approvedStatusId=3 AND finalStatusId=1` | DEPTHEAD, PLANTMANAGER, ADMIN, SUPERADMIN |
| Procurement | `finalStatusId=4 AND procurementStatusId=4` | PROCUREMENT, ADMIN, SUPERADMIN |

The approve button label was also corrected: L1 → "Approve (RM Review)", L2 → "Approve (Dept Head)", Procurement → "Procurement Approve".

**File changed:** `frontend/src/pages/indents/IndentDetailPage.tsx`

**Build results:**
- `tsc -b` — clean, 0 errors

---
