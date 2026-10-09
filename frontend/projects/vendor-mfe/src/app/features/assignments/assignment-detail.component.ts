import { AsyncPipe, DatePipe, JsonPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { assignmentActions } from '../../store/assignments.actions';
import { selectAssignmentsError, selectSelectedAssignment } from '../../store/assignments.selectors';
import { VendorApiService } from '../../services/vendor-api.service';
import { VendorPerformanceResponse } from '../../models/vendor.models';

/**
 * Assignment detail: accept / decline / progress.
 * Backend: GET|POST|PATCH /api/vendor-assignments/{id}[ /accept|/decline|/status].
 */
@Component({
  selector: 'vendor-assignment-detail',
  standalone: true,
  imports: [ReactiveFormsModule, AsyncPipe, DatePipe, JsonPipe],
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
          @if (canAccept(a.status)) {
            <button class="btn-primary" (click)="accept(a.assignmentId)">Accept assignment</button>
          }
          @if (a.status === 'ACCEPTED') {
            <button class="btn-secondary" (click)="startProgress(a.assignmentId)">Start work</button>
          }
          @if (a.status === 'ACCEPTED' || a.status === 'IN_PROGRESS') {
            <button class="btn-secondary" (click)="complete(a.assignmentId)">Complete work</button>
          }
        </div>

        @if (a.status === 'ACCEPTED' || a.status === 'IN_PROGRESS') {
          <form [formGroup]="workForm" class="mt-4 space-y-2">
            <label class="block text-sm font-semibold">Progress or completion note</label>
            <textarea class="input-field" rows="3" formControlName="progressNote" placeholder="Record the work performed or current progress"></textarea>
            <label class="block text-sm font-semibold">Evidence document IDs (optional, comma separated)</label>
            <input class="input-field" formControlName="evidenceDocumentIds" placeholder="Document UUIDs from Document & Audit Service" />
            @if (a.status === 'IN_PROGRESS') {
              <p class="text-xs text-gray-500">Complete the assignment after the work is finished and include any available evidence document IDs.</p>
            }
          </form>
        }

        @if (a.status === 'COMPLETED') {
          <section class="mt-5 rounded border border-emerald-100 bg-emerald-50 p-3">
            <h2 class="font-semibold text-emerald-900">Record vendor performance</h2>
            <p class="mt-1 text-xs text-emerald-800">Score the completed network assignment so future recovery routing can use real partner performance.</p>
            <form [formGroup]="performanceForm" (ngSubmit)="recordPerformance(a.vendorId, a.assignmentId)" class="mt-3 grid grid-cols-2 gap-2">
              <input class="input-field" type="number" min="0" max="100" placeholder="Quality (0–100)" formControlName="qualityScore" />
              <input class="input-field" type="number" min="0" max="100" placeholder="Timeliness (0–100)" formControlName="timelinessScore" />
              <input class="input-field" type="number" min="0" max="100" placeholder="Communication (0–100)" formControlName="communicationScore" />
              <input class="input-field" type="number" min="0" max="100" placeholder="Outcome (0–100)" formControlName="outcomeScore" />
              <input class="input-field col-span-2" placeholder="Performance note (optional)" formControlName="note" />
              <button class="btn-primary col-span-2" type="submit" [disabled]="performanceForm.invalid || performanceSaving">{{ performanceSaving ? 'Saving…' : 'Record performance' }}</button>
            </form>
            @if (performanceError) { <p class="mt-2 text-sm text-rose-600" role="alert">{{ performanceError }}</p> }
            @if (performance.length) {
              <div class="mt-3 space-y-1 text-sm text-emerald-900">
                @for (score of performance; track score.recordedAt) {
                  <p><strong>Overall {{ score.overallScore }}</strong> · Quality {{ score.qualityScore }} · Timeliness {{ score.timelinessScore }} · Outcome {{ score.outcomeScore }} · {{ score.recordedAt | date:'medium' }}</p>
                }
              </div>
            }
          </section>
        }

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
  private readonly vendorApi = inject(VendorApiService);
  readonly assignment$ = this.store.select(selectSelectedAssignment);
  readonly error$ = this.store.select(selectAssignmentsError);

  readonly declineForm = inject(FormBuilder).nonNullable.group({
    declineReason: ['', [Validators.required, Validators.minLength(5)]],
  });

  readonly workForm = inject(FormBuilder).nonNullable.group({
    progressNote: [''],
    evidenceDocumentIds: [''],
  });

  readonly performanceForm = inject(FormBuilder).nonNullable.group({
    qualityScore: ['', [Validators.required, Validators.min(0), Validators.max(100)]],
    timelinessScore: ['', [Validators.required, Validators.min(0), Validators.max(100)]],
    communicationScore: ['', [Validators.required, Validators.min(0), Validators.max(100)]],
    outcomeScore: ['', [Validators.required, Validators.min(0), Validators.max(100)]],
    note: [''],
  });

  performance: VendorPerformanceResponse[] = [];
  performanceSaving = false;
  performanceError: string | null = null;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('assignmentId');
    if (id) this.store.dispatch(assignmentActions.loadOne({ assignmentId: id }));
    this.assignment$.subscribe((assignment) => {
      if (assignment?.status === 'COMPLETED' && assignment.vendorId) this.loadPerformance(assignment.vendorId);
    });
  }

  loadPerformance(vendorId: string): void {
    this.vendorApi.getPerformance(vendorId).subscribe({
      next: (scores) => { this.performance = scores; this.performanceError = null; },
      error: (error) => { this.performanceError = error?.error?.message || 'Unable to load vendor performance.'; },
    });
  }

  recordPerformance(vendorId: string, assignmentId: string): void {
    if (this.performanceForm.invalid) return;
    this.performanceSaving = true;
    this.performanceError = null;
    const value = this.performanceForm.getRawValue();
    this.vendorApi.recordPerformance(vendorId, {
      assignmentId,
      qualityScore: Number(value.qualityScore),
      timelinessScore: Number(value.timelinessScore),
      communicationScore: Number(value.communicationScore),
      outcomeScore: Number(value.outcomeScore),
      note: value.note || undefined,
    }).subscribe({
      next: (score) => { this.performance = [score, ...this.performance]; this.performanceSaving = false; },
      error: (error) => { this.performanceSaving = false; this.performanceError = error?.error?.message || 'Unable to record vendor performance.'; },
    });
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
      assignmentActions.updateStatus({
        assignmentId: id,
        request: { status: 'IN_PROGRESS', progressNote: this.workForm.controls.progressNote.value || undefined },
      })
    );
  }

  complete(id: string): void {
    this.store.dispatch(
      assignmentActions.updateStatus({
        assignmentId: id,
        request: {
          status: 'COMPLETED',
          progressNote: this.workForm.controls.progressNote.value || undefined,
          completionDate: new Date().toISOString().slice(0, 10),
          evidenceDocumentIds: this.workForm.controls.evidenceDocumentIds.value
            .split(',')
            .map((value) => value.trim())
            .filter(Boolean),
        },
      })
    );
  }

  canAccept(status: string): boolean {
    return ['DISPATCHED', 'OFFERED', 'ASSIGNED', 'PENDING', 'REQUESTED'].includes(status);
  }
}
