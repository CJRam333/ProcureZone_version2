// From LoginAction.java lines 287-338:
roleId = 1 → session.put("SuperAdmin", 1)
roleId = 2 → session.put("Admin", 2)  
roleId = 3 → session.put("role", 3)
roleId = 4 → session.put("Supervisor", 4)
roleId = 5 → session.put("DepartmentHead", 5)
roleId = 6 → session.put("Procurement", 6)
roleId = 7 → session.put("PlantManager", 7)
roleId = 8 → session.put("FloorIncharge", 8)
roleId = 9 → session.put("DataEntryOperator", 9)
roleId = 10 → session.put("GoodsIncharge", 10)
roleId = 11 → session.put("GRNIncharge", 11)
roleId = 12 → session.put("IssueConfirm", 12)
roleId = 13 → session.put("ReceiptConfirm", 13)
roleId = 14 → session.put("QualityManager", 14)

Authentication Flow:

Email Domain Check: If email contains "@" → LDAP authentication
Domain Validation: @nslindia.com → AD LDAP, others → OpenLDAP
Local Authentication: Non-email usernames → Local MD5 check
Role Assignment: Based on TblMapEmpRoles mapping (NOT email patterns)

## Default Credentials

| Source Script                     | Username                         | Password        | Hash (MD5)                         | Notes                            |
| --------------------------------- | -------------------------------- | --------------- | ---------------------------------- | -------------------------------- |
| `SEED_BASELINE_DATA.sql`          | `admin@example.com`              | `admin123`      | `0192023a7bbd73250516f069df18b500` | Minimal smoke-test administrator |
| `LOGIN_CREDENTIALS_CORRECTED.sql` | `admin.pz@nslindia.com`          | `admin123`      | `0192023a7bbd73250516f069df18b500` | Super Administrator (role ID 1)  |
| `LOGIN_CREDENTIALS_CORRECTED.sql` | `john.plant@nslindia.com`        | `manager123`    | `3fc0a7acf087f549ac2b266baf94b8b1` | Plant Manager (role ID 7)        |
| `LOGIN_CREDENTIALS_CORRECTED.sql` | `alice.procurement@nslindia.com` | `user123`       | `32250170a0dca92d53ec9624f336ca24` | Procurement Officer (role ID 6)  |
| `LOGIN_CREDENTIALS_CORRECTED.sql` | `bob.quality@nslindia.com`       | `user123`       | `32250170a0dca92d53ec9624f336ca24` | Quality Manager (role ID 14)     |
| `LOGIN_CREDENTIALS_CORRECTED.sql` | `carol.finance@nslindia.com`     | `user123`       | `32250170a0dca92d53ec9624f336ca24` | Department Head (role ID 5)      |
| `EXTRA_DUMMY_DATA.sql`            | `stores.manager@nslindia.com`    | `stores123`     | `6d46f135ab453d2878478976efcdd8d1` | Goods Incharge (role ID 10)      |
| `EXTRA_DUMMY_DATA.sql`            | `qa.lead@nslindia.com`           | `qaLead2024`    | `6a05985e658be7f2e338e3ba99116c3f` | Quality Manager (role ID 14)     |
| `EXTRA_DUMMY_DATA.sql`            | `warehouse.ops@nslindia.com`     | `warehouse2024` | `d7edb4897f4e289432114fdb6e60e8c5` | Issue Confirm (role ID 12)       |

> 🔐 All hashes intentionally remain MD5 to stay compatible with the legacy `Utils#getEncript` method. Upgrade plans should introduce bcrypt together with code changes.
