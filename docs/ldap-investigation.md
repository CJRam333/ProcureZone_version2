# LDAP Login Investigation — Legacy vs New System

**Date:** 2026-07-06
**Scope:** Investigation only. No implementation. Explains why LDAP login does not work in the new
Spring Boot system and what must be built to enable it.

**Method note:** the legacy app ships as compiled `.class` files (no `.java` source present). The
legacy flow below was reconstructed with `javap -c -p` from
`WEB-INF/classes/seeds/login/action/LoginAction.class`,
`WEB-INF/classes/seeds/masters/action/LdapService.class`,
`WEB-INF/classes/seeds/masters/action/EmployeeService.class`, and the Hibernate mapping
`WEB-INF/classes/pojo/TblLdapConfig.hbm.xml`. Class/method/string constants are quoted verbatim.

---

## 1. Legacy LDAP flow (complete)

**Library:** plain **JNDI** — `com.sun.jndi.ldap.LdapCtxFactory` with a `java.util.Hashtable`
environment. No Spring LDAP, no Spring Security. `javax.naming.directory.SearchControls` is used
for the AD search.

**Entry point:** `seeds.login.action.LoginAction.getLogin()` (Struts 2 action). It reads the login
form fields `userName` + `password` and decides the auth path **purely on whether `userName`
contains `"@"`:**

```
getLogin():
  if (userName contains "@")  →  EmployeeService.checkLdapLogin(userName, password)   // LDAP
  else                        →  EmployeeService.checkLogin(userName, password)        // local DB
  if (login ok) → getUserObject(userName, password) → build session → "success"
                else → "Invalid User Name Or Password"
```

There is **no fallback** between the two — it is strictly one path or the other based on the
username format. This is also how the system decides "who is an LDAP user": **there is no per-user
flag column** — an account is treated as LDAP simply because the person logs in with an
`name@domain` email. (Most users log in with their `emp_email`, so in practice email login = LDAP.)

**`EmployeeService.checkLogin(userName, password)` — local DB auth:**
```sql
SELECT count(empEmail) FROM tbl_emp_master
 WHERE empEmail = '<userName>' AND empPassword = '<password>' AND empStatus = 1
```
Plaintext password compared directly in SQL against `emp_password`. (No hashing in the legacy
local path.)

**`EmployeeService.checkLdapLogin(userName, password)` — directory auth:**
```
domain = userName.split("@")[1]
if (domain.equals("nslindia.com"))  →  LdapService.ADLdap(userName, password)   // "AD LDAP LOGIN"
else                                →  LdapService.OpenLdap(userName, password)  // "OPEN LDAP LOGIN"
// on bind success, confirm the account exists & is active:
SELECT count(empEmail) FROM tbl_emp_master WHERE empEmail = '<userName>' AND empStatus = 1
```

**`LdapService.ADLdap(user, pwd)` — Active Directory (service-account bind + search):**
- Loads config: `LdapConfigDaoImpl.getList(" where configDomine ='nslindia.com'")` → `TblLdapConfig`.
- JNDI env:
  - `java.naming.factory.initial = com.sun.jndi.ldap.LdapCtxFactory`
  - `java.naming.provider.url = configUrl`
  - `java.naming.security.authentication = simple`
  - `java.naming.security.principal = "cn=" + configUser + ",cn=users," + configPrinc`
  - `java.naming.security.credentials = configPwd`  (service/bind account)
- Search base seen in constants: `dc=nslgroup,dc=local`; search filter `mail=<user>` via
  `getSimpleSearchControls()` (subtree). The matched user's DN/`cn` is then used to authenticate
  the supplied password. (The AD domain label `nslindia.com` and the directory base
  `dc=nslgroup,dc=local` are different strings — the email domain is the router key, the base DN is
  where the directory search runs.)

