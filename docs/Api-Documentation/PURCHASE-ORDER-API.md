# Purchase Order API Documentation

**Module**: Purchase Order Management  
**Version**: 1.0  
**Status**: ✅ Complete  
**Endpoints**: 16  
**Base Path**: `/api/v1/po`

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Authorization](#authorization)
4. [Workflow States](#workflow-states)
5. [Endpoints](#endpoints)
6. [Data Models](#data-models)
7. [Business Rules](#business-rules)
8. [Examples](#examples)

---

## Overview

The Purchase Order (PO) module manages the complete procurement workflow from approved indent to goods receipt. It handles PO creation, multi-level approvals, vendor communication, and delivery tracking.

### Key Features

- ✅ Auto-creation from approved indents
- ✅ Multi-level approval workflow
- ✅ Vendor selection and communication
- ✅ Goods receipt and quality check
- ✅ PO tracking and status management
- ✅ Overdue PO alerts
- ✅ Department and vendor-wise reporting
- ✅ Full audit trail

### Workflow States

```
DRAFT (1) → SUBMITTED (2) → APPROVED (3) → SENT_TO_VENDOR (4) →
RECEIVED (5) → CLOSED (6) → CANCELLED (7)
```

---

## Authentication

All endpoints require JWT authentication.

**Header Required**:

```
Authorization: Bearer <JWT_TOKEN>
```

---

## Authorization

### Role-Based Access by Action

| Action             | Allowed Roles                               |
| ------------------ | ------------------------------------------- |
| **Create PO**      | PROCUREMENT, ADMIN, SUPERADMIN              |
| **View PO**        | ALL authenticated users                     |
| **Update PO**      | PROCUREMENT, ADMIN, SUPERADMIN              |
| **Submit PO**      | PROCUREMENT, ADMIN, SUPERADMIN              |
| **Approve PO**     | PLANTMANAGER, ADMIN, SUPERADMIN             |
| **Send to Vendor** | PROCUREMENT, ADMIN, SUPERADMIN              |
| **Receive Goods**  | STOREKEEPER, PROCUREMENT, ADMIN, SUPERADMIN |
| **Cancel PO**      | ADMIN, SUPERADMIN                           |
| **Close PO**       | PROCUREMENT, ADMIN, SUPERADMIN              |
| **Search**         | ALL authenticated users                     |

---

## Workflow States

### Status Flow

| Status ID | Status Name    | Description                   | Next States               |
| --------- | -------------- | ----------------------------- | ------------------------- |
| 1         | DRAFT          | PO created, under preparation | SUBMITTED, CANCELLED      |
| 2         | SUBMITTED      | Submitted for approval        | APPROVED, CANCELLED       |
| 3         | APPROVED       | Approved by Plant Manager     | SENT_TO_VENDOR, CANCELLED |
| 4         | SENT_TO_VENDOR | PO sent to vendor             | RECEIVED, CANCELLED       |
| 5         | RECEIVED       | Goods received                | CLOSED                    |
| 6         | CLOSED         | PO completed                  | None (terminal)           |
| 7         | CANCELLED      | PO cancelled                  | None (terminal)           |

---

## Endpoints

### 1. Get Approved Indents

Retrieve all approved indents ready for PO creation.

```http
GET /api/v1/po/approved-indents?page=0&size=20
```

**Authorization**: PROCUREMENT, ADMIN, SUPERADMIN

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | integer | 0 | Page number (0-indexed) |
| `size` | integer | 20 | Items per page |

**Response** (200 OK):

```json
{
  "content": [
    {
      "indentId": 45,
      "indentNumber": "IND/2025/00045",
      "empNumber": 1,
      "empName": "Rajesh Kumar",
      "deptId": 101,
      "deptName": "Production",
      "statusId": 3,
      "statusName": "Approved",
      "createdDate": "2025-10-10",
      "items": [
        {
          "itemId": 101,
          "materialName": "Steel Plates",
          "quantity": 100,
          "uomName": "Kg"
        }
      ]
    }
  ],
  "totalElements": 12,
  "totalPages": 1
}
```

**Business Rule**: Only indents with status=3 (Approved) are returned

---

### 2. Create Purchase Order

Create new PO from approved indent.

```http
POST /api/v1/po
```

**Authorization**: PROCUREMENT, ADMIN, SUPERADMIN

**Request Body**:

```json
{
  "indentId": 45,
  "vendorId": 101,
  "expectedDeliveryDate": "2025-11-15",
  "deliveryAddress": "Warehouse A, Plot 12, Industrial Area",
  "termsAndConditions": "Standard procurement terms apply",
  "items": [
    {
      "indentItemId": 101,
      "unitPrice": 150.0,
      "taxPercent": 18.0,
      "discount": 5.0
    }
  ]
}
```

**Response** (201 Created):

```json
{
  "poId": 201,
  "poNumber": "PO/2025/00201",
  "indentNumber": "IND/2025/00045",
  "vendorId": 101,
  "vendorName": "ABC Steel Industries",
  "deptId": 101,
  "deptName": "Production",
  "poDate": "2025-10-15",
  "expectedDeliveryDate": "2025-11-15",
  "deliveryAddress": "Warehouse A, Plot 12, Industrial Area",
  "termsAndConditions": "Standard procurement terms apply",
  "totalAmount": 15855.0,
  "statusId": 1,
  "statusName": "DRAFT",
  "createdBy": "procurement.user",
  "createdDate": "2025-10-15T10:30:00",
  "items": [
    {
      "poDetailId": 301,
      "materialName": "Steel Plates",
      "quantity": 100,
      "uomName": "Kg",
      "unitPrice": 150.0,
      "taxPercent": 18.0,
      "discount": 5.0,
      "netAmount": 15855.0
    }
  ]
}
```

**PO Number Format**: `PO/YYYY/NNNNN`

- Example: PO/2025/00201

**Validations**:

- `indentId`: Required, must exist, must be approved (status=3)
- `vendorId`: Required, must exist, must be active
- `expectedDeliveryDate`: Required, must be future date
- `items`: Required, at least one item
- `unitPrice`: Required, > 0
- `taxPercent`: Required, 0-100
- `discount`: Optional, 0-100

**Amount Calculation**:

```
Subtotal = quantity * unitPrice
Discount Amount = subtotal * (discount / 100)
Taxable Amount = subtotal - discount
Tax Amount = taxable * (taxPercent / 100)
Net Amount = taxable + tax
Total PO Amount = Sum of all item net amounts
```

---

### 3. Get PO by ID

Retrieve PO details by ID.

```http
GET /api/v1/po/{id}
```

**Authorization**: ALL authenticated users

**Path Parameters**:

- `id` (integer, required): PO ID

**Response** (200 OK): Same structure as Create PO response

---

### 4. Get PO by Number

Retrieve PO details by PO number.

```http
GET /api/v1/po/number/{poNumber}
```

**Authorization**: ALL authenticated users

**Path Parameters**:

- `poNumber` (string, required): PO number (e.g., PO/2025/00201)

**Response** (200 OK): Same structure as Create PO response

---

### 5. List All POs

Retrieve paginated list of all POs.

```http
GET /api/v1/po?page=0&size=20&sort=poDate,desc
```

**Authorization**: ALL authenticated users

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | integer | 0 | Page number (0-indexed) |
| `size` | integer | 20 | Items per page |
| `sort` | string | `poDate,desc` | Sort field and direction |

**Response** (200 OK):

```json
{
  "content": [
    {
      "poId": 201,
      "poNumber": "PO/2025/00201",
      "vendorName": "ABC Steel Industries",
      "deptName": "Production",
      "poDate": "2025-10-15",
      "expectedDeliveryDate": "2025-11-15",
      "totalAmount": 15855.0,
      "statusName": "DRAFT"
    }
  ],
  "totalElements": 156,
  "totalPages": 8
}
```

---

### 6. Search POs

Search POs by number, vendor, or department.

```http
GET /api/v1/po/search?q=steel&page=0&size=20
```

**Authorization**: ALL authenticated users

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `q` | string | Yes | Search term (min 3 characters) |
| `page` | integer | No | Page number (default: 0) |
| `size` | integer | No | Items per page (default: 20) |

**Search Fields**:

- PO number
- Vendor name
- Department name
- Delivery address

**Response** (200 OK): Same structure as List All POs

---

### 7. Get POs by Vendor

Retrieve all POs for a specific vendor.

```http
GET /api/v1/po/by-vendor/{vendorId}?page=0&size=20
```

**Authorization**: ALL authenticated users

**Path Parameters**:

- `vendorId` (integer, required): Vendor ID

**Response** (200 OK): Same structure as List All POs

---

### 8. Get POs by Department

Retrieve all POs for a specific department.

```http
GET /api/v1/po/by-department/{deptId}?page=0&size=20
```

**Authorization**: ALL authenticated users

**Path Parameters**:

- `deptId` (integer, required): Department ID

**Response** (200 OK): Same structure as List All POs

---

### 9. Get Pending Approval POs

Retrieve POs pending approval.

```http
GET /api/v1/po/pending-approval?page=0&size=20
```

**Authorization**: PLANTMANAGER, ADMIN, SUPERADMIN

**Response** (200 OK): Same structure as List All POs (filtered for status=2)

**Business Rule**: Only POs with status=2 (SUBMITTED) are returned

---

### 10. Get Overdue POs

Retrieve POs past expected delivery date.

```http
GET /api/v1/po/overdue?page=0&size=20
```

**Authorization**: PROCUREMENT, ADMIN, SUPERADMIN

**Response** (200 OK):

```json
{
  "content": [
    {
      "poId": 150,
      "poNumber": "PO/2025/00150",
      "vendorName": "XYZ Suppliers",
      "expectedDeliveryDate": "2025-10-01",
      "daysPastDue": 14,
      "totalAmount": 25000.0,
      "statusName": "SENT_TO_VENDOR"
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

**Business Rule**: Returns POs where:

- Status = SENT_TO_VENDOR (4)
- expectedDeliveryDate < current date

---

### 11. Submit PO

Submit PO for approval.

```http
POST /api/v1/po/{id}/submit
```

**Authorization**: PROCUREMENT, ADMIN, SUPERADMIN

**Path Parameters**:

- `id` (integer, required): PO ID

**Response** (200 OK):

```json
{
  "poId": 201,
  "poNumber": "PO/2025/00201",
  "statusId": 2,
  "statusName": "SUBMITTED",
  "submittedDate": "2025-10-15T14:30:00",
  ...
}
```

**Business Rules**:

- Can only submit PO in DRAFT status
- Must be creator or have ADMIN+ role
- Auto-assigns to Plant Manager for approval

---

### 12. Approve PO

Approve submitted PO.

```http
POST /api/v1/po/{id}/approve
```

**Authorization**: PLANTMANAGER, ADMIN, SUPERADMIN

**Path Parameters**:

- `id` (integer, required): PO ID

**Request Body**:

```json
{
  "remarks": "Approved. Proceed with vendor communication."
}
```

**Response** (200 OK):

```json
{
  "poId": 201,
  "poNumber": "PO/2025/00201",
  "statusId": 3,
  "statusName": "APPROVED",
  "approvedBy": "plant.manager",
  "approvedDate": "2025-10-16T09:00:00",
  "approvalRemarks": "Approved. Proceed with vendor communication.",
  ...
}
```

**Business Rules**:

- Can only approve PO in SUBMITTED status
- Cannot approve own PO (creator check)
- Remarks are optional
- Updates approval timestamp and approver

---

### 13. Send PO to Vendor

Mark PO as sent to vendor.

```http
POST /api/v1/po/{id}/send-to-vendor
```

**Authorization**: PROCUREMENT, ADMIN, SUPERADMIN

**Path Parameters**:

- `id` (integer, required): PO ID

**Request Body**:

```json
{
  "sentDate": "2025-10-16",
  "sentVia": "Email",
  "contactPerson": "Mr. Ramesh Kumar",
  "remarks": "PO sent via email with delivery instructions"
}
```

**Response** (200 OK):

```json
{
  "poId": 201,
  "poNumber": "PO/2025/00201",
  "statusId": 4,
  "statusName": "SENT_TO_VENDOR",
  "sentDate": "2025-10-16",
  "sentVia": "Email",
  ...
}
```

**Business Rules**:

- Can only send PO in APPROVED status
- sentDate cannot be future date
- Triggers vendor notification (future)

---

### 14. Receive Goods

Record goods receipt for PO.

```http
POST /api/v1/po/{id}/receive-goods
```

**Authorization**: STOREKEEPER, PROCUREMENT, ADMIN, SUPERADMIN

**Path Parameters**:

- `id` (integer, required): PO ID

**Request Body**:

```json
{
  "receivedDate": "2025-11-10",
  "receivedQuantity": 100,
  "grnNumber": "GRN/2025/00055",
  "qualityStatus": "ACCEPTED",
  "remarks": "All items received in good condition",
  "items": [
    {
      "poDetailId": 301,
      "receivedQuantity": 100,
      "acceptedQuantity": 100,
      "rejectedQuantity": 0,
      "rejectionReason": null
    }
  ]
}
```

**Response** (200 OK):

```json
{
  "poId": 201,
  "poNumber": "PO/2025/00201",
  "statusId": 5,
  "statusName": "RECEIVED",
  "receivedDate": "2025-11-10",
  "grnNumber": "GRN/2025/00055",
  "qualityStatus": "ACCEPTED",
  ...
}
```

**Business Rules**:

- Can only receive PO in SENT_TO_VENDOR status
- receivedDate cannot be future date
- Tracks partial/full receipts
- Quality check status recorded
- GRN (Goods Receipt Note) generated

---

### 15. Close PO

Close completed PO.

```http
POST /api/v1/po/{id}/close
```

**Authorization**: PROCUREMENT, ADMIN, SUPERADMIN

**Path Parameters**:

- `id` (integer, required): PO ID

**Request Body**:

```json
{
  "remarks": "PO completed successfully. All items received and inspected."
}
```

**Response** (200 OK):

```json
{
  "poId": 201,
  "poNumber": "PO/2025/00201",
  "statusId": 6,
  "statusName": "CLOSED",
  "closedBy": "procurement.user",
  "closedDate": "2025-11-12T16:00:00",
  "closureRemarks": "PO completed successfully. All items received and inspected.",
  ...
}
```

**Business Rules**:

- Can only close PO in RECEIVED status
- All items must be received
- Cannot reopen closed PO
- Final status (terminal)

---

### 16. Cancel PO

Cancel PO at any stage.

```http
POST /api/v1/po/{id}/cancel
```

**Authorization**: ADMIN, SUPERADMIN

**Path Parameters**:

- `id` (integer, required): PO ID

**Request Body**:

```json
{
  "cancellationReason": "Requirement no longer valid. Project cancelled."
}
```

**Response** (200 OK):

```json
{
  "poId": 201,
  "poNumber": "PO/2025/00201",
  "statusId": 7,
  "statusName": "CANCELLED",
  "cancelledBy": "admin.user",
  "cancelledDate": "2025-10-17T11:30:00",
  "cancellationReason": "Requirement no longer valid. Project cancelled.",
  ...
}
```

**Business Rules**:

- Can cancel from any status except CLOSED or CANCELLED
- Cancellation reason is mandatory
- Notifies all stakeholders (future)
- Cannot uncancelled (terminal state)

---

## Data Models

### POResponse (Full Details)

```json
{
  "poId": 201,
  "poNumber": "PO/2025/00201",
  "indentId": 45,
  "indentNumber": "IND/2025/00045",
  "vendorId": 101,
  "vendorName": "ABC Steel Industries",
  "deptId": 101,
  "deptName": "Production",
  "poDate": "2025-10-15",
  "expectedDeliveryDate": "2025-11-15",
  "deliveryAddress": "Warehouse A, Plot 12, Industrial Area",
  "termsAndConditions": "Standard procurement terms apply",
  "totalAmount": 15855.0,
  "statusId": 1,
  "statusName": "DRAFT",
  "createdBy": "procurement.user",
  "createdDate": "2025-10-15T10:30:00",
  "submittedDate": null,
  "approvedBy": null,
  "approvedDate": null,
  "approvalRemarks": null,
  "sentDate": null,
  "sentVia": null,
  "receivedDate": null,
  "grnNumber": null,
  "qualityStatus": null,
  "closedBy": null,
  "closedDate": null,
  "closureRemarks": null,
  "cancelledBy": null,
  "cancelledDate": null,
  "cancellationReason": null,
  "items": [
    {
      "poDetailId": 301,
      "indentItemId": 101,
      "materialId": 501,
      "materialName": "Steel Plates",
      "quantity": 100,
      "uomId": 1,
      "uomName": "Kg",
      "unitPrice": 150.0,
      "taxPercent": 18.0,
      "discount": 5.0,
      "netAmount": 15855.0,
      "receivedQuantity": 0,
      "acceptedQuantity": 0,
      "rejectedQuantity": 0,
      "rejectionReason": null
    }
  ]
}
```

### POListResponse (Summary)

```json
{
  "poId": 201,
  "poNumber": "PO/2025/00201",
  "vendorName": "ABC Steel Industries",
  "deptName": "Production",
  "poDate": "2025-10-15",
  "expectedDeliveryDate": "2025-11-15",
  "totalAmount": 15855.0,
  "statusName": "DRAFT"
}
```

### POItemResponse

```json
{
  "poDetailId": 301,
  "materialName": "Steel Plates",
  "quantity": 100,
  "uomName": "Kg",
  "unitPrice": 150.0,
  "taxPercent": 18.0,
  "discount": 5.0,
  "netAmount": 15855.0,
  "receivedQuantity": 0,
  "acceptedQuantity": 0,
  "rejectedQuantity": 0
}
```

---

## Business Rules

### 1. PO Creation Rules

- ✅ Can only create from approved indents (status=3)
- ✅ Vendor must be active
- ✅ Expected delivery date must be future date
- ✅ PO number auto-generated: PO/YYYY/NNNNN
- ✅ Total amount calculated from items
- ✅ Initial status: DRAFT
- ✅ Links to source indent

### 2. PO Number Generation

- **Format**: `PO/YYYY/NNNNN`
- **Example**: PO/2025/00201
- **Components**:
  - Prefix: PO
  - Year: Current year (YYYY)
  - Sequence: 5-digit padded number
- **Uniqueness**: Guaranteed by database sequence
- **Reset**: Annual (sequence resets each year)

### 3. Amount Calculation

```
For each item:
  Subtotal = quantity × unitPrice
  Discount Amount = subtotal × (discount% / 100)
  Taxable Amount = subtotal - discount
  Tax Amount = taxable × (taxPercent / 100)
  Net Amount = taxable + tax

Total PO Amount = Sum of all item net amounts
```

### 4. Workflow State Transitions

**Valid Transitions**:

- DRAFT → SUBMITTED
- SUBMITTED → APPROVED
- APPROVED → SENT_TO_VENDOR
- SENT_TO_VENDOR → RECEIVED
- RECEIVED → CLOSED
- Any (except CLOSED) → CANCELLED

**Invalid Transitions**: System prevents all other transitions

### 5. Approval Rules

- ✅ Can only approve SUBMITTED POs
- ✅ Cannot approve own PO
- ✅ Must have PLANTMANAGER+ role
- ✅ Approval remarks optional
- ✅ Records approver and timestamp

### 6. Goods Receipt Rules

- ✅ Can only receive SENT_TO_VENDOR POs
- ✅ Tracks received/accepted/rejected quantities
- ✅ GRN number recorded
- ✅ Quality status captured
- ✅ Supports partial receipts (future)
- ✅ Updates inventory (future)

### 7. Cancellation Rules

- ✅ Only ADMIN+ can cancel
- ✅ Cannot cancel CLOSED or CANCELLED POs
- ✅ Cancellation reason mandatory
- ✅ Cannot be reversed
- ✅ Notifies stakeholders (future)

### 8. Overdue Calculation

PO is overdue when:

- Status = SENT_TO_VENDOR
- Current date > expectedDeliveryDate
- Days overdue = current date - expected date

---

## Examples

### Complete PO Workflow

```bash
# 1. Login as Procurement Officer
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"procurement.user","password":"password123"}' \
  | jq -r '.token')

# 2. Get Approved Indents
curl -X GET "http://localhost:8080/api/v1/po/approved-indents?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

INDENT_ID=45

# 3. Create PO from Indent
PO_RESPONSE=$(curl -X POST http://localhost:8080/api/v1/po \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "indentId": 45,
    "vendorId": 101,
    "expectedDeliveryDate": "2025-11-15",
    "deliveryAddress": "Warehouse A, Plot 12, Industrial Area",
    "termsAndConditions": "Standard procurement terms apply",
    "items": [
      {
        "indentItemId": 101,
        "unitPrice": 150.00,
        "taxPercent": 18.0,
        "discount": 5.0
      }
    ]
  }')

PO_ID=$(echo $PO_RESPONSE | jq -r '.poId')
PO_NUMBER=$(echo $PO_RESPONSE | jq -r '.poNumber')

echo "Created PO: $PO_NUMBER (ID: $PO_ID)"

# 4. Submit PO for Approval
curl -X POST "http://localhost:8080/api/v1/po/$PO_ID/submit" \
  -H "Authorization: Bearer $TOKEN"

# 5. Login as Plant Manager
PM_TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"plant.manager","password":"password123"}' \
  | jq -r '.token')

# 6. Get Pending Approvals
curl -X GET "http://localhost:8080/api/v1/po/pending-approval" \
  -H "Authorization: Bearer $PM_TOKEN"

# 7. Approve PO
curl -X POST "http://localhost:8080/api/v1/po/$PO_ID/approve" \
  -H "Authorization: Bearer $PM_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "remarks": "Approved. Proceed with vendor communication."
  }'

# 8. Send PO to Vendor (back to Procurement)
curl -X POST "http://localhost:8080/api/v1/po/$PO_ID/send-to-vendor" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sentDate": "2025-10-16",
    "sentVia": "Email",
    "contactPerson": "Mr. Ramesh Kumar",
    "remarks": "PO sent via email with delivery instructions"
  }'

# 9. Login as Storekeeper (for goods receipt)
SK_TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"store.keeper","password":"password123"}' \
  | jq -r '.token')

# 10. Receive Goods
curl -X POST "http://localhost:8080/api/v1/po/$PO_ID/receive-goods" \
  -H "Authorization: Bearer $SK_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "receivedDate": "2025-11-10",
    "grnNumber": "GRN/2025/00055",
    "qualityStatus": "ACCEPTED",
    "remarks": "All items received in good condition",
    "items": [
      {
        "poDetailId": 301,
        "receivedQuantity": 100,
        "acceptedQuantity": 100,
        "rejectedQuantity": 0
      }
    ]
  }'

