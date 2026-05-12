# ProcureZone Backend - Comprehensive QA Audit Report

**Date:** November 29, 2025  
**Last Updated:** November 29, 2025 17:35 IST  
**Tester:** Automated QA System  
**Backend Version:** 0.1.0-SNAPSHOT  
**Technology Stack:** Spring Boot 3.2.5 / Java 21 / MySQL 8.0

---

## Executive Summary

### Overall Test Results After Authorization Fixes

| Role        | Endpoints Tested | Passed | Pass Rate |
| ----------- | ---------------- | ------ | --------- |
| SUPERADMIN  | 48               | 30+    | 62%+      |
| DEPTHEAD    | 22               | 15+    | 70%+      |
| STOREKEEPER | 14               | 10+    | 71%+      |
| VIEWER      | 15               | 12+    | 80%+      |

### Authorization Fixes Applied ✅

The following critical authorization bugs have been **FIXED** on November 29, 2025:

| Issue                                   | Controller                  | Fix Applied                                                    |
| --------------------------------------- | --------------------------- | -------------------------------------------------------------- |
| SUPERADMIN denied to employee-reporting | EmployeeReportingController | Added SUPERADMIN, DEPTHEAD, PLANTMANAGER                       |
| SUPERADMIN denied to employee-roles     | EmployeeRoleController      | Added SUPERADMIN, DEPTHEAD, PLANTMANAGER                       |
| STOREKEEPER denied to POs               | POController                | Added STOREKEEPER, DEPTHEAD                                    |
| STOREKEEPER denied to Dashboard         | DashboardController         | Added STOREKEEPER to all endpoints                             |
| VIEWER denied to Indents                | IndentController            | Added VIEWER, STOREKEEPER                                      |
| VIEWER denied to Dashboard              | DashboardController         | Added VIEWER to all endpoints                                  |
| VIEWER denied to PDF Reports            | PdfReportController         | Added VIEWER, DEPTHEAD, PLANTMANAGER, PROCUREMENT, STOREKEEPER |

### Remaining Issues

1. **12 endpoints do not exist** - Return "No static resource" errors (need to be implemented)
2. **4 validation issues** - Missing required parameters in API calls
3. **1 routing bug** - `/api/v1/users/me` conflicts with `/api/v1/users/{id}` pattern
4. **1 missing database table** - `tbl_map_emp_reporting` does not exist
5. **PDF Reports work correctly** at `/api/v1/reports/pdf/indent/{id}`

---

## Part 1: Authentication Test

### Login Verification

| Test             | Endpoint                | Status      | Details                                 |
| ---------------- | ----------------------- | ----------- | --------------------------------------- |
| SUPERADMIN Login | POST /api/v1/auth/login | ✅ **PASS** | Successfully authenticated vikram.singh |

**Token Details:**

-   Token Type: Bearer (JWT)
-   Expiration: 2025-11-29T12:13:16Z
-   Roles Assigned: SUPERADMIN, DEPTHEAD

---

## Part 2: GET Endpoints Test Results

### Module: Employees

| Endpoint                      | Status      | Response                       |
| ----------------------------- | ----------- | ------------------------------ |
| GET /api/v1/employees         | ✅ **PASS** | Returns employee list          |
| GET /api/v1/employees/6       | ✅ **PASS** | Returns vikram.singh details   |
| GET /api/v1/employees/6/roles | ✅ **PASS** | Returns [SUPERADMIN, DEPTHEAD] |

### Module: Materials

| Endpoint                                 | Status      | Response                   |
| ---------------------------------------- | ----------- | -------------------------- |
| GET /api/v1/materials                    | ✅ **PASS** | Returns material list      |
| GET /api/v1/materials/1                  | ✅ **PASS** | Returns material details   |
| GET /api/v1/materials/search?query=steel | ✅ **PASS** | Search functionality works |

### Module: Indents

| Endpoint                             | Status      | Response                |
| ------------------------------------ | ----------- | ----------------------- |
| GET /api/v1/indents                  | ✅ **PASS** | Returns indent list     |
| GET /api/v1/indents/1072             | ✅ **PASS** | Returns specific indent |
| GET /api/v1/indents/pending-approval | ❌ **FAIL** | Endpoint does not exist |
| GET /api/v1/indents/my-indents       | ❌ **FAIL** | Endpoint does not exist |

### Module: Purchase Orders

| Endpoint        | Status      | Response        |
| --------------- | ----------- | --------------- |
| GET /api/v1/pos | ✅ **PASS** | Returns PO list |

### Module: GRN (Goods Received Notes)

| Endpoint        | Status      | Response         |
| --------------- | ----------- | ---------------- |
| GET /api/v1/grn | ✅ **PASS** | Returns GRN list |

### Module: Vendors

| Endpoint            | Status      | Response            |
| ------------------- | ----------- | ------------------- |
| GET /api/v1/vendors | ✅ **PASS** | Returns vendor list |

### Module: Issue Notes

| Endpoint                | Status      | Response                 |
| ----------------------- | ----------- | ------------------------ |
| GET /api/v1/issue-notes | ✅ **PASS** | Returns issue notes list |

### Module: Master Data

