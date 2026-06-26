import apiClient from "./client";

// ============== Types ==============
export interface LoginRequest {
    username: string;
    password: string;
}

// Backend returns only { accessToken, user } — no tokenType, expiresAt, refreshToken
export interface LoginResponse {
    accessToken: string;
    user: UserInfo;
}

export interface UserInfo {
    // Fields from backend AuthenticatedUser
    userId: number;
    employeeNumber?: number;
    employeeId: string;
    displayName: string;
    email: string;
    roles: string[];
    canView: boolean;
    canAdd: boolean;
    canEdit: boolean;
    canDelete: boolean;

    // Additional fields that may be available (not in AuthenticatedUser but kept optional)
    username?: string;
    employeeName?: string;
    employeeCode?: string;
    departmentId?: number;
    departmentName?: string;
    companyName?: string;
    locationName?: string;
    designation?: string;
    plantId?: number;
    plantName?: string;
    permissions?: string[];
    // Module access codes — fetched after login from /module-access/my-modules
    allowedModules?: string[];
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

// ============== API Functions ==============

export const authApi = {
    login: async (data: LoginRequest): Promise<LoginResponse> => {
        const response = await apiClient.post<LoginResponse>(
            "/auth/login",
            data
        );
        return response.data;
    },

    logout: async (): Promise<void> => {
        await apiClient.post("/auth/logout");
    },

    // No /auth/refresh endpoint exists in backend.
    // JWT expires after 1 hour. On 401, user must re-login.

    getMe: async (): Promise<UserInfo> => {
        const response = await apiClient.get<UserInfo>("/auth/me");
        return response.data;
    },

    changePassword: async (data: ChangePasswordRequest): Promise<void> => {
        await apiClient.patch("/auth/change-password", {
            currentPassword: data.currentPassword,
            newPassword: data.newPassword,
        });
    },
};

export default authApi;

