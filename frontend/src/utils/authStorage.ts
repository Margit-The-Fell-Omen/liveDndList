import type {User} from '@/types';

const ACCESS_TOKEN_KEY = 'token';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_KEY = 'user';

interface AuthSessionData {
  accessToken: string;
  refreshToken?: string;
  user?: User;
}

function getActiveStorage(): Storage | null {
  if (window.localStorage.getItem(ACCESS_TOKEN_KEY)) return window.localStorage;
  if (window.sessionStorage.getItem(ACCESS_TOKEN_KEY)) return window.sessionStorage;
  return null;
}

function clearStorage(storage: Storage) {
  storage.removeItem(ACCESS_TOKEN_KEY);
  storage.removeItem(REFRESH_TOKEN_KEY);
  storage.removeItem(USER_KEY);
}

export function setAuthSession(data: AuthSessionData, remember?: boolean) {
  const existingStorage = getActiveStorage();

  const storage = remember !== undefined
      ? (remember ? window.localStorage : window.sessionStorage)
      : (existingStorage ?? window.sessionStorage);

  if (remember !== undefined) {
    clearStorage(window.localStorage);
    clearStorage(window.sessionStorage);
  }

  storage.setItem(ACCESS_TOKEN_KEY, data.accessToken);

  if (data.refreshToken) {
    storage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
  }

  if (data.user) {
    storage.setItem(USER_KEY, JSON.stringify(data.user));
  }
}

export function setStoredUser(user: User) {
  const storage = getActiveStorage();
  if (!storage) return;
  storage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearAuthSession() {
  clearStorage(window.localStorage);
  clearStorage(window.sessionStorage);
}

export function getAccessToken(): string | null {
  return (
      window.localStorage.getItem(ACCESS_TOKEN_KEY) ??
      window.sessionStorage.getItem(ACCESS_TOKEN_KEY)
  );
}

export function getRefreshToken(): string | null {
  return (
      window.localStorage.getItem(REFRESH_TOKEN_KEY) ??
      window.sessionStorage.getItem(REFRESH_TOKEN_KEY)
  );
}
