import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AuthRemoteState } from './auth.reducer';

export const selectAuthRemote = createFeatureSelector<AuthRemoteState | undefined>('authRemote');
export const selectRemoteToken = createSelector(selectAuthRemote, (s) => s?.token ?? null);
export const selectRemoteProfile = createSelector(selectAuthRemote, (s) => s?.profile ?? null);
export const selectRemoteLoading = createSelector(selectAuthRemote, (s) => s?.loading ?? false);
export const selectRemoteError = createSelector(selectAuthRemote, (s) => s?.error ?? null);
export const selectRegistered = createSelector(selectAuthRemote, (s) => s?.registered ?? false);
