import apiClient from "./client";

export interface Company {
    id: number;
    code: string;
    name: string;
    status: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface CompanyCreateRequest {
    code: string;
    name: string;
    status?: number;
}

export interface CompanyUpdateRequest {
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

export const companiesApi = {
    getAll: async (
        page = 0,
        size = 20,
        search?: string
    ): Promise<PageResponse<Company>> => {
        const params: Record<string, any> = { page, size };
        if (search) params.searchTerm = search;
        const endpoint = search ? "/companies/search" : "/companies";
        const response = await apiClient.get<PageResponse<Company>>(
            endpoint,
            { params }
        );
        return response.data;
    },

    getActive: async (page = 0, size = 100): Promise<PageResponse<Company>> => {
        const response = await apiClient.get<PageResponse<Company>>(
            "/companies/active",
            {
                params: { page, size },
            }
        );
        return response.data;
    },

    getById: async (id: number): Promise<Company> => {
        const response = await apiClient.get<Company>(`/companies/${id}`);
        return response.data;
    },

    create: async (data: CompanyCreateRequest): Promise<Company> => {
        const response = await apiClient.post<Company>("/companies", data);
        return response.data;
    },

    update: async (
        id: number,
        data: CompanyUpdateRequest
    ): Promise<Company> => {
        const response = await apiClient.put<Company>(`/companies/${id}`, data);
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/companies/${id}`);
    },
};

export default companiesApi;
