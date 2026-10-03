import { ApplicationConfig } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideEffects } from '@ngrx/effects';
import { provideStore } from '@ngrx/store';

import { routes } from './app.routes';
import { correlationIdInterceptor } from './core/interceptors/correlation-id.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { authReducer } from './core/store/auth/auth.reducer';
import * as AuthEffects from './core/store/auth/auth.effects';
import { uiReducer } from './core/store/ui/ui.reducer';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([correlationIdInterceptor, jwtInterceptor, errorInterceptor])),
    provideStore({ auth: authReducer, ui: uiReducer }),
    provideEffects(AuthEffects),
  ],
};
