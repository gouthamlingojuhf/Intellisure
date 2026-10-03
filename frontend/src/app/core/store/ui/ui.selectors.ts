import { createFeatureSelector, createSelector } from '@ngrx/store';
import { UiState } from './ui.reducer';

export const selectUiState = createFeatureSelector<UiState>('ui');
export const selectGlobalLoading = createSelector(selectUiState, (s) => s.loading);
export const selectToasts = createSelector(selectUiState, (s) => s.toasts);
