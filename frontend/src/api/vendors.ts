import apiClient from "./client";
import { PageRequest, PageResponse } from "./materials";

// ============== Types ==============
// Response matching backend VendorResponse (with optional enriched fields for UI)
export interface Vendor {
    id: number;
    vendorCode: string;
    vendorName: string;
    vendorType?: string;
    contactPerson?: string;
    contactPhone?: string;
    contactEmail?: string;
    addressLine1?: string;
    addressLine2?: string;
    city?: string;
    state?: string;
    country?: string;
    pincode?: string;
    gstNumber?: string;
    panNumber?: string;
    paymentTerms?: string;
    creditPeriodDays?: number;
    rating?: number;
    totalOrders?: number;
    totalOrderValue?: number;
    onTimeDeliveryRate?: number;
    qualityRating?: number;
    status: number;
    statusName?: string;
    remarks?: string;
    registrationDate?: string;
    lastOrderDate?: string;
    createdDate?: string;
    lastModifiedDate?: string;
    // Optional enriched fields for UI display/forms
    email?: string; // alias for contactEmail
    phone?: string; // alias for contactPhone
    mobile?: string;
    address?: string; // combined address fields
    bankName?: string;
    bankBranch?: string;
    accountNumber?: string;
    ifscCode?: string;
    creditDays?: number; // alias for creditPeriodDays
    creditLimit?: number;
    isActive?: boolean; // derived from status === 1
    isBlacklisted?: boolean;
    categories?: VendorCategory[];
    createdAt?: string; // alias for createdDate
    updatedAt?: string; // alias for lastModifiedDate
}

export interface VendorCategory {
    id: number;
    categoryId: number;
    categoryName: string;
}

export interface VendorCreateRequest {
    vendorCode: string;
    vendorName: string;
    contactPerson?: string;
    email?: string;
    phone?: string;
    mobile?: string;
    address?: string;
    city?: string;
    state?: string;
    pincode?: string;
    country?: string;
    gstNumber?: string;
    panNumber?: string;
    bankName?: string;
    bankBranch?: string;
    accountNumber?: string;
    ifscCode?: string;
    paymentTerms?: string;
    creditDays?: number;
    creditLimit?: number;
    categoryIds?: number[];
}

export interface VendorSearchParams extends PageRequest {
    search?: string;
    categoryId?: number;
    isActive?: boolean;
    city?: string;
    state?: string;
}

// ============== API Functions ==============

export const vendorsApi = {
    list: async (
        params: VendorSearchParams = {}
    ): Promise<PageResponse<Vendor>> => {
        const response = await apiClient.get<PageResponse<Vendor>>("/vendors", {
            params,
        });
        return response.data;
    },

    getById: async (id: number): Promise<Vendor> => {
        const response = await apiClient.get<Vendor>(`/vendors/${id}`);
        return response.data;
    },

    getByCode: async (code: string): Promise<Vendor> => {
        const response = await apiClient.get<Vendor>(`/vendors/code/${code}`);
        return response.data;
    },

    create: async (data: VendorCreateRequest): Promise<Vendor> => {
        const response = await apiClient.post<Vendor>("/vendors", data);
        return response.data;
    },

    update: async (
        id: number,
        data: Partial<VendorCreateRequest>
    ): Promise<Vendor> => {
        const response = await apiClient.put<Vendor>(`/vendors/${id}`, data);
        return response.data;
    },

    delete: async (id: number): Promise<void> => {
        await apiClient.delete(`/vendors/${id}`);
    },

    // --- Backend-confirmed query endpoints ---
    getActive: async (): Promise<Vendor[]> => {
        const response = await apiClient.get<Vendor[]>("/vendors/active");
        return response.data;
    },

    search: async (params: VendorSearchParams = {}): Promise<PageResponse<Vendor>> => {
        const response = await apiClient.get<PageResponse<Vendor>>(
            "/vendors/search",
            { params }
        );
        return response.data;
    },

    updateRating: async (id: number, rating: number): Promise<Vendor> => {
        const response = await apiClient.put<Vendor>(`/vendors/${id}/rating`, {
            rating,
        });
        return response.data;
    },

    getPerformance: async (id: number): Promise<any> => {
        const response = await apiClient.get(`/vendors/${id}/performance`);
        return response.data;
    },

    // --- Commented out: endpoints not confirmed on backend ---
    // activate: async (id: number): Promise<Vendor> => {
    //     const response = await apiClient.post<Vendor>(`/vendors/${id}/activate`);
    //     return response.data;
    // },
    // deactivate: async (id: number): Promise<Vendor> => {
    //     const response = await apiClient.post<Vendor>(`/vendors/${id}/deactivate`);
    //     return response.data;
    // },
    // blacklist: async (id: number, reason: string): Promise<Vendor> => {
    //     const response = await apiClient.post<Vendor>(`/vendors/${id}/blacklist`, { reason });
    //     return response.data;
    // },
    // removeBlacklist: async (id: number): Promise<Vendor> => {
    //     const response = await apiClient.post<Vendor>(`/vendors/${id}/remove-blacklist`);
    //     return response.data;
    // },
    // getByCategory: async (categoryId: number): Promise<Vendor[]> => {
    //     const response = await apiClient.get<Vendor[]>(`/vendors/category/${categoryId}`);
    //     return response.data;
    // },
    // bulkImport: async (file: File): Promise<{ success: number; failed: number; errors: string[] }> => {
    //     const formData = new FormData();
    //     formData.append("file", file);
    //     const response = await apiClient.post("/vendors/bulk-import", formData, {
    //         headers: { "Content-Type": "multipart/form-data" },
    //     });
    //     return response.data;
    // },
    // downloadTemplate: async (): Promise<Blob> => {
    //     const response = await apiClient.get("/vendors/template", { responseType: "blob" });
    //     return response.data;
    // },
};

export default vendorsApi;
