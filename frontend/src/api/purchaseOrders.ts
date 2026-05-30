import apiClient from "./client";

// ============================================================
// Backend enum: POStatus (integer codes)
// DRAFT=1, SUBMITTED=2, APPROVED=3, SENT_TO_VENDOR=4,
// PARTIALLY_RECEIVED=5, FULLY_RECEIVED=6, CANCELLED=7, CLOSED=8
// ============================================================
export enum POStatus {
    DRAFT = 1,
    SUBMITTED = 2,
    APPROVED = 3,
    SENT_TO_VENDOR = 4,
    PARTIALLY_RECEIVED = 5,
    FULLY_RECEIVED = 6,
    CANCELLED = 7,
    CLOSED = 8,
}

// ============================================================
// PurchaseOrder — full detail response (GET /pos/{id})
// Maps exactly to backend PurchaseOrderResponse record.
// ============================================================
export interface PurchaseOrder {
    id: number;
    poNumber: string;
    poDate: string;                    // LocalDate → ISO string
    indentId?: number;
    indentNumber?: string;
    vendorId: number;
    vendorCode: string;
    vendorName: string;
    departmentId?: number;
    departmentName?: string;
    // Backend field name is poStatus (integer), NOT status
    poStatus: number;
    poStatusName?: string;
    totalAmount: number;               // BigDecimal → number
    taxAmount: number;
    discountAmount?: number;
    netAmount: number;                 // was: grandTotal — backend field is netAmount
    currency?: string;
    paymentTerms?: string;
    deliveryAddress?: string;          // physical delivery address
    deliveryDate?: string;             // actual delivery date (LocalDate)
    expectedDeliveryDate?: string;
    actualDeliveryDate?: string;
    termsConditions?: string;          // contractual terms (was: deliveryTerms)
    notes?: string;                    // was: remarks
    priority?: string;
    approvedBy?: number;               // was: confirmedBy
    approvedDate?: string;             // was: confirmedAt
    sentToVendorBy?: number;
    sentToVendorDate?: string;
    cancelledBy?: number;
    cancelledDate?: string;
    cancellationReason?: string;
    closedBy?: number;
    closedDate?: string;
    createdDate?: string;              // was: createdAt
    lastModifiedDate?: string;         // was: updatedAt
    // Backend sends line items under "details", not "items"
    details: PODetail[];
    amendments?: POAmendment[];
}

// ============================================================
// PODetail — line item (from PODetailResponse record)
// ============================================================
export interface PODetail {
    id: number;
    lineNumber?: number;
    indentDetailId?: number;           // was: indentItemId
    materialId: number;
    materialCode: string;
    materialName?: string;
    materialDescription?: string;      // also present in backend DTO
    quantity: number;                  // was: orderedQuantity — backend field is quantity
    unitOfMeasure?: string;            // was: uomCode
    unitPrice: number;                 // was: unitRate
    taxRate?: number;
    taxAmount?: number;
    discountRate?: number;
    discountAmount?: number;
    lineTotal: number;                 // was: totalAmount
    receivedQuantity: number;
    pendingQuantity: number;
    rejectedQuantity?: number;
    expectedDeliveryDate?: string;     // was: deliveryDate
    actualDeliveryDate?: string;
    deliveryStatus?: number;
    deliveryStatusName?: string;
    deliveryPercentage?: number;
    notes?: string;                    // was: remarks
}

// ============================================================
// POSummaryResponse — list view item (GET /pos returns this, NOT PurchaseOrder)
// Maps exactly to backend POSummaryResponse record.
// ============================================================
export interface POSummaryResponse {
    id: number;
    poNumber: string;
    poDate: string;
    vendorName: string;
    departmentName?: string;
    poStatus: number;
    poStatusName?: string;
    netAmount: number;
    priority?: string;
    expectedDeliveryDate?: string;
    totalLineItems?: number;
    deliveryPercentage?: number;
}

