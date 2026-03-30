# ProcureZone Frontend Development Plan

## 🎯 Overview

Complete React + Vite + Bootstrap frontend for the ProcureZone Procurement Management System.

---

## 📋 Backend API Integration Map

### Phase 1: Core Authentication & Navigation ✅

| Feature           | API Endpoints              | Status     |
| ----------------- | -------------------------- | ---------- |
| Login             | POST /auth/login           | ✅ Done    |
| Logout            | POST /auth/logout          | ✅ Done    |
| Token Refresh     | POST /auth/refresh         | ✅ Done    |
| Current User      | GET /auth/me               | ✅ Done    |
| Change Password   | POST /auth/change-password | 🔄 Pending |
| Protected Routes  | -                          | ✅ Done    |
| Role-based Access | -                          | ✅ Done    |

### Phase 2: Dashboard & Analytics 🔄

| Feature                 | API Endpoints                 | Status         |
| ----------------------- | ----------------------------- | -------------- |
| Dashboard Stats         | GET /dashboard/stats          | 🔄 In Progress |
| Pending Approvals Count | GET /indents/pending-approval | ✅ Done        |
| Low Stock Alerts        | GET /inventory/low-stock      | ✅ Done        |
| Recent Activity         | GET /activity/recent          | 🔄 Pending     |

### Phase 3: Indent Management

| Feature          | API Endpoints                 | Status     |
| ---------------- | ----------------------------- | ---------- |
| List Indents     | GET /indents                  | ✅ Done    |
| View Indent      | GET /indents/{id}             | 🔄 Pending |
| Create Indent    | POST /indents                 | 🔄 Pending |
| Edit Indent      | PUT /indents/{id}             | 🔄 Pending |
| Submit Indent    | POST /indents/{id}/submit     | 🔄 Pending |
| Approve Indent   | POST /indents/{id}/approve    | 🔄 Pending |
| Reject Indent    | POST /indents/{id}/reject     | 🔄 Pending |
| Cancel Indent    | POST /indents/{id}/cancel     | 🔄 Pending |
| My Indents       | GET /indents/my-indents       | 🔄 Pending |
| Pending Approval | GET /indents/pending-approval | 🔄 Pending |

### Phase 4: Purchase Order Management

| Feature            | API Endpoints                      | Status     |
| ------------------ | ---------------------------------- | ---------- |
| List POs           | GET /purchase-orders               | 🔄 Pending |
| View PO            | GET /purchase-orders/{id}          | 🔄 Pending |
| Create PO          | POST /purchase-orders              | 🔄 Pending |
| Create from Indent | POST /purchase-orders/from-indent  | 🔄 Pending |
| Confirm PO         | POST /purchase-orders/{id}/confirm | 🔄 Pending |
| Amend PO           | POST /purchase-orders/{id}/amend   | 🔄 Pending |
| Cancel PO          | POST /purchase-orders/{id}/cancel  | 🔄 Pending |
| Download PDF       | GET /purchase-orders/{id}/pdf      | 🔄 Pending |
| Email to Vendor    | POST /purchase-orders/{id}/email   | 🔄 Pending |

### Phase 5: GRN (Goods Receipt Note)

| Feature           | API Endpoints             | Status     |
| ----------------- | ------------------------- | ---------- |
| List GRNs         | GET /grn                  | 🔄 Pending |
| View GRN          | GET /grn/{id}             | 🔄 Pending |
| Create GRN        | POST /grn                 | 🔄 Pending |
| Submit GRN        | POST /grn/{id}/submit     | 🔄 Pending |
| QC Approve        | POST /grn/{id}/qc-approve | 🔄 Pending |
| QC Reject         | POST /grn/{id}/qc-reject  | 🔄 Pending |
| Post to Inventory | POST /grn/{id}/post       | 🔄 Pending |
| Pending QC        | GET /grn/pending-qc       | 🔄 Pending |

### Phase 6: Issue Notes

| Feature           | API Endpoints                  | Status     |
| ----------------- | ------------------------------ | ---------- |
| List Issue Notes  | GET /issue-notes               | 🔄 Pending |
| View Issue Note   | GET /issue-notes/{id}          | 🔄 Pending |
| Create Issue Note | POST /issue-notes              | 🔄 Pending |
| Approve           | POST /issue-notes/{id}/approve | 🔄 Pending |
| Issue Materials   | POST /issue-notes/{id}/issue   | 🔄 Pending |
| Return Materials  | POST /issue-notes/{id}/return  | 🔄 Pending |

