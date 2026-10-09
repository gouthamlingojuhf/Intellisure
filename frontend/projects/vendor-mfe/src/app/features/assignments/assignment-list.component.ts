import { AsyncPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { assignmentActions } from '../../store/assignments.actions';
import {
  selectAssignmentItems,
  selectAssignmentsError,
  selectAssignmentsLoading,
  selectAssignmentTotal,
} from '../../store/assignments.selectors';

/** Assignment work queue. Backend: GET /api/vendor-assignments (gateway :8080). */
@Component({
  selector: 'vendor-assignment-list',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AsyncPipe],
  template: `
    <section class="card">
      <h1 class="text-xl font-bold text-blue-900">Vendor assignments</h1>
      <form [formGroup]="filter" (ngSubmit)="reload()" class="mt-3 flex flex-wrap gap-2">
        <input class="input-field !w-64" placeholder="Vendor reference" formControlName="vendorId" />
        <input class="input-field !w-64" placeholder="Claim reference" formControlName="claimId" />
        <select class="input-field !w-48" formControlName="status">
          <option value="">Any status</option>
          <option>DISPATCHED</option>
          <option>OFFERED</option>
          <option>ACCEPTED</option>
          <option>DECLINED</option>
          <option>IN_PROGRESS</option>
          <option>COMPLETED</option>
        </select>
        <button class="btn-primary" type="submit">Filter</button>
        <a class="btn-secondary" routerLink="new">New assignment</a>
      </form>
      @if (error$ | async; as err) {
        <p class="mt-2 text-sm text-rose-600">{{ err }}</p>
      }
      <p class="mt-2 text-sm text-gray-600">Total: {{ total$ | async }}</p>
      <table class="table mt-2">
        <thead>
          <tr>
            <th>Type</th>
            <th>Task</th>
            <th>Status</th>
            <th>Due</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          @for (a of items$ | async; track a.assignmentId) {
            <tr>
              <td>{{ a.assignmentType }}</td>
              <td>{{ a.taskDescription }}</td>
              <td>
                <span
                  class="badge-success"
                  [class.badge-warning]="a.status === 'DISPATCHED' || a.status === 'OFFERED'"
                  [class.badge-danger]="a.status === 'DECLINED'"
                >
                  {{ a.status }}
                </span>
              </td>
              <td>{{ a.dueDate }}</td>
              <td><a class="underline" [routerLink]="[a.assignmentId]">Open</a></td>
            </tr>
          } @empty {
            <tr>
              <td colspan="5" class="text-gray-500">
                {{ (loading$ | async) ? 'Loading…' : 'No assignments found.' }}
              </td>
            </tr>
          }
        </tbody>
      </table>
    </section>
  `,
})
export class AssignmentListComponent implements OnInit {
  private readonly store = inject(Store);
  readonly items$ = this.store.select(selectAssignmentItems);
  readonly total$ = this.store.select(selectAssignmentTotal);
  readonly loading$ = this.store.select(selectAssignmentsLoading);
  readonly error$ = this.store.select(selectAssignmentsError);

  readonly filter = inject(FormBuilder).nonNullable.group({
    vendorId: [''],
    claimId: [''],
    status: [''],
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    const v = this.filter.getRawValue();
    this.store.dispatch(
      assignmentActions.loadList({
        vendorId: v.vendorId || undefined,
        claimId: v.claimId || undefined,
        status: v.status || undefined,
      })
    );
  }
}
