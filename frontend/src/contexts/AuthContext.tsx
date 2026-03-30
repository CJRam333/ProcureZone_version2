import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { authApi, UserInfo, LoginRequest } from '../api';

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
  canView: boolean;
  canAdd: boolean;
  canEdit: boolean;
  canDelete: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialize auth state from localStorage
  useEffect(() => {
    const initAuth = async () => {
      const token = localStorage.getItem('accessToken');
      const storedUser = localStorage.getItem('user');

      if (token && storedUser) {
        try {
          const parsedUser = JSON.parse(storedUser);
          setUser(parsedUser);
          // Optionally refresh user data from API
          // const freshUser = await authApi.getCurrentUser();
          // setUser(freshUser);
        } catch (error) {
          console.error('Failed to parse stored user:', error);
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = useCallback(async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials);

    localStorage.setItem('accessToken', response.accessToken);
    localStorage.setItem('user', JSON.stringify(response.user));

    setUser(response.user);
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      setUser(null);
    }
  }, []);

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
