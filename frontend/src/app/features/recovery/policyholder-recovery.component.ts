import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { BadgeComponent, ButtonComponent, CardComponent, EmptyStateComponent, SkeletonComponent } from 'ui-core';
import { CustomerProfileService } from '../../core/services/customer-profile.service';
import { RecoveryService } from '../../core/services/recovery.service';
import { RecoveryCaseResponse, RecoveryPath } from '../../core/models/recovery.models';

@Component({
  selector: 'is-policyholder-recovery',
  standalone: true,
  imports: [DatePipe, BadgeComponent, ButtonComponent, CardComponent, EmptyStateComponent, SkeletonComponent],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div>
          <p class="page-eyebrow">Business continuity</p>
          <h1>Recovery cases</h1>
          <p class="page-description">Track recovery progress and choose the supported recovery path for your own active cases.</p>
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
          description="Recovery cases created for your claims will appear here when they are available."
          icon="↻"
        />
      } @else {
        <div class="case-grid">
          @for (recoveryCase of cases; track recoveryCase.recoveryCaseId) {
            <is-card
              [title]="'Recovery case ' + recoveryCase.recoveryCaseId"
              [subtitle]="'Claim ' + recoveryCase.claimId"
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
                  <span class="section-label">Choose recovery path</span>
                  <p class="help-text">Network vendor selection does not dispatch a vendor automatically.</p>
                  <div class="action-row">
                    <select class="control" [value]="selectedPath(recoveryCase)" (change)="onPathChange(recoveryCase.recoveryCaseId, $event)">
                      <option value="">Select a path</option>
                      <option value="NETWORK_VENDOR">Network vendor</option>
                      <option value="CUSTOMER_VENDOR">Customer vendor</option>
                      <option value="CUSTOMER_MANAGED">Customer managed</option>
                    </select>
                    <is-button
                      variant="secondary"
                      size="sm"
                      [disabled]="!selectedPath(recoveryCase) || savingCaseId === recoveryCase.recoveryCaseId"
                      (click)="savePath(recoveryCase)"
                    >{{ savingCaseId === recoveryCase.recoveryCaseId ? 'Saving…' : 'Save path' }}</is-button>
                  </div>
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
    @media (max-width: 620px) { :host { padding: 16px; } .page-header { align-items: flex-start; flex-direction: column; } .case-grid { grid-template-columns: 1fr; } .meta-grid { grid-template-columns: 1fr; } }
  `],
})
export class PolicyholderRecoveryComponent implements OnInit {
  private readonly profileService = inject(CustomerProfileService);
  private readonly recoveryService = inject(RecoveryService);

  cases: RecoveryCaseResponse[] = [];
  loading = false;
  error: string | null = null;
  customerId: string | null = null;
  savingCaseId: string | null = null;
  private readonly paths: Record<string, RecoveryPath | ''> = {};
  private readonly progress: Record<string, number> = {};

  ngOnInit(): void { this.loadCases(); }

  loadCases(): void {
    this.loading = true;
    this.error = null;
    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.customerId = profile.customerId;
        this.recoveryService.getCases(profile.customerId).subscribe({
          next: (response) => { this.cases = response?.items ?? []; this.loading = false; },
          error: (err) => this.handleError(err),
        });
      },
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

  savePath(recoveryCase: RecoveryCaseResponse): void {
    const recoveryPath = this.selectedPath(recoveryCase);
    if (!recoveryPath) return;
    this.savingCaseId = recoveryCase.recoveryCaseId;
    this.recoveryService.selectPath(recoveryCase.recoveryCaseId, { recoveryPath }).subscribe({
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
