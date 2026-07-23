import apiClient from "./client";
import { PageRequest, PageResponse } from "./materials";

// ============== Types ==============

// Response matching backend IssueNoteResponse — enriched with resolved names
export interface IssueNote {
    id: number;
    issueNoteNumber: string;
    issueDate: string;
    companyId: number;
    companyName?: string;
    departmentId: number;
    departmentName?: string;
    sectionId?: number;
    sectionName?: string;
    plantId: number;
    plantName?: string;
    issuedTo?: string;
    purpose?: string;
    comments?: string;
    createdBy: number;
    employeeNumber?: number;   // creator emp number (= createdBy)
    employeeName?: string;     // resolved creator name
    approvedBy?: number;
    approvedByDate?: string;
    storesBy?: number;
    storesByDate?: string;
    issuedByName?: string;     // resolved from storesBy
    rmApprovedBy?: number;
    rmApprovedByName?: string; // resolved RM approver name
    rmApprovedByDate?: string;
    status: number;
    statusDescription: string;
    displayStatus?: string;    // derived from two-column model
    approvedStatus?: number;
    storesByStatus?: number;
    lastModifiedDate: string;
    lastModifiedBy: number;
    details: IssueNoteDetail[];
    totalAmount: number;
}

// Response matching backend IssueNoteDetailResponse — enriched with material/UOM codes
export interface IssueNoteDetail {
    id: number;
    materialId: number;
    materialCode?: string;
    materialName?: string;
    unitOfMeasureId: number;
    uomCode?: string;
    quantity: number;
    rate?: number;
    amount?: number;
    purpose?: string;
    status: number;
    companies?: string; // comma-separated company names stocking this material
}

// Backend Issue Note status values (1-based, from issue_note_status column)
export enum IssueNoteStatus {
    CREATED = 1,                // Draft
    PENDING_RM_APPROVAL = 2,    // Submitted
    RM_APPROVED = 3,            // Pending manager approval
    APPROVED_BY_MANAGER = 4,    // Ready for stores
    REJECTED_BY_RM = 5,
    REJECTED_BY_MANAGER = 6,
    PENDING_STORE_ISSUE = 7,    // Waiting for stores to issue
    ISSUED = 8,                 // Materials released — done
    REJECTED_BY_STORES = 9,     // Insufficient stock
    RETURNED = 10,              // Materials returned to stores
}

// Legacy interface for backwards compatibility
export interface IssueNoteItem {
    id: number;
    issueNoteId: number;
    materialId: number;
    materialCode: string;
    materialDescription: string;
    uomId: number;
    uomCode: string;
    requestedQuantity: number;
    approvedQuantity?: number;
    issuedQuantity?: number;
    returnedQuantity?: number;
    batchNumber?: string;
    remarks?: string;
}

export interface IssueNoteCreateRequest {
    // company / department / plant / section captured server-side from the employee record — optional
    companyId?: number;
    departmentId?: number;
    sectionId?: number;
    plantId?: number;
    issuedTo?: string; // optional — no legacy equivalent; creator is the requester of record
    purpose?: string;
    comments?: string;
    lineItems: IssueNoteLineItemCreateRequest[];
}

export interface IssueNoteLineItemCreateRequest {
    materialId: number;
    unitOfMeasureId: number;
    quantity: number;
    rate?: number;
    purpose?: string;
    quantityStores?: number;
}

export interface IssueNoteSearchParams extends PageRequest {
    search?: string;
    status?: number;          // legacy — use approvedStatus+storesByStatus for list page
    approvedStatus?: number;
    storesByStatus?: number;
    departmentId?: number;
    plantId?: number;
    fromDate?: string;
    toDate?: string;
}

export interface IssueItemsRequest {
    items: { itemId: number; issuedQuantity: number; batchNumber?: string }[];
    remarks?: string;
}

export interface ReturnIssueNoteRequest {
    items: { itemId: number; returnedQuantity: number; returnReason: string }[];
    remarks?: string;
}

export interface IssueNoteFormMeta {
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
    nextIssueNoteNumber: string;
}

// ============== API Functions ==============