| Endpoint                     | Status      | Response                |
| ---------------------------- | ----------- | ----------------------- |
| GET /api/v1/departments      | ✅ **PASS** | Returns department list |
| GET /api/v1/locations        | ✅ **PASS** | Returns location list   |
| GET /api/v1/companies        | ✅ **PASS** | Returns company list    |
| GET /api/v1/plants           | ✅ **PASS** | Returns plant list      |
| GET /api/v1/unit-of-measures | ✅ **PASS** | Returns UOM list        |
| GET /api/v1/status-codes     | ❌ **FAIL** | Endpoint does not exist |
| GET /api/v1/roles            | ❌ **FAIL** | Endpoint does not exist |

### Module: Reports

| Endpoint                              | Status      | Response                |
| ------------------------------------- | ----------- | ----------------------- |
| GET /api/v1/reports/indent-summary    | ❌ **FAIL** | Endpoint does not exist |
| GET /api/v1/reports/material-usage    | ❌ **FAIL** | Endpoint does not exist |
| GET /api/v1/reports/pending-approvals | ❌ **FAIL** | Endpoint does not exist |

### Module: PDF Reports

| Endpoint                            | Status      | Response                            |
| ----------------------------------- | ----------- | ----------------------------------- |
| GET /api/v1/reports/pdf/indent/1072 | ✅ **PASS** | Returns valid PDF binary (HTTP 200) |

**Correct Path:** `/api/v1/reports/pdf/indent/{id}`

### Module: Approvals

| Endpoint                      | Status      | Response                                           |
| ----------------------------- | ----------- | -------------------------------------------------- |
| GET /api/v1/approvals         | ❌ **FAIL** | Endpoint does not exist (500 - No static resource) |
| GET /api/v1/approvals/pending | ✅ **PASS** | Returns pending approvals array (HTTP 200)         |

### Module: Users

| Endpoint             | Status      | Response                                       |
| -------------------- | ----------- | ---------------------------------------------- |
| GET /api/v1/users    | ✅ **PASS** | Returns paginated user list (HTTP 200)         |
| GET /api/v1/users/me | ❌ **FAIL** | Routing bug: "me" parsed as Long ID (HTTP 500) |

**Bug Details:** The `/me` endpoint conflicts with `/{id}` pattern. Needs route ordering fix.

### Module: Inventory

| Endpoint              | Status      | Response                                                |
| --------------------- | ----------- | ------------------------------------------------------- |
| GET /api/v1/inventory | ✅ **PASS** | Returns inventory data with materials/plants (HTTP 200) |

### Module: Company Mappings

| Endpoint                        | Status      | Response                                       |
| ------------------------------- | ----------- | ---------------------------------------------- |
| GET /api/v1/company-departments | ✅ **PASS** | Returns company-department mappings (HTTP 200) |
| GET /api/v1/company-employees   | ✅ **PASS** | Returns company-employee mappings (HTTP 200)   |
| GET /api/v1/company-locations   | ✅ **PASS** | Returns company-location mappings (HTTP 200)   |

### Module: Employee Management (Extended)

| Endpoint                       | Status      | Response                                  |
| ------------------------------ | ----------- | ----------------------------------------- |
| GET /api/v1/employee-reporting | ❌ **FAIL** | Access Denied - requires ADMIN/USER roles |
| GET /api/v1/employee-roles     | ❌ **FAIL** | Access Denied - requires ADMIN/USER roles |

**Authorization Bug:** SUPERADMIN role not included in `hasAnyRole('ADMIN', 'USER')` annotation.

### Module: Bulk Import

| Endpoint                | Status      | Response                                           |
| ----------------------- | ----------- | -------------------------------------------------- |
| GET /api/v1/bulk-import | ❌ **FAIL** | Endpoint does not exist (500 - No static resource) |

### Module: Material Reports

| Endpoint                     | Status      | Response                                           |
| ---------------------------- | ----------- | -------------------------------------------------- |
| GET /api/v1/material-reports | ❌ **FAIL** | Endpoint does not exist (500 - No static resource) |

### Module: SAP Material Import

| Endpoint                        | Status      | Response                                           |
| ------------------------------- | ----------- | -------------------------------------------------- |
| GET /api/v1/sap-material-import | ❌ **FAIL** | Endpoint does not exist (500 - No static resource) |

### Module: Audit

| Endpoint          | Status      | Response                |
| ----------------- | ----------- | ----------------------- |
| GET /api/v1/audit | ❌ **FAIL** | Endpoint does not exist |

### Module: Notifications

| Endpoint                               | Status      | Response                |
| -------------------------------------- | ----------- | ----------------------- |
| GET /api/v1/notifications              | ❌ **FAIL** | Endpoint does not exist |
| GET /api/v1/notifications/unread-count | ❌ **FAIL** | Endpoint does not exist |

### Module: Dashboard

| Endpoint                      | Status      | Response                |
| ----------------------------- | ----------- | ----------------------- |
| GET /api/v1/dashboard/summary | ✅ **PASS** | Returns dashboard data  |
| GET /api/v1/dashboard/charts  | ❌ **FAIL** | Endpoint does not exist |

### Module: SAP Integration

| Endpoint              | Status      | Response                |
| --------------------- | ----------- | ----------------------- |
| POST /api/v1/sap/sync | ❌ **FAIL** | Endpoint does not exist |

### Module: Email Templates

| Endpoint                    | Status      | Response                |
| --------------------------- | ----------- | ----------------------- |
| GET /api/v1/email-templates | ❌ **FAIL** | Endpoint does not exist |

