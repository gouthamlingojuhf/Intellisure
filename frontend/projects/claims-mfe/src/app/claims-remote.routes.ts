import { Routes } from '@angular/router';
import { ClaimCreateComponent } from './features/claim-create/claim-create.component';
import { ClaimDetailComponent } from './features/claim-detail/claim-detail.component';
import { ClaimListComponent } from './features/claim-list/claim-list.component';

export const CLAIMS_REMOTE_ROUTES: Routes = [
  { path: '', pathMatch: 'full', component: ClaimListComponent },
  { path: 'new', component: ClaimCreateComponent },
  { path: ':claimId', component: ClaimDetailComponent },
];
