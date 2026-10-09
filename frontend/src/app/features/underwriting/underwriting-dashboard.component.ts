import { AsyncPipe, DatePipe, NgClass } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject, catchError, map, of, startWith, switchMap, take, tap } from 'rxjs';
import { RiskAssessment } from '../../core/models/underwriting.models';
import { OfferQuoteTermsRequest, OfferedCoverageRequest, QuoteResponse, RecordUnderwritingDecisionRequest, UnderwritingDecisionType } from '../../core/models/quote.models';
import { UnderwritingService } from '../../core/services/underwriting.service';
import { QuoteService } from '../../core/services/quote.service';
import { selectUserId, selectUserRole } from '../../core/store/auth/auth.selectors';

interface UnderwritingQueueState {
  loading: boolean;
  error: string | null;
  items: RiskAssessment[];
}

interface OfferedCoverageForm extends OfferedCoverageRequest {
  coverageName: string;
}

@Component({
  selector: 'is-underwriting-dashboard',
  standalone: true,
  imports: [AsyncPipe, DatePipe, NgClass, RouterLink],
  template: `
    <section class="page-shell">
      <header class="page-header">
        <div>
          <p class="eyebrow">Risk review</p>
          <h1>Assigned assessment queue</h1>
          <p class="description">Assessments assigned to the signed-in underwriting or risk engineering user.</p>
        </div>
        <a class="secondary-link" routerLink="/policy">View quotes and policies <span aria-hidden="true">→</span></a>
      </header>

      @if (queue$ | async; as state) {
        @if (state.loading) {
          <div class="state-card" role="status" aria-live="polite">Loading assigned assessments…</div>
        } @else if (state.error) {
          <div class="state-card error" role="alert">
            <strong>Underwriting queue unavailable</strong>
            <p>{{ state.error }}</p>
            <button type="button" (click)="reload()">Try again</button>
          </div>
        } @else if (!state.items.length) {
          <div class="state-card empty" role="status">
            <strong>No assigned assessments</strong>
            <p>New assessments assigned to this user will appear here.</p>
          </div>
        } @else {
          <section class="summary-grid" aria-label="Assigned assessment summary">
            <article><span>Total assigned</span><strong>{{ state.items.length }}</strong></article>
            <article><span>Awaiting review</span><strong>{{ countByStatus(state.items, 'UNDER_REVIEW') }}</strong></article>
            <article><span>Needs information</span><strong>{{ countByStatus(state.items, 'NEEDS_INFORMATION') }}</strong></article>
          </section>

          @if (selectedAssessment) {
            <section class="review-card" aria-labelledby="review-heading">
              <div class="review-heading">
                <div>
                  <p class="eyebrow">Employee action</p>
                  <h2 id="review-heading">Review {{ selectedAssessment.assessmentNumber || selectedAssessment.assessmentId }}</h2>
                  <p class="review-meta">Quote {{ selectedAssessment.quoteId }} · {{ reviewQuote?.productCode || 'Loading quote details…' }}</p>
                </div>
                <button type="button" class="secondary-button" (click)="clearReview()">Close review</button>
              </div>

              @if (reviewLoading) {
                <div class="inline-state" role="status">Loading the assigned quote…</div>
              } @else if (reviewError) {
                <div class="inline-state error" role="alert">{{ reviewError }}</div>
              } @else if (reviewQuote) {
                @if (canManageQuote) {
                  @if (reviewQuote.status === 'ACCEPTED') {
                    <div class="review-grid" style="grid-template-columns: 1fr;">
                      <div class="review-section">
                        <h3>Customer Accepted Terms — Ready for Binding</h3>
                        <p class="helper">The customer accepted the terms on {{ reviewQuote.acceptedAt | date:'medium' }}. You can now bind this quote into an active commercial policy.</p>
                        <div style="display: flex; gap: 12px; align-items: center; margin-top: 10px;">
                          <button type="button" class="primary-button" [disabled]="actionLoading" (click)="bindPolicy()">
                            {{ actionLoading === 'bind' ? 'Binding policy…' : 'Bind & Issue Policy →' }}
                          </button>
                          <a class="secondary-button" [routerLink]="['/policy', reviewQuote.quoteId]" style="text-decoration: none; display: inline-block;">
                            View full quote specifications
                          </a>
                        </div>
                      </div>
                    </div>
                  } @else if (reviewQuote.status === 'BOUND' || reviewQuote.status === 'ISSUED') {
                    <div class="review-grid" style="grid-template-columns: 1fr;">
                      <div class="review-section">
                        <h3>Coverage Bound & Active</h3>
                        <p class="helper">This quote has been bound into an active policy contract. Commercial coverage is in effect.</p>
                        <div style="display: flex; gap: 12px; align-items: center; margin-top: 10px;">
                          <a class="secondary-button" [routerLink]="['/policy', reviewQuote.quoteId]" style="text-decoration: none; display: inline-block;">
                            View quote details
                          </a>
                          <a class="primary-button" routerLink="/policy" style="text-decoration: none; display: inline-block;">
                            View policy portfolio
                          </a>
                        </div>
                      </div>
                    </div>
                  } @else {
                  <div class="review-grid">
                    <div class="review-section">
                    <h3>Decision</h3>
                    <label>Outcome
                      <select [value]="decision" (change)="setDecision(readValue($event))">
                        <option value="APPROVED">Approved</option>
                        <option value="APPROVED_WITH_MODIFIED_TERMS">Approved with modified terms</option>
                        <option value="MORE_INFORMATION_REQUIRED">More information required</option>
                        <option value="REFERRED">Referred</option>
                        <option value="DECLINED">Declined</option>
                      </select>
                    </label>
                    <label>Decision rationale
                      <textarea rows="3" [value]="decisionReason" (input)="decisionReason = readValue($event)" placeholder="Record the underwriting rationale"></textarea>
                    </label>
                    <label>Authority level
                      <input [value]="authorityLevel" (input)="authorityLevel = readValue($event)" placeholder="For example: STANDARD_UW" />
                    </label>
                    <label>Conditions (optional)
                      <textarea rows="2" [value]="decisionConditions" (input)="decisionConditions = readValue($event)" placeholder="Any conditions attached to the decision"></textarea>
                    </label>
                    <button type="button" class="primary-button" [disabled]="actionLoading || !canSubmitDecision()" (click)="submitDecision()">
                      {{ actionLoading === 'decision' ? 'Saving decision…' : 'Record decision' }}
                    </button>
                  </div>

                  <div class="review-section terms-section">
                    <h3>Offer terms</h3>
                    <p class="helper">Offer terms are available after an approved decision. Values below are loaded from this quote and must be confirmed by the assigned underwriter.</p>
                    <label>Quote expiration
                      <input type="datetime-local" [value]="quoteExpiresAt" (input)="quoteExpiresAt = readValue($event)" />
                    </label>
                    <div class="coverage-forms">
                      @for (coverage of offeredCoverages; track coverage.coverageCode; let index = $index) {
                        <div class="coverage-form">
                          <strong>{{ coverage.coverageName }} <small>{{ coverage.coverageCode }}</small></strong>
                          <div class="field-grid">
                            <label>Limit<input type="number" min="0.01" [value]="coverage.offeredLimit" (input)="updateCoverage(index, 'offeredLimit', $event)" /></label>
                            <label>Deductible<input type="number" min="0" [value]="coverage.offeredDeductible" (input)="updateCoverage(index, 'offeredDeductible', $event)" /></label>
                            <label>Premium<input type="number" min="0.01" [value]="coverage.coveragePremium" (input)="updateCoverage(index, 'coveragePremium', $event)" /></label>
                          </div>
                          <label>Conditions<input [value]="coverage.conditions || ''" (input)="updateCoverage(index, 'conditions', $event)" /></label>
                          <label>Exclusions<input [value]="coverage.exclusions || ''" (input)="updateCoverage(index, 'exclusions', $event)" /></label>
                        </div>
                      }
                    </div>
                    <button type="button" class="primary-button" [disabled]="actionLoading || !canOfferTerms()" (click)="offerTerms()">
                      {{ actionLoading === 'terms' ? 'Publishing terms…' : 'Offer quote terms' }}
                    </button>
                  </div>
                </div>
                }
                } @else {
                  <div class="read-only-note" role="status">
                    <strong>Risk review access</strong>
                    <p>This assessment and quote are available for risk-engineering review. Underwriting decisions and commercial terms can only be published by the assigned underwriter.</p>
                  </div>
                }
                @if (actionMessage) { <div class="success-message" role="status">{{ actionMessage }}</div> }
                @if (actionError) { <div class="inline-state error" role="alert">{{ actionError }}</div> }
              }
            </section>
          }

          <section class="table-card" aria-labelledby="queue-heading">
            <div class="table-heading"><div><p class="eyebrow">Live records</p><h2 id="queue-heading">Assigned assessments</h2></div></div>
            <div class="table-wrap">
              <table>
                <thead><tr><th>Assessment</th><th>Location</th><th>Status</th><th>Risk band</th><th>Updated</th></tr></thead>
                <tbody>
                  @for (assessment of state.items; track assessment.assessmentId) {
                    <tr>
                      <td><strong>{{ assessment.assessmentNumber || assessment.assessmentId }}</strong><small>{{ assessment.assessmentType || 'Risk assessment' }}</small></td>
                      <td>{{ assessment.location || '—' }}</td>
                      <td><span class="status" [ngClass]="statusClass(assessment.status)">{{ formatStatus(assessment.status) }}</span></td>
                      <td>{{ assessment.riskBand || 'Not scored' }}</td>
                      <td>
                        {{ assessment.updatedAt ? (assessment.updatedAt | date:'mediumDate') : '—' }}
                        <button type="button" class="review-link" (click)="selectAssessment(assessment)">Review</button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </section>
        }
      }
    </section>
  `,
  styles: [`
    :host { display: block; padding: 24px; }
    .page-shell { max-width: 1540px; margin: 0 auto; display: grid; gap: 20px; }
    .page-header { display: flex; align-items: end; justify-content: space-between; gap: 20px; }
    .eyebrow { margin: 0 0 7px; color: var(--claret); font-size: 9px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; }
    h1, h2 { margin: 0; color: var(--ink); letter-spacing: -.04em; }
    h1 { font-size: clamp(25px, 2.5vw, 34px); }
    h2 { font-size: 18px; }
    .description { margin: 9px 0 0; color: var(--muted); font-size: 12px; }
    .secondary-link { color: var(--claret); font-size: 12px; font-weight: 700; text-decoration: none; }
    .summary-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
    .summary-grid article, .table-card, .state-card { border: 1px solid var(--border); border-radius: 9px; background: var(--surface); box-shadow: var(--shadow); }
    .summary-grid article { padding: 18px; display: grid; gap: 12px; }
    .summary-grid span { color: var(--muted); font-size: 11px; }
    .summary-grid strong { color: var(--ink); font-size: 26px; }
    .table-card { overflow: hidden; }
    .table-heading { padding: 18px 20px; border-bottom: 1px solid var(--border); }
    .table-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; min-width: 720px; }
    th, td { padding: 14px 20px; text-align: left; border-bottom: 1px solid var(--border); font-size: 12px; color: var(--ink); }
    th { color: var(--muted); font-size: 10px; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; background: var(--surface-alt, #faf8f7); }
    tr:last-child td { border-bottom: 0; }
    td strong, td small { display: block; }
    td small { margin-top: 4px; color: var(--muted); font-size: 10px; }
    .status { display: inline-flex; padding: 5px 8px; border-radius: 999px; font-size: 10px; font-weight: 700; }
    .status-review { color: #875b00; background: #fff4d6; }
    .status-active { color: #1d6270; background: #e2f5f6; }
    .status-complete { color: #28704e; background: #e7f5ed; }
    .status-neutral { color: #5f5a5d; background: #f0eded; }
    .state-card { padding: 30px; color: var(--muted); }
    .state-card strong { color: var(--ink); }
    .state-card p { margin: 8px 0 0; font-size: 12px; }
    .state-card.error { border-color: #e8bcbc; }
    .state-card button { margin-top: 16px; padding: 9px 13px; border: 0; border-radius: 6px; color: white; background: var(--claret); cursor: pointer; }
    .review-card { border: 1px solid var(--border); border-radius: 9px; background: var(--surface); box-shadow: var(--shadow); padding: 20px; }
    .review-heading { display: flex; align-items: start; justify-content: space-between; gap: 16px; padding-bottom: 16px; border-bottom: 1px solid var(--border); }
    .review-meta, .helper { margin: 7px 0 0; color: var(--muted); font-size: 11px; }
    .review-grid { display: grid; grid-template-columns: minmax(0, .85fr) minmax(0, 1.15fr); gap: 24px; padding-top: 18px; }
    .review-section { display: grid; align-content: start; gap: 12px; }
    .review-section h3 { margin: 0; color: var(--ink); font-size: 15px; }
    label { display: grid; gap: 6px; color: var(--muted); font-size: 10px; font-weight: 700; letter-spacing: .04em; text-transform: uppercase; }
    input, select, textarea { width: 100%; box-sizing: border-box; border: 1px solid var(--border); border-radius: 6px; padding: 9px 10px; color: var(--ink); background: var(--surface); font: inherit; font-size: 12px; text-transform: none; letter-spacing: normal; }
    textarea { resize: vertical; }
    .primary-button, .secondary-button { border: 0; border-radius: 6px; padding: 10px 13px; font-size: 11px; font-weight: 700; cursor: pointer; }
    .primary-button { color: #fff; background: var(--claret); }
    .primary-button:disabled { opacity: .5; cursor: not-allowed; }
    .secondary-button { color: var(--claret); background: var(--surface-alt, #faf8f7); border: 1px solid var(--border); }
    .review-link { display: block; margin-top: 7px; border: 0; padding: 0; color: var(--claret); background: transparent; cursor: pointer; font-size: 10px; font-weight: 700; }
    .coverage-forms { display: grid; gap: 10px; }
    .coverage-form { display: grid; gap: 10px; padding: 12px; border: 1px solid var(--border); border-radius: 7px; background: var(--surface-alt, #faf8f7); }
    .coverage-form strong { color: var(--ink); font-size: 12px; }
    .coverage-form small { margin-left: 5px; color: var(--muted); font-weight: 400; }
    .field-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; }
    .inline-state { padding: 14px; color: var(--muted); font-size: 12px; }
    .inline-state.error { color: #9c2d2d; background: #fff7f7; border-radius: 6px; }
    .success-message { margin-top: 16px; padding: 11px; color: #28704e; background: #e7f5ed; border-radius: 6px; font-size: 11px; }
    @media (max-width: 760px) { .page-header { align-items: start; flex-direction: column; } .summary-grid { grid-template-columns: 1fr; } }
    @media (max-width: 920px) { .review-grid { grid-template-columns: 1fr; } .field-grid { grid-template-columns: 1fr; } }
  `],
})

