import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AssignmentsState } from './assignments.reducer';

export const selectAssignments = createFeatureSelector<AssignmentsState>('assignments');
export const selectAssignmentItems = createSelector(selectAssignments, (s) => s.items);
export const selectAssignmentTotal = createSelector(selectAssignments, (s) => s.totalElements);
export const selectSelectedAssignment = createSelector(selectAssignments, (s) => s.selected);
export const selectAssignmentsLoading = createSelector(selectAssignments, (s) => s.loading);
export const selectAssignmentsError = createSelector(selectAssignments, (s) => s.error);
