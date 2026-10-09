import { Routes } from '@angular/router';
import { PolicyholderDocumentsComponent } from './policyholder-documents.component';
import { PlatformGuideComponent } from './platform-guide.component';

export const DOCS_SHELL_ROUTES: Routes = [
  { path: 'guide', component: PlatformGuideComponent },
  { path: '', pathMatch: 'full', component: PolicyholderDocumentsComponent },
  { path: '**', component: PolicyholderDocumentsComponent },
];
