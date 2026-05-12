# Database Setup Strategy Guide

## 🎯 RECOMMENDED APPROACH

### ✅ **Keep Full Schema + Add Feature-Specific Test Data**

## Why This Approach?

### 1. **Referential Integrity Protection**

```
❌ Incremental:
   Feature A creates users → Feature B needs materials → FK fails

✅ Full Schema:
   All tables exist → Any feature can reference any table → No FK errors
```

### 2. **Real Production Testing**

- Tests run against actual schema
- Catch issues early (column names, constraints, types)
- No surprises in deployment

### 3. **Feature Independence**

- Team members work on different features simultaneously
- No schema conflicts
- Each feature adds only its test data

### 4. **Migration Safety**

- Flyway tracks all changes
- Easy rollback
- Clear audit trail

---

## 📋 SETUP INSTRUCTIONS

### Step 1: Create Fresh Database with Full Schema

```sql
-- 1. Drop existing database (CAUTION: Deletes all data!)
DROP DATABASE IF EXISTS seeds_indent;

-- 2. Create fresh database
CREATE DATABASE seeds_indent;

-- 3. Import full schema
USE seeds_indent;
SOURCE /path/to/DEFINITIVE_PROCUREZONE_SCHEMA.sql;
```

**Recommended**: Keep a backup first!

```bash
mysqldump -u root -p seeds_indent > backup_$(date +%Y%m%d).sql
```

---

### Step 2: Add Minimal Test Data for Authentication

```sql
USE seeds_indent;

-- Insert supporting master data
INSERT INTO tbl_company_master (comp_id, comp_code, comp_name, comp_status, comp_lmd, comp_lmu)
VALUES (1, 'NSL', 'NSL India', 1, CURRENT_DATE(), 0);

INSERT INTO tbl_department_master (dept_id, dept_code, dept_name, dept_status, dept_lmd, dept_lmu)
VALUES (1, 'IT', 'Information Technology', 1, CURRENT_DATE(), 0);

INSERT INTO tbl_location_master (loc_id, loc_code, loc_name, loc_status, loc_lmd, loc_lmu)
VALUES (1, 'HQ', 'Head Office', 1, CURRENT_DATE(), 0);

-- Insert role
INSERT INTO tbl_roles_master (role_id, role_code, role_name, role_status, role_lmd, role_lmu)
VALUES (1, 'ADMIN', 'Administrator', 1, CURRENT_DATE(), 0);

-- Insert test employee
INSERT INTO tbl_emp_master (
    emp_number, emp_id, emp_name, emp_email, emp_password,
    emp_designation, emp_status, emp_lmd,
    emp_department, emp_location, emp_company
) VALUES (
    1, 'E-1001', 'Test Admin', 'admin@nsl.com',
    '482c811da5d5b4bc6d497ffa98491e38', -- MD5: password123
    'System Administrator', 1, CURRENT_DATE(), 1, 1, 1
);

-- Insert user account
INSERT INTO tbl_user_master (
    user_id, user_name, user_password, user_status,
    user_lmd, emp_number, user_lmu
) VALUES (
    1, 'admin', '482c811da5d5b4bc6d497ffa98491e38',
    1, CURRENT_DATE(), 1, 0
);

-- Map employee to role
INSERT INTO tbl_map_emp_roles (
    emp_roles_id, emp_number, role_id,
    emp_roles_status, emp_roles_lmd, emp_roles_lmu
) VALUES (1, 1, 1, 1, CURRENT_DATE(), 0);
```

---

### Step 3: Verify Setup

```sql
-- Check all tables exist
SELECT COUNT(*) as table_count
FROM information_schema.tables
WHERE table_schema = 'seeds_indent';
-- Expected: ~30+ tables

-- Check test data
SELECT
    u.user_name,
    e.emp_name,
    e.emp_email,
    r.role_code
FROM tbl_user_master u
JOIN tbl_emp_master e ON u.emp_number = e.emp_number
JOIN tbl_map_emp_roles mer ON e.emp_number = mer.emp_number
JOIN tbl_roles_master r ON mer.role_id = r.role_id
WHERE u.user_status = 1;
-- Expected: 1 row (admin user)
```

---

## 📦 Per-Feature Data Strategy

### Authentication Feature (✅ DONE)

**Tables needed:**

- tbl_company_master (1 record)
- tbl_department_master (1 record)
- tbl_location_master (1 record)
- tbl_roles_master (1-3 records)
- tbl_emp_master (1-3 test users)
- tbl_user_master (1-3 accounts)
- tbl_map_emp_roles (1-3 mappings)

### Material Master Feature (Future)

**Additional data needed:**

- tbl_umo_master (5-10 units)
- tbl_material_master (10-20 materials)
- tbl_map_company_location_material (relationships)

**Don't add until needed!** Keep test data minimal.

### Indent Feature (Future)

**Additional data needed:**

- Materials (from above)
- tbl_indent_master (2-3 indents)
- tbl_indent_details (5-10 line items)
- tbl_indent_status (workflow states)

---

## 🔄 Daily Workflow

### For Each New Feature:

