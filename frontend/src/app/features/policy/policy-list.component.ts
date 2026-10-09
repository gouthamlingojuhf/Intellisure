import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { QuoteService } from '../../core/services/quote.service';
import { PolicyService } from '../../core/services/policy.service';
import { CustomerProfileService } from '../../core/services/customer-profile.service';
import { QuoteResponse } from '../../core/models/quote.models';
import { PolicyResponse } from '../../core/models/policy.models';

// ui-core components
import {
  CardComponent,
  ButtonComponent,
  BadgeComponent,
  SkeletonComponent,
  EmptyStateComponent,
} from 'ui-core';

@Component({
  selector: 'is-policy-list',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    RouterLink,
    CardComponent,
    ButtonComponent,
    BadgeComponent,
    SkeletonComponent,
    EmptyStateComponent,
  ],
  template: `
    <div class="policy-list-page">
      <header class="page-header">
        <div class="header-main">
          <p class="page-eyebrow">Portfolio Management</p>
          <div class="title-row">
            <h1>Quotes & Policies</h1>
            @if (!loading) {
              <is-badge variant="info" size="md">
                {{ policies.length }} Policies · {{ quotes.length }} Quotes
              </is-badge>
            }
          </div>
          <p class="page-description">
            Manage your commercial insurance contracts, track quotes in underwriting review, and initiate new coverage applications.
          </p>
        </div>
        <div class="header-actions">
          <is-button variant="primary" routerLink="/policy/quotes/new">
            Request New Quote &rarr;
          </is-button>
        </div>
      </header>

      @if (loading) {
        <div class="loading-state" role="status" aria-live="polite">
          <is-skeleton variant="text" width="30%" height="24px" />
          <is-skeleton variant="card" />
          <is-skeleton variant="card" />
        </div>
      } @else if (profileMissing) {
        <div class="notice-card warning">
          <div class="notice-icon" aria-hidden="true">🏢</div>
          <div>
            <h3>Business Profile Incomplete</h3>
            <p>Setup your legal business details and address before creating and viewing commercial quotes.</p>
          </div>
          <is-button variant="primary" size="sm" routerLink="/profile">Complete Profile &rarr;</is-button>
        </div>
      } @else {
        <!-- Card 1: Bound & Active Policies -->
        <is-card
          title="In-Force Insurance Policies"
          subtitle="Contractually bound commercial policies protecting your operations"
        >
          @if (policies.length === 0) {
            <is-empty-state
              title="No active policies"
              description="You do not have any active or bound policies. Once a quote is accepted and issued, it will appear here."
              icon="📋"
              actionLabel="Create Insurance Quote"
              (action)="navigate('/policy/quotes/new')"
            />
          } @else {
            <div class="table-container">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Policy Number</th>
                    <th>Product</th>
                    <th>Status</th>
                    <th>Annual Premium</th>
                    <th>Term Dates</th>
                    <th class="text-right">Action</th>
                  </tr>
                </thead>
                <tbody>
                  @for (policy of policies; track policy.policyId) {
                    <tr>
                      <td>
                        <strong>{{ policy.policyNumber }}</strong>
                      </td>
                      <td>
                        <span class="code-pill">{{ policy.productCode }}</span>
                      </td>
                      <td>
                        <is-badge [variant]="getPolicyStatusVariant(policy.status)" size="sm">
                          {{ formatStatus(policy.status) }}
                        </is-badge>
                      </td>
                      <td>
                        <strong>{{ policy.totalPremium | currency:'INR':'symbol':'1.0-0' }}</strong>
                      </td>
                      <td>
                        <small class="text-muted">{{ policy.startDate }} to {{ policy.endDate }}</small>
                      </td>
                      <td class="text-right">
                        <is-button
                          variant="text"
                          size="sm"
                          (click)="navigate('/policy/' + policy.policyId)"
                        >
                          View Contract &rarr;
                        </is-button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </is-card>

        <!-- Card 2: Commercial Quotes Queue -->
        <is-card
          title="Quotes & Submissions Queue"
          subtitle="Applications in progress, underwriting reviews, and offered terms"
        >
          @if (quotes.length === 0) {
            <is-empty-state
              title="No quotes found"
              description="Submit a commercial insurance application to receive custom limits and pricing."
              icon="📝"
              actionLabel="Start a Quote"
              (action)="navigate('/policy/quotes/new')"
            />
          } @else {
            <div class="table-container">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Quote Number</th>
                    <th>Product</th>
                    <th>Status</th>
                    <th>Effective Date</th>
                    <th>Total Premium</th>
                    <th class="text-right">Action</th>
                  </tr>
                </thead>
                <tbody>
                  @for (quote of quotes; track quote.quoteId) {
                    <tr>
                      <td>
                        <strong>{{ quote.quoteNumber }}</strong>
                      </td>
                      <td>
                        <span class="code-pill">{{ quote.productCode }}</span>
                      </td>
                      <td>
                        <is-badge [variant]="getQuoteStatusVariant(quote.status)" size="sm">
                          {{ formatStatus(quote.status) }}
                        </is-badge>
                      </td>
                      <td>{{ quote.requestedEffectiveDate }}</td>
                      <td>
                        @if (quote.totalPremium !== null && quote.totalPremium !== undefined) {
                          <strong>{{ quote.totalPremium | currency:'INR':'symbol':'1.0-0' }}</strong>
                        } @else {
                          <span class="text-muted">Awaiting Rating</span>
                        }
                      </td>
                      <td class="text-right">
                        <is-button
                          variant="text"
                          size="sm"
                          (click)="navigate('/policy/quotes/' + quote.quoteId)"
                        >
                          Details &rarr;
                        </is-button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </is-card>
      }
    </div>
  `,
  styles: [`
    :host {
      display: block;
      padding: 24px;
    }

    .policy-list-page {
      display: grid;
      gap: 22px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      gap: 16px;
      flex-wrap: wrap;
    }

    .page-eyebrow {
      margin: 0 0 6px;
      color: var(--claret, #75013f);
      font-size: 10px;
      font-weight: 700;
      letter-spacing: 0.14em;
      text-transform: uppercase;
    }

    .title-row {
      display: flex;
      align-items: center;
      gap: 14px;
      flex-wrap: wrap;
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

    .notice-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px 20px;
      border-radius: 8px;
      background: #fff8eb;
      border: 1px solid #f9dba5;
    }

    .notice-card h3 {
      margin: 0 0 2px;
      font-size: 13px;
      color: #92400e;
    }

    .notice-card p {
      margin: 0;
      font-size: 11px;
      color: #78350f;
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

    .data-table tr:hover td {
      background: var(--warm-light, #f7f5f3);
    }

    .text-right {
      text-align: right;
    }

    .text-muted {
      color: var(--muted, #6f6a6d);
      font-size: 11px;
    }

    .code-pill {
      font-size: 10px;
      font-family: monospace;
      padding: 2px 6px;
      background: var(--warm-light, #f7f5f3);
      border: 1px solid var(--border, #eae5df);
      border-radius: 4px;
    }

    .loading-state {
      display: grid;
      gap: 16px;
    }
  `],
})
export class PolicyListComponent implements OnInit {
  private readonly quoteService = inject(QuoteService);
  private readonly policyService = inject(PolicyService);
  private readonly profileService = inject(CustomerProfileService);
  private readonly router = inject(Router);

