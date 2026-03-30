import apiClient from "./client";

export interface User {
    id: number;
    username: string;
    employeeId: number;
    employeeName?: string;
    employeeEmail?: string;
    status: number;
    lastLoginIp?: string;
    lastModifiedDate?: string;
    createdAt?: string;
}

export interface UserCreateRequest {
    username: string;
    password: string;
    employeeId: number;
    status?: number;
}

export interface UserUpdateRequest {
    username?: string;
    password?: string;
    status?: number;
}

export interface ChangePasswordRequest {
    oldPassword: string;
    newPassword: string;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export const usersApi = {
    getAll: async (
        page = 0,
        size = 20,
        search?: string
    ): Promise<PageResponse<User>> => {
        const params: Record<string, any> = { page, size };
        if (search) params.search = search;
        const response = await apiClient.get<PageResponse<User>>("/users", {
            params,
        });
        return response.data;
    },

    getActive: async (): Promise<User[]> => {
        const response = await apiClient.get<User[]>("/users/active");
        return response.data;
    },

    getById: async (id: number): Promise<User> => {
        const response = await apiClient.get<User>(`/users/${id}`);
        return response.data;
    },

    getByUsername: async (username: string): Promise<User> => {
        const response = await apiClient.get<User>(
            `/users/username/${username}`
        );
        return response.data;
    },

    create: async (data: UserCreateRequest): Promise<User> => {
        const response = await apiClient.post<User>("/users", data);
        return response.data;
    },

    update: async (id: number, data: UserUpdateRequest): Promise<User> => {
        const response = await apiClient.put<User>(`/users/${id}`, data);
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/users/${id}`);
    },

    changePassword: async (
        userId: number,
        data: ChangePasswordRequest
    ): Promise<void> => {
        await apiClient.post(`/users/${userId}/change-password`, data);
    },

    resetPassword: async (
        userId: number,
        newPassword: string
    ): Promise<void> => {
        await apiClient.post(`/users/${userId}/reset-password`, {
            newPassword,
        });
    },
};

export default usersApi;
