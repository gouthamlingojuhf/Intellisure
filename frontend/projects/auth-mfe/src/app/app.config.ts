import { ApplicationConfig } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideEffects } from '@ngrx/effects';
import { provideStore } from '@ngrx/store';

import { routes } from './app.routes';
import * as AuthRemoteEffects from './store/auth.effects';
import { authRemoteReducer } from './store/auth.reducer';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    provideStore({ authRemote: authRemoteReducer }),
    provideEffects(AuthRemoteEffects),
  ],
};
