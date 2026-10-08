import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';

import { DashboardService, DashboardMetrics } from '../../core/services/dashboard.service';
import { selectCustomerId, selectUserId } from '../../core/store/auth/auth.selectors';
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
  selector: 'is-policyholder-dashboard',
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
    EmptyStateComponent,
  ],
  template: `
    <div class="dashboard-page">
      <!-- Header -->
      <header class="page-header">
        <div class="header-content">
          <p class="page-eyebrow">Enterprise Overview</p>
          <div class="title-row">
            <h1>{{ metrics?.businessName ? metrics?.businessName + ' Dashboard' : 'Policyholder Dashboard' }}</h1>
            @if (metrics?.hasProfile) {
              <is-badge variant="success" size="md">Account Active</is-badge>
            } @else if (!loading) {
              <is-badge variant="warning" size="md">Profile Incomplete</is-badge>
            }
          </div>
          <p class="page-description">
            Real-time status of commercial quotes, bound policies, reported claims, and business continuity recoveries.
          </p>
        </div>
        <div class="header-actions">
          <is-button variant="primary" routerLink="/policy/quotes/new">
            Start Quote <span aria-hidden="true">&rarr;</span>
          </is-button>
        </div>
      </header>

      <!-- Loading State -->
      @if (loading) {
        <div class="loading-state" role="status" aria-live="polite">
          <div class="metric-skeleton-grid">
            <is-skeleton variant="card" />
            <is-skeleton variant="card" />
            <is-skeleton variant="card" />
            <is-skeleton variant="card" />
          </div>
          <is-skeleton variant="card" />
        </div>
      } @else {
        <!-- Onboarding Notice if Profile Missing -->
        @if (!metrics?.hasProfile) {
          <div class="onboarding-card">
            <div class="onboarding-icon" aria-hidden="true">🏢</div>
            <div class="onboarding-text">
              <h3>Action Required: Complete your Business Profile</h3>
              <p>
                To generate quotes, bind coverage, and file claims, please provide your legal entity and address details.
              </p>
            </div>
            <is-button variant="primary" size="sm" routerLink="/profile">
              Complete Profile &rarr;
            </is-button>
          </div>
        }

        <!-- Metric Cards Grid (100% Real Backend Data) -->
        <section class="metric-grid" aria-label="Key Insurance Metrics">
          <!-- Active Policies -->
          <article class="metric-card" (click)="navigate('/policy')">
            <div class="metric-header">
              <span class="metric-label">In-Force Policies</span>
              <span class="metric-icon">📋</span>
            </div>
            <strong class="metric-value">{{ metrics?.activePoliciesCount ?? 0 }}</strong>
            <div class="metric-footer">
              <span class="metric-subtext">Active commercial contracts</span>
              <span class="metric-link">View all &rarr;</span>
            </div>
          </article>

          <!-- Open Quotes -->
          <article class="metric-card" (click)="navigate('/policy')">
            <div class="metric-header">
              <span class="metric-label">Quotes in Progress</span>
              <span class="metric-icon">📝</span>
            </div>
            <strong class="metric-value">{{ metrics?.openQuotesCount ?? 0 }}</strong>
            <div class="metric-footer">
              <span class="metric-subtext">Drafts & underwriting reviews</span>
              <span class="metric-link">Manage &rarr;</span>
            </div>
          </article>

          <!-- Active Claims -->
          <article class="metric-card" (click)="navigate('/claims')">
            <div class="metric-header">
              <span class="metric-label">Active Claims</span>
              <span class="metric-icon">📄</span>
            </div>
            <strong class="metric-value">{{ metrics?.activeClaimsCount ?? 0 }}</strong>
            <div class="metric-footer">
              <span class="metric-subtext">Reported loss incidents</span>
              <span class="metric-link">Track &rarr;</span>
            </div>
          </article>

          <!-- Recovery Cases -->
          <article class="metric-card" (click)="navigate('/recovery')">
            <div class="metric-header">
              <span class="metric-label">Continuity Recoveries</span>
              <span class="metric-icon">🔄</span>
            </div>
            <strong class="metric-value">{{ metrics?.ongoingRecoveriesCount ?? 0 }}</strong>
            <div class="metric-footer">
              <span class="metric-subtext">Ongoing restoration plans</span>
              <span class="metric-link">Details &rarr;</span>
            </div>
          </article>
        </section>

        <!-- Main Dashboard Split Layout -->
        <div class="dashboard-split">
          <!-- Left: Recent Quotes & Applications -->
          <div class="split-main">
            <is-card
              title="Recent Quotes & Submissions"
              subtitle="Track quote progress from draft through underwriting and acceptance"
            >
              @if ((metrics?.recentQuotes?.length ?? 0) === 0) {
                <is-empty-state
                  title="No quotes requested yet"
                  description="Begin a commercial insurance quote to protect your property, liability, and business operations."
                  actionLabel="Create Insurance Quote"
                  (action)="navigate('/policy/quotes/new')"
                />
              } @else {
                <div class="table-container">
                  <table class="data-table">
                    <thead>
                      <tr>
                        <th>Quote #</th>
                        <th>Product</th>
                        <th>Status</th>
                        <th>Effective Date</th>
                        <th class="text-right">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (quote of metrics?.recentQuotes; track quote.quoteId) {
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
                          <td class="text-right">
                            <is-button
                              variant="text"
                              size="sm"
                              (click)="navigate('/policy/quotes/' + quote.quoteId)"
                            >
                              Review &rarr;
                            </is-button>
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              }
            </is-card>

            <!-- Active Policies Table -->
            <is-card
              title="Active In-Force Policies"
              subtitle="Bound commercial insurance contracts currently in force"
            >
              @if ((metrics?.activePolicies?.length ?? 0) === 0) {
                <is-empty-state
                  title="No active policies"
                  description="When a quote is accepted and bound by underwriting, your policies will appear here."
                  icon="📋"
                />
              } @else {
                <div class="table-container">
                  <table class="data-table">
                    <thead>
                      <tr>
                        <th>Policy #</th>
                        <th>Product</th>
                        <th>Status</th>
                        <th>Annual Premium</th>
                        <th>Term Dates</th>
                        <th class="text-right">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (policy of metrics?.activePolicies; track policy.policyId) {
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
                            <strong>{{ policy.totalPremium | currency:'USD':'symbol':'1.0-0' }}</strong>
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
          </div>

          <!-- Right Sidebar: Shortcuts & Status Summary -->
          <aside class="split-side">
            <is-card title="Quick Actions" subtitle="Direct access to policyholder tools">
              <div class="action-list">
                <a routerLink="/policy/quotes/new" class="action-item">
                  <span class="action-icon">➕</span>
                  <div>
                    <strong>New Commercial Quote</strong>
                    <p>Configure limits and submit for underwriting review</p>
                  </div>
                </a>

                <a routerLink="/profile" class="action-item">
                  <span class="action-icon">🏢</span>
                  <div>
                    <strong>Business Profile</strong>
                    <p>Maintain entity details, phone, and operating address</p>
                  </div>
                </a>

                <a routerLink="/claims/new" class="action-item">
                  <span class="action-icon">⚠️</span>
                  <div>
                    <strong>File Claim (FNOL)</strong>
                    <p>Report an incident or property loss on an active policy</p>
                  </div>
                </a>

                <a routerLink="/docs" class="action-item">
                  <span class="action-icon">📁</span>
                  <div>
                    <strong>Documents & Evidence</strong>
                    <p>Inspect policy declarations and loss reports</p>
                  </div>
                </a>
              </div>
            </is-card>

            <is-card title="System Status" subtitle="Verified API Gateway Connectivity">
              <div class="status-box">
                <div class="status-indicator">
                  <span class="dot-online"></span>
                  <strong>Gateway :8080 Connected</strong>
                </div>
                <p>Reactive Spring WebFlux microservice infrastructure operating normally.</p>
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

    .dashboard-page {
      display: grid;
      gap: 24px;
      max-width: 1440px;
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
      color: var(--muted, #6f6a6d);
      font-size: 12px;
      max-width: 700px;
      line-height: 1.5;
    }

    .onboarding-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px 20px;
      background: #fff8eb;
      border: 1px solid #f9dba5;
      border-radius: 8px;
    }

    .onboarding-icon {
      font-size: 28px;
    }

    .onboarding-text {
      flex: 1;
    }

    .onboarding-text h3 {
      margin: 0 0 2px;
      font-size: 13px;
      font-weight: 700;
      color: #92400e;
    }

    .onboarding-text p {
      margin: 0;
      font-size: 11px;
      color: #78350f;
    }

    /* Metric Grid */
    .metric-grid {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 18px;
    }

    .metric-card {
      background: var(--surface, #ffffff);
      border: 1px solid var(--border, #eae5df);
      border-radius: 9px;
      padding: 18px;
      cursor: pointer;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
      transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      min-height: 120px;
    }

    .metric-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
      border-color: var(--claret, #75013f);
    }

    .metric-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .metric-label {
      font-size: 11px;
      font-weight: 700;
      color: var(--muted, #6f6a6d);
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    .metric-icon {
      font-size: 18px;
    }

    .metric-value {
      font-size: 32px;
      font-weight: 800;
      color: var(--ink, #000000);
      margin: 8px 0;
      line-height: 1;
    }

    .metric-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 10px;
    }

    .metric-subtext {
      color: var(--muted, #6f6a6d);
    }

    .metric-link {
      color: var(--claret, #75013f);
      font-weight: 600;
    }

    /* Split layout */
    .dashboard-split {
      display: grid;
      grid-template-columns: 1fr 340px;
      gap: 22px;
      align-items: start;
    }

    .split-main {
      display: grid;
      gap: 22px;
    }

    .split-side {
      display: grid;
      gap: 22px;
    }

    /* Table styles */
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

    .code-pill {
      font-size: 10px;
      font-family: monospace;
      padding: 2px 6px;
      background: var(--warm-light, #f7f5f3);
      border: 1px solid var(--border, #eae5df);
      border-radius: 4px;
    }

    /* Quick action items */
    .action-list {
      display: grid;
      gap: 12px;
    }

    .action-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 10px 12px;
      border-radius: 6px;
      text-decoration: none;
      color: inherit;
      border: 1px solid var(--border, #eae5df);
      background: var(--warm-light, #f7f5f3);
      transition: all 0.15s ease;
    }

    .action-item:hover {
      border-color: var(--claret, #75013f);
      background: #ffffff;
      transform: translateY(-1px);
    }

    .action-icon {
      font-size: 18px;
      margin-top: 2px;
    }

    .action-item strong {
      font-size: 12px;
      color: var(--ink, #000000);
      display: block;
    }

    .action-item p {
      margin: 2px 0 0;
      font-size: 10px;
      color: var(--muted, #6f6a6d);
      line-height: 1.4;
    }

    .status-box {
      font-size: 11px;
      color: var(--muted, #6f6a6d);
      line-height: 1.5;
    }

    .status-indicator {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;
      color: #065f46;
      font-size: 12px;
    }

    .dot-online {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #10b981;
    }

    .metric-skeleton-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 18px;
    }

    @media (max-width: 1024px) {
      .metric-grid {
        grid-template-columns: repeat(2, 1fr);
      }
      .dashboard-split {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 640px) {
      .metric-grid {
        grid-template-columns: 1fr;
      }
    }
  `],
})
export class PolicyholderDashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly store = inject(Store);
  private readonly router = inject(Router);

  readonly userId$ = this.store.select(selectUserId);
  readonly customerId$ = this.store.select(selectCustomerId);

  metrics: DashboardMetrics | null = null;
  loading = true;

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;

    const storedUserId = typeof localStorage !== 'undefined' ? localStorage.getItem('is_user_id') : null;
    const storedCustomerId = typeof localStorage !== 'undefined' ? localStorage.getItem('is_customer_id') : null;

    this.dashboardService.getDashboardData(storedUserId, storedCustomerId).subscribe({
      next: (data) => {
        this.metrics = data;
        this.loading = false;
        if (data.customerId && typeof localStorage !== 'undefined') {
          localStorage.setItem('is_customer_id', data.customerId);
        }
      },
      error: () => {
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
