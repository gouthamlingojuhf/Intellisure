// Auth DTOs — must match customer-party-service JSON contracts via gateway :8080.
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken?: string;
  token?: string;
  tokenType?: string;
  expiresIn?: number;
  userId?: string;
  customerId?: string;
  email?: string;
  role?: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
  registrationType?: 'POLICYHOLDER' | 'VENDOR_APPLICANT';
}

export interface UserProfile {
  userId?: string;
  email?: string;
  displayName?: string;
  role?: string;
  accountStatus?: string;
}
