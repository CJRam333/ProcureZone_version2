import apiClient from "./client";
import { PageRequest, PageResponse } from "./materials";

// ============== Types ==============
export interface Indent {
    id: number;
    indentNumber: string;
    indentDate: string;
    indentYear?: string;
    companyId?: number;
    companyName?: string;
    departmentId?: number;
    departmentName?: string;
    sectionId?: number;
    sectionName?: string;
    plantId?: number;
    plantName?: string;
    employeeId?: number;
    employeeName?: string;
    comments?: string;
    deliveryDate?: string;
    poNumber?: string;
    createdById?: number;
    createdByName?: string;
    approvedById?: number;
    approvedByName?: string;
    approvedByDate?: string;
    finalApprovedById?: number;
    finalApprovedByName?: string;
    finalApprovedDate?: string;
    procurementById?: number;
    procurementByName?: string;
    remarks?: string;
    finalRemarks?: string;
    statusId?: number;
    statusName?: string;
    approvedStatusId?: number;
    approvedStatusName?: string;
    finalStatusId?: number;
    finalStatusName?: string;
    procurementStatusId?: number;
    procurementStatusName?: string;
    lastModifiedDate?: string;
    lastModifiedBy?: number;
    displayStatus?: string;
    details?: IndentItem[];
    // List endpoint fields
    detailsCount?: number;
    items?: IndentItemSummary[]; // compact per-line summary for the list-page Items hover-preview
    // Legacy aliases for backward compatibility
    requestedBy?: number;
    requestedByName?: string;
    purpose?: string;
    status?: IndentStatus;
    priority?: "LOW" | "MEDIUM" | "HIGH" | "URGENT";
    requiredDate?: string;
    approvalLevel?: number;
    approvedAt?: string;
    rejectionReason?: string;
    cancellationReason?: string;
    totalEstimatedValue?: number;
    createdAt?: string;
    updatedAt?: string;
}

// Compact line-item summary returned by the indent LIST endpoint (IndentListResponse.ItemSummary)
// — powers the Items hover-preview without a per-row detail fetch.
export interface IndentItemSummary {
    materialName?: string;
    companies?: string; // comma-separated company names stocking this material
    uomCode?: string;
    quantity?: number;
}

export enum IndentStatus {
    DRAFT = 1,
    SUBMITTED = 2,
    DEPT_HEAD_APPROVED = 3,    // Dept Head / L2 approved
    REJECTED = 4,              // Rejected at any stage (legacy DB ID 4 = REJECTED)
    PROCUREMENT_APPROVED = 5,  // Proc. In Progress — finalApproveIndent()
    PO_CREATED = 6,            // PO Created — procurementApproveIndent() (legacy DB ID 6 = PO_CREATED)
    ON_HOLD = 7,
    COMPLETED = 8,
}

// One entry in a line item's quantity-edit history (from the backend audit trail).
export interface QuantityEdit {
    stage: string;            // 'RM' | 'DEPTHEAD'
    oldQuantity?: number;
    newQuantity: number;
    editedByName?: string;
    editedAt?: string;
}

export interface IndentItem {
    id: number;
    indentId: number;
    materialId: number;
    materialCode: string;
    materialName: string;
    unitOfMeasureId: number;
    unitOfMeasureCode: string;
    unitOfMeasureName: string;
    quantity: number;               // requester's ORIGINAL quantity
    rmQuantity?: number;            // RM (L1) adjustment
    deptQuantity?: number;          // DeptHead (L2) adjustment
    currentEffectiveQuantity?: number; // deptQuantity ?? rmQuantity ?? quantity (server-computed)
    quantityHistory?: QuantityEdit[];  // per-edit trail, oldest first (empty if never adjusted)
    stockAvailable?: number;
    pricing?: number;
    purpose?: string;
    vendor?: string;
    status: number;
    companies?: string; // comma-separated company names stocking this material
    currentStock?: number; // aggregated current stock across companies/plants
    // Optional aliases for UI compatibility
    materialDescription?: string; // alias for materialName
    uomCode?: string; // alias for unitOfMeasureCode
    requestedQuantity?: number; // alias for quantity
    estimatedRate?: number; // alias for pricing
    remarks?: string; // alias for purpose
}

