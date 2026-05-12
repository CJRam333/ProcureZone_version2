# PROCUREZONE COMPREHENSIVE API TEST PLAN

## Executive Summary

-   **Total API Endpoints in Collection:** 90
-   **Total Controllers in Backend:** 29
-   **Total API Modules:** 19 Postman folders

---

## 📋 TESTING TASK DIVISION

### PHASE 1: CORE CRUD OPERATIONS (Priority: HIGH)

**Owner:** QA Team Lead  
**Estimated Time:** 2-3 hours

| Module         | Endpoints | Tests Required                             |
| -------------- | --------- | ------------------------------------------ |
| Authentication | 5         | Login, Logout, Token Validation            |
| Users          | 8         | CRUD, Password Change, Lock/Unlock         |
| Employees      | 12        | CRUD, Role Assignment, Activate/Deactivate |
| Vendors        | 5         | List, Search, Get by ID                    |
| Materials      | 4         | List, Search, Get Active                   |

### PHASE 2: BUSINESS LOGIC TESTING (Priority: HIGH)

**Owner:** Senior QA  
**Estimated Time:** 3-4 hours

| Module          | Endpoints | Tests Required                        |
| --------------- | --------- | ------------------------------------- |
| Indents         | 5         | CRUD, Status Workflows                |
| Approvals       | 8         | Approve/Reject/Request-Info Workflows |
| Purchase Orders | 6         | PO Creation, Status Tracking          |
| GRN             | 2         | Goods Receipt Processing              |
| Issue Notes     | 2         | Material Issue Processing             |
| Inventory       | 5         | Stock Levels, Transactions            |

### PHASE 3: MASTER DATA TESTING (Priority: MEDIUM)

**Owner:** Junior QA  
**Estimated Time:** 1-2 hours

| Module      | Endpoints | Tests Required            |
| ----------- | --------- | ------------------------- |
| Companies   | 5         | List, Search, Active Only |
| Departments | 3         | CRUD, Hierarchies         |
| Plants      | 3         | List, Get by ID           |
| Locations   | 1         | List All                  |
| UOMs        | 1         | List All                  |

### PHASE 4: DASHBOARD & REPORTS (Priority: MEDIUM)

**Owner:** QA Analyst  
**Estimated Time:** 2 hours

| Module    | Endpoints | Tests Required                |
| --------- | --------- | ----------------------------- |
| Dashboard | 12        | All Statistics & Summary APIs |
| Reports   | 2         | Material & Low Stock Reports  |

### PHASE 5: ADDITIONAL CONTROLLERS (NOT IN POSTMAN)

**Owner:** QA Lead  
**Estimated Time:** 2 hours

These controllers exist in backend but not in Postman collection:

| Controller              | Path                                     | Status        |
| ----------------------- | ---------------------------------------- | ------------- |
| CompanyLocationMaterial | `/api/mapping/company-location-material` | ⚠️ NOT TESTED |
| CompanyPlantMaterial    | `/api/mapping/company-plant-material`    | ⚠️ NOT TESTED |
| CompanyDepartment       | `/api/v1/company-departments`            | ⚠️ NOT TESTED |
| CompanyLocation         | `/api/v1/company-locations`              | ⚠️ NOT TESTED |
| CompanyEmployee         | `/api/v1/company-employees`              | ⚠️ NOT TESTED |
| EmployeeReporting       | `/api/v1/employee-reporting`             | ⚠️ NOT TESTED |
| EmployeeRole            | `/api/v1/employee-roles`                 | ⚠️ NOT TESTED |
| BulkImport              | `/api/v1/bulk-import`                    | ⚠️ NOT TESTED |
| SapMaterialImport       | `/api/v1/sap/materials`                  | ⚠️ NOT TESTED |
| PdfReport               | `/api/v1/reports/pdf`                    | ⚠️ NOT TESTED |

---

## 🧪 EDGE CASE TEST MATRIX

### Authentication Edge Cases

```
✅ AUTH-01: Valid login with correct credentials
✅ AUTH-02: Invalid password (should return 401)
✅ AUTH-03: Non-existent user (should return 401)
✅ AUTH-04: Empty payload (should return 400)
✅ AUTH-05: Get current user (requires valid token)
⚠️ EDGE-AUTH-01: Expired token access
⚠️ EDGE-AUTH-02: Malformed JWT token
⚠️ EDGE-AUTH-03: SQL injection in username
⚠️ EDGE-AUTH-04: XSS in login payload
⚠️ EDGE-AUTH-05: Rate limiting (brute force)
```

### User Management Edge Cases

```
✅ USER-01: List all users
✅ USER-02: Get user by valid ID
✅ USER-03: Create user with valid data
✅ USER-04: Update user profile
✅ USER-05: Change password
✅ USER-06: Reset password (admin)
✅ USER-07: Lock user account
✅ USER-08: Unlock user account
⚠️ EDGE-USER-01: Create duplicate username
⚠️ EDGE-USER-02: Create user with invalid email
⚠️ EDGE-USER-03: Update non-existent user
⚠️ EDGE-USER-04: Password too short (<8 chars)
⚠️ EDGE-USER-05: Lock already locked account
⚠️ EDGE-USER-06: SQL injection in username
```

### Employee Edge Cases