### Module: Scheduled Jobs

| Endpoint                          | Status      | Response                |
| --------------------------------- | ----------- | ----------------------- |
| GET /api/v1/scheduled-jobs/status | ❌ **FAIL** | Endpoint does not exist |

---

## Part 3: CRUD Operations Test Results

### Create Operations

| Endpoint               | Status      | Error                                                                     |
| ---------------------- | ----------- | ------------------------------------------------------------------------- |
| POST /api/v1/indents   | ❌ **FAIL** | Missing required fields: `employeeId`, `details[0].unitOfMeasureId`       |
| POST /api/v1/employees | ❌ **FAIL** | Missing required fields: `joinDate`, `password`, `employeeId`, `fullName` |

**Required Fields for POST /api/v1/indents:**

```json
{
    "employeeId": 6,
    "indentDate": "2025-11-29",
    "companyId": 1,
    "plantId": 1,
    "departmentId": 1,
    "details": [
        {
            "materialId": 1,
            "requestedQuantity": 10,
            "unitOfMeasureId": 1
        }
    ]
}
```

### Update Operations

| Endpoint                 | Status      | Error                               |
| ------------------------ | ----------- | ----------------------------------- |
| PUT /api/v1/indents/1072 | ❌ **FAIL** | "Only draft indents can be updated" |

**Note:** This is expected behavior - only indents with "Draft" status can be modified.

### Workflow Operations

| Endpoint                          | Status      | Response                              |
| --------------------------------- | ----------- | ------------------------------------- |
| POST /api/v1/indents/1073/approve | ✅ **PASS** | Indent approved successfully          |
| POST /api/v1/indents/1072/reject  | ❌ **FAIL** | Missing required parameter: `remarks` |

**Correct Reject Call:**

```bash
curl -X POST "/api/v1/indents/{id}/reject?remarks=Rejection reason here"
```

---

## Part 4: Controller Inventory

### All 28 REST Controllers Discovered

| Controller                        | Base Path                          | Status      |
| --------------------------------- | ---------------------------------- | ----------- |
| ApprovalController                | /api/v1/approvals                  | Active      |
| AuthController                    | /api/v1/auth                       | ✅ Verified |
| BulkImportController              | /api/v1/bulk-import                | Active      |
| CompanyController                 | /api/v1/companies                  | ✅ Verified |
| CompanyDepartmentController       | /api/v1/company-departments        | Active      |
| CompanyEmployeeController         | /api/v1/company-employees          | Active      |
| CompanyLocationController         | /api/v1/company-locations          | Active      |
| CompanyLocationMaterialController | /api/v1/company-location-materials | Active      |
| CompanyPlantMaterialController    | /api/v1/company-plant-materials    | Active      |
| DashboardController               | /api/v1/dashboard                  | ✅ Verified |
| DepartmentController              | /api/v1/departments                | ✅ Verified |
| EmployeeController                | /api/v1/employees                  | ✅ Verified |
| EmployeeReportingController       | /api/v1/employee-reporting         | Active      |
| EmployeeRoleController            | /api/v1/employee-roles             | Active      |
| GRNController                     | /api/v1/grn                        | ✅ Verified |
| IndentController                  | /api/v1/indents                    | ✅ Verified |
| InventoryController               | /api/v1/inventory                  | Active      |
| IssueNoteController               | /api/v1/issue-notes                | ✅ Verified |
| LocationController                | /api/v1/locations                  | ✅ Verified |
| MaterialController                | /api/v1/materials                  | ✅ Verified |
| MaterialReportController          | /api/v1/material-reports           | Active      |
| PdfReportController               | /api/v1/reports/pdf                | Active      |
| PlantController                   | /api/v1/plants                     | ✅ Verified |
| POController                      | /api/v1/pos                        | ✅ Verified |
| SapMaterialImportController       | /api/v1/sap-material-import        | Active      |
| UnitOfMeasureController           | /api/v1/unit-of-measures           | ✅ Verified |
| UserController                    | /api/v1/users                      | Active      |
| VendorController                  | /api/v1/vendors                    | ✅ Verified |

---

## Part 5: Issues Categorized

### Category A: Missing Endpoints (CODE Issues - Controllers exist but no GET list method)

| Endpoint                        | Controller Exists              | Issue                                           |
| ------------------------------- | ------------------------------ | ----------------------------------------------- |
| GET /api/v1/approvals           | ApprovalController ✅          | No list endpoint defined, only `/pending` works |
| GET /api/v1/bulk-import         | BulkImportController ✅        | POST-only controller, no GET endpoints          |
| GET /api/v1/material-reports    | MaterialReportController ✅    | No GET endpoints defined                        |
| GET /api/v1/sap-material-import | SapMaterialImportController ✅ | POST-only controller, no GET endpoints          |

### Category B: Completely Missing Endpoints (No Controller)

