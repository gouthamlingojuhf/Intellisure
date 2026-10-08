import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { map, take } from 'rxjs';
import { selectIsAuthenticated } from '../store/auth/auth.selectors';
import { authActions } from '../store/auth/auth.actions';

export const authGuard: CanActivateFn = () => {
  const store = inject(Store);
  const router = inject(Router);
  return store.select(selectIsAuthenticated).pipe(
    take(1),
    map((ok) => {
      if (ok) return true;
      if (typeof localStorage !== 'undefined') {
        const token = localStorage.getItem('is_token');
        if (token) {
          const role = localStorage.getItem('is_role');
          const userId = localStorage.getItem('is_user_id');
          const email = localStorage.getItem('is_email');
          const customerId = localStorage.getItem('is_customer_id');
          store.dispatch(authActions.restoreSession({ token, role, email, userId, customerId }));
          return true;
        }
      }
      return router.createUrlTree(['/auth']);
    })
  );
};
