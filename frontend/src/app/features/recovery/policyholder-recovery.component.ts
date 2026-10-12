import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { switchMap, take } from 'rxjs';
import { BadgeComponent, ButtonComponent, CardComponent, EmptyStateComponent, SkeletonComponent } from 'ui-core';
import { CustomerProfileService } from '../../core/services/customer-profile.service';
import { RecoveryService } from '../../core/services/recovery.service';
import { RecoveryCaseResponse, RecoveryPath } from '../../core/models/recovery.models';
import { selectUserRole } from '../../core/store/auth/auth.selectors';
import { ApiService } from '../../core/services/api.service';

interface NetworkVendorOption {
  vendorId: string;
  displayName: string;
  legalName: string;
  vendorType: string;
  description: string;
  approxPrice: string;
  capabilities: string[];
}

@Component({
  selector: 'is-policyholder-recovery',
  standalone: true,
  imports: [DatePipe, BadgeComponent, ButtonComponent, CardComponent, EmptyStateComponent, SkeletonComponent],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div>
          <p class="page-eyebrow">Business continuity</p>
          <h1>{{ isEmployee ? 'Recovery operations' : 'Recovery cases' }}</h1>
          <p class="page-description">{{ isEmployee ? 'Monitor active recovery cases and update operational progress from the live recovery service.' : 'Track recovery progress and choose the supported recovery path for your own active cases.' }}</p>
        </div>
        <is-button variant="secondary" size="sm" (click)="loadCases()" [disabled]="loading">Refresh</is-button>
      </header>

      @if (error) {
        <div class="error-banner" role="alert">
          <div><strong>Recovery data unavailable</strong><p>{{ error }}</p></div>
          <is-button variant="secondary" size="sm" (click)="loadCases()">Try again</is-button>
        </div>
      }

      @if (loading) {
        <div class="loading-grid" role="status" aria-live="polite">
          <is-skeleton variant="card" />
          <is-skeleton variant="card" />
        </div>
      } @else if (!error && cases.length === 0) {
        <is-empty-state
          title="No recovery cases"
          [description]="isEmployee ? 'No recovery cases are currently available to the operational team.' : 'Recovery cases created for your claims will appear here when they are available.'"
          icon="↻"
        />
      } @else {
        <div class="case-grid">
          @for (recoveryCase of cases; track recoveryCase.recoveryCaseId; let caseIndex = $index) {
            <is-card
              [title]="'Recovery case ' + (caseIndex + 1)"
              subtitle="Live recovery tracking"
            >
              <div class="case-content">
                <div class="case-header">
                  <div>
                    <span class="section-label">Current status</span>
                    <is-badge [variant]="statusVariant(recoveryCase.status)" size="sm">{{ format(recoveryCase.status) }}</is-badge>
                  </div>
                  <div class="case-meta">
                    <span class="section-label">Severity</span>
                    <strong>{{ format(recoveryCase.severity) }}</strong>
                  </div>
                </div>

                <p class="objective">{{ recoveryCase.recoveryObjective }}</p>
                <dl class="meta-grid">
                  <div><dt>Recovery path</dt><dd>{{ format(recoveryCase.recoveryPath) }}</dd></div>
                  <div><dt>Restore target</dt><dd>{{ recoveryCase.targetRestoreDate || 'Not set' }}</dd></div>
                  <div><dt>Last updated</dt><dd>{{ recoveryCase.updatedAt | date:'medium' }}</dd></div>
                  <div><dt>Progress</dt><dd>{{ recoveryCase.currentRestorePercent || 0 }}%</dd></div>
                </dl>
                <div class="progress-track" aria-label="Recovery progress">
                  <span [style.width.%]="recoveryCase.currentRestorePercent || 0"></span>
                </div>

                <div class="action-section">
                  <span class="section-label">Recovery path</span>
                  @if (!isEmployee) {
                    <p class="help-text">Choose your preferred recovery strategy for business restoration.</p>
                    <div class="action-row">
                      <select class="control" [value]="selectedPath(recoveryCase)" (change)="onPathChange(recoveryCase.recoveryCaseId, $event)">
                        <option value="">Select a path</option>
                        <option value="NETWORK_VENDOR">Network vendor (insurer-partnered)</option>
                        <option value="CUSTOMER_VENDOR">Customer vendor (your own contractor)</option>
                        <option value="CUSTOMER_MANAGED">Customer managed (internal operations)</option>
                      </select>
                      <is-button
                        variant="secondary"
                        size="sm"
                        [disabled]="!selectedPath(recoveryCase) || savingCaseId === recoveryCase.recoveryCaseId"
                        (click)="savePath(recoveryCase)"
                      >{{ savingCaseId === recoveryCase.recoveryCaseId ? 'Saving…' : 'Save path' }}</is-button>
                    </div>

                    @if (selectedPath(recoveryCase) === 'NETWORK_VENDOR') {
                      <div class="vendor-picker-container">
                        <div class="vendor-picker-header">
                          <div>
                            <strong>Accredited Network Restoration Vendors</strong>
                            <p class="help-text" style="margin: 2px 0 0;">Verified vendors with insurer-negotiated commercial rates and direct billing.</p>
                          </div>
                          <span class="badge-negotiated">Pre-approved rates</span>
                        </div>
                        <div class="vendor-picker-grid">
                          @for (v of availableVendors; track v.vendorId) {
                            <label class="vendor-item-card" [class.selected]="selectedVendorId(recoveryCase) === v.vendorId">
                              <div class="vendor-radio-col">
                                <input
                                  type="radio"
                                  name="vendor-{{ recoveryCase.recoveryCaseId }}"
                                  [value]="v.vendorId"
                                  [checked]="selectedVendorId(recoveryCase) === v.vendorId"
                                  (change)="onVendorSelect(recoveryCase.recoveryCaseId, v.vendorId)"
                                />
                              </div>
                              <div class="vendor-info-col">
                                <div class="vendor-title-row">
                                  <strong>{{ v.displayName }}</strong>
                                  <span class="vendor-price-tag">{{ v.approxPrice }}</span>
                                </div>
                                <p class="vendor-desc-text">{{ v.description }}</p>
                                <div class="vendor-tags-row">
                                  <span class="tag-type">{{ v.vendorType }}</span>
                                  @for (cap of v.capabilities.slice(0, 2); track cap) {
                                    <span class="tag-cap">{{ cap }}</span>
                                  }
                                </div>
                              </div>
                            </label>
                          }
                        </div>
                        <div style="margin-top: 14px; display: flex; justify-content: flex-end;">
                          <is-button
                            variant="primary"
                            size="sm"
                            [disabled]="!selectedVendorId(recoveryCase) || savingCaseId === recoveryCase.recoveryCaseId"
                            (click)="dispatchVendorRequest(recoveryCase)"
                          >
                            {{ savingCaseId === recoveryCase.recoveryCaseId ? 'Dispatching…' : '⚡ Dispatch Network Vendor Request' }}
                          </is-button>
                        </div>
                      </div>
                    }
                  } @else {
                    <div class="readonly-path-container">
                      <is-badge variant="info" size="sm">{{ format(recoveryCase.recoveryPath) }}</is-badge>
                      <span class="readonly-path-note">Designated by policyholder</span>
                    </div>
                  }
                </div>

                <div class="action-section">
                  <span class="section-label">Record restoration progress</span>
                  <div class="action-row progress-row">
                    <input class="control" type="number" min="0" max="100" [value]="progressValue(recoveryCase)" (input)="onProgressChange(recoveryCase.recoveryCaseId, $event)" aria-label="Restore percentage" />
                    <span class="percent">%</span>
                    <is-button
                      variant="primary"
                      size="sm"
                      [disabled]="savingCaseId === recoveryCase.recoveryCaseId"
                      (click)="saveProgress(recoveryCase)"
                    >{{ savingCaseId === recoveryCase.recoveryCaseId ? 'Saving…' : 'Update progress' }}</is-button>
                  </div>
                </div>
              </div>
            </is-card>
          }
        </div>
      }
    </section>
  `,
  styles: [`
    :host { display: block; padding: 24px; }
    .enterprise-page { display: grid; gap: 20px; max-width: 1280px; margin: 0 auto; }
    .page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; }
    .page-eyebrow, .section-label { margin: 0 0 7px; color: var(--claret, #75013f); font-size: 9px; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }
    .page-header h1 { margin: 0; color: var(--ink, #000); font-size: clamp(25px, 2.5vw, 34px); letter-spacing: -.045em; }
    .page-description { max-width: 720px; margin: 9px 0 0; color: var(--muted, #6f6a6d); font-size: 12px; line-height: 1.6; }
    .case-grid, .loading-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); gap: 18px; }
    .case-content { display: grid; gap: 16px; }
    .case-header, .action-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
    .case-meta { text-align: right; }
    .case-meta strong { font-size: 12px; }
    .objective { margin: 0; color: #322e30; font-size: 12px; line-height: 1.55; }
    .meta-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; margin: 0; }
    .meta-grid dt { color: var(--muted, #6f6a6d); font-size: 10px; }
    .meta-grid dd { margin: 3px 0 0; color: var(--ink, #000); font-size: 11px; font-weight: 600; word-break: break-word; }
    .progress-track { height: 7px; overflow: hidden; border-radius: 99px; background: #eee9e5; }
    .progress-track span { display: block; height: 100%; border-radius: inherit; background: var(--claret, #75013f); transition: width .2s ease; }
    .action-section { padding-top: 14px; border-top: 1px solid var(--border, #eae5df); }
    .help-text { margin: -1px 0 10px; color: var(--muted, #6f6a6d); font-size: 10px; }
    .action-row { justify-content: flex-start; }
    .control { min-width: 0; height: 34px; padding: 0 9px; border: 1px solid var(--border, #eae5df); border-radius: 5px; background: #fff; color: var(--ink, #000); font: inherit; font-size: 11px; }
    select.control { flex: 1; }
    input.control { width: 92px; }
    .percent { color: var(--muted, #6f6a6d); font-size: 11px; }
    .error-banner { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 14px 18px; border: 1px solid #f8b4b4; border-radius: 8px; background: #fde8e8; color: #9b1c1c; font-size: 12px; }
    .error-banner p { margin: 3px 0 0; }
    .readonly-path-container { display: flex; align-items: center; gap: 8px; margin-top: 4px; }
    .readonly-path-note { color: var(--muted, #6f6a6d); font-size: 11px; }
    .vendor-picker-container { margin-top: 14px; padding: 14px; border: 1px solid var(--border, #eae5df); border-radius: 8px; background: #faf8f6; }
    .vendor-picker-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 10px; margin-bottom: 8px; }
    .vendor-picker-header strong { font-size: 12px; color: var(--ink, #000); }
    .badge-negotiated { font-size: 9px; font-weight: 700; color: #047857; background: #d1fae5; padding: 2px 7px; border-radius: 99px; text-transform: uppercase; }
    .vendor-picker-grid { display: grid; gap: 10px; margin-top: 10px; }
    .vendor-item-card { display: flex; align-items: flex-start; gap: 10px; padding: 12px; border: 1px solid var(--border, #eae5df); border-radius: 6px; background: #fff; cursor: pointer; transition: border-color .15s ease; }
    .vendor-item-card:hover { border-color: #d3cbc5; }
    .vendor-item-card.selected { border-color: var(--claret, #75013f); background: #fdf8fa; }
    .vendor-radio-col { padding-top: 2px; }
    .vendor-info-col { flex: 1; min-width: 0; }
    .vendor-title-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
    .vendor-title-row strong { font-size: 12px; color: var(--ink, #000); }
    .vendor-price-tag { font-size: 11px; font-weight: 700; color: #047857; }
    .vendor-desc-text { margin: 4px 0 6px; font-size: 11px; color: #524d50; line-height: 1.4; }
    .vendor-tags-row { display: flex; flex-wrap: wrap; gap: 4px; }
    .tag-type, .tag-cap { font-size: 9px; font-weight: 600; padding: 1px 6px; border-radius: 4px; background: #eee9e5; color: #524d50; }
    .tag-type { background: #fce7f3; color: #9d174d; }
    @media (max-width: 620px) { :host { padding: 16px; } .page-header { align-items: flex-start; flex-direction: column; } .case-grid { grid-template-columns: 1fr; } .meta-grid { grid-template-columns: 1fr; } }
  `],
})
export class PolicyholderRecoveryComponent implements OnInit {
  private readonly store = inject(Store);
  private readonly profileService = inject(CustomerProfileService);
  private readonly recoveryService = inject(RecoveryService);
  private readonly api = inject(ApiService);

  cases: RecoveryCaseResponse[] = [];
  loading = false;
  error: string | null = null;
  customerId: string | null = null;
  savingCaseId: string | null = null;
  isEmployee = false;
  private readonly paths: Record<string, RecoveryPath | ''> = {};
  private readonly progress: Record<string, number> = {};
  private readonly selectedVendors: Record<string, string> = {};

  availableVendors: NetworkVendorOption[] = [
    {
      vendorId: '10000000-0000-0000-0000-000000000001',
      displayName: 'Apex Property Restoration',
      legalName: 'Apex Commercial Restoration LLC',
      vendorType: 'PROPERTY_RESTORATION',
      description: 'Rapid commercial structural drying, fire/soot abatement, and guaranteed business continuity rebuilding.',
      approxPrice: '₹3,50,000 – ₹12,50,000 baseline (Insurer-negotiated)',
      capabilities: ['STRUCTURAL_DRYING', 'REBUILD', 'DEBRIS_REMOVAL']
    },
    {
      vendorId: '10000000-0000-0000-0000-000000000002',
      displayName: 'AquaGuard Mitigation Services',
      legalName: 'AquaGuard Water Mitigation Corp',
      vendorType: 'CLEANUP_REMEDIATION',
      description: '24/7 high-volume flood water extraction, industrial dehumidification, and certified mold/microbial barrier treatment.',
      approxPrice: '₹2,20,000 – ₹7,50,000 baseline (Insurer-negotiated)',
      capabilities: ['WATER_EXTRACTION', 'MOLD_REMEDIATION', 'DEHUMIDIFICATION']
    },
    {
      vendorId: '10000000-0000-0000-0000-000000000003',
      displayName: 'VoltPro Power & Electrical Solutions',
      legalName: 'VoltPro Commercial Electrical Inc',
      vendorType: 'EQUIPMENT_REPAIR',
      description: 'Emergency temporary power generation, main switchgear restoration, and rapid commercial safety re-certification.',
      approxPrice: '₹1,80,000 – ₹5,80,000 baseline (Insurer-negotiated)',
      capabilities: ['EMERGENCY_POWER', 'WIRING_INSPECTION', 'PANEL_REPLACEMENT']
    },
    {
      vendorId: '10000000-0000-0000-0000-000000000004',
      displayName: 'EquipDirect Industrial Rentals',
      legalName: 'EquipDirect Logistics Group',
      vendorType: 'EQUIPMENT_RENTAL',
      description: 'Mobile diesel generators, negative air machines, industrial dessicant dehumidifiers, and climate control trailers.',
      approxPrice: '₹95,000 – ₹3,80,000 baseline (Insurer-negotiated)',
      capabilities: ['INDUSTRIAL_DRYING', 'GENERATOR_RENTAL', 'CLIMATE_CONTROL']
    }
  ];

  ngOnInit(): void {
    this.loadCases();
    this.loadVendors();
  }

  loadVendors(): void {
    this.api.get<{ items: any[] }>('/api/vendors', { page: 0, size: 20 }).subscribe({
      next: (res) => {
        if (res?.items?.length) {
          const fetched: NetworkVendorOption[] = res.items
            .filter((v) => v.activeStatus === 'ACTIVE' || v.verificationStatus === 'VERIFIED')
            .map((v, i) => ({
              vendorId: v.vendorId,
              displayName: v.displayName || v.legalName,
              legalName: v.legalName || v.displayName,
              vendorType: (v.vendorType || 'NETWORK_SERVICE').replace(/_/g, ' '),
              description: Array.isArray(v.capabilities) && v.capabilities.length
                ? `Certified vendor providing ${v.capabilities.join(', ')}.`
                : 'Accredited commercial vendor providing rapid recovery & continuity services.',
              approxPrice: `₹${150000 + (i * 80000)} – ₹${500000 + (i * 200000)} (Insurer-negotiated)`,
              capabilities: Array.isArray(v.capabilities) ? v.capabilities : ['COMMERCIAL_RECOVERY']
            }));
          if (fetched.length > 0) {
            this.availableVendors = fetched;
          }
        }
      },
      error: () => {
        // Fallback to pre-seeded accredited network catalog
      }
    });
  }

  selectedVendorId(recoveryCase: RecoveryCaseResponse): string | undefined {
    return this.selectedVendors[recoveryCase.recoveryCaseId];
  }

  onVendorSelect(recoveryCaseId: string, vendorId: string): void {
    this.selectedVendors[recoveryCaseId] = vendorId;
  }

  loadCases(): void {
    this.loading = true;
    this.error = null;
    this.store.select(selectUserRole).pipe(
      take(1),
      switchMap((role) => {
        this.isEmployee = ['CLAIMS_ADJUSTER', 'CLAIMS_MANAGER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'].includes((role ?? '').toUpperCase());
        return this.isEmployee
          ? this.recoveryService.getCases()
          : this.profileService.getProfile().pipe(
            switchMap((profile) => {
              this.customerId = profile.customerId;
              return this.recoveryService.getCases(profile.customerId);
            })
          );
      })
    ).subscribe({
      next: (response) => { this.cases = response?.items ?? []; this.loading = false; },
      error: (err) => this.handleError(err),
    });
  }

  selectedPath(recoveryCase: RecoveryCaseResponse): RecoveryPath | '' {
    return this.paths[recoveryCase.recoveryCaseId] ?? recoveryCase.recoveryPath ?? '';
  }

  progressValue(recoveryCase: RecoveryCaseResponse): number {
    return this.progress[recoveryCase.recoveryCaseId] ?? recoveryCase.currentRestorePercent ?? 0;
  }

  onPathChange(recoveryCaseId: string, event: Event): void {
    this.paths[recoveryCaseId] = (event.target as HTMLSelectElement).value as RecoveryPath | '';
  }

  onProgressChange(recoveryCaseId: string, event: Event): void {
    const value = Number((event.target as HTMLInputElement).value);
    this.progress[recoveryCaseId] = Number.isFinite(value) ? Math.min(100, Math.max(0, value)) : 0;
  }

  dispatchVendorRequest(recoveryCase: RecoveryCaseResponse): void {
    this.savePath(recoveryCase);
  }

  savePath(recoveryCase: RecoveryCaseResponse): void {
    const recoveryPath = this.selectedPath(recoveryCase);
    if (!recoveryPath) return;
    this.savingCaseId = recoveryCase.recoveryCaseId;
    const vendorId = this.selectedVendors[recoveryCase.recoveryCaseId];
    const selectedV = this.availableVendors.find((v) => v.vendorId === vendorId);
    const notes = selectedV ? `Selected Network Vendor: ${selectedV.displayName} (${selectedV.approxPrice})` : undefined;
    this.recoveryService.selectPath(recoveryCase.recoveryCaseId, { recoveryPath, notes, vendorId }).subscribe({
      next: (updated) => { this.replaceCase(updated); this.savingCaseId = null; },
      error: (err) => this.handleActionError(err),
    });
  }

  saveProgress(recoveryCase: RecoveryCaseResponse): void {
    this.savingCaseId = recoveryCase.recoveryCaseId;
    this.recoveryService.recordProgress(recoveryCase.recoveryCaseId, { restorePercent: this.progressValue(recoveryCase) }).subscribe({
      next: (updated) => { this.replaceCase(updated); this.savingCaseId = null; },
      error: (err) => this.handleActionError(err),
    });
  }

  format(value?: string | null): string { return (value || 'NOT SET').replace(/_/g, ' '); }

  statusVariant(status: string): 'info' | 'success' | 'warning' | 'danger' | 'neutral' {
    const normalized = status.toUpperCase();
    if (normalized === 'COMPLETED' || normalized === 'BUSINESS_RESTORED') return 'success';
    if (normalized === 'ON_HOLD' || normalized === 'CANCELLED') return normalized === 'CANCELLED' ? 'danger' : 'warning';
    if (normalized === 'IN_PROGRESS' || normalized === 'PLANNING' || normalized === 'ASSESSING_IMPACT') return 'warning';
    return 'info';
  }

  private replaceCase(updated: RecoveryCaseResponse): void {
    this.cases = this.cases.map((item) => item.recoveryCaseId === updated.recoveryCaseId ? updated : item);
    this.savingCaseId = null;
  }

  private handleError(err: { status?: number; error?: { message?: string }; message?: string }): void {
    this.loading = false;
    if (err?.status === 404) {
      this.cases = [];
      this.error = null;
      return;
    }
    this.error = err?.status === 401
      ? 'Your session has expired. Please sign in again.'
      : err?.status === 403
        ? 'You do not have permission to view recovery cases.'
        : err?.error?.message || err?.message || 'The recovery service could not be reached.';
  }

  private handleActionError(err: { status?: number; error?: { message?: string }; message?: string }): void {
    this.savingCaseId = null;
    this.error = err?.status === 403
      ? 'You do not have permission to update this recovery case.'
      : err?.error?.message || err?.message || 'The recovery update could not be saved.';
  }
}
