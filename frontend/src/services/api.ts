import type {
  LoginCredentials,
  RegisterData,
  AuthResponse,
  User,
  Character,
} from '@/types';

const API_BASE_URL = 'http://localhost:8080/api/v1';

/**
 * Get stored auth token
 */
function getToken(): string | null {
  return localStorage.getItem('token');
}

/**
 * Base fetch wrapper with auth and error handling
 */
async function fetchWithAuth<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();

  const config: RequestInit = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    },
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

  // Handle 401 Unauthorized
  if (response.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/auth';
    throw new Error('Session expired. Please log in again.');
  }

  // Handle non-OK responses
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `Request failed with status ${response.status}`);
  }

  // Handle empty responses
  if (response.status === 204) {
    return null as T;
  }

  return response.json();
}

/**
 * Auth API endpoints
 */
export const authApi = {
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || 'Login failed');
    }

    return response.json();
  },

  register: async (userData: RegisterData): Promise<AuthResponse> => {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || 'Registration failed');
    }

    return response.json();
  },

  logout: async (): Promise<void> => {
    try {
      await fetchWithAuth<void>('/auth/logout', { method: 'POST' });
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  },

  refreshToken: async (): Promise<AuthResponse> => {
    return fetchWithAuth<AuthResponse>('/auth/refresh', { method: 'POST' });
  },

  getCurrentUser: async (): Promise<User> => {
    return fetchWithAuth<User>('/auth/me');
  },
};

/**
 * Characters API endpoints
 */
export const charactersApi = {
  getAll: async (): Promise<Character[]> => {
    return fetchWithAuth<Character[]>('/characters');
  },

  getById: async (id: number): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${id}`);
  },

  getLastEdited: async (): Promise<Character> => {
    return fetchWithAuth<Character>('/characters/last-edited');
  },

  create: async (characterData: Partial<Character>): Promise<Character> => {
    return fetchWithAuth<Character>('/characters', {
      method: 'POST',
      body: JSON.stringify(characterData),
    });
  },

  update: async (id: number, characterData: Character): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${id}`, {
      method: 'PUT',
      body: JSON.stringify(characterData),
    });
  },

  patch: async (id: number, partialData: Partial<Character>): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(partialData),
    });
  },

  delete: async (id: number): Promise<void> => {
    return fetchWithAuth<void>(`/characters/${id}`, {
      method: 'DELETE',
    });
  },

  duplicate: async (id: number): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${id}/duplicate`, {
      method: 'POST',
    });
  },

  export: async (id: number, format: string = 'json'): Promise<unknown> => {
    return fetchWithAuth<unknown>(`/characters/${id}/export?format=${format}`);
  },
};

export interface UserPreferences {
  theme: 'light' | 'dark' | 'system';
  lastCharacterId?: number;
}

/**
 * User preferences API
 */
export const preferencesApi = {
  get: async (): Promise<UserPreferences> => {
    return fetchWithAuth<UserPreferences>('/preferences');
  },

  update: async (preferences: Partial<UserPreferences>): Promise<UserPreferences> => {
    return fetchWithAuth<UserPreferences>('/preferences', {
      method: 'PUT',
      body: JSON.stringify(preferences),
    });
  },
};

const api = {
  auth: authApi,
  characters: charactersApi,
  preferences: preferencesApi,
};

export default api;
