# ProcureZone Development Rules — MANDATORY, READ BEFORE EVERY TASK

These rules exist because past changes have re-broken previously-fixed functionality
multiple times (visibility scoping reverted, creation flows silently failing, buttons
disappearing) — each fix "untying one knot while tying another." This must stop.

## Rule 1 — Read the current code before changing it. Never guess, never rewrite from memory.
Before editing any file, `view` its current, exact content. Never reconstruct a file's
content from memory or from an earlier version seen in this conversation. If a file was
touched in an earlier commit, re-view it fresh — do not assume it still looks the way
it was last reported.

## Rule 2 — Targeted edits only. Never rewrite a whole file when a small change will do.
Use `str_replace` for surgical changes. A full-file rewrite risks silently reverting
unrelated fixes that exist elsewhere in that file. If a file must be substantially
restructured, diff the old vs new version explicitly and list every behavioral change,
not just the intended one.

## Rule 3 — Before touching a shared method, list every caller and confirm each still works.
Methods like `filterIndents()`, `deriveDisplayStatus()`, `l1Approve()`, role-gating
helpers, etc. are called from many places. Before changing one, grep for every call site.
After changing it, mentally (or explicitly) re-verify each call site's behavior is
unchanged unless the task specifically intends to change it.

## Rule 4 — Never mix unrelated changes in one task. One task = one concern.
If a task is scoped to "add quantity editing," do not touch visibility scoping, route
guards, or button conditions unless the task explicitly requires it. If you notice
something unrelated that looks wrong, REPORT it, don't fix it silently in the same commit.

## Rule 5 — After any change, explicitly re-verify the previously-fixed behaviors that
touch the same files/methods. Keep a mental (or written) list of "known-good behaviors"
for any file you're editing, based on this project's stabilization log, and confirm each
still holds after your edit — before declaring the task done.

## Rule 6 — Report a diff-shaped summary, not just a feature-shaped summary.
When reporting completion, include "before" and "after" for every method/component
changed — not just what the new feature does. This is how regressions get caught before
deployment instead of after.

## Rule 7 — Compile passing is not verification. HQL/JPQL errors, role-gating regressions,
and silent frontend failures do NOT show up in `mvn compile`. State explicitly what
verification was actually done (context startup, a manual trace through the changed
logic, specific role/permission paths checked) — do not imply full verification when only
compilation was checked.

## Rule 8 — When a task involves DTOs or shared response shapes, grep every frontend
consumer of that DTO before and after. A field rename or restructure can silently break
a page that wasn't in scope for the current task.

## Rule 9 — Never let error-handling swallow failures silently. Any catch block that
suppresses an exception without surfacing it (to logs at minimum, to the user where
appropriate) is a bug. A failed submit must show an error, not silently return to a list
page as if nothing happened.

## Rule 10 — If a change to fix bug A requires modifying code related to previously-fixed
bug B, stop and explicitly flag it before proceeding. Do not silently trade one fix for
another.

---

# General software engineering discipline (applies always)

- **Single responsibility**: one function/method/component does one thing.
- **Don't repeat yourself**, but don't over-abstract prematurely either.
- **Fail loud, not silent**: errors must surface — to logs, to the user, or both.
- **Validate at the boundary**: server-side validation is authoritative; frontend
  validation is convenience only.
- **Idempotency where possible**: operations that might be retried (imports, approvals)
  should not double-apply.
- **Backward compatibility**: don't remove/rename a working endpoint or field without
  confirming nothing else depends on it.
- **Small, reviewable diffs**: prefer several small, clearly-scoped commits over one huge
  commit touching many unrelated files.
- **Test the unhappy path**, not just the happy path — permission denials, empty states,
  boundary values (zero quantity, no data).
- **Never trust "it compiled" as "it works"**: runtime behavior (HQL validation, role
  checks, actual data flow) must be reasoned about explicitly, not inferred from a green
  build.
- **Explicit over implicit**: prefer clear, explicit role/permission checks over clever
  shortcuts that are easy to accidentally revert.
