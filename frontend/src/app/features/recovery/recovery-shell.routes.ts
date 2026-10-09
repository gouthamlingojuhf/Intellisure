import { Routes } from '@angular/router';
import { PolicyholderRecoveryComponent } from './policyholder-recovery.component';
import { roleGuard } from '../../core/guards/role.guard';

export const RECOVERY_SHELL_ROUTES: Routes = [
  { path: '', pathMatch: 'full', canActivate: [roleGuard(['POLICYHOLDER', 'USER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'])], component: PolicyholderRecoveryComponent },
  { path: '**', canActivate: [roleGuard(['POLICYHOLDER', 'USER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'])], component: PolicyholderRecoveryComponent },
];