  customerId: string | null = null;
  loading = true;
  profileMissing = false;

  quotes: QuoteResponse[] = [];
  policies: PolicyResponse[] = [];

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.profileMissing = false;

    this.profileService.getProfile().subscribe({
      next: (profile) => {
        if (!profile?.customerId) {
          this.profileMissing = true;
          this.loading = false;
          return;
        }

        this.customerId = profile.customerId;
        forkJoin({
          quotes: this.quoteService.getQuotesByCustomerId(this.customerId).pipe(catchError(() => of([]))),
          policies: this.policyService.getPoliciesByCustomerId(this.customerId).pipe(catchError(() => of([]))),
        }).subscribe({
          next: ({ quotes, policies }) => {
            this.quotes = quotes || [];
            this.policies = policies || [];
            this.loading = false;
          },
          error: () => {
            this.loading = false;
          },
        });
      },
      error: () => {
        this.profileMissing = true;
        this.loading = false;
      },
    });
  }

  navigate(path: string): void {
    this.router.navigateByUrl(path);
  }

  formatStatus(status?: string): string {
    return (status || 'UNKNOWN').replace(/_/g, ' ');
  }

  getQuoteStatusVariant(status?: string): 'info' | 'success' | 'warning' | 'danger' | 'neutral' {
    const s = (status || '').toUpperCase();
    if (s === 'QUOTED' || s === 'ACCEPTED' || s === 'BOUND' || s === 'ISSUED') return 'success';
    if (s === 'DRAFT') return 'info';
    if (s === 'SUBMITTED' || s === 'IN_REVIEW' || s === 'TRIAGE' || s === 'RISK_ASSESSMENT') return 'warning';
    if (s === 'DECLINED_BY_CUSTOMER' || s === 'DECLINED_BY_INSURER' || s === 'EXPIRED') return 'danger';
    return 'neutral';
  }

  getPolicyStatusVariant(status?: string): 'info' | 'success' | 'warning' | 'danger' | 'neutral' {
    const s = (status || '').toUpperCase();
    if (s === 'IN_FORCE' || s === 'BOUND') return 'success';
    if (s === 'PENDING_ISSUANCE') return 'warning';
    if (s === 'CANCELLED' || s === 'EXPIRED') return 'danger';
    return 'neutral';
  }
}