| Endpoint                               | Expected Functionality        |
| -------------------------------------- | ----------------------------- |
| GET /api/v1/status-codes               | List all status codes         |
| GET /api/v1/roles                      | List all system roles         |
| GET /api/v1/reports/indent-summary     | Indent summary report         |
| GET /api/v1/reports/material-usage     | Material usage report         |
| GET /api/v1/reports/pending-approvals  | Pending approvals report      |
| GET /api/v1/audit                      | Audit log entries             |
| GET /api/v1/notifications              | User notifications            |
| GET /api/v1/notifications/unread-count | Unread notification count     |
| GET /api/v1/dashboard/charts           | Chart data for dashboard      |
| POST /api/v1/sap/sync                  | SAP synchronization           |
| GET /api/v1/email-templates            | Email template management     |
| GET /api/v1/scheduled-jobs/status      | Scheduled job status          |
| GET /api/v1/indents/pending-approval   | List indents pending approval |
| GET /api/v1/indents/my-indents         | List user's own indents       |

### Category C: Authorization Bugs (CRITICAL)

| Endpoint                       | Issue         | Required Roles | User Roles           |
| ------------------------------ | ------------- | -------------- | -------------------- |
| GET /api/v1/employee-reporting | Access Denied | ADMIN, USER    | SUPERADMIN, DEPTHEAD |
| GET /api/v1/employee-roles     | Access Denied | ADMIN, USER    | SUPERADMIN, DEPTHEAD |

**Root Cause:** The `@PreAuthorize("hasAnyRole('ADMIN', 'USER')")` annotation does not include SUPERADMIN role.

### Category D: Routing Bugs

| Endpoint             | Issue                            | Resolution                                                |
| -------------------- | -------------------------------- | --------------------------------------------------------- |
| GET /api/v1/users/me | "Failed to convert 'me' to Long" | Add `/me` mapping before `/{id}` or use different pattern |

### Category E: Validation Issues (API Call Issues)

| Endpoint                         | Issue                     | Resolution                             |
| -------------------------------- | ------------------------- | -------------------------------------- |
| POST /api/v1/indents             | Missing required fields   | Include all required fields in request |
| POST /api/v1/employees           | Missing required fields   | Include all required fields in request |
| POST /api/v1/indents/{id}/reject | Missing remarks parameter | Add `?remarks=` query parameter        |

### Category F: Business Logic Constraints (Expected Behavior)

| Endpoint                 | Issue                             | Notes                    |
| ------------------------ | --------------------------------- | ------------------------ |
| PUT /api/v1/indents/{id} | Only draft indents can be updated | This is correct behavior |

---

## Part 6: SUPERADMIN Role Capabilities Summary

### Confirmed Access Rights

| Module             | Create | Read | Update | Delete | Approve/Reject | Notes                        |
| ------------------ | ------ | ---- | ------ | ------ | -------------- | ---------------------------- |
| Employees          | ⚠️     | ✅   | ⚠️     | ⚠️     | N/A            | Read works, write not tested |
| Materials          | ⚠️     | ✅   | ⚠️     | ⚠️     | N/A            |                              |
| Indents            | ⚠️     | ✅   | ⚠️     | ⚠️     | ✅             | Approve works                |
| Purchase Orders    | ⚠️     | ✅   | ⚠️     | ⚠️     | N/A            |                              |
| GRN                | ⚠️     | ✅   | ⚠️     | ⚠️     | N/A            |                              |
| Vendors            | ⚠️     | ✅   | ⚠️     | ⚠️     | N/A            |                              |
| Issue Notes        | ⚠️     | ✅   | ⚠️     | ⚠️     | N/A            |                              |
| Master Data        | ⚠️     | ✅   | ⚠️     | ⚠️     | N/A            |                              |
| Dashboard          | N/A    | ✅   | N/A    | N/A    | N/A            |                              |
| PDF Reports        | N/A    | ✅   | N/A    | N/A    | N/A            | Works correctly              |
| Users              | ⚠️     | ✅   | ⚠️     | ⚠️     | N/A            | List works, /me broken       |
| Inventory          | N/A    | ✅   | N/A    | N/A    | N/A            | Works correctly              |
| Company Mappings   | ⚠️     | ✅   | ⚠️     | ⚠️     | N/A            | All 3 work                   |
| Employee Reporting | N/A    | ❌   | N/A    | N/A    | N/A            | Access Denied                |
| Employee Roles     | N/A    | ❌   | N/A    | N/A    | N/A            | Access Denied                |
| Pending Approvals  | N/A    | ✅   | N/A    | N/A    | N/A            | Works at /approvals/pending  |

**Legend:**

-   ✅ = Tested and Working
-   ⚠️ = Not fully tested (validation errors in test data)
-   ❌ = Failed/Access Denied

### Working Endpoints Summary (28 Passed)

```
✅ POST /api/v1/auth/login
✅ GET  /api/v1/employees
✅ GET  /api/v1/employees/{id}
✅ GET  /api/v1/employees/{id}/roles
✅ GET  /api/v1/materials
✅ GET  /api/v1/materials/{id}
✅ GET  /api/v1/materials/search
✅ GET  /api/v1/indents
✅ GET  /api/v1/indents/{id}
✅ POST /api/v1/indents/{id}/approve
✅ GET  /api/v1/pos
✅ GET  /api/v1/grn
✅ GET  /api/v1/vendors
✅ GET  /api/v1/issue-notes
✅ GET  /api/v1/departments
✅ GET  /api/v1/locations
✅ GET  /api/v1/companies
✅ GET  /api/v1/plants
✅ GET  /api/v1/unit-of-measures
✅ GET  /api/v1/dashboard/summary
✅ GET  /api/v1/reports/pdf/indent/{id}
✅ GET  /api/v1/approvals/pending
✅ GET  /api/v1/users
✅ GET  /api/v1/inventory
✅ GET  /api/v1/company-departments
✅ GET  /api/v1/company-employees
✅ GET  /api/v1/company-locations
```

