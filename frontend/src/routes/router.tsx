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
// Backend Role Reference (6 roles from tbl_roles_master):
// Backend Role Reference (9 roles from tbl_roles_master):
//   SUPERADMIN   – Full system access
//   ADMIN        – Administrative access (everything except role CRUD)
//   DEPTHEAD     – Department head, approves indents & issue notes
//   EMPLOYEE     – Creates indents, issue notes
//   PROCUREMENT  – PO management, vendor management
//   STOREKEEPER  – GRN, inventory, issue/receipt confirmation
//   PLANTMANAGER – Plant-level management
//   VIEWER       – Read-only access
//   AUDITOR      – Read-only + audit access
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
          <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'AUDITOR']}>
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
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'EMPLOYEE', 'DEPTHEAD', 'PROCUREMENT']}>
                <IndentsListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'approvals',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'DEPTHEAD']}>
                <IndentApprovalPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'new',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'EMPLOYEE']}>
                <IndentFormPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'EMPLOYEE', 'DEPTHEAD', 'PROCUREMENT']}>
                <IndentDetailPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/edit',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'EMPLOYEE']}>
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
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'QUALITY']}>
                <GRNListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'new',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'STOREKEEPER']}>
                <GRNFormPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'QUALITY']}>
                <GRNDetailPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/edit',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'QUALITY']}>
                <GRNFormPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/inspect',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'QUALITY']}>
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
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'EMPLOYEE', 'STOREKEEPER', 'DEPTHEAD']}>
                <IssueNotesListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'approvals',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'STOREKEEPER']}>
                <IssueNoteApprovalPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'new',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'EMPLOYEE']}>
                <IssueNoteFormPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'EMPLOYEE', 'STOREKEEPER', 'DEPTHEAD']}>
                <IssueNoteDetailPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/edit',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'EMPLOYEE']}>
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
            index: true,
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'STOREKEEPER']}>
                <InventoryListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/adjust',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'STOREKEEPER']}>
                <StockAdjustmentPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/history',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'STOREKEEPER']}>
                <TransactionHistoryPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'movements',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'STOREKEEPER']}>
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
      {
        path: 'vendors',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PROCUREMENT']}>
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
            path: ':id',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PROCUREMENT']}>
                <VendorDetailPage />
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
            element: (
              <ProtectedRoute roles={['SUPERADMIN']}>
                <MaterialFormPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
      // ==================== PLANT INDENT ROUTES ====================
      {
        path: 'plant-indent',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'STOREKEEPER']}>
                <PlantIndentListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'new',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PLANTMANAGER']}>
                <PlantIndentFormPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'STOREKEEPER']}>
                <PlantIndentDetailPage />
              </ProtectedRoute>
            ),
          },
          {
            path: ':id/edit',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'PLANTMANAGER']}>
                <PlantIndentFormPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
      // ==================== QUALITY CONTROL ROUTES ====================
      {
        path: 'quality-control',
        children: [
          {
            index: true,
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'QUALITY']}>
                <QualityControlListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'rejected',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'QUALITY', 'STOREKEEPER']}>
                <QCRejectedListPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
      // ==================== CONFIRMATIONS ROUTES ====================
      {
        path: 'confirmations',
        children: [
          {
            index: true,
            element: <Navigate to="/confirmations/issue" replace />,
          },
          {
            path: 'issue',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'DEPTHEAD']}>
                <IssueConfirmationPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'receipt',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'DEPTHEAD', 'EMPLOYEE']}>
                <ReceiptConfirmationPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
      {
        path: 'reports',
        element: (
          <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'AUDITOR']}>
            <ReportsPage />
          </ProtectedRoute>
        ),
      },
      {
        // Admin routes - system administration
        path: 'admin',
        children: [
          {
            index: true,
            element: <Navigate to="/admin/audit-logs" replace />,
          },
          {
            path: 'audit-logs',
            element: (
              <ProtectedRoute roles={['SUPERADMIN', 'ADMIN', 'AUDITOR']}>
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
      // ==================== MAPPING ROUTES ====================
      {
        path: 'mappings',
        children: [
          {
            index: true,
            element: <Navigate to="/mappings/company-departments" replace />,
          },
          {
            path: 'company-departments',
            element: (
              <ProtectedRoute roles={['SUPERADMIN']}>
                <CompanyDeptMappingPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'company-locations',
            element: (
              <ProtectedRoute roles={['SUPERADMIN']}>
                <CompanyLocationMappingPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'employee-roles',
            element: (
              <ProtectedRoute roles={['SUPERADMIN']}>
                <EmployeeRoleMappingPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'company-location-materials',
            element: (
              <ProtectedRoute roles={['SUPERADMIN']}>
                <CompanyLocationMaterialPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'reporting-hierarchy',
            element: (
              <ProtectedRoute roles={['SUPERADMIN']}>
                <ReportingHierarchyPage />
              </ProtectedRoute>
            ),
          },
          {
            path: 'plant-materials',
            element: (
              <ProtectedRoute roles={['SUPERADMIN']}>
                <CompanyPlantMaterialPage />
              </ProtectedRoute>
            ),
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
