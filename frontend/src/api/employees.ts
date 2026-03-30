import apiClient from "./client";

export interface Employee {
    id: number;
    empId: string;
    empName: string;
    empEmail: string;
    empDesignation?: string;
    empJoinDate?: string;
    empCostCenter?: string;
    empStatus: number;
    departmentId?: number;
    departmentName?: string;
    locationId?: number;
    locationName?: string;
    companyId?: number;
    companyName?: string;
    plantName?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface EmployeeCreateRequest {
    empId: string;
    empName: string;
    empEmail: string;
    empDesignation?: string;
    empJoinDate?: string;
    empCostCenter?: string;
    empStatus?: number;
    departmentId?: number;
    locationId?: number;
    companyId?: number;
    plantName?: string;
}

export interface EmployeeUpdateRequest {
    empId?: string;
    empName?: string;
    empEmail?: string;
    empDesignation?: string;
    empJoinDate?: string;
    empCostCenter?: string;
    empStatus?: number;
    departmentId?: number;
    locationId?: number;
    companyId?: number;
    plantName?: string;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export const employeesApi = {
    getAll: async (
        page = 0,
        size = 20,
        search?: string
    ): Promise<PageResponse<Employee>> => {
        const params: Record<string, any> = { page, size };
        if (search) params.search = search;
        const response = await apiClient.get<PageResponse<Employee>>(
            "/employees",
            { params }
        );
        return response.data;
    },

    getActive: async (
        page = 0,
        size = 100
    ): Promise<PageResponse<Employee>> => {
        const response = await apiClient.get<PageResponse<Employee>>(
            "/employees/active",
            {
                params: { page, size },
            }
        );
        return response.data;
    },

    getAvailableForUser: async (
        currentEmployeeId?: number | null,
        page = 0,
        size = 100
    ): Promise<PageResponse<Employee>> => {
        const params: Record<string, any> = { page, size };
        if (currentEmployeeId) params.currentEmployeeId = currentEmployeeId;
        const response = await apiClient.get<PageResponse<Employee>>(
            "/employees/available-for-user",
            { params }
        );
        return response.data;
    },

    getById: async (id: number): Promise<Employee> => {
        const response = await apiClient.get<Employee>(`/employees/${id}`);
        return response.data;
    },

    create: async (data: EmployeeCreateRequest): Promise<Employee> => {
        const response = await apiClient.post<Employee>("/employees", data);
        return response.data;
    },

    update: async (
        id: number,
        data: EmployeeUpdateRequest
    ): Promise<Employee> => {
        const response = await apiClient.put<Employee>(
            `/employees/${id}`,
            data
        );
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/employees/${id}`);
    },
};

export default employeesApi;
