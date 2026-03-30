import apiClient from "./client";

// ============== Types ==============
export interface Plant {
    id: number;
    plantCode: string;
    plantName: string;
    address?: string;
    city?: string;
    state?: string;
    pincode?: string;
    phone?: string;
    email?: string;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface Department {
    id: number;
    departmentCode: string;
    departmentName: string;
    description?: string;
    headEmployeeId?: number;
    headEmployeeName?: string;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface UnitOfMeasure {
    id: number;
    uomCode: string;
    uomName: string;
    description?: string;
    isActive: boolean;
}

export interface MaterialCategory {
    id: number;
    categoryCode: string;
    categoryName: string;
    description?: string;
    parentId?: number;
    parentName?: string;
    isActive: boolean;
}

export interface ApprovalLevel {
    id: number;
    levelNumber: number;
    levelName: string;
    roleRequired: string;
    minAmount?: number;
    maxAmount?: number;
    isActive: boolean;
}

// ============== API Functions ==============

export const masterDataApi = {
    // Plants
    getPlants: async (): Promise<Plant[]> => {
        const response = await apiClient.get<Plant[]>("/plants");
        return response.data;
    },

    getPlantById: async (id: number): Promise<Plant> => {
        const response = await apiClient.get<Plant>(`/plants/${id}`);
        return response.data;
    },

    createPlant: async (
        data: Omit<Plant, "id" | "createdAt" | "updatedAt">
    ): Promise<Plant> => {
        const response = await apiClient.post<Plant>("/plants", data);
        return response.data;
    },

    updatePlant: async (id: number, data: Partial<Plant>): Promise<Plant> => {
        const response = await apiClient.put<Plant>(`/plants/${id}`, data);
        return response.data;
    },

    // Departments
    getDepartments: async (): Promise<Department[]> => {
        const response = await apiClient.get<Department[]>("/departments");
        return response.data;
    },

    getDepartmentById: async (id: number): Promise<Department> => {
        const response = await apiClient.get<Department>(`/departments/${id}`);
        return response.data;
    },

    createDepartment: async (
        data: Omit<Department, "id" | "createdAt" | "updatedAt">
    ): Promise<Department> => {
        const response = await apiClient.post<Department>("/departments", data);
        return response.data;
    },

    updateDepartment: async (
        id: number,
        data: Partial<Department>
    ): Promise<Department> => {
        const response = await apiClient.put<Department>(
            `/departments/${id}`,
            data
        );
        return response.data;
    },

    // Units of Measure
    getUOMs: async (): Promise<UnitOfMeasure[]> => {
        const response = await apiClient.get<UnitOfMeasure[]>("/uom");
        return response.data;
    },

    getUOMById: async (id: number): Promise<UnitOfMeasure> => {
        const response = await apiClient.get<UnitOfMeasure>(`/uom/${id}`);
        return response.data;
    },

    createUOM: async (
        data: Omit<UnitOfMeasure, "id">
    ): Promise<UnitOfMeasure> => {
        const response = await apiClient.post<UnitOfMeasure>("/uom", data);
        return response.data;
    },

    updateUOM: async (
        id: number,
        data: Partial<UnitOfMeasure>
    ): Promise<UnitOfMeasure> => {
        const response = await apiClient.put<UnitOfMeasure>(`/uom/${id}`, data);
        return response.data;
    },

    // Material Categories
    getCategories: async (): Promise<MaterialCategory[]> => {
        const response = await apiClient.get<MaterialCategory[]>("/categories");
        return response.data;
    },

    getCategoryById: async (id: number): Promise<MaterialCategory> => {
        const response = await apiClient.get<MaterialCategory>(
            `/categories/${id}`
        );
        return response.data;
    },

    createCategory: async (
        data: Omit<MaterialCategory, "id">
    ): Promise<MaterialCategory> => {
        const response = await apiClient.post<MaterialCategory>(
            "/categories",
            data
        );
        return response.data;
    },

    updateCategory: async (
        id: number,
        data: Partial<MaterialCategory>
    ): Promise<MaterialCategory> => {
        const response = await apiClient.put<MaterialCategory>(
            `/categories/${id}`,
            data
        );
        return response.data;
    },

    // Approval Levels
    getApprovalLevels: async (): Promise<ApprovalLevel[]> => {
        const response = await apiClient.get<ApprovalLevel[]>(
            "/approval-levels"
        );
        return response.data;
    },

    updateApprovalLevel: async (
        id: number,
        data: Partial<ApprovalLevel>
    ): Promise<ApprovalLevel> => {
        const response = await apiClient.put<ApprovalLevel>(
            `/approval-levels/${id}`,
            data
        );
        return response.data;
    },
};

export default masterDataApi;