export const issueNotesApi = {
    list: async (
        params: IssueNoteSearchParams = {}
    ): Promise<PageResponse<IssueNote>> => {
        const response = await apiClient.get<PageResponse<IssueNote>>(
            "/issue-notes",
            { params }
        );
        return response.data;
    },

    // Export the (filtered) list to CSV/Excel — returns a Blob for browser download
    export: async (params: {
        format: "csv" | "xlsx";
        search?: string;
        approvedStatus?: number;
        storesByStatus?: number;
        departmentId?: number;
    }): Promise<Blob> => {
        const response = await apiClient.get("/issue-notes/export", {
            params,
            responseType: "blob",
        });
        return response.data;
    },

    // Single-entity endpoints wrap the body in { success, [message,] data }.
    // Axios response.data is that envelope — the issue note lives at response.data.data.
    getById: async (id: number): Promise<IssueNote> => {
        const response = await apiClient.get<{ data: IssueNote }>(`/issue-notes/${id}`);
        return response.data.data;
    },

    getByNumber: async (issueNoteNumber: string): Promise<IssueNote> => {
        const response = await apiClient.get<{ data: IssueNote }>(
            `/issue-notes/by-number`,
            { params: { issueNoteNumber } }
        );
        return response.data.data;
    },

    create: async (data: IssueNoteCreateRequest): Promise<IssueNote> => {
        const response = await apiClient.post<{ data: IssueNote }>("/issue-notes", data);
        return response.data.data;
    },

    update: async (
        id: number,
        data: Partial<IssueNoteCreateRequest>
    ): Promise<IssueNote> => {
        const response = await apiClient.put<IssueNote>(
            `/issue-notes/${id}`,
            data
        );
        return response.data;
    },

    submit: async (id: number): Promise<IssueNote> => {
        const response = await apiClient.post<{ data: IssueNote }>(
            `/issue-notes/${id}/submit`
        );
        return response.data.data;
    },

    // DEPRECATED — manager-stage endpoint returns 410 GONE (flow is User→RM→Stores).
    // Kept only for backwards compatibility; use rmApprove instead.
    approve: async (
        id: number,
        data?: {
            items?: { itemId: number; approvedQuantity: number }[];
            remarks?: string;
        }
    ): Promise<IssueNote> => {
        const response = await apiClient.post<IssueNote>(
            `/issue-notes/${id}/approve`,
            data || {}
        );
        return response.data;
    },

    // DEPRECATED — manager-stage endpoint returns 410 GONE. Use rmReject instead.
    reject: async (
        id: number,
        data: { reason: string }
    ): Promise<IssueNote> => {
        const response = await apiClient.post<IssueNote>(
            `/issue-notes/${id}/reject`,
            data
        );
        return response.data;
    },

    issue: async (
        id: number,
        data?: {
            items?: {
                itemId: number;
                issuedQuantity: number;
                batchNumber?: string;
            }[];
            remarks?: string;
        }
    ): Promise<IssueNote> => {
        const response = await apiClient.post<{ data: IssueNote }>(
            `/issue-notes/${id}/issue`,
            data || {}
        );
        return response.data.data;
    },

    // Return materials to stores (status 8→10)
    return: async (
        id: number,
        data: ReturnIssueNoteRequest
    ): Promise<IssueNote> => {
        const response = await apiClient.post<{ data: IssueNote }>(
            `/issue-notes/${id}/return`,
            data
        );
        return response.data.data;
    },

    // Cancel issue note (only when status = 1 or 2)
    cancel: async (id: number, reason: string): Promise<IssueNote> => {
        const response = await apiClient.post<{ data: IssueNote }>(
            `/issue-notes/${id}/cancel`,
            { reason }
        );
        return response.data.data;
    },

    // --- Additional backend endpoints ---

    // Stores rejection (status 4/7 → 9)
    storesReject: async (
        id: number,
        data: { reason: string }
    ): Promise<IssueNote> => {
        const response = await apiClient.post<{ data: IssueNote }>(
            `/issue-notes/${id}/reject-stores`,
            data
        );
        return response.data.data;
    },

    // RM Approval (status 2 → 3, skipped if supervisorBypass)
    rmApprove: async (
        id: number,
        data: { remarks: string }
    ): Promise<IssueNote> => {
        const response = await apiClient.post<{ data: IssueNote }>(
            `/issue-notes/${id}/rm-approve`,
            data
        );
        return response.data.data;
    },

    // RM Rejection (status 2 → 5)
    rmReject: async (
        id: number,
        data: { reason: string }
    ): Promise<IssueNote> => {
        const response = await apiClient.post<{ data: IssueNote }>(
            `/issue-notes/${id}/rm-reject`,
            data
        );
        return response.data.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/issue-notes/${id}`);
    },

    getMyNotes: async (
        params: PageRequest = {}
    ): Promise<PageResponse<IssueNote>> => {
        const response = await apiClient.get<PageResponse<IssueNote>>(
            "/issue-notes/my-issue-notes",
            { params }
        );
        return response.data;
    },

    getByDepartment: async (
        deptId: number,
        params: PageRequest = {}
    ): Promise<PageResponse<IssueNote>> => {
        const response = await apiClient.get<PageResponse<IssueNote>>(
            `/issue-notes/by-department/${deptId}`,
            { params }
        );
        return response.data;
    },

    // Get pending approval
    getPendingApproval: async (
        params: PageRequest = {}
    ): Promise<PageResponse<IssueNote>> => {
        const response = await apiClient.get<PageResponse<IssueNote>>(
            "/issue-notes/pending-approval",
            { params }
        );
        return response.data;
    },

    // Get pending issue
    getPendingIssue: async (
        params: PageRequest = {}
    ): Promise<PageResponse<IssueNote>> => {
        const response = await apiClient.get<PageResponse<IssueNote>>(
            "/issue-notes/pending-issue",
            { params }
        );
        return response.data;
    },

    // Get pending RM approval queue
    getPendingRmApproval: async (
        params: PageRequest = {}
    ): Promise<PageResponse<IssueNote>> => {
        const response = await apiClient.get<PageResponse<IssueNote>>(
            "/issue-notes/pending-rm-approval",
            { params }
        );
        return response.data;
    },

    getStatistics: async (): Promise<any> => {
        const response = await apiClient.get("/issue-notes/statistics");
        return response.data;
    },

    getMeta: async (): Promise<IssueNoteFormMeta> => {
        const response = await apiClient.get<IssueNoteFormMeta>("/issue-notes/meta");
        return response.data;
    },
};

export default issueNotesApi;
