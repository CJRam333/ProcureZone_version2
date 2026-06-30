import apiClient from "./client";
import { PageRequest, PageResponse } from "./materials";

// ============== Types ==============

// Response matching backend IssueNoteResponse (with optional enriched fields for UI)
export interface IssueNote {
    id: number;
    issueNoteNumber: string;
    issueDate: string;
    companyId: number;
    departmentId: number;
    sectionId?: number;
    plantId: number;
    issuedTo: string;
    purpose?: string;
    comments?: string;
    remarks?: string; // alias for comments
    createdBy: number;
    approvedBy?: number;
    approvedByDate?: string;
    storesBy?: number;
    storesByDate?: string;
    status: number;
    statusDescription: string;
    approvedStatus?: number;
    storesByStatus?: number;
    lastModifiedDate: string;
    lastModifiedBy: number;
    details: IssueNoteDetail[];
    totalAmount: number;
    // Optional enriched fields for UI display
    issueNumber?: string; // alias for issueNoteNumber (for legacy UI compatibility)
    requestedByName?: string;
    departmentName?: string;
    plantName?: string;
    statusName?: string; // alias for statusDescription
    issuedByName?: string;
    createdAt?: string; // alias for issueDate
    items?: IssueNoteDetail[]; // alias for details (for legacy UI compatibility)
}

// Response matching backend IssueNoteDetailResponse (with optional enriched fields for UI)
export interface IssueNoteDetail {
    id: number;
    materialId: number;
    unitOfMeasureId: number;
    quantity: number;
    rate?: number;
    amount?: number;
    purpose?: string;
    status: number;
    // Optional enriched fields for UI display
    materialCode?: string;
    materialDescription?: string;
    materialName?: string;
    uomId?: number; // alias for unitOfMeasureId
    uomCode?: string;
    uomName?: string;
    requestedQuantity?: number; // alias for quantity
    approvedQuantity?: number;
    issuedQuantity?: number;
    returnedQuantity?: number;
    batchNumber?: string;
    remarks?: string; // alias for purpose
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
    companyId: number;
    departmentId: number;
    sectionId?: number;
    plantId: number;
    issuedTo: string;
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

    getById: async (id: number): Promise<IssueNote> => {
        const response = await apiClient.get<IssueNote>(`/issue-notes/${id}`);
        return response.data;
    },

    getByNumber: async (issueNoteNumber: string): Promise<IssueNote> => {
        const response = await apiClient.get<IssueNote>(
            `/issue-notes/by-number`,
            { params: { issueNoteNumber } }
        );
        return response.data;
    },

    create: async (data: IssueNoteCreateRequest): Promise<IssueNote> => {
        const response = await apiClient.post<IssueNote>("/issue-notes", data);
        return response.data;
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
        const response = await apiClient.post<IssueNote>(
            `/issue-notes/${id}/submit`
        );
        return response.data;
    },

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
        const response = await apiClient.post<IssueNote>(
            `/issue-notes/${id}/issue`,
            data || {}
        );
        return response.data;
    },

    // Return materials to stores (status 8→10)
    return: async (
        id: number,
        data: ReturnIssueNoteRequest
    ): Promise<IssueNote> => {
        const response = await apiClient.post<IssueNote>(
            `/issue-notes/${id}/return`,
            data
        );
        return response.data;
    },

    // Cancel issue note (only when status = 1 or 2)
    cancel: async (id: number, reason: string): Promise<IssueNote> => {
        const response = await apiClient.post<IssueNote>(
            `/issue-notes/${id}/cancel`,
            { reason }
        );
        return response.data;
    },

    // --- Additional backend endpoints ---

    // Stores rejection (status 4/7 → 9)
    storesReject: async (
        id: number,
        data: { reason: string }
    ): Promise<IssueNote> => {
        const response = await apiClient.post<IssueNote>(
            `/issue-notes/${id}/reject-stores`,
            data
        );
        return response.data;
    },

    // RM Approval (status 2 → 3, skipped if supervisorBypass)
    rmApprove: async (
        id: number,
        data: { remarks: string }
    ): Promise<IssueNote> => {
        const response = await apiClient.post<IssueNote>(
            `/issue-notes/${id}/rm-approve`,
            data
        );
        return response.data;
    },

    // RM Rejection (status 2 → 5)
    rmReject: async (
        id: number,
        data: { reason: string }
    ): Promise<IssueNote> => {
        const response = await apiClient.post<IssueNote>(
            `/issue-notes/${id}/rm-reject`,
            data
        );
        return response.data;
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
