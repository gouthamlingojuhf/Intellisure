import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { catchError, exhaustMap, map, of, switchMap, tap, withLatestFrom } from 'rxjs';
import { AuthApiService } from '../services/auth-api.service';
import { authRemoteActions } from './auth.actions';
import { selectRemoteToken } from './auth.selectors';

export const loginRemoteEffect = createEffect(
  (actions$ = inject(Actions), api = inject(AuthApiService), router = inject(Router)) =>
    actions$.pipe(
      ofType(authRemoteActions.login),
      exhaustMap(({ request }) =>
        api.login(request).pipe(
          tap((res) => {
            const token = res?.accessToken ?? res?.token;
            if (typeof localStorage !== 'undefined' && token) {
              localStorage.setItem('is_token', token);
              if (res.role) localStorage.setItem('is_role', res.role);
              if (res.userId) localStorage.setItem('is_user_id', res.userId);
              if (res.customerId) localStorage.setItem('is_customer_id', res.customerId);
              if (res.email) localStorage.setItem('is_email', res.email);
            }
            const role = (res?.role ?? '').toUpperCase();
            if (role === 'VENDOR_APPLICANT') {
              router.navigateByUrl('/vendor/onboarding');
            } else if (role === 'ADMIN' || role === 'SYSTEM_ADMINISTRATOR') {
              router.navigateByUrl('/admin');
            } else if (role === 'UNDERWRITER' || role === 'RISK_ENGINEER') {
              router.navigateByUrl('/underwriting');
            } else if (role === 'CLAIMS_ADJUSTER' || role === 'CLAIMS_MANAGER') {
              router.navigateByUrl('/claims');
            } else if (role === 'VENDOR_MANAGER') {
              router.navigateByUrl('/vendor');
            } else if (res?.customerId) {
              router.navigateByUrl('/dashboard');
            } else {
              router.navigateByUrl('/profile');
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
  (actions$ = inject(Actions), router = inject(Router)) =>
    actions$.pipe(
      ofType(authRemoteActions.logout),
      tap(() => {
        if (typeof localStorage !== 'undefined') {
          localStorage.removeItem('is_token');
          localStorage.removeItem('is_role');
          localStorage.removeItem('is_user_id');
          localStorage.removeItem('is_customer_id');
          localStorage.removeItem('is_email');
        }
        router.navigateByUrl('/');
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
