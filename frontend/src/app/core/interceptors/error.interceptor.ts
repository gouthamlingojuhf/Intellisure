import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { catchError, throwError } from 'rxjs';
import { authActions } from '../store/auth/auth.actions';
import { uiActions } from '../store/ui/ui.actions';

/** Central error handling: 401→logout+login, 403→toast, 400→form mapping via store, 500→toast. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(Store);
  const router = inject(Router);
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        store.dispatch(authActions.logout());
        router.navigate(['/auth/login']);
      } else if (err.status === 403) {
        store.dispatch(uiActions.showToast({ message: 'Access denied: insufficient role privileges', kind: 'error' }));
      } else if (err.status === 404) {
        router.navigate(['/not-found']);
      } else if (err.status >= 500) {
        store.dispatch(uiActions.showToast({ message: 'Server error. Please try again.', kind: 'error' }));
      }
      return throwError(() => err);
    })
  );
};
