import { Routes } from '@angular/router';
import { provideState } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { assignmentsReducer } from './store/assignments.reducer';
import * as AssignmentEffects from './store/assignments.effects';
import { AssignmentCreateComponent } from './features/assignments/assignment-create.component';
import { AssignmentDetailComponent } from './features/assignments/assignment-detail.component';
import { AssignmentListComponent } from './features/assignments/assignment-list.component';
import { OnboardingListComponent } from './features/onboarding/onboarding-list.component';
import { vendorRoleGuard } from './vendor-role.guard';

/** Exposed to the shell host as `vendorMfe/Routes`. */
export const VENDOR_REMOTE_ROUTES: Routes = [
  {
    path: '',
    providers: [
      provideState('assignments', assignmentsReducer),
      provideEffects(AssignmentEffects),
    ],
    children: [
      { path: '', pathMatch: 'full', canActivate: [vendorRoleGuard(['VENDOR_MANAGER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'])], component: AssignmentListComponent },
      { path: 'new', canActivate: [vendorRoleGuard(['VENDOR_MANAGER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'])], component: AssignmentCreateComponent },
      { path: 'onboarding', canActivate: [vendorRoleGuard(['VENDOR_APPLICANT', 'VENDOR_MANAGER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'])], component: OnboardingListComponent },
      { path: ':assignmentId', canActivate: [vendorRoleGuard(['VENDOR_MANAGER', 'CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'])], component: AssignmentDetailComponent },
    ],
  },
];
