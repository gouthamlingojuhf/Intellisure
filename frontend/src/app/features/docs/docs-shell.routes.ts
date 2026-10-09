import { Routes } from '@angular/router';
import { PolicyholderDocumentsComponent } from './policyholder-documents.component';

export const DOCS_SHELL_ROUTES: Routes = [
  { path: '', pathMatch: 'full', component: PolicyholderDocumentsComponent },
  { path: '**', component: PolicyholderDocumentsComponent },
];
