import { inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, exhaustMap, map, of, tap } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { LoginResponse } from '../../models/api.models';
import { authActions } from './auth.actions';

export const loginEffect = createEffect(
  (actions$ = inject(Actions), api = inject(ApiService)) => {
    return actions$.pipe(
      ofType(authActions.login),
      exhaustMap(({ request }) =>
        api.post<LoginResponse>('/api/auth/login', request).pipe(
          tap((res) => {
            const token = res?.accessToken ?? res?.token;
            if (typeof localStorage !== 'undefined' && token) {
              localStorage.setItem('is_token', token);
              if (res.role) localStorage.setItem('is_role', res.role);
              if (res.userId) localStorage.setItem('is_user_id', res.userId);
              if (res.email) localStorage.setItem('is_email', res.email);
            }
          }),
          map((response) => authActions.loginSuccess({ response })),
          catchError((err) =>
            of(authActions.loginFailure({ error: err?.error?.message ?? 'Login failed' }))
          )
        )
      )
    );
  },
  { functional: true }
);

export const logoutEffect = createEffect(
  (actions$ = inject(Actions)) => {
    return actions$.pipe(
      ofType(authActions.logout),
      tap(() => {
        if (typeof localStorage !== 'undefined') {
          localStorage.removeItem('is_token');
          localStorage.removeItem('is_role');
          localStorage.removeItem('is_user_id');
          localStorage.removeItem('is_email');
        }
      })
    );
  },
  { functional: true, dispatch: false }
);
