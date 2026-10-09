import { AsyncPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { assignmentActions } from '../../store/assignments.actions';
import { selectAssignmentsError, selectAssignmentsLoading } from '../../store/assignments.selectors';
import { VendorApiService } from '../../services/vendor-api.service';
import { VendorResponse } from '../../models/vendor.models';

/** Dispatch a work order. Backend: POST /api/vendor-assignments (gateway :8080). */
@Component({
  selector: 'vendor-assignment-create',
  standalone: true,
  imports: [ReactiveFormsModule, AsyncPipe],
  template: `
    <section class="card max-w-xl">
      <h1 class="text-xl font-bold text-blue-900">Dispatch network vendor work</h1>
      <p class="mt-2 text-sm text-gray-600">Only verified and active vendors returned by the live directory can receive a network assignment.</p>
      <form [formGroup]="form" (ngSubmit)="submit()" class="mt-4 space-y-3">
        <div class="rounded border border-blue-100 bg-blue-50 p-3">
          <h2 class="font-semibold text-blue-900">Find an eligible vendor</h2>
          <div class="mt-2 grid grid-cols-2 gap-2">
            <input class="input-field" placeholder="Service type (optional)" formControlName="serviceTypeFilter" />
            <input class="input-field" placeholder="Service area (optional)" formControlName="locationFilter" />
          </div>
          <button class="btn-secondary mt-2" type="button" (click)="loadVendors()" [disabled]="vendorsLoading">
            {{ vendorsLoading ? 'Loading vendors…' : 'Refresh eligible vendors' }}
          </button>
          @if (vendorsError) {
            <p class="mt-2 text-sm text-rose-600" role="alert">{{ vendorsError }}</p>
          } @else if (!vendorsLoading && vendors.length === 0) {
            <p class="mt-2 text-sm text-gray-600">No verified active vendors match these filters.</p>
          }
        </div>
        <div>
          <label class="block text-sm font-semibold">Verified vendor</label>
          <select class="input-field" formControlName="vendorId">
            <option value="">Select a verified active vendor</option>
            @for (vendor of vendors; track vendor.vendorId) {
              <option [value]="vendor.vendorId">{{ vendor.displayName }} · {{ vendor.vendorType }} · {{ vendor.serviceAreas.join(', ') }}</option>
            }
          </select>
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
          <label class="block text-sm font-semibold">Claim reference (optional)</label>
          <input class="input-field" formControlName="claimId" placeholder="Enter the claim reference" />
        </div>
        <div>
          <label class="block text-sm font-semibold">Recovery case reference (optional)</label>
          <input class="input-field" formControlName="recoveryCaseId" placeholder="Enter the recovery case reference" />
          <p class="mt-1 text-xs text-gray-500">A recovery assignment is created only for the explicit network-vendor path. Customer-owned paths never dispatch here.</p>
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
        <button class="btn-primary w-full" type="submit" [disabled]="form.invalid || (loading$ | async) || vendorsLoading">
          Dispatch selected vendor
        </button>
      </form>
    </section>
  `,
})
export class AssignmentCreateComponent implements OnInit {
  private readonly store = inject(Store);
  private readonly vendorApi = inject(VendorApiService);
  readonly loading$ = this.store.select(selectAssignmentsLoading);
  readonly error$ = this.store.select(selectAssignmentsError);
  vendors: VendorResponse[] = [];
  vendorsLoading = false;
  vendorsError: string | null = null;

  readonly form = inject(FormBuilder).nonNullable.group({
    vendorId: ['', Validators.required],
    serviceTypeFilter: [''],
    locationFilter: [''],
    assignmentType: ['CLAIM_REPAIR', Validators.required],
    claimId: [''],
    recoveryCaseId: [''],
    recoveryPath: ['NETWORK_VENDOR' as const],
    taskDescription: ['', [Validators.required, Validators.minLength(5)]],
    dueDate: ['', Validators.required],
    priority: ['HIGH', Validators.required],
  });

  ngOnInit(): void {
    this.loadVendors();
  }

  loadVendors(): void {
    const value = this.form.getRawValue();
    this.vendorsLoading = true;
    this.vendorsError = null;
    this.vendorApi.searchVendors({
      serviceType: value.serviceTypeFilter || undefined,
      location: value.locationFilter || undefined,
      page: 0,
      size: 50,
    }).subscribe({
      next: (response) => {
        this.vendors = response.items;
        this.vendorsLoading = false;
        if (!this.vendors.some((vendor) => vendor.vendorId === this.form.controls.vendorId.value)) {
          this.form.controls.vendorId.setValue('');
        }
      },
      error: (error) => {
        this.vendorsLoading = false;
        this.vendorsError = error?.error?.message || 'Unable to load verified and active vendors.';
      },
    });
  }

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
          recoveryPath: v.recoveryCaseId ? 'NETWORK_VENDOR' : null,
          taskDescription: v.taskDescription,
          dueDate: v.dueDate,
          priority: v.priority,
        },
      })
    );
  }
}
