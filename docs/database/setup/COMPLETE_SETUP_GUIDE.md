# Complete Database Setup Guide

**Last Updated:** October 11, 2025  
**Database:** seeds_indent  
**Time Required:** 10-15 minutes

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Automated Setup](#automated-setup)
3. [Manual Setup](#manual-setup)
4. [Verification](#verification)
5. [Troubleshooting](#troubleshooting)
6. [What's Included](#whats-included)

---

## Prerequisites

### Required Software

- **MySQL 8.0+** installed and running
- **Java 21** (for backend testing)
- **Maven 3.6+** (for backend)
- Terminal/Command Prompt access

### Required Information

- MySQL root password
- Database name: `seeds_indent`

### Check Prerequisites

```bash
# Check MySQL version
mysql --version

# Check MySQL is running
mysql -u root -p -e "SELECT VERSION();"

# Check Java version
java -version

# Check Maven version
mvn -version
```

---

## Automated Setup

### Option 1: Using Bash Script (Recommended)

**For Linux/Mac/Git Bash on Windows:**

```bash
cd /e/Net-Beans/ProcureZone/database
./setup_complete_database.sh
```

**Script will prompt for:**

- MySQL root password

**Script will execute:**

1. Drop existing `seeds_indent` database (if exists)
2. Create fresh `seeds_indent` database
3. Create all 41 tables
4. Seed master data (companies, departments, locations, materials, roles)
5. Create 25 realistic employees
6. Create 25 user accounts (all password: password123)
7. Set up role mappings and relationships
8. Add business transaction data (indents, workflows)

**Expected output:**

```
🚀 ProcureZone Database Complete Setup
========================================

⏳ Step 1/6: Creating database...
✅ Success!

⏳ Step 2/6: Creating schema (41 tables)...
✅ Success!

⏳ Step 3/6: Seeding master data...
✅ Success!

⏳ Step 4/6: Creating employees and users (25 realistic users)...
✅ Success!

⏳ Step 5/6: Creating role mappings and relationships...
✅ Success!

⏳ Step 6/6: Seeding business transaction data...
✅ Success!

🎉 Database Setup Completed Successfully!
```

### Option 2: Using Windows Batch Script

**For Windows Command Prompt:**

```cmd
cd e:\Net-Beans\ProcureZone\database
setup_complete_database.bat
```

Same functionality as bash script but for Windows.

---

## Manual Setup

If you prefer manual control or automated script fails:

### Step 1: Create Database

```bash
cd /e/Net-Beans/ProcureZone/database
mysql -u root -p < 01_create_database.sql
```

**What it does:**

- Drops `seeds_indent` if exists
- Creates fresh `seeds_indent` database
- Sets character set to utf8mb4

### Step 2: Create Schema

```bash
mysql -u root -p seeds_indent < 02_create_schema.sql
```

**What it does:**

- Creates all 41 tables
- Sets up foreign key relationships
- Creates indexes

**Tables created:**

- tbl_company_master
- tbl_department_master
- tbl_location_master
- tbl_plant_master
- tbl_material_master
- tbl_roles_master
- tbl_emp_master
- tbl_user_master
- tbl_map_emp_roles
- tbl_indent_master
- tbl_indent_details
- ... and 30 more tables

### Step 3: Seed Master Data

```bash
mysql -u root -p seeds_indent < 03_seed_master_data.sql
```

**What it does:**

- Inserts 3 companies (Syngenta, AgroTech, Green Seeds)
- Inserts 10 departments (Production, Procurement, QC, etc.)
- Inserts 6 locations (Mumbai, Delhi, Bangalore, etc.)
- Inserts 4 plants
- Inserts 12 materials (Seeds, Fertilizers, Pesticides)
- Inserts 10 roles (Super Admin to Employee)
- Inserts 6 crop types
- Inserts 7 units of measure
- Inserts 8 indent status types

### Step 4: Create Employees & Users

```bash
mysql -u root -p seeds_indent < 04_seed_employees_users.sql
```

**What it does:**

- Creates 25 employees with realistic Indian names
- Creates 25 user accounts (one per employee)
- All passwords set to: `password123` (MD5 hashed)
- All users active and ready

**Sample employees:**

- Rajesh Kumar (CTO)
- Priya Sharma (IT Manager)
- Suresh Reddy (Plant Manager)
- Neha Gupta (Procurement Officer)
- ... and 21 more

### Step 5: Create Mappings & Relationships

```bash
mysql -u root -p seeds_indent < 05_seed_mappings.sql
```

**What it does:**

- Maps 27 employee-role assignments
- Maps 17 company-department relationships
- Maps 9 company-location relationships
- Maps 25 company-employee assignments
- Creates 24 employee reporting hierarchy entries
- Sets up material authorization mappings

### Step 6: Seed Business Data

```bash
mysql -u root -p seeds_indent < 06_seed_business_data.sql
```

**What it does:**

- Creates 10 indents (purchase requisitions)
  - 2 completed (from 2024)
  - 5 in various approval stages
  - 2 recently submitted
  - 1 draft
- Creates 19 indent line items
- Creates 12 procurement workflow log entries

---

## Verification

### Check Database Created

```bash
mysql -u root -p -e "SHOW DATABASES LIKE 'seeds_indent';"
```

### Check Table Count

```bash
mysql -u root -p seeds_indent -e "SELECT COUNT(*) AS table_count FROM information_schema.tables WHERE table_schema = 'seeds_indent';"
```

**Expected:** 41 tables

### Check User Count

```bash
mysql -u root -p seeds_indent -e "SELECT COUNT(*) AS user_count FROM tbl_user_master;"
```

**Expected:** 25 users

### Verify All Users

```bash
cd /e/Net-Beans/ProcureZone/database
mysql -u root -p seeds_indent < verify_users.sql
```

### Check Sample User

```bash
mysql -u root -p seeds_indent -e "SELECT user_id, user_name, emp_number, user_status FROM tbl_user_master WHERE user_name = 'rajesh.kumar';"
```

**Expected output:**

```
+---------+--------------+------------+-------------+
| user_id | user_name    | emp_number | user_status |
+---------+--------------+------------+-------------+
|       1 | rajesh.kumar |          1 |           1 |
+---------+--------------+------------+-------------+
```

### Verify Password

```bash
mysql -u root -p seeds_indent -e "SELECT user_name, CASE WHEN user_password = '482c811da5d5b4bc6d497ffa98491e38' THEN '✓ password123' ELSE '✗ Different' END AS password FROM tbl_user_master WHERE user_name = 'rajesh.kumar';"
```

---

## Test Authentication

### 1. Configure Backend

Check `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/seeds_indent
    username: root
    password: password # Your MySQL password
```

### 2. Start Backend

```bash
cd /e/Net-Beans/ProcureZone/backend
mvn clean install
mvn spring-boot:run
```

**Wait for:**

```
Started ProcurezoneBackendApplication in X seconds
```

### 3. Test Login with cURL

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rajesh.kumar",
    "password": "password123"
  }'
```

### 4. Expected Response

```json
{
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresAt": "2025-10-11T01:49:34Z",
  "authenticatedUser": {
    "userId": 1,
    "employeeNumber": 1,
    "employeeId": 1,
    "fullName": "Rajesh Kumar",
    "email": null,
    "roles": ["SUPER_ADMIN"]
  }
}
```

### 5. Test with Other Users

```bash
# IT Manager
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"priya.sharma","password":"password123"}'

# Plant Manager
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"suresh.reddy","password":"password123"}'

# Procurement Officer
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"neha.gupta","password":"password123"}'
```

---

## Troubleshooting

### Database Connection Failed

**Symptom:** Script hangs or shows "Access denied"

**Solutions:**

```bash
# 1. Check MySQL is running
mysql -u root -p -e "SELECT 1;"

# 2. Verify credentials
mysql -u root -p

# 3. Check MySQL service
# Linux:
sudo systemctl status mysql
# Windows:
net start MySQL80
```

### Table Already Exists

**Symptom:** "Table 'xxx' already exists"

**Solution:**

```bash
# Drop and recreate
mysql -u root -p -e "DROP DATABASE IF EXISTS seeds_indent;"
./setup_complete_database.sh
```

### Login Returns "Invalid Credentials"

**Symptom:** Authentication fails in backend

**Causes:**

1. Password is case-sensitive: Use exactly `password123`
2. Username is case-sensitive: Use lowercase (e.g., `rajesh.kumar`)
3. Backend not connected to correct database

**Solutions:**

- See [Login Fix Documentation](../troubleshooting/LOGIN_FIX_APPLIED.md)
- Verify database in application.yml
- Check user exists: `SELECT * FROM tbl_user_master WHERE user_name = 'rajesh.kumar';`

### Backend Won't Start

**Symptom:** Maven build fails or application won't start

**Solutions:**

```bash
# Clean and rebuild
cd /e/Net-Beans/ProcureZone/backend
mvn clean
mvn install -DskipTests
mvn spring-boot:run

# Check Java version
java -version  # Should be Java 21

# Check port 8080 not in use
# Linux/Mac:
lsof -i :8080
# Windows:
netstat -ano | findstr :8080
```

### Foreign Key Constraint Fails

**Symptom:** Insert fails with "Cannot add or update a child row"

**Solution:**

```bash
# Run files in correct order:
01_create_database.sql
02_create_schema.sql
03_seed_master_data.sql  # Must run BEFORE employees
04_seed_employees_users.sql
05_seed_mappings.sql
06_seed_business_data.sql
```

---

## What's Included

### Companies (3)

1. Syngenta Seeds India Ltd
2. AgroTech Solutions Pvt Ltd
3. Green Seeds Corporation

### Departments (10)

- Production
- Procurement
- Quality Control
- Packing
- HR
- Finance
- Sales
- IT
- Admin
- Logistics

### Locations (6)

- Mumbai
- Delhi
- Bangalore
- Hyderabad
- Chennai
- Pune

### Materials (12)

- Seeds: Corn, Wheat, Rice, Cotton
- Fertilizers: NPK, Urea, Potash
- Pesticides, Lab Equipment
- Packaging: Bags, Cartons
- Machinery: Water Pumps, Sprayers

### Roles (10)

1. Super Administrator
2. Administrator
3. Department Head
4. Plant Manager
5. Procurement Officer
6. Finance Manager
7. Store Manager
8. Quality Inspector
9. Employee
10. Auditor

### Users (25)

- All have realistic Indian names
- All passwords: `password123`
- All active (user_status = 1)
- Proper role assignments
- Complete reporting hierarchy

**Full list:** [All Users](../reference/ALL_USERS_LIST.md)

### Indents (10)

- 2 completed (2024)
- 3 in procurement
- 2 approved (awaiting procurement)
- 1 department approved
- 1 submitted
- 1 draft

---

## Database Statistics

| Entity                | Count |
| --------------------- | ----- |
| Total Tables          | 41    |
| Companies             | 3     |
| Departments           | 10    |
| Locations             | 6     |
| Plants                | 4     |
| Materials             | 12    |
| Roles                 | 10    |
| Employees             | 25    |
| Users                 | 25    |
| Role Mappings         | 27    |
| Company-Dept Maps     | 17    |
| Company-Location Maps | 9     |
| Company-Employee Maps | 25    |
| Reporting Hierarchy   | 24    |
| Indents               | 10    |
| Indent Details        | 19    |
| Procurement Logs      | 12    |

---

## Next Steps

1. ✅ Database setup complete
2. ✅ Users verified
3. ⏩ Start backend server
4. ⏩ Test authentication
5. ⏩ Begin frontend integration

---

## Additional Resources

- **[Quick Start](QUICK_START.md)** - 5-minute setup
- **[Setup Summary](SETUP_SUCCESS_SUMMARY.md)** - Latest results
- **[All Users](../reference/ALL_USERS_LIST.md)** - Complete credentials
- **[Schema Docs](../reference/DATABASE_SCHEMA_DOCUMENTATION.md)** - Table structures
- **[Troubleshooting](../troubleshooting/)** - Common issues

---

**Questions?** Check the [main README](../../README.md) or troubleshooting guides.
