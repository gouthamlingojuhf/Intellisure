import { createReducer, on } from '@ngrx/store';
import { authActions } from './auth.actions';

export interface AuthState {
  token: string | null;
  userId: string | null;
  email: string | null;
  role: string | null;
  isAuthenticated: boolean;
  loginError: string | null;
  loading: boolean;
}

export const initialAuthState: AuthState = {
  token: typeof localStorage !== 'undefined' ? localStorage.getItem('is_token') : null,
  userId: null,
  email: null,
  role: null,
  isAuthenticated: false,
  loginError: null,
  loading: false,
};

export const authReducer = createReducer(
  initialAuthState,
  on(authActions.login, (s) => ({ ...s, loading: true, loginError: null })),
  on(authActions.loginSuccess, (s, { response }) => ({
    ...s,
    loading: false,
    token: response.token,
    userId: response.userId ?? null,
    email: response.email ?? null,
    role: response.role ?? null,
    isAuthenticated: true,
    loginError: null,
  })),
  on(authActions.loginFailure, (s, { error }) => ({ ...s, loading: false, loginError: error })),
  on(authActions.logout, () => ({ ...initialAuthState, token: null })),
  on(authActions.restoreSession, (s, { token }) => ({ ...s, token, isAuthenticated: !!token }))
);
