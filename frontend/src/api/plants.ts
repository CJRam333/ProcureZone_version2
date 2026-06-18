import apiClient from "./client";

export interface Plant {
    id: number;
    code: string;
    name: string;
    status: number;
    companyId?: number; // optional: not returned by backend today; used as fallback key in plant-material mappings
    createdAt?: string;
    updatedAt?: string;
}

export interface PlantCreateRequest {
    code: string;
    name: string;
    status?: number;
}

export interface PlantUpdateRequest {
    code?: string;
    name?: string;
    status?: number;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export const plantsApi = {
    getAll: async (
        page = 0,
        size = 20,
        search?: string
    ): Promise<PageResponse<Plant>> => {
        const params: Record<string, any> = { page, size };
        if (search) params.searchTerm = search;
        const endpoint = search ? "/plants/search" : "/plants";
        const response = await apiClient.get<PageResponse<Plant>>(endpoint, {
            params,
        });
        return response.data;
    },

    getActive: async (page = 0, size = 100): Promise<PageResponse<Plant>> => {
        const response = await apiClient.get<PageResponse<Plant>>(
            "/plants/active",
            {
                params: { page, size },
            }
        );
        return response.data;
    },

    getById: async (id: number): Promise<Plant> => {
        const response = await apiClient.get<Plant>(`/plants/${id}`);
        return response.data;
    },

    create: async (data: PlantCreateRequest): Promise<Plant> => {
        const response = await apiClient.post<Plant>("/plants", data);
        return response.data;
    },

    update: async (id: number, data: PlantUpdateRequest): Promise<Plant> => {
        const response = await apiClient.put<Plant>(`/plants/${id}`, data);
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/plants/${id}`);
    },
};

export default plantsApi;
