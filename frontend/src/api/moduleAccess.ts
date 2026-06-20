import apiClient from "./client";

export interface ModuleDefinition {
    moduleCode: string;
    moduleName: string;
    active: boolean;
    isFuture: boolean;
    defaultRoles: string | null;
}

export interface EmpModuleEntry {
    moduleCode: string;
    moduleName: string;
    enabled: boolean;
    isFuture: boolean;
    customOverride: boolean;
}

export interface ModuleUpdate {
    code: string;
    enabled: boolean;
}

export const moduleAccessApi = {
    getMyModules: async (): Promise<string[]> => {
        const response = await apiClient.get<string[]>("/module-access/my-modules");
        return response.data;
    },

    getAllModules: async (): Promise<ModuleDefinition[]> => {
        const response = await apiClient.get<ModuleDefinition[]>("/module-access/modules");
        return response.data;
    },

    getEmployeeModules: async (empNumber: number): Promise<EmpModuleEntry[]> => {
        const response = await apiClient.get<EmpModuleEntry[]>(`/module-access/employee/${empNumber}`);
        return response.data;
    },

    updateEmployeeModules: async (empNumber: number, moduleCodes: ModuleUpdate[]): Promise<void> => {
        await apiClient.put(`/module-access/employee/${empNumber}`, { moduleCodes });
    },
};

export default moduleAccessApi;
