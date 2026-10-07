import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ClaimsService } from '../../services/claims.service';

import { CardComponent, ButtonComponent, InputComponent, SelectComponent, TextareaComponent, StepperComponent, StepperStep } from 'ui-core';

@Component({
  selector: 'claims-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule, 
    RouterLink,
    CardComponent,
    ButtonComponent,
    InputComponent,
    SelectComponent,
    TextareaComponent,
    StepperComponent
  ],
  template: `
    <div class="form-workflow">
      <is-card title="File a new claim" subtitle="Complete each section to submit a new claim for processing.">
        <is-stepper
          [steps]="steps"
          [currentStep]="currentStep"
          [orientation]="'horizontal'"
          [submitLabel]="'Submit claim'"
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
      <div class="form-grid">
        <is-input
          label="Policy number"
          placeholder="POL-XXXX"
          formControlName="policyNumber"
          [error]="getError('policyNumber')"
        />
        <is-input
          label="Claimant"
          placeholder="Full name"
          formControlName="claimantName"
          [error]="getError('claimantName')"
        />
        <is-select
          label="Claim type"
          placeholder="Select claim type"
          formControlName="claimType"
          [options]="claimTypes"
          [error]="getError('claimType')"
        />
        <is-input
          label="Loss date"
          type="date"
          formControlName="lossDate"
          [error]="getError('lossDate')"
        />
        <div class="md:col-span-2">
          <is-input
            label="Claim amount (USD)"
            type="number"
            placeholder="0.00"
            formControlName="claimedAmount"
            [error]="getError('claimedAmount')"
          />
        </div>
      </div>
    </ng-template>

    <ng-template #detailsSection>
      <div class="form-grid">
        <div class="md:col-span-2">
          <is-textarea
            label="Description"
            placeholder="Describe the incident, damage, and circumstances..."
            formControlName="description"
            [rows]="5"
            [error]="getError('description')"
          />
        </div>
        <is-select
          label="Priority"
          placeholder="Select priority"
          formControlName="priority"
          [options]="priorityOptions"
        />
        <is-input
          label="Adjuster assigned"
          placeholder="Auto-assigned or manual"
          formControlName="assignedAdjuster"
        />
      </div>
    </ng-template>

    <ng-template #reviewSection>
      <div class="review-summary">
        <h4>Review your submission</h4>
        <dl class="review-grid">
          <dt>Policy number</dt>
          <dd>{{ claimForm.get('policyNumber')?.value }}</dd>
          <dt>Claimant</dt>
          <dd>{{ claimForm.get('claimantName')?.value }}</dd>
          <dt>Claim type</dt>
          <dd>{{ getClaimTypeLabel(claimForm.get('claimType')?.value ?? '') }}</dd>
          <dt>Loss date</dt>
          <dd>{{ claimForm.get('lossDate')?.value }}</dd>
          <dt>Claim amount</dt>
          <dd>{{ claimForm.get('claimedAmount')?.value | currency:'USD':'symbol':'1.0-0' }}</dd>
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
      color: var(--ink);
    }
    
    .review-grid {
      display: grid;
      grid-template-columns: 180px 1fr;
      gap: 8px 16px;
      font-size: 12px;
    }
    
    .review-grid dt {
      color: var(--muted);
      font-weight: 500;
    }
    
    .review-grid dd {
      margin: 0;
      color: var(--ink);
      font-weight: 500;
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
  private readonly router = inject(Router);

  readonly steps: StepperStep[] = [
    { label: 'Policy & Claimant', description: 'Basic policy and claimant information' },
    { label: 'Claim Details', description: 'Incident description and priority' },
    { label: 'Review & Submit', description: 'Review and confirm submission' },
  ];

  currentStep = 0;

  readonly claimTypes = [
    { value: 'AUTO_COLLISION', label: 'Auto Collision' },
    { value: 'AUTO_COMPREHENSIVE', label: 'Auto Comprehensive' },
    { value: 'PROPERTY_FIRE', label: 'Property Fire' },
    { value: 'PROPERTY_WATER', label: 'Property Water Damage' },
    { value: 'GENERAL_LIABILITY', label: 'General Liability' },
    { value: 'WORKERS_COMP', label: 'Workers Compensation' },
    { value: 'PROFESSIONAL_LIABILITY', label: 'Professional Liability' },
    { value: 'CYBER', label: 'Cyber Liability' },
    { value: 'OTHER', label: 'Other' },
  ];

  readonly priorityOptions = [
    { value: 'LOW', label: 'Low' },
    { value: 'NORMAL', label: 'Normal' },
    { value: 'HIGH', label: 'High' },
    { value: 'URGENT', label: 'Urgent' },
  ];

  readonly claimForm = this.fb.nonNullable.group({
    policyNumber: ['', Validators.required],
    claimantName: ['', Validators.required],
    claimType: ['', Validators.required],
    lossDate: ['', Validators.required],
    claimedAmount: [0, [Validators.required, Validators.min(1)]],
    description: ['', Validators.required],
    priority: ['NORMAL'],
    assignedAdjuster: [''],
  });

  constructor(private readonly claimsService: ClaimsService) {}

  getError(controlName: string): string | undefined {
    const control = this.claimForm.get(controlName);
    if (control?.invalid && (control.dirty || control.touched)) {
      if (control.errors?.['required']) return `${this.getFieldLabel(controlName)} is required`;
      if (control.errors?.['min']) return 'Amount must be greater than 0';
    }
    return undefined;
  }

  getFieldLabel(controlName: string): string {
    const labels: Record<string, string> = {
      policyNumber: 'Policy number',
      claimantName: 'Claimant name',
      claimType: 'Claim type',
      lossDate: 'Loss date',
      claimedAmount: 'Claim amount',
      description: 'Description',
    };
    return labels[controlName] || controlName;
  }

  getClaimTypeLabel(value: string): string {
    return this.claimTypes.find(t => t.value === value)?.label || value;
  }

  onNext(): void {
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

    const payload = this.claimForm.getRawValue();
    const created = this.claimsService.addClaim({
      policyNumber: payload.policyNumber,
      claimantName: payload.claimantName,
      claimType: payload.claimType,
      lossDate: payload.lossDate,
      claimedAmount: payload.claimedAmount,
      description: payload.description,
    });

    this.router.navigateByUrl(`/${created.claimId}`);
  }
}