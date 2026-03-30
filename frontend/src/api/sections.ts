import apiClient from "./client";

export interface Section {
    id: number;
    code: string;
    name: string;
    status: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface SectionCreateRequest {
    code: string;
    name: string;
    status?: number;
}

export interface SectionUpdateRequest {
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

export const sectionsApi = {
    getAll: async (
        page = 0,
        size = 20,
        search?: string
    ): Promise<PageResponse<Section>> => {
        const params: Record<string, any> = { page, size };
        if (search) params.searchTerm = search;
        const response = await apiClient.get<PageResponse<Section>>(
            "/sections",
            { params }
        );
        return response.data;
    },

    getActive: async (page = 0, size = 100): Promise<PageResponse<Section>> => {
        const response = await apiClient.get<PageResponse<Section>>(
            "/sections/active",
            {
                params: { page, size },
            }
        );
        return response.data;
    },

    getById: async (id: number): Promise<Section> => {
        const response = await apiClient.get<Section>(`/sections/${id}`);
        return response.data;
    },

    create: async (data: SectionCreateRequest): Promise<Section> => {
        const response = await apiClient.post<Section>("/sections", data);
        return response.data;
    },

    update: async (
        id: number,
        data: SectionUpdateRequest
    ): Promise<Section> => {
        const response = await apiClient.put<Section>(`/sections/${id}`, data);
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/sections/${id}`);
    },
};

export default sectionsApi;
