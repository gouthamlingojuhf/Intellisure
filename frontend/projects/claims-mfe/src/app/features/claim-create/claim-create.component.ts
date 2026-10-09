import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClaimsService } from '../../services/claims.service';
import { PolicyService } from '../../services/policy.service';
import { PolicyOption } from '../../models/policy.models';

import { CardComponent, ButtonComponent, InputComponent, SelectComponent, TextareaComponent, StepperComponent, StepperStep } from 'ui-core';

@Component({
  selector: 'claims-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule, 
    CardComponent,
    ButtonComponent,
    InputComponent,
    SelectComponent,
    TextareaComponent,
    StepperComponent
  ],
  template: `
    <div class="form-workflow">
      <is-card title="File a new claim (FNOL)" subtitle="Submit first notice of loss to initialize automated claims processing and adjuster assignment.">
        @if (error) {
          <div class="error-banner" role="alert">
            <strong>Submission error:</strong>
            <p>{{ error }}</p>
          </div>
        }

        <is-stepper
          [steps]="steps"
          [currentStep]="currentStep"
          [orientation]="'horizontal'"
          [submitLabel]="submitting ? 'Submitting…' : 'Submit claim'"
          (currentStepChange)="currentStep = $event"
          (next)="onNext()"
          (previous)="onPrevious()"
          (submit)="onSubmit()"
        >
          <ng-template #stepContent>
            @switch (currentStep) {
              @case (0) { <ng-container *ngTemplateOutlet="policySection" /> }
              @case (1) { <ng-container *ngTemplateOutlet="detailsSection" /> }
              @case (2) { <ng-container *ngTemplateOutlet="reviewSection" /> }
            }
          </ng-template>
        </is-stepper>
      </is-card>
    </div>

    <ng-template #policySection>
      <div class="form-grid" [formGroup]="claimForm">
        <div class="md:col-span-2">
          <is-select
            label="Policy number"
            placeholder="Select the policy for this claim"
            [options]="policyOptions"
            formControlName="policyId"
            [error]="getError('policyId')"
          />
          @if (policiesLoading) {
            <small class="field-hint">Loading your eligible policies…</small>
          } @else if (policyLookupError) {
            <small class="field-error">{{ policyLookupError }}</small>
          } @else if (!policyOptions.length) {
            <small class="field-hint">No eligible policies were found for this account.</small>
          }
        </div>
        <is-input
          label="Incident date"
          type="date"
          formControlName="incidentDate"
          [error]="getError('incidentDate')"
        />
        <is-input
          label="Estimated loss (USD)"
          type="number"
          placeholder="0.00"
          formControlName="estimatedLoss"
          [error]="getError('estimatedLoss')"
        />
      </div>
    </ng-template>

    <ng-template #detailsSection>
      <div class="form-grid" [formGroup]="claimForm">
        <div class="md:col-span-2">
          <is-textarea
            label="Incident description"
            placeholder="Describe the incident, damage details, and circumstances..."
            formControlName="description"
            [rows]="5"
            [error]="getError('description')"
          />
        </div>
      </div>
    </ng-template>

    <ng-template #reviewSection>
      <div class="review-summary">
        <h4>Review your submission</h4>
        <dl class="review-grid">
          <dt>Policy number</dt>
          <dd>{{ selectedPolicy?.policyNumber || 'Not selected' }}</dd>
          <dt>Incident date</dt>
          <dd>{{ claimForm.get('incidentDate')?.value }}</dd>
          <dt>Estimated loss</dt>
          <dd>{{ claimForm.get('estimatedLoss')?.value | currency:'INR':'symbol':'1.0-0' }}</dd>
          <dt>Description</dt>
          <dd>{{ claimForm.get('description')?.value }}</dd>
        </dl>
      </div>
    </ng-template>
  `,
  styles: [`
    :host {
      display: block;
      padding: 24px;
    }
    
    .form-workflow {
      display: grid;
      gap: 18px;
      max-width: 800px;
      margin: 0 auto;
    }

    .error-banner {
      margin-bottom: 16px;
      padding: 12px 16px;
      background: #fde8e8;
      border: 1px solid #f8b4b4;
      border-radius: 8px;
      color: #9b1c1c;
      font-size: 12px;
    }
    .error-banner p {
      margin: 2px 0 0;
    }
    
    .form-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 16px;
      padding: 20px 0;
    }
    
    .form-grid > .md\\:col-span-2 {
      grid-column: span 2;
    }
    
    .review-summary {
      padding: 20px 0;
    }
    
    .review-summary h4 {
      margin: 0 0 16px;
      font-size: 14px;
      font-weight: 600;
      color: var(--ink, #000000);
    }
    
    .review-grid {
      display: grid;
      grid-template-columns: 180px 1fr;
      gap: 8px 16px;
      font-size: 12px;
    }
    
    .review-grid dt {
      color: var(--muted, #6f6a6d);
      font-weight: 500;
    }
    
    .review-grid dd {
      margin: 0;
      color: var(--ink, #000000);
      font-weight: 500;
      word-break: break-all;
    }
    
    @media (max-width: 700px) {
      .form-grid {
        grid-template-columns: 1fr;
      }
      .form-grid > .md\\:col-span-2 {
        grid-column: span 1;
      }
      .review-grid {
        grid-template-columns: 1fr;
      }
    }
  `],
})
export class ClaimCreateComponent {
  private readonly fb = new FormBuilder();
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly claimsService = inject(ClaimsService);
  private readonly policyService = inject(PolicyService);