### Phase 7: Inventory Management

| Feature            | API Endpoints                           | Status     |
| ------------------ | --------------------------------------- | ---------- |
| List Inventory     | GET /inventory                          | 🔄 Pending |
| Stock Details      | GET /inventory/plant/{id}/material/{id} | 🔄 Pending |
| Stock Transactions | GET /inventory/.../transactions         | 🔄 Pending |
| Adjust Stock       | POST /inventory/adjust                  | 🔄 Pending |
| Transfer Stock     | POST /inventory/transfer                | 🔄 Pending |
| Low Stock Report   | GET /inventory/low-stock                | 🔄 Pending |
| Export Report      | GET /inventory/export                   | 🔄 Pending |

### Phase 8: Master Data Management

| Feature          | API Endpoints               | Status     |
| ---------------- | --------------------------- | ---------- |
| Materials CRUD   | /materials/\*               | 🔄 Pending |
| Vendors CRUD     | /vendors/\*                 | 🔄 Pending |
| Plants CRUD      | /plants/\*                  | 🔄 Pending |
| Departments CRUD | /departments/\*             | 🔄 Pending |
| Categories CRUD  | /categories/\*              | 🔄 Pending |
| UOM CRUD         | /uom/\*                     | 🔄 Pending |
| Bulk Import      | POST /materials/bulk-import | 🔄 Pending |

### Phase 9: User & Role Management

| Feature        | API Endpoints          | Status     |
| -------------- | ---------------------- | ---------- |
| List Users     | GET /users             | 🔄 Pending |
| Create User    | POST /users            | 🔄 Pending |
| Update User    | PUT /users/{id}        | 🔄 Pending |
| Assign Roles   | POST /users/{id}/roles | 🔄 Pending |
| List Employees | GET /employees         | 🔄 Pending |

### Phase 10: Reports & Analytics

| Feature            | API Endpoints                | Status     |
| ------------------ | ---------------------------- | ---------- |
| Indent Report      | GET /reports/indents         | 🔄 Pending |
| PO Report          | GET /reports/purchase-orders | 🔄 Pending |
| GRN Report         | GET /reports/grn             | 🔄 Pending |
| Inventory Report   | GET /reports/inventory       | 🔄 Pending |
| Vendor Performance | GET /reports/vendors         | 🔄 Pending |

---

## 🎨 UI/UX Design Principles

### Responsive Breakpoints

-   **Mobile**: < 576px (xs)
-   **Tablet**: 576px - 992px (sm, md)
-   **Desktop**: 992px - 1200px (lg)
-   **Large Desktop**: > 1200px (xl, xxl)

### Design System

