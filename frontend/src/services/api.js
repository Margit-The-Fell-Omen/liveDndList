const API_BASE_URL = 'http://localhost:8080/api/v1';

/**
 * Get stored auth token
 */
function getToken() {
  return localStorage.getItem('token');
}

/**
 * Base fetch wrapper with auth and error handling
 */
async function fetchWithAuth(endpoint, options = {}) {
  const token = getToken();
  
  const config = {
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
    return null;
  }

  return response.json();
}

/**
 * Auth API endpoints
 */
export const authApi = {
  login: async (credentials) => {
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

  register: async (userData) => {
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

  logout: async () => {
    try {
      await fetchWithAuth('/auth/logout', { method: 'POST' });
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  },

  refreshToken: async () => {
    return fetchWithAuth('/auth/refresh', { method: 'POST' });
  },

  getCurrentUser: async () => {
    return fetchWithAuth('/auth/me');
  },
};

/**
 * Characters API endpoints
 */
export const charactersApi = {
  getAll: async () => {
    return fetchWithAuth('/characters');
  },

  getById: async (id) => {
    return fetchWithAuth(`/characters/${id}`);
  },

  getLastEdited: async () => {
    return fetchWithAuth('/characters/last-edited');
  },

  create: async (characterData) => {
    return fetchWithAuth('/characters', {
      method: 'POST',
      body: JSON.stringify(characterData),
    });
  },

  update: async (id, characterData) => {
    return fetchWithAuth(`/characters/${id}`, {
      method: 'PUT',
      body: JSON.stringify(characterData),
    });
  },

  patch: async (id, partialData) => {
    return fetchWithAuth(`/characters/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(partialData),
    });
  },

  delete: async (id) => {
    return fetchWithAuth(`/characters/${id}`, {
      method: 'DELETE',
    });
  },

  duplicate: async (id) => {
    return fetchWithAuth(`/characters/${id}/duplicate`, {
      method: 'POST',
    });
  },

  export: async (id, format = 'json') => {
    return fetchWithAuth(`/characters/${id}/export?format=${format}`);
  },
};

/**
 * User preferences API
 */
export const preferencesApi = {
  get: async () => {
    return fetchWithAuth('/preferences');
  },

  update: async (preferences) => {
    return fetchWithAuth('/preferences', {
      method: 'PUT',
      body: JSON.stringify(preferences),
    });
  },
};

export default {
  auth: authApi,
  characters: charactersApi,
  preferences: preferencesApi,
};