  readonly steps: StepperStep[] = [
    { label: 'Policy & Loss', description: 'Select a policy and incident date' },
    { label: 'Incident Details', description: 'Describe the damage' },
    { label: 'Review & Submit', description: 'Confirm FNOL submission' },
  ];

  currentStep = 0;
  submitting = false;
  error: string | null = null;
  policyOptions: Array<{ value: string; label: string }> = [];
  selectedPolicy: PolicyOption | null = null;
  policiesLoading = false;
  policyLookupError: string | null = null;

  readonly claimForm = this.fb.nonNullable.group({
    policyId: [
      '',
      [
        Validators.required,
        Validators.pattern(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i),
      ],
    ],
    incidentDate: [new Date().toISOString().slice(0, 10), Validators.required],
    estimatedLoss: [0, [Validators.required, Validators.min(0)]],
    description: ['', [Validators.required, Validators.minLength(5)]],
  });

  constructor() {
    const policyId = this.route.snapshot.queryParamMap.get('policyId');
    if (policyId) {
      this.claimForm.controls.policyId.setValue(policyId);
    }
    this.loadPolicies(policyId);
  }

  getError(controlName: string): string | undefined {
    const control = this.claimForm.get(controlName);
    if (control?.invalid && (control.dirty || control.touched)) {
      if (control.errors?.['required']) return `${this.getFieldLabel(controlName)} is required`;
      if (control.errors?.['min']) return 'Amount must be greater than or equal to 0';
      if (control.errors?.['minlength']) return 'Description must be at least 5 characters';
    }
    return undefined;
  }

  getFieldLabel(controlName: string): string {
    const labels: Record<string, string> = {
      policyId: 'Policy number',
      incidentDate: 'Incident date',
      estimatedLoss: 'Estimated loss',
      description: 'Description',
    };
    return labels[controlName] || controlName;
  }

  private loadPolicies(preselectedPolicyId: string | null): void {
    const customerId = typeof localStorage !== 'undefined' ? localStorage.getItem('is_customer_id') : null;
    if (!customerId) {
      this.policyLookupError = 'Complete your business profile before filing a claim.';
      return;
    }

    this.policiesLoading = true;
    this.policyService.getCustomerPolicies(customerId).subscribe({
      next: (policies) => {
        this.policiesLoading = false;
        const eligiblePolicies = policies.filter((policy) => ['ACTIVE', 'BOUND', 'ISSUED', 'IN_FORCE'].includes(policy.status.toUpperCase()));
        this.policyOptions = eligiblePolicies.map((policy) => ({
          value: policy.policyId,
          label: `${policy.policyNumber} · ${this.formatProduct(policy.productCode)}`,
        }));
        this.selectedPolicy = eligiblePolicies.find((policy) => policy.policyId === preselectedPolicyId) ?? null;
        if (!this.selectedPolicy && this.policyOptions.length === 1) {
          this.claimForm.controls.policyId.setValue(this.policyOptions[0].value);
          this.selectedPolicy = eligiblePolicies[0];
        }
        this.claimForm.controls.policyId.valueChanges.subscribe((value) => {
          this.selectedPolicy = eligiblePolicies.find((policy) => policy.policyId === value) ?? null;
        });
      },
      error: () => {
        this.policiesLoading = false;
        this.policyLookupError = 'Your policies could not be loaded. Please try again.';
      },
    });
  }

  private formatProduct(productCode: string): string {
    return productCode.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
  }

  onNext(): void {
    if (this.currentStep === 0) {
      const pControl = this.claimForm.get('policyId');
      const dControl = this.claimForm.get('incidentDate');
      pControl?.markAsTouched();
      dControl?.markAsTouched();
      if (pControl?.invalid || dControl?.invalid) return;
    } else if (this.currentStep === 1) {
      const descControl = this.claimForm.get('description');
      descControl?.markAsTouched();
      if (descControl?.invalid) return;
    }

    if (this.currentStep < this.steps.length - 1) {
      this.currentStep++;
    }
  }

  onPrevious(): void {
    if (this.currentStep > 0) {
      this.currentStep--;
    }
  }

  onSubmit(): void {
    if (this.claimForm.invalid) {
      this.claimForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.error = null;
    const payload = this.claimForm.getRawValue();

    this.claimsService
      .fileClaim({
        policyId: payload.policyId.trim(),
        incidentDate: payload.incidentDate,
        description: payload.description.trim(),
        estimatedLoss: payload.estimatedLoss,
      })
      .subscribe({
        next: (created) => {
          this.submitting = false;
          this.router.navigate(['/claims', created.claimId]);
        },
        error: (err) => {
          this.submitting = false;
          this.error = this.describeError(err);
        },
      });
  }

  private describeError(err: { status?: number; error?: { message?: string }; message?: string }): string {
    if (err?.status === 401) return 'Your session has expired. Please sign in again.';
    if (err?.status === 403) return 'You do not have permission to file a claim.';
    if (err?.status === 404) return 'The selected policy or claims endpoint could not be found.';
    return err?.error?.message || err?.message || 'Failed to file claim. Please verify that the selected policy is eligible.';
  }
}
