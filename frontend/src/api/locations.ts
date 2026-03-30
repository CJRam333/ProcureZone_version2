import apiClient from "./client";

export interface Location {
    id: number;
    code: string;
    name: string;
    status: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface LocationCreateRequest {
    code: string;
    name: string;
    status?: number;
}

export interface LocationUpdateRequest {
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

export const locationsApi = {
    getAll: async (
        page = 0,
        size = 20,
        search?: string
    ): Promise<PageResponse<Location>> => {
        const params: Record<string, any> = { page, size };
        if (search) params.searchTerm = search;
        const response = await apiClient.get<PageResponse<Location>>(
            "/locations",
            { params }
        );
        return response.data;
    },

    getActive: async (
        page = 0,
        size = 100
    ): Promise<PageResponse<Location>> => {
        const response = await apiClient.get<PageResponse<Location>>(
            "/locations/active",
            {
                params: { page, size },
            }
        );
        return response.data;
    },

    getById: async (id: number): Promise<Location> => {
        const response = await apiClient.get<Location>(`/locations/${id}`);
        return response.data;
    },

    create: async (data: LocationCreateRequest): Promise<Location> => {
        const response = await apiClient.post<Location>("/locations", data);
        return response.data;
    },

    update: async (
        id: number,
        data: LocationUpdateRequest
    ): Promise<Location> => {
        const response = await apiClient.put<Location>(
            `/locations/${id}`,
            data
        );
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/locations/${id}`);
    },
};

export default locationsApi;
