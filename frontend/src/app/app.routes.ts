import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { unauthGuard } from './core/guards/unauth.guard';
import { HomeComponent } from './features/home/home.component';
import { NotFoundComponent } from './features/not-found/not-found.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'help', loadComponent: () => import('./features/docs/platform-guide.component').then((m) => m.PlatformGuideComponent) },
  {
    path: 'dashboard',
    canActivate: [authGuard, roleGuard(['POLICYHOLDER', 'USER'])],
    loadComponent: () =>
      import('./features/dashboard/policyholder-dashboard.component').then(
        (m) => m.PolicyholderDashboardComponent
      ),
  },
  {
    path: 'profile',
    canActivate: [authGuard, roleGuard(['POLICYHOLDER', 'USER'])],
    loadComponent: () =>
      import('./features/profile/customer-profile.component').then(
        (m) => m.CustomerProfileComponent
      ),
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard(['ADMIN', 'SYSTEM_ADMINISTRATOR'])],
    loadComponent: () => import('./features/admin/admin-workspace.component').then((m) => m.AdminWorkspaceComponent),
  },
  // Remote MFEs mount here via Module Federation (loadRemoteModule).
  // Placeholders keep deep links stable until each remote lands:
  {
    path: 'auth',
    canActivate: [unauthGuard],
    loadChildren: () =>
      import('./features/auth/auth-shell.routes').then((m) => m.AUTH_SHELL_ROUTES),
  },
  {
    path: 'quotes',
    canActivate: [authGuard, roleGuard(['POLICYHOLDER', 'USER', 'UNDERWRITER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'ADMIN', 'SYSTEM_ADMINISTRATOR'])],
    children: [
      { path: '', pathMatch: 'full', loadComponent: () => import('./features/policy/quotes-list.component').then((m) => m.QuotesListComponent) },
      { path: 'new', canActivate: [roleGuard(['POLICYHOLDER', 'USER'])], loadComponent: () => import('./features/policy/quote-create.component').then((m) => m.QuoteCreateComponent) },
      { path: ':quoteId', loadComponent: () => import('./features/policy/quote-detail.component').then((m) => m.QuoteDetailComponent) },
    ],
  },
  { path: 'policy', canActivate: [authGuard, roleGuard(['POLICYHOLDER', 'USER', 'UNDERWRITER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'ADMIN', 'SYSTEM_ADMINISTRATOR'])], loadChildren: () => import('./features/policy/policy-shell.routes').then((m) => m.POLICY_SHELL_ROUTES) },
  { path: 'underwriting', canActivate: [authGuard, roleGuard(['UNDERWRITER', 'RISK_ENGINEER'])], loadChildren: () => import('./features/underwriting/underwriting-shell.routes').then((m) => m.UNDERWRITING_SHELL_ROUTES) },
  { path: 'claims', canActivate: [authGuard], loadChildren: () => import('./features/claims/claims-shell.routes').then((m) => m.CLAIMS_SHELL_ROUTES) },
  { path: 'vendor', canActivate: [authGuard, roleGuard(['VENDOR_APPLICANT', 'VENDOR_MANAGER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'ADMIN', 'SYSTEM_ADMINISTRATOR'])], loadChildren: () => import('./features/vendor/vendor-shell.routes').then((m) => m.VENDOR_SHELL_ROUTES) },
  { path: 'analytics', canActivate: [authGuard, roleGuard(['UNDERWRITER', 'RISK_ENGINEER', 'CLAIMS_MANAGER', 'ADMIN', 'SYSTEM_ADMINISTRATOR'])], loadChildren: () => import('./features/analytics/analytics-shell.routes').then((m) => m.ANALYTICS_SHELL_ROUTES) },
  { path: 'recovery', canActivate: [authGuard], loadChildren: () => import('./features/recovery/recovery-shell.routes').then((m) => m.RECOVERY_SHELL_ROUTES) },
  { path: 'docs', canActivate: [authGuard, roleGuard(['POLICYHOLDER', 'USER', 'UNDERWRITER', 'RISK_ENGINEER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'VENDOR_MANAGER', 'ADMIN', 'SYSTEM_ADMINISTRATOR'])], loadChildren: () => import('./features/docs/docs-shell.routes').then((m) => m.DOCS_SHELL_ROUTES) },
  {
    path: 'notifications',
    canActivate: [authGuard],
    loadComponent: () => import('./features/notifications/notifications.component').then((m) => m.NotificationsComponent),
  },
  {
    path: 'chat',
    canActivate: [authGuard],
    loadComponent: () => import('./features/chat/chat-workspace.component').then((m) => m.ChatWorkspaceComponent),
  },
  { path: 'not-found', component: NotFoundComponent },
  { path: '**', redirectTo: 'not-found' },
];
