import { createActionGroup, props } from '@ngrx/store';

export const uiActions = createActionGroup({
  source: 'UI',
  events: {
    showToast: props<{ message: string; kind: 'info' | 'success' | 'error' }>(),
    dismissToast: props<{ id: number }>(),
    setLoading: props<{ loading: boolean }>(),
  },
});