// ============================================================
// POListResponse — backend list wrapper (Map<String, Object>)
// Different from standard PageResponse: uses currentPage + totalItems
// ============================================================
export interface POListResponse {
    content: POSummaryResponse[];
    currentPage: number;               // was: number (PageResponse)
    totalItems: number;                // was: totalElements (PageResponse)
    totalPages: number;
}

// ============================================================
// POAmendment — maps to backend POAmendmentResponse record
// ============================================================
export interface POAmendment {
    id: number;
    poId: number;
    amendmentVersion: number;
    fieldName: string;
    originalValue: string;
    amendedValue: string;
    amendmentReason: string;
    amendedBy: number;
    amendedByName: string;
    amendedDate: string;               // was: amendedAt — backend field is amendedDate
    approvedBy?: number;
    approvedByName?: string;
    approvedDate?: string;
    status: string;
}

// ============================================================
// POAmendmentHistory — getAmendments() returns this wrapper
// Backend endpoint returns Map<String, Object>
// ============================================================
export interface POAmendmentHistory {
    poId: number;
    currentVersion: number;
    amendments: POAmendment[];
    totalAmendments: number;
}

// ============================================================
// CreatePORequest — maps to backend CreatePORequest record
// NOTE: uses lineItems (not items), indentId is required
// ============================================================
export interface POCreateRequest {
    indentId: number;                  // required by backend
    vendorId: number;
    paymentTerms?: string;
    deliveryAddress?: string;
    expectedDeliveryDate?: string;
    priority?: string;
    termsConditions?: string;          // was: deliveryTerms
    notes?: string;                    // was: remarks
    lineItems: POLineItemRequest[];    // was: items: POItemCreateRequest[]
}

// ============================================================
// POLineItemRequest — maps to backend POLineItemRequest record
// ============================================================
export interface POLineItemRequest {
    indentDetailId: number;            // required; was: indentItemId
    materialId: number;
    quantity: number;                  // was: orderedQuantity
    unitPrice: number;                 // was: unitRate
    taxRate?: number;
    discountRate?: number;
    expectedDeliveryDate?: string;     // was: deliveryDate
    notes?: string;                    // was: remarks
}

// ============================================================
// UpdatePORequest — maps to backend UpdatePORequest record
// Only header fields; line items cannot be changed after creation
// ============================================================
export interface UpdatePORequest {
    deliveryDate?: string;
    deliveryAddress?: string;
    paymentTerms?: string;
    termsConditions?: string;
    notes?: string;
    priority?: string;
}

// ============================================================
// AmendPORequest — maps to backend AmendPORequest record
// Sends COMPLETE new field values (not fieldName/newValue pair)
// amendmentReason is required
// ============================================================
export interface AmendPORequest {
    deliveryDate?: string;
    deliveryAddress?: string;
    paymentTerms?: string;
    termsConditions?: string;
    notes?: string;
    priority?: string;
    amendmentReason: string;           // required, min 10 chars
}

// ============================================================
// POSearchParams — for list/filter queries
// status is a plain number matching POStatus enum codes
// ============================================================
export interface POSearchParams {
    page?: number;
    size?: number;
    search?: string;
    status?: number;
    vendorId?: number;
    sort?: string;
}

// ============================================================
// ApprovedIndentDTO — from GET /pos/approved-indents
// ============================================================
export interface ApprovedIndentDTO {
    indentId: number;
    indentNumber: string;
    indentDate: string;
    departmentId: number;
    departmentName: string;
    requestorId: number;
    requestorName: string;
    purpose?: string;
    priority?: string;
    totalLineItems: number;
    requiredByDate?: string;
}

// ============================================================
// API Functions
// ============================================================