1. **Colors**: Bootstrap 5 color palette with custom primary (#0d6efd)
2. **Typography**: Segoe UI font family, clear hierarchy
3. **Spacing**: Consistent 8px grid system
4. **Shadows**: Subtle elevation for cards and modals
5. **Animations**: 0.2-0.3s transitions for smooth UX

### Component Library

-   **Forms**: react-hook-form with zod validation
-   **Tables**: Custom DataTable with pagination, sorting, filtering
-   **Modals**: Bootstrap modals for confirmations and forms
-   **Toasts**: React-hot-toast for notifications
-   **Icons**: React Icons (Font Awesome)

---

## 📱 Page Layouts

### 1. Authentication Pages

-   Login (full-page, centered card)
-   Forgot Password
-   Reset Password

### 2. Dashboard Layout

-   Sidebar navigation (collapsible)
-   Top navbar with user menu
-   Content area with responsive grid

### 3. List Pages Pattern

```
┌─────────────────────────────────────┐
│ Page Header + Breadcrumbs + Actions │
├─────────────────────────────────────┤
│ Search Bar + Filters                │
├─────────────────────────────────────┤
│ Data Table                          │
│ - Sortable columns                  │
│ - Row actions                       │
│ - Pagination                        │
└─────────────────────────────────────┘
```

### 4. Detail Pages Pattern

```
┌─────────────────────────────────────┐
│ Page Header + Status + Actions      │
├─────────────────────────────────────┤
│ Info Cards (2-3 columns)            │
├─────────────────────────────────────┤
│ Line Items Table                    │
├─────────────────────────────────────┤
│ Activity Timeline / History         │
└─────────────────────────────────────┘
```

### 5. Form Pages Pattern

```
┌─────────────────────────────────────┐
│ Page Header + Cancel/Save           │
├─────────────────────────────────────┤
│ Form Sections                       │
│ - Basic Info                        │
│ - Line Items (dynamic)              │
│ - Attachments                       │
├─────────────────────────────────────┤
│ Form Actions (Submit/Save Draft)    │
└─────────────────────────────────────┘
```

---

## 🔄 User Flow Diagrams

### Indent Workflow

```
Create Indent → Submit → L1 Approval → L2 Approval → Generate PO
     ↓                      ↓              ↓
   Draft               Rejected       Rejected
```

### PO Workflow

```
Create PO → Confirm → Receive GRN → QC Check → Post to Inventory
    ↓           ↓          ↓           ↓
  Draft     Cancelled   Partial    Rejected
```

### Issue Note Workflow

```
Create Request → Approve → Issue Materials → Close
      ↓            ↓            ↓
    Draft      Rejected     Returned
```

---

## 🚀 Implementation Priority

### Sprint 1 (Week 1) - Foundation ✅

-   [x] Project setup (Vite + React + TS)
-   [x] Bootstrap integration
-   [x] Authentication flow
-   [x] Layout components
-   [x] API client setup
-   [x] Protected routes

### Sprint 2 (Week 2) - Indents

-   [ ] Indent List (advanced filters)
-   [ ] Indent Detail Page
-   [ ] Indent Create/Edit Form
-   [ ] Indent Approval Flow
-   [ ] Indent PDF/Print

### Sprint 3 (Week 3) - Purchase Orders

-   [ ] PO List Page
-   [ ] PO Detail Page
-   [ ] PO Create Form
-   [ ] PO from Indent
-   [ ] PO Amendment
-   [ ] PO PDF/Email

### Sprint 4 (Week 4) - GRN & Issue Notes

-   [ ] GRN List & Detail
-   [ ] GRN Create (from PO)
-   [ ] QC Workflow
-   [ ] Issue Note List & Detail
-   [ ] Issue Note Create
-   [ ] Material Return

### Sprint 5 (Week 5) - Inventory & Reports

-   [ ] Inventory Dashboard
-   [ ] Stock Movements
-   [ ] Stock Adjustments
-   [ ] Reports Pages
-   [ ] Export Functionality

### Sprint 6 (Week 6) - Admin & Polish

-   [ ] User Management
-   [ ] Master Data CRUD
-   [ ] Bulk Import
-   [ ] Notifications
-   [ ] Performance Optimization

---

## 📁 Folder Structure

```
src/
├── api/                 # API client & services
│   ├── client.ts        # Axios instance
│   ├── auth.ts          # Auth API
│   ├── indents.ts       # Indents API
│   └── ...
├── components/
│   ├── common/          # Reusable components
│   │   ├── DataTable/
│   │   ├── PageHeader/
│   │   ├── StatusBadge/
│   │   └── ...
│   ├── layout/          # Layout components
│   │   ├── MainLayout/
│   │   ├── Sidebar/
│   │   └── TopNavbar/
│   └── forms/           # Form components
│       ├── IndentForm/
│       ├── POForm/
│       └── ...
├── contexts/            # React contexts
│   └── AuthContext.tsx
├── hooks/               # Custom hooks
│   ├── useAuth.ts
│   ├── usePagination.ts
│   └── useToast.ts
├── pages/               # Page components
│   ├── auth/
│   ├── dashboard/
│   ├── indents/
│   ├── purchase-orders/
│   └── ...
├── routes/              # Routing config
├── styles/              # Global styles
├── types/               # TypeScript types
└── utils/               # Utility functions
```

---

## ✅ Next Steps

1. **Continue with Sprint 2** - Build complete Indent module
2. Add toast notifications for user feedback
3. Implement PDF generation/preview
4. Add keyboard shortcuts for power users
5. Implement dark mode toggle
