export { default as apiClient, getErrorMessage } from "./client";
export type { ApiError } from "./client";

export { authApi } from "./auth";
export type {
    LoginRequest,
    LoginResponse,
    UserInfo,
    ChangePasswordRequest,
} from "./auth";

export { materialsApi } from "./materials";
export type {
    Material,
    MaterialCreateRequest,
    MaterialUpdateRequest,
    MaterialSearchParams,
    PageRequest,
    PageResponse,
} from "./materials";

// Master Data APIs
export { companiesApi } from "./companies";
export type {
    Company,
    CompanyCreateRequest,
    CompanyUpdateRequest,
} from "./companies";

export { plantsApi } from "./plants";
export type { Plant, PlantCreateRequest, PlantUpdateRequest } from "./plants";

export { locationsApi } from "./locations";
export type {
    Location,
    LocationCreateRequest,
    LocationUpdateRequest,
} from "./locations";

export { departmentsApi } from "./departments";
export type {
    Department,
    DepartmentCreateRequest,
    DepartmentUpdateRequest,
} from "./departments";

export { sectionsApi } from "./sections";
export type {
    Section,
    SectionCreateRequest,
    SectionUpdateRequest,
} from "./sections";

export { uomApi } from "./uom";
export type { UOM, UOMCreateRequest, UOMUpdateRequest } from "./uom";

export { employeesApi } from "./employees";
export type {
    Employee,
    EmployeeCreateRequest,
    EmployeeUpdateRequest,
} from "./employees";

export { usersApi } from "./users";
export type { User, UserCreateRequest, UserUpdateRequest } from "./users";

export { rolesApi } from "./roles";
export type {
    Role,
    RoleCreateRequest,
    RoleUpdateRequest,
    EmployeeRole,
    EmployeeRoleAssignRequest,
} from "./roles";

export { indentsApi, IndentStatus } from "./indents";
export type {
    Indent,
    IndentItem,
    IndentCreateRequest,
    IndentItemCreateRequest,
    IndentSearchParams,
    ApproveIndentRequest,
    RejectIndentRequest,
    CancelIndentRequest,
    IndentItemSummary,
} from "./indents";

export { plantIndentsApi } from "./plantIndents";
export type {
    PlantIndent as PlantIndentType,
    PlantIndentDetail as PlantIndentDetailType,
    PlantIndentCreateRequest,
    PlantIndentDetailCreateRequest,
} from "./plantIndents";

export {
    purchaseOrdersApi,
    purchaseOrdersApi as poApi,
    POStatus,
} from "./purchaseOrders";
export type {
    PurchaseOrder,
    PODetail,
    POSummaryResponse,
    POAmendment,
    POAmendmentHistory,
    POCreateRequest,
    POLineItemRequest,
    UpdatePORequest,
    AmendPORequest,
    POSearchParams,
    ApprovedIndentDTO,
} from "./purchaseOrders";

export { grnApi, GRNStatus } from "./grn";
export type {
    GRN,
    GRNItem,
    GRNCreateRequest,
    GRNItemCreateRequest,
    GRNSearchParams,
    QCApproveRequest,
    QCRejectRequest,
} from "./grn";

export { issueNotesApi, IssueNoteStatus } from "./issueNotes";
export type {
    IssueNote,
    IssueNoteItem,
    IssueNoteCreateRequest,
    IssueNoteLineItemCreateRequest,
    IssueNoteSearchParams,
    IssueItemsRequest,
    ReturnIssueNoteRequest,
    IssueNoteItemSummary,
} from "./issueNotes";

export { inventoryApi } from "./inventory";
export type {
    Inventory,
    InventoryTransaction,
    InventorySearchParams,
    StockAdjustmentRequest,
    StockTransferRequest,
} from "./inventory";

export { vendorsApi } from "./vendors";
export type {
    Vendor,
    VendorCategory,
    VendorCreateRequest,
    VendorSearchParams,
} from "./vendors";

export { masterDataApi } from "./masterData";
export type {
    UnitOfMeasure,
    MaterialCategory,
    ApprovalLevel,
} from "./masterData";
// Note: Plant and Department are exported from ./plants and ./departments respectively

export { userApi } from "./user";
export type { UserProfile, UpdateProfileRequest } from "./user";

export { reportsApi } from "./reports";
export type {
    ReportParams,
    ReportSummary,
    IndentReportItem,
    POReportItem,
    InventoryReportItem,
    VendorPerformanceItem,
} from "./reports";

export { dashboardApi } from "./dashboard";
export type {
    DashboardStatistics,
    DashboardSummary,
    MonthlyTrend,
    DepartmentBreakdown,
    TopMaterial,
    PendingApprovalItem,
    SystemAlert,
} from "./dashboard";

export { adminApi } from "./admin";
export type { SystemConfig, AuditLog, SystemStats, BackupInfo } from "./admin";

export { moduleAccessApi } from "./moduleAccess";
export type {
    ModuleDefinition,
    EmpModuleEntry,
    ModuleUpdate,
} from "./moduleAccess";

export { notificationsApi } from "./notifications";
export type {
    NotificationItem,
    NotificationPage,
} from "./notifications";

// Extended list response types with statistics
export type {
    MaterialsListResponse,
    VendorsListResponse,
    IssueNotesListResponse,
    GRNListResponse,
    POListResponse,
    IndentsListResponse,
    InventoryListResponse,
} from "./types";
