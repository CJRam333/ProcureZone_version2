import apiClient from "./client";

// ============== Types ==============

export interface PlantIndentDetail {
    id: number;
    materialId: number;
    materialCode: string;
    materialName: string;
    unitOfMeasureId: number;
    uomCode: string;
    uomName: string;
    quantity: number;
    rmQuantity?: number;
    deptQuantity?: number;
    stockAvailable?: number;
    pricing?: number;
    purpose?: string;
    vendor?: string;
    status?: number;
    indentMaterial?: string;
    indentMaterialDescription?: string;
    lotNumber?: string;
    storageLocation?: string;
    // QC parameters (optional, only shown in detail view)
    stl?: string;
    odv?: string;
    got?: string;
    elisa?: string;
    moisture?: string;
    pureSeed?: string;
    germNormal?: string;
    lastModifiedDate?: string;
}

export interface PlantIndent {
    id: number;
    indentCode: string;
    employeeNumber: string;
    plantId: number;
    plantName: string;
    indentNumber: string;
    remarks: string;
    cropTypeId?: number;
    cropTypeName?: string;
    cropId?: number;
    cropName?: string;
    packProcess?: string;
    outputMaterial?: string;
    outputDescription?: string;
    batchNumber?: number;
    masterUom?: string;
    lineCode?: string;
    lineDescription?: string;
    expectedQuantity?: string;
    status?: number;
    statusDescription?: string;
    detailCount: number;
    details?: PlantIndentDetail[];
}

export interface PlantIndentCreateRequest {
    employeeNumber: string;
    plantId: number;
    remarks?: string;
    cropTypeId?: number;
    cropId?: number;
    packProcess?: string;
    outputMaterial?: string;
    outputDescription?: string;
    batchNumber?: number;
    masterUom?: string;
    lineCode?: string;
    lineDescription?: string;
    expectedQuantity?: string;
    details: PlantIndentDetailCreateRequest[];
}

export interface PlantIndentDetailCreateRequest {
    materialId: number;
    unitOfMeasureId: number;
    quantity: number;
    stockAvailable?: number;
    pricing?: number;
    purpose?: string;
    vendor?: string;
    indentMaterial?: string;
    indentMaterialDescription?: string;
    lotNumber?: string;
    storageLocation?: string;
}

interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

// ============== API Functions ==============

export const plantIndentsApi = {
    // List all plant indents with pagination
    list: async (params: {
        page?: number;
        size?: number;
    } = {}): Promise<PageResponse<PlantIndent>> => {
        const response = await apiClient.get<PageResponse<PlantIndent>>(
            "/plant-indents",
            { params: { page: params.page ?? 0, size: params.size ?? 20 } }
        );
        return response.data;
    },

    // List by plant
    listByPlant: async (
        plantId: number,
        params: { page?: number; size?: number } = {}
    ): Promise<PageResponse<PlantIndent>> => {
        const response = await apiClient.get<PageResponse<PlantIndent>>(
            `/plant-indents/plant/${plantId}`,
            { params: { page: params.page ?? 0, size: params.size ?? 20 } }
        );
        return response.data;
    },

    // List by employee
    listByEmployee: async (
        employeeNumber: string,
        params: { page?: number; size?: number } = {}
    ): Promise<PageResponse<PlantIndent>> => {
        const response = await apiClient.get<PageResponse<PlantIndent>>(
            `/plant-indents/employee/${employeeNumber}`,
            { params: { page: params.page ?? 0, size: params.size ?? 20 } }
        );
        return response.data;
    },

    // Search
    search: async (
        query: string,
        params: { page?: number; size?: number } = {}
    ): Promise<PageResponse<PlantIndent>> => {
        const response = await apiClient.get<PageResponse<PlantIndent>>(
            "/plant-indents/search",
            { params: { query, page: params.page ?? 0, size: params.size ?? 20 } }
        );
        return response.data;
    },

    // Get by ID
    getById: async (id: number): Promise<PlantIndent> => {
        const response = await apiClient.get<PlantIndent>(
            `/plant-indents/${id}`
        );
        return response.data;
    },

    // Get by indent number
    getByNumber: async (indentNumber: string): Promise<PlantIndent> => {
        const response = await apiClient.get<PlantIndent>(
            `/plant-indents/number/${indentNumber}`
        );
        return response.data;
    },

    // Create
    create: async (data: PlantIndentCreateRequest): Promise<PlantIndent> => {
        const response = await apiClient.post<PlantIndent>(
            "/plant-indents",
            data
        );
        return response.data;
    },

    // Update
    update: async (
        id: number,
        data: PlantIndentCreateRequest
    ): Promise<PlantIndent> => {
        const response = await apiClient.put<PlantIndent>(
            `/plant-indents/${id}`,
            data
        );
        return response.data;
    },

    // Delete
    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/plant-indents/${id}`);
    },

    // === Workflow Actions ===

    submitForReview: async (id: number): Promise<PlantIndent> => {
        const response = await apiClient.post<PlantIndent>(
            `/plant-indents/${id}/submit`
        );
        return response.data;
    },

    deoApprove: async (
        id: number,
        remarks?: string
    ): Promise<PlantIndent> => {
        const response = await apiClient.post<PlantIndent>(
            `/plant-indents/${id}/deo-approve`,
            null,
            { params: { remarks } }
        );
        return response.data;
    },

    deoReject: async (
        id: number,
        remarks: string
    ): Promise<PlantIndent> => {
        const response = await apiClient.post<PlantIndent>(
            `/plant-indents/${id}/deo-reject`,
            null,
            { params: { remarks } }
        );
        return response.data;
    },

    managerApprove: async (
        id: number,
        remarks?: string
    ): Promise<PlantIndent> => {
        const response = await apiClient.post<PlantIndent>(
            `/plant-indents/${id}/manager-approve`,
            null,
            { params: { remarks } }
        );
        return response.data;
    },

    managerReject: async (
        id: number,
        remarks: string
    ): Promise<PlantIndent> => {
        const response = await apiClient.post<PlantIndent>(
            `/plant-indents/${id}/manager-reject`,
            null,
            { params: { remarks } }
        );
        return response.data;
    },

    startProcessing: async (id: number): Promise<PlantIndent> => {
        const response = await apiClient.post<PlantIndent>(
            `/plant-indents/${id}/start-processing`
        );
        return response.data;
    },

    complete: async (id: number): Promise<PlantIndent> => {
        const response = await apiClient.post<PlantIndent>(
            `/plant-indents/${id}/complete`
        );
        return response.data;
    },

    resubmit: async (id: number): Promise<PlantIndent> => {
        const response = await apiClient.post<PlantIndent>(
            `/plant-indents/${id}/resubmit`
        );
        return response.data;
    },

    // Dashboard counts
    getDashboardCounts: async (
        plantId: number
    ): Promise<Record<string, number>> => {
        const response = await apiClient.get<Record<string, number>>(
            `/plant-indents/dashboard/${plantId}`
        );
        return response.data;
    },

    // By status
    getByStatus: async (
        status: number,
        params: { page?: number; size?: number } = {}
    ): Promise<PageResponse<PlantIndent>> => {
        const response = await apiClient.get<PageResponse<PlantIndent>>(
            `/plant-indents/status/${status}`,
            { params: { page: params.page ?? 0, size: params.size ?? 20 } }
        );
        return response.data;
    },
};

export default plantIndentsApi;
