import apiClient from "./client";

// ============== Types ==============

export interface DashboardStatistics {
    totalIndents: number;
    totalPOs: number;
    totalGRNs: number;
    totalIssueNotes: number;
    totalVendors: number;
    totalMaterials: number;
    totalEmployees: number;
    pendingApprovals: number;
}

export interface DashboardSummary {
    indentCount: number;
    poCount: number;
    grnCount: number;
    issueNoteCount: number;
    pendingIndents: number;
    pendingPOs: number;
    pendingGRNs: number;
    pendingIssueNotes: number;
}

export interface MonthlyTrend {
    month: string;
    indents: number;
    pos: number;
    grns: number;
    issueNotes: number;
}

export interface DepartmentBreakdown {
    departmentId: number;
    departmentName: string;
    indentCount: number;
    poCount: number;
    totalValue: number;
}

export interface TopMaterial {
    materialId: number;
    materialCode: string;
    materialName: string;
    totalQuantity: number;
    totalValue: number;
    orderCount: number;
}

export interface PendingApprovalItem {
    id: number;
    type: string; // INDENT, PO, GRN, ISSUE_NOTE
    number: string;
    requestedBy: string;
    date: string;
    status: string;
}

export interface SystemAlert {
    id: number;
    type: string;
    message: string;
    severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
    timestamp: string;
}

export interface LatestActivityItem {
    type: "INDENT" | "ISSUE_NOTE";
    id: number;
    documentNumber: string;
    displayStatus: string;
    lastModifiedDate: string | null;
    creatorName: string | null;
}

// ============== API Functions ==============

export const dashboardApi = {
    // Comprehensive statistics
    getStatistics: async (): Promise<DashboardStatistics> => {
        const response = await apiClient.get<DashboardStatistics>(
            "/dashboard/statistics"
        );
        return response.data;
    },

    // Summary cards
    getSummary: async (): Promise<DashboardSummary> => {
        const response = await apiClient.get<DashboardSummary>(
            "/dashboard/summary"
        );
        return response.data;
    },

    // Module-specific stats
    getIndentStats: async (): Promise<any> => {
        const response = await apiClient.get("/dashboard/indents/stats");
        return response.data;
    },

    getPOStats: async (): Promise<any> => {
        const response = await apiClient.get(
            "/dashboard/purchase-orders/stats"
        );
        return response.data;
    },

    getGRNStats: async (): Promise<any> => {
        const response = await apiClient.get("/dashboard/goods-receipts/stats");
        return response.data;
    },

    getIssueNoteStats: async (): Promise<any> => {
        const response = await apiClient.get("/dashboard/issue-notes/stats");
        return response.data;
    },

    getInventoryStats: async (): Promise<any> => {
        const response = await apiClient.get("/dashboard/inventory/stats");
        return response.data;
    },

    getVendorStats: async (): Promise<any> => {
        const response = await apiClient.get("/dashboard/vendors/stats");
        return response.data;
    },

    // Trends & analytics
    getMonthlyTrends: async (): Promise<MonthlyTrend[]> => {
        const response = await apiClient.get<MonthlyTrend[]>(
            "/dashboard/trends/monthly"
        );
        return response.data;
    },

    getDepartmentBreakdown: async (): Promise<DepartmentBreakdown[]> => {
        const response = await apiClient.get<DepartmentBreakdown[]>(
            "/dashboard/breakdown/department"
        );
        return response.data;
    },

    getTopMaterials: async (): Promise<TopMaterial[]> => {
        const response = await apiClient.get<TopMaterial[]>(
            "/dashboard/top-materials"
        );
        return response.data;
    },

    // Approvals & alerts
    getPendingApprovals: async (): Promise<PendingApprovalItem[]> => {
        const response = await apiClient.get<PendingApprovalItem[]>(
            "/dashboard/pending-approvals"
        );
        return response.data;
    },

    getAlerts: async (): Promise<SystemAlert[]> => {
        const response = await apiClient.get<SystemAlert[]>(
            "/dashboard/alerts"
        );
        return response.data;
    },

    // Unified "Latest Activity" feed (indents + issue notes), role-scoped server-side.
    getLatestActivity: async (limit = 15): Promise<LatestActivityItem[]> => {
        const response = await apiClient.get<LatestActivityItem[]>(
            "/dashboard/latest-activity",
            { params: { limit } }
        );
        return response.data;
    },
};

export default dashboardApi;
