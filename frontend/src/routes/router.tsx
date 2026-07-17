import React from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { MainLayout } from '../components/layout';
import ProtectedRoute from './ProtectedRoute';
import {
  LoginPage,
  DashboardPage,
  AnalyticsDashboard,
  IndentsListPage,
  IndentDetailPage,
  IndentFormPage,
  IndentApprovalPage,
  PlantIndentListPage,
  PlantIndentFormPage,
  PlantIndentDetailPage,
  POListPage,
  PODetailPage,
  POFormPage,
  POAmendmentPage,
  GRNListPage,
  GRNFormPage,
  GRNDetailPage,
  GRNInspectionPage,
  QualityControlListPage,
  QCRejectedListPage,
  IssueNotesListPage,
  IssueNoteFormPage,
  IssueNoteDetailPage,
  IssueNoteApprovalPage,
  IssueConfirmationPage,
  ReceiptConfirmationPage,
  InventoryListPage,
  InventoryStockPage,
  StockAdjustmentPage,
  TransactionHistoryPage,
  VendorsListPage,
  VendorFormPage,
  VendorDetailPage,
  MaterialsListPage,
  MaterialFormPage,
  MaterialDetailPage,
  MaterialBulkImportPage,
  ReportsPage,
  IndentReportPage,
  IssueNoteReportPage,
  AuditLogViewerPage,
  EmailTemplateManagerPage,
  ProfilePage,
  SettingsPage,
  NotFoundPage,
  // Master Data Pages
  MasterDataPage,
  CompaniesListPage,
  CompanyFormPage,
  PlantsListPage,
  PlantFormPage,
  LocationsListPage,
  LocationFormPage,
  DepartmentsListPage,
  DepartmentFormPage,
  SectionsListPage,
  SectionFormPage,
  UOMListPage,
  UOMFormPage,
  EmployeesListPage,
  EmployeeFormPage,
  EmployeeDetailPage,
  UsersListPage,
  UserFormPage,
  RolesListPage,
  RoleFormPage,
  CropMasterPage,
  // Mapping Pages
  CompanyDeptMappingPage,
  CompanyLocationMappingPage,
  EmployeeRoleMappingPage,
  CompanyLocationMaterialPage,
  ReportingHierarchyPage,
  CompanyPlantMaterialPage,
} from '../pages';

// ============================================================
// Normalized role codes — derived from tbl_roles_master.role_code via RoleNormalizer:
//   SUPERADMIN      ← "Super Admin"    Full system access
//   ADMIN           ← "Admin"          Administrative access
//   DEPTHEAD        ← "Department"     Department head, approves indents & issue notes
//   USER            ← "User"           Creates indents, issue notes (was EMPLOYEE)
//   PROCUREMENT     ← "Procurement"    PO management, vendor management
//   PLANTMANAGER    ← "Plant Manager"  Plant-level management
//   SUPERVISOR      ← "Supervisor"     RM-level approvals
//   FLOORINCHARGE   ← "FloorIncharge"  Plant floor, plant indent, inventory
//   GOODSINCHARGE   ← "GoodsIncharge"  GRN creation, goods receipt
//   GRNINCHARGE     ← "GRNIncharge"    GRN finalization, QC access
//   ISSUECONFIRM    ← "IssueConfirm"   Issue note issuance, confirmation
//   RECEIPTCONFIRM  ← "ReceiptConfirm" Receipt confirmation
//   QUALITYMANAGER  ← "QualityManager" QC inspection (was QUALITY + QUALITYMANAGER)
//   VIEWER          – Synthetic from canView=1  (read-only access)
//   DATAENTRYOPERATOR ← "DataEntry"    DEO operations
// ============================================================

