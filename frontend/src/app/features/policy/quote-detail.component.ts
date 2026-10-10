import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';

import { QuoteService } from '../../core/services/quote.service';
import { PolicyService } from '../../core/services/policy.service';
import {
  QuoteResponse,
  SubjectivityResponse,
  UnderwritingDecisionResponse,
} from '../../core/models/quote.models';
import { uiActions } from '../../core/store/ui/ui.actions';
import { selectUserRole } from '../../core/store/auth/auth.selectors';

// ui-core components
import {
  CardComponent,
  ButtonComponent,
  BadgeComponent,
  SkeletonComponent,
} from 'ui-core';

@Component({
  selector: 'is-quote-detail',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    DatePipe,
    RouterLink,
    FormsModule,
    CardComponent,
    ButtonComponent,
    BadgeComponent,
    SkeletonComponent,
  ],
  template: `
    <div class="quote-detail-page">
      <!-- Header -->
      <header class="page-header">
        <a routerLink="/quotes" class="back-link">&larr; Back to quotes</a>
        <div class="header-main">
          <div class="title-row">
            <h1>Quote {{ quote?.quoteNumber || 'details' }}</h1>
            @if (quote) {
              <is-badge [variant]="getStatusVariant(quote.status)" size="md">
                {{ formatStatus(quote.status) }}
              </is-badge>
            }
          </div>
          <p class="page-description">
            Detailed commercial quote application, underwriting review outcomes, coverage pricing, and policy binding handshake.
          </p>
        </div>
      </header>

      <!-- Loading State -->
      @if (loading) {
        <div class="loading-state" role="status" aria-live="polite">
          <is-skeleton variant="text" width="40%" height="32px" />
          <is-skeleton variant="card" />
          <is-skeleton variant="card" />
        </div>
      } @else if (errorMessage) {
        <div class="error-banner" role="alert">
          <div>
            <strong>Error loading quote:</strong>
            <p>{{ errorMessage }}</p>
          </div>
          <is-button variant="secondary" size="sm" (click)="loadQuoteData()">Retry</is-button>
        </div>
      } @else if (quote) {
        <!-- Status Action & Lifecycle Banner -->
        <section class="lifecycle-banner" [ngClass]="getBannerClass(quote.status)">
          <div class="banner-body">
            <!-- DRAFT STATUS -->
            @if (quote.status === 'DRAFT') {
              <div class="status-action-row">
                <div>
                  <h3>Draft Quote Prepared</h3>
                  <p>Your quote application is ready. Submit it to the underwriting desk to initiate risk assessment and commercial pricing.</p>
                </div>
                <is-button
                  variant="primary"
                  [disabled]="submitting"
                  (click)="onSubmitToUnderwriting()"
                >
                  {{ submitting ? 'Submitting to Underwriter…' : 'Submit for Underwriting →' }}
                </is-button>
              </div>
            }

            <!-- SUBMITTED / IN_REVIEW -->
            @if (quote.status === 'SUBMITTED' || quote.status === 'IN_REVIEW' || quote.status === 'TRIAGE' || quote.status === 'RISK_ASSESSMENT') {
              <div class="status-action-row">
                <div>
                  <h3>Underwriting Review in Progress</h3>
                  <p>
                    Your submission has been assigned to an underwriter. As risk assessments, conditions, or pricing are offered, they will update here.
                  </p>
                </div>
                <is-button variant="secondary" size="sm" (click)="loadQuoteData()">
                  Refresh Status ⟳
                </is-button>
              </div>
            }

            <!-- QUOTED TERMS OFFERED -->
            @if (quote.status === 'QUOTED') {
              <div class="status-action-row">
                <div>
                  <h3>Commercial Terms Offered!</h3>
                  <p>
                    Underwriter has approved terms with an indicated total premium of
                    <strong>{{ quote.totalPremium | currency:'INR':'symbol':'1.0-0' }}</strong>.
                    Review the offered limits below and accept to proceed to binding.
                  </p>
                </div>
                <div class="action-buttons">
                  <is-button
                    variant="secondary"
                    size="sm"
                    [disabled]="actionLoading"
                    (click)="showDeclineBox = !showDeclineBox"
                  >
                    Decline Quote
                  </is-button>
                  <is-button
                    variant="primary"
                    [disabled]="actionLoading"
                    (click)="onAcceptQuote()"
                  >
                    {{ actionLoading ? 'Accepting Terms…' : 'Accept Quoted Terms ✓' }}
                  </is-button>
                </div>
              </div>

              <!-- Decline reason box -->
              @if (showDeclineBox) {
                <div class="decline-box">
                  <label for="declineReason">Reason for declining terms:</label>
                  <input
                    id="declineReason"
                    type="text"
                    [(ngModel)]="declineReason"
                    placeholder="e.g. Higher limit requested, pricing out of budget, or duplicate quote..."
                    class="decline-input"
                  />
                  <div class="decline-actions">
                    <is-button variant="secondary" size="sm" (click)="showDeclineBox = false">Cancel</is-button>
                    <is-button
                      variant="primary"
                      size="sm"
                      [disabled]="!declineReason.trim() || actionLoading"
                      (click)="onDeclineQuote()"
                    >
                      Confirm Decline
                    </is-button>
                  </div>
                </div>
              }
            }

            <!-- ACCEPTED STATUS -->
            @if (quote.status === 'ACCEPTED') {
              <div class="status-action-row">
                <div>
                  <h3>Quote Accepted by Policyholder</h3>
                  <p>
                    @if (isEmployee) {
                      Terms accepted on {{ quote.acceptedAt | date:'medium' }}. You can now bind this quote into an in-force commercial policy.
                    } @else {
                      You accepted the offered terms on {{ quote.acceptedAt | date:'medium' }}. The underwriting desk is now binding the policy contract.
                    }
                  </p>
                </div>
                <div class="status-action-buttons">
                  @if (canBindQuote) {
                    <is-button variant="primary" size="sm" [disabled]="actionLoading" (click)="onBindQuote()">
                      {{ actionLoading ? 'Binding…' : 'Bind & Issue Policy →' }}
                    </is-button>
                  }
                  <is-button variant="secondary" size="sm" (click)="loadQuoteData()">
                    Check Binding Status ⟳
                  </is-button>
                </div>
              </div>
            }

            <!-- BOUND / ISSUED -->
            @if (quote.status === 'BOUND' || quote.status === 'ISSUED') {
              <div class="status-action-row">
                <div>
                  <h3>Coverage Bound & Active!</h3>
                  <p>
                    Contractually bound on {{ quote.boundAt | date:'medium' }}. Your commercial policy is active in the portfolio.
                  </p>
                </div>
                <is-button variant="primary" size="sm" routerLink="/policy">
                  View Bound Policy &rarr;
                </is-button>
              </div>
            }

            <!-- DECLINED -->
            @if (quote.status === 'DECLINED_BY_CUSTOMER' || quote.status === 'DECLINED_BY_INSURER') {
              <div>
                <h3>Quote Terminated</h3>
                <p>
                  {{ quote.status === 'DECLINED_BY_CUSTOMER' ? 'Declined by customer.' : 'Declined by insurer underwriting desk.' }}
                  @if (quote.declineReason) {
                    Reason: <em>{{ quote.declineReason }}</em>
                  }
                </p>
              </div>
            }
          </div>
        </section>

        <!-- Quote Summary Card -->
        <div class="detail-grid">
          <div class="grid-main">
            <!-- Coverages Comparison Table -->
            <is-card
              title="Coverage Specification & Pricing"
              subtitle="Requested limits vs underwriter-offered limits and indicated deductibles"
            >
              <div class="table-container">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Code</th>
                      <th>Coverage Name</th>
                      <th class="text-right">Requested Limit</th>
                      <th class="text-right">Offered Limit</th>
                      <th class="text-right">Deductible</th>
                      <th class="text-right">Indicated Premium</th>
                    </tr>
                  </thead>
                  <tbody>
                    @for (cov of quote.coverages; track cov.quoteCoverageId) {
                      <tr>
                        <td><code>{{ cov.coverageCode }}</code></td>
                        <td>
                          <strong>{{ cov.coverageName }}</strong>
                          @if (cov.conditions) {
                            <div class="conditions-note">Condition: {{ cov.conditions }}</div>
                          }
                          @if (cov.exclusions) {
                            <div class="exclusions-note">Exclusion: {{ cov.exclusions }}</div>
                          }
                        </td>
                        <td class="text-right">
                          \${{ cov.requestedLimit | number:'1.0-0' }}
                        </td>
                        <td class="text-right">
                          @if (cov.offeredLimit !== null && cov.offeredLimit !== undefined) {
                            <strong class="text-emerald">\${{ cov.offeredLimit | number:'1.0-0' }}</strong>
                          } @else {
                            <span class="text-muted">Pending Review</span>
                          }
                        </td>
                        <td class="text-right">
                          @if (cov.offeredDeductible !== null && cov.offeredDeductible !== undefined) {
                            \${{ cov.offeredDeductible | number:'1.0-0' }}
                          } @else {
                            \${{ cov.requestedDeductible | number:'1.0-0' }}
                          }
                        </td>
                        <td class="text-right">
                          @if (cov.coveragePremium !== null && cov.coveragePremium !== undefined) {
                            <strong>{{ cov.coveragePremium | currency:'INR':'symbol':'1.0-0' }}</strong>
                          } @else {
                            <span class="text-muted">—</span>
                          }
                        </td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            </is-card>

            <!-- Real Underwriting Decision History -->
            @if (decisions.length > 0) {
              <is-card
                title="Underwriting Decision Audit"
                subtitle="Authoritative decision record from the carrier underwriting desk"
              >
                <div class="timeline-list">
                  @for (dec of decisions; track dec.underwritingDecisionId) {
                    <div class="decision-card">
                      <div class="decision-header">
                        <is-badge [variant]="getDecisionVariant(dec.decision)" size="sm">
                          {{ formatStatus(dec.decision) }}
                        </is-badge>
                        <time class="decision-time">{{ dec.decidedAt | date:'medium' }}</time>
                      </div>
                      @if (dec.decisionReason) {
                        <p class="decision-reason">
                          <strong>Rationale:</strong> {{ dec.decisionReason }}
                        </p>
                      }
                      @if (dec.conditions) {
                        <p class="decision-conditions">
                          <strong>Conditions:</strong> {{ dec.conditions }}
                        </p>
                      }
                    </div>
                  }
                </div>
              </is-card>
            }

            <!-- Real Subjectivities / Pre-Bind Contingencies -->
            @if (subjectivities.length > 0) {
              <is-card
                title="Policy Subjectivities & Contingencies"
                subtitle="Requirements that must be satisfied prior to contractual policy binding"
              >
                <div class="subjectivities-list">
                  @for (sub of subjectivities; track sub.subjectivityId) {
                    <div class="subjectivity-row">
                      <div class="sub-status">
                        <is-badge [variant]="sub.status === 'SATISFIED' ? 'success' : (sub.status === 'WAIVED' ? 'neutral' : 'warning')" size="sm">
                          {{ sub.status }}
                        </is-badge>
                      </div>
                      <div class="sub-details">
                        <strong>{{ sub.subjectivityCode }}:</strong>
                        <span>{{ sub.description }}</span>
                        @if (sub.satisfiedAt) {
                          <small class="text-muted">Satisfied on {{ sub.satisfiedAt | date:'medium' }}</small>
                        }
                      </div>
                    </div>
                  }
                </div>
              </is-card>
            }
          </div>

          <!-- Right Sidebar Summary Meta -->
          <aside class="grid-side">
            <is-card title="Quote Overview" subtitle="Contract parameters">
              <dl class="meta-list">
                <div>
                  <dt>Quote number</dt>
                  <dd><strong>{{ quote.quoteNumber || 'Pending assignment' }}</strong></dd>
                </div>
                <div>
                  <dt>Product Code</dt>
                  <dd><strong>{{ quote.productCode }}</strong></dd>
                </div>
                <div>
                  <dt>Effective Date</dt>
                  <dd>{{ quote.requestedEffectiveDate }}</dd>
                </div>
                @if (quote.quoteExpiresAt) {
                  <div>
                    <dt>Quote Valid Until</dt>
                    <dd>{{ quote.quoteExpiresAt | date:'medium' }}</dd>
                  </div>
                }
                @if (quote.assignedUnderwriterId) {
                  <div>
                    <dt>Assigned Underwriter</dt>
                    <dd class="code-sm">{{ quote.assignedUnderwriterId }}</dd>
                  </div>
                }
                @if (quote.totalPremium) {
                  <div class="premium-highlight">
                    <dt>Total Indicated Premium</dt>
                    <dd class="premium-amount">{{ quote.totalPremium | currency:'INR':'symbol':'1.0-0' }}</dd>
                  </div>
                }
              </dl>
            </is-card>

            <is-card title="Business Operations" subtitle="Stated risk profile">
              <div class="operations-box">
                <span class="sub-label">Insurance Need</span>
                <p>{{ quote.insuranceNeed }}</p>
                <span class="sub-label">Operations Detail</span>
                <p>{{ quote.businessOperations }}</p>
              </div>
            </is-card>
          </aside>
        </div>
      }
    </div>
  `,
  styles: [`
    :host {
      display: block;
      padding: 24px;
    }

    .quote-detail-page {
      display: grid;
      gap: 22px;
      max-width: 1400px;
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

    .title-row {
      display: flex;
      align-items: center;
      gap: 16px;
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
      max-width: 760px;
      line-height: 1.5;
    }

    /* Lifecycle Banner */
    .lifecycle-banner {
      padding: 18px 22px;
      border-radius: 9px;
      border: 1px solid var(--border, #eae5df);
      background: var(--surface, #ffffff);
    }

    .lifecycle-banner.banner-draft {
      background: #eff6ff;
      border-color: #bfdbfe;
    }

    .lifecycle-banner.banner-review {
      background: #fffbeb;
      border-color: #fde68a;
    }

    .lifecycle-banner.banner-quoted {
      background: #f0fdf4;
      border-color: #bbf7d0;
    }

    .lifecycle-banner.banner-accepted {
      background: #fdf4ff;
      border-color: #f5d0fe;
    }

    .lifecycle-banner.banner-bound {
      background: #ecfdf5;
      border-color: #a7f3d0;
    }

    .lifecycle-banner.banner-declined {
      background: #fef2f2;
      border-color: #fecaca;
    }

    .status-action-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 20px;
      flex-wrap: wrap;
    }

    .status-action-buttons {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }

    .status-action-row h3 {
      margin: 0 0 4px;
      font-size: 15px;
      font-weight: 700;
      color: var(--ink, #000000);
    }

    .status-action-row p {
      margin: 0;
      font-size: 12px;
      color: #374151;
      line-height: 1.5;
    }

    .action-buttons {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .decline-box {
      margin-top: 14px;
      padding-top: 14px;
      border-top: 1px solid rgba(0, 0, 0, 0.08);
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .decline-box label {
      font-size: 11px;
      font-weight: 600;
      color: #991b1b;
    }

    .decline-input {
      padding: 8px 12px;
      border: 1px solid #f87171;
      border-radius: 6px;
      font-size: 12px;
    }

    .decline-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }

    /* Grid Layout */
    .detail-grid {
      display: grid;
      grid-template-columns: 1fr 340px;
      gap: 22px;
      align-items: start;
    }

    .grid-main {
      display: grid;
      gap: 22px;
    }

    .grid-side {
      display: grid;
      gap: 22px;
    }

    .table-container {
      overflow-x: auto;
    }

    .data-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 12px;
    }

    .data-table th {
      padding: 10px 14px;
      font-size: 10px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--muted, #6f6a6d);
      border-bottom: 1px solid var(--border, #eae5df);
      text-align: left;
    }

    .data-table td {
      padding: 12px 14px;
      border-bottom: 1px solid var(--border, #eae5df);
      color: var(--ink, #000000);
    }

    .text-right {
      text-align: right;
    }

    .text-emerald {
      color: #059669;
    }

    .text-muted {
      color: var(--muted, #6f6a6d);
      font-size: 11px;
    }

    .conditions-note, .exclusions-note {
      font-size: 10px;
      margin-top: 4px;
      color: #4b5563;
    }

    .meta-list {
      display: grid;
      gap: 12px;
      margin: 0;
      font-size: 12px;
    }

    .meta-list dt {
      font-size: 10px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--muted, #6f6a6d);
      margin-bottom: 2px;
    }

    .meta-list dd {
      margin: 0;
      color: var(--ink, #000000);
      word-break: break-all;
    }

    .code-sm {
      font-family: monospace;
      font-size: 11px;
    }

    .premium-highlight {
      background: var(--warm-light, #f7f5f3);
      padding: 12px;
      border-radius: 6px;
      border: 1px solid var(--border, #eae5df);
      margin-top: 6px;
    }

    .premium-amount {
      font-size: 22px;
      font-weight: 800;
      color: #065f46;
      margin-top: 4px;
    }

    .operations-box {
      font-size: 11px;
      color: var(--ink, #000000);
      line-height: 1.5;
    }

    .sub-label {
      display: block;
      font-size: 9px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--muted, #6f6a6d);
      margin-top: 8px;
      margin-bottom: 2px;
    }

    .sub-label:first-child {
      margin-top: 0;
    }

    .timeline-list {
      display: grid;
      gap: 12px;
    }

    .decision-card {
      padding: 12px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 6px;
      background: var(--warm-light, #f7f5f3);
      font-size: 11px;
    }

    .decision-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6px;
    }

    .decision-time {
      color: var(--muted, #6f6a6d);
      font-size: 10px;
    }

    .decision-reason, .decision-conditions {
      margin: 4px 0 0;
      line-height: 1.4;
    }

    .subjectivities-list {
      display: grid;
      gap: 10px;
    }

    .subjectivity-row {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 10px 12px;
      border: 1px solid var(--border, #eae5df);
      border-radius: 6px;
      background: #ffffff;
      font-size: 11px;
    }

    .sub-details {
      display: flex;
      flex-direction: column;
      gap: 2px;
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

    .error-banner p {
      margin: 2px 0 0;
    }

    @media (max-width: 1024px) {
      .detail-grid {
        grid-template-columns: 1fr;
      }
    }
  `],
})
export class QuoteDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly quoteService = inject(QuoteService);
  private readonly store = inject(Store);

  quoteId = '';
  quote: QuoteResponse | null = null;
  decisions: UnderwritingDecisionResponse[] = [];
  subjectivities: SubjectivityResponse[] = [];

  loading = true;
  submitting = false;
  actionLoading = false;
  errorMessage: string | null = null;

  showDeclineBox = false;
  declineReason = '';
  userRole: string | null = null;

  get isEmployee(): boolean {
    const role = (this.userRole || '').toUpperCase();
    return (
      role === 'UNDERWRITER' ||
      role === 'ADMIN' ||
      role === 'SYSTEM_ADMINISTRATOR' ||
      role === 'RISK_ENGINEER' ||
      role.startsWith('CLAIMS_')
    );
  }

  get canBindQuote(): boolean {
    const role = (this.userRole || '').toUpperCase();
    return (
      (role === 'UNDERWRITER' || role === 'ADMIN' || role === 'SYSTEM_ADMINISTRATOR') &&
      this.quote?.status === 'ACCEPTED'
    );
  }

  ngOnInit(): void {
    this.quoteId = this.route.snapshot.paramMap.get('quoteId') ?? '';
    this.store.select(selectUserRole).subscribe((role) => {
      this.userRole = role;
    });
    this.loadQuoteData();
  }

  onBindQuote(): void {
    if (!this.quoteId) return;

    this.actionLoading = true;
    this.quoteService.bindQuote(this.quoteId).subscribe({
      next: (policy) => {
        this.actionLoading = false;
        this.store.dispatch(
          uiActions.showToast({
            message: `Policy ${policy.policyNumber} bound successfully! Coverage is now active.`,
            kind: 'success',
          })
        );
        this.loadQuoteData();
      },
      error: (err) => {
        this.actionLoading = false;
        const msg = err?.error?.message || err?.message || 'Failed to bind quote.';
        this.store.dispatch(uiActions.showToast({ message: msg, kind: 'error' }));
      },
    });
  }

  loadQuoteData(): void {
    if (!this.quoteId) return;

    this.loading = true;
    this.errorMessage = null;

    this.quoteService.getQuoteById(this.quoteId).subscribe({
      next: (q) => {
        this.quote = q;
        this.loading = false;
        this.loadDecisionsAndSubjectivities();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage =
          err?.error?.message || err?.message || 'Unable to retrieve quote details from the server.';
      },
    });
  }

  private loadDecisionsAndSubjectivities(): void {
    this.quoteService.getUnderwritingDecisions(this.quoteId).subscribe({
      next: (decs) => (this.decisions = decs || []),
      error: () => (this.decisions = []),
    });

    this.quoteService.getSubjectivities(this.quoteId).subscribe({
      next: (subs) => (this.subjectivities = subs || []),
      error: () => (this.subjectivities = []),
    });
  }

  onSubmitToUnderwriting(): void {
    if (!this.quoteId) return;

    this.submitting = true;
    this.quoteService.submitQuote(this.quoteId).subscribe({
      next: (updated) => {
        this.submitting = false;
        this.quote = updated;
        this.store.dispatch(
          uiActions.showToast({
            message: 'Quote submitted to underwriting desk successfully.',
            kind: 'success',
          })
        );
        this.loadDecisionsAndSubjectivities();
      },
      error: (err) => {
        this.submitting = false;
        const msg = err?.error?.message || err?.message || 'Failed to submit quote.';
        this.store.dispatch(uiActions.showToast({ message: msg, kind: 'error' }));
      },
    });
  }

  onAcceptQuote(): void {
    if (!this.quoteId) return;

    this.actionLoading = true;
    this.quoteService.acceptQuote(this.quoteId).subscribe({
      next: (accepted) => {
        this.actionLoading = false;
        this.quote = accepted;
        this.store.dispatch(
          uiActions.showToast({
            message: 'Quote terms accepted! Underwriting will now bind policy.',
            kind: 'success',
          })
        );
      },
      error: (err) => {
        this.actionLoading = false;
        const msg = err?.error?.message || err?.message || 'Failed to accept quote.';
        this.store.dispatch(uiActions.showToast({ message: msg, kind: 'error' }));
      },
    });
  }

  onDeclineQuote(): void {
    if (!this.quoteId || !this.declineReason.trim()) return;

    this.actionLoading = true;
    this.quoteService.declineQuote(this.quoteId, this.declineReason.trim()).subscribe({
      next: (declined) => {
        this.actionLoading = false;
        this.quote = declined;
        this.showDeclineBox = false;
        this.store.dispatch(
          uiActions.showToast({
            message: 'Quote terms declined.',
            kind: 'info',
          })
        );
      },
      error: (err) => {
        this.actionLoading = false;
        const msg = err?.error?.message || err?.message || 'Failed to decline quote.';
        this.store.dispatch(uiActions.showToast({ message: msg, kind: 'error' }));
      },
    });
  }

  formatStatus(status?: string): string {
    return (status || 'UNKNOWN').replace(/_/g, ' ');
  }

  getStatusVariant(status?: string): 'info' | 'success' | 'warning' | 'danger' | 'neutral' {
    const s = (status || '').toUpperCase();
    if (s === 'QUOTED' || s === 'ACCEPTED' || s === 'BOUND' || s === 'ISSUED') return 'success';
    if (s === 'DRAFT') return 'info';
    if (s === 'SUBMITTED' || s === 'IN_REVIEW' || s === 'TRIAGE' || s === 'RISK_ASSESSMENT') return 'warning';
    if (s === 'DECLINED_BY_CUSTOMER' || s === 'DECLINED_BY_INSURER' || s === 'EXPIRED') return 'danger';
    return 'neutral';
  }

  getDecisionVariant(decision?: string): 'info' | 'success' | 'warning' | 'danger' | 'neutral' {
    const d = (decision || '').toUpperCase();
    if (d === 'APPROVED' || d === 'APPROVED_WITH_MODIFIED_TERMS') return 'success';
    if (d === 'MORE_INFORMATION_REQUIRED' || d === 'REFERRED') return 'warning';
    if (d === 'DECLINED') return 'danger';
    return 'neutral';
  }

  getBannerClass(status?: string): string {
    const s = (status || '').toUpperCase();
    if (s === 'DRAFT') return 'banner-draft';
    if (s === 'SUBMITTED' || s === 'IN_REVIEW') return 'banner-review';
    if (s === 'QUOTED') return 'banner-quoted';
    if (s === 'ACCEPTED') return 'banner-accepted';
    if (s === 'BOUND' || s === 'ISSUED') return 'banner-bound';
    if (s.includes('DECLINED')) return 'banner-declined';
    return '';
  }
}
