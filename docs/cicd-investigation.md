# CI/CD Investigation — why the pipeline stopped swapping the deployed jar (~July 7)

**Date:** 2026-07-15
**Scope:** Investigation only. No changes. Diagnose why GitHub Actions "green" runs stopped
updating the running app; manual `sudo` deploys have been required since ~July 7.

## STEP 1 — The workflow (`.github/workflows/deploy.yml`, the git-tracked one)

Trigger: `push` to `main` or `auth/employee-only-login`. Runner: `self-hosted`. Steps:
1. **Checkout** (`actions/checkout@v4`).
2. **Set up Java 21** (temurin, maven cache).
3. **Set up Node 20** (npm cache on `frontend/package-lock.json`).
4. **Build backend JAR:** `mvn package -DskipTests -f backend/pom.xml`.
5. **Build frontend:** `cd frontend && npm ci && npm run build`.
6. **Deploy JAR:** `cp backend/target/procurezone-backend-0.1.0-SNAPSHOT.jar /opt/procurezone/backend/…` — **plain cp, NO sudo.**
7. **Deploy frontend:** `rm -rf /opt/procurezone/frontend/* && cp -r frontend/dist/* /opt/procurezone/frontend/` — **plain, NO sudo.**
8. **Restart application:** `sudo systemctl restart procurezone` — **sudo.**
9. **Reload Nginx:** `sudo systemctl reload nginx` — **sudo.**
10. **Health check:** poll `http://localhost:8080/actuator/health` up to 12×10s; on non-200 within 2 min, `tail -50 /opt/procurezone/logs/app.log` and `exit 1`.

Ruled out by inspection: the hardcoded jar name matches the pom (`artifactId=procurezone-backend`,
`version=0.1.0-SNAPSHOT`, no `finalName` override) — no version drift. Frontend `npm run build`
(`tsc -b && vite build`) emits `dist/` — matches the copy. So it is NOT a renamed-artifact or
wrong-output-dir problem.

## STEP 2 — Deploy mechanism
Self-hosted runner at `/home/nsl/actions-runner/`. It builds in its checkout
(`_work/ProcureZone_version2/ProcureZone_version2`), copies the jar to `/opt/procurezone/backend/`
and the frontend to `/opt/procurezone/frontend/`, then `sudo systemctl restart procurezone` +
`sudo systemctl reload nginx`, then health-gates on `/actuator/health` (success = HTTP 200).

## STEP 3 — Likely failure point

Key fact about GitHub Actions: `run:` steps use `bash --noprofile --norc -e -o pipefail` by default,
and there is **no `continue-on-error`** anywhere. So **any** failing command turns its step — and the
whole run — **RED**. A genuinely green run therefore means steps 6–9 all returned 0.

The split between **plain `cp` (steps 6–7)** and **`sudo` (steps 8–9)** is the tell, and it lines up
exactly with the manual workaround (which uses `sudo cp` and `sudo systemctl restart`). That manual
process proves the interactive admin needs sudo to write `/opt/procurezone` and to restart — i.e.
`/opt/procurezone` is **not** writable by an ordinary user and the service restart needs root.

Two mutually-exclusive readings, and the owner's run logs decide which:

- **(A) The runs are actually RED at Deploy JAR or Restart (most likely).** If the runner user can't
  write `/opt/procurezone/backend`, step 6's `cp` fails → RED, jar never swapped. If it can write but
  lacks passwordless sudo, step 8's `sudo systemctl restart` fails ("sudo: a password is required")
  → RED. Either way the jar/app stops updating and manual deploys fill the gap. "Green" is then a
  misread (e.g. looking at the last pre-July-7 success, or the build steps being green while a later
  step is red). Something around July 7 flipped this: a `/opt/procurezone` ownership/permission
  change, a server reboot, the runner service re-registering under a different user, or a sudoers
  edit.

- **(B) The runs are genuinely green but the service runs a different jar.** Possible only if steps
  6–9 all pass (runner owns `/opt/procurezone` AND has passwordless sudo) yet `procurezone.service`'s
  `ExecStart` points at a **different path** than `/opt/procurezone/backend/procurezone-backend-0.1.0-SNAPSHOT.jar`
  (e.g. a versioned filename, a symlink not updated, or `/opt/tomcat/...`). Then cp updates a file
  nobody runs → green, app unchanged.

