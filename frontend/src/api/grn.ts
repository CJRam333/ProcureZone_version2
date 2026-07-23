import apiClient from "./client";
import { PageRequest, PageResponse } from "./materials";

// ============== Types ==============
export interface GRN {
    id: number;
    grnNumber: string;
    receiptDate: string;
    indentId: number;
    indentDetailsId: number;
    vendorName: string;

    receivedQuantity: number;
    issuedQuantity?: number;
    balanceInventory?: number;
    openingQuantity?: number;
    requestedQuantity?: number;
    balanceQuantityStores?: number;

    rate: number;
    amount: number;
    comments?: string;

    status: number; // Integer status (1-7)
    statusName: string;
    supervisorBypass?: boolean; // If true, skip RM approval step

    createdBy: number;
    createdDate: string;
    createdRemarks?: string;

    approvedBy?: number;
    approvedDate?: string;
    approvedRemarks?: string;

    finalApprovedBy?: number;
    finalApprovedDate?: string;
    finalApprovedRemarks?: string;

    storedBy?: number;
    storedDate?: string;
    storedRemarks?: string;

    // items: GRNItem[]; // Removed as backend is single-item based
    createdAt: string; // Alias for createdDate
    updatedAt: string; // Alias or not present
}

// Backend status values (1-based, from goods_receipt_status column)
export enum GRNStatus {
    CREATED = 1,           // Pending inspection
    INSPECTED = 2,         // Pending RM approval
    RM_APPROVED = 3,       // Pending Dept Head approval
    APPROVED = 4,          // Pending final approval
    FINAL_APPROVED = 5,    // Pending storage
    STORED = 6,            // Completed — goods in inventory
    REJECTED = 7,
}

export interface GRNItem {
    id: number;
    grnId: number;
    poItemId: number;
    materialId: number;
    materialCode: string;
    materialDescription: string;
    uomId: number;
    uomCode: string;
    orderedQuantity: number;
    previouslyReceived: number;
    receivedQuantity: number;
    acceptedQuantity?: number;
    rejectedQuantity?: number;
    rejectionReason?: string;
    batchNumber?: string;
    remarks?: string;
}

export interface GRNCreateRequest {
    indentId: number;
    indentDetailsId: number;
    receivedQuantity: number;
    rate: number;
    vendorName: string;
    openingQuantity?: number;
    comments?: string;
}

export interface GRNItemCreateRequest {
    poItemId: number;
    receivedQuantity: number;
    batchNumber?: string;
    remarks?: string;
}

export interface GRNSearchParams extends PageRequest {
    search?: string;
    status?: number;
    poId?: number;
    vendorId?: number;
    plantId?: number;
    fromDate?: string;
    toDate?: string;
    qualityStatus?: string;
}

export interface QCApproveRequest {
    acceptedQuantity?: number;
    rejectedQuantity?: number;
    rejectionReason?: string;
    remarks?: string;
}

export interface QCRejectRequest {
    rejectionReason?: string;
    remarks: string;
}

// Backend-aligned request types
export interface InspectionRequest {
    remarks: string;
    qualityApproved: boolean;
}

export interface ApprovalRequest {
    remarks: string;
}

export interface RejectionRequest {
    reason: string;
}

export interface StoreRequest {
    remarks: string;
}

// ============== API Functions ==============
// All endpoints use /grn (SINGULAR) to match backend

