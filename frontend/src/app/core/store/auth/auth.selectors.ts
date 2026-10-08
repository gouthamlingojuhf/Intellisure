import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AuthState } from './auth.reducer';

export const selectAuthState = createFeatureSelector<AuthState>('auth');
export const selectAuthToken = createSelector(selectAuthState, (s) => s.token);
export const selectIsAuthenticated = createSelector(selectAuthState, (s) => s.isAuthenticated);
export const selectUserRole = createSelector(selectAuthState, (s) => s.role);
export const selectUserId = createSelector(selectAuthState, (s) => s.userId);
export const selectCustomerId = createSelector(selectAuthState, (s) => s.customerId);
export const selectLoginError = createSelector(selectAuthState, (s) => s.loginError);
export const selectAuthLoading = createSelector(selectAuthState, (s) => s.loading);
