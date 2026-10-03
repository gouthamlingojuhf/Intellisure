import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { LoginRequest, LoginResponse, RegisterRequest, UserProfile } from '../models/auth.models';

export const authRemoteActions = createActionGroup({
  source: 'AuthMfe',
  events: {
    login: props<{ request: LoginRequest }>(),
    loginSuccess: props<{ response: LoginResponse }>(),
    loginFailure: props<{ error: string }>(),
    register: props<{ request: RegisterRequest }>(),
    registerSuccess: emptyProps(),
    registerFailure: props<{ error: string }>(),
    loadProfile: emptyProps(),
    profileLoaded: props<{ profile: UserProfile }>(),
    logout: emptyProps(),
  },
});
