import apiClient from "./client";

// ============== Types ==============
export interface PageRequest {
    page?: number;
    size?: number;
    sort?: string;
    direction?: "asc" | "desc";
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

// Material response matching backend MaterialResponse (with optional enriched fields for UI)
export interface Material {
    id: number;
    code: string;
    name: string;
    description: string;
    status: number;
    statusText: string;
    lastModifiedDate?: string;
    lastModifiedBy?: number;
    // Optional enriched fields for UI display/forms
    materialCode?: string; // alias for code
    uomId?: number;
    uomCode?: string;
    uomName?: string;
    categoryId?: number;
    categoryName?: string;
    hsnCode?: string;
    gstRate?: number;
    minStockLevel?: number;
    maxStockLevel?: number;
    reorderLevel?: number;
    isActive?: boolean; // derived from status === 1
    updatedAt?: string; // alias for lastModifiedDate
    createdAt?: string; // creation timestamp
}

// Extended Material for forms with additional frontend fields
export interface MaterialWithUOM extends Material {
    uomId?: number;
    uomCode?: string;
    uomName?: string;
}

export interface MaterialCreateRequest {
    code: string;
    name: string;
    description?: string;
    status?: number;
    // Optional extended fields (may not be supported by backend)
    materialCode?: string; // alias for code
    uomId?: number;
    categoryId?: number;
    hsnCode?: string;
    gstRate?: number;
    minStockLevel?: number;
    maxStockLevel?: number;
    reorderLevel?: number;
    isActive?: boolean;
}

export interface MaterialUpdateRequest extends Partial<MaterialCreateRequest> {}

export interface MaterialSearchParams extends PageRequest {
    search?: string;
    searchTerm?: string;
    activeOnly?: boolean;
    isActive?: boolean;
    categoryId?: number;
}

// ============== API Functions ==============

export const materialsApi = {
    list: async (
        params: MaterialSearchParams = {}
    ): Promise<PageResponse<Material>> => {
        const response = await apiClient.get<PageResponse<Material>>(
            "/materials",
            { params }
        );
        return response.data;
    },

    getById: async (id: number): Promise<Material> => {
        const response = await apiClient.get<Material>(`/materials/${id}`);
        return response.data;
    },

    getByCode: async (code: string): Promise<Material> => {
        const response = await apiClient.get<Material>(
            `/materials/code/${code}`
        );
        return response.data;
    },

    create: async (data: MaterialCreateRequest): Promise<Material> => {
        const response = await apiClient.post<Material>("/materials", data);
        return response.data;
    },

    update: async (
        id: number,
        data: MaterialUpdateRequest
    ): Promise<Material> => {
        const response = await apiClient.put<Material>(
            `/materials/${id}`,
            data
        );
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/materials/${id}`);
    },

    bulkImport: async (
        file: File
    ): Promise<{ success: number; failed: number; errors: string[] }> => {
        const formData = new FormData();
        formData.append("file", file);
        const response = await apiClient.post(
            "/materials/bulk-import",
            formData,
            {
                headers: { "Content-Type": "multipart/form-data" },
            }
        );
        return response.data;
    },

    downloadTemplate: async (): Promise<Blob> => {
        const response = await apiClient.get("/materials/template", {
            responseType: "blob",
        });
        return response.data;
    },

    activate: async (id: number): Promise<Material> => {
        const response = await apiClient.put<Material>(
            `/materials/${id}/activate`
        );
        return response.data;
    },

    deactivate: async (id: number): Promise<Material> => {
        const response = await apiClient.put<Material>(
            `/materials/${id}/deactivate`
        );
        return response.data;
    },
};

export default materialsApi;
