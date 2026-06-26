import React, { createContext, useContext, useState, useEffect, useCallback, useRef, ReactNode } from 'react';
import { authApi, UserInfo, LoginRequest } from '../api';
import { moduleAccessApi } from '../api/moduleAccess';
import queryClient from '../queryClient';

interface AuthContextType {
  user: UserInfo | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  hasPermission: (permission: string) => boolean;
  hasModuleAccess: (moduleCode: string) => boolean;
  canView: boolean;
  canAdd: boolean;
  canEdit: boolean;
  canDelete: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

// localStorage keys wiped on any session end (logout, expiry, 401)
const SESSION_KEYS = ['accessToken', 'refreshToken', 'user', 'procurezone_settings'];

function getTokenExpiry(token: string): number | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(atob(base64));
    return typeof payload.exp === 'number' ? payload.exp : null;
  } catch {
    return null;
  }
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const expiryTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const clearAllSession = useCallback(() => {
    if (expiryTimerRef.current) {
      clearTimeout(expiryTimerRef.current);
      expiryTimerRef.current = null;
    }
    SESSION_KEYS.forEach((key) => localStorage.removeItem(key));
    sessionStorage.clear();
    queryClient.clear();
    setUser(null);
  }, []);

  const scheduleAutoLogout = useCallback(
    (token: string) => {
      const exp = getTokenExpiry(token);
      if (!exp) return;
      const msUntilExpiry = exp * 1000 - Date.now();
      if (msUntilExpiry <= 0) {
        clearAllSession();
        return;
      }
      if (expiryTimerRef.current) clearTimeout(expiryTimerRef.current);
      expiryTimerRef.current = setTimeout(() => {
        clearAllSession();
        window.location.href = '/login?reason=expired';
      }, msUntilExpiry);
    },
    [clearAllSession]
  );

  // Initialize auth state from localStorage; validate token has not already expired
  useEffect(() => {
    const initAuth = async () => {
      const token = localStorage.getItem('accessToken');
      const storedUser = localStorage.getItem('user');

      if (token && storedUser) {
        const exp = getTokenExpiry(token);
        if (exp !== null && exp * 1000 <= Date.now()) {
          // Token is already expired — wipe session silently
          clearAllSession();
          setIsLoading(false);
          return;
        }
        try {
          const parsedUser = JSON.parse(storedUser);
          setUser(parsedUser);
          scheduleAutoLogout(token);
        } catch {
          clearAllSession();
        }
      }
      setIsLoading(false);
    };

    initAuth();

    return () => {
      if (expiryTimerRef.current) clearTimeout(expiryTimerRef.current);
    };
  }, [clearAllSession, scheduleAutoLogout]);

  const login = useCallback(
    async (credentials: LoginRequest) => {
      // Clear any previous session before storing the new one
      clearAllSession();
      const response = await authApi.login(credentials);
      // Store token first so module-access request can authenticate
      localStorage.setItem('accessToken', response.accessToken);

      // Fetch module access codes and merge into user object
      let userWithModules = response.user;
      try {
        const allowedModules = await moduleAccessApi.getMyModules();
        userWithModules = { ...response.user, allowedModules };
      } catch {
        // Non-fatal: fall back to role-based defaults enforced in Sidebar
        userWithModules = { ...response.user, allowedModules: [] };
      }

      localStorage.setItem('user', JSON.stringify(userWithModules));
      setUser(userWithModules);
      scheduleAutoLogout(response.accessToken);
    },
    [clearAllSession, scheduleAutoLogout]
  );

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // ignore — clear session regardless
    } finally {
      clearAllSession();
    }
  }, [clearAllSession]);

  const refreshUser = useCallback(async () => {
    try {
      const freshUser = await authApi.getMe();
      setUser(freshUser);
      localStorage.setItem('user', JSON.stringify(freshUser));
    } catch (error) {
      console.error('Failed to refresh user:', error);
    }
  }, []);

  const hasRole = useCallback(
    (role: string): boolean => {
      if (!user) return false;
      return user.roles.includes(role) || user.roles.includes('SUPERADMIN');
    },
    [user]
  );

  const hasAnyRole = useCallback(
    (roles: string[]): boolean => {
      if (!user) return false;
      if (user.roles.includes('SUPERADMIN') || user.canView) return true;
      return roles.some((role) => user.roles.includes(role));
    },
    [user]
  );

  const hasPermission = useCallback(
    (permission: string): boolean => {
      if (!user) return false;
      if (user.roles.includes('SUPERADMIN')) return true;

      if (permission === 'view' && user.canView) return true;
      if (permission === 'add' && user.canAdd) return true;
      if (permission === 'edit' && user.canEdit) return true;
      if (permission === 'delete' && user.canDelete) return true;

      return user.permissions?.includes(permission) ?? false;
    },
    [user]
  );

  const hasModuleAccess = useCallback(
    (moduleCode: string): boolean => {
      if (!user) return false;
      // SUPERADMIN always has access to all modules
      if (user.roles.includes('SUPERADMIN')) return true;
      if (!user.allowedModules) return true; // no data yet — default open
      return user.allowedModules.includes(moduleCode);
    },
    [user]
  );

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    logout,
    refreshUser,
    hasRole,
    hasAnyRole,
    hasPermission,
    hasModuleAccess,
    canView: user?.canView ?? false,
    canAdd: user?.canAdd ?? false,
    canEdit: user?.canEdit ?? false,
    canDelete: user?.canDelete ?? false,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
