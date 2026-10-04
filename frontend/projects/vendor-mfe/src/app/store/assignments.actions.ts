import { createActionGroup, emptyProps, props } from '@ngrx/store';
import {
  AcceptAssignmentRequest,
  CreateVendorAssignmentRequest,
  DeclineAssignmentRequest,
  UpdateAssignmentStatusRequest,
  VendorAssignmentResponse,
} from '../models/vendor.models';

export const assignmentActions = createActionGroup({
  source: 'VendorMfe/Assignments',
  events: {
    loadList: props<{ vendorId?: string; claimId?: string; status?: string }>(),
    loadListSuccess: props<{ items: VendorAssignmentResponse[]; totalElements: number }>(),
    loadListFailure: props<{ error: string }>(),
    loadOne: props<{ assignmentId: string }>(),
    loadOneSuccess: props<{ assignment: VendorAssignmentResponse }>(),
    loadOneFailure: props<{ error: string }>(),
    create: props<{ request: CreateVendorAssignmentRequest }>(),
    createSuccess: props<{ assignment: VendorAssignmentResponse }>(),
    createFailure: props<{ error: string }>(),
    accept: props<{ assignmentId: string; request: AcceptAssignmentRequest }>(),
    decline: props<{ assignmentId: string; request: DeclineAssignmentRequest }>(),
    updateStatus: props<{ assignmentId: string; request: UpdateAssignmentStatusRequest }>(),
    mutationSuccess: props<{ assignment: VendorAssignmentResponse }>(),
    mutationFailure: props<{ error: string }>(),
    clearSelected: emptyProps(),
  },
});