export class UnderwritingDashboardComponent {
  private readonly store = inject(Store);
  private readonly underwriting = inject(UnderwritingService);
  private readonly quotes = inject(QuoteService);
  private readonly reloadTrigger = new Subject<void>();

  selectedAssessment: RiskAssessment | null = null;
  reviewQuote: QuoteResponse | null = null;
  offeredCoverages: OfferedCoverageForm[] = [];
  reviewLoading = false;
  reviewError: string | null = null;
  actionLoading: 'decision' | 'terms' | 'bind' | false = false;
  actionMessage: string | null = null;
  actionError: string | null = null;
  decision: UnderwritingDecisionType = 'APPROVED';
  decisionReason = '';
  authorityLevel = '';
  decisionConditions = '';
  quoteExpiresAt = '';
  userRole: string | null = null;

  readonly queue$ = this.reloadTrigger.pipe(
    startWith(void 0),
    switchMap(() => this.store.select(selectUserId).pipe(take(1))),
    switchMap((userId): Observable<UnderwritingQueueState> => userId
      ? this.store.select(selectUserRole).pipe(
        take(1),
        switchMap((role) => {
          this.userRole = role;
          return this.underwriting.getAssignedQueue(userId, role);
        })
      ).pipe(map((items) => ({ loading: false, error: null, items })))
      : of({ loading: false, error: 'Your signed-in user identity is unavailable.', items: [] })),
    catchError((error: HttpErrorResponse) => of({ loading: false, error: this.errorMessage(error), items: [] })),
    startWith({ loading: true, error: null, items: [] as RiskAssessment[] })
  );

