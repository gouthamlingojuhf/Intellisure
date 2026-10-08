import { createReducer, on } from '@ngrx/store';
import { authActions } from './auth.actions';

export interface AuthState {
  token: string | null;
  userId: string | null;
  customerId: string | null;
  email: string | null;
  role: string | null;
  isAuthenticated: boolean;
  loginError: string | null;
  loading: boolean;
}

export const initialAuthState: AuthState = {
  token: typeof localStorage !== 'undefined' ? localStorage.getItem('is_token') : null,
  userId: typeof localStorage !== 'undefined' ? localStorage.getItem('is_user_id') : null,
  customerId: typeof localStorage !== 'undefined' ? localStorage.getItem('is_customer_id') : null,
  email: typeof localStorage !== 'undefined' ? localStorage.getItem('is_email') : null,
  role: typeof localStorage !== 'undefined' ? localStorage.getItem('is_role') : null,
  isAuthenticated: typeof localStorage !== 'undefined' ? !!localStorage.getItem('is_token') : false,
  loginError: null,
  loading: false,
};

export const authReducer = createReducer(
  initialAuthState,
  on(authActions.login, (s) => ({ ...s, loading: true, loginError: null })),
  on(authActions.loginSuccess, (s, { response }) => {
    const token = response.accessToken ?? response.token ?? null;
    return {
      ...s,
      loading: false,
      token,
      userId: response.userId ?? null,
      customerId: response.customerId ?? null,
      email: response.email ?? null,
      role: response.role ?? null,
      isAuthenticated: !!token,
      loginError: null,
    };
  }),
  on(authActions.loginFailure, (s, { error }) => ({ ...s, loading: false, loginError: error })),
  on(authActions.logout, () => ({
    ...initialAuthState,
    token: null,
    userId: null,
    customerId: null,
    email: null,
    role: null,
    isAuthenticated: false,
  })),
  on(authActions.restoreSession, (s, { token, role, email, userId, customerId }) => ({
    ...s,
    token,
    role: role ?? s.role,
    email: email ?? s.email,
    userId: userId ?? s.userId,
    customerId: customerId ?? s.customerId,
    isAuthenticated: !!token,
  }))
);
