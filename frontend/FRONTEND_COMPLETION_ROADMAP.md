# ProcureZone Frontend Completion Roadmap

## Executive Summary

This roadmap outlines the complete implementation plan for the ProcureZone React frontend, bridging the gap between the legacy JSP application and the modern React + TypeScript implementation.

---

## 1. Current State Analysis

### 1.1 Existing React Frontend Modules

| Module              | Status     | Completeness | Notes                                |
| ------------------- | ---------- | ------------ | ------------------------------------ |
| **Authentication**  | ✅ Working | 95%          | Login/logout functional              |
| **Dashboard**       | ⚠️ Partial | 60%          | Basic stats, needs charts            |
| **Indents**         | ⚠️ Partial | 70%          | List/view works, form incomplete     |
| **Purchase Orders** | ⚠️ Partial | 50%          | List works, detail/create incomplete |
| **GRN**             | ⚠️ Partial | 40%          | Basic list only                      |
| **Issue Notes**     | ✅ Fixed   | 70%          | List works, detail incomplete        |
| **Inventory**       | ⚠️ Partial | 30%          | Basic list only                      |
| **Materials**       | ⚠️ Partial | 60%          | List/form partial                    |
| **Vendors**         | ⚠️ Partial | 60%          | List/form partial                    |
| **Admin**           | ⚠️ Partial | 40%          | Hardcoded data, no API               |
| **Reports**         | ❌ Missing | 10%          | Placeholder only                     |
| **Profile**         | ⚠️ Partial | 50%          | Basic view only                      |

### 1.2 Legacy JSP Features (Gap Analysis)

#### Master Data Management (MISSING in React)

-   Company Master (CRUD + List + View)
-   Plant Master (CRUD + List + View)
-   Location Master (CRUD + List + View)
-   Department Master (CRUD + List + View)
-   Section Master (CRUD + List + View)
-   Employee Master (CRUD + List + View)
-   User Master (CRUD + Password Reset)
-   Roles Master (CRUD + Permissions)
-   Material Master (CRUD + Bulk Import)
-   UOM Master (CRUD)
-   Crop Master (CRUD)

#### Mapping Tables (MISSING in React)

-   Company-Department Mapping
-   Company-Location Mapping
-   Company-Location-Material Mapping
-   Company-Plant-Material Mapping
-   Employee Reporting Hierarchy
-   Employee-Role Assignment

#### Workflow Features (PARTIAL in React)

-   Indent Request Form (multi-line items)
-   Indent Approval (Dept Head view)
-   Indent Approval (RM view)
-   Indent Procurement Assignment
-   Indent Rejection with Remarks
-   Indent History Tracking

#### Goods Receipt (PARTIAL in React)

-   GRN Creation from PO
-   GRN Quality Check
-   GRN Approval Workflow
-   GRN Rejection

#### Issue Note (PARTIAL in React)

-   Issue Note Creation
-   Issue Note Approval
-   Material Return Processing
-   Stock Adjustment

---

## 2. Design System Standards

### 2.1 Color Palette (Bootstrap 5 + Custom)

```scss
// Primary Brand Colors
$primary: #2563eb; // Blue - Primary actions
$secondary: #64748b; // Slate - Secondary elements
$success: #22c55e; // Green - Success states
$danger: #ef4444; // Red - Errors/Alerts
$warning: #f59e0b; // Amber - Warnings
$info: #06b6d4; // Cyan - Information

// Background Colors
$bg-body: #f8fafc; // Light gray background
$bg-card: #ffffff; // White cards
$bg-sidebar: #1e293b; // Dark sidebar

// Text Colors
$text-primary: #1e293b; // Dark text
$text-secondary: #64748b; // Muted text
$text-muted: #94a3b8; // Light text
```

### 2.2 Typography

```scss
$font-family-base: "Inter", -apple-system, BlinkMacSystemFont, sans-serif;
$font-size-base: 0.875rem; // 14px
$font-size-sm: 0.75rem; // 12px
$font-size-lg: 1rem; // 16px
$headings-font-weight: 600;
```

### 2.3 Spacing System

```scss
$spacer: 1rem;
// xs: 0.25rem (4px)
// sm: 0.5rem (8px)
// md: 1rem (16px)
// lg: 1.5rem (24px)
// xl: 2rem (32px)
// xxl: 3rem (48px)
```

### 2.4 Component Standards

#### Cards