  reload(): void { this.reloadTrigger.next(); }

  get canManageQuote(): boolean {
    return (this.userRole ?? '').toUpperCase() === 'UNDERWRITER';
  }

  selectAssessment(assessment: RiskAssessment): void {
    this.selectedAssessment = assessment;
    this.reviewQuote = null;
    this.offeredCoverages = [];
    this.reviewLoading = true;
    this.reviewError = null;
    this.actionMessage = null;
    this.actionError = null;
    this.quotes.getQuoteById(assessment.quoteId).subscribe({
      next: (quote) => {
        this.reviewQuote = quote;
        this.offeredCoverages = quote.coverages.map((coverage) => ({
          coverageCode: coverage.coverageCode,
          coverageName: coverage.coverageName,
          offeredLimit: coverage.offeredLimit ?? coverage.requestedLimit,
          offeredDeductible: coverage.offeredDeductible ?? coverage.requestedDeductible,
          coveragePremium: coverage.coveragePremium ?? 0,
          conditions: coverage.conditions ?? '',
          exclusions: coverage.exclusions ?? '',
          waitingPeriodDays: coverage.waitingPeriodDays ?? 0,
        }));
        this.reviewLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.reviewLoading = false;
        this.reviewError = this.errorMessage(error);
      },
    });
  }

