import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { switchMap, take } from 'rxjs';
import { selectAuthToken } from '../store/auth/auth.selectors';

/** Injects `Authorization: Bearer <jwt>` from NgRx AuthState. */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(Store);
  return store.select(selectAuthToken).pipe(
    take(1),
    switchMap((token) => {
      if (token && req.url.startsWith('http')) {
        req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
      }
      return next(req);
    })
  );
};
