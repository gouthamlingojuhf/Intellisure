import { Routes } from '@angular/router';
import { AssignmentCreateComponent } from './features/assignments/assignment-create.component';
import { AssignmentDetailComponent } from './features/assignments/assignment-detail.component';
import { AssignmentListComponent } from './features/assignments/assignment-list.component';
import { OnboardingListComponent } from './features/onboarding/onboarding-list.component';

/** Exposed to the shell host as `vendorMfe/Routes`. */
export const VENDOR_REMOTE_ROUTES: Routes = [
  { path: '', pathMatch: 'full', component: AssignmentListComponent },
  { path: 'new', component: AssignmentCreateComponent },
  { path: 'onboarding', component: OnboardingListComponent },
  { path: ':assignmentId', component: AssignmentDetailComponent },
];
