import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { map, take } from 'rxjs';
import { selectIsAuthenticated } from '../store/auth/auth.selectors';

/**
 * Prevents authenticated users from accessing public authentication pages (/auth/login, /auth/register).
 * If authenticated, redirects to /policy (or appropriate workspace destination).
 */
export const unauthGuard: CanActivateFn = () => {
  const store = inject(Store);
  const router = inject(Router);

  return store.select(selectIsAuthenticated).pipe(
    take(1),
    map((isAuth) => {
      if (isAuth) {
        return router.createUrlTree(['/policy']);
      }
      if (typeof localStorage !== 'undefined') {
        const token = localStorage.getItem('is_token');
        if (token) {
          return router.createUrlTree(['/policy']);
        }
      }
      return true;
    })
  );
};
