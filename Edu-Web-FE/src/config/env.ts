const normalizeBaseUrl = (value?: string) => {
  if (!value) {
    return 'http://localhost:8080';
  }

  // Remove trailing slash to simplify request building.
  return value.replace(/\/$/, '');
};

export const env = {
  apiBaseUrl: normalizeBaseUrl(import.meta.env.VITE_API_BASE_URL as string | undefined),
};