-   Rounded corners: 0.5rem
-   Shadow: 0 1px 3px rgba(0,0,0,0.1)
-   Padding: 1.25rem

#### Tables

-   Striped rows
-   Hover effect
-   Sticky headers for long lists
-   Pagination with 10/25/50 options

#### Forms

-   Floating labels for cleaner look
-   Inline validation with icons
-   Required field indicators (\*)
-   Helpful placeholder text

#### Buttons

-   Primary: Solid blue, white text
-   Secondary: Outline gray
-   Sizes: sm (28px), md (36px), lg (44px)
-   Icons with text for clarity

#### Status Badges

| Status     | Color  | Use Case           |
| ---------- | ------ | ------------------ |
| Draft      | Gray   | Unsaved/Initial    |
| Pending    | Yellow | Awaiting action    |
| Approved   | Green  | Completed approval |
| Rejected   | Red    | Declined           |
| Processing | Blue   | In progress        |
| Closed     | Dark   | Finalized          |

---

## 3. Implementation Phases

### Phase 1: Core Fixes & Foundation (Week 1-2)

#### 1.1 Fix Existing Bugs

-   [x] Issue Notes date formatting
-   [ ] PO list date handling
-   [ ] GRN list API integration
-   [ ] Inventory list API integration

#### 1.2 Enhance Error Handling

-   [ ] Global error boundary component
-   [ ] Toast notifications system
-   [ ] API error interceptor improvements
-   [ ] Offline detection

#### 1.3 Design System Setup

-   [ ] SCSS variables file
-   [ ] Custom Bootstrap theme
-   [ ] Icon library setup (React Icons)
-   [ ] Loading states standardization

### Phase 2: Master Data Modules (Week 3-5)

#### 2.1 Company Management

```
Pages: CompanyListPage, CompanyFormPage, CompanyViewPage
Features: CRUD, Status toggle, Search/Filter
```

#### 2.2 Plant Management

```
Pages: PlantListPage, PlantFormPage, PlantViewPage
Features: CRUD, Company association, Status toggle
```

#### 2.3 Location Management

```
Pages: LocationListPage, LocationFormPage, LocationViewPage
Features: CRUD, Search/Filter
```

#### 2.4 Department Management

```
Pages: DepartmentListPage, DepartmentFormPage, DepartmentViewPage
Features: CRUD, Head assignment, Status toggle
```

#### 2.5 Section Management

```
Pages: SectionListPage, SectionFormPage, SectionViewPage
Features: CRUD, Department association
```

#### 2.6 Employee Management

```
Pages: EmployeeListPage, EmployeeFormPage, EmployeeViewPage
Features: CRUD, Department assignment, Role assignment, Status toggle
```

#### 2.7 User Management

```
Pages: UserListPage, UserFormPage, UserViewPage
Features: CRUD, Password reset, Lock/Unlock, Role assignment
```

#### 2.8 Role Management

```
Pages: RoleListPage, RoleFormPage, RoleViewPage
Features: CRUD, Permission matrix, Employee count
```

#### 2.9 Material Management

```
Pages: MaterialListPage, MaterialFormPage, MaterialViewPage, MaterialImportPage
Features: CRUD, Bulk import (CSV), Category filter, Search
```

#### 2.10 UOM Management

```
Pages: UOMListPage, UOMFormPage
Features: CRUD, Simple list
```

### Phase 3: Mapping & Configuration (Week 6-7)

#### 3.1 Company-Department Mapping

```
Page: CompanyDepartmentMappingPage
Features: Multi-select assignment, Quick toggle
```

#### 3.2 Company-Location Mapping

```
Page: CompanyLocationMappingPage
Features: Multi-select assignment
```

#### 3.3 Material-Location Mapping

```
Page: MaterialLocationMappingPage
Features: Stock levels, Reorder points, Min/Max levels
```

#### 3.4 Employee Reporting Hierarchy

```
Page: ReportingHierarchyPage
Features: Tree view, Drag-drop assignment
```

#### 3.5 Employee-Role Assignment

```
Page: EmployeeRolesPage
Features: Multi-role assignment, Effective dates
```

### Phase 4: Core Workflow Completion (Week 8-10)

#### 4.1 Indent Module Enhancement

```
IndentFormPage:
- Multi-line item entry with material search
- Auto-calculate totals
- Save as draft
- Submit for approval
- File attachments

IndentApprovalPage:
- Dept Head approval queue
- RM approval queue
- Approve/Reject with remarks
- Request more info
- Approval history

IndentProcurementPage:
- Assign to procurement officer
- Split between vendors
- Create PO link
```