  clearReview(): void {
    this.selectedAssessment = null;
    this.reviewQuote = null;
    this.offeredCoverages = [];
    this.actionLoading = false;
    this.actionMessage = null;
    this.actionError = null;
  }

  submitDecision(): void {
    if (!this.reviewQuote || !this.canSubmitDecision()) return;
    const request: RecordUnderwritingDecisionRequest = {
      decision: this.decision,
      decisionReason: this.decisionReason.trim(),
      authorityLevel: this.authorityLevel.trim(),
      conditions: this.decisionConditions.trim() || undefined,
    };
    this.actionLoading = 'decision';
    this.actionError = null;
    this.quotes.recordUnderwritingDecision(this.reviewQuote.quoteId, request).subscribe({
      next: () => {
        this.actionLoading = false;
        this.actionMessage = 'Decision recorded. Confirm the final commercial terms before publishing an offer.';
        this.refreshReviewQuote();
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoading = false;
        this.actionError = this.errorMessage(error);
      },
    });
  }

  offerTerms(): void {
    if (!this.reviewQuote || !this.canOfferTerms()) return;
    const request: OfferQuoteTermsRequest = {
      quoteExpiresAt: this.quoteExpiresAt,
      coverages: this.offeredCoverages.map(({ coverageName, ...coverage }) => coverage),
    };
    this.actionLoading = 'terms';
    this.actionError = null;
    this.quotes.offerQuoteTerms(this.reviewQuote.quoteId, request).subscribe({
      next: (quote) => {
        this.actionLoading = false;
        this.reviewQuote = quote;
        this.actionMessage = 'Quote terms published. The policyholder can now review and accept the offer.';
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoading = false;
        this.actionError = this.errorMessage(error);
      },
    });
  }

