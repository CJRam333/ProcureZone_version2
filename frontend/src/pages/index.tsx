// Auth pages
export { LoginPage } from './auth';

// Dashboard
export { DashboardPage } from './dashboard';

// Analytics
export { AnalyticsDashboard } from './analytics';

// Indents
export { IndentsListPage, IndentDetailPage, IndentFormPage, IndentApprovalPage } from './indents';

// Plant Indent
export { PlantIndentListPage, PlantIndentFormPage, PlantIndentDetailPage } from './plant-indent';

// Purchase Orders
export { POListPage, PODetailPage, POFormPage, POAmendmentPage } from './purchase-orders';

// GRN
export { GRNListPage, GRNFormPage, GRNDetailPage, GRNInspectionPage } from './grn';

// Quality Control
export { QualityControlListPage, QCRejectedListPage } from './quality-control';

// Issue Notes
export { IssueNotesListPage, IssueNoteFormPage, IssueNoteDetailPage, IssueNoteApprovalPage } from './issue-notes';

// Confirmations
export { IssueConfirmationPage, ReceiptConfirmationPage } from './confirmations';

// Inventory
export { InventoryListPage, StockAdjustmentPage, TransactionHistoryPage } from './inventory';

// Vendors (legacy routes - keep for backward compatibility)
export { VendorsListPage, VendorFormPage, VendorDetailPage } from './vendors';

// Materials (legacy routes - keep for backward compatibility)
export { MaterialsListPage, MaterialFormPage, MaterialDetailPage, MaterialBulkImportPage } from './materials';

// Reports
export { ReportsPage } from './reports';

// Admin
export { AdminPage, AuditLogViewerPage, EmailTemplateManagerPage } from './admin';

// Profile & Settings
export { ProfilePage, SettingsPage } from './profile';

// Master Data Pages
export {
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
  MaterialsListPage as MasterMaterialsListPage,
  MaterialFormPage as MasterMaterialFormPage,
  VendorsListPage as MasterVendorsListPage,
  VendorFormPage as MasterVendorFormPage,
} from './masters';

// Mapping Pages
export {
  CompanyDeptMappingPage,
  CompanyLocationMappingPage,
  EmployeeRoleMappingPage,
  CompanyLocationMaterialPage,
  ReportingHierarchyPage,
  CompanyPlantMaterialPage,
} from './mappings';

// Not Found page
export const NotFoundPage = () => (
  <div className="text-center py-5">
    <h1 className="display-1 text-muted">404</h1>
    <h3>Page Not Found</h3>
    <p className="text-muted">The page you're looking for doesn't exist or has been moved.</p>
    <a href="/dashboard" className="btn btn-primary">Go to Dashboard</a>
  </div>
);