#### 4.2 Purchase Order Module

```
POFormPage:
- Create from approved indent
- Vendor selection with search
- Terms & conditions
- Delivery schedule
- Amendment support

POApprovalPage:
- Finance approval queue
- Approve/Reject workflow

POTrackingPage:
- Delivery tracking
- Partial receipt handling
```

#### 4.3 GRN Module

```
GRNFormPage:
- Create from PO
- Quantity verification
- Quality check entry
- Shortage/Excess handling

GRNApprovalPage:
- Quality approval
- Store acceptance
```

#### 4.4 Issue Note Module

```
IssueNoteFormPage:
- Material selection from stock
- Purpose entry
- Approver selection

IssueNoteApprovalPage:
- Manager approval
- Stock verification

ReturnNotePage:
- Return against issue note
- Condition assessment
```

### Phase 5: Reports & Analytics (Week 11-12)

#### 5.1 Dashboard Enhancement

```
Components:
- KPI cards with trends
- Indent status chart (doughnut)
- PO value trend (line)
- Top vendors (bar)
- Pending approvals widget
- Recent activity feed
```

#### 5.2 Report Pages

```
IndentReport:
- Date range filter
- Status filter
- Department filter
- Export to Excel/PDF

POReport:
- Vendor-wise summary
- Status-wise summary
- Value analysis

InventoryReport:
- Stock position
- Reorder alerts
- Movement history

AuditReport:
- User activity log
- Approval history
- Change tracking
```

### Phase 6: Polish & Optimization (Week 13-14)

#### 6.1 UX Improvements

-   [ ] Keyboard shortcuts
-   [ ] Bulk actions
-   [ ] Advanced search
-   [ ] Saved filters
-   [ ] Print layouts

#### 6.2 Performance

-   [ ] Code splitting
-   [ ] Lazy loading
-   [ ] Image optimization
-   [ ] API caching strategy

#### 6.3 Accessibility

-   [ ] ARIA labels
-   [ ] Focus management
-   [ ] Screen reader testing
-   [ ] Color contrast check

---

## 4. File Structure

```
frontend/src/
├── api/                    # API layer
│   ├── client.ts          # Axios instance
│   ├── auth.ts            # Auth endpoints
│   ├── indents.ts         # Indent endpoints
│   ├── purchaseOrders.ts  # PO endpoints
│   ├── grn.ts             # GRN endpoints
│   ├── issueNotes.ts      # Issue Note endpoints
│   ├── inventory.ts       # Inventory endpoints
│   ├── materials.ts       # Material endpoints
│   ├── vendors.ts         # Vendor endpoints
│   ├── masterData.ts      # Master data endpoints
│   ├── reports.ts         # Report endpoints
│   └── index.ts           # Barrel export
│
├── components/
│   ├── common/            # Shared components
│   │   ├── DataTable.tsx
│   │   ├── PageHeader.tsx
│   │   ├── StatusBadge.tsx
│   │   ├── LoadingSpinner.tsx
│   │   ├── ConfirmDialog.tsx
│   │   ├── ErrorAlert.tsx
│   │   ├── FormField.tsx       # NEW
│   │   ├── SearchInput.tsx     # NEW
│   │   ├── DatePicker.tsx      # NEW
│   │   ├── FileUpload.tsx      # NEW
│   │   └── index.ts
│   │
│   ├── layout/            # Layout components
│   │   ├── MainLayout.tsx
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   └── Footer.tsx
│   │
│   ├── forms/             # Form components (NEW)
│   │   ├── IndentForm/
│   │   ├── POForm/
│   │   ├── GRNForm/
│   │   └── IssueNoteForm/
│   │
│   └── charts/            # Chart components (NEW)
│       ├── KPICard.tsx
│       ├── StatusChart.tsx
│       └── TrendChart.tsx
│
├── contexts/
│   ├── AuthContext.tsx
│   └── ThemeContext.tsx    # NEW
│
├── hooks/                  # Custom hooks (NEW)
│   ├── useDebounce.ts
│   ├── useLocalStorage.ts
│   ├── usePagination.ts
│   └── useExport.ts
│
├── pages/
│   ├── admin/
│   │   ├── AdminPage.tsx
│   │   ├── UsersPage.tsx       # NEW
│   │   ├── RolesPage.tsx       # NEW
│   │   └── SettingsPage.tsx    # NEW
│   │
│   ├── masters/                # NEW MODULE
│   │   ├── companies/
│   │   ├── plants/
│   │   ├── locations/
│   │   ├── departments/
│   │   ├── sections/
│   │   ├── employees/
│   │   ├── materials/
│   │   └── uom/
│   │
│   ├── mappings/               # NEW MODULE
│   │   ├── CompanyDeptMapping/
│   │   ├── CompanyLocMapping/
│   │   ├── MaterialMapping/
│   │   └── EmployeeReporting/
│   │
│   ├── indents/
│   │   ├── IndentsListPage.tsx
│   │   ├── IndentDetailPage.tsx
│   │   ├── IndentFormPage.tsx
│   │   ├── IndentApprovalPage.tsx  # NEW
│   │   └── index.ts
│   │
│   ├── purchase-orders/
│   ├── grn/
│   ├── issue-notes/
│   ├── inventory/
│   ├── reports/                # ENHANCE
│   │   ├── ReportsPage.tsx
│   │   ├── IndentReport.tsx    # NEW
│   │   ├── POReport.tsx        # NEW
│   │   └── InventoryReport.tsx # NEW
│   │
│   └── dashboard/
│
├── styles/
│   ├── _variables.scss
│   ├── _mixins.scss
│   ├── _components.scss
│   ├── _utilities.scss
│   └── main.scss
│
├── types/                  # TypeScript types (NEW)
│   ├── api.ts
│   ├── models.ts
│   └── forms.ts
│
└── utils/                  # Utility functions
    ├── formatters.ts
    ├── validators.ts
    ├── constants.ts
    └── helpers.ts
```

