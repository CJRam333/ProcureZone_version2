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

// Material response matching backend MaterialResponse
export interface Material {
    id: number;
    // Primary fields from aligned backend
    materialCode: string;       // material_code
    materialName: string;       // material_name
    description: string;
    status: number;
    statusText: string;
    isActive: boolean;          // derived server-side: status === 1
    stockQuantity: number;      // sum of map_quantity_stores across all plants
    companyName?: string;       // from dropdown endpoint
    plantName?: string;         // from dropdown endpoint
    lastModifiedDate?: string;
    lastModifiedBy?: number;
    // Legacy aliases — kept so other pages compile without changes
    code: string;               // backward compat alias (runtime value may be undefined)
    name: string;               // backward compat alias (runtime value may be undefined)
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
    updatedAt?: string;
    createdAt?: string;
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

// Dropdown item returned by GET /materials/dropdown — includes company and stock
export interface MaterialDropdownItem {
    materialId: number;
    materialCode: string;
    materialName: string;
    materialDescription: string | null;
    companyId: number;
    companyName: string;
    plantId: number;
    plantName: string;
    stockQuantity: number | null;
}

// ============== API Functions ==============

export const materialsApi = {
    list: async (
        params: MaterialSearchParams = {}
    ): Promise<PageResponse<Material>> => {
        const { search, searchTerm, isActive, activeOnly, categoryId, page, size, sort, direction } = params;
        const term = search || searchTerm;

        if (term) {
            // Route to dedicated search endpoint which handles searchTerm + activeOnly
            const searchParams: Record<string, any> = {
                page: page ?? 0,
                size: size ?? 20,
                searchTerm: term,
            };
            if (isActive !== undefined) searchParams.activeOnly = isActive;
            if (activeOnly !== undefined) searchParams.activeOnly = activeOnly;
            const response = await apiClient.get<PageResponse<Material>>(
                "/materials/search",
                { params: searchParams }
            );
            return response.data;
        }

        // No search term — use main list endpoint
        const listParams: Record<string, any> = { page: page ?? 0, size: size ?? 20 };
        if (isActive !== undefined) listParams.activeOnly = isActive;
        if (sort) listParams.sort = direction ? `${sort},${direction}` : sort;
        const response = await apiClient.get<PageResponse<Material>>(
            "/materials",
            { params: listParams }
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

    dropdown: async (search?: string): Promise<MaterialDropdownItem[]> => {
        const params: Record<string, string> = {};
        if (search) params.search = search;
        const response = await apiClient.get<MaterialDropdownItem[]>("/materials/dropdown", { params });
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
