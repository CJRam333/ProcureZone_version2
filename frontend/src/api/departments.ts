import apiClient from "./client";

export interface Department {
    id: number;
    code: string;
    name: string;
    status: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface DepartmentCreateRequest {
    code: string;
    name: string;
    status?: number;
}

export interface DepartmentUpdateRequest {
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

export const departmentsApi = {
    getAll: async (
        page = 0,
        size = 20,
        search?: string
    ): Promise<PageResponse<Department>> => {
        const params: Record<string, any> = { page, size };
        if (search) params.searchTerm = search;
        const endpoint = search ? "/departments/search" : "/departments";
        const response = await apiClient.get<PageResponse<Department>>(
            endpoint,
            { params }
        );
        return response.data;
    },

    getActive: async (
        page = 0,
        size = 100
    ): Promise<PageResponse<Department>> => {
        const response = await apiClient.get<PageResponse<Department>>(
            "/departments/active",
            {
                params: { page, size },
            }
        );
        return response.data;
    },

    getById: async (id: number): Promise<Department> => {
        const response = await apiClient.get<Department>(`/departments/${id}`);
        return response.data;
    },

    create: async (data: DepartmentCreateRequest): Promise<Department> => {
        const response = await apiClient.post<Department>("/departments", data);
        return response.data;
    },

    update: async (
        id: number,
        data: DepartmentUpdateRequest
    ): Promise<Department> => {
        const response = await apiClient.put<Department>(
            `/departments/${id}`,
            data
        );
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/departments/${id}`);
    },
};

export default departmentsApi;
