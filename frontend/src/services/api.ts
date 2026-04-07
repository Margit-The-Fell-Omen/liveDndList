import type {
  Archetype,
  AuthResponse,
  Character,
  CharacterClass,
  CharacterCreateRequest,
  CharacterUpdateRequest,
  LoginCredentials,
  Race,
  RegisterData,
  User,
} from '@/types';

const API_BASE_URL = 'http://localhost:8080/api/v1';

function getToken(): string | null {
  return localStorage.getItem('token');
}

// Wrapped response structure from your backend
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

async function fetchWithAuth<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();

  const config: RequestInit = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token && {Authorization: `Bearer ${token}`}),
      ...options.headers,
    },
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

  if (response.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/auth';
    throw new Error('Session expired. Please log in again.');
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  if (response.status === 204) {
    return null as T;
  }

  const jsonResponse = await response.json();

  // Unwrap if response has { success, data, message } structure
  if (jsonResponse.success !== undefined && jsonResponse.data !== undefined) {
    if (!jsonResponse.success) {
      throw new Error(jsonResponse.message || 'Request failed');
    }
    return jsonResponse.data as T;
  }

  return jsonResponse as T;
}

// ═══════════════════════════════════════════════════════════════
// AUTH API
// ═══════════════════════════════════════════════════════════════

interface BackendAuthData {
  accessToken: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
  user: {
    id: number;
    username: string;
    email: string;
    roles?: string[];
    enabled?: boolean;
    createdAt?: string;
    updatedAt?: string;
  };
}

export const authApi = {
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      const errorResponse = await response.json().catch(() => ({}));
      throw new Error(errorResponse.message || 'Login failed');
    }

    const jsonResponse: ApiResponse<BackendAuthData> = await response.json();

    if (!jsonResponse.success) {
      throw new Error(jsonResponse.message || 'Login failed');
    }

    const authData = jsonResponse.data;

    return {
      token: authData.accessToken,
      user: {
        id: authData.user.id,
        username: authData.user.username,
        email: authData.user.email,
        roles: authData.user.roles,
        enabled: authData.user.enabled,
        createdAt: authData.user.createdAt,
        updatedAt: authData.user.updatedAt,
      },
    };
  },

  register: async (userData: RegisterData): Promise<AuthResponse> => {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const errorResponse = await response.json().catch(() => ({}));
      throw new Error(errorResponse.message || 'Registration failed');
    }

    const jsonResponse: ApiResponse<BackendAuthData> = await response.json();

    if (!jsonResponse.success) {
      throw new Error(jsonResponse.message || 'Registration failed');
    }

    const authData = jsonResponse.data;

    return {
      token: authData.accessToken,
      user: {
        id: authData.user.id,
        username: authData.user.username,
        email: authData.user.email,
        roles: authData.user.roles,
        enabled: authData.user.enabled,
        createdAt: authData.user.createdAt,
        updatedAt: authData.user.updatedAt,
      },
    };
  },

  logout: async (): Promise<void> => {
    try {
      await fetchWithAuth<void>('/auth/logout', {method: 'POST'});
    } catch (error) {
      console.error('Logout API call failed:', error);
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  },

  getCurrentUser: async (): Promise<User> => {
    return fetchWithAuth<User>('/auth/me');
  },
};

// ═══════════════════════════════════════════════════════════════
// REFERENCE DATA API (Races, Classes, Archetypes)
// ═══════════════════════════════════════════════════════════════

export const referenceDataApi = {
  getRaces: async (): Promise<Race[]> => {
    return fetchWithAuth<Race[]>('/races');
  },

  getClasses: async (): Promise<CharacterClass[]> => {
    return fetchWithAuth<CharacterClass[]>('/classes');
  },

  getArchetypes: async (classId?: number): Promise<Archetype[]> => {
    const endpoint = classId ? `/archetypes?classId=${classId}` : '/archetypes';
    return fetchWithAuth<Archetype[]>(endpoint);
  },

  getArchetypesByClass: async (classId: number): Promise<Archetype[]> => {
    return fetchWithAuth<Archetype[]>(`/classes/${classId}/archetypes`);
  },
};

// ═══════════════════════════════════════════════════════════════
// CHARACTERS API
// ═══════════════════════════════════════════════════════════════

export const charactersApi = {
  getAll: async (): Promise<Character[]> => {
    return fetchWithAuth<Character[]>('/characters');
  },

  getById: async (id: number): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${id}`);
  },

  create: async (data: CharacterCreateRequest): Promise<Character> => {
    console.log('Creating character with data:', data);
    return fetchWithAuth<Character>('/characters', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  update: async (id: number, data: CharacterUpdateRequest): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },

  patch: async (id: number, data: Partial<CharacterUpdateRequest>): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  },

  delete: async (id: number): Promise<void> => {
    return fetchWithAuth<void>(`/characters/${id}`, {
      method: 'DELETE',
    });
  },
};

const api = {
  auth: authApi,
  referenceData: referenceDataApi,
  characters: charactersApi,
};

export default api;