export const purchaseOrdersApi = {

    // GET /api/v1/pos — returns POListResponse (not PageResponse)
    list: async (params: POSearchParams = {}): Promise<POListResponse> => {
        const response = await apiClient.get<POListResponse>("/pos", { params });
        return response.data;
    },

    // GET /api/v1/pos/{id} — returns full PurchaseOrder
    getById: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.get<PurchaseOrder>(`/pos/${id}`);
        return response.data;
    },

    // GET /api/v1/pos/number/{poNumber}
    getByNumber: async (poNumber: string): Promise<PurchaseOrder> => {
        const response = await apiClient.get<PurchaseOrder>(`/pos/number/${poNumber}`);
        return response.data;
    },

    // POST /api/v1/pos
    create: async (data: POCreateRequest): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>("/pos", data);
        return response.data;
    },

    // PUT /api/v1/pos/{id} — only header fields, no line items
    update: async (id: number, data: UpdatePORequest): Promise<PurchaseOrder> => {
        const response = await apiClient.put<PurchaseOrder>(`/pos/${id}`, data);
        return response.data;
    },

    // POST /api/v1/pos/{id}/submit
    submit: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(`/pos/${id}/submit`);
        return response.data;
    },

    // POST /api/v1/pos/{id}/approve
    approve: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(`/pos/${id}/approve`);
        return response.data;
    },

    // POST /api/v1/pos/{id}/send-to-vendor
    sendToVendor: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(`/pos/${id}/send-to-vendor`);
        return response.data;
    },

    // POST /api/v1/pos/{id}/cancel — body must be { cancellationReason }
    cancel: async (id: number, cancellationReason: string): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(
            `/pos/${id}/cancel`,
            { cancellationReason }             // was: { reason } — backend expects cancellationReason
        );
        return response.data;
    },

    // POST /api/v1/pos/{id}/close
    close: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(`/pos/${id}/close`);
        return response.data;
    },

    // POST /api/v1/pos/{id}/amend — sends full AmendPORequest
    amend: async (id: number, data: AmendPORequest): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(`/pos/${id}/amend`, data);
        return response.data;
    },

    // GET /api/v1/pos/{id}/amendments — returns POAmendmentHistory wrapper
    getAmendments: async (id: number): Promise<POAmendmentHistory> => {
        const response = await apiClient.get<POAmendmentHistory>(`/pos/${id}/amendments`);
        return response.data;
    },

    // GET /api/v1/pos/approved-indents
    getApprovedIndents: async (): Promise<ApprovedIndentDTO[]> => {
        const response = await apiClient.get<ApprovedIndentDTO[]>("/pos/approved-indents");
        return response.data;
    },

    // GET /api/v1/pos/by-vendor/{vendorId}
    getByVendor: async (vendorId: number, params: POSearchParams = {}): Promise<POListResponse> => {
        const response = await apiClient.get<POListResponse>(`/pos/by-vendor/${vendorId}`, { params });
        return response.data;
    },

    // GET /api/v1/pos/by-department/{deptId}
    getByDepartment: async (deptId: number, params: POSearchParams = {}): Promise<POListResponse> => {
        const response = await apiClient.get<POListResponse>(`/pos/by-department/${deptId}`, { params });
        return response.data;
    },

    // GET /api/v1/pos/pending-approval
    getPendingApproval: async (params: POSearchParams = {}): Promise<POListResponse> => {
        const response = await apiClient.get<POListResponse>("/pos/pending-approval", { params });
        return response.data;
    },

    // GET /api/v1/pos/overdue
    getOverdue: async (params: POSearchParams = {}): Promise<POListResponse> => {
        const response = await apiClient.get<POListResponse>("/pos/overdue", { params });
        return response.data;
    },

    // GET /api/v1/pos/dashboard/statistics
    getStatistics: async (): Promise<Record<string, unknown>> => {
        const response = await apiClient.get<Record<string, unknown>>("/pos/dashboard/statistics");
        return response.data;
    },

    // GET /api/v1/pos/{id}/pdf
    downloadPdf: async (id: number): Promise<Blob> => {
        const response = await apiClient.get(`/pos/${id}/pdf`, { responseType: "blob" });
        return response.data;
    },
};

export default purchaseOrdersApi;
