import { AsyncPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { VendorApiService } from '../../services/vendor-api.service';

/**
 * Vendor onboarding queue. Backend: GET /api/vendors/onboarding-requests,
 * POST /api/vendors/{id}/verify (gateway :8080). Kept service-local (no NgRx)
 * to keep the remote slice focused on assignments.
 */
@Component({
  selector: 'vendor-onboarding-list',
  standalone: true,
  imports: [ReactiveFormsModule, AsyncPipe],
  template: `
    <section class="card">
      <h1 class="text-xl font-bold text-blue-900">Onboarding requests</h1>
      <button class="btn-secondary mt-2" (click)="reload()">Refresh</button>
      @if (error) {
        <p class="mt-2 text-sm text-rose-600">{{ error }}</p>
      }
      <table class="table mt-2">
        <thead>
          <tr>
            <th>Vendor</th>
            <th>Status</th>
            <th>Submitted</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          @for (r of items; track r.onboardingRequestId) {
            <tr>
              <td>{{ r.vendorId }}</td>
              <td>{{ r.status }}</td>
              <td>{{ r.submittedAt }}</td>
              <td class="flex gap-2">
                <button class="btn-primary !py-1" (click)="verify(r.vendorId, 'APPROVE')">Approve</button>
                <button class="btn-secondary !py-1" (click)="verify(r.vendorId, 'REJECT')">Reject</button>
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="4" class="text-gray-500">No onboarding requests.</td>
            </tr>
          }
        </tbody>
      </table>

      <h2 class="mt-6 font-bold">Submit onboarding request</h2>
      <form [formGroup]="form" (ngSubmit)="submit()" class="mt-2 grid grid-cols-2 gap-2">
        <input class="input-field" placeholder="Legal name" formControlName="legalName" />
        <input class="input-field" placeholder="Display name" formControlName="displayName" />
        <input class="input-field" placeholder="Vendor type (e.g. REPAIR)" formControlName="vendorType" />
        <input class="input-field" placeholder="Contact name" formControlName="contactName" />
        <input class="input-field" placeholder="Contact phone" formControlName="contactPhone" />
        <input class="input-field" placeholder="Contact email" formControlName="contactEmail" />
        <input
          class="input-field col-span-2"
          placeholder="Service types (comma separated)"
          formControlName="serviceTypes"
        />
        <input
          class="input-field col-span-2"
          placeholder="Service areas (comma separated)"
          formControlName="serviceAreas"
        />
        <button class="btn-primary col-span-2" type="submit" [disabled]="form.invalid">Submit</button>
      </form>
    </section>
  `,
})
export class OnboardingListComponent implements OnInit {
  private readonly api = inject(VendorApiService);
  private readonly fb = inject(FormBuilder);
  items: import('../../models/vendor.models').VendorOnboardingResponse[] = [];
  error: string | null = null;

  readonly form = this.fb.nonNullable.group({
    legalName: ['', Validators.required],
    displayName: ['', Validators.required],
    vendorType: ['', Validators.required],
    serviceTypes: ['REPAIR', Validators.required],
    capabilities: ['REPAIR'],
    serviceAreas: ['Northeast', Validators.required],
    contactName: ['', Validators.required],
    contactPhone: ['', Validators.required],
    contactEmail: ['', [Validators.required, Validators.email]],
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.api.getOnboardingRequests().subscribe({
      next: (items) => {
        this.items = items;
        this.error = null;
      },
      error: (e) => (this.error = e?.error?.message ?? 'Failed to load onboarding requests'),
    });
  }

  verify(vendorId: string, decision: 'APPROVE' | 'REJECT'): void {
    this.api
      .verifyVendor(vendorId, { verificationDecision: decision, verificationNote: `${decision} via vendor-mfe` })
      .subscribe({ next: () => this.reload(), error: () => this.reload() });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const split = (s: string) =>
      s
        .split(',')
        .map((x) => x.trim())
        .filter(Boolean);
    this.api
      .submitOnboarding({
        legalName: v.legalName,
        displayName: v.displayName,
        vendorType: v.vendorType,
        serviceTypes: split(v.serviceTypes),
        capabilities: split(v.capabilities),
        serviceAreas: split(v.serviceAreas),
        contactName: v.contactName,
        contactPhone: v.contactPhone,
        contactEmail: v.contactEmail,
      })
      .subscribe({ next: () => this.reload() });
  }
}
