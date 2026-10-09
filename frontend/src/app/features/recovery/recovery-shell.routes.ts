import { Routes } from '@angular/router';
import { PolicyholderRecoveryComponent } from './policyholder-recovery.component';

export const RECOVERY_SHELL_ROUTES: Routes = [
  { path: '', pathMatch: 'full', component: PolicyholderRecoveryComponent },
  { path: '**', component: PolicyholderRecoveryComponent },
];
