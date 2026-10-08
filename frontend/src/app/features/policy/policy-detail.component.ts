import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { PolicyService } from '../../core/services/policy.service';
import { PolicyResponse } from '../../core/models/policy.models';

// ui-core components
import {
  CardComponent,
  ButtonComponent,
  BadgeComponent,
  SkeletonComponent,
} from 'ui-core';

@Component({
  selector: 'is-policy-detail',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    DatePipe,
    RouterLink,
    CardComponent,
    ButtonComponent,
    BadgeComponent,
    SkeletonComponent,
  ],
  template: `
    <div class="policy-detail-page">
      <header class="page-header">
        <a routerLink="/policy" class="back-link">&larr; Back to quotes & policies</a>
        <div class="header-main">
          <div class="title-row">
            <h1>Policy {{ policy?.policyNumber || policyId }}</h1>
            @if (policy) {
              <is-badge [variant]="getStatusVariant(policy.status)" size="md">
                {{ formatStatus(policy.status) }}
              </is-badge>
            }
          </div>
          <p class="page-description">
            Active commercial insurance contract, binding terms, verified endorsements, and coverage schedules.
          </p>
        </div>
      </header>

      @if (loading) {
        <div class="loading-state" role="status" aria-live="polite">
          <is-skeleton variant="text" width="40%" height="30px" />
          <is-skeleton variant="card" />
        </div>
      } @else if (errorMessage) {
        <div class="error-banner" role="alert">
          <div>
            <strong>Error loading policy:</strong>
            <p>{{ errorMessage }}</p>
          </div>
          <is-button variant="secondary" size="sm" (click)="loadPolicy()">Retry</is-button>
        </div>
      } @else if (policy) {
        <!-- Action bar -->
        <section class="policy-action-banner">
          <div>
            <h3>Active Contract Protection</h3>
            <p>
              Term period from <strong>{{ policy.startDate }}</strong> to <strong>{{ policy.endDate }}</strong>. Total annual written premium:
              <strong>{{ policy.totalPremium | currency:'USD':'symbol':'1.0-0' }}</strong>.
            </p>
          </div>
          <is-button variant="primary" [routerLink]="['/claims/new']" [queryParams]="{ policyId: policy.policyId }">
            Report a Claim (FNOL) &rarr;
          </is-button>
        </section>

        <!-- Main Content -->
        <div class="detail-grid">
          <div class="grid-main">
            <!-- Coverages Schedule -->
            <is-card
              title="Coverage Schedule & Limits"
              subtitle="Bound risk coverages and applied deductibles"
            >
              <div class="table-container">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Coverage Code</th>
                      <th>Coverage Name</th>
                      <th class="text-right">Policy Limit</th>
                      <th class="text-right">Deductible</th>
                      <th class="text-right">Premium</th>
                    </tr>
                  </thead>
                  <tbody>
                    @for (cov of policy.coverages; track cov.policyCoverageId) {
                      <tr>
                        <td><code>{{ cov.coverageCode }}</code></td>
                        <td>
                          <strong>{{ cov.coverageName }}</strong>
                          @if (cov.conditions) {
                            <div class="note-text">Condition: {{ cov.conditions }}</div>
                          }
                          @if (cov.exclusions) {
                            <div class="note-text">Exclusion: {{ cov.exclusions }}</div>
                          }
                        </td>
                        <td class="text-right">
                          <strong class="text-emerald">\${{ cov.limitAmount | number:'1.0-0' }}</strong>
                        </td>
                        <td class="text-right">
                          \${{ cov.deductibleAmount | number:'1.0-0' }}
                        </td>
                        <td class="text-right">
                          <strong>{{ cov.coveragePremium | currency:'USD':'symbol':'1.0-0' }}</strong>
                        </td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            </is-card>
          </div>

          <!-- Sidebar -->
          <aside class="grid-side">
            <is-card title="Contract Details" subtitle="Policy identity & dates">
              <dl class="meta-list">
                <div>
                  <dt>Policy ID</dt>
                  <dd class="code-sm">{{ policy.policyId }}</dd>
                </div>
                <div>
                  <dt>Policy Number</dt>
                  <dd><strong>{{ policy.policyNumber }}</strong></dd>
                </div>
                <div>
                  <dt>Product Line</dt>
                  <dd>{{ policy.productCode }}</dd>
                </div>
                <div>
                  <dt>Customer ID</dt>
                  <dd class="code-sm">{{ policy.customerId }}</dd>
                </div>
                @if (policy.boundAt) {
                  <div>
                    <dt>Bound Date</dt>
                    <dd>{{ policy.boundAt | date:'medium' }}</dd>
                  </div>
                }
                @if (policy.issuedAt) {
                  <div>
                    <dt>Issued Date</dt>
                    <dd>{{ policy.issuedAt | date:'medium' }}</dd>
                  </div>
                }
                <div class="premium-box">
                  <dt>Annual Written Premium</dt>
                  <dd class="premium-text">{{ policy.totalPremium | currency:'USD':'symbol':'1.0-0' }}</dd>
                </div>
              </dl>
            </is-card>

            <is-card title="Claims Assistance" subtitle="Incident support">
              <div class="help-box">
                <p>Need to report property damage, auto collision, or a liability incident under this contract?</p>
                <is-button
                  variant="secondary"
                  size="sm"
                  [routerLink]="['/claims/new']"
                  [queryParams]="{ policyId: policy.policyId }"
                >
                  File First Notice of Loss
                </is-button>
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

    .policy-detail-page {
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
      max-width: 740px;
      line-height: 1.5;
    }

    .policy-action-banner {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 16px;
      padding: 18px 22px;
      background: #ecfdf5;
      border: 1px solid #a7f3d0;
      border-radius: 9px;
      flex-wrap: wrap;
    }

    .policy-action-banner h3 {
      margin: 0 0 2px;
      font-size: 14px;
      font-weight: 700;
      color: #065f46;
    }

    .policy-action-banner p {
      margin: 0;
      font-size: 12px;
      color: #047857;
    }

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
      text-align: left;
    }

    .data-table th {
      padding: 10px 14px;
      font-size: 10px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--muted, #6f6a6d);
      border-bottom: 1px solid var(--border, #eae5df);
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

    .note-text {
      font-size: 10px;
      color: #6b7280;
      margin-top: 2px;
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

    .premium-box {
      background: var(--warm-light, #f7f5f3);
      padding: 12px;
      border-radius: 6px;
      border: 1px solid var(--border, #eae5df);
      margin-top: 6px;
    }

    .premium-text {
      font-size: 22px;
      font-weight: 800;
      color: #065f46;
      margin-top: 4px;
    }

    .help-box {
      font-size: 11px;
      color: var(--muted, #6f6a6d);
      display: grid;
      gap: 10px;
    }

    .help-box p {
      margin: 0;
      line-height: 1.4;
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
export class PolicyDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly policyService = inject(PolicyService);

  policyId = '';
  policy: PolicyResponse | null = null;
  loading = true;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.policyId = this.route.snapshot.paramMap.get('policyId') ?? '';
    this.loadPolicy();
  }

  loadPolicy(): void {
    if (!this.policyId) return;

    this.loading = true;
    this.errorMessage = null;

    this.policyService.getPolicyById(this.policyId).subscribe({
      next: (p) => {
        this.policy = p;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage =
          err?.error?.message || err?.message || 'Unable to retrieve policy details from the server.';
      },
    });
  }

  formatStatus(status?: string): string {
    return (status || 'UNKNOWN').replace(/_/g, ' ');
  }

  getStatusVariant(status?: string): 'info' | 'success' | 'warning' | 'danger' | 'neutral' {
    const s = (status || '').toUpperCase();
    if (s === 'IN_FORCE' || s === 'BOUND') return 'success';
    if (s === 'PENDING_ISSUANCE') return 'warning';
    if (s === 'CANCELLED' || s === 'EXPIRED') return 'danger';
    return 'neutral';
  }
}
