import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ClaimResponse } from '../../models/claim.models';
import { ClaimsService } from '../../services/claims.service';

import { CardComponent, ButtonComponent, BadgeComponent, SkeletonComponent } from 'ui-core';

@Component({
  selector: 'claim-detail',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink, CardComponent, ButtonComponent, BadgeComponent, SkeletonComponent],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div class="header-left">
          <a routerLink="/claims" class="back-link">&larr; Back to claims queue</a>
          <div class="header-title-row">
            <h1>Claim {{ claim?.claimNumber || claimId }}</h1>
            @if (claim) {
              <is-badge [variant]="statusVariant(claim.status)" size="md">
                {{ formatStatus(claim.status) }}
              </is-badge>
            }
          </div>
          <p class="page-description">Detailed overview of incident report, coverage checks, reserves, and adjuster triage.</p>
        </div>
      </header>

      @if (loading) {
        <div class="loading-state" role="status" aria-live="polite">
          <is-skeleton variant="card" />
        </div>
      } @else if (notFound) {
        <is-card title="Claim Not Found" subtitle="The requested claim identifier does not exist in the claims registry.">
          <div class="state-message">
            <p>No claim record matching identifier <code>{{ claimId }}</code> was found.</p>
            <is-button variant="secondary" routerLink="/claims">Return to claims queue</is-button>
          </div>
        </is-card>
      } @else if (error) {
        <div class="error-banner" role="alert">
          <div>
            <strong>Error retrieving claim:</strong>
            <p>{{ error }}</p>
          </div>
          <is-button variant="secondary" size="sm" (click)="loadClaim()">Retry</is-button>
        </div>
      } @else if (claim) {
        <div class="detail-grid">
          <!-- Main Details Card -->
          <article class="detail-card">
            <div class="card-section">
              <span class="section-label">Incident Information</span>
              <dl class="meta-grid">
                <div>
                  <dt>Claim Number</dt>
                  <dd class="font-mono font-bold">{{ claim.claimNumber }}</dd>
                </div>
                <div>
                  <dt>Claim UUID</dt>
                  <dd class="font-mono text-xs">{{ claim.claimId }}</dd>
                </div>
                <div>
                  <dt>Policy ID</dt>
                  <dd class="font-mono text-xs">{{ claim.policyId }}</dd>
                </div>
                <div>
                  <dt>Customer ID</dt>
                  <dd class="font-mono text-xs">{{ claim.customerId }}</dd>
                </div>
                <div>
                  <dt>Incident Date</dt>
                  <dd>{{ claim.incidentDate }}</dd>
                </div>
                <div>
                  <dt>Reported Date</dt>
                  <dd>{{ claim.reportedDate }}</dd>
                </div>
              </dl>
            </div>

            <div class="card-section divider">
              <span class="section-label">Incident Description</span>
              <p class="description-text">{{ claim.description }}</p>
            </div>

            @if (claim.closureReason) {
              <div class="card-section divider">
                <span class="section-label">Closure Reason</span>
                <p class="closure-text">{{ claim.closureReason }}</p>
              </div>
            }
          </article>

          <!-- Financials & Workflow Sidebar Card -->
          <aside class="detail-sidebar">
            <article class="detail-card">
              <div class="card-section">
                <span class="section-label">Loss Valuation</span>
                <div class="amount-hero">
                  <span class="amount-label">Estimated Loss</span>
                  <strong class="amount-value">{{ claim.estimatedLoss | currency:'USD':'symbol':'1.0-0' }}</strong>
                </div>
                @if (claim.payoutAmount !== undefined && claim.payoutAmount !== null) {
                  <div class="amount-sub">
                    <span class="amount-label">Payout Amount</span>
                    <strong class="amount-subvalue">{{ claim.payoutAmount | currency:'USD':'symbol':'1.0-0' }}</strong>
                  </div>
                }
              </div>

              <div class="card-section divider">
                <span class="section-label">Triage & Coverage</span>
                <dl class="sidebar-meta">
                  <div>
                    <dt>Assigned Adjuster</dt>
                    <dd>{{ claim.assignedAdjusterId || 'Unassigned / Automated Queue' }}</dd>
                  </div>
                  <div>
                    <dt>Coverage Decision</dt>
                    <dd>{{ claim.coverageDecision || 'Pending Investigation' }}</dd>
                  </div>
                  <div>
                    <dt>Coverage Confirmed</dt>
                    <dd>{{ claim.coverageConfirmed ? 'Yes' : 'Awaiting Review' }}</dd>
                  </div>
                  <div>
                    <dt>Last Updated</dt>
                    <dd>{{ claim.updatedAt | date:'medium' }}</dd>
                  </div>
                </dl>
              </div>
            </article>
          </aside>
        </div>
      }
    </section>
  `,
  styles: [`
    :host {
      display: block;
      padding: 24px;
    }

    .enterprise-page {
      display: grid;
      gap: 20px;
      max-width: 1380px;
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

    .header-title-row {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .header-title-row h1 {
      margin: 0;
      color: var(--ink, #000000);
      font-size: clamp(24px, 2.4vw, 32px);
      letter-spacing: -0.04em;
      font-weight: 700;
    }

    .page-description {
      max-width: 730px;
      margin: 6px 0 0;
      color: var(--muted, #6f6a6d);
      font-size: 12px;
      line-height: 1.6;
    }

    .detail-grid {
      display: grid;
      grid-template-columns: 1.4fr 0.8fr;
      gap: 20px;
      align-items: start;
    }

    .detail-card {
      background: #ffffff;
      border: 1px solid var(--border, #eae5df);
      border-radius: 10px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
      overflow: hidden;
    }

    .card-section {
      padding: 20px;
    }
    .card-section.divider {
      border-top: 1px solid var(--border, #eae5df);
    }

    .section-label {
      display: block;
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: var(--claret, #75013f);
      margin-bottom: 12px;
    }

    .meta-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 12px 18px;
      font-size: 11px;
    }
    .meta-grid dt {
      color: var(--muted, #6f6a6d);
      font-size: 10px;
      margin-bottom: 2px;
    }
    .meta-grid dd {
      margin: 0;
      color: var(--ink, #000000);
      font-weight: 600;
      word-break: break-all;
    }

    .description-text, .closure-text {
      margin: 0;
      font-size: 12px;
      line-height: 1.65;
      color: #312e30;
    }

    .amount-hero {
      background: var(--warm-light, #f7f5f3);
      padding: 14px 16px;
      border-radius: 8px;
      border: 1px solid var(--border, #eae5df);
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .amount-label {
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: #7d777c;
    }

    .amount-value {
      font-size: 24px;
      font-weight: 800;
      color: var(--ink, #000000);
    }

    .amount-sub {
      margin-top: 10px;
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      font-size: 11px;
      padding: 0 4px;
    }
    .amount-subvalue {
      font-weight: 700;
      color: #176b45;
    }

    .sidebar-meta {
      display: flex;
      flex-direction: column;
      gap: 12px;
      font-size: 11px;
    }
    .sidebar-meta dt {
      color: var(--muted, #6f6a6d);
      font-size: 10px;
      margin-bottom: 2px;
    }
    .sidebar-meta dd {
      margin: 0;
      font-weight: 600;
      color: var(--ink, #000000);
      word-break: break-all;
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

    .state-message {
      display: flex;
      flex-direction: column;
      gap: 12px;
      align-items: flex-start;
      padding: 12px 0;
    }

    @media (max-width: 900px) {
      .detail-grid {
        grid-template-columns: 1fr;
      }
      .meta-grid {
        grid-template-columns: 1fr;
      }
    }
  `],
})
export class ClaimDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly claimsService = inject(ClaimsService);

  claimId = '';
  claim: ClaimResponse | null = null;
  loading = false;
  error: string | null = null;
  notFound = false;

  ngOnInit(): void {
    this.claimId = this.route.snapshot.paramMap.get('claimId') ?? '';
    this.loadClaim();
  }

  loadClaim(): void {
    if (!this.claimId) return;

    this.loading = true;
    this.error = null;
    this.notFound = false;

    this.claimsService.getClaim(this.claimId).subscribe({
      next: (data) => {
        this.claim = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        if (err?.status === 404) {
          this.notFound = true;
        } else if (err?.status === 403) {
          this.error = 'Access denied: You do not have permission to view this claim.';
        } else if (err?.status === 401) {
          this.error = 'Session expired. Please sign in again.';
        } else {
          this.error = err?.error?.message || err?.message || 'Failed to retrieve claim details from the backend.';
        }
      },
    });
  }

  statusVariant(status?: string): 'info' | 'success' | 'warning' | 'danger' | 'neutral' {
    const s = (status || '').toUpperCase();
    if (s === 'APPROVED' || s === 'SETTLED' || s === 'PAID') return 'success';
    if (s === 'RESERVED' || s === 'COVERAGE_REVIEW') return 'warning';
    if (s === 'DENIED') return 'danger';
    if (s === 'OPEN' || s === 'FNOL_RECEIVED') return 'info';
    return 'neutral';
  }

  formatStatus(status?: string): string {
    return (status || 'UNKNOWN').replace(/_/g, ' ');
  }
}
