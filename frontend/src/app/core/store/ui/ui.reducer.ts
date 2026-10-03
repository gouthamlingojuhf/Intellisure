import { createReducer, on } from '@ngrx/store';
import { uiActions } from './ui.actions';

export interface Toast {
  id: number;
  message: string;
  kind: 'info' | 'success' | 'error';
}

export interface UiState {
  loading: boolean;
  toasts: Toast[];
}

let nextId = 1;

export const initialUiState: UiState = { loading: false, toasts: [] };

export const uiReducer = createReducer(
  initialUiState,
  on(uiActions.setLoading, (s, { loading }) => ({ ...s, loading })),
  on(uiActions.showToast, (s, { message, kind }) => ({
    ...s,
    toasts: [...s.toasts, { id: nextId++, message, kind }],
  })),
  on(uiActions.dismissToast, (s, { id }) => ({ ...s, toasts: s.toasts.filter((t) => t.id !== id) }))
);
