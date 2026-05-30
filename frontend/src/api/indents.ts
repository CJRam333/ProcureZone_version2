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
    details?: IndentItem[];
    // List endpoint fields
    detailsCount?: number;
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

export enum IndentStatus {
    DRAFT = 1,
    SUBMITTED = 2,
    DEPT_HEAD_APPROVED = 3,   // L1 approved
    FINANCE_APPROVED = 4,     // L2 approved
    PROCUREMENT_APPROVED = 5, // PO Created
    REJECTED = 6,
    ON_HOLD = 7,              // Legacy hold status
    COMPLETED = 8,
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
    quantity: number;
    rmQuantity?: number;
    deptQuantity?: number;
    stockAvailable?: number;
    pricing?: number;
    purpose?: string;
    vendor?: string;
    status: number;
    // Optional aliases for UI compatibility
    materialDescription?: string; // alias for materialName
    uomCode?: string; // alias for unitOfMeasureCode
    requestedQuantity?: number; // alias for quantity
    estimatedRate?: number; // alias for pricing
    remarks?: string; // alias for purpose
}

export interface IndentCreateRequest {
    companyId: number;
    departmentId: number;
    plantId: number;
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
    priority?: string;
    fromDate?: string;
    toDate?: string;
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

    getStatistics: async (): Promise<any> => {
        const response = await apiClient.get("/indents/statistics");
        return response.data;
    },
};

export default indentsApi;
