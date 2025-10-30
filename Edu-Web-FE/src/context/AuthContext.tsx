import { createContext, useCallback, useMemo, useState } from 'react';
import type { ReactNode } from 'react';

import * as authService from '../services/authService';
import { tokenStorage } from '../utils/tokenStorage';
import type { AuthUser, LoginCredentials, LoginResponse } from '../types/auth';

export type AuthStatus = 'unauthenticated' | 'authenticating' | 'authenticated';

interface AuthContextValue {
  user: AuthUser | null;
  accessToken: string | null;
  refreshToken: string | null;
  status: AuthStatus;
  isAuthenticated: boolean;
  isAuthenticating: boolean;
  login: (credentials: LoginCredentials) => Promise<LoginResponse>;
  logout: () => Promise<void>;
  refresh: () => Promise<LoginResponse>;
}

interface AuthState {
  user: AuthUser | null;
  accessToken: string | null;
  refreshToken: string | null;
  status: AuthStatus;
}

const persistedState = tokenStorage.load();

const initialState: AuthState = {
  user: persistedState?.user ?? null,
  accessToken: persistedState?.accessToken ?? null,
  refreshToken: persistedState?.refreshToken ?? null,
  status: persistedState ? 'authenticated' : 'unauthenticated',
};

const normalizeRole = (rawRole: string | undefined): string => {
  if (!rawRole) {
    return 'UNKNOWN';
  }

  // Backend returns roles wrapped in brackets, e.g. [ROLE_STUDENT].
  const withoutBrackets = rawRole.replace(/^[\[]|[\]]$/g, '');
  const firstRole = withoutBrackets.split(',')[0]?.trim() ?? 'UNKNOWN';
  return firstRole;
};

const mapResponseToUser = (response: LoginResponse): AuthUser => ({
  id: response.userId,
  email: response.email,
  role: normalizeRole(response.role),
});

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [state, setState] = useState<AuthState>(initialState);

  const login = useCallback(async (credentials: LoginCredentials) => {
    setState((prev) => ({ ...prev, status: 'authenticating' }));

    try {
      const response = await authService.login(credentials);
      const user = mapResponseToUser(response);

      const nextState: AuthState = {
        user,
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
        status: 'authenticated',
      };

      tokenStorage.save({
        user,
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
      });

      setState(nextState);
      return response;
    } catch (error) {
      setState((prev) => ({
        ...prev,
        status: prev.user && prev.accessToken ? 'authenticated' : 'unauthenticated',
      }));
      throw error;
    }
  }, []);

  const logout = useCallback(async () => {
    const accessToken = state.accessToken;
    const refreshToken = state.refreshToken;

    if (accessToken) {
      try {
        await authService.logout(accessToken, refreshToken);
      } catch (error) {
        console.warn('Logout request failed', error);
      }
    }

    tokenStorage.clear();
    setState({ user: null, accessToken: null, refreshToken: null, status: 'unauthenticated' });
  }, [state.accessToken, state.refreshToken]);

  const refresh = useCallback(async () => {
    if (!state.refreshToken) {
      throw new Error('Missing refresh token');
    }

    const response = await authService.refresh(state.refreshToken);
    const user = mapResponseToUser(response);

    const nextState: AuthState = {
      user,
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      status: 'authenticated',
    };

    tokenStorage.save({
      user,
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
    });

    setState(nextState);
    return response;
  }, [state.refreshToken]);

  const contextValue = useMemo<AuthContextValue>(() => ({
    user: state.user,
    accessToken: state.accessToken,
    refreshToken: state.refreshToken,
    status: state.status,
    isAuthenticated: state.status === 'authenticated',
    isAuthenticating: state.status === 'authenticating',
    login,
    logout,
    refresh,
  }), [login, logout, refresh, state.accessToken, state.refreshToken, state.status, state.user]);

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
};
