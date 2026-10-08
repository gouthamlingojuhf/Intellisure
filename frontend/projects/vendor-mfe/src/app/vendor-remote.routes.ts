import { Routes } from '@angular/router';
import { provideState } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { assignmentsReducer } from './store/assignments.reducer';
import * as AssignmentEffects from './store/assignments.effects';
import { AssignmentCreateComponent } from './features/assignments/assignment-create.component';
import { AssignmentDetailComponent } from './features/assignments/assignment-detail.component';
import { AssignmentListComponent } from './features/assignments/assignment-list.component';
import { OnboardingListComponent } from './features/onboarding/onboarding-list.component';

/** Exposed to the shell host as `vendorMfe/Routes`. */
export const VENDOR_REMOTE_ROUTES: Routes = [
  {
    path: '',
    providers: [
      provideState('assignments', assignmentsReducer),
      provideEffects(AssignmentEffects),
    ],
    children: [
      { path: '', pathMatch: 'full', component: AssignmentListComponent },
      { path: 'new', component: AssignmentCreateComponent },
      { path: 'onboarding', component: OnboardingListComponent },
      { path: ':assignmentId', component: AssignmentDetailComponent },
    ],
  },
];