const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <MainLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />,
      },
      {
        path: 'dashboard',
        element: <DashboardPage />,
      },
      {
        path: 'analytics',
        element: (
          <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'DEPTHEAD']}>
            <AnalyticsDashboard />
          </ProtectedRoute>
        ),
      },
      {
        path: 'indents',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'USER', 'DEPTHEAD', 'PROCUREMENT', 'SUPERVISOR']}>
                <IndentsListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'approvals',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'SUPERVISOR']}>
                <IndentApprovalPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'new',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'USER', 'SUPERVISOR']}>
                <IndentFormPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'USER', 'DEPTHEAD', 'PROCUREMENT', 'SUPERVISOR']}>
                <IndentDetailPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/edit',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'USER', 'SUPERVISOR']}>
                <IndentFormPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
      {
        path: 'purchase-orders',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PROCUREMENT']}>
                <POListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'new',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PROCUREMENT']}>
                <POFormPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PROCUREMENT']}>
                <PODetailPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/edit',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PROCUREMENT']}>
                <POFormPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/amend',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PROCUREMENT']}>
                <POAmendmentPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
      {
        path: 'grn',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'GOODSINCHARGE', 'GRNINCHARGE', 'QUALITYMANAGER']}>
                <GRNListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'new',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'GOODSINCHARGE', 'GRNINCHARGE']}>
                <GRNFormPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'GOODSINCHARGE', 'GRNINCHARGE', 'QUALITYMANAGER']}>
                <GRNDetailPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/edit',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'GOODSINCHARGE', 'GRNINCHARGE', 'QUALITYMANAGER']}>
                <GRNFormPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/inspect',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'QUALITYMANAGER']}>
                <GRNInspectionPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
      {
        path: 'issue-notes',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'USER', 'ISSUECONFIRM', 'DEPTHEAD', 'SUPERVISOR', 'PROCUREMENT']}>
                <IssueNotesListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'approvals',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'ISSUECONFIRM', 'SUPERVISOR']}>
                <IssueNoteApprovalPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'new',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'USER', 'SUPERVISOR', 'DEPTHEAD']}>
                <IssueNoteFormPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'USER', 'ISSUECONFIRM', 'DEPTHEAD', 'SUPERVISOR', 'PROCUREMENT']}>
                <IssueNoteDetailPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/edit',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'USER', 'SUPERVISOR', 'DEPTHEAD']}>
                <IssueNoteFormPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
      {
        path: 'inventory',
        children: [
          {
            // Read-only stock view — operational roles. The editable inventory-management pages
            // (adjust/history/movements below) remain restricted to their management roles.
            index: true,
            element: (
              <ProtectedRoute roles={['USER', 'SUPERVISOR', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN', 'PROCUREMENT']}>
                <InventoryStockPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/adjust',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'GOODSINCHARGE', 'GRNINCHARGE']}>
                <StockAdjustmentPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/history',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'FLOORINCHARGE', 'GOODSINCHARGE']}>
                <TransactionHistoryPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'movements',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'FLOORINCHARGE', 'GOODSINCHARGE']}>
                <TransactionHistoryPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'reports',
            element: (
              <ProtectedRoute roles={['SUPERADMIN']}>
                <InventoryListPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
      // Vendor routes - redirect to masters
      {
        path: 'vendors',
        children: [
          {
            index: true,
            element: <Navigate to="/masters/vendors" replace />,
          },
          {
            path: 'new',
            element: <Navigate to="/masters/vendors/new" replace />,
          },
          {
            path: ':id',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PROCUREMENT']}>
                <VendorDetailPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/edit',
            element: <Navigate to="/masters/vendors" replace />,
          },
        ],
      },
      // Materials routes - redirect to masters
      {
        path: 'materials',
        children: [
          {
            index: true,
            element: <Navigate to="/masters/materials" replace />,
          },
          {
            path: 'new',
            element: <Navigate to="/masters/materials/new" replace />,
          },
          {
            path: 'import',
            element: (
              <ProtectedRoute roles={['SUPERADMIN']}>
                <MaterialBulkImportPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id',
            element: (
              <ProtectedRoute roles={['SUPERADMIN']}>
                <MaterialDetailPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/edit',
            element: <Navigate to="/masters/materials" replace />,
          },
        ],
      },
      // ==================== PLANT INDENT ROUTES (DORMANT — hidden from all roles) ==============
      // Module is dormant: every route redirects to /dashboard so old bookmarked URLs resolve
      // predictably and no user (incl. ADMIN/SUPERADMIN) can reach the pages. Routes stay
      // registered and the page components (PlantIndent*Page) are preserved for re-enable —
      // restore the ProtectedRoute elements to bring it back.
      {
        path: 'plant-indent',
        children: [
          { index: true, element: <Navigate to="/dashboard" replace /> },
          { path: 'new', element: <Navigate to="/dashboard" replace /> },
          { path: ':id', element: <Navigate to="/dashboard" replace /> },
          { path: ':id/edit', element: <Navigate to="/dashboard" replace /> },
        ],
      },
      // ==================== QUALITY CONTROL ROUTES ====================
      {
        path: 'quality-control',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'QUALITYMANAGER']}>
                <QualityControlListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'rejected',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'QUALITYMANAGER', 'GOODSINCHARGE', 'GRNINCHARGE']}>
                <QCRejectedListPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
      // ==================== CONFIRMATIONS ROUTES (DORMANT — hidden from all roles) ============
      // Dormant placeholder module: every route redirects to /dashboard so no user reaches the
      // pages and old bookmarks resolve predictably. Routes stay registered and the page
      // components (IssueConfirmationPage/ReceiptConfirmationPage) are preserved for re-enable.
      {
        path: 'confirmations',
        children: [
          { index: true, element: <Navigate to="/dashboard" replace /> },
          { path: 'issue', element: <Navigate to="/dashboard" replace /> },
          { path: 'receipt', element: <Navigate to="/dashboard" replace /> },
        ],
      },
      {
        path: 'reports',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'SUPERVISOR']}>
                <ReportsPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'indents',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'SUPERVISOR']}>
                <IndentReportPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'issue-notes',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'SUPERVISOR']}>
                <IssueNoteReportPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
      // Standalone admin pages (no longer nested under /admin group)
      {
        path: 'audit-logs',
        element: (
          <ProtectedRoute roles={['SUPERADMIN', 'ADMIN']}>
            <AuditLogViewerPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'email-templates',
        element: (
          <ProtectedRoute roles={['SUPERADMIN', 'ADMIN']}>
            <EmailTemplateManagerPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'material-import',
        element: (
          <ProtectedRoute roles={['SUPERADMIN', 'ADMIN']}>
            <MaterialBulkImportPage />
          </ProtectedRoute>
        ),
      },
      {
        // Legacy /admin/* redirects for bookmarked links
        path: 'admin',
        children: [
          {
            index: true,
            element: <Navigate to="/audit-logs" replace />,
          },
          {
            path: 'audit-logs',
            element: <Navigate to="/audit-logs" replace />,
          },
          {
            path: 'email-templates',
            element: <Navigate to="/email-templates" replace />,
          },
          {
            path: 'users',
            element: <Navigate to="/masters/users" replace />,
          },
        ],
      },
      {
        path: 'profile',
        element: <ProfilePage />,
      },
      {
        path: 'settings',
        element: <SettingsPage />,
      },
      {
        path: 'change-password',
        element: <Navigate to="/profile" replace />, // Password change is in profile page
      },
      // ==================== MASTER DATA ROUTES ====================
      {
        path: 'masters',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN']}>
                <MasterDataPage />
              </ProtectedRoute>
            ),
          },
          // Companies
          {
            path: 'companies',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <CompaniesListPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'new',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <CompanyFormPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id/edit',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <CompanyFormPage />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          // Plants
          {
            path: 'plants',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <PlantsListPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'new',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <PlantFormPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id/edit',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <PlantFormPage />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          // Locations
          {
            path: 'locations',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <LocationsListPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'new',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <LocationFormPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id/edit',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <LocationFormPage />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          // Departments
          {
            path: 'departments',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <DepartmentsListPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'new',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <DepartmentFormPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id/edit',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <DepartmentFormPage />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          // Sections
          {
            path: 'sections',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <SectionsListPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'new',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <SectionFormPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id/edit',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <SectionFormPage />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          // UOM (Unit of Measure)
          {
            path: 'uom',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <UOMListPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'new',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <UOMFormPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id/edit',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <UOMFormPage />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          // Employees
          {
            path: 'employees',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <EmployeesListPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'new',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <EmployeeFormPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <EmployeeDetailPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id/edit',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <EmployeeFormPage />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          // Users
          {
            path: 'users',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <UsersListPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'new',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <UserFormPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id/edit',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <UserFormPage />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          // Roles
          {
            path: 'roles',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <RolesListPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'new',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <RoleFormPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id/edit',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <RoleFormPage />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          // Crops Master
          {
            path: 'crops',
            element: (
              <ProtectedRoute roles={['SUPERADMIN']}>
                <CropMasterPage />
              </ProtectedRoute>
            ),
          },
          // Materials (in masters)
          {
            path: 'materials',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <MaterialsListPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'new',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <MaterialFormPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id/edit',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN']}>
                    <MaterialFormPage />
                  </ProtectedRoute>
                ),
              },
            ],
          },
          // Vendors (in masters)
          {
            path: 'vendors',
            children: [
              {
                index: true,
                element: (
                  <ProtectedRoute roles={['SUPERADMIN', 'PROCUREMENT']}>
                    <VendorsListPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: 'new',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN', 'PROCUREMENT']}>
                    <VendorFormPage />
                  </ProtectedRoute>
                ),
              },
              {
                path: ':id/edit',
                element: (
                  <ProtectedRoute roles={['SUPERADMIN', 'PROCUREMENT']}>
                    <VendorFormPage />
                  </ProtectedRoute>
                ),
              },
            ],
          },
        ],
      },
      // ==================== MAPPING REDIRECTS (backward compatibility) ====================
      {
        path: 'mappings',
        children: [
          {
            index: true,
            element: <Navigate to="/masters/companies?tab=department-mapping" replace />,
          },
          {
            path: 'company-departments',
            element: <Navigate to="/masters/companies?tab=department-mapping" replace />,
          },
          {
            path: 'company-locations',
            element: <Navigate to="/masters/companies?tab=location-mapping" replace />,
          },
          {
            path: 'employee-roles',
            element: <Navigate to="/masters/employees?tab=role-mapping" replace />,
          },
          {
            path: 'company-location-materials',
            element: <Navigate to="/masters/locations?tab=material-mapping" replace />,
          },
          {
            path: 'reporting-hierarchy',
            element: <Navigate to="/masters/employees?tab=reporting-hierarchy" replace />,
          },
          {
            path: 'plant-materials',
            element: <Navigate to="/masters/plants?tab=material-mapping" replace />,
          },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <NotFoundPage />,
  },
]);

export default router;
