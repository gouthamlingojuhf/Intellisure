import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { assignmentActions } from '../../store/assignments.actions';
import { selectAssignmentsError, selectAssignmentsLoading } from '../../store/assignments.selectors';

/** Dispatch a work order. Backend: POST /api/vendor-assignments (gateway :8080). */
@Component({
  selector: 'vendor-assignment-create',
  standalone: true,
  imports: [ReactiveFormsModule, AsyncPipe],
  template: `
    <section class="card max-w-xl">
      <h1 class="text-xl font-bold text-blue-900">New assignment</h1>
      <form [formGroup]="form" (ngSubmit)="submit()" class="mt-4 space-y-3">
        <div>
          <label class="block text-sm font-semibold">Vendor ID</label>
          <input class="input-field" formControlName="vendorId" placeholder="UUID of verified vendor" />
        </div>
        <div>
          <label class="block text-sm font-semibold">Assignment type</label>
          <select class="input-field" formControlName="assignmentType">
            <option value="CLAIM_REPAIR">CLAIM_REPAIR</option>
            <option value="CLAIM_INSPECTION">CLAIM_INSPECTION</option>
            <option value="CLAIM_TOWING">CLAIM_TOWING</option>
            <option value="RECOVERY_REPAIR">RECOVERY_REPAIR</option>
            <option value="RECOVERY_TEMPORARY_WORKSPACE">RECOVERY_TEMPORARY_WORKSPACE</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-semibold">Claim ID (optional)</label>
          <input class="input-field" formControlName="claimId" placeholder="UUID" />
        </div>
        <div>
          <label class="block text-sm font-semibold">Task description</label>
          <input class="input-field" formControlName="taskDescription" />
        </div>
        <div class="grid grid-cols-2 gap-2">
          <div>
            <label class="block text-sm font-semibold">Due date</label>
            <input class="input-field" type="date" formControlName="dueDate" />
          </div>
          <div>
            <label class="block text-sm font-semibold">Priority</label>
            <select class="input-field" formControlName="priority">
              <option>HIGH</option>
              <option>MEDIUM</option>
              <option>LOW</option>
            </select>
          </div>
        </div>
        @if (error$ | async; as err) {
          <p class="text-sm text-rose-600">{{ err }}</p>
        }
        <button class="btn-primary w-full" type="submit" [disabled]="form.invalid || (loading$ | async)">
          Dispatch
        </button>
      </form>
    </section>
  `,
})
export class AssignmentCreateComponent {
  private readonly store = inject(Store);
  readonly loading$ = this.store.select(selectAssignmentsLoading);
  readonly error$ = this.store.select(selectAssignmentsError);

  readonly form = inject(FormBuilder).nonNullable.group({
    vendorId: ['', Validators.required],
    assignmentType: ['CLAIM_REPAIR', Validators.required],
    claimId: [''],
    recoveryCaseId: [''],
    taskDescription: ['', [Validators.required, Validators.minLength(5)]],
    dueDate: ['', Validators.required],
    priority: ['HIGH', Validators.required],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.store.dispatch(
      assignmentActions.create({
        request: {
          vendorId: v.vendorId,
          assignmentType: v.assignmentType,
          claimId: v.claimId || null,
          recoveryCaseId: v.recoveryCaseId || null,
          taskDescription: v.taskDescription,
          dueDate: v.dueDate,
          priority: v.priority,
        },
      })
    );
  }
}