Reading (A) is far more probable given the manual deploys explicitly use `sudo`.

## STEP 4 — sudo / permissions (owner must confirm — no server access here)
Run on the server and report:
```bash
# 1. What user runs the self-hosted runner?
ps -o user= -p "$(pgrep -f actions-runner/bin/Runner.Listener | head -1)"
sudo systemctl cat actions.runner.* 2>/dev/null | grep -iE 'User=|ExecStart'   # if installed as a service

# 2. Can that user write the deploy targets WITHOUT sudo? (run AS the runner user)
sudo -u <runner_user> test -w /opt/procurezone/backend && echo "backend writable" || echo "NOT writable"
sudo -u <runner_user> test -w /opt/procurezone/frontend && echo "frontend writable" || echo "NOT writable"
ls -ld /opt/procurezone /opt/procurezone/backend /opt/procurezone/frontend

# 3. Does the runner user have PASSWORDLESS sudo for the restart/reload?  (-n = never prompt)
sudo -u <runner_user> sudo -n systemctl restart procurezone && echo "restart OK (passwordless)" || echo "restart BLOCKED"
sudo -u <runner_user> sudo -n systemctl reload nginx && echo "nginx OK" || echo "nginx BLOCKED"

# 4. Where does the service actually load the jar from? (reading (B) check)
sudo systemctl cat procurezone | grep -iE 'ExecStart|WorkingDirectory'
ls -la /opt/procurezone/backend/         # jar timestamp — did it change on the last "green" run?
```
Also, in the GitHub Actions UI, open the most recent run and report the **per-step status** — which
step (Deploy JAR / Restart / Health check) is the first non-green, and its log output. That single
screen resolves (A) vs (B) immediately.

## Additional finding — duplicate workflow file outside the repo
There are two `deploy.yml`s: the git-tracked `Net-Beans/Net-Beans/.github/workflows/deploy.yml` (69
lines, the one GitHub runs) and a **non-tracked** `Net-Beans/.github/workflows/deploy.yml` (91 lines)
one directory above the repo root. GitHub only uses the tracked one; the outer file is a stray local
copy (different content/size) and is ignored by CI — but it's confusing and should be removed to
avoid someone editing the wrong file.

## STEP 5 — Fix plan (for a later task; nothing changed here)

1. **Make the deploy steps able to write + restart non-interactively.** Pick one:
   - **Ownership (simplest):** `chown -R <runner_user> /opt/procurezone` so steps 6–7 need no sudo,
     and add a **passwordless sudoers** entry scoped to only the two restart/reload commands:
     ```
     <runner_user> ALL=(root) NOPASSWD: /usr/bin/systemctl restart procurezone, /usr/bin/systemctl reload nginx
     ```
   - Or run the deploy over SSH to a small privileged script. The scoped-sudoers + chown is the least
     moving parts.
2. **Confirm the service jar path** matches the copy target (reading (B)); if it uses a versioned name
   or symlink, either copy to that exact path or update the symlink in the workflow.
3. **Make failures loud:** the pipeline already has `bash -e`, but add explicit checks after the copy
   (`test -f /opt/procurezone/backend/...jar` and compare mtime) so a no-op copy can't look green.
4. **Startup health-gate + rollback (would have caught the July 14/15 `reportStart` outage):** the
   health check already fails the run on non-200 — but by then the bad jar is already deployed.
   Improve to: back up the current jar before step 6 (`cp …jar …jar.prev`); after restart, poll
   `/actuator/health`; if it doesn't reach UP within N seconds, **restore `…jar.prev` and restart**
   so a bad build auto-rolls-back instead of leaving the app down. Since builds use `-DskipTests`,
   this health-gate is the only thing standing between a startup-breaking commit and an outage.
5. **Remove the stray out-of-repo `deploy.yml`.**

**Bottom line:** the workflow itself is sound in shape; the break is almost certainly environmental —
the self-hosted runner user cannot write `/opt/procurezone` and/or cannot `sudo` non-interactively, so
the Deploy/Restart steps fail (turning runs red at those steps while the owner deploys manually with
sudo). The owner's per-step run log + the `sudo -n` / `test -w` checks above will confirm which of the
two commands is the block, and the fix is passwordless-sudo-scoping + ownership, plus a
health-gated rollback so this class of outage can't recur silently.
