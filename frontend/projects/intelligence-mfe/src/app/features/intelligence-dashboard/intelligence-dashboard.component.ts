import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CardComponent, ButtonComponent, SkeletonComponent } from 'ui-core';
import { AnalyticsApiService } from '../../services/analytics-api.service';
import { ExecutiveDashboardSummary } from '../../models/analytics.models';

@Component({
  selector: 'intelligence-dashboard',
  standalone: true,
  imports: [RouterLink, CardComponent, ButtonComponent, SkeletonComponent],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div>
          <p class="page-eyebrow">Executive Intelligence</p>
          <h1>Analytics & risk dashboard</h1>
          <p class="page-description">Persisted portfolio metrics from the Analytics service.</p>
        </div>
        <div class="header-actions">
          <is-button variant="secondary" routerLink="overview">Portfolio overview</is-button>
          <is-button variant="primary" routerLink="alerts">Risk alerts</is-button>
        </div>
      </header>

      @if (loading) {
        <div class="metric-grid" aria-label="Loading analytics">
          <is-skeleton variant="card" />
          <is-skeleton variant="card" />
          <is-skeleton variant="card" />
          <is-skeleton variant="card" />
        </div>
      } @else if (errorMessage) {
        <is-card title="Analytics unavailable" subtitle="No persisted executive summary could be loaded from the Analytics service.">
          <p class="empty-copy">{{ errorMessage }}</p>
        </is-card>
      } @else if (summary) {
        <div class="metric-grid" aria-label="Key performance indicators">
          <article class="metric-card"><p>Loss ratio</p><strong>{{ summary.lossRatioPercentage }}%</strong><small>Persisted summary</small></article>
          <article class="metric-card"><p>Claims frequency</p><strong>{{ summary.claimsFrequency }}%</strong><small>Persisted summary</small></article>
          <article class="metric-card"><p>Net subrogation yield</p><strong>{{ formatCurrency(summary.netSubrogationYield) }}</strong><small>Persisted summary</small></article>
          <article class="metric-card"><p>Open claims</p><strong>{{ summary.openClaimsCount }}</strong><small>As of {{ formatDate(summary.calculatedAt) }}</small></article>
        </div>

        <div class="dashboard-grid">
          <is-card title="Portfolio summary" subtitle="Persisted operational totals for the latest reporting period.">
            <dl class="summary-list">
              <div><dt>Total written premium</dt><dd>{{ formatCurrency(summary.totalWrittenPremium) }}</dd></div>
              <div><dt>Total earned premium</dt><dd>{{ formatCurrency(summary.totalEarnedPremium) }}</dd></div>
              <div><dt>Total incurred losses</dt><dd>{{ formatCurrency(summary.totalIncurredLosses) }}</dd></div>
              <div><dt>Policies / claims</dt><dd>{{ summary.activePolicyCount }} / {{ summary.totalClaimsFiled }}</dd></div>
              <div><dt>Closed claims</dt><dd>{{ summary.closedClaimsCount }}</dd></div>
            </dl>
          </is-card>
          <is-card title="Active alerts" subtitle="Alert feed is not exposed by the current Analytics API contract.">
            <p class="empty-copy">No alert records are available.</p>
          </is-card>
        </div>
      } @else {
        <is-card title="No analytics data" subtitle="The Analytics service has not published a summary yet.">
          <p class="empty-copy">Once a persisted summary is available, this dashboard will display it here.</p>
        </is-card>
      }
    </section>
  `,
  styles: [`
    :host { display: block; padding: 24px; }
    .enterprise-page { display: grid; gap: 20px; max-width: 1540px; margin: 0 auto; }
    .page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; }
    .header-actions { display: flex; gap: 8px; }
    .page-eyebrow { margin: 0 0 7px; color: var(--claret); font-size: 9px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; }
    h1 { margin: 0; color: var(--ink); font-size: clamp(25px, 2.5vw, 34px); line-height: 1.08; }
    .page-description { max-width: 730px; margin: 9px 0 0; color: var(--muted); font-size: 12px; line-height: 1.6; }
    .metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
    .metric-card { min-height: 132px; padding: 18px; border: 1px solid var(--border); border-radius: 9px; background: var(--surface); box-shadow: var(--shadow); }
    .metric-card p { margin: 0; color: var(--muted); font-size: 10px; font-weight: 600; }
    .metric-card strong { display: block; margin-top: 18px; color: var(--ink); font-size: 25px; }
    .metric-card small { display: block; margin-top: 12px; color: var(--muted); font-size: 9px; }
    .dashboard-grid { display: grid; grid-template-columns: minmax(0, 1.4fr) minmax(280px, .6fr); gap: 16px; }
    .summary-list { display: grid; gap: 0; margin: 0; }
    .summary-list div { display: flex; justify-content: space-between; gap: 16px; padding: 13px 0; border-bottom: 1px solid var(--border); font-size: 11px; }
    .summary-list div:last-child { border-bottom: 0; }
    dt { color: var(--muted); } dd { margin: 0; color: var(--ink); font-weight: 700; }
    .empty-copy { margin: 0; color: var(--muted); font-size: 12px; line-height: 1.6; }
    @media (max-width: 900px) { .metric-grid, .dashboard-grid { grid-template-columns: 1fr 1fr; } .dashboard-grid { grid-template-columns: 1fr; } }
    @media (max-width: 620px) { .metric-grid { grid-template-columns: 1fr; } .page-header { align-items: flex-start; flex-direction: column; } .header-actions { width: 100%; } }
  `],
})
export class IntelligenceDashboardComponent implements OnInit {
  private readonly analyticsApi = inject(AnalyticsApiService);

  summary: ExecutiveDashboardSummary | null = null;
  loading = true;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.analyticsApi.getDashboardSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error?.status === 404
          ? 'The Analytics service has no persisted summary for the selected period.'
          : 'The Analytics service could not be reached.';
      },
    });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(value);
  }

  formatDate(value: string): string {
    return new Date(value).toLocaleString();
  }
}