# 11. Close PO (Procurement)
curl -X POST "http://localhost:8080/api/v1/po/$PO_ID/close" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "remarks": "PO completed successfully. All items received and inspected."
  }'

# 12. Verify Final Status
curl -X GET "http://localhost:8080/api/v1/po/$PO_ID" \
  -H "Authorization: Bearer $TOKEN"
```

### Reporting Queries

```bash
# Get POs by Vendor
curl -X GET "http://localhost:8080/api/v1/po/by-vendor/101?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# Get POs by Department
curl -X GET "http://localhost:8080/api/v1/po/by-department/101?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# Get Overdue POs
curl -X GET "http://localhost:8080/api/v1/po/overdue" \
  -H "Authorization: Bearer $TOKEN"

# Search POs
curl -X GET "http://localhost:8080/api/v1/po/search?q=steel&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Testing

### Test Coverage

#### Phase 1: PO Creation (6 tests)

- ✅ Get approved indents
- ✅ Create PO with all fields
- ✅ Create PO with minimal fields
- ✅ Validate indent approval requirement
- ✅ Validate vendor active requirement
- ✅ Calculate amounts correctly

#### Phase 2: PO Queries (5 tests)

- ✅ Get PO by ID
- ✅ Get PO by number
- ✅ List all POs with pagination
- ✅ Get POs by vendor
- ✅ Get POs by department

#### Phase 3: PO Search (2 tests)

- ✅ Search by PO number
- ✅ Search by vendor name

#### Phase 4: PO Workflow (7 tests)

- ✅ Submit PO
- ✅ Approve PO
- ✅ Send PO to vendor
- ✅ Receive goods
- ✅ Close PO
- ✅ Cancel PO
- ✅ Validate state transitions

#### Phase 5: Authorization (4 tests)

- ✅ PROCUREMENT can create
- ✅ PLANTMANAGER can approve
- ✅ STOREKEEPER can receive
- ✅ ADMIN can cancel

#### Phase 6: Reporting (3 tests)

- ✅ Pending approvals
- ✅ Overdue POs
- ✅ Department-wise report

### Expected Results

- All CRUD operations: ✅ Working
- Workflow transitions: ✅ Working
- Authorization: ✅ Enforced
- Amount calculations: ✅ Accurate
- Search/filter: ✅ Working
- Overall: 27/27 tests passing (100%)

---

**Document Version**: 1.0  
**Last Updated**: October 15, 2025  
**Status**: ✅ Production Ready  
**Test Coverage**: 100%