export interface IndentCreateRequest {
    // company / department / plant / section captured server-side from the employee record — optional
    companyId?: number;
    departmentId?: number;
    plantId?: number;
    employeeId: number;
    sectionId?: number;
    comments?: string;
    deliveryDate?: string | null;
    details: IndentDetailCreateRequest[];
}

export interface IndentDetailCreateRequest {
    materialId: number;
    unitOfMeasureId: number;
    quantity: number;
    rmQuantity?: number;
    deptQuantity?: number;
    stockAvailable?: number;
    pricing?: number;
    purpose?: string;
    vendor?: string;
    status?: number;
}

// Legacy interface for backward compatibility
export interface IndentItemCreateRequest {
    materialId: number;
    unitOfMeasureId: number;
    quantity: number;
    pricing?: number;
    purpose?: string;
}

export interface IndentSearchParams extends PageRequest {
    search?: string;
    status?: IndentStatus;
    departmentId?: number;
    plantId?: number;
    companyId?: number;
    priority?: string;
    fromDate?: string;
    toDate?: string;
    approvedStatus?: number;
    finalStatus?: number;
    procurementStatus?: number;
}

export interface ApproveIndentRequest {
    items?: { itemId: number; approvedQuantity: number }[];
    remarks?: string;
}

export interface RejectIndentRequest {
    reason: string;
}

export interface CancelIndentRequest {
    reason: string;
}

export interface IndentFormMeta {
    empNumber: number;
    empName: string;
    empId: string;
    departmentId: number | null;
    departmentName: string | null;
    companies: { id: number; name: string }[];
    defaultCompanyId: number | null;
    locationId: number | null;
    locationName: string | null;
    financialYear: string;
    date: string;
    nextIndentNumber: string;
}

// ============== API Functions ==============

