import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';
import { UnderwritingDashboardComponent } from './underwriting-dashboard.component';

export const UNDERWRITING_SHELL_ROUTES: Routes = [
  {
    path: '**',
    component: UnderwritingDashboardComponent,
    canActivate: [roleGuard(['UNDERWRITER', 'RISK_ENGINEER', 'ADMIN'])],
  },
];
