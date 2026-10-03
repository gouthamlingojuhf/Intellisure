import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AuthRemoteState } from './auth.reducer';

export const selectAuthRemote = createFeatureSelector<AuthRemoteState>('authRemote');
export const selectRemoteToken = createSelector(selectAuthRemote, (s) => s.token);
export const selectRemoteProfile = createSelector(selectAuthRemote, (s) => s.profile);
export const selectRemoteLoading = createSelector(selectAuthRemote, (s) => s.loading);
export const selectRemoteError = createSelector(selectAuthRemote, (s) => s.error);
export const selectRegistered = createSelector(selectAuthRemote, (s) => s.registered);