---

## 5. Priority Order

### HIGH Priority (Week 1-4)

1. Fix all existing bugs (date formatting, API errors)
2. Error boundary & notifications
3. Master Data: Employee, User, Role management
4. Indent form with multi-line items
5. Indent approval workflow

### MEDIUM Priority (Week 5-8)

6. Master Data: Company, Plant, Department, Location
7. Purchase Order creation and approval
8. GRN creation and approval
9. Issue Note completion
10. Material management with import

### LOW Priority (Week 9-14)

11. Mapping tables
12. Reports & Analytics
13. Dashboard enhancement
14. UX polish & optimization
15. Accessibility compliance

---

## 6. API Checklist

| Endpoint           | Backend | Frontend API | UI Page |
| ------------------ | ------- | ------------ | ------- |
| `/auth/login`      | ✅      | ✅           | ✅      |
| `/auth/logout`     | ✅      | ✅           | ✅      |
| `/auth/me`         | ✅      | ✅           | ✅      |
| `/employees`       | ✅      | ⚠️           | ❌      |
| `/users`           | ✅      | ⚠️           | ❌      |
| `/roles`           | ✅      | ⚠️           | ❌      |
| `/companies`       | ✅      | ⚠️           | ❌      |
| `/plants`          | ✅      | ✅           | ❌      |
| `/departments`     | ✅      | ✅           | ❌      |
| `/locations`       | ✅      | ⚠️           | ❌      |
| `/materials`       | ✅      | ✅           | ⚠️      |
| `/vendors`         | ✅      | ✅           | ⚠️      |
| `/indents`         | ✅      | ✅           | ⚠️      |
| `/purchase-orders` | ✅      | ✅           | ⚠️      |
| `/grn`             | ✅      | ✅           | ⚠️      |
| `/issue-notes`     | ✅      | ✅           | ⚠️      |
| `/inventory`       | ✅      | ✅           | ⚠️      |

Legend: ✅ Complete | ⚠️ Partial | ❌ Missing

---

## 7. Testing Strategy

### Unit Tests

-   Component rendering
-   Hook behavior
-   Utility functions

### Integration Tests

-   API calls
-   Form submissions
-   Navigation flows

### E2E Tests (Cypress)

-   Login flow
-   Indent creation → approval → PO
-   GRN creation → inventory update

---

## 8. Next Steps

1. **Immediate**: Fix remaining date formatting issues across all pages
2. **This Week**: Implement error boundary and toast notifications
3. **Next Week**: Start Master Data modules (Employee, User, Role)
4. **Month 1**: Complete Phase 1 & 2
5. **Month 2**: Complete Phase 3 & 4
6. **Month 3**: Complete Phase 5 & 6

---

_Last Updated: December 5, 2025_
_Version: 1.0_
