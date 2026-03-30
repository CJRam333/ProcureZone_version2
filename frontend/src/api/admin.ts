import apiClient from "./client";

export interface SystemConfig {
    id: number;
    configKey: string;
    configValue: string;
    description?: string;
    category: string;
    isEditable: boolean;
    updatedAt?: string;
    updatedBy?: string;
}

export interface AuditLog {
    id: number;
    action: string;
    entityType: string;
    entityId: number;
    userId: number;
    userName?: string;
    ipAddress?: string;
    details?: string;
    oldValue?: string;
    newValue?: string;
    timestamp: string;
}

export interface SystemStats {
    totalUsers: number;
    activeUsers: number;
    totalIndents: number;
    totalPOs: number;
    totalGRNs: number;
    pendingApprovals: number;
    todayLogins: number;
    systemUptime: string;
}

export interface BackupInfo {
    id: number;
    fileName: string;
    fileSize: number;
    createdAt: string;
    createdBy: string;
    status: string;
}

export const adminApi = {
    // System Configuration
    getConfigs: async (): Promise<SystemConfig[]> => {
        const response = await apiClient.get<SystemConfig[]>("/admin/configs");
        return response.data;
    },

    updateConfig: async (id: number, value: string): Promise<SystemConfig> => {
        const response = await apiClient.put<SystemConfig>(
            `/admin/configs/${id}`,
            { value }
        );
        return response.data;
    },

    // Audit Logs
    getAuditLogs: async (
        params: {
            page?: number;
            size?: number;
            startDate?: string;
            endDate?: string;
            action?: string;
            entityType?: string;
            userId?: number;
        } = {}
    ): Promise<{
        content: AuditLog[];
        totalElements: number;
        totalPages: number;
    }> => {
        const response = await apiClient.get("/admin/audit-logs", { params });
        return response.data;
    },

    // System Stats
    getSystemStats: async (): Promise<SystemStats> => {
        const response = await apiClient.get<SystemStats>("/admin/stats");
        return response.data;
    },

    // Backup & Restore
    getBackups: async (): Promise<BackupInfo[]> => {
        const response = await apiClient.get<BackupInfo[]>("/admin/backups");
        return response.data;
    },

    createBackup: async (): Promise<BackupInfo> => {
        const response = await apiClient.post<BackupInfo>("/admin/backups");
        return response.data;
    },

    downloadBackup: async (id: number): Promise<Blob> => {
        const response = await apiClient.get(`/admin/backups/${id}/download`, {
            responseType: "blob",
        });
        return response.data;
    },

    restoreBackup: async (
        id: number
    ): Promise<{ success: boolean; message: string }> => {
        const response = await apiClient.post(`/admin/backups/${id}/restore`);
        return response.data;
    },

    // Cache Management
    clearCache: async (
        cacheType?: string
    ): Promise<{ success: boolean; message: string }> => {
        const response = await apiClient.post("/admin/cache/clear", {
            cacheType,
        });
        return response.data;
    },

    // Email Settings
    testEmailSettings: async (
        email: string
    ): Promise<{ success: boolean; message: string }> => {
        const response = await apiClient.post("/admin/email/test", { email });
        return response.data;
    },

    // User Management Shortcuts
    resetUserPassword: async (
        userId: number
    ): Promise<{ success: boolean; newPassword: string }> => {
        const response = await apiClient.post(
            `/admin/users/${userId}/reset-password`
        );
        return response.data;
    },

    toggleUserStatus: async (
        userId: number
    ): Promise<{ success: boolean; newStatus: number }> => {
        const response = await apiClient.post(
            `/admin/users/${userId}/toggle-status`
        );
        return response.data;
    },
};

export default adminApi;
