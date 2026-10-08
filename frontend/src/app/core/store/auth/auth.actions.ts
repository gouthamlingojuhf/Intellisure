import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { LoginRequest, LoginResponse } from '../../models/api.models';

export const authActions = createActionGroup({
  source: 'Auth',
  events: {
    login: props<{ request: LoginRequest }>(),
    loginSuccess: props<{ response: LoginResponse }>(),
    loginFailure: props<{ error: string }>(),
    logout: emptyProps(),
    restoreSession: props<{ token: string; role?: string | null; email?: string | null; userId?: string | null; customerId?: string | null }>(),
  },
});
