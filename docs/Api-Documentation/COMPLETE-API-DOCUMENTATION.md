# ProcureZone Backend - Complete API Documentation

**Version**: 1.0  
**Last Updated**: October 15, 2025  
**Base URL**: `http://localhost:8080`  
**API Prefix**: `/api/v1`

---

## 📚 Table of Contents

1. [Getting Started](#getting-started)
2. [Authentication](#authentication)
3. [Authorization & Roles](#authorization--roles)
4. [API Modules](#api-modules)
   - [Authentication Module](#1-authentication-module)
   - [Indent Module](#2-indent-module)
   - [Vendor Module](#3-vendor-module)
   - [Purchase Order Module](#4-purchase-order-module)
   - [User Management Module](#5-user-management-module)
   - [Master Data Module](#6-master-data-module)
   - [Employee Module](#7-employee-module)
5. [Common Patterns](#common-patterns)
6. [Error Handling](#error-handling)
7. [Testing](#testing)

---

## Getting Started

### Prerequisites

- Backend running on `http://localhost:8080`
- Valid user credentials
- MySQL database `seeds_indent` configured

### Quick Start

1. **Login** to get JWT token:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rajesh.kumar",
    "password": "password123"
  }'
```

2. **Use the token** in subsequent requests:

```bash
curl -X GET http://localhost:8080/api/v1/indents \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

---

## Authentication

### JWT Token-Based Authentication

All API endpoints (except `/api/v1/auth/**`) require authentication via JWT token.

**Token Format**: `Bearer <token>`

**Token Validity**: 1 hour

**Token Claims**:

- `sub`: username
- `uid`: user_id
- `emp`: employee_number
- `empId`: employee_id (e.g., "EMP001")
- `name`: full name
- `email`: email address
- `roles`: array of role names

### Login Endpoint

```
POST /api/v1/auth/login
```

**Request**:

```json
{
  "username": "rajesh.kumar",
  "password": "password123"
}
```

**Response** (200 OK):

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "userId": 1,
  "username": "rajesh.kumar",
  "employeeNumber": 1,
  "employeeId": "EMP001",
  "name": "Rajesh Kumar",
  "email": "rajesh.kumar@nslindia.com",
  "roles": ["SUPERADMIN"]
}
```

**Error Responses**:

- `401 Unauthorized`: Invalid credentials
- `403 Forbidden`: Account inactive/locked

---

## Authorization & Roles

### Role Hierarchy

| Role             | Code           | Description          | Level |
| ---------------- | -------------- | -------------------- | ----- |
| **SUPERADMIN**   | `SUPERADMIN`   | System Administrator | 5     |
| **ADMIN**        | `ADMIN`        | Administrator        | 4     |
| **PLANTMANAGER** | `PLANTMANAGER` | Plant Manager        | 3     |
| **DEPTHEAD**     | `DEPTHEAD`     | Department Head      | 2     |
| **PROCUREMENT**  | `PROCUREMENT`  | Procurement Officer  | 2     |
| **EMPLOYEE**     | `EMPLOYEE`     | Regular Employee     | 1     |
| **VIEWER**       | `VIEWER`       | Read-only Access     | 1     |
| **STOREKEEPER**  | `STOREKEEPER`  | Store Keeper         | 1     |

### Role-Based Access Matrix

| Module             | Create       | Read             | Update           | Delete         | Approve       | Special              |
| ------------------ | ------------ | ---------------- | ---------------- | -------------- | ------------- | -------------------- |
| **Indent**         | EMPLOYEE+    | ALL              | EMPLOYEE (own)   | EMPLOYEE (own) | DEPTHEAD+     | Submit: EMPLOYEE     |
| **Vendor**         | PROCUREMENT+ | ALL              | PROCUREMENT+     | ADMIN+         | N/A           | Rating: PROCUREMENT+ |
| **Purchase Order** | PROCUREMENT+ | ALL              | PROCUREMENT+     | ADMIN+         | PLANTMANAGER+ | Send: PROCUREMENT+   |
| **User**           | ADMIN+       | ADMIN+ (or self) | ADMIN+ (or self) | ADMIN+         | N/A           | Password: Self       |
| **Master Data**    | ADMIN+       | ALL              | ADMIN+           | ADMIN+         | N/A           | N/A                  |
| **Employee**       | ADMIN+       | ALL              | ADMIN+           | ADMIN+         | N/A           | N/A                  |

**Legend**: `+` means "and above" in hierarchy

---

## API Modules

### 1. Authentication Module

**Status**: ✅ Complete  
**Endpoints**: 3  
**Documentation**: [Authentication API](./AUTHENTICATION-API.md)

| Method | Endpoint               | Description   | Auth Required |
| ------ | ---------------------- | ------------- | ------------- |
| POST   | `/api/v1/auth/login`   | User login    | ❌ No         |
| POST   | `/api/v1/auth/logout`  | User logout   | ✅ Yes        |
| POST   | `/api/v1/auth/refresh` | Refresh token | ✅ Yes        |

[📖 View Complete Documentation](./AUTHENTICATION-API.md)

---

### 2. Indent Module

**Status**: ✅ Complete  
**Endpoints**: 14  
**Documentation**: [Indent API](./INDENT-API.md)

#### Quick Overview

| Method | Endpoint                               | Description         | Roles          |
| ------ | -------------------------------------- | ------------------- | -------------- |
| POST   | `/api/v1/indents`                      | Create indent       | EMPLOYEE+      |
| GET    | `/api/v1/indents`                      | List all indents    | DEPTHEAD+      |
| GET    | `/api/v1/indents/{id}`                 | Get indent by ID    | ALL            |
| GET    | `/api/v1/indents/search`               | Search indents      | DEPTHEAD+      |
| GET    | `/api/v1/indents/status/{statusId}`    | List by status      | ALL            |
| GET    | `/api/v1/indents/employee/{empNumber}` | List by employee    | ALL            |
| PUT    | `/api/v1/indents/{id}`                 | Update indent       | EMPLOYEE (own) |
| DELETE | `/api/v1/indents/{id}`                 | Delete indent       | EMPLOYEE (own) |
| POST   | `/api/v1/indents/{id}/submit`          | Submit for approval | EMPLOYEE (own) |
| POST   | `/api/v1/indents/{id}/approve`         | Approve indent      | DEPTHEAD+      |
| POST   | `/api/v1/indents/{id}/reject`          | Reject indent       | DEPTHEAD+      |

**Key Features**:

- Complete CRUD lifecycle
- Multi-level approval workflow
- Status tracking (Draft → Submitted → Approved → Rejected)
- Search and filter capabilities
- Role-based access control

[📖 View Complete Documentation](./INDENT-API.md)

---

### 3. Vendor Module

**Status**: ✅ Complete  
**Endpoints**: 9  
**Documentation**: [Vendor API](./VENDOR-API.md)

#### Quick Overview

| Method | Endpoint                           | Description              | Roles        |
| ------ | ---------------------------------- | ------------------------ | ------------ |
| POST   | `/api/v1/vendors`                  | Create vendor            | PROCUREMENT+ |
| GET    | `/api/v1/vendors`                  | List vendors (paginated) | ALL          |
| GET    | `/api/v1/vendors/{id}`             | Get vendor by ID         | ALL          |
| GET    | `/api/v1/vendors/active`           | List active vendors      | ALL          |
| GET    | `/api/v1/vendors/search`           | Search vendors           | ALL          |
| PUT    | `/api/v1/vendors/{id}`             | Update vendor            | PROCUREMENT+ |
| DELETE | `/api/v1/vendors/{id}`             | Soft delete vendor       | ADMIN+       |
| PUT    | `/api/v1/vendors/{id}/rating`      | Update rating            | PROCUREMENT+ |
| GET    | `/api/v1/vendors/{id}/performance` | Get performance          | PROCUREMENT+ |

**Key Features**:

- Vendor lifecycle management
- Multi-criteria search (name, code, contact)
- Rating and performance tracking
- Active/Inactive status management
- Purchase history integration

[📖 View Complete Documentation](./VENDOR-API.md)

---

### 4. Purchase Order Module

**Status**: ✅ Complete  
**Endpoints**: 16  
**Documentation**: [Purchase Order API](./PURCHASE-ORDER-API.md)

#### Quick Overview

| Method | Endpoint                            | Description           | Roles         |
| ------ | ----------------------------------- | --------------------- | ------------- |
| GET    | `/api/v1/po/approved-indents`       | Get approved indents  | PROCUREMENT+  |
| POST   | `/api/v1/po`                        | Create PO from indent | PROCUREMENT+  |
| GET    | `/api/v1/po`                        | List POs (paginated)  | ALL           |
| GET    | `/api/v1/po/{id}`                   | Get PO by ID          | ALL           |
| GET    | `/api/v1/po/number/{poNumber}`      | Get by PO number      | ALL           |
| POST   | `/api/v1/po/{id}/submit`            | Submit for approval   | PROCUREMENT+  |
| POST   | `/api/v1/po/{id}/approve`           | Approve PO            | PLANTMANAGER+ |
| POST   | `/api/v1/po/{id}/send-to-vendor`    | Send to vendor        | PROCUREMENT+  |
| POST   | `/api/v1/po/{id}/receive-goods`     | Mark goods received   | STOREKEEPER+  |
| POST   | `/api/v1/po/{id}/cancel`            | Cancel PO             | PROCUREMENT+  |
| POST   | `/api/v1/po/{id}/close`             | Close PO              | PROCUREMENT+  |
| GET    | `/api/v1/po/search`                 | Search POs            | ALL           |
| GET    | `/api/v1/po/by-vendor/{vendorId}`   | Filter by vendor      | ALL           |
| GET    | `/api/v1/po/by-department/{deptId}` | Filter by department  | ALL           |
| GET    | `/api/v1/po/pending-approval`       | Pending approval      | PLANTMANAGER+ |
| GET    | `/api/v1/po/overdue`                | Overdue POs           | PROCUREMENT+  |

**Key Features**:

- Create PO from approved indent
- Multi-level workflow (Draft → Submitted → Approved → Sent → Received → Closed)
- Vendor management integration
- Department-based filtering
- Overdue tracking
- Goods receipt management

[📖 View Complete Documentation](./PURCHASE-ORDER-API.md)

---

### 5. User Management Module

**Status**: ✅ Complete  
**Endpoints**: 8  
**Documentation**: [User API](./USER-API.md)

#### Quick Overview

| Method | Endpoint                             | Description            | Roles            |
| ------ | ------------------------------------ | ---------------------- | ---------------- |
| POST   | `/api/v1/users`                      | Create user account    | ADMIN+           |
| GET    | `/api/v1/users`                      | List users (paginated) | ADMIN+, VIEWER   |
| GET    | `/api/v1/users/{id}`                 | Get user by ID         | ADMIN+ (or self) |
| PUT    | `/api/v1/users/{id}`                 | Update profile         | ADMIN+ (or self) |
| POST   | `/api/v1/users/{id}/change-password` | Change password        | Self only        |
| POST   | `/api/v1/users/{id}/activate`        | Activate user          | ADMIN+           |
| POST   | `/api/v1/users/{id}/deactivate`      | Deactivate user        | ADMIN+           |
| POST   | `/api/v1/users/{id}/reset-password`  | Reset password         | ADMIN+           |

**Key Features**:

- User account lifecycle
- Self-service password management
- Admin password reset
- Account activation/deactivation
- Employee linkage
- Role assignment integration

[📖 View Complete Documentation](./USER-API.md)

---

### 6. Master Data Module

**Status**: ✅ Complete  
**Endpoints**: 28+ (across 4 entities)  
**Documentation**: [Master Data API](./MASTER-DATA-API.md)

#### Entities

1. **Unit of Measure** (7 endpoints)
2. **Department** (7 endpoints)
3. **Material** (7 endpoints)
4. **Company** (7 endpoints)

#### Quick Overview - Unit of Measure

| Method | Endpoint                          | Description           | Roles  |
| ------ | --------------------------------- | --------------------- | ------ |
| POST   | `/api/v1/unit-of-measures`        | Create UOM            | ADMIN+ |
| GET    | `/api/v1/unit-of-measures`        | List UOMs (paginated) | ALL    |
| GET    | `/api/v1/unit-of-measures/{id}`   | Get UOM by ID         | ALL    |
| GET    | `/api/v1/unit-of-measures/active` | List active UOMs      | ALL    |
| GET    | `/api/v1/unit-of-measures/search` | Search UOMs           | ALL    |
| PUT    | `/api/v1/unit-of-measures/{id}`   | Update UOM            | ADMIN+ |
| DELETE | `/api/v1/unit-of-measures/{id}`   | Soft delete UOM       | ADMIN+ |

**Key Features**:

- Centralized master data management
- Active/Inactive status
- Search and filter
- Audit trail (last modified by/date)
- Validation and uniqueness checks

[📖 View Complete Documentation](./MASTER-DATA-API.md)

---

### 7. Employee Module

**Status**: ✅ Complete  
**Endpoints**: 11  
**Documentation**: [Employee API](./EMPLOYEE-API.md)

#### Quick Overview

| Method | Endpoint                                   | Description           | Roles  |
| ------ | ------------------------------------------ | --------------------- | ------ |
| POST   | `/api/v1/employees`                        | Create employee       | ADMIN+ |
| GET    | `/api/v1/employees`                        | List employees        | ALL    |
| GET    | `/api/v1/employees/{id}`                   | Get employee by ID    | ALL    |
| GET    | `/api/v1/employees/active`                 | List active employees | ALL    |
| GET    | `/api/v1/employees/by-department/{deptId}` | By department         | ALL    |
| GET    | `/api/v1/employees/search`                 | Search employees      | ALL    |
| PUT    | `/api/v1/employees/{id}`                   | Update employee       | ADMIN+ |
| DELETE | `/api/v1/employees/{id}`                   | Soft delete employee  | ADMIN+ |
| POST   | `/api/v1/employees/{id}/assign-role`       | Assign role           | ADMIN+ |
| POST   | `/api/v1/employees/{id}/remove-role`       | Remove role           | ADMIN+ |
| GET    | `/api/v1/employees/{id}/roles`             | Get employee roles    | ADMIN+ |

**Key Features**:

- Employee profile management
- Department assignment
- Role management (multiple roles per employee)
- Search by name, ID, email
- Active/Inactive status
- Integration with User and Indent modules

[📖 View Complete Documentation](./EMPLOYEE-API.md)

---

## Common Patterns

### Pagination

All list endpoints support pagination with these query parameters:

| Parameter | Type    | Default | Description                                 |
| --------- | ------- | ------- | ------------------------------------------- |
| `page`    | integer | 0       | Page number (0-indexed)                     |
| `size`    | integer | 10-20   | Items per page                              |
| `sort`    | string  | varies  | Sort field and direction (e.g., `name,asc`) |

**Example**:

```
GET /api/v1/indents?page=0&size=20&sort=indentDate,desc
```

**Response Structure**:

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 20
}
```

### Filtering

Common filter patterns:

- **By Status**: `/api/v1/indents/status/1`
- **By Employee**: `/api/v1/indents/employee/1001`
- **By Department**: `/api/v1/po/by-department/3`
- **Active Only**: `/api/v1/vendors/active`

### Search

Search endpoints support:

- **Query Parameter**: `?q=searchTerm`
- **Multi-field Search**: Searches across name, code, description
- **Case-Insensitive**: All searches are case-insensitive
- **Partial Match**: Supports wildcard matching

**Example**:

```
GET /api/v1/vendors/search?q=steel&page=0&size=10
```

### Date Formats

- **Request**: ISO 8601 format `YYYY-MM-DD`
- **Response**: ISO 8601 format `YYYY-MM-DD`
- **DateTime**: ISO 8601 format `YYYY-MM-DDTHH:mm:ss`

### Status Codes

Standard HTTP status codes:

| Code | Meaning               | Usage                                      |
| ---- | --------------------- | ------------------------------------------ |
| 200  | OK                    | Successful GET, PUT, POST (non-creation)   |
| 201  | Created               | Successful POST (resource creation)        |
| 204  | No Content            | Successful DELETE                          |
| 400  | Bad Request           | Validation error, invalid input            |
| 401  | Unauthorized          | Missing/invalid authentication             |
| 403  | Forbidden             | Insufficient permissions                   |
| 404  | Not Found             | Resource doesn't exist                     |
| 409  | Conflict              | Duplicate resource (e.g., username exists) |
| 500  | Internal Server Error | Server-side error                          |

---

## Error Handling

### Error Response Format

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2025-10-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for field 'indentDate': must not be null",
  "path": "/api/v1/indents"
}
```

### Validation Errors

Field validation errors include details:

```json
{
  "timestamp": "2025-10-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "indentDate",
      "message": "must not be null"
    },
    {
      "field": "quantity",
      "message": "must be greater than 0"
    }
  ]
}
```

### Common Error Messages

| Error                        | Meaning                        | Solution                          |
| ---------------------------- | ------------------------------ | --------------------------------- |
| `Invalid credentials`        | Wrong username/password        | Check credentials                 |
| `Token expired`              | JWT token expired              | Re-login to get new token         |
| `Access denied`              | Insufficient role permissions  | Contact admin for role assignment |
| `Resource not found`         | Entity doesn't exist           | Verify ID/identifier              |
| `Duplicate entry`            | Unique constraint violation    | Use different value               |
| `Indent not in DRAFT status` | Cannot modify submitted indent | Create new indent                 |

---

## Testing

### Test Users

Default test users available:

| Username       | Password      | Role         | Employee # | Purpose              |
| -------------- | ------------- | ------------ | ---------- | -------------------- |
| `rajesh.kumar` | `password123` | SUPERADMIN   | 1          | Full system access   |
| `priya.sharma` | `password123` | ADMIN        | 2          | Administrative tasks |
| `amit.patel`   | `password123` | ADMIN        | 3          | Administrative tasks |
| `suresh.reddy` | `password123` | PLANTMANAGER | 4          | Approval workflows   |

### Test Scripts

Comprehensive test scripts available:

1. **Indent API Tests**: `comprehensive_api_test_for_indent.sh`

   - 24 tests across 6 phases
   - 79%+ pass rate
   - Tests: CRUD, workflow, error handling

2. **Complete API Tests**: `comprehensive_api_test.sh`
   - 102 tests across all modules
   - Full system integration testing

### Running Tests

```bash
# Indent module tests
bash e:/Net-Beans/comprehensive_api_test_for_indent.sh

# Complete API tests
cd backend/scripts
bash comprehensive_api_test.sh
```

### Postman Collections

Import Postman collections:

1. **Collection**: `backend/ProcureZone-Auth.postman_collection.json`
2. **Environment**: `backend/ProcureZone-Local.postman_environment.json`

---

## Additional Resources

### Documentation Files

- [Database Setup Guide](../DATABASE_SETUP_GUIDE.md)
- [Schema Fix Report](../../SCHEMA-FIX-AND-TEST-SUCCESS.md)
- [Development Action Plan](../DEVELOPMENT-ACTION-PLAN.md)
- [Week 1 Approval Workflow](../WEEK1-APPROVAL-WORKFLOW-IMPLEMENTATION-COMPLETE.md)
- [Week 3 Vendor Module](../WEEK3-VENDOR-MODULE-COMPLETE.md)
- [Week 4-6 PO Module](../WEEK4-6-PO-MODULE-COMPLETE.md)
- [Week 9-10 User Module](../WEEK9-10-USER-MODULE-COMPLETE.md)

### Quick Links

- [API Testing Guide](./API_TESTING_GUIDE.md)
- [Error Log](../Errorlog-Documentation/ERROR_LOG.md)
- [Feature Summary](../Features-Documentation/FEATURE_IMPLEMENTATION_SUMMARY.md)
- [Project Completion Plan](../PROJECT-COMPLETION-PLAN.md)

---

## Support & Contact

For issues or questions:

1. Check error logs: `backend/logs/`
2. Review test results: `*_api_test_results_*.txt`
3. Verify backend status: `curl http://localhost:8080/actuator/health`
4. Check database schema: `DESCRIBE tbl_name`

---

**Document Version**: 1.0  
**Last Updated**: October 15, 2025  
**Status**: ✅ All Phase 1 & 2 Modules Complete  
**Total Endpoints**: 70+ across 7 modules