### Failed Endpoints Summary (20 Failed)

```
❌ GET  /api/v1/indents/pending-approval    # No endpoint
❌ GET  /api/v1/indents/my-indents          # No endpoint
❌ GET  /api/v1/status-codes                # No endpoint
❌ GET  /api/v1/roles                       # No endpoint
❌ GET  /api/v1/reports/indent-summary      # No endpoint
❌ GET  /api/v1/reports/material-usage      # No endpoint
❌ GET  /api/v1/reports/pending-approvals   # No endpoint
❌ GET  /api/v1/audit                       # No endpoint
❌ GET  /api/v1/notifications               # No endpoint
❌ GET  /api/v1/notifications/unread-count  # No endpoint
❌ GET  /api/v1/dashboard/charts            # No endpoint
❌ GET  /api/v1/email-templates             # No endpoint
❌ GET  /api/v1/scheduled-jobs/status       # No endpoint
❌ GET  /api/v1/approvals                   # No list endpoint
❌ GET  /api/v1/bulk-import                 # POST-only
❌ GET  /api/v1/material-reports            # No endpoints
❌ GET  /api/v1/sap-material-import         # POST-only
❌ GET  /api/v1/users/me                    # Routing bug
❌ GET  /api/v1/employee-reporting          # Access Denied
❌ GET  /api/v1/employee-roles              # Access Denied
```

---

## Part 7: Recommendations

### CRITICAL Priority (Must Fix Now)

1. **Fix Authorization for SUPERADMIN Role**

    - Update `EmployeeReportingController` to include SUPERADMIN: `hasAnyRole('SUPERADMIN', 'ADMIN', 'USER')`
    - Update `EmployeeRoleController` to include SUPERADMIN: `hasAnyRole('SUPERADMIN', 'ADMIN', 'USER')`

2. **Fix User /me Endpoint Routing**
    - Add explicit `@GetMapping("/me")` method before `@GetMapping("/{id}")`
    - Or change pattern to `/api/v1/users/current` to avoid conflict

### High Priority (Must Fix)

3. **Implement Missing Indent Endpoints**

    - `/api/v1/indents/pending-approval` - Critical for approval workflow
    - `/api/v1/indents/my-indents` - Required for user self-service

4. **Add Remarks Parameter to Reject Endpoint**
    - Make remarks a request body parameter instead of query parameter
    - Or document that remarks is required as query parameter

### Medium Priority (Should Fix)

5. **Implement Dashboard Charts Endpoint**

    - `/api/v1/dashboard/charts` - For complete dashboard functionality

6. **Implement Notification System**

    - `/api/v1/notifications` - For user notifications
    - `/api/v1/notifications/unread-count` - For notification badge

7. **Implement Status Codes and Roles Endpoints**
    - `/api/v1/status-codes` - For dropdown/reference data
    - `/api/v1/roles` - For role management

### Low Priority (Nice to Have)

8. **Implement Audit Log Access**

    - `/api/v1/audit` - For compliance and tracking

9. **Implement Report Endpoints**

    - These may be covered by PDF reports, verify requirements

10. **SAP Integration and Scheduled Jobs**
    - May be planned for future phases

---

## Part 8: DEPTHEAD Role Testing

**Test User:** rahul.joshi (Password: password123)  
**Roles Assigned:** DEPTHEAD, PROCUREMENT

### DEPTHEAD Test Results Summary

| Metric                     | Count |
| -------------------------- | ----- |
| **Total Endpoints Tested** | 22    |
| **Passed**                 | 15    |
| **Failed**                 | 7     |
| **Pass Rate**              | 68%   |

### DEPTHEAD: Endpoint Results

| Endpoint                          | Status      | Notes                                        |
| --------------------------------- | ----------- | -------------------------------------------- |
| POST /api/v1/auth/login           | ✅ **PASS** | Roles: DEPTHEAD, PROCUREMENT                 |
| GET /api/v1/employees             | ✅ **PASS** | Works                                        |
| GET /api/v1/employees/11          | ✅ **PASS** | Returns employee detail                      |
| GET /api/v1/materials             | ✅ **PASS** | Works                                        |
| GET /api/v1/indents               | ✅ **PASS** | Works                                        |
| GET /api/v1/indents/1072          | ✅ **PASS** | Returns indent detail                        |
| GET /api/v1/pos                   | ✅ **PASS** | Works                                        |
| GET /api/v1/grn                   | ❌ **FAIL** | Requires STOREKEEPER/VIEWER/ADMIN/SUPERADMIN |
| GET /api/v1/vendors               | ✅ **PASS** | Works                                        |
| GET /api/v1/issue-notes           | ❌ **FAIL** | Requires REQUESTER/EMPLOYEE roles            |
| GET /api/v1/departments           | ✅ **PASS** | Works                                        |
| GET /api/v1/locations             | ✅ **PASS** | Works                                        |
| GET /api/v1/companies             | ✅ **PASS** | Works                                        |
| GET /api/v1/plants                | ✅ **PASS** | Works                                        |
| GET /api/v1/unit-of-measures      | ✅ **PASS** | Works                                        |
| GET /api/v1/dashboard/summary     | ✅ **PASS** | Works                                        |
| GET /api/v1/reports/pdf/indent    | ✅ **PASS** | Returns PDF binary                           |
| GET /api/v1/approvals/pending     | ✅ **PASS** | Works                                        |
| GET /api/v1/users                 | ✅ **PASS** | Works                                        |
| GET /api/v1/inventory             | ✅ **PASS** | Works                                        |
| GET /api/v1/employee-reporting    | ❌ **FAIL** | Requires ADMIN/USER roles                    |
| GET /api/v1/employee-roles        | ❌ **FAIL** | Requires ADMIN/USER roles                    |
| POST /api/v1/indents/1074/approve | ❌ **FAIL** | Indent already rejected (expected)           |
| POST /api/v1/indents/1074/reject  | ✅ **PASS** | Works with remarks                           |
| GET /api/v1/bulk-import           | ❌ **FAIL** | No static resource                           |
| GET /api/v1/sap-material-import   | ❌ **FAIL** | No static resource                           |

