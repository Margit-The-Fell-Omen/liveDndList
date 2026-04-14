// src/context/AuthContext.tsx

import {createContext, type ReactNode, useCallback, useContext, useEffect, useState} from 'react';

// FIX 1: Import `authApi` as a named export from your services file.
// I'm assuming the file is at 'src/services/api.ts'. Adjust if needed.
import {authApi} from '@/services/api';

// These types should all be correctly defined and exported from your main types file.
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
  const [loading, setLoading] = useState<boolean>(true); // Start as true to check session
  const [error, setError] = useState<string | null>(null);

  // This effect runs once on app startup to check for an existing session.
  useEffect(() => {
    const token = localStorage.getItem('token');

    if (token) {
      // We have a token, so let's verify it with the backend.
      authApi.getCurrentUser()
          .then((freshUserData) => {
            // Token is valid, update user state and local storage
            setUser(freshUserData);
            localStorage.setItem('user', JSON.stringify(freshUserData));
          })
          .catch(() => {
            // Token is invalid or expired, clear everything.
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            setUser(null);
          })
          .finally(() => {
            // We are done checking, so stop the loading state.
            setLoading(false);
          });
    } else {
      // No token found, we are not logged in.
      setLoading(false);
    }
  }, []); // The empty dependency array ensures this runs only once.


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
      throw err; // Re-throw the error so the calling component can handle it
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
    // We call the API but clear local state regardless of whether the API call succeeds or fails.
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

  // Memoize the context value to prevent unnecessary re-renders of consumers
  const value: AuthContextType = {
    user,
    loading,
    error,
    isAuthenticated: !!user && !loading, // Only authenticated if not loading and user exists
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
