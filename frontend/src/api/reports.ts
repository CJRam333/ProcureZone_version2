import apiClient from "./client";

export interface ReportParams {
    startDate?: string;
    endDate?: string;
    departmentId?: number;
    plantId?: number;
    companyId?: number;
    status?: string;
    format?: "json" | "excel" | "pdf";
}

export interface ReportSummary {
    totalCount: number;
    totalValue: number;
    pendingCount: number;
    approvedCount: number;
    rejectedCount: number;
    byStatus: Record<string, number>;
    byDepartment: Record<string, number>;
    byMonth: { month: string; count: number; value: number }[];
}

export interface IndentReportItem {
    indentId: number;
    indentNo: string;
    indentDate: string;
    departmentName: string;
    employeeName: string;
    totalItems: number;
    totalValue: number;
    status: string;
    approvedDate?: string;
}

export interface POReportItem {
    poId: number;
    poNumber: string;
    poDate: string;
    vendorName: string;
    totalAmount: number;
    status: string;
    deliveryDate?: string;
}

export interface InventoryReportItem {
    materialId: number;
    materialCode: string;
    materialName: string;
    uomName: string;
    currentStock: number;
    reservedQty: number;
    availableQty: number;
    reorderLevel: number;
    status: string;
}

export interface VendorPerformanceItem {
    vendorId: number;
    vendorCode: string;
    vendorName: string;
    totalOrders: number;
    totalValue: number;
    onTimeDeliveryRate: number;
    qualityRating: number;
    averageLeadTime: number;
}

export const reportsApi = {
    // Indent Reports
    getIndentSummary: async (
        params: ReportParams = {}
    ): Promise<ReportSummary> => {
        const response = await apiClient.get<ReportSummary>(
            "/reports/indent-summary",
            { params }
        );
        return response.data;
    },

    getIndentDetails: async (
        params: ReportParams = {}
    ): Promise<IndentReportItem[]> => {
        const response = await apiClient.get<IndentReportItem[]>(
            "/reports/indent-details",
            { params }
        );
        return response.data;
    },

    // PO Reports
    getPOSummary: async (params: ReportParams = {}): Promise<ReportSummary> => {
        const response = await apiClient.get<ReportSummary>(
            "/reports/po-summary",
            { params }
        );
        return response.data;
    },

    getPODetails: async (
        params: ReportParams = {}
    ): Promise<POReportItem[]> => {
        const response = await apiClient.get<POReportItem[]>(
            "/reports/po-details",
            { params }
        );
        return response.data;
    },

    // Inventory Reports
    getInventorySummary: async (
        params: ReportParams = {}
    ): Promise<ReportSummary> => {
        const response = await apiClient.get<ReportSummary>(
            "/reports/inventory-summary",
            { params }
        );
        return response.data;
    },

    getInventoryDetails: async (
        params: ReportParams = {}
    ): Promise<InventoryReportItem[]> => {
        const response = await apiClient.get<InventoryReportItem[]>(
            "/reports/inventory-details",
            { params }
        );
        return response.data;
    },

    getLowStockItems: async (
        params: ReportParams = {}
    ): Promise<InventoryReportItem[]> => {
        const response = await apiClient.get<InventoryReportItem[]>(
            "/reports/low-stock",
            { params }
        );
        return response.data;
    },

    // Vendor Reports
    getVendorPerformance: async (
        params: ReportParams = {}
    ): Promise<VendorPerformanceItem[]> => {
        const response = await apiClient.get<VendorPerformanceItem[]>(
            "/reports/vendor-performance",
            { params }
        );
        return response.data;
    },

    // Export Reports
    exportReport: async (
        reportType: string,
        params: ReportParams = {}
    ): Promise<Blob> => {
        const response = await apiClient.get(`/reports/export/${reportType}`, {
            params: { ...params, format: params.format || "excel" },
            responseType: "blob",
        });
        return response.data;
    },

    // Dashboard Stats
    getDashboardStats: async (): Promise<{
        indentStats: { total: number; pending: number; approved: number; rejected: number };
        poStats: { total: number; pending: number; delivered: number };
        grnStats: { total: number; pending: number };
        inventoryStats: {
            totalItems: number;
            lowStock: number;
            outOfStock: number;
        };
    }> => {
        const response = await apiClient.get("/reports/dashboard-stats");
        return response.data;
    },

    // ============================================================================
    // NEW: INVENTORY REPORTS
    // ============================================================================

    getInventoryStockStatus: async (params?: {
        companyId?: number;
        plantId?: number;
        locationId?: number;
    }): Promise<any[]> => {
        const response = await apiClient.get(
            "/reports/inventory/stock-status",
            { params }
        );
        return response.data;
    },

    getLowStockAlerts: async (companyId?: number): Promise<any[]> => {
        const response = await apiClient.get("/reports/inventory/low-stock", {
            params: { companyId },
        });
        return response.data;
    },

    getMaterialUsageReport: async (
        startDate: string,
        endDate: string,
        materialId?: number
    ): Promise<any[]> => {
        const response = await apiClient.get(
            "/reports/inventory/material-usage",
            {
                params: { startDate, endDate, materialId },
            }
        );
        return response.data;
    },

    getInventoryValueReport: async (companyId?: number): Promise<any[]> => {
        const response = await apiClient.get("/reports/inventory/value", {
            params: { companyId },
        });
        return response.data;
    },

    // ============================================================================
    // NEW: VENDOR PERFORMANCE REPORTS (Extended)
    // ============================================================================

    getVendorPerformanceByDate: async (
        startDate: string,
        endDate: string,
        vendorId?: number
    ): Promise<any[]> => {
        const response = await apiClient.get("/reports/vendor-performance", {
            params: { startDate, endDate, vendorId },
        });
        return response.data;
    },

    getVendorDeliveryPerformance: async (
        startDate: string,
        endDate: string
    ): Promise<any[]> => {
        const response = await apiClient.get(
            "/reports/vendor-performance/delivery",
            {
                params: { startDate, endDate },
            }
        );
        return response.data;
    },

    getVendorRanking: async (
        startDate: string,
        endDate: string,
        rankBy?: "value" | "quality" | "delivery" | "volume"
    ): Promise<any[]> => {
        const response = await apiClient.get(
            "/reports/vendor-performance/ranking",
            {
                params: { startDate, endDate, rankBy: rankBy || "value" },
            }
        );
        return response.data;
    },

    // ============================================================================
    // NEW: DOCUMENT GENERATION
    // ============================================================================

    downloadPOPdf: async (poId: number): Promise<Blob> => {
        const response = await apiClient.get(`/documents/po/${poId}/pdf`, {
            responseType: "blob",
        });
        return response.data;
    },

    previewPOPdf: async (poId: number): Promise<Blob> => {
        const response = await apiClient.get(`/documents/po/${poId}/preview`, {
            responseType: "blob",
        });
        return response.data;
    },

    downloadIndentPdf: async (indentId: number): Promise<Blob> => {
        const response = await apiClient.get(
            `/documents/indent/${indentId}/pdf`,
            {
                responseType: "blob",
            }
        );
        return response.data;
    },

    previewIndentPdf: async (indentId: number): Promise<Blob> => {
        const response = await apiClient.get(
            `/documents/indent/${indentId}/preview`,
            {
                responseType: "blob",
            }
        );
        return response.data;
    },
};

export default reportsApi;