**`LdapService.OpenLdap(user, pwd)` — OpenLDAP (direct user bind):**
- JNDI env same factory/simple auth, but principal is a **direct user bind**:
  `java.naming.security.principal = "uid=" + <user> + "," + <base>` with
  `java.naming.security.credentials = <password>` (the user's own password). Config row selected by
  the login's own domain (`where configDomine ='<domain>'`).

**Summary of the legacy decision tree**

| Login `userName` | Path | Mechanism |
|---|---|---|
| no `@` | local | plaintext `emp_password` SQL match |
| `x@nslindia.com` | AD LDAP | service bind (`cn=configUser,cn=users,configPrinc`) → `mail=` search under `dc=nslgroup,dc=local` → verify password |
| `x@<other-domain>` | OpenLDAP | direct `uid=<user>` bind with the user's password |

---

## 2. LDAP config location in the DB

**Config table:** `tbl_ldap_config` (legacy catalog `seeds_indent`), mapped by `pojo.TblLdapConfig`.
Columns (from the Hibernate mapping):

| Column | Meaning |
|---|---|
| `config_id` | PK |
| `config_domine` | domain key used to select the row (e.g. `nslindia.com`) |
| `config_url` | **LDAP server URL** — `ldap://<host>:<port>` (this is the host/port to network-test) |
| `config_user` | bind / service account username (AD path) |
| `config_pwd` | bind / service account password |
| `config_princ` | base principal fragment appended after `cn=<user>,cn=users,` (base DN tail) |
| `config_lmd` | last-modified date (string) |
| `config_lmu` | last-modified user |

**"LDAP user" flag:** none. Legacy infers it from the `"@"` in the login name; the user record
lives in `tbl_emp_master` (`emp_email`, `emp_password`, `emp_status`). The new system already
encodes the same convention in comments (`CreateEmployeeRequest`: "email contains '@' → LDAP user";
`EmployeeService`: LDAP users store empty `emp_password`).

### SQL for the business owner to run

```sql
-- 1. The LDAP server config (gives the host/port + bind account for the network test)
DESCRIBE tbl_ldap_config;
SELECT config_id, config_domine, config_url, config_user, config_princ FROM tbl_ldap_config;
-- (config_pwd intentionally omitted from the report; you can inspect it privately if needed)

-- 2. Confirm how users are stored (LDAP users should have empty/blank emp_password)
DESCRIBE tbl_emp_master;
SELECT emp_number, emp_email, emp_status,
       CASE WHEN emp_password IS NULL OR emp_password = '' THEN 'LDAP (no local pwd)'
            ELSE 'has local pwd' END AS pwd_state
FROM tbl_emp_master
WHERE emp_status = 1
LIMIT 20;
```

The `config_url` value from query (1) is what you plug into the network test in section 5.

---

## 3. New system LDAP code state

- **Dependencies (`backend/pom.xml`):** `spring-boot-starter-security` is present, but there is
  **no** `spring-security-ldap`, **no** `spring-ldap-core`, and no JNDI LDAP usage. Missing.
- **LDAP service class:** **none.** A repo-wide `grep -rn "ldap|Ldap|LDAP"` over
  `backend/src/main/java` matches only two files — `employee/EmployeeService.java` and
  `employee/dto/CreateEmployeeRequest.java` — and in both it is **comment text only**, describing
  the "email contains '@' → LDAP user" convention and that LDAP users store an empty `emp_password`.
  No `ADLdap`/`OpenLdap` equivalent, no `LdapContextSource`, no `InitialDirContext`.
- **Login path (`AuthService.login()`):** DB-only. It loads the employee by email
  (`findByEmailIgnoreCase`), checks `emp_status == 1`, then verifies the password against
  `emp_password` via `PasswordService.verifyPassword` (auto-detects MD5 vs BCrypt). It **never**
  inspects `"@"`, never selects an auth path, and never binds to a directory.
- **Config (`application.yml`, `application-prod.yml`):** **no** LDAP settings (`grep` for
  `ldap|directory|active.directory` → none). Nothing reads `tbl_ldap_config`; there is no
  `LdapConfig` entity/repository in the new backend.

**Conclusion:** LDAP is entirely absent from the new system — not dead code, simply **never built**.
Only the naming convention survived, in comments.

---

## 4. Specific gaps preventing LDAP login

1. **No LDAP authentication code exists** in the new backend (no service, no context, no bind).
2. **Login never routes `@` users to LDAP** — `AuthService.login()` is unconditionally a local
   `emp_password` check.
3. **LDAP users cannot authenticate locally by design** — they are created with an empty
   `emp_password` (`EmployeeService` stores `""` for LDAP users), so `verifyPassword` always fails
   for them. Today an LDAP user simply *cannot log in at all*.
4. **No LDAP dependency** on the classpath (`spring-security-ldap` / `spring-ldap-core` / JNDI).
5. **No LDAP configuration wiring** — nothing reads `tbl_ldap_config` and there are no LDAP keys in
   the yml. The directory host/port/bind account are unknown to the app.
6. **(To be confirmed) network reachability** — even once built, the app server must reach the
   directory host/port (section 5).

---

## 5. Network reachability test

First get the host/port from `tbl_ldap_config.config_url` (section 2 query 1). It will look like
`ldap://<host>:389` (AD default) or `ldaps://<host>:636`. Then, **from the app server
(172.16.9.158)**, test TCP reachability:

```bash
# Linux app server (replace host/port with the values from config_url):
nc -zv <ldap_host> 389
# LDAPS:
nc -zv <ldap_host> 636
```

```powershell
# If the app server is Windows:
Test-NetConnection -ComputerName <ldap_host> -Port 389
```

A successful TCP connect confirms the directory is reachable; a timeout/refused means a
firewall/routing problem to fix before LDAP can work regardless of code.

---

## 6. Implementation recommendation (for a later task — not done here)

To restore LDAP login in the new system, matching legacy behaviour:

1. **Add dependencies:** `spring-ldap-core` + `spring-security-ldap` (or use raw JNDI to mirror
   legacy exactly and avoid new abstractions).
2. **Add config source:** either map `tbl_ldap_config` as a JPA entity/repository (keeps the single
   source of truth the legacy app used) **or** move the values into `application-prod.yml`
   (`ldap.url`, `ldap.base`, `ldap.bind-dn`, `ldap.bind-password`, `ldap.domain`). Reusing
   `tbl_ldap_config` is closer to legacy and avoids storing the bind password in yml.
3. **Add an `LdapAuthService`** replicating the two legacy methods:
   - AD: service-account bind (`cn=configUser,cn=users,configPrinc` / `configPwd`), subtree search
     `mail=<login>` under the configured base, then authenticate the supplied password against the
     found DN.
   - OpenLDAP: direct `uid=<user>,<base>` bind with the user's password.
4. **Branch in `AuthService.login()`** on the same rule: `username.contains("@")` → LDAP
   (`domain == nslindia.com` → AD, else OpenLDAP); no `@` → existing local `emp_password` check.
   On LDAP bind success, load the employee by `emp_email` + `emp_status = 1` and issue the JWT
   exactly as the local path does (roles, companyIds, etc. are unchanged).
5. **Do not require `emp_password`** for LDAP users (already the case — stored as `""`).
6. **Verify network reachability** (section 5) from 172.16.9.158 before enabling in prod, and
   decide LDAP vs LDAPS (389 vs 636).

**Decision needed from the business owner before implementation:**
- Confirm the `config_url`, base DN, and that the `nslindia.com` → AD / other → OpenLDAP split still
  applies (or whether only AD is used now).
- Confirm the directory is reachable from the app server (section 5).
- Confirm whether config should live in `tbl_ldap_config` (preferred) or yml.