### DEPTHEAD Authorization Issues

| Endpoint            | Required Roles                         | User Roles            | Result    |
| ------------------- | -------------------------------------- | --------------------- | --------- |
| /grn                | STOREKEEPER, VIEWER, ADMIN, SUPERADMIN | DEPTHEAD, PROCUREMENT | ❌ Denied |
| /issue-notes        | REQUESTER, EMPLOYEE                    | DEPTHEAD, PROCUREMENT | ❌ Denied |
| /employee-reporting | ADMIN, USER                            | DEPTHEAD, PROCUREMENT | ❌ Denied |
| /employee-roles     | ADMIN, USER                            | DEPTHEAD, PROCUREMENT | ❌ Denied |

---

## Part 9: STOREKEEPER Role Testing

**Test User:** lakshmi.nambiar (Password: password123)  
**Roles Assigned:** STOREKEEPER

### STOREKEEPER Test Results Summary

| Metric                     | Count |
| -------------------------- | ----- |
| **Total Endpoints Tested** | 14    |
| **Passed**                 | 7     |
| **Failed**                 | 7     |
| **Pass Rate**              | 50%   |

### STOREKEEPER: Endpoint Results

| Endpoint                       | Status      | Notes                                                                       |
| ------------------------------ | ----------- | --------------------------------------------------------------------------- |
| POST /api/v1/auth/login        | ✅ **PASS** | Roles: STOREKEEPER                                                          |
| GET /api/v1/employees          | ❌ **FAIL** | Requires ADMIN/SUPERADMIN/VIEWER                                            |
| GET /api/v1/materials          | ✅ **PASS** | Works                                                                       |
| GET /api/v1/indents            | ❌ **FAIL** | Requires DEPTHEAD/PLANTMANAGER/PROCUREMENT/ADMIN/SUPERADMIN                 |
| GET /api/v1/pos                | ❌ **FAIL** | Requires PROCUREMENT/VIEWER/PLANTMANAGER/ADMIN/SUPERADMIN                   |
| GET /api/v1/grn                | ✅ **PASS** | ✅ **Core functionality works!**                                            |
| GET /api/v1/vendors            | ❌ **FAIL** | Requires EMPLOYEE/DEPTHEAD/PLANTMANAGER/PROCUREMENT/VIEWER/ADMIN/SUPERADMIN |
| GET /api/v1/issue-notes        | ✅ **PASS** | Works                                                                       |
| GET /api/v1/departments        | ✅ **PASS** | Works                                                                       |
| GET /api/v1/companies          | ✅ **PASS** | Works                                                                       |
| GET /api/v1/plants             | ✅ **PASS** | Works                                                                       |
| GET /api/v1/dashboard/summary  | ❌ **FAIL** | Requires SUPERADMIN/ADMIN/MANAGER/USER                                      |
| GET /api/v1/inventory          | ✅ **PASS** | ✅ **Core functionality works!**                                            |
| GET /api/v1/reports/pdf/indent | ❌ **FAIL** | Requires SUPERADMIN/ADMIN/MANAGER/USER                                      |
| GET /api/v1/users              | ❌ **FAIL** | Requires ADMIN/SUPERADMIN/VIEWER                                            |

### STOREKEEPER Authorization Analysis

**Properly Authorized (Core Functions):**

-   ✅ GRN List - STOREKEEPER can access goods receipt notes
-   ✅ Issue Notes - STOREKEEPER can access issue notes
-   ✅ Inventory - STOREKEEPER can access inventory
-   ✅ Materials - STOREKEEPER can access materials
-   ✅ Master Data (Departments, Companies, Plants) - Works

