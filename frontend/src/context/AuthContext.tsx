// src/context/AuthContext.tsx

import {createContext, type ReactNode, useCallback, useContext, useEffect, useState} from 'react';

import {authApi} from '@/services/api';

import type {AuthResponse, LoginCredentials, RegisterData, User} from '@/types';


// ===============================================================
// CONTEXT TYPE DEFINITION
// We define the context type here to ensure it matches the hook's return value.
// ===============================================================

export interface AuthContextType {
  user: User | null;
  loading: boolean;
  error: string | null;
  isAuthenticated: boolean;
  login: (credentials: LoginCredentials) => Promise<AuthResponse>;
  register: (data: RegisterData) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  clearError: () => void;
}


// ===============================================================
// CONTEXT PROVIDER
// ===============================================================

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({children}: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('token');

    if (token) {
      authApi.getCurrentUser()
          .then((freshUserData) => {
            setUser(freshUserData);
            localStorage.setItem('user', JSON.stringify(freshUserData));
          })
          .catch(() => {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            setUser(null);
          })
          .finally(() => {
            setLoading(false);
          });
    } else {
      setLoading(false);
    }
  }, []);


  const login = useCallback(async (credentials: LoginCredentials): Promise<AuthResponse> => {
    setError(null);
    try {
      const response = await authApi.login(credentials);
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response.user));
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
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response.user));
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
      await authApi.logout();
    } catch (err) {
      console.error("Logout API call failed, but logging out locally anyway.", err);
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
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


// ===============================================================
// CUSTOM HOOK
// ===============================================================

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
