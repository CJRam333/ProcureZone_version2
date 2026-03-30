import apiClient from "./client";

export interface UOM {
    id: number;
    code: string;
    name: string;
    status: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface UOMCreateRequest {
    code: string;
    name: string;
    status?: number;
}

export interface UOMUpdateRequest {
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

export const uomApi = {
    getAll: async (
        page = 0,
        size = 20,
        search?: string
    ): Promise<PageResponse<UOM>> => {
        const params: Record<string, any> = { page, size };
        if (search) params.searchTerm = search;
        const response = await apiClient.get<PageResponse<UOM>>(
            "/unit-of-measures",
            {
                params,
            }
        );
        return response.data;
    },

    getActive: async (page = 0, size = 100): Promise<PageResponse<UOM>> => {
        const response = await apiClient.get<PageResponse<UOM>>(
            "/unit-of-measures/active",
            {
                params: { page, size },
            }
        );
        return response.data;
    },

    getById: async (id: number): Promise<UOM> => {
        const response = await apiClient.get<UOM>(`/unit-of-measures/${id}`);
        return response.data;
    },

    create: async (data: UOMCreateRequest): Promise<UOM> => {
        const response = await apiClient.post<UOM>("/unit-of-measures", data);
        return response.data;
    },

    update: async (id: number, data: UOMUpdateRequest): Promise<UOM> => {
        const response = await apiClient.put<UOM>(
            `/unit-of-measures/${id}`,
            data
        );
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/unit-of-measures/${id}`);
    },
};

export default uomApi;
