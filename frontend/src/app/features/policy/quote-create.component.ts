import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';

import { QuoteService } from '../../core/services/quote.service';
import { CustomerProfileService } from '../../core/services/customer-profile.service';
import { CreateQuoteRequest } from '../../core/models/quote.models';
import { CustomerResponse } from '../../core/models/customer.models';
import { uiActions } from '../../core/store/ui/ui.actions';

// ui-core components
import {
  CardComponent,
  InputComponent,
  SelectComponent,
  ButtonComponent,
  SkeletonComponent,
  BadgeComponent,
} from 'ui-core';

interface ProductPreset {
  code: string;
  name: string;
  coverages: Array<{ code: string; name: string; defaultLimit: number; defaultDeductible: number }>;
}

@Component({
  selector: 'is-quote-create',
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
    <div class="quote-create-page">
      <header class="page-header">
        <a routerLink="/policy" class="back-link">&larr; Back to quotes & policies</a>
        <div class="title-row">
          <h1>Request Commercial Insurance Quote</h1>
        </div>
        <p class="page-description">
          Provide your enterprise risk details and customize coverage limits for underwriter review and automated pricing.
        </p>
      </header>

      @if (loadingProfile) {
        <div class="loading-state" role="status" aria-live="polite">
          <is-skeleton variant="text" width="50%" height="28px" />
          <is-skeleton variant="card" />
        </div>
      } @else if (!customerId) {
        <!-- Blocked if Customer Profile does not exist -->
        <div class="notice-card warning">
          <div class="notice-icon" aria-hidden="true">⚠️</div>
          <div class="notice-content">
            <h3>Business Profile Required Before Quoting</h3>
            <p>
              Underwriting rules require a verified commercial legal entity and business address before a quote can be drafted.
            </p>
          </div>
          <is-button variant="primary" size="sm" routerLink="/profile">
            Setup Business Profile &rarr;
          </is-button>
        </div>
      } @else {
        <!-- Stepper Navigation Indicator Bar -->
        <nav class="stepper-bar" aria-label="Quote steps">
          @for (step of steps; track step.label; let i = $index) {
            <div
              class="step-item"
              [class.active]="i === currentStep"
              [class.complete]="i < currentStep"
              (click)="onStepClick(i)"
              role="button"
              [attr.tabindex]="i <= currentStep ? 0 : -1"
            >
              <div class="step-indicator">
                @if (i < currentStep) {
                  <span class="step-check" aria-hidden="true">✓</span>
                } @else {
                  <span class="step-num">{{ i + 1 }}</span>
                }
              </div>
              <div class="step-meta">
                <span class="step-label">{{ step.label }}</span>
                <span class="step-desc">{{ step.description }}</span>
              </div>
            </div>
            @if (i < steps.length - 1) {
              <div class="step-divider" [class.complete]="i < currentStep"></div>
            }
          }
        </nav>

        <is-card title="Commercial Quote Application" [subtitle]="steps[currentStep].description">
          @if (errorMessage) {
            <div class="error-banner" role="alert">
              <strong>Error creating quote:</strong>
              <p>{{ errorMessage }}</p>
            </div>
          }

          <form [formGroup]="form" class="step-container">
            <!-- STEP 1: Risk & Business Operations -->
            @if (currentStep === 0) {
              <div class="step-pane">
                @if (profile) {
                  <div class="profile-summary-box">
                    <div class="profile-summary-header">
                      <strong>Applicant Entity: {{ profile.businessName }}</strong>
                      <is-badge variant="success" size="sm">Verified Business Profile</is-badge>
                    </div>
                    <p class="profile-summary-meta">
                      Principal: {{ profile.ownerName }} · Classification: {{ profile.businessType || 'Commercial Entity' }} · Location: {{ profile.city }}, {{ profile.state }}
                    </p>
                  </div>
                }

                <div class="form-grid">
                  <div class="col-span-2">
                    <is-select
                      id="productCode"
                      label="Insurance Product Line *"
                      placeholder="Select product"
                      [options]="productOptions"
                      formControlName="productCode"
                      (change)="onProductChange()"
                      [error]="getFieldError('productCode')"
                    />
                  </div>

                  <div class="col-span-2">
                    <is-input
                      id="requestedEffectiveDate"
                      label="Requested Effective Date *"
                      type="date"
                      formControlName="requestedEffectiveDate"
                      [error]="getFieldError('requestedEffectiveDate')"
                    />
                  </div>

                  <div class="col-span-2 form-group">
                    <label class="field-label" for="insuranceNeed">Coverage Objective & Insurance Need *</label>
                    <textarea
                      id="insuranceNeed"
                      class="custom-textarea"
                      rows="3"
                      placeholder="Describe primary coverage priorities (e.g. Building and structural loss, equipment breakdown, general liability protection)..."
                      formControlName="insuranceNeed"
                    ></textarea>
                    @if (getFieldError('insuranceNeed')) {
                      <span class="field-error">{{ getFieldError('insuranceNeed') }}</span>
                    }
                  </div>

                  <div class="col-span-2 form-group">
                    <label class="field-label" for="businessOperations">Business Operations Description *</label>
                    <textarea
                      id="businessOperations"
                      class="custom-textarea"
                      rows="3"
                      placeholder="Detail physical locations, daily activities, hours of operation, customer volume, and safety controls..."
                      formControlName="businessOperations"
                    ></textarea>
                    @if (getFieldError('businessOperations')) {
                      <span class="field-error">{{ getFieldError('businessOperations') }}</span>
                    }
                  </div>
                </div>
              </div>
            }

            <!-- STEP 2: Coverages & Limits Configuration -->
            @if (currentStep === 1) {
              <div class="step-pane">
                <div class="coverages-header">
                  <div>
                    <h4>Coverages & Deductibles</h4>
                    <p class="section-subtext">Configure requested policy limits for each specific line of insurance.</p>
                  </div>
                  <is-button variant="secondary" size="sm" type="button" (click)="addCoverage()">
                    + Add Coverage Line
                  </is-button>
                </div>

                <div formArrayName="coverages" class="coverages-list">
                  @for (covGroup of coveragesArray.controls; track $index) {
                    <div [formGroupName]="$index" class="coverage-row-card">
                      <div class="coverage-row-header">
                        <span class="coverage-index">Coverage Line #{{ $index + 1 }}</span>
                        @if (coveragesArray.length > 1) {
                          <button
                            type="button"
                            class="remove-btn"
                            (click)="removeCoverage($index)"
                            aria-label="Remove coverage"
                          >
                            &times; Remove
                          </button>
                        }
                      </div>

                      <div class="form-grid">
                        <div>
                          <is-input
                            label="Coverage Code *"
                            placeholder="e.g. BLDG"
                            formControlName="coverageCode"
                            [error]="getCovError($index, 'coverageCode')"
                          />
                        </div>

                        <div>
                          <is-input
                            label="Coverage Name *"
                            placeholder="e.g. Building Property"
                            formControlName="coverageName"
                            [error]="getCovError($index, 'coverageName')"
                          />
                        </div>

                        <div>
                          <is-input
                            label="Requested Limit ($) *"
                            type="number"
                            placeholder="e.g. 500000"
                            formControlName="requestedLimit"
                            [error]="getCovError($index, 'requestedLimit')"
                          />
                        </div>

                        <div>
                          <is-input
                            label="Requested Deductible ($) *"
                            type="number"
                            placeholder="e.g. 5000"
                            formControlName="requestedDeductible"
                            [error]="getCovError($index, 'requestedDeductible')"
                          />
                        </div>
                      </div>
                    </div>
                  }
                </div>
              </div>
            }

            <!-- STEP 3: Review & Generate Draft -->
            @if (currentStep === 2) {
              <div class="step-pane">
                <h4>Review Quote Application</h4>
                <p class="review-intro">
                  Verify the commercial risk information before generating your draft quote. Once created, you can submit it to the underwriting desk.
                </p>

                <div class="review-summary-grid">
                  <div class="summary-item">
                    <span class="summary-label">Applicant Entity</span>
                    <strong class="summary-value">{{ profile?.businessName }}</strong>
                  </div>

                  <div class="summary-item">
                    <span class="summary-label">Product Line</span>
                    <strong class="summary-value">{{ form.get('productCode')?.value }}</strong>
                  </div>

                  <div class="summary-item">
                    <span class="summary-label">Requested Effective Date</span>
                    <strong class="summary-value">{{ form.get('requestedEffectiveDate')?.value }}</strong>
                  </div>

                  <div class="summary-item">
                    <span class="summary-label">Customer ID</span>
                    <span class="code-sm">{{ customerId }}</span>
                  </div>

                  <div class="summary-item col-span-2">
                    <span class="summary-label">Insurance Need</span>
                    <p class="summary-text">{{ form.get('insuranceNeed')?.value }}</p>
                  </div>

                  <div class="summary-item col-span-2">
                    <span class="summary-label">Business Operations</span>
                    <p class="summary-text">{{ form.get('businessOperations')?.value }}</p>
                  </div>
                </div>

                <h5 class="sub-heading">Configured Coverages ({{ coveragesArray.length }})</h5>
                <div class="table-container">
                  <table class="review-table">
                    <thead>
                      <tr>
                        <th>Code</th>
                        <th>Coverage Name</th>
                        <th class="text-right">Requested Limit</th>
                        <th class="text-right">Deductible</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (cov of coveragesArray.value; track $index) {
                        <tr>
                          <td><code>{{ cov.coverageCode }}</code></td>
                          <td><strong>{{ cov.coverageName }}</strong></td>
                          <td class="text-right">
                            <strong class="text-emerald">\${{ cov.requestedLimit | number:'1.0-0' }}</strong>
                          </td>
                          <td class="text-right">
                            \${{ cov.requestedDeductible | number:'1.0-0' }}
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              </div>
            }

            <!-- Step Navigation Buttons -->
            <div class="stepper-actions-bar">
              <is-button
                variant="secondary"
                type="button"
                (click)="onPrevStep()"
                [disabled]="currentStep === 0 || submitting"
              >
                &larr; Previous Step
              </is-button>

              @if (currentStep < steps.length - 1) {
                <is-button
                  variant="primary"
                  type="button"
                  (click)="onNextStep()"
                >
                  Continue to {{ steps[currentStep + 1].label }} &rarr;
                </is-button>
              } @else {
                <is-button
                  variant="primary"
                  type="button"
                  (click)="onSubmitQuote()"
                  [disabled]="submitting || form.invalid"
                >
                  {{ submitting ? 'Generating Draft Quote…' : 'Generate Draft Quote ✓' }}
                </is-button>
              }
            </div>
          </form>
        </is-card>
      }
    </div>
  `,
  styles: [`
    :host {
      display: block;
      padding: 24px;
    }

    .quote-create-page {
      display: grid;
      gap: 20px;
      max-width: 1040px;
      margin: 0 auto;
    }

    .page-header {
      padding: 2px 0 4px;
    }

    .back-link {
      display: inline-block;
      font-size: 11px;
      font-weight: 600;
      color: var(--claret, #75013f);
      text-decoration: none;
      margin-bottom: 8px;
    }

    .back-link:hover {
      text-decoration: underline;
    }

    .title-row h1 {
      margin: 0;
      font-size: clamp(22px, 2.4vw, 30px);
      font-weight: 700;
      color: var(--ink, #000000);
      letter-spacing: -0.03em;
    }

    .page-description {
      margin: 6px 0 0;
      font-size: 12px;
      color: var(--muted, #6f6a6d);
      max-width: 720px;
      line-height: 1.5;
    }

    /* Stepper Bar */
    .stepper-bar {
      display: flex;
      align-items: center;
      gap: 12px;
      background: var(--surface, #ffffff);
      padding: 14px 20px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 9px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
      overflow-x: auto;
    }

    .step-item {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      user-select: none;
      flex-shrink: 0;
    }

    .step-indicator {
      width: 26px;
      height: 26px;
      border-radius: 50%;
      border: 2px solid var(--border, #eae5df);
      display: grid;
      place-items: center;
      font-size: 11px;
      font-weight: 700;
      color: var(--muted, #6f6a6d);
      background: var(--warm-light, #f7f5f3);
      transition: all 0.15s ease;
    }

    .step-item.active .step-indicator {
      border-color: var(--claret, #75013f);
      background: var(--claret, #75013f);
      color: #ffffff;
    }

    .step-item.complete .step-indicator {
      border-color: #10b981;
      background: #10b981;
      color: #ffffff;
    }

    .step-meta {
      display: flex;
      flex-direction: column;
    }

    .step-label {
      font-size: 12px;
      font-weight: 600;
      color: var(--ink, #000000);
    }

    .step-item.active .step-label {
      color: var(--claret, #75013f);
    }

    .step-desc {
      font-size: 10px;
      color: var(--muted, #6f6a6d);
    }

    .step-divider {
      flex: 1;
      min-width: 24px;
      height: 2px;
      background: var(--border, #eae5df);
    }

    .step-divider.complete {
      background: #10b981;
    }

    .step-pane {
      display: grid;
      gap: 20px;
    }

    .profile-summary-box {
      padding: 14px 16px;
      background: var(--warm-light, #f7f5f3);
      border: 1px solid var(--border, #eae5df);
      border-radius: 8px;
    }

    .profile-summary-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-bottom: 4px;
    }

    .profile-summary-header strong {
      font-size: 13px;
      color: var(--ink, #000000);
    }

    .profile-summary-meta {
      margin: 0;
      font-size: 11px;
      color: var(--muted, #6f6a6d);
    }

    .form-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 16px 20px;
    }

    .col-span-2 {
      grid-column: span 2;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .field-label {
      color: #393536;
      font-size: 10px;
      font-weight: 600;
    }

    .custom-textarea {
      width: 100%;
      padding: 9px 12px;
      border: 1px solid var(--warm, #eae5df);
      border-radius: 6px;
      background: var(--surface, #ffffff);
      font-size: 12px;
      font-family: inherit;
      color: var(--ink, #000000);
      outline: none;
      box-sizing: border-box;
      resize: vertical;
    }

    .custom-textarea:focus {
      border-color: var(--claret, #75013f);
      box-shadow: 0 0 0 3px rgba(117, 1, 63, 0.12);
    }

    .field-error {
      color: #e11d48;
      font-size: 9px;
    }

    .coverages-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 16px;
      margin-bottom: 14px;
    }

    .coverages-header h4 {
      margin: 0;
      font-size: 14px;
      font-weight: 700;
      color: var(--ink, #000000);
    }

    .section-subtext {
      margin: 2px 0 0;
      font-size: 11px;
      color: var(--muted, #6f6a6d);
    }

    .coverages-list {
      display: grid;
      gap: 16px;
    }

    .coverage-row-card {
      padding: 16px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 8px;
      background: var(--warm-light, #f7f5f3);
    }

    .coverage-row-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }

    .coverage-index {
      font-size: 11px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--claret, #75013f);
    }

    .remove-btn {
      background: none;
      border: none;
      color: #e11d48;
      font-size: 11px;
      font-weight: 600;
      cursor: pointer;
    }

    .review-intro {
      margin: 0 0 16px;
      font-size: 12px;
      color: var(--muted, #6f6a6d);
    }

    .review-summary-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 14px;
      background: var(--warm-light, #f7f5f3);
      padding: 16px;
      border-radius: 8px;
      border: 1px solid var(--border, #eae5df);
      margin-bottom: 20px;
    }

    .summary-label {
      display: block;
      font-size: 9px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--muted, #6f6a6d);
      margin-bottom: 2px;
    }

    .summary-value {
      font-size: 12px;
      color: var(--ink, #000000);
    }

    .summary-text {
      margin: 0;
      font-size: 11px;
      color: var(--ink, #000000);
      line-height: 1.5;
    }

    .sub-heading {
      margin: 16px 0 10px;
      font-size: 12px;
      font-weight: 700;
      color: var(--ink, #000000);
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    .review-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 12px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 6px;
      overflow: hidden;
    }

    .review-table th {
      background: #ffffff;
      padding: 8px 12px;
      text-align: left;
      font-size: 10px;
      font-weight: 700;
      color: var(--muted, #6f6a6d);
      border-bottom: 1px solid var(--border, #eae5df);
      text-transform: uppercase;
    }

    .review-table td {
      padding: 10px 12px;
      border-bottom: 1px solid var(--border, #eae5df);
      background: #ffffff;
    }

    .text-right {
      text-align: right;
    }

    .text-emerald {
      color: #059669;
    }

    .code-sm {
      font-family: monospace;
      font-size: 11px;
    }

    .stepper-actions-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      padding-top: 20px;
      border-top: 1px solid var(--border, #eae5df);
      margin-top: 24px;
    }

    .notice-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 18px 22px;
      border-radius: 9px;
      background: #fff8eb;
      border: 1px solid #f9dba5;
    }

    .notice-icon {
      font-size: 26px;
    }

    .notice-content h3 {
      margin: 0 0 2px;
      font-size: 14px;
      font-weight: 700;
      color: #92400e;
    }

    .notice-content p {
      margin: 0;
      font-size: 12px;
      color: #78350f;
    }

    .error-banner {
      padding: 12px 16px;
      background: #fde8e8;
      border: 1px solid #f8b4b4;
      border-radius: 6px;
      color: #9b1c1c;
      font-size: 11px;
      margin-bottom: 16px;
    }

    .error-banner p {
      margin: 2px 0 0;
    }

    @media (max-width: 640px) {
      .form-grid {
        grid-template-columns: 1fr;
      }
      .col-span-2 {
        grid-column: span 1;
      }
      .review-summary-grid {
        grid-template-columns: 1fr;
      }
      .stepper-bar {
        padding: 10px;
      }
    }
  `],
})
export class QuoteCreateComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly quoteService = inject(QuoteService);
  private readonly profileService = inject(CustomerProfileService);
  private readonly store = inject(Store);
  private readonly router = inject(Router);

  customerId: string | null = null;
  profile: CustomerResponse | null = null;
  loadingProfile = true;
  submitting = false;
  errorMessage: string | null = null;
  currentStep = 0;

  readonly steps = [
    { label: 'Risk & Operations', description: 'Product and commercial operations' },
    { label: 'Coverages & Limits', description: 'Limits, deductibles and schedules' },
    { label: 'Review & Draft', description: 'Verify application and draft quote' },
  ];

  readonly productPresets: Record<string, ProductPreset> = {
    COMMERCIAL_PROPERTY: {
      code: 'COMMERCIAL_PROPERTY',
      name: 'Commercial Property Insurance',
      coverages: [
        { code: 'BLDG', name: 'Building & Structural Property', defaultLimit: 1000000, defaultDeductible: 5000 },
        { code: 'BPP', name: 'Business Personal Property', defaultLimit: 250000, defaultDeductible: 2500 },
      ],
    },
    GENERAL_LIABILITY: {
      code: 'GENERAL_LIABILITY',
      name: 'Commercial General Liability (CGL)',
      coverages: [
        { code: 'GL_OCCURRENCE', name: 'General Liability (Each Occurrence)', defaultLimit: 1000000, defaultDeductible: 1000 },
        { code: 'GL_AGGREGATE', name: 'General Liability (General Aggregate)', defaultLimit: 2000000, defaultDeductible: 1000 },
      ],
    },
    BUSINESS_OWNERS_POLICY: {
      code: 'BUSINESS_OWNERS_POLICY',
      name: "Business Owner's Policy (BOP Package)",
      coverages: [
        { code: 'BLDG', name: 'Building Property', defaultLimit: 750000, defaultDeductible: 5000 },
        { code: 'BPP', name: 'Business Contents & Inventory', defaultLimit: 200000, defaultDeductible: 2500 },
        { code: 'GL_OCCURRENCE', name: 'Commercial General Liability', defaultLimit: 1000000, defaultDeductible: 1000 },
        { code: 'BI', name: 'Business Interruption Loss', defaultLimit: 150000, defaultDeductible: 0 },
      ],
    },
    COMMERCIAL_AUTO: {
      code: 'COMMERCIAL_AUTO',
      name: 'Commercial Fleet Auto',
      coverages: [
        { code: 'AUTO_LIABILITY', name: 'Commercial Auto Liability', defaultLimit: 1000000, defaultDeductible: 1000 },
        { code: 'AUTO_COLLISION', name: 'Comprehensive & Collision', defaultLimit: 100000, defaultDeductible: 1000 },
      ],
    },
  };

  readonly productOptions = [
    { value: 'COMMERCIAL_PROPERTY', label: 'Commercial Property Insurance' },
    { value: 'GENERAL_LIABILITY', label: 'Commercial General Liability (CGL)' },
    { value: 'BUSINESS_OWNERS_POLICY', label: "Business Owner's Policy (BOP Package)" },
    { value: 'COMMERCIAL_AUTO', label: 'Commercial Fleet Auto' },
  ];

  readonly form = this.fb.nonNullable.group({
    productCode: ['COMMERCIAL_PROPERTY', [Validators.required]],
    requestedEffectiveDate: [new Date().toISOString().slice(0, 10), [Validators.required]],
    insuranceNeed: ['', [Validators.required, Validators.minLength(5)]],
    businessOperations: ['', [Validators.required, Validators.minLength(5)]],
    coverages: this.fb.array([]),
  });

  get coveragesArray(): FormArray {
    return this.form.get('coverages') as FormArray;
  }

  ngOnInit(): void {
    this.initCustomerProfile();
    this.applyPreset('COMMERCIAL_PROPERTY');
  }

  private initCustomerProfile(): void {
    this.loadingProfile = true;
    this.customerId = typeof localStorage !== 'undefined' ? localStorage.getItem('is_customer_id') : null;

    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.loadingProfile = false;
        if (profile) {
          this.profile = profile;
          if (profile.customerId) {
            this.customerId = profile.customerId;
            if (typeof localStorage !== 'undefined') {
              localStorage.setItem('is_customer_id', profile.customerId);
            }
          }
          if (profile.businessName && !this.form.get('businessOperations')?.value) {
            this.form.patchValue({
              businessOperations: `${profile.businessName} operating in ${profile.businessType || 'commercial operations'}. Principal contact: ${profile.ownerName}, ${profile.city || ''} ${profile.state || ''}.`,
              insuranceNeed: `Commercial insurance coverage for facilities, contents, and active operations of ${profile.businessName}.`,
            });
          }
        }
      },
      error: () => {
        this.loadingProfile = false;
      },
    });
  }

  onProductChange(): void {
    const selected = this.form.get('productCode')?.value;
    if (selected && this.productPresets[selected]) {
      this.applyPreset(selected);
    }
  }

  applyPreset(productCode: string): void {
    const preset = this.productPresets[productCode];
    if (!preset) return;

    this.coveragesArray.clear();
    for (const cov of preset.coverages) {
      this.coveragesArray.push(
        this.fb.group({
          coverageCode: [cov.code, [Validators.required]],
          coverageName: [cov.name, [Validators.required]],
          requestedLimit: [cov.defaultLimit, [Validators.required, Validators.min(1)]],
          requestedDeductible: [cov.defaultDeductible, [Validators.required, Validators.min(0)]],
          waitingPeriodDays: [0],
        })
      );
    }
  }

  addCoverage(): void {
    this.coveragesArray.push(
      this.fb.group({
        coverageCode: ['', [Validators.required]],
        coverageName: ['', [Validators.required]],
        requestedLimit: [100000, [Validators.required, Validators.min(1)]],
        requestedDeductible: [1000, [Validators.required, Validators.min(0)]],
        waitingPeriodDays: [0],
      })
    );
  }

  removeCoverage(index: number): void {
    if (this.coveragesArray.length > 1) {
      this.coveragesArray.removeAt(index);
    }
  }

  onStepClick(index: number): void {
    if (index < this.currentStep) {
      this.currentStep = index;
    } else if (index === this.currentStep + 1) {
      this.onNextStep();
    }
  }

  onNextStep(): void {
    if (this.currentStep === 0) {
      const controls = ['productCode', 'requestedEffectiveDate', 'insuranceNeed', 'businessOperations'];
      for (const ctrl of controls) {
        this.form.get(ctrl)?.markAsTouched();
      }
      if (
        this.form.get('productCode')?.invalid ||
        this.form.get('requestedEffectiveDate')?.invalid ||
        this.form.get('insuranceNeed')?.invalid ||
        this.form.get('businessOperations')?.invalid
      ) {
        return;
      }
    } else if (this.currentStep === 1) {
      if (this.coveragesArray.invalid || this.coveragesArray.length === 0) {
        this.coveragesArray.markAllAsTouched();
        return;
      }
    }

    if (this.currentStep < this.steps.length - 1) {
      this.currentStep++;
    }
  }

  onPrevStep(): void {
    if (this.currentStep > 0) {
      this.currentStep--;
    }
  }

  onSubmitQuote(): void {
    if (this.form.invalid || !this.customerId) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = null;

    const raw = this.form.getRawValue();
    const payload: CreateQuoteRequest = {
      customerId: this.customerId,
      productCode: raw.productCode,
      insuranceNeed: raw.insuranceNeed.trim(),
      businessOperations: raw.businessOperations.trim(),
      requestedEffectiveDate: raw.requestedEffectiveDate,
      coverages: raw.coverages.map((c: any) => ({
        coverageCode: c.coverageCode.trim(),
        coverageName: c.coverageName.trim(),
        requestedLimit: Number(c.requestedLimit),
        requestedDeductible: Number(c.requestedDeductible),
        waitingPeriodDays: c.waitingPeriodDays ? Number(c.waitingPeriodDays) : undefined,
      })),
    };

    this.quoteService.createDraftQuote(payload).subscribe({
      next: (created) => {
        this.submitting = false;
        this.store.dispatch(
          uiActions.showToast({
            message: `Draft quote ${created.quoteNumber} created successfully.`,
            kind: 'success',
          })
        );
        this.router.navigate(['/policy/quotes', created.quoteId]);
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage =
          err?.error?.message ||
          err?.message ||
          'Failed to create quote. Please verify that your Business Profile has been completed and that your session is valid.';
        this.store.dispatch(
          uiActions.showToast({
            message: this.errorMessage || 'Failed to create quote.',
            kind: 'error',
          })
        );
      },
    });
  }

  getFieldError(name: string): string | undefined {
    const ctrl = this.form.get(name);
    if (ctrl?.invalid && (ctrl.dirty || ctrl.touched)) {
      if (ctrl.errors?.['required']) return 'This field is required';
      if (ctrl.errors?.['minlength']) return 'Must be at least 5 characters';
    }
    return undefined;
  }

  getCovError(index: number, field: string): string | undefined {
    const ctrl = this.coveragesArray.at(index)?.get(field);
    if (ctrl?.invalid && (ctrl.dirty || ctrl.touched)) {
      if (ctrl.errors?.['required']) return 'Required';
      if (ctrl.errors?.['min']) return 'Must be greater than 0';
    }
    return undefined;
  }
}
