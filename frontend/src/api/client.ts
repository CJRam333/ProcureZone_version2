import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from "axios";
import { toast } from "react-toastify";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";

// Create axios instance
const apiClient: AxiosInstance = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
    timeout: 30000,
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("accessToken");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as AxiosRequestConfig & {
            _retry?: boolean;
        };

        // Handle 401 Unauthorized or 403 Forbidden
        if (
            (error.response?.status === 401 ||
                error.response?.status === 403) &&
            !originalRequest._retry
        ) {
            originalRequest._retry = true;

            // No refresh endpoint exists in backend.
            // Clear tokens and redirect to login.
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            localStorage.removeItem("user");
            window.location.href = "/login";
            return Promise.reject(error);
        }

        // Generic error toast (skip for silent refresh attempts)
        if (error.response?.status && error.response.status >= 500) {
            toast.error("Server error. Please try again.");
        } else if (!navigator.onLine) {
            toast.error("You are offline. Check your connection.");
        } else {
            const message =
                (error.response?.data as ApiError)?.message || error.message;
            if (message) {
                toast.error(message);
            }
        }

        return Promise.reject(error);
    }
);

// API Error type
export interface ApiError {
    timestamp: string;
    code: string;
    message: string;
    details?: Record<string, string>;
}

// Extract error message from API response
export const getErrorMessage = (error: unknown): string => {
    if (axios.isAxiosError(error)) {
        const apiError = error.response?.data as ApiError;
        return (
            apiError?.message || error.message || "An unexpected error occurred"
        );
    }
    if (error instanceof Error) {
        return error.message;
    }
    return "An unexpected error occurred";
};

export default apiClient;
