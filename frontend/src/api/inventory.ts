import apiClient from "./client";
import { PageRequest, PageResponse, MaterialDropdownItem } from "./materials";

// ============== Types ==============

// One row of the read-only stock view — material + company/plant + available stock from
// tbl_map_company_plant_material.map_quantity_stores (same source as the material dropdown).
export type InventoryStockRow = MaterialDropdownItem;
export interface Inventory {
    id: number;
    plantId: number;
    plantName: string;
    materialId: number;
    materialCode: string;
    materialDescription: string;
    uomId: number;
    uomCode: string;
    categoryId?: number;
    categoryName?: string;
    companyId?: number;
    companyName?: string;
    quantity: number;
    reservedQuantity?: number;
    availableQuantity?: number;
    minStockLevel?: number;
    maxStockLevel?: number;
    reorderLevel?: number;
    lastReceivedDate?: string;
    lastIssuedDate?: string;
    averageConsumption?: number;
    updatedAt?: string;
    // From backend InventoryResponse
    balanceQuantity?: number;
    issuedQuantity?: number;
    openingQuantity?: number;
}

export interface InventoryTransaction {
    id: number;
    plantId: number;
    materialId: number;
    transactionType:
    | "RECEIPT"
    | "ISSUE"
    | "RETURN"
    | "ADJUSTMENT"
    | "TRANSFER"
    | "GRN"
    | "ISSUE_NOTE";
    quantity: number;
    previousQuantity?: number;
    newQuantity?: number;
    referenceType?: string;
    referenceId?: number;
    referenceNumber?: string;
    remarks?: string;
    createdBy?: number;
    createdByName?: string;
    createdAt: string;
}

export interface InventoryStatistics {
    totalItems: number;
    totalValue: number;
    lowStockItems: number;
    criticalStockItems: number;
    outOfStockItems: number;
}

export interface InventorySearchParams extends PageRequest {
    search?: string;
    plantId?: number;
    companyId?: number;
    materialId?: number;
    categoryId?: number;
    lowStock?: boolean;
    belowReorderLevel?: boolean;
    belowMinStock?: boolean;
}

export interface StockAdjustmentRequest {
    companyId: number;
    plantId: number;
    materialId: number;
    adjustmentType: "ADD" | "SUBTRACT" | "SET";
    quantity: number;
    reason: string;
}

export interface StockTransferRequest {
    fromPlantId: number;
    toPlantId: number;
    materialId: number;
    quantity: number;
    remarks?: string;
}

// Backend response wrapper type
interface BackendResponse<T> {
    success: boolean;
    count?: number;
    data: T;
    alert?: string;
}

// ============== API Functions ==============

