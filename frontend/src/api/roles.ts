import apiClient from "./client";

export interface Role {
    id: number;
    code: string;
    name: string;
    canView: boolean;
    canAdd: boolean;
    canEdit: boolean;
    canDelete: boolean;
    status: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface RoleCreateRequest {
    code: string;
    name: string;
    canView?: boolean;
    canAdd?: boolean;
    canEdit?: boolean;
    canDelete?: boolean;
    status?: number;
}

export interface RoleUpdateRequest {
    code?: string;
    name?: string;
    canView?: boolean;
    canAdd?: boolean;
    canEdit?: boolean;
    canDelete?: boolean;
    status?: number;
}

export interface EmployeeRole {
    id: number;
    employeeId: number;
    employeeName?: string;
    roleId: number;
    roleName?: string;
    status: number;
    assignedBy?: number;
    assignedDate?: string;
    remarks?: string;
}

export interface EmployeeRoleAssignRequest {
    employeeId: number;
    roleId: number;
    remarks?: string;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export const rolesApi = {
    getAll: async (page = 0, size = 20): Promise<PageResponse<Role>> => {
        const response = await apiClient.get<PageResponse<Role>>("/roles", {
            params: { page, size },
        });
        return response.data;
    },

    getActive: async (): Promise<Role[]> => {
        const response = await apiClient.get<Role[]>("/roles/active");
        return response.data;
    },

    getById: async (id: number): Promise<Role> => {
        const response = await apiClient.get<Role>(`/roles/${id}`);
        return response.data;
    },

    create: async (data: RoleCreateRequest): Promise<Role> => {
        const response = await apiClient.post<Role>("/roles", data);
        return response.data;
    },

    update: async (id: number, data: RoleUpdateRequest): Promise<Role> => {
        const response = await apiClient.put<Role>(`/roles/${id}`, data);
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/roles/${id}`);
    },

    // Employee Role Assignments
    getEmployeeRoles: async (employeeId: number): Promise<EmployeeRole[]> => {
        const response = await apiClient.get<EmployeeRole[]>(
            `/employee-roles/employee/${employeeId}/roles`
        );
        return response.data;
    },

    assignRole: async (
        data: EmployeeRoleAssignRequest
    ): Promise<EmployeeRole> => {
        const response = await apiClient.post<EmployeeRole>(
            "/employee-roles",
            data
        );
        return response.data;
    },

    removeRole: async (id: number): Promise<void> => {
        await apiClient.delete(`/employee-roles/${id}`);
    },
};

export default rolesApi;
