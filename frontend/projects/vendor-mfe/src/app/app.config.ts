import { ApplicationConfig } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideEffects } from '@ngrx/effects';
import { provideStore } from '@ngrx/store';

import { routes } from './app.routes';
import * as AssignmentEffects from './store/assignments.effects';
import { assignmentsReducer } from './store/assignments.reducer';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    provideStore({ assignments: assignmentsReducer }),
    provideEffects(AssignmentEffects),
  ],
};
