import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';

import { CustomerProfileService } from '../../core/services/customer-profile.service';
import { CustomerResponse } from '../../core/models/customer.models';
import { uiActions } from '../../core/store/ui/ui.actions';
import { authActions } from '../../core/store/auth/auth.actions';
import { selectAuthToken, selectUserId, selectUserRole } from '../../core/store/auth/auth.selectors';

// ui-core components
import {
  CardComponent,
  InputComponent,
  SelectComponent,
  ButtonComponent,
  SkeletonComponent,
  BadgeComponent,
} from 'ui-core';

@Component({
  selector: 'is-customer-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    CardComponent,
    InputComponent,
    SelectComponent,
    ButtonComponent,
    SkeletonComponent,
    BadgeComponent,
  ],
  template: `
    <div class="profile-page">
      <!-- Breadcrumb / Header Area -->
      <header class="page-header">
        <div class="header-left">
          <p class="page-eyebrow">Enterprise Account</p>
          <div class="title-row">
            <h1>Business Profile</h1>
            @if (profile) {
              <is-badge variant="success" size="md">Profile Active</is-badge>
            } @else if (!loading && notFound) {
              <is-badge variant="warning" size="md">Incomplete Setup</is-badge>
            }
          </div>
          <p class="page-description">
            Maintain your legal entity, risk classification, and principal business contact information required for commercial quoting, underwriting, and policy issuance.
          </p>
        </div>
      </header>

      <!-- Loading State -->
      @if (loading) {
        <div class="loading-state" role="status" aria-live="polite">
          <is-skeleton variant="text" width="45%" height="32px" />
          <is-skeleton variant="text" width="65%" height="16px" />
          <div class="skeleton-grid">
            <is-skeleton variant="card" />
            <is-skeleton variant="card" />
          </div>
        </div>
      } @else {
        <!-- Onboarding / No Profile State Notice -->
        @if (notFound) {
          <div class="onboarding-notice" role="region" aria-label="Onboarding notice">
            <div class="notice-icon" aria-hidden="true">🏢</div>
            <div class="notice-body">
              <h3>Complete your business profile</h3>
              <p>
                Welcome to IntelliSure! To begin creating insurance quotes, bind policies, or file claims, we require your verified commercial business and address details. Fill out the information below to activate your policyholder account.
              </p>
            </div>
          </div>
        }

        <!-- Error Alert Banner -->
        @if (errorMessage) {
          <div class="error-banner" role="alert">
            <div class="error-content">
              <strong>Error updating profile:</strong>
              <p>{{ errorMessage }}</p>
            </div>
            <is-button variant="secondary" size="sm" (click)="loadProfile()">Retry</is-button>
          </div>
        }

        <!-- Success Feedback Banner -->
        @if (successMessage) {
          <div class="success-banner" role="status">
            <div class="success-content">
              <strong>Success:</strong>
              <p>{{ successMessage }}</p>
            </div>
            <div class="success-actions">
              <is-button variant="primary" size="sm" routerLink="/policy">
                View Policies & Quotes →
              </is-button>
            </div>
          </div>
        }

        <!-- Form Wrapper -->
        <form [formGroup]="profileForm" (ngSubmit)="onSave()" class="profile-form">
          <!-- Card 1: Business Information -->
          <is-card
            title="Business Information"
            subtitle="Entity legal identification, owner principal, and trade classification"
          >
            <div class="form-grid">
              <div class="col-span-2">
                <is-input
                  id="businessName"
                  label="Legal Business Name *"
                  placeholder="e.g. Apex Freight Logistics LLC"
                  formControlName="businessName"
                  [error]="getFieldError('businessName')"
                  [disabled]="saving"
                />
              </div>

              <div>
                <is-input
                  id="ownerName"
                  label="Owner / Principal Name *"
                  placeholder="e.g. Jane Doe"
                  formControlName="ownerName"
                  [error]="getFieldError('ownerName')"
                  [disabled]="saving"
                />
              </div>

              <div>
                <is-select
                  id="businessType"
                  label="Business Entity Type"
                  placeholder="Select classification"
                  [options]="businessTypeOptions"
                  formControlName="businessType"
                  [disabled]="saving"
                />
              </div>

              <div class="col-span-2">
                <is-input
                  id="phone"
                  label="Contact Phone Number"
                  type="tel"
                  placeholder="e.g. +1 (555) 019-2834"
                  formControlName="phone"
                  [error]="getFieldError('phone')"
                  [disabled]="saving"
                />
              </div>
            </div>
          </is-card>

          <!-- Card 2: Operating Address -->
          <is-card
            title="Business Operating Address"
            subtitle="Headquarters location and principal place of operations"
          >
            <div class="form-grid">
              <div class="col-span-2">
                <is-input
                  id="address"
                  label="Street Address"
                  placeholder="e.g. 100 Corporate Center Parkway, Suite 400"
                  formControlName="address"
                  [disabled]="saving"
                />
              </div>

              <div>
                <is-input
                  id="city"
                  label="City"
                  placeholder="e.g. Hartford"
                  formControlName="city"
                  [disabled]="saving"
                />
              </div>

              <div>
                <is-input
                  id="state"
                  label="State / Province"
                  placeholder="e.g. CT"
                  formControlName="state"
                  [disabled]="saving"
                />
              </div>

              <div>
                <is-input
                  id="postalCode"
                  label="Postal / ZIP Code"
                  placeholder="e.g. 06103"
                  formControlName="postalCode"
                  [error]="getFieldError('postalCode')"
                  [disabled]="saving"
                />
              </div>

              <div>
                <is-input
                  id="country"
                  label="Country"
                  placeholder="e.g. USA"
                  formControlName="country"
                  [disabled]="saving"
                />
              </div>
            </div>
          </is-card>

          <!-- Metadata & Actions Bar -->
          <div class="actions-bar">
            <div class="metadata-info">
              @if (profile?.customerId) {
                <span class="customer-tag">
                  <strong>Customer ID:</strong> <code>{{ profile?.customerId }}</code>
                </span>
              }
              @if (profile?.updatedAt) {
                <span class="last-updated">
                  Last updated: {{ profile?.updatedAt | date:'medium' }}
                </span>
              }
            </div>

            <div class="action-buttons">
              <is-button
                variant="secondary"
                type="button"
                (click)="onReset()"
                [disabled]="saving || !profileForm.dirty"
              >
                Reset Changes
              </is-button>

              <is-button
                variant="primary"
                type="submit"
                [disabled]="saving || profileForm.invalid"
              >
                @if (saving) {
                  Saving Changes…
                } @else if (notFound) {
                  Create Business Profile
                } @else {
                  Save Changes
                }
              </is-button>
            </div>
          </div>
        </form>

        <!-- Post-Save Quick Actions / Next Steps -->
        @if (profile) {
          <div class="next-steps-panel">
            <h4>Next Steps for Your Account</h4>
            <div class="next-steps-grid">
              <div class="step-card">
                <div class="step-card-header">
                  <span class="step-icon">📋</span>
                  <h5>Get an Insurance Quote</h5>
                </div>
                <p>Request commercial property, liability, or business owners coverage with instant rate calculations.</p>
                <is-button variant="secondary" size="sm" routerLink="/policy">
                  Manage Quotes & Policies →
                </is-button>
              </div>

              <div class="step-card">
                <div class="step-card-header">
                  <span class="step-icon">📄</span>
                  <h5>Claims Center</h5>
                </div>
                <p>File a First Notice of Loss (FNOL) or monitor ongoing damage assessments and adjuster reviews.</p>
                <is-button variant="secondary" size="sm" routerLink="/claims">
                  Open Claims Queue →
                </is-button>
              </div>
            </div>
          </div>
        }
      }
    </div>
  `,
  styles: [`
    :host {
      display: block;
      padding: 24px;
    }

    .profile-page {
      display: grid;
      gap: 24px;
      max-width: 1040px;
      margin: 0 auto;
    }

    .page-header {
      padding: 2px 0 4px;
    }

    .page-eyebrow {
      margin: 0 0 7px;
      color: var(--claret, #75013f);
      font-size: 10px;
      font-weight: 700;
      letter-spacing: 0.14em;
      text-transform: uppercase;
    }

    .title-row {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .title-row h1 {
      margin: 0;
      color: var(--ink, #000000);
      font-size: clamp(24px, 2.5vw, 32px);
      letter-spacing: -0.04em;
      font-weight: 700;
      line-height: 1.1;
    }

    .page-description {
      max-width: 780px;
      margin: 8px 0 0;
      color: var(--muted, #6f6a6d);
      font-size: 12px;
      line-height: 1.6;
    }

    .loading-state {
      display: grid;
      gap: 16px;
      padding: 20px 0;
    }

    .skeleton-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 16px;
      margin-top: 8px;
    }

    .onboarding-notice {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      padding: 18px 20px;
      background: #fff8eb;
      border: 1px solid #f9dba5;
      border-radius: 9px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
    }

    .notice-icon {
      font-size: 28px;
      line-height: 1;
      padding: 4px;
    }

    .notice-body h3 {
      margin: 0 0 4px;
      color: #92400e;
      font-size: 14px;
      font-weight: 700;
    }

    .notice-body p {
      margin: 0;
      color: #78350f;
      font-size: 12px;
      line-height: 1.55;
    }

    .error-banner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      padding: 14px 18px;
      background: #fde8e8;
      border: 1px solid #f8b4b4;
      border-radius: 8px;
      color: #9b1c1c;
      font-size: 12px;
    }

    .error-content p {
      margin: 2px 0 0;
    }

    .success-banner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      padding: 14px 18px;
      background: #eaf6f0;
      border: 1px solid #a7f3d0;
      border-radius: 8px;
      color: #065f46;
      font-size: 12px;
    }

    .success-content p {
      margin: 2px 0 0;
    }

    .profile-form {
      display: grid;
      gap: 20px;
    }

    .form-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 16px 20px;
    }

    .col-span-2 {
      grid-column: span 2;
    }

    .actions-bar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 16px;
      padding: 14px 18px;
      background: var(--surface, #ffffff);
      border: 1px solid var(--border, #eae5df);
      border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
    }

    .metadata-info {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
      font-size: 11px;
      color: var(--muted, #6f6a6d);
    }

    .customer-tag code {
      font-family: monospace;
      font-weight: 700;
      color: var(--ink, #000000);
      background: var(--warm-light, #f7f5f3);
      padding: 2px 6px;
      border-radius: 4px;
      border: 1px solid var(--border, #eae5df);
    }

    .action-buttons {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .next-steps-panel {
      margin-top: 8px;
      padding: 22px;
      background: var(--surface, #ffffff);
      border: 1px solid var(--border, #eae5df);
      border-radius: 9px;
    }

    .next-steps-panel h4 {
      margin: 0 0 16px;
      font-size: 13px;
      font-weight: 700;
      letter-spacing: 0.04em;
      text-transform: uppercase;
      color: var(--claret, #75013f);
    }

    .next-steps-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 16px;
    }

    .step-card {
      padding: 16px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 8px;
      background: var(--warm-light, #f7f5f3);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      gap: 12px;
    }

    .step-card-header {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .step-card-header h5 {
      margin: 0;
      font-size: 13px;
      font-weight: 700;
      color: var(--ink, #000000);
    }

    .step-icon {
      font-size: 18px;
    }

    .step-card p {
      margin: 0;
      font-size: 11px;
      line-height: 1.5;
      color: var(--muted, #6f6a6d);
      flex: 1;
    }

    @media (max-width: 768px) {
      .form-grid {
        grid-template-columns: 1fr;
      }
      .col-span-2 {
        grid-column: span 1;
      }
      .actions-bar {
        flex-direction: column;
        align-items: stretch;
      }
      .action-buttons {
        justify-content: flex-end;
      }
      .next-steps-grid {
        grid-template-columns: 1fr;
      }
    }
  `],
})
export class CustomerProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(CustomerProfileService);
  private readonly store = inject(Store);
  private readonly router = inject(Router);

  readonly token$ = this.store.select(selectAuthToken);
  readonly userId$ = this.store.select(selectUserId);
  readonly role$ = this.store.select(selectUserRole);

  profile: CustomerResponse | null = null;
  loading = false;
  saving = false;
  notFound = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  readonly businessTypeOptions = [
    { value: 'LIMITED_LIABILITY_COMPANY', label: 'Limited Liability Company (LLC)' },
    { value: 'CORPORATION', label: 'Corporation (C-Corp / S-Corp)' },
    { value: 'SOLE_PROPRIETORSHIP', label: 'Sole Proprietorship' },
    { value: 'PARTNERSHIP', label: 'Partnership / LLP' },
    { value: 'FREIGHT_TRANSPORTATION', label: 'Freight & Transportation' },
    { value: 'RETAIL_COMMERCE', label: 'Retail & Commerce' },
    { value: 'MANUFACTURING', label: 'Manufacturing & Distribution' },
    { value: 'PROFESSIONAL_SERVICES', label: 'Professional Services' },
    { value: 'CONSTRUCTION_CONTRACTOR', label: 'Construction & Contractor' },
    { value: 'HOSPITALITY_RESTAURANT', label: 'Hospitality & Food Services' },
    { value: 'HEALTHCARE_MEDICAL', label: 'Healthcare & Medical' },
    { value: 'TECHNOLOGY_IT', label: 'Technology & Software' },
    { value: 'OTHER', label: 'Other Business Type' },
  ];

  readonly profileForm = this.fb.nonNullable.group({
    businessName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
    ownerName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
    businessType: ['LIMITED_LIABILITY_COMPANY'],
    phone: ['', [Validators.maxLength(50)]],
    address: ['', [Validators.maxLength(255)]],
    city: ['', [Validators.maxLength(100)]],
    state: ['', [Validators.maxLength(50)]],
    country: ['USA', [Validators.maxLength(50)]],
    postalCode: ['', [Validators.maxLength(20)]],
  });

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading = true;
    this.errorMessage = null;
    this.notFound = false;

    this.profileService.getProfile().subscribe({
      next: (data) => {
        this.profile = data;
        this.loading = false;
        this.notFound = false;
        this.populateForm(data);
        if (data.customerId && typeof localStorage !== 'undefined') {
          localStorage.setItem('is_customer_id', data.customerId);
        }
      },
      error: (err) => {
        this.loading = false;
        if (err?.status === 404) {
          // Profile does not exist yet (first-time user onboarding state)
          this.notFound = true;
          this.profile = null;
        } else {
          this.errorMessage =
            err?.error?.message ||
            err?.message ||
            'Unable to load business profile. Please verify network connection.';
        }
      },
    });
  }

  populateForm(data: CustomerResponse): void {
    this.profileForm.patchValue({
      businessName: data.businessName || '',
      ownerName: data.ownerName || '',
      businessType: data.businessType || 'LIMITED_LIABILITY_COMPANY',
      phone: data.phone || '',
      address: data.address || '',
      city: data.city || '',
      state: data.state || '',
      country: data.country || 'USA',
      postalCode: data.postalCode || '',
    });
    this.profileForm.markAsPristine();
  }

  onReset(): void {
    if (this.profile) {
      this.populateForm(this.profile);
    } else {
      this.profileForm.reset({
        businessName: '',
        ownerName: '',
        businessType: 'LIMITED_LIABILITY_COMPANY',
        phone: '',
        address: '',
        city: '',
        state: '',
        country: 'USA',
        postalCode: '',
      });
    }
    this.errorMessage = null;
  }

  onSave(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = null;
    this.successMessage = null;

    const raw = this.profileForm.getRawValue();
    const payload = {
      businessName: raw.businessName.trim(),
      ownerName: raw.ownerName.trim(),
      businessType: raw.businessType || undefined,
      phone: raw.phone?.trim() || undefined,
      address: raw.address?.trim() || undefined,
      city: raw.city?.trim() || undefined,
      state: raw.state?.trim() || undefined,
      country: raw.country?.trim() || undefined,
      postalCode: raw.postalCode?.trim() || undefined,
    };

    this.profileService.updateProfile(payload).subscribe({
      next: (updated) => {
        this.saving = false;
        this.profile = updated;
        this.notFound = false;
        this.populateForm(updated);

        if (updated.customerId && typeof localStorage !== 'undefined') {
          localStorage.setItem('is_customer_id', updated.customerId);
        }

        const msg = 'Business profile updated successfully.';
        this.successMessage = msg;
        this.store.dispatch(uiActions.showToast({ message: msg, kind: 'success' }));
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage =
          err?.error?.message ||
          err?.message ||
          'Failed to update business profile. Please check entered information and try again.';
        this.store.dispatch(
          uiActions.showToast({
            message: this.errorMessage || 'Failed to update profile.',
            kind: 'error',
          })
        );
      },
    });
  }

  getFieldError(controlName: string): string | undefined {
    const ctrl = this.profileForm.get(controlName);
    if (ctrl?.invalid && (ctrl.dirty || ctrl.touched)) {
      if (ctrl.errors?.['required']) return `${this.getFieldLabel(controlName)} is required`;
      if (ctrl.errors?.['minlength']) return `${this.getFieldLabel(controlName)} must be at least 2 characters`;
      if (ctrl.errors?.['maxlength']) return `${this.getFieldLabel(controlName)} exceeds maximum length`;
    }
    return undefined;
  }

  private getFieldLabel(controlName: string): string {
    const labels: Record<string, string> = {
      businessName: 'Business name',
      ownerName: 'Owner name',
      phone: 'Phone number',
      postalCode: 'Postal code',
    };
    return labels[controlName] || controlName;
  }
}
