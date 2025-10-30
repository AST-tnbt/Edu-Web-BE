import { env } from '../config/env';
import type { LoginCredentials, LoginResponse } from '../types/auth';

const headers = {
  'Content-Type': 'application/json',
  Accept: 'application/json',
};

const handleResponse = async <T>(response: Response): Promise<T> => {
  if (response.ok) {
    return response.json() as Promise<T>;
  }

  let message = 'Authentication request failed';

  try {
    const data = await response.json();
    if (typeof data === 'string') {
      message = data;
    } else if (data?.message) {
      message = data.message;
    } else if (data?.error) {
      message = data.error;
    }
  } catch (error) {
    console.warn('Unable to parse error response', error);
  }

  throw new Error(message);
};

export const login = async (credentials: LoginCredentials): Promise<LoginResponse> => {
  try {
    const response = await fetch(`${env.apiBaseUrl}/api/auth/login`, {
      method: 'POST',
      headers,
      body: JSON.stringify({
        email: credentials.email.trim(),
        password: credentials.password,
      }),
    });

    return handleResponse<LoginResponse>(response);
  } catch (error) {
    if (error instanceof TypeError) {
      throw new Error('Không thể kết nối tới máy chủ xác thực. Vui lòng thử lại sau.');
    }
    throw error;
  }
};

export const refresh = async (refreshToken: string): Promise<LoginResponse> => {
  try {
    const response = await fetch(`${env.apiBaseUrl}/api/auth/refresh`, {
      method: 'POST',
      headers,
      body: JSON.stringify({ refreshToken }),
    });

    return handleResponse<LoginResponse>(response);
  } catch (error) {
    if (error instanceof TypeError) {
      throw new Error('Không thể làm mới phiên đăng nhập.');
    }
    throw error;
  }
};

export const logout = async (accessToken: string, refreshToken: string | null): Promise<void> => {
  try {
    const response = await fetch(`${env.apiBaseUrl}/api/auth/logout`, {
      method: 'POST',
      headers: {
        ...headers,
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify({ refreshToken: refreshToken ?? undefined }),
    });

    if (!response.ok) {
      console.warn('Logout request failed with status', response.status);
    }
  } catch (error) {
    console.warn('Unable to reach logout endpoint', error);
  }
};
