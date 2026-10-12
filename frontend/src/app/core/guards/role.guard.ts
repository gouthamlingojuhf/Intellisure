import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { map, take } from 'rxjs';
import { selectUserRole } from '../store/auth/auth.selectors';
import { uiActions } from '../store/ui/ui.actions';

/** Usage: canActivate: [roleGuard(['UNDERWRITER','SYSTEM_ADMINISTRATOR'])] via route data.roles */
export function roleGuard(allowed: string[]): CanActivateFn {
  return (route: ActivatedRouteSnapshot) => {
    const store = inject(Store);
    const router = inject(Router);
    const roles = (route.data?.['roles'] as string[] | undefined) ?? allowed;
    return store.select(selectUserRole).pipe(
      take(1),
      map((role) => {
        const cleanRole = (role ?? '').trim().toUpperCase().replace(/^ROLE_/, '');
        if (cleanRole && roles.some((allowedRole) => allowedRole.trim().toUpperCase().replace(/^ROLE_/, '') === cleanRole)) return true;
        store.dispatch(uiActions.showToast({ message: 'Access denied: insufficient role privileges', kind: 'error' }));
        return router.createUrlTree(['/']);
      })
    );
  };
}