export const indentsApi = {
    list: async (
        params: IndentSearchParams = {}
    ): Promise<PageResponse<Indent>> => {
        const response = await apiClient.get<PageResponse<Indent>>("/indents", {
            params,
        });
        return response.data;
    },

    export: async (params: Omit<IndentSearchParams, 'page' | 'size'> & { format?: 'excel' | 'csv' }): Promise<Blob> => {
        const response = await apiClient.get("/indents/export", {
            params,
            responseType: 'blob',
        });
        return response.data;
    },

    getById: async (id: number): Promise<Indent> => {
        const response = await apiClient.get<Indent>(`/indents/${id}`);
        return response.data;
    },

    getByNumber: async (indentNumber: string): Promise<Indent> => {
        const response = await apiClient.get<Indent>(
            `/indents/number/${indentNumber}`
        );
        return response.data;
    },

    create: async (data: IndentCreateRequest): Promise<Indent> => {
        const response = await apiClient.post<Indent>("/indents", data);
        return response.data;
    },

    update: async (
        id: number,
        data: Partial<IndentCreateRequest>
    ): Promise<Indent> => {
        const response = await apiClient.put<Indent>(`/indents/${id}`, data);
        return response.data;
    },

    submit: async (id: number): Promise<Indent> => {
        const response = await apiClient.post<Indent>(`/indents/${id}/submit`);
        return response.data;
    },

    // --- Approval Workflow (uses /approvals/indents/ base path) ---
    approve: async (
        id: number,
        data?: ApproveIndentRequest
    ): Promise<Indent> => {
        const response = await apiClient.post<Indent>(
            `/approvals/indents/${id}/approve`,
            null,
            { params: { remarks: data?.remarks || '' } }
        );
        return response.data;
    },

    reject: async (id: number, data: RejectIndentRequest): Promise<Indent> => {
        const response = await apiClient.post<Indent>(
            `/approvals/indents/${id}/reject`,
            null,
            { params: { remarks: data.reason } }
        );
        return response.data;
    },

    // --- Pass 3: direct L1/L2 approval carrying per-line quantity adjustments ---
    // These hit the indent endpoints (NOT the remarks-only smart-router) so RM/DeptHead
    // quantity edits + audit trail are recorded. `adjustments` may be omitted for approve-as-is.
    l1Approve: async (
        id: number,
        data: { remarks?: string; adjustments?: { detailId: number; rmQuantity: number }[] }
    ): Promise<Indent> => {
        const response = await apiClient.post<Indent>(`/indents/${id}/l1-approve`, {
            remarks: data.remarks ?? '',
            adjustments: data.adjustments ?? [],
        });
        return response.data;
    },

    l2Approve: async (
        id: number,
        data: { remarks?: string; adjustments?: { detailId: number; deptQuantity: number }[] }
    ): Promise<Indent> => {
        const response = await apiClient.post<Indent>(`/indents/${id}/l2-approve`, {
            remarks: data.remarks ?? '',
            adjustments: data.adjustments ?? [],
        });
        return response.data;
    },

    finalApprove: async (
        id: number,
        data?: ApproveIndentRequest
    ): Promise<Indent> => {
        const response = await apiClient.post<Indent>(
            `/approvals/indents/${id}/approve`,
            null,
            { params: { remarks: data?.remarks || '' } }
        );
        return response.data;
    },

    finalReject: async (id: number, data: RejectIndentRequest): Promise<Indent> => {
        const response = await apiClient.post<Indent>(
            `/approvals/indents/${id}/reject`,
            null,
            { params: { remarks: data.reason } }
        );
        return response.data;
    },

    requestInfo: async (
        id: number,
        data: { message: string }
    ): Promise<Indent> => {
        const response = await apiClient.post<Indent>(
            `/approvals/indents/${id}/request-info`,
            data
        );
        return response.data;
    },

    getApprovalHistory: async (id: number): Promise<any[]> => {
        const response = await apiClient.get(`/approvals/indents/${id}/approval-workflow`);
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/indents/${id}`);
    },

    // Get indents pending approval for current user (uses /approvals/ path)
    getPendingApproval: async (
        params: IndentSearchParams = {}
    ): Promise<PageResponse<Indent>> => {
        const response = await apiClient.get<PageResponse<Indent>>(
            "/approvals/pending",
            { params }
        );
        return response.data;
    },

    // --- Additional backend endpoints ---
    getByStatus: async (
        status: IndentStatus,
        params: PageRequest = {}
    ): Promise<PageResponse<Indent>> => {
        const response = await apiClient.get<PageResponse<Indent>>(
            `/indents/status/${status}`,
            { params }
        );
        return response.data;
    },

    getByEmployee: async (
        empId: number,
        params: PageRequest = {}
    ): Promise<PageResponse<Indent>> => {
        const response = await apiClient.get<PageResponse<Indent>>(
            `/indents/employee/${empId}`,
            { params }
        );
        return response.data;
    },

    search: async (
        params: IndentSearchParams = {}
    ): Promise<PageResponse<Indent>> => {
        const response = await apiClient.get<PageResponse<Indent>>(
            "/indents/search",
            { params }
        );
        return response.data;
    },

    procurementUpdate: async (
        id: number,
        data: { procurementSubStatus: number; poNumber?: string; deliveryDate?: string; remarks?: string }
    ): Promise<Indent> => {
        const response = await apiClient.post<Indent>(`/indents/${id}/procurement-update`, data);
        return response.data;
    },

    getStatistics: async (): Promise<any> => {
        const response = await apiClient.get("/indents/statistics");
        return response.data;
    },

    getMeta: async (): Promise<IndentFormMeta> => {
        const response = await apiClient.get<IndentFormMeta>("/indents/meta");
        return response.data;
    },
};

export default indentsApi;