export const inventoryApi = {
    // Read-only stock view — GET /inventory/stock-view. Returns a Spring Page of material+stock
    // rows from the REAL stock source (map_quantity_stores), for the operational Inventory module.
    stockView: async (
        params: { search?: string; page?: number; size?: number } = {}
    ): Promise<PageResponse<InventoryStockRow>> => {
        const response = await apiClient.get<PageResponse<InventoryStockRow>>(
            "/inventory/stock-view",
            { params }
        );
        return response.data;
    },

    // Main list endpoint - adapts backend response to PageResponse format
    list: async (
        params: InventorySearchParams = {}
    ): Promise<PageResponse<Inventory>> => {
        const response = await apiClient.get<BackendResponse<Inventory[]>>(
            "/inventory",
            { params }
        );
        // Adapt backend response to PageResponse format
        const data = response.data.data || [];
        const totalElements = response.data.count || data.length;
        return {
            content: data,
            totalElements,
            totalPages: Math.ceil(totalElements / (params.size || 10)) || 1,
            size: params.size || data.length,
            number: params.page || 0,
            first: (params.page || 0) === 0,
            last:
                (params.page || 0) >=
                Math.ceil(totalElements / (params.size || 10)) - 1,
            empty: data.length === 0,
        };
    },

    // Alias for list - used by InventoryListPage
    getStock: async (
        params: {
            page?: number;
            size?: number;
            search?: string;
            stockStatus?: string;
            locationId?: number;
            plantId?: number;
            companyId?: number;
            materialId?: number;
            lowStock?: boolean;
        } = {}
    ): Promise<PageResponse<Inventory>> => {
        const response = await apiClient.get<BackendResponse<Inventory[]>>(
            "/inventory",
            { params }
        );
        const data = response.data.data || [];
        const totalElements = response.data.count || data.length;
        return {
            content: data,
            totalElements,
            totalPages: Math.ceil(totalElements / (params.size || 10)) || 1,
            size: params.size || data.length,
            number: params.page || 0,
            first: (params.page || 0) === 0,
            last:
                (params.page || 0) >=
                Math.ceil(totalElements / (params.size || 10)) - 1,
            empty: data.length === 0,
        };
    },

    // Get inventory statistics
    getStatistics: async (companyId?: number): Promise<InventoryStatistics> => {
        const response = await apiClient.get<
            BackendResponse<InventoryStatistics>
        >("/inventory/statistics", { params: companyId ? { companyId } : {} });
        return response.data.data;
    },

    // Get inventory summary (alias for statistics)
    getSummary: async (): Promise<InventoryStatistics> => {
        try {
            const response = await apiClient.get<
                BackendResponse<InventoryStatistics>
            >("/inventory/statistics");
            return response.data.data;
        } catch {
            // Return default values if endpoint fails
            return {
                totalItems: 0,
                lowStockItems: 0,
                criticalStockItems: 0,
                outOfStockItems: 0,
                totalValue: 0,
            };
        }
    },

    // Get low stock items
    getLowStock: async (): Promise<Inventory[]> => {
        const response = await apiClient.get<BackendResponse<Inventory[]>>(
            "/inventory/low-stock"
        );
        return response.data.data || [];
    },

    // Get critical stock items
    getCriticalStock: async (): Promise<Inventory[]> => {
        const response = await apiClient.get<BackendResponse<Inventory[]>>(
            "/inventory/critical-stock"
        );
        return response.data.data || [];
    },

    // Get stock for specific material at plant
    getStockByMaterialAndPlant: async (
        materialId: number,
        plantId: number
    ): Promise<{ availableStock: number }> => {
        const response = await apiClient.get<
            BackendResponse<{ availableStock: number }>
        >(`/inventory/stock/${materialId}/plant/${plantId}`);
        return response.data.data || { availableStock: 0 };
    },

    // Stock adjustment
    adjustStock: async (data: StockAdjustmentRequest): Promise<Inventory> => {
        const response = await apiClient.post<BackendResponse<Inventory>>(
            "/inventory/adjustment",
            data
        );
        return response.data.data;
    },

    // Check availability for a material at a plant
    checkAvailability: async (
        materialId: number,
        plantId: number,
        requiredQuantity?: number
    ): Promise<{ availableStock: number }> => {
        const response = await apiClient.get<
            BackendResponse<{ availableStock: number }>
        >(`/inventory/check-stock/${materialId}/plant/${plantId}`, {
            params: requiredQuantity ? { requiredQuantity } : undefined,
        });
        return response.data.data || { availableStock: 0 };
    },

    // --- Commented out: endpoints not confirmed on backend ---
    // transferStock: async (data: StockTransferRequest): Promise<void> => {
    //     await apiClient.post("/inventory/transfer", data);
    // },

    // Get by plant and material - for StockAdjustmentPage and TransactionHistoryPage
    getByPlantAndMaterial: async (
        plantId: number,
        materialId: number
    ): Promise<Inventory | null> => {
        try {
            const response = await apiClient.get<BackendResponse<Inventory>>(
                `/inventory/stock/${materialId}/plant/${plantId}`
            );
            return response.data.data || null;
        } catch {
            return null;
        }
    },

    // Get transactions for a material/plant
    getTransactions: async (
        params: {
            materialId?: number;
            plantId?: number;
            fromDate?: string;
            toDate?: string;
            transactionType?: string;
            page?: number;
            size?: number;
        } = {}
    ): Promise<PageResponse<InventoryTransaction>> => {
        const response = await apiClient.get<
            BackendResponse<InventoryTransaction[]>
        >("/inventory/transactions", { params });
        const data = response.data.data || [];
        const totalElements = response.data.count || data.length;
        return {
            content: data,
            totalElements,
            totalPages: Math.ceil(totalElements / (params.size || 10)) || 1,
            size: params.size || data.length,
            number: params.page || 0,
            first: (params.page || 0) === 0,
            last:
                (params.page || 0) >=
                Math.ceil(totalElements / (params.size || 10)) - 1,
            empty: data.length === 0,
        };
    },

    // --- Commented out: endpoints not confirmed on backend ---
    // exportReport: async (params: InventorySearchParams = {}): Promise<Blob> => {
    //     const response = await apiClient.get("/inventory/export", {
    //         params,
    //         responseType: "blob",
    //     });
    //     return response.data;
    // },

    // runReconciliation: async (
    //     plantId?: number
    // ): Promise<{ success: boolean; message: string }> => {
    //     const response = await apiClient.post("/inventory/reconcile", {
    //         plantId,
    //     });
    //     return response.data;
    // },
};

export default inventoryApi;
