import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export function vendorRoleGuard(allowedRoles: string[]): CanActivateFn {
  return () => {
    const role = typeof localStorage !== 'undefined' ? (localStorage.getItem('is_role') ?? '').toUpperCase() : '';
    if (allowedRoles.map((allowed) => allowed.toUpperCase()).includes(role)) return true;
    return inject(Router).createUrlTree(['/vendor/onboarding']);
  };
}
