import { Routes } from '@angular/router';

export const POLICY_SHELL_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./policy-list.component').then((m) => m.PolicyListComponent),
  },
  {
    path: 'quotes/new',
    loadComponent: () =>
      import('./quote-create.component').then((m) => m.QuoteCreateComponent),
  },
  {
    path: 'quotes/:quoteId',
    loadComponent: () =>
      import('./quote-detail.component').then((m) => m.QuoteDetailComponent),
  },
  {
    path: ':policyId',
    loadComponent: () =>
      import('./policy-detail.component').then((m) => m.PolicyDetailComponent),
  },
];
