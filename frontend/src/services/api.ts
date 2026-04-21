// services/api.ts

import type {
  Archetype,
  AuthResponse,
  Character,
  CharacterClass,
  CharacterCreateRequest,
  CharacterSummary,
  CharacterUpdateRequest,
  EquipmentData,
  EquipmentResponse,
  LoginCredentials,
  Page,
  Race,
  RegisterData,
  SpellResponse,
  User,
} from '@/types';

const API_BASE_URL = '/';

// =================================================================
// HELPER FUNCTIONS (No changes here)
// =================================================================

function getToken(): string | null {
  return localStorage.getItem('token');
}

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
  if (jsonResponse.success !== undefined && jsonResponse.data !== undefined) {
    if (!jsonResponse.success) {
      throw new Error(jsonResponse.message || 'Request failed');
    }
    return jsonResponse.data as T;
  }
  return jsonResponse as T;
}

interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
}

const buildPageQuery = (params: PageParams): string => {
  const query = new URLSearchParams();
  if (params.page !== undefined) query.append('page', params.page.toString());
  if (params.size !== undefined) query.append('size', params.size.toString());
  if (params.sort) query.append('sort', params.sort);
  const queryString = query.toString();
  return queryString ? `?${queryString}` : '';
};


// =================================================================
// API MODULES
// =================================================================

// ═══════════════════════════════════════════════════════════════
// AUTH API
// ═══════════════════════════════════════════════════════════════

interface BackendAuthData {
  accessToken: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
  user: User;
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
    return {token: jsonResponse.data.accessToken, user: jsonResponse.data.user};
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
    return {token: jsonResponse.data.accessToken, user: jsonResponse.data.user};
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
    return fetchWithAuth<User>('/users/me');
  },
};

// ═══════════════════════════════════════════════════════════════
// REFERENCE DATA API (Races, Classes, Archetypes)
// ═══════════════════════════════════════════════════════════════

export const referenceDataApi = {
  getRaces: async (): Promise<Race[]> => {
    return fetchWithAuth<Race[]>('/sync/races/list');
  },
  getClasses: async (): Promise<CharacterClass[]> => {
    return fetchWithAuth<CharacterClass[]>('/sync/classes/list');
  },
  getArchetypesByClass: async (classId: number): Promise<Archetype[]> => {
    const endpoint = `/dndclass/${classId}/archetypes`;
    return fetchWithAuth<Archetype[]>(endpoint);
  }
};

// ═══════════════════════════════════════════════════════════════
// CHARACTERS API
// ═══════════════════════════════════════════════════════════════

export const charactersApi = {
  getSummaries: async (params: PageParams = {}): Promise<Page<CharacterSummary>> => {
    const query = buildPageQuery(params);
    return fetchWithAuth<Page<CharacterSummary>>(`/characters${query}`);
  },
  getAllFull: async (params: PageParams = {}): Promise<Page<Character>> => {
    const query = buildPageQuery(params);
    return fetchWithAuth<Page<Character>>(`/characters/mine${query}`);
  },
  getById: async (id: number): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${id}`);
  },
  create: async (data: CharacterCreateRequest): Promise<Character> => {
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
  delete: async (id: number): Promise<void> => {
    return fetchWithAuth<void>(`/characters/${id}`, {
      method: 'DELETE',
    });
  },

  addEquipment: async (characterId: number, data: EquipmentData): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${characterId}/equipment`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  removeEquipment: async (characterId: number, equipmentId: number): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${characterId}/equipment/${equipmentId}`, {
      method: 'DELETE',
    });
  },

  addSpell: async (characterId: number, spellId: number): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${characterId}/spells/${spellId}`, {
      method: 'POST',
    });
  },

  removeSpell: async (characterId: number, spellId: number): Promise<Character> => {
    return fetchWithAuth<Character>(`/characters/${characterId}/spells/${spellId}`, {
      method: 'DELETE',
    });
  },
};

export const equipmentApi = {
  update: async (equipmentId: number, data: EquipmentData): Promise<EquipmentResponse> => {
    return fetchWithAuth<EquipmentResponse>(`/equipment/${equipmentId}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  },
};

export const spellsApi = {
  search: async (name: string): Promise<SpellResponse[]> => {
    if (!name.trim()) return [];
    const query = new URLSearchParams({name});
    return fetchWithAuth<SpellResponse[]>(`/spells/search?${query}`);
  },
};