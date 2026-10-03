import { inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { catchError, exhaustMap, map, of, switchMap, tap, withLatestFrom } from 'rxjs';
import { AuthApiService } from '../services/auth-api.service';
import { authRemoteActions } from './auth.actions';
import { selectRemoteToken } from './auth.selectors';

export const loginRemoteEffect = createEffect(
  (actions$ = inject(Actions), api = inject(AuthApiService)) =>
    actions$.pipe(
      ofType(authRemoteActions.login),
      exhaustMap(({ request }) =>
        api.login(request).pipe(
          tap((res) => {
            alert('login response: auth.effects ' + JSON.stringify(res));
            if (typeof localStorage !== 'undefined' && res?.token) {
              localStorage.setItem('is_token', res.token);
            }
          }),
          map((response) => authRemoteActions.loginSuccess({ response })),
          catchError((err) =>
            of(authRemoteActions.loginFailure({ error: err?.error?.message ?? 'Login failed' }))
          )
        )
      )
    ),
  { functional: true }
);

export const registerRemoteEffect = createEffect(
  (actions$ = inject(Actions), api = inject(AuthApiService)) =>
    actions$.pipe(
      ofType(authRemoteActions.register),
      exhaustMap(({ request }) =>
        api.register(request).pipe(
          map(() => authRemoteActions.registerSuccess()),
          catchError((err) =>
            of(authRemoteActions.registerFailure({ error: err?.error?.message ?? 'Registration failed' }))
          )
        )
      )
    ),
  { functional: true }
);

export const logoutRemoteEffect = createEffect(
  (actions$ = inject(Actions)) =>
    actions$.pipe(
      ofType(authRemoteActions.logout),
      tap(() => {
        if (typeof localStorage !== 'undefined') localStorage.removeItem('is_token');
      })
    ),
  { functional: true, dispatch: false }
);

export const loadProfileRemoteEffect = createEffect(
  (actions$ = inject(Actions), api = inject(AuthApiService), store = inject(Store)) =>
    actions$.pipe(
      ofType(authRemoteActions.loadProfile),
      withLatestFrom(store.select(selectRemoteToken)),
      switchMap(([, token]) => {
        if (!token) return of(authRemoteActions.loginFailure({ error: 'Not authenticated' }));
        return api.me(token).pipe(
          map((profile) => authRemoteActions.profileLoaded({ profile })),
          catchError((err) =>
            of(authRemoteActions.loginFailure({ error: err?.error?.message ?? 'Profile load failed' }))
          )
        );
      })
    ),
  { functional: true }
);
