import apiClient from "./client";

// ============== Types ==============
export interface UserProfile {
    id: number;
    username: string;
    fullName: string;
    email: string;
    phone?: string;
    department?: string;
    designation?: string;
    address?: string;
    avatarUrl?: string;
    roles: string[];
    createdAt: string;
    updatedAt: string;
}

export interface UpdateProfileRequest {
    fullName?: string;
    email?: string;
    phone?: string;
    department?: string;
    designation?: string;
    address?: string;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

// ============== API Functions ==============

export const userApi = {
    getProfile: async (): Promise<UserProfile> => {
        const response = await apiClient.get<UserProfile>("/users/profile");
        return response.data;
    },

    updateProfile: async (data: UpdateProfileRequest): Promise<UserProfile> => {
        const response = await apiClient.put<UserProfile>(
            "/users/profile",
            data
        );
        return response.data;
    },

    changePassword: async (
        userId: number,
        data: ChangePasswordRequest
    ): Promise<void> => {
        await apiClient.post(`/users/${userId}/change-password`, data);
    },

    uploadAvatar: async (file: File): Promise<{ avatarUrl: string }> => {
        const formData = new FormData();
        formData.append("file", file);
        const response = await apiClient.post<{ avatarUrl: string }>(
            "/users/avatar",
            formData,
            {
                headers: {
                    "Content-Type": "multipart/form-data",
                },
            }
        );
        return response.data;
    },
};

export default userApi;
