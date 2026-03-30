import apiClient from "./client";
import { PageRequest, PageResponse } from "./materials";

// ============== Types ==============
export interface PurchaseOrder {
    id: number;
    poNumber: string;
    poDate: string;
    vendorId: number;
    vendorName: string;
    vendorCode: string;
    plantId: number;
    plantName: string;
    indentId?: number;
    indentNumber?: string;
    status: POStatus;
    statusName: string;
    deliveryDate: string;
    paymentTerms: string;
    deliveryTerms: string;
    remarks?: string;
    totalAmount: number;
    taxAmount: number;
    grandTotal: number;
    confirmedAt?: string;
    confirmedBy?: number;
    confirmedByName?: string;
    items: POItem[];
    amendments?: POAmendment[];
    createdAt: string;
    updatedAt: string;
}

// Backend PO status values (1-based, from po_status column)
export enum POStatus {
    DRAFT = 1,
    SUBMITTED = 2,             // Pending approval
    APPROVED = 3,
    SENT_TO_VENDOR = 4,
    PARTIALLY_RECEIVED = 5,
    FULLY_RECEIVED = 6,
    CANCELLED = 7,
    CLOSED = 8,
}

export interface POItem {
    id: number;
    poId: number;
    materialId: number;
    materialCode: string;
    materialDescription: string;
    uomId: number;
    uomCode: string;
    indentItemId?: number;
    orderedQuantity: number;
    receivedQuantity: number;
    pendingQuantity: number;
    unitRate: number;
    taxRate: number;
    taxAmount: number;
    totalAmount: number;
    deliveryDate?: string;
    remarks?: string;
}

export interface POAmendment {
    id: number;
    poId: number;
    amendmentVersion: number;
    fieldName: string;
    originalValue: string;
    amendedValue: string;
    amendmentReason: string;
    status: string;
    amendedBy: number;
    amendedByName: string;
    amendedAt: string;
}

export interface POCreateRequest {
    vendorId: number;
    indentId?: number;
    deliveryDate: string;
    paymentTerms: string;
    deliveryTerms: string;
    remarks?: string;
    items: POItemCreateRequest[];
}

export interface POItemCreateRequest {
    materialId: number;
    indentItemId?: number;
    orderedQuantity: number;
    unitRate: number;
    taxRate: number;
    deliveryDate?: string;
    remarks?: string;
}

export interface POSearchParams extends PageRequest {
    search?: string;
    status?: POStatus;
    vendorId?: number;
    plantId?: number;
    fromDate?: string;
    toDate?: string;
}

export interface AmendPORequest {
    fieldName: string;
    newValue: string;
    amendmentReason: string;
}

// ============== API Functions ==============

export const purchaseOrdersApi = {
    list: async (
        params: POSearchParams = {}
    ): Promise<PageResponse<PurchaseOrder>> => {
        const response = await apiClient.get<PageResponse<PurchaseOrder>>(
            "/pos",
            { params }
        );
        return response.data;
    },

    getById: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.get<PurchaseOrder>(`/pos/${id}`);
        return response.data;
    },

    getByNumber: async (poNumber: string): Promise<PurchaseOrder> => {
        const response = await apiClient.get<PurchaseOrder>(
            `/pos/number/${poNumber}`
        );
        return response.data;
    },

    create: async (data: POCreateRequest): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>("/pos", data);
        return response.data;
    },

    update: async (
        id: number,
        data: Partial<POCreateRequest>
    ): Promise<PurchaseOrder> => {
        const response = await apiClient.put<PurchaseOrder>(`/pos/${id}`, data);
        return response.data;
    },

    // --- PO Workflow (matches backend) ---
    submit: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(
            `/pos/${id}/submit`
        );
        return response.data;
    },

    approve: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(
            `/pos/${id}/approve`
        );
        return response.data;
    },

    reject: async (id: number, reason: string): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(
            `/pos/${id}/reject`,
            { reason }
        );
        return response.data;
    },

    sendToVendor: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(
            `/pos/${id}/send-to-vendor`
        );
        return response.data;
    },

    cancel: async (id: number, reason: string): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(
            `/pos/${id}/cancel`,
            { reason }
        );
        return response.data;
    },

    close: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(
            `/pos/${id}/close`
        );
        return response.data;
    },

    amend: async (id: number, data: AmendPORequest): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(
            `/pos/${id}/amend`,
            data
        );
        return response.data;
    },

    getAmendments: async (id: number): Promise<POAmendment[]> => {
        const response = await apiClient.get<POAmendment[]>(
            `/pos/${id}/amendments`
        );
        return response.data;
    },

    // --- Query endpoints ---
    getByVendor: async (
        vendorId: number,
        params: PageRequest = {}
    ): Promise<PageResponse<PurchaseOrder>> => {
        const response = await apiClient.get<PageResponse<PurchaseOrder>>(
            `/pos/by-vendor/${vendorId}`,
            { params }
        );
        return response.data;
    },

    getByDepartment: async (
        deptId: number,
        params: PageRequest = {}
    ): Promise<PageResponse<PurchaseOrder>> => {
        const response = await apiClient.get<PageResponse<PurchaseOrder>>(
            `/pos/by-department/${deptId}`,
            { params }
        );
        return response.data;
    },

    getPendingApproval: async (
        params: PageRequest = {}
    ): Promise<PageResponse<PurchaseOrder>> => {
        const response = await apiClient.get<PageResponse<PurchaseOrder>>(
            "/pos/pending-approval",
            { params }
        );
        return response.data;
    },

    getOverdue: async (
        params: PageRequest = {}
    ): Promise<PageResponse<PurchaseOrder>> => {
        const response = await apiClient.get<PageResponse<PurchaseOrder>>(
            "/pos/overdue",
            { params }
        );
        return response.data;
    },

    search: async (
        params: POSearchParams = {}
    ): Promise<PageResponse<PurchaseOrder>> => {
        const response = await apiClient.get<PageResponse<PurchaseOrder>>(
            "/pos/search",
            { params }
        );
        return response.data;
    },

    getStatistics: async (): Promise<any> => {
        const response = await apiClient.get("/pos/dashboard/statistics");
        return response.data;
    },

    // Download PO as PDF
    downloadPdf: async (id: number): Promise<Blob> => {
        const response = await apiClient.get(`/pos/${id}/pdf`, {
            responseType: "blob",
        });
        return response.data;
    },

    // --- Commented out: endpoints not confirmed on backend ---
    // generateFromIndent: async (indentId: number, vendorId: number): Promise<PurchaseOrder> => {
    //     const response = await apiClient.post<PurchaseOrder>("/pos/from-indent", { indentId, vendorId });
    //     return response.data;
    // },

    // Backend confirmed: GET /pos/approved-indents EXISTS
    getApprovedIndents: async (): Promise<any[]> => {
        const response = await apiClient.get("/pos/approved-indents");
        return response.data;
    },

    // getPendingGRN: async (params: PageRequest = {}): Promise<PageResponse<PurchaseOrder>> => {
    //     const response = await apiClient.get<PageResponse<PurchaseOrder>>("/pos/pending-grn", { params });
    //     return response.data;
    // },
    // emailToVendor: async (id: number): Promise<void> => {
    //     await apiClient.post(`/pos/${id}/email`);
    // },
};

export default purchaseOrdersApi;
