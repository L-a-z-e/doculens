import { apiFetch } from './client';
import type { AuthResponse, LoginRequest, RegisterRequest } from '@/types/auth';

export function register(data: RegisterRequest): Promise<AuthResponse> {
  return apiFetch('/auth/register', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function login(data: LoginRequest): Promise<AuthResponse> {
  return apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}