```
✅ EMP-01: List all employees
✅ EMP-02: Get employee by ID
✅ EMP-03: Get non-existent employee (404 expected)
✅ EMP-04: Search employees
⚠️ EDGE-EMP-01: Search with empty query
⚠️ EDGE-EMP-02: Search with special characters
⚠️ EDGE-EMP-03: Create duplicate employee number
⚠️ EDGE-EMP-04: Assign invalid role
⚠️ EDGE-EMP-05: Deactivate already inactive
```

### Indent Edge Cases

```
✅ IND-01: List all indents
✅ IND-02: Get indent by ID
✅ IND-03: Get by status
✅ IND-04: Search indents
✅ IND-05: Get non-existent indent
⚠️ EDGE-IND-01: Create indent with invalid material
⚠️ EDGE-IND-02: Create indent with negative quantity
⚠️ EDGE-IND-03: Create indent with past date
⚠️ EDGE-IND-04: Update approved indent
⚠️ EDGE-IND-05: Delete indent with PO
```

### Approval Edge Cases

```
✅ APR-01: Get pending approvals
✅ APR-02: Get pending for me
✅ APR-03: Get department indents
✅ APR-04: Get approval workflow
✅ APR-05: Approve indent
✅ APR-06: Reject indent
✅ APR-07: Request info
✅ APR-08: Approve non-existent (404 expected)
⚠️ EDGE-APR-01: Approve already approved
⚠️ EDGE-APR-02: Approve without permission
⚠️ EDGE-APR-03: Reject without remarks
⚠️ EDGE-APR-04: Self-approve indent
```

### Purchase Order Edge Cases

```
✅ PO-01: List all POs
✅ PO-02: Get PO by ID
✅ PO-03: Get approved indents for PO
✅ PO-04: Get pending approval POs
✅ PO-05: Get overdue POs
✅ PO-06: Dashboard statistics
⚠️ EDGE-PO-01: Create PO with invalid vendor
⚠️ EDGE-PO-02: Create PO with 0 quantity
⚠️ EDGE-PO-03: Update completed PO
⚠️ EDGE-PO-04: Delete PO with GRN
```

### Inventory Edge Cases

```
✅ INV-01: List all inventory
✅ INV-02: Get low stock items
✅ INV-03: Get critical stock items
✅ INV-04: Inventory statistics
✅ INV-05: Transaction history
⚠️ EDGE-INV-01: Negative stock adjustment
⚠️ EDGE-INV-02: Issue more than available
⚠️ EDGE-INV-03: Stock for invalid material
```

---

## 📊 API ENDPOINT TO CONTROLLER MAPPING

| Postman Folder     | Controller               | Base Path                  | Status     |
| ------------------ | ------------------------ | -------------------------- | ---------- |
| 01-Authentication  | AuthController           | `/api/v1/auth`             | ✅ MATCHED |
| 02-Users           | UserController           | `/api/v1/users`            | ✅ MATCHED |
| 03-Employees       | EmployeeController       | `/api/v1/employees`        | ✅ MATCHED |
| 04-Approvals       | ApprovalController       | `/api/v1/approvals`        | ✅ MATCHED |
| 05-Companies       | CompanyController        | `/api/v1/companies`        | ✅ MATCHED |
| 06-Materials       | MaterialController       | `/api/v1/materials`        | ✅ MATCHED |
| 07-Departments     | DepartmentController     | `/api/v1/departments`      | ✅ MATCHED |
| 09-Plants          | PlantController          | `/api/v1/plants`           | ✅ MATCHED |
| 10-Locations       | LocationController       | `/api/v1/locations`        | ✅ MATCHED |
| 11-UOMs            | UnitOfMeasureController  | `/api/v1/unit-of-measures` | ✅ MATCHED |
| 12-Vendors         | VendorController         | `/api/v1/vendors`          | ✅ MATCHED |
| 13-Indents         | IndentController         | `/api/v1/indents`          | ✅ MATCHED |
| 14-Purchase-Orders | POController             | `/api/v1/pos`              | ✅ MATCHED |
| 15-GRN             | GRNController            | `/api/v1/grn`              | ✅ MATCHED |
| 16-Issue-Notes     | IssueNoteController      | `/api/v1/issue-notes`      | ✅ MATCHED |
| 17-Inventory       | InventoryController      | `/api/v1/inventory`        | ✅ MATCHED |
| 18-Dashboard       | DashboardController      | `/api/v1/dashboard`        | ✅ MATCHED |
| 19-Reports         | MaterialReportController | `/api/v1/reports`          | ✅ MATCHED |

---

## 🚀 TEST EXECUTION COMMANDS

### Run All Tests

```bash
newman run ProcureZone-API-Collection-V2.json -e env-newman.json --reporters cli,json --reporter-json-export results.json
```

### Run Specific Folder

```bash
newman run ProcureZone-API-Collection-V2.json -e env-newman.json --folder "01-Authentication"
```

### Run with Detailed Output

```bash
newman run ProcureZone-API-Collection-V2.json -e env-newman.json --reporters cli --verbose
```

---

## ✅ ACCEPTANCE CRITERIA

1. **All 90 API endpoints tested** - 100% coverage
2. **All CRUD operations validated** - Create, Read, Update, Delete
3. **Edge cases verified** - Error handling, validation
4. **Authentication flows tested** - Login, token, logout
5. **Role-based access tested** - Admin vs User permissions
6. **Response format validated** - JSON structure, status codes
7. **Performance baseline established** - Response times < 2s

---

## 📝 NEXT STEPS

1. Run complete Newman test suite
2. Document all failures
3. Test missing controllers (10 endpoints)
4. Run edge case tests
5. Generate final report
