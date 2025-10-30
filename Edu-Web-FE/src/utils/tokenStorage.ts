import type { AuthPersistedState } from '../types/auth';

const STORAGE_KEY = 'edu-web-fe.auth';

const isBrowser = () => typeof window !== 'undefined' && !!window.localStorage;

const load = (): AuthPersistedState | null => {
  if (!isBrowser()) {
    return null;
  }

  try {
    const serialized = window.localStorage.getItem(STORAGE_KEY);
    if (!serialized) {
      return null;
    }

    return JSON.parse(serialized) as AuthPersistedState;
  } catch (error) {
    console.warn('Failed to read auth state from storage', error);
    window.localStorage.removeItem(STORAGE_KEY);
    return null;
  }
};

const save = (state: AuthPersistedState) => {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
};

const clear = () => {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.removeItem(STORAGE_KEY);
};

export const tokenStorage = {
  load,
  save,
  clear,
};
