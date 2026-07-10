import {createContext, type ReactNode, useCallback, useContext, useEffect, useState} from 'react';
import {authApi} from '@/services/api';
import {
  clearAuthSession,
  getAccessToken,
  getRefreshToken,
  setAuthSession,
  setStoredUser
} from '@/utils/authStorage';
import type {AuthResponse, LoginCredentials, RegisterData, User} from '@/types';

interface LoginOptions {
  remember?: boolean;
}

export interface AuthContextType {
  user: User | null;
  loading: boolean;
  error: string | null;
  isAuthenticated: boolean;
  login: (credentials: LoginCredentials, options?: LoginOptions) => Promise<AuthResponse>;
  register: (data: RegisterData) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({children}: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = getAccessToken();

    if (!token) {
      setLoading(false);
      return;
    }

    authApi
        .getCurrentUser()
        .then((freshUserData) => {
          setUser(freshUserData);
          setStoredUser(freshUserData);
        })
        .catch(() => {
          clearAuthSession();
          setUser(null);
        })
        .finally(() => {
          setLoading(false);
        });
  }, []);

  const login = useCallback(async (
      credentials: LoginCredentials,
      options: LoginOptions = {},
  ): Promise<AuthResponse> => {
    const remember = options.remember ?? true;
    setError(null);

    try {
      const response = await authApi.login(credentials);

      setAuthSession(
          {
            accessToken: response.token,
            refreshToken: response.refreshToken,
            user: response.user,
          },
          remember,
      );

      setUser(response.user);
      return response;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Login failed';
      setError(message);
      throw err;
    }
  }, []);

  const register = useCallback(async (userData: RegisterData): Promise<AuthResponse> => {
    setError(null);

    try {
      const response = await authApi.register(userData);

      setAuthSession(
          {
            accessToken: response.token,
            refreshToken: response.refreshToken,
            user: response.user,
          },
          true,
      );

      setUser(response.user);
      return response;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Registration failed';
      setError(message);
      throw err;
    }
  }, []);

  const logout = useCallback(async (): Promise<void> => {
    try {
      const refreshToken = getRefreshToken();
      await authApi.logout(refreshToken ?? undefined);
    } catch (err) {
      console.error('Logout API call failed, but logging out locally anyway.', err);
    } finally {
      clearAuthSession();
      setUser(null);
    }
  }, []);

  const value: AuthContextType = {
    user,
    loading,
    error,
    isAuthenticated: !!user && !loading,
    login,
    register,
    logout,
    clearError: () => setError(null),
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  return context;
}
