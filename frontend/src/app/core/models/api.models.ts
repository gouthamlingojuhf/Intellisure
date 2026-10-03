// Shared DTOs matching backend JSON contracts (gateway :8080).
// Keep in sync with backend DTOs; see FRONTEND_MICROFRONTEND_PLAN.md §5.

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType?: string;
  expiresIn?: number;
  userId?: string;
  email?: string;
  role?: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

export interface ApiError {
  message: string;
  fieldErrors?: Record<string, string>;
}

export type UserRole =
  | 'POLICYHOLDER'
  | 'UNDERWRITER'
  | 'CLAIMS_ADJUSTER'
  | 'VENDOR_MANAGER'
  | 'SYSTEM_ADMINISTRATOR';
