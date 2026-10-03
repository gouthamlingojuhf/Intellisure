import { createReducer, on } from '@ngrx/store';
import { authRemoteActions } from './auth.actions';
import { UserProfile } from '../models/auth.models';

export interface AuthRemoteState {
  token: string | null;
  profile: UserProfile | null;
  loading: boolean;
  error: string | null;
  registered: boolean;
}

export const initialAuthRemoteState: AuthRemoteState = {
  token: typeof localStorage !== 'undefined' ? localStorage.getItem('is_token') : null,
  profile: null,
  loading: false,
  error: null,
  registered: false,
};

export const authRemoteReducer = createReducer(
  initialAuthRemoteState,
  on(authRemoteActions.login, (s) => ({ ...s, loading: true, error: null })),
  on(authRemoteActions.loginSuccess, (s, { response }) => ({
    ...s,
    loading: false,
    token: response.token,
    error: null,
  })),
  on(authRemoteActions.loginFailure, (s, { error }) => ({ ...s, loading: false, error })),
  on(authRemoteActions.register, (s) => ({ ...s, loading: true, error: null, registered: false })),
  on(authRemoteActions.registerSuccess, (s) => ({ ...s, loading: false, registered: true })),
  on(authRemoteActions.registerFailure, (s, { error }) => ({ ...s, loading: false, error })),
  on(authRemoteActions.logout, () => ({ ...initialAuthRemoteState, token: null, profile: null })),
  on(authRemoteActions.profileLoaded, (s, { profile }) => ({ ...s, profile }))
);
