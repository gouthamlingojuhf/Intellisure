import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export function claimsRoleGuard(allowedRoles: string[], fallback: string[] = ['/']): CanActivateFn {
  return () => {
    let role = typeof localStorage !== 'undefined' ? (localStorage.getItem('is_role') ?? '').toUpperCase() : '';
    role = role.replace(/^ROLE_/, '');
    if (role === 'USER') role = 'POLICYHOLDER';
    const normalizedAllowed = allowedRoles.map((allowed) => {
      let norm = allowed.toUpperCase().replace(/^ROLE_/, '');
      if (norm === 'USER') norm = 'POLICYHOLDER';
      return norm;
    });
    if (normalizedAllowed.includes(role)) return true;
    return inject(Router).createUrlTree(fallback);
  };
}
