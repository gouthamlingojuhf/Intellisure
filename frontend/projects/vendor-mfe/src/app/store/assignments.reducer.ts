import { createReducer, on } from '@ngrx/store';
import { VendorAssignmentResponse } from '../models/vendor.models';
import { assignmentActions } from './assignments.actions';

export interface AssignmentsState {
  items: VendorAssignmentResponse[];
  totalElements: number;
  selected: VendorAssignmentResponse | null;
  loading: boolean;
  error: string | null;
}

export const initialAssignmentsState: AssignmentsState = {
  items: [],
  totalElements: 0,
  selected: null,
  loading: false,
  error: null,
};

export const assignmentsReducer = createReducer(
  initialAssignmentsState,
  on(assignmentActions.loadList, assignmentActions.loadOne, assignmentActions.create, (s) => ({
    ...s,
    loading: true,
    error: null,
  })),
  on(assignmentActions.loadListSuccess, (s, { items, totalElements }) => ({
    ...s,
    loading: false,
    items,
    totalElements,
  })),
  on(assignmentActions.loadOneSuccess, assignmentActions.createSuccess, assignmentActions.mutationSuccess, (s, a) => ({
    ...s,
    loading: false,
    selected: 'assignment' in a ? a.assignment : s.selected,
    items: 'assignment' in a ? upsert(s.items, a.assignment) : s.items,
  })),
  on(
    assignmentActions.loadListFailure,
    assignmentActions.loadOneFailure,
    assignmentActions.createFailure,
    assignmentActions.mutationFailure,
    (s, { error }) => ({ ...s, loading: false, error })
  ),
  on(
    assignmentActions.accept,
    assignmentActions.decline,
    assignmentActions.updateStatus,
    (s) => ({ ...s, loading: true, error: null })
  ),
  on(assignmentActions.clearSelected, (s) => ({ ...s, selected: null }))
);

function upsert(items: VendorAssignmentResponse[], a: VendorAssignmentResponse): VendorAssignmentResponse[] {
  const i = items.findIndex((x) => x.assignmentId === a.assignmentId);
  if (i < 0) return [a, ...items];
  return items.map((x, idx) => (idx === i ? a : x));
}
