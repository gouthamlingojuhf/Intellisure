import { AsyncPipe, JsonPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { assignmentActions } from '../../store/assignments.actions';
import { selectAssignmentsError, selectSelectedAssignment } from '../../store/assignments.selectors';

/**
 * Assignment detail: accept / decline / progress.
 * Backend: GET|POST|PATCH /api/vendor-assignments/{id}[ /accept|/decline|/status].
 */
@Component({
  selector: 'vendor-assignment-detail',
  standalone: true,
  imports: [ReactiveFormsModule, AsyncPipe, JsonPipe],
  template: `
    <section class="card max-w-2xl">
      <h1 class="text-xl font-bold text-blue-900">Assignment detail</h1>
      @if (assignment$ | async; as a) {
        <dl class="mt-3 grid grid-cols-2 gap-2 text-sm">
          <dt class="font-semibold">Type</dt>
          <dd>{{ a.assignmentType }}</dd>
          <dt class="font-semibold">Status</dt>
          <dd>{{ a.status }}</dd>
          <dt class="font-semibold">Task</dt>
          <dd>{{ a.taskDescription }}</dd>
          <dt class="font-semibold">Due</dt>
          <dd>{{ a.dueDate }}</dd>
          <dt class="font-semibold">Priority</dt>
          <dd>{{ a.priority }}</dd>
        </dl>

        <div class="mt-4 flex flex-wrap gap-2">
          <button class="btn-primary" (click)="accept(a.assignmentId)">Accept</button>
          <button class="btn-secondary" (click)="startProgress(a.assignmentId)">Start work</button>
          <button class="btn-secondary" (click)="complete(a.assignmentId)">Complete</button>
        </div>

        <form [formGroup]="declineForm" (ngSubmit)="decline(a.assignmentId)" class="mt-4 space-y-2">
          <label class="block text-sm font-semibold">Decline reason</label>
          <input class="input-field" formControlName="declineReason" placeholder="Required when declining" />
          <button class="btn-secondary" type="submit" [disabled]="declineForm.invalid">Decline</button>
        </form>

        @if (error$ | async; as err) {
          <p class="mt-2 text-sm text-rose-600">{{ err }}</p>
        }
        <details class="mt-4 text-xs text-gray-600">
          <summary>Raw payload</summary>
          <pre>{{ a | json }}</pre>
        </details>
      } @else {
        <p class="mt-3 text-gray-700">Loading assignment…</p>
      }
    </section>
  `,
})
export class AssignmentDetailComponent implements OnInit {
  private readonly store = inject(Store);
  private readonly route = inject(ActivatedRoute);
  readonly assignment$ = this.store.select(selectSelectedAssignment);
  readonly error$ = this.store.select(selectAssignmentsError);

  readonly declineForm = inject(FormBuilder).nonNullable.group({
    declineReason: ['', [Validators.required, Validators.minLength(5)]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('assignmentId');
    if (id) this.store.dispatch(assignmentActions.loadOne({ assignmentId: id }));
  }

  accept(id: string): void {
    this.store.dispatch(assignmentActions.accept({ assignmentId: id, request: {} }));
  }

  decline(id: string): void {
    if (this.declineForm.invalid) {
      this.declineForm.markAllAsTouched();
      return;
    }
    this.store.dispatch(
      assignmentActions.decline({ assignmentId: id, request: this.declineForm.getRawValue() })
    );
  }

  startProgress(id: string): void {
    this.store.dispatch(
      assignmentActions.updateStatus({ assignmentId: id, request: { status: 'IN_PROGRESS' } })
    );
  }

  complete(id: string): void {
    this.store.dispatch(
      assignmentActions.updateStatus({
        assignmentId: id,
        request: { status: 'COMPLETED', completionDate: new Date().toISOString().slice(0, 10) },
      })
    );
  }
}
