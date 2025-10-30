import { env } from '../config/env';

export interface UserProfilePayload {
  email?: string;
  firstName?: string;
  lastName?: string;
  address?: string;
  country?: string;
  state?: string;
  city?: string;
  postalCode?: string;
  phone?: string;
  bio?: string;
  avatarUrl?: string | null;
}

export interface UserProfile {
  userId: string;
  fullName: string | null;
  avatarUrl: string | null;
  bio: string | null;
  phoneNumber: string | null;
  address: string | null;
  createdAt?: string;
  updatedAt?: string;
}

const defaultHeaders = (accessToken?: string) => ({
  'Content-Type': 'application/json',
  Accept: 'application/json',
  ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
});

const buildFullName = (payload: UserProfilePayload) => {
  const first = payload.firstName?.trim();
  const last = payload.lastName?.trim();
  const combined = [first, last].filter(Boolean).join(' ').trim();

  if (combined.length > 0) {
    return combined;
  }

  if (payload.email) {
    return payload.email.split('@')[0] ?? payload.email;
  }

  return 'User';
};

const buildAddress = (payload: UserProfilePayload) => {
  const parts = [payload.address, payload.city, payload.state, payload.country, payload.postalCode]
    .map((value) => value?.trim())
    .filter((value): value is string => Boolean(value));

  if (parts.length === 0) {
    return undefined;
  }

  return parts.join(', ');
};

const mapPayloadToRequest = (userId: string, payload: UserProfilePayload) => ({
  userId,
  fullName: buildFullName(payload),
  avatarUrl: payload.avatarUrl ?? null,
  bio: payload.bio?.trim() ?? undefined,
  phoneNumber: payload.phone?.trim() ?? undefined,
  address: buildAddress(payload),
});

export const getProfile = async (accessToken?: string, userId?: string): Promise<UserProfile | null> => {
  const endpoint = userId
    ? `${env.apiBaseUrl}/api/users/profiles/${userId}`
    : `${env.apiBaseUrl}/api/users/profiles/me`;

  const res = await fetch(endpoint, {
    method: 'GET',
    headers: defaultHeaders(accessToken),
  });

  if (res.status === 404) {
    return null;
  }

  if (!res.ok) {
    throw new Error(`Failed to fetch profile: ${res.status}`);
  }

  if (res.status === 204) {
    return null;
  }

  const profile = (await res.json()) as UserProfile;
  return profile;
};

export const createProfile = async (
  accessToken: string | undefined,
  userId: string,
  payload: UserProfilePayload,
): Promise<UserProfile> => {
  const res = await fetch(`${env.apiBaseUrl}/api/users/profiles`, {
    method: 'POST',
    headers: defaultHeaders(accessToken),
    body: JSON.stringify(mapPayloadToRequest(userId, payload)),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Create failed: ${res.status} ${text}`);
  }

  const created = (await res.json()) as UserProfile;
  return created;
};

export const updateProfile = async (
  accessToken: string | undefined,
  userId: string,
  payload: UserProfilePayload,
): Promise<UserProfile> => {
  const res = await fetch(`${env.apiBaseUrl}/api/users/profiles/${userId}`, {
    method: 'PUT',
    headers: defaultHeaders(accessToken),
    body: JSON.stringify(mapPayloadToRequest(userId, payload)),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Update failed: ${res.status} ${text}`);
  }

  const updated = (await res.json()) as UserProfile;
  return updated;
};