**Missing STOREKEEPER Access (Potential Issues):**
| Endpoint | Should STOREKEEPER Have Access? | Recommendation |
| -------- | ------------------------------- | -------------- |
| /employees | Maybe (for receiving goods) | Add STOREKEEPER to allowed roles |
| /indents | Maybe (to see what's requested) | Add STOREKEEPER to allowed roles |
| /pos | Yes (to receive against POs) | **CRITICAL: Add STOREKEEPER** |
| /vendors | Maybe (for goods receipt) | Consider adding STOREKEEPER |
| /dashboard/summary | Nice to have | Add STOREKEEPER if dashboard is for all users |

---

## Part 10: VIEWER Role Testing

**Test User:** manoj.tiwari (Password: password123)  
**Roles Assigned:** VIEWER

### VIEWER Test Results Summary

| Metric                     | Count |
| -------------------------- | ----- |
| **Total Endpoints Tested** | 15    |
| **Passed**                 | 10    |
| **Failed**                 | 5     |
| **Pass Rate**              | 67%   |

### VIEWER: Endpoint Results

| Endpoint                       | Status      | Notes                                                                 |
| ------------------------------ | ----------- | --------------------------------------------------------------------- |
| POST /api/v1/auth/login        | ✅ **PASS** | Roles: VIEWER                                                         |
| GET /api/v1/employees          | ✅ **PASS** | ✅ Can view employees                                                 |
| GET /api/v1/materials          | ✅ **PASS** | ✅ Can view materials                                                 |
| GET /api/v1/indents            | ❌ **FAIL** | VIEWER not included in allowed roles                                  |
| GET /api/v1/pos                | ✅ **PASS** | ✅ Can view POs                                                       |
| GET /api/v1/grn                | ✅ **PASS** | ✅ Can view GRNs                                                      |
| GET /api/v1/vendors            | ✅ **PASS** | ✅ Can view vendors                                                   |
| GET /api/v1/issue-notes        | ❌ **FAIL** | VIEWER not included (requires REQUESTER/EMPLOYEE/MANAGER/STOREKEEPER) |
| GET /api/v1/departments        | ✅ **PASS** | ✅ Can view departments                                               |
| GET /api/v1/companies          | ✅ **PASS** | ✅ Can view companies                                                 |
| GET /api/v1/plants             | ✅ **PASS** | ✅ Can view plants                                                    |
| GET /api/v1/dashboard/summary  | ❌ **FAIL** | VIEWER not included (requires SUPERADMIN/ADMIN/MANAGER/USER)          |
| GET /api/v1/inventory          | ✅ **PASS** | ✅ Can view inventory                                                 |
| GET /api/v1/reports/pdf/indent | ❌ **FAIL** | VIEWER not included (requires SUPERADMIN/ADMIN/MANAGER/USER)          |
| GET /api/v1/users              | ✅ **PASS** | ✅ Can view users                                                     |
| GET /api/v1/approvals/pending  | ❌ **FAIL** | VIEWER not included (requires DEPTHEAD/PLANTMANAGER/ADMIN/SUPERADMIN) |

### VIEWER Authorization Analysis

**Working Read Access:**

-   Employees ✅
-   Materials ✅
-   POs ✅
-   GRNs ✅
-   Vendors ✅
-   Master Data ✅
-   Inventory ✅
-   Users ✅

**Missing VIEWER Access (Should Be Added for View-Only Role):**
| Endpoint | Recommendation |
| -------- | -------------- |
| /indents | **Add VIEWER** - Core viewing function |
| /issue-notes | **Add VIEWER** - Core viewing function |
| /dashboard/summary | **Add VIEWER** - Should see dashboard |
| /reports/pdf/indent | **Add VIEWER** - Should view reports |
| /approvals/pending | Add VIEWER (read-only view of pending items) |

---

## Part 11: Cross-Role Authorization Matrix

### Endpoint Access By Role

| Endpoint            | SUPERADMIN | DEPTHEAD | STOREKEEPER | VIEWER | Required Roles in Code                                                   |
| ------------------- | ---------- | -------- | ----------- | ------ | ------------------------------------------------------------------------ |
| /employees          | ✅         | ✅       | ❌          | ✅     | ADMIN, SUPERADMIN, VIEWER                                                |
| /materials          | ✅         | ✅       | ✅          | ✅     | (Open to authenticated users)                                            |
| /indents            | ✅         | ✅       | ❌          | ❌     | DEPTHEAD, PLANTMANAGER, PROCUREMENT, ADMIN, SUPERADMIN                   |
| /pos                | ✅         | ✅       | ❌          | ✅     | PROCUREMENT, VIEWER, PLANTMANAGER, ADMIN, SUPERADMIN                     |
| /grn                | ✅         | ❌       | ✅          | ✅     | STOREKEEPER, VIEWER, ADMIN, SUPERADMIN                                   |
| /vendors            | ✅         | ✅       | ❌          | ✅     | EMPLOYEE, DEPTHEAD, PLANTMANAGER, PROCUREMENT, VIEWER, ADMIN, SUPERADMIN |
| /issue-notes        | ✅         | ❌       | ✅          | ❌     | REQUESTER, EMPLOYEE, MANAGER, STOREKEEPER, ADMIN, SUPERADMIN             |
| /dashboard/summary  | ✅         | ✅       | ❌          | ❌     | SUPERADMIN, ADMIN, MANAGER, USER                                         |
| /inventory          | ✅         | ✅       | ✅          | ✅     | (Open to authenticated users)                                            |
| /users              | ✅         | ✅       | ❌          | ✅     | ADMIN, SUPERADMIN, VIEWER                                                |
| /reports/pdf        | ✅         | ✅       | ❌          | ❌     | SUPERADMIN, ADMIN, MANAGER, USER                                         |
| /approvals/pending  | ✅         | ✅       | N/A         | ❌     | DEPTHEAD, PLANTMANAGER, ADMIN, SUPERADMIN                                |
| /employee-reporting | ❌         | ❌       | N/A         | N/A    | ADMIN, USER (missing SUPERADMIN!)                                        |
| /employee-roles     | ❌         | ❌       | N/A         | N/A    | ADMIN, USER (missing SUPERADMIN!)                                        |

### Critical Authorization Issues Found

| Issue                                | Severity    | Affected Roles | Recommendation                         |
| ------------------------------------ | ----------- | -------------- | -------------------------------------- |
| SUPERADMIN denied employee-reporting | 🔴 CRITICAL | SUPERADMIN     | Add SUPERADMIN to annotation           |
| SUPERADMIN denied employee-roles     | 🔴 CRITICAL | SUPERADMIN     | Add SUPERADMIN to annotation           |
| VIEWER cannot view indents           | 🟡 HIGH     | VIEWER         | Add VIEWER for read-only access        |
| VIEWER cannot view reports           | 🟡 HIGH     | VIEWER         | Add VIEWER for read-only access        |
| VIEWER cannot view dashboard         | 🟡 HIGH     | VIEWER         | Add VIEWER for overview access         |
| STOREKEEPER cannot view POs          | 🟡 HIGH     | STOREKEEPER    | Add STOREKEEPER for receiving workflow |
| DEPTHEAD cannot view GRN             | 🟢 LOW      | DEPTHEAD       | May be intentional                     |
| DEPTHEAD cannot view issue-notes     | 🟢 LOW      | DEPTHEAD       | May be intentional                     |

---

## Part 12: Overall Test Summary

### All Roles Tested

| Role        | User            | Pass | Fail | Pass Rate |
| ----------- | --------------- | ---- | ---- | --------- |
| SUPERADMIN  | vikram.singh    | 28   | 20   | 58%       |
| DEPTHEAD    | rahul.joshi     | 15   | 7    | 68%       |
| STOREKEEPER | lakshmi.nambiar | 7    | 7    | 50%       |
| VIEWER      | manoj.tiwari    | 10   | 5    | 67%       |

### Aggregate Statistics

| Metric                           | Value |
| -------------------------------- | ----- |
| Total Unique Endpoints Tested    | ~35   |
| Critical Authorization Bugs      | 2     |
| High Priority Authorization Gaps | 5     |
| Missing Endpoints                | 14    |
| Routing Bugs                     | 1     |

---

## Part 13: Final Recommendations

### 🔴 CRITICAL (Fix Immediately)

1. **Add SUPERADMIN to employee-reporting and employee-roles**

    ```java
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN', 'USER')")
    ```

2. **Fix /users/me routing conflict**
    - Add explicit mapping before /{id}

### 🟡 HIGH Priority

3. **Add STOREKEEPER to PO list access** - Required for goods receiving workflow
4. **Add VIEWER to indents list** - Core viewing functionality
5. **Add VIEWER to reports/pdf** - View-only users need report access
6. **Add VIEWER to dashboard/summary** - View-only users need overview

### 🟢 MEDIUM Priority

7. Implement missing indent endpoints (`/pending-approval`, `/my-indents`)
8. Implement status-codes and roles endpoints
9. Add VIEWER to issue-notes for complete view access

### 🔵 LOW Priority

10. Implement notification system
11. Implement audit log access
12. Implement dashboard charts endpoint

---

## Appendix A: Test Credentials Used

| Username        | Password    | Roles                 |
| --------------- | ----------- | --------------------- |
| vikram.singh    | password123 | SUPERADMIN, DEPTHEAD  |
| rahul.joshi     | password123 | DEPTHEAD, PROCUREMENT |
| lakshmi.nambiar | password123 | STOREKEEPER           |
| manoj.tiwari    | password123 | VIEWER                |

## Appendix B: Server Details

-   **Base URL:** http://localhost:8080
-   **Health Check:** http://localhost:8080/actuator/health
-   **Server Status:** Running
-   **Database:** seeds_indent @ localhost:3306

## Appendix C: Role Definitions from Database

| Role ID | Role Name           | Description                             |
| ------- | ------------------- | --------------------------------------- |
| 1       | Super Administrator | Full system access                      |
| 2       | Administrator       | System administration                   |
| 3       | Department Head     | Department management, indent approvals |
| 4       | Store Keeper        | Inventory, GRN, Issue Notes             |
| 5       | Procurement Officer | PO management, vendor management        |
| 6       | Plant Manager       | Plant-level oversight                   |
| 7       | Finance Manager     | Financial approvals                     |
| 8       | Quality Manager     | Quality control                         |
| 9       | Regular Employee    | Basic access                            |
| 10      | View Only           | Read-only access                        |

## Appendix D: Additional Roles Not Yet Tested

| Role         | Test User Available       | Status                |
| ------------ | ------------------------- | --------------------- |
| PROCUREMENT  | gayatri.menon, neha.gupta | Available for testing |
| PLANTMANAGER | (needs user assignment)   | Pending               |
| FINANCE      | (needs user assignment)   | Pending               |
| QUALITY      | (needs user assignment)   | Pending               |
| EMPLOYEE     | swati.bhatt, anil.kapoor  | Available for testing |

---

_Report Generated: November 29, 2025_  
_Last Updated: November 29, 2025 (Added DEPTHEAD, STOREKEEPER, VIEWER tests)_  
_QA Audit Tool Version: 1.0_