1. **Schema exists** ✅ (already done)
2. **Add minimal test data** for that feature only
3. **Write tests** using that data
4. **Document** what data the feature needs
5. **Move to next feature**

### Example: Starting "Material Management"

```sql
-- Add only what you need for material tests
INSERT INTO tbl_umo_master (umo_id, umo_code, umo_name, umo_status, umo_lmd, umo_lmu)
VALUES
    (1, 'KG', 'Kilogram', 1, CURRENT_DATE(), 0),
    (2, 'LTR', 'Liter', 1, CURRENT_DATE(), 0),
    (3, 'PCS', 'Pieces', 1, CURRENT_DATE(), 0);

INSERT INTO tbl_material_master (mat_id, mat_code, mat_name, mat_umo, mat_status, mat_lmd, mat_lmu)
VALUES
    (1, 'MAT-001', 'Test Material A', 1, 1, CURRENT_DATE(), 0),
    (2, 'MAT-002', 'Test Material B', 2, 1, CURRENT_DATE(), 0);
```

**Don't add 1000s of materials!** Add 5-10 for testing, that's enough.

---

## ⚠️ ANTI-PATTERNS TO AVOID

### ❌ DON'T: Build Schema Incrementally

```
Week 1: Create users table
Week 2: Oh, we need roles... add that
Week 3: Wait, employees need departments... add that
Week 4: Foreign keys fail... fix dependencies
```

**Result**: Constant rework, broken tests, wasted time

### ❌ DON'T: Add All Production Data

```
INSERT 10,000 materials...
INSERT 5,000 employees...
INSERT 20,000 indent records...
```

**Result**: Slow tests, hard to debug, data conflicts

### ✅ DO: Full Schema + Minimal Data

```
Schema: All tables (5 minutes to import)
Data: 5-10 records per feature (easy to understand)
Tests: Fast, predictable, reliable
```

---

## 🛠️ Fixing Your Current Errors

### Error: `Field 'emp_designation' doesn't have a default value`

**Fix**: Include all required fields in INSERT

```sql
INSERT INTO tbl_emp_master (
    ...,
    emp_designation,  -- ADD THIS
    emp_department,   -- ADD THIS
    emp_location,     -- ADD THIS
    emp_company       -- ADD THIS
)
```

### Error: `Duplicate entry 'ADMIN' for key 'uk_role_code'`

**Fix**: Use INSERT IGNORE or check if exists first

```sql
INSERT IGNORE INTO tbl_roles_master ...
-- OR
INSERT INTO tbl_roles_master ... ON DUPLICATE KEY UPDATE role_name=role_name;
```

### Error: `Cannot add... foreign key constraint fails`

**Fix**: Insert parent records first

```sql
-- Order matters!
1. INSERT INTO tbl_company_master
2. INSERT INTO tbl_department_master
3. INSERT INTO tbl_location_master
4. INSERT INTO tbl_roles_master
5. INSERT INTO tbl_emp_master      -- References 1,2,3
6. INSERT INTO tbl_user_master     -- References 5
7. INSERT INTO tbl_map_emp_roles   -- References 4,5
```

---

## 📊 Data Volume Guidelines

| Feature        | Master Data                                         | Transactional Data       |
| -------------- | --------------------------------------------------- | ------------------------ |
| Authentication | 1 company, 1 dept, 1 location, 3 roles, 3 employees | 3 user accounts          |
| Materials      | 5 units, 10 material types                          | 10 materials             |
| Indents        | (reuse above)                                       | 3 indents, 10 line items |
| Approvals      | (reuse above)                                       | 5 workflow states        |

**Rule of Thumb**: Keep test data under 100 total records per feature.

---

## 🚀 Quick Start Commands

### Fresh Setup (CAUTION: Destroys existing data!)

```bash
# Backup first!
mysqldump -u root -p seeds_indent > backup.sql

# Fresh setup
mysql -u root -p << EOF
DROP DATABASE IF EXISTS seeds_indent;
CREATE DATABASE seeds_indent;
USE seeds_indent;
SOURCE /path/to/DEFINITIVE_PROCUREZONE_SCHEMA.sql;
SOURCE /path/to/minimal_test_data.sql;
EOF

# Verify
mysql -u root -p seeds_indent -e "
SELECT COUNT(*) FROM tbl_user_master;
SELECT COUNT(*) FROM tbl_emp_master;
"
```

### Keep Existing Schema, Add Test Data

```bash
mysql -u root -p seeds_indent < minimal_test_data.sql
```

---

## 📝 Checklist Before Starting New Feature

- [ ] Full schema imported
- [ ] Authentication test data working
- [ ] Can login via Postman
- [ ] Integration tests passing
- [ ] Documented what data feature needs
- [ ] Created feature-specific data script
- [ ] Updated test fixtures (H2 data.sql)

---

## 🎯 SUMMARY

**Schema Strategy**: ✅ Full schema from day one  
**Data Strategy**: ✅ Minimal data per feature  
**Migration Strategy**: ✅ Flyway tracks changes  
**Testing Strategy**: ✅ H2 mirrors production schema

**Result**: Fast development, reliable tests, easy maintenance! 🎉
