import apiClient from "./client";

export interface NotificationItem {
    id: number;
    recipientEmpNumber: number;
    type: string;
    title: string;
    message: string | null;
    entityType: string | null;
    entityId: number | null;
    link: string | null;
    read: boolean;
    createdAt: string;
    readAt: string | null;
}

export interface NotificationPage {
    content: NotificationItem[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

export const notificationsApi = {
    getList: async (page = 0, size = 10): Promise<NotificationPage> => {
        const response = await apiClient.get<NotificationPage>(
            `/notifications?page=${page}&size=${size}`
        );
        return response.data;
    },

    getUnreadCount: async (): Promise<number> => {
        const response = await apiClient.get<{ count: number }>(
            "/notifications/unread-count"
        );
        return response.data.count;
    },

    markAsRead: async (id: number): Promise<void> => {
        await apiClient.patch(`/notifications/${id}/read`);
    },

    markAllAsRead: async (): Promise<void> => {
        await apiClient.patch("/notifications/read-all");
    },
};

export default notificationsApi;