export const grnApi = {
    list: async (params: GRNSearchParams = {}): Promise<PageResponse<GRN>> => {
        const response = await apiClient.get<PageResponse<GRN>>("/grn", {
            params,
        });
        return response.data;
    },

    // Export the (filtered) list to CSV/Excel — returns a Blob for browser download
    export: async (params: {
        format: "csv" | "xlsx";
        status?: number;
        search?: string;
    }): Promise<Blob> => {
        const response = await apiClient.get("/grn/export", {
            params,
            responseType: "blob",
        });
        return response.data;
    },

    getById: async (id: number): Promise<GRN> => {
        const response = await apiClient.get<GRN>(`/grn/${id}`);
        return response.data;
    },

    getByNumber: async (grnNumber: string): Promise<GRN> => {
        const response = await apiClient.get<GRN>(`/grn/number/${grnNumber}`);
        return response.data;
    },

    create: async (data: GRNCreateRequest): Promise<GRN> => {
        const response = await apiClient.post<GRN>("/grn", data);
        return response.data;
    },

    update: async (
        id: number,
        data: Partial<GRNCreateRequest>
    ): Promise<GRN> => {
        const response = await apiClient.put<GRN>(`/grn/${id}`, data);
        return response.data;
    },

    // --- Quality Inspection (status 1 → 2) ---
    inspect: async (id: number, data: InspectionRequest): Promise<GRN> => {
        const response = await apiClient.post<GRN>(
            `/grn/${id}/inspect`,
            data
        );
        return response.data;
    },

    // Convenience wrappers for QC approve/reject
    approveQC: async (id: number, data: QCApproveRequest): Promise<GRN> => {
        const inspectionData: InspectionRequest = {
            remarks: data.remarks || "QC Approved",
            qualityApproved: true,
        };
        const response = await apiClient.post<GRN>(
            `/grn/${id}/inspect`,
            inspectionData
        );
        return response.data;
    },

    rejectQC: async (id: number, data: QCRejectRequest): Promise<GRN> => {
        const inspectionData: InspectionRequest = {
            remarks: data.remarks || data.rejectionReason || "QC Rejected",
            qualityApproved: false,
        };
        const response = await apiClient.post<GRN>(
            `/grn/${id}/inspect`,
            inspectionData
        );
        return response.data;
    },

    // --- RM Approval (status 2 → 3, skipped if supervisorBypass) ---
    rmApprove: async (id: number, remarks: string): Promise<GRN> => {
        const response = await apiClient.post<GRN>(`/grn/${id}/rm-approve`, {
            remarks,
        });
        return response.data;
    },

    rmReject: async (id: number, reason: string): Promise<GRN> => {
        const response = await apiClient.post<GRN>(`/grn/${id}/rm-reject`, {
            reason,
        });
        return response.data;
    },

    // --- Dept Head Approval (status 3 → 4) ---
    approve: async (id: number, remarks: string): Promise<GRN> => {
        const response = await apiClient.post<GRN>(`/grn/${id}/approve`, {
            remarks,
        });
        return response.data;
    },

    // --- Final Approval (status 4 → 5, adds to inventory) ---
    finalApprove: async (id: number, remarks: string): Promise<GRN> => {
        const response = await apiClient.post<GRN>(`/grn/${id}/final-approve`, {
            remarks,
        });
        return response.data;
    },

    // --- Store Goods (status 5 → 6) ---
    store: async (id: number, remarks: string): Promise<GRN> => {
        const response = await apiClient.post<GRN>(`/grn/${id}/store`, {
            remarks,
        });
        return response.data;
    },

    // --- Reject (any status except 6, 7) ---
    reject: async (id: number, reason: string): Promise<GRN> => {
        const response = await apiClient.post<GRN>(`/grn/${id}/reject`, {
            reason,
        });
        return response.data;
    },

    // Legacy alias for store
    post: async (id: number): Promise<GRN> => {
        const response = await apiClient.post<GRN>(`/grn/${id}/store`, {
            remarks: "Posted to inventory",
        });
        return response.data;
    },

    // --- Query Endpoints ---
    getPendingInspection: async (
        params: PageRequest = {}
    ): Promise<PageResponse<GRN>> => {
        const response = await apiClient.get<PageResponse<GRN>>(
            "/grn/pending-inspection",
            { params }
        );
        return response.data;
    },

    getPendingApproval: async (
        params: PageRequest = {}
    ): Promise<PageResponse<GRN>> => {
        const response = await apiClient.get<PageResponse<GRN>>(
            "/grn/pending-approval",
            { params }
        );
        return response.data;
    },

    getStatistics: async (): Promise<any> => {
        const response = await apiClient.get("/grn/statistics");
        return response.data;
    },
};

export default grnApi;
