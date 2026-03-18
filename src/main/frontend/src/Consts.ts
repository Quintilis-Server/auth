export const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export const AUTH_URL = import.meta.env.VITE_AUTH_URL || "http://localhost:9000";

export const FORUM_URL = import.meta.env.VITE_FORUM_URL || "http://localhost:3000";

export const MAP_URL = import.meta.env.VITE_MAP_URL || "http://localhost:3001";

export const FRONTEND_URL = import.meta.env.VITE_FRONTEND_URL || "https://quintilis.org"

export const API_AUTH_ROUTES = `${AUTH_URL}/auth`; // Direto no Auth Service (ex: /auth/register)
export const API_OAUTH2_ROUTES = `${AUTH_URL}/oauth2`; // Endpoints OAuth2 (ex: /oauth2/token)
export const API_FORUM_ROUTES = `${API_URL}/api/forum`; // Via Gateway
