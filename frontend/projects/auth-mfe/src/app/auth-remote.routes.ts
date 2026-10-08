import { Routes } from '@angular/router';
import { provideState } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { authRemoteReducer } from './store/auth.reducer';
import * as AuthRemoteEffects from './store/auth.effects';
import { LoginComponent } from './features/login/login.component';
import { ProfileComponent } from './features/profile/profile.component';
import { RegisterComponent } from './features/register/register.component';

/** Exposed to the shell host as `authMfe/Routes`. */
export const AUTH_REMOTE_ROUTES: Routes = [
  {
    path: '',
    providers: [
      provideState('authRemote', authRemoteReducer),
      provideEffects(AuthRemoteEffects),
    ],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'login' },
      { path: 'login', component: LoginComponent },
      { path: 'register', component: RegisterComponent },
      { path: 'me', component: ProfileComponent },
    ],
  },
];