  bindPolicy(): void {
    if (!this.reviewQuote) return;
    this.actionLoading = 'bind';
    this.actionError = null;
    this.quotes.bindQuote(this.reviewQuote.quoteId).subscribe({
      next: (policy) => {
        this.actionLoading = false;
        this.actionMessage = `Policy ${policy.policyNumber} bound successfully! Coverage is now active.`;
        this.refreshReviewQuote();
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoading = false;
        this.actionError = this.errorMessage(error);
      },
    });
  }

  canSubmitDecision(): boolean {
    return !!this.reviewQuote && !!this.decisionReason.trim() && !!this.authorityLevel.trim() && this.reviewQuote.status === 'IN_REVIEW';
  }

  canOfferTerms(): boolean {
    return !!this.reviewQuote && this.reviewQuote.status === 'IN_REVIEW' && !!this.quoteExpiresAt && this.offeredCoverages.length > 0
      && this.offeredCoverages.every((coverage) => coverage.offeredLimit > 0 && coverage.offeredDeductible >= 0 && coverage.coveragePremium > 0);
  }

  updateCoverage(index: number, field: keyof OfferedCoverageRequest, event: Event): void {
    const value = this.readValue(event);
    const coverage = this.offeredCoverages[index];
    if (!coverage) return;
    if (field === 'offeredLimit' || field === 'offeredDeductible' || field === 'coveragePremium' || field === 'waitingPeriodDays') {
      coverage[field] = Number(value);
    } else {
      coverage[field] = value;
    }
  }

  readValue(event: Event): string { return (event.target as HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement).value; }

  setDecision(value: string): void { this.decision = value as UnderwritingDecisionType; }

  private refreshReviewQuote(): void {
    if (!this.reviewQuote) return;
    this.quotes.getQuoteById(this.reviewQuote.quoteId).subscribe({
      next: (quote) => { this.reviewQuote = quote; },
      error: () => { /* The successful mutation remains visible; the next reload will reconcile the quote. */ },
    });
  }

  countByStatus(items: RiskAssessment[], status: string): number {
    return items.filter((item) => item.status === status).length;
  }

  formatStatus(status: string): string { return status.replaceAll('_', ' ').toLowerCase().replace(/^./, (value) => value.toUpperCase()); }

  statusClass(status: string): string {
    if (status === 'UNDER_REVIEW' || status === 'REFERRED' || status === 'NEEDS_INFORMATION') return 'status-review';
    if (status === 'COMPLETED') return 'status-complete';
    if (status === 'IN_PROGRESS' || status === 'RISK_ENGINEERING_REQUIRED') return 'status-active';
    return 'status-neutral';
  }

  private errorMessage(error: HttpErrorResponse): string {
    if (error.status === 401) return 'Please sign in again to view your assigned assessments.';
    if (error.status === 403) return 'Your role or assignment does not allow access to this queue.';
    return error.error?.message || 'The underwriting service did not return the assigned queue.';
  }
}
