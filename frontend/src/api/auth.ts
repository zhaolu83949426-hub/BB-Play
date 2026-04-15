import http from './http';

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

export function login(username: string, password: string) {
  return http.post<AuthResponse>('/auth/login', { username, password });
}

export function register(username: string, password: string) {
  return http.post<void>('/auth/register', { username, password });
}

export function validateToken() {
  return http.get<AuthResponse>('/auth/validate');
}
