import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { take } from 'rxjs';

import { CustomerProfileService } from '../../core/services/customer-profile.service';
import { ApiService } from '../../core/services/api.service';
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
          <p class="page-eyebrow">{{ isEmployee ? 'Internal Staff Account' : 'Enterprise Account' }}</p>
          <div class="title-row">
            <h1>{{ isEmployee ? 'Staff Profile' : 'Business Profile' }}</h1>
            @if (isEmployee) {
              <is-badge variant="info" size="md">{{ currentUser?.role || 'Staff' }}</is-badge>
            } @else if (profile) {
              <is-badge variant="success" size="md">Profile Active</is-badge>
            } @else if (!loading && notFound) {
              <is-badge variant="warning" size="md">Incomplete Setup</is-badge>
            }
          </div>
          <p class="page-description">
            {{ isEmployee
              ? 'View your internal staff credentials, roles, system permissions, and update your sign-in password.'
              : 'Maintain your legal entity, risk classification, and principal business contact information required for commercial quoting, underwriting, and policy issuance.' }}
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
        @if (isEmployee) {
          <!-- Employee Account Overview -->
          <is-card
            title="Account Overview"
            subtitle="Signed-in identity, system roles, and account security details"
          >
            <div class="form-grid">
              <div>
                <label class="info-label">Display Name</label>
                <div class="info-value">{{ currentUser?.displayName || '—' }}</div>
              </div>
              <div>
                <label class="info-label">Email Address</label>
                <div class="info-value">{{ currentUser?.email || '—' }}</div>
              </div>
              <div>
                <label class="info-label">Assigned Role</label>
                <div class="info-value">
                  <is-badge variant="info" size="sm">{{ currentUser?.role || '—' }}</is-badge>
                </div>
              </div>
              <div>
                <label class="info-label">Account Status</label>
                <div class="info-value">
                  <is-badge variant="success" size="sm">{{ currentUser?.accountStatus || 'ACTIVE' }}</is-badge>
                </div>
              </div>
              <div class="col-span-2">
                <label class="info-label">User ID</label>
                <div class="info-value mono">{{ currentUser?.userId || '—' }}</div>
              </div>
            </div>
          </is-card>
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
              @if (reauthenticationRequired) {
                <is-button variant="secondary" size="sm" (click)="reauthenticate()">
                  Sign in again
                </is-button>
              }
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

              <div>
                <is-input
                  id="annualTurnover"
                  label="Annual Turnover (₹)"
                  type="number"
                  placeholder="e.g. 5000000"
                  formControlName="annualTurnover"
                  [disabled]="saving"
                />
              </div>

              <div>
                <is-input
                  id="employeeCount"
                  label="Employee Count"
                  type="number"
                  placeholder="e.g. 25"
                  formControlName="employeeCount"
                  [disabled]="saving"
                />
              </div>

              <div class="col-span-2">
                <is-input
                  id="phone"
                  label="Contact Phone Number"
                  type="tel"
                  placeholder="e.g. +91 98765 43210"
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
        }

        <!-- Change Password Card (Available for all authenticated users) -->
        <is-card
          title="Security & Password"
          subtitle="Update your sign-in password to keep your account secure"
        >
          @if (passwordError) {
            <div class="error-banner" role="alert" style="margin-bottom: 16px;">
              <div class="error-content">
                <strong>Password update failed:</strong>
                <p>{{ passwordError }}</p>
              </div>
            </div>
          }

          @if (passwordSuccess) {
            <div class="success-banner" role="status" style="margin-bottom: 16px;">
              <div class="success-content">
                <strong>Password updated:</strong>
                <p>{{ passwordSuccess }}</p>
              </div>
            </div>
          }

          <form [formGroup]="passwordForm" (ngSubmit)="onChangePassword()" class="profile-form">
            <div class="form-grid">
              <div class="col-span-2">
                <is-input
                  id="currentPassword"
                  label="Current Password *"
                  type="password"
                  placeholder="Enter your current password"
                  formControlName="currentPassword"
                  [error]="getPasswordFieldError('currentPassword')"
                  [disabled]="changingPassword"
                />
              </div>

              <div>
                <is-input
                  id="newPassword"
                  label="New Password *"
                  type="password"
                  placeholder="At least 8 characters"
                  formControlName="newPassword"
                  [error]="getPasswordFieldError('newPassword')"
                  [disabled]="changingPassword"
                />
              </div>

              <div>
                <is-input
                  id="confirmPassword"
                  label="Confirm New Password *"
                  type="password"
                  placeholder="Re-enter new password"
                  formControlName="confirmPassword"
                  [error]="getPasswordFieldError('confirmPassword')"
                  [disabled]="changingPassword"
                />
              </div>
            </div>

            <div class="actions-bar" style="margin-top: 8px;">
              <div class="metadata-info">
                <span>Must be at least 8 characters. Requires your current password for verification.</span>
              </div>
              <div class="action-buttons">
                <is-button
                  variant="primary"
                  type="submit"
                  [disabled]="changingPassword || passwordForm.invalid"
                >
                  {{ changingPassword ? 'Updating Password…' : 'Update Password' }}
                </is-button>
              </div>
            </div>
          </form>
        </is-card>

        <!-- Post-Save Quick Actions / Next Steps -->
        @if (!isEmployee && profile) {
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

    .info-label {
      display: block;
      color: var(--muted, #6f6a6d);
      font-size: 10px;
      font-weight: 700;
      letter-spacing: .06em;
      text-transform: uppercase;
      margin-bottom: 4px;
    }

    .info-value {
      font-size: 13px;
      color: var(--ink, #111111);
      font-weight: 500;
    }

    .info-value.mono {
      font-family: monospace;
      font-size: 11px;
      color: var(--muted, #6f6a6d);
    }
  `],
})
export class CustomerProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(CustomerProfileService);
  private readonly api = inject(ApiService);
  private readonly store = inject(Store);
  private readonly router = inject(Router);

  readonly token$ = this.store.select(selectAuthToken);
  readonly userId$ = this.store.select(selectUserId);
  readonly role$ = this.store.select(selectUserRole);

  currentUser: {
    userId?: string;
    email?: string;
    role?: string;
    displayName?: string;
    accountStatus?: string;
  } | null = null;
  isEmployee = false;

  profile: CustomerResponse | null = null;
  loading = false;
  saving = false;
  notFound = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  reauthenticationRequired = false;

  readonly passwordForm = this.fb.nonNullable.group({
    currentPassword: ['', [Validators.required]],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]],
  });
  changingPassword = false;
  passwordError: string | null = null;
  passwordSuccess: string | null = null;

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

  readonly profileForm = this.fb.group({
    businessName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
    ownerName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
    businessType: ['LIMITED_LIABILITY_COMPANY'],
    annualTurnover: [null as number | null, [Validators.min(0)]],
    employeeCount: [null as number | null, [Validators.min(0)]],
    phone: ['', [Validators.maxLength(50)]],
    address: ['', [Validators.maxLength(255)]],
    city: ['', [Validators.maxLength(100)]],
    state: ['', [Validators.maxLength(50)]],
    country: ['India', [Validators.maxLength(50)]],
    postalCode: ['', [Validators.maxLength(20)]],
  });

  ngOnInit(): void {
    this.loadUserAndProfile();
  }

  loadUserAndProfile(): void {
    this.loading = true;
    this.api.get<any>('/api/auth/me').subscribe({
      next: (user) => {
        this.currentUser = user;
        const role = (user?.role || '').toUpperCase();
        this.isEmployee = role !== 'POLICYHOLDER' && role !== 'USER' && !!role;
        if (this.isEmployee) {
          this.loading = false;
        } else {
          this.loadProfile();
        }
      },
      error: () => {
        this.role$.pipe(take(1)).subscribe((role) => {
          const upper = (role || '').toUpperCase();
          this.isEmployee = upper !== 'POLICYHOLDER' && upper !== 'USER' && !!upper;
          if (this.isEmployee) {
            this.loading = false;
          } else {
            this.loadProfile();
          }
        });
      }
    });
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
      annualTurnover: data.annualTurnover ?? null,
      employeeCount: data.employeeCount ?? null,
      phone: data.phone || '',
      address: data.address || '',
      city: data.city || '',
      state: data.state || '',
      country: data.country || 'India',
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
        annualTurnover: null,
        employeeCount: null,
        phone: '',
        address: '',
        city: '',
        state: '',
        country: 'India',
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
    this.reauthenticationRequired = false;

    const profileWasMissing = !this.profile;

    const raw = this.profileForm.getRawValue();
    const payload = {
      businessName: (raw.businessName || '').trim(),
      ownerName: (raw.ownerName || '').trim(),
      businessType: raw.businessType || undefined,
      annualTurnover: raw.annualTurnover != null && (raw.annualTurnover as any) !== '' ? Number(raw.annualTurnover) : undefined,
      employeeCount: raw.employeeCount != null && (raw.employeeCount as any) !== '' ? Number(raw.employeeCount) : undefined,
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

        // Seamless token refresh so JWT contains the new customerId immediately:
        this.api.post<any>('/api/auth/refresh', {}).subscribe({
          next: (refreshed) => {
            const token = refreshed?.accessToken ?? refreshed?.token;
            if (token && typeof localStorage !== 'undefined') {
              localStorage.setItem('is_token', token);
              if (refreshed.role) localStorage.setItem('is_role', refreshed.role);
              if (refreshed.userId) localStorage.setItem('is_user_id', refreshed.userId);
              if (refreshed.customerId) localStorage.setItem('is_customer_id', refreshed.customerId);
            }
            this.store.dispatch(authActions.loginSuccess({ response: refreshed }));
          },
          error: () => {
            // Non-blocking fallback; local storage is_customer_id is already set
          }
        });

        const msg = profileWasMissing
          ? 'Business profile created successfully. Your account is now fully active for commercial quoting and policy services.'
          : 'Business profile updated successfully.';
        this.successMessage = msg;
        this.reauthenticationRequired = false;
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

  reauthenticate(): void {
    this.store.dispatch(authActions.logout());
    this.router.navigate(['/auth']);
  }

  onChangePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    const { currentPassword, newPassword, confirmPassword } = this.passwordForm.getRawValue();
    if (newPassword !== confirmPassword) {
      this.passwordError = 'New password and confirm password do not match.';
      return;
    }
    if (newPassword.length < 8) {
      this.passwordError = 'New password must be at least 8 characters long.';
      return;
    }

    this.changingPassword = true;
    this.passwordError = null;
    this.passwordSuccess = null;

    this.api.post('/api/auth/change-password', {
      currentPassword,
      newPassword,
    }).subscribe({
      next: () => {
        this.changingPassword = false;
        this.passwordSuccess = 'Password updated successfully.';
        this.passwordForm.reset();
        this.store.dispatch(uiActions.showToast({ message: 'Password updated successfully.', kind: 'success' }));
      },
      error: (err) => {
        this.changingPassword = false;
        this.passwordError = err?.error?.message || err?.message || 'Failed to update password. Please check your current password and try again.';
        this.store.dispatch(uiActions.showToast({ message: this.passwordError || 'Password change failed.', kind: 'error' }));
      }
    });
  }

  getPasswordFieldError(field: string): string | undefined {
    const ctrl = this.passwordForm.get(field);
    if (ctrl?.invalid && (ctrl.dirty || ctrl.touched)) {
      if (ctrl.errors?.['required']) {
        if (field === 'currentPassword') return 'Current password is required';
        if (field === 'newPassword') return 'New password is required';
        if (field === 'confirmPassword') return 'Please confirm your new password';
      }
      if (ctrl.errors?.['minlength']) return 'Password must be at least 8 characters';
    }
    return undefined;
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
