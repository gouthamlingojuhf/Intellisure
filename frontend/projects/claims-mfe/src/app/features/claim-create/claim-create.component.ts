import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ClaimsService } from '../../services/claims.service';

@Component({
  selector: 'claims-create',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="card max-w-2xl mx-auto">
      <div class="mb-4 flex items-center justify-between">
        <div>
          <p class="text-xs uppercase tracking-[0.18em] text-blue-700 font-semibold">Claims Ops</p>
          <h1 class="mt-2 text-2xl font-bold text-slate-900">File a new claim</h1>
        </div>
        <a routerLink="/" class="btn-secondary">Back to queue</a>
      </div>

      <form [formGroup]="claimForm" (ngSubmit)="submit()" class="space-y-4">
        <div class="grid gap-4 md:grid-cols-2">
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Policy number</label>
            <input class="input-field" formControlName="policyNumber" placeholder="POL-XXXX" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Claimant</label>
            <input class="input-field" formControlName="claimantName" placeholder="Full name" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Claim type</label>
            <input class="input-field" formControlName="claimType" placeholder="AUTO_COLLISION" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Loss date</label>
            <input class="input-field" type="date" formControlName="lossDate" />
          </div>
          <div class="md:col-span-2">
            <label class="mb-1 block text-sm font-medium text-slate-700">Claim amount</label>
            <input class="input-field" type="number" formControlName="claimedAmount" />
          </div>
          <div class="md:col-span-2">
            <label class="mb-1 block text-sm font-medium text-slate-700">Description</label>
            <textarea class="input-field" rows="4" formControlName="description"></textarea>
          </div>
        </div>

        <div class="flex justify-end gap-3">
          <a routerLink="/" class="btn-secondary">Cancel</a>
          <button class="btn-primary" type="submit" [disabled]="claimForm.invalid">Submit claim</button>
        </div>
      </form>
    </section>
  `,
})
export class ClaimCreateComponent {
  private readonly fb = new FormBuilder();
  private readonly router = new Router();

  readonly claimForm = this.fb.nonNullable.group({
    policyNumber: ['', Validators.required],
    claimantName: ['', Validators.required],
    claimType: ['', Validators.required],
    lossDate: ['', Validators.required],
    claimedAmount: [0, [Validators.required, Validators.min(1)]],
    description: ['', Validators.required],
  });

  constructor(private readonly claimsService: ClaimsService) {}

  submit(): void {
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
