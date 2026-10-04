import { inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, exhaustMap, map, mergeMap, of } from 'rxjs';
import { VendorApiService } from '../services/vendor-api.service';
import { assignmentActions } from './assignments.actions';

const err = (e: unknown, fallback: string) =>
  (e as { error?: { message?: string } })?.error?.message ?? fallback;

export const loadListEffect = createEffect(
  (actions$ = inject(Actions), api = inject(VendorApiService)) =>
    actions$.pipe(
      ofType(assignmentActions.loadList),
      exhaustMap(({ vendorId, claimId, status }) =>
        api.getAssignments({ vendorId, claimId, status, page: 0, size: 20 }).pipe(
          map((res) =>
            assignmentActions.loadListSuccess({ items: res.items, totalElements: res.totalElements })
          ),
          catchError((e) => of(assignmentActions.loadListFailure({ error: err(e, 'Failed to load assignments') })))
        )
      )
    ),
  { functional: true }
);

export const loadOneEffect = createEffect(
  (actions$ = inject(Actions), api = inject(VendorApiService)) =>
    actions$.pipe(
      ofType(assignmentActions.loadOne),
      exhaustMap(({ assignmentId }) =>
        api.getAssignment(assignmentId).pipe(
          map((assignment) => assignmentActions.loadOneSuccess({ assignment })),
          catchError((e) => of(assignmentActions.loadOneFailure({ error: err(e, 'Assignment not found') })))
        )
      )
    ),
  { functional: true }
);

export const createAssignmentEffect = createEffect(
  (actions$ = inject(Actions), api = inject(VendorApiService)) =>
    actions$.pipe(
      ofType(assignmentActions.create),
      exhaustMap(({ request }) =>
        api.createAssignment(request).pipe(
          map((assignment) => assignmentActions.createSuccess({ assignment })),
          catchError((e) => of(assignmentActions.createFailure({ error: err(e, 'Failed to create assignment') })))
        )
      )
    ),
  { functional: true }
);

export const mutateEffect = createEffect(
  (actions$ = inject(Actions), api = inject(VendorApiService)) =>
    actions$.pipe(
      ofType(assignmentActions.accept, assignmentActions.decline, assignmentActions.updateStatus),
      mergeMap((action) => {
        if (action.type === assignmentActions.accept.type) {
          const a = action as ReturnType<typeof assignmentActions.accept>;
          return api.acceptAssignment(a.assignmentId, a.request).pipe(
            map((assignment) => assignmentActions.mutationSuccess({ assignment })),
            catchError((e) => of(assignmentActions.mutationFailure({ error: err(e, 'Accept failed') })))
          );
        }
        if (action.type === assignmentActions.decline.type) {
          const a = action as ReturnType<typeof assignmentActions.decline>;
          return api.declineAssignment(a.assignmentId, a.request).pipe(
            map((assignment) => assignmentActions.mutationSuccess({ assignment })),
            catchError((e) => of(assignmentActions.mutationFailure({ error: err(e, 'Decline failed') })))
          );
        }
        const a = action as ReturnType<typeof assignmentActions.updateStatus>;
        return api.updateAssignmentStatus(a.assignmentId, a.request).pipe(
          map((assignment) => assignmentActions.mutationSuccess({ assignment })),
          catchError((e) => of(assignmentActions.mutationFailure({ error: err(e, 'Status update failed') })))
        );
      })
    ),
  { functional: true }
);
