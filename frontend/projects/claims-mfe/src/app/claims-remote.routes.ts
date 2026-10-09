import { Routes } from '@angular/router';
import { ClaimCreateComponent } from './features/claim-create/claim-create.component';
import { ClaimDetailComponent } from './features/claim-detail/claim-detail.component';
import { ClaimListComponent } from './features/claim-list/claim-list.component';
import { claimsRoleGuard } from './claims-role.guard';

export const CLAIMS_REMOTE_ROUTES: Routes = [
  { path: '', pathMatch: 'full', canActivate: [claimsRoleGuard(['POLICYHOLDER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'ADMIN', 'SYSTEM_ADMINISTRATOR'])], component: ClaimListComponent },
  { path: 'new', canActivate: [claimsRoleGuard(['POLICYHOLDER'], ['/claims'])], component: ClaimCreateComponent },
  { path: ':claimId', canActivate: [claimsRoleGuard(['POLICYHOLDER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'ADMIN', 'SYSTEM_ADMINISTRATOR'])], component: ClaimDetailComponent },
];
