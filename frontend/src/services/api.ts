import type {
  AuthResponse,
  Background,
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
import {
  clearAuthSession,
  getAccessToken,
  getRefreshToken,
  setAuthSession
} from '@/utils/authStorage';

const API_BASE_URL = '/api/v1';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

interface BackendAuthData {
  accessToken: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
  user?: User;
}

let isRefreshing = false;
let pendingRequests: Array<(token: string) => void> = [];

function onRefreshSuccess(newToken: string) {
  pendingRequests.forEach((cb) => cb(newToken));
  pendingRequests = [];
}

async function attemptTokenRefresh(): Promise<string | null> {
  const refreshToken = getRefreshToken();

  if (!refreshToken) {
    return null;
  }

  const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({refreshToken}),
  });

  if (!response.ok) {
    return null;
  }

  const json: ApiResponse<BackendAuthData> = await response.json();

  if (!json.success || !json.data.accessToken) {
    return null;
  }

  setAuthSession(
      {
        accessToken: json.data.accessToken,
        refreshToken: json.data.refreshToken,
      },
  );

  return json.data.accessToken;
}

async function fetchWithAuth<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getAccessToken();

  const buildConfig = (t: string | null): RequestInit => ({
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(t && {Authorization: `Bearer ${t}`}),
      ...options.headers,
    },
  });

  const response = await fetch(`${API_BASE_URL}${endpoint}`, buildConfig(token));

  if (response.status === 401) {
    if (isRefreshing) {
      const newToken = await new Promise<string>((resolve, reject) => {
        pendingRequests.push((t) => (t ? resolve(t) : reject()));
      });
      const retryResponse = await fetch(`${API_BASE_URL}${endpoint}`, buildConfig(newToken));
      return parseResponse<T>(retryResponse);
    }

    isRefreshing = true;

    try {
      const newToken = await attemptTokenRefresh();

      if (!newToken) {
        clearAuthSession();
        window.location.href = '/auth';
        throw new Error('Session expired. Please log in again.');
      }

      onRefreshSuccess(newToken);
      const retryResponse = await fetch(`${API_BASE_URL}${endpoint}`, buildConfig(newToken));
      return parseResponse<T>(retryResponse);
    } finally {
      isRefreshing = false;
    }
  }

  return parseResponse<T>(response);
}

async function parseResponse<T>(response: Response): Promise<T> {
  if (response.status === 401) {
    clearAuthSession();
    window.location.href = '/auth';
    throw new Error('Session expired. Please log in again.');
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `Request failed with status ${response.status}`);
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

    return {
      token: jsonResponse.data.accessToken,
      refreshToken: jsonResponse.data.refreshToken,
      user: jsonResponse.data.user!,
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

    return {
      token: jsonResponse.data.accessToken,
      refreshToken: jsonResponse.data.refreshToken,
      user: jsonResponse.data.user!,
    };
  },

  logout: async (refreshToken?: string): Promise<void> => {
    const accessToken = getAccessToken();

    if (!accessToken) {
      return;
    }

    await fetch(`${API_BASE_URL}/auth/logout`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify(refreshToken ? {refreshToken} : {}),
    }).catch(() => {
    });
  },

  getCurrentUser: async (): Promise<User> => {
    return fetchWithAuth<User>('/users/me');
  },
};

export const referenceDataApi = {
  getRaces: async (): Promise<Race[]> => fetchWithAuth<Race[]>('/sync/races/list'),
  getClasses: async (): Promise<CharacterClass[]> => fetchWithAuth<CharacterClass[]>('/sync/classes/list'),
  getBackgrounds: async (): Promise<Background[]> => fetchWithAuth<Background[]>('/backgrounds/list'),
};

export const charactersApi = {
  getSummaries: async (params: PageParams = {}): Promise<Page<CharacterSummary>> =>
      fetchWithAuth<Page<CharacterSummary>>(`/characters${buildPageQuery(params)}`),
  getAllFull: async (params: PageParams = {}): Promise<Page<Character>> =>
      fetchWithAuth<Page<Character>>(`/characters/mine${buildPageQuery(params)}`),
  getById: async (id: number): Promise<Character> =>
      fetchWithAuth<Character>(`/characters/${id}`),
  create: async (data: CharacterCreateRequest): Promise<Character> =>
      fetchWithAuth<Character>('/characters', {method: 'POST', body: JSON.stringify(data)}),
  update: async (id: number, data: CharacterUpdateRequest): Promise<Character> =>
      fetchWithAuth<Character>(`/characters/${id}`, {method: 'PUT', body: JSON.stringify(data)}),
  delete: async (id: number): Promise<void> =>
      fetchWithAuth<void>(`/characters/${id}`, {method: 'DELETE'}),
  addEquipment: async (characterId: number, data: EquipmentData): Promise<Character> =>
      fetchWithAuth<Character>(`/characters/${characterId}/equipment`, {
        method: 'POST',
        body: JSON.stringify(data),
      }),
  removeEquipment: async (characterId: number, equipmentId: number): Promise<Character> =>
      fetchWithAuth<Character>(`/characters/${characterId}/equipment/${equipmentId}`, {
        method: 'DELETE',
      }),
  addSpell: async (characterId: number, spellId: number): Promise<Character> =>
      fetchWithAuth<Character>(`/characters/${characterId}/spells/${spellId}`, {method: 'POST'}),
  removeSpell: async (characterId: number, spellId: number): Promise<Character> =>
      fetchWithAuth<Character>(`/characters/${characterId}/spells/${spellId}`, {method: 'DELETE'}),
};

export const equipmentApi = {
  update: async (equipmentId: number, data: EquipmentData): Promise<EquipmentResponse> =>
      fetchWithAuth<EquipmentResponse>(`/equipment/${equipmentId}`, {
        method: 'PUT',
        body: JSON.stringify(data),
      }),
};

export const spellsApi = {
  search: async (name: string): Promise<SpellResponse[]> => {
    if (!name.trim()) return [];
    return fetchWithAuth<SpellResponse[]>(`/spells/search?${new URLSearchParams({name})}`);
  },
};
