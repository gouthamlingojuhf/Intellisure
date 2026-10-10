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
  styles: [`
    :host { display: block; padding: 24px 0; }
    .card { background: var(--surface, #fff); border: 1px solid var(--border, #eae5df); border-radius: 10px; padding: 24px; box-shadow: var(--shadow, 0 1px 3px rgba(0,0,0,0.05)); }
    h1 { margin: 0 0 16px; font-size: 24px; font-weight: 700; color: var(--ink, #111); letter-spacing: -0.03em; }
    form { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; margin-bottom: 16px; }
    .input-field { min-height: 38px; padding: 8px 12px; border: 1px solid var(--border, #eae5df); border-radius: 6px; background: var(--surface, #fff); color: var(--ink, #111); font-size: 12px; }
    .input-field:focus { outline: none; border-color: var(--claret, #75013f); box-shadow: 0 0 0 3px rgba(117, 1, 63, 0.1); }
    .btn-primary { min-height: 38px; padding: 0 16px; border: 1px solid var(--claret, #75013f); border-radius: 6px; background: var(--claret, #75013f); color: #fff; font-size: 12px; font-weight: 600; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; text-decoration: none; }
    .btn-primary:hover { background: var(--claret-hover, #8f1750); }
    .btn-secondary { min-height: 38px; padding: 0 16px; border: 1px solid var(--border, #eae5df); border-radius: 6px; background: var(--surface, #fff); color: var(--ink, #272427); font-size: 12px; font-weight: 600; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; text-decoration: none; }
    .btn-secondary:hover { background: var(--warm-light, #f7f5f3); }
    .table { width: 100%; border-collapse: collapse; margin-top: 16px; font-size: 12px; }
    .table th { padding: 12px 14px; text-align: left; font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: var(--muted, #6f6a6d); border-bottom: 1px solid var(--border, #eae5df); background: var(--surface-hover, #faf9f8); }
    .table td { padding: 14px; border-bottom: 1px solid var(--border, #eae5df); color: var(--ink, #272427); }
    .table tr:hover td { background: var(--surface-hover, #faf9f8); }
    .badge-success, .badge-warning, .badge-danger { display: inline-flex; align-items: center; padding: 3px 8px; border-radius: 999px; font-size: 10px; font-weight: 700; text-transform: uppercase; }
    .badge-success { background: var(--success-light, #eaf6f0); color: var(--success, #176b45); }
    .badge-warning { background: var(--warning-light, #fff5dc); color: var(--warning, #9a5b00); }
    .badge-danger { background: var(--danger-light, #fdecea); color: var(--danger, #a32120); }
    .underline { color: var(--claret, #75013f); font-weight: 600; text-decoration: none; }
    .underline:hover { text-decoration: underline; }
  `],
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
