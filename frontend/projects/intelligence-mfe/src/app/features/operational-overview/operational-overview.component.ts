import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AnalyticsApiService } from '../../services/analytics-api.service';
import { ExecutiveDashboardSummary } from '../../models/analytics.models';

@Component({
  selector: 'operational-overview',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="card page">
      <div class="heading"><div><p class="eyebrow">Portfolio overview</p><h2>Operational snapshot</h2></div><a routerLink="/analytics" class="btn-secondary">Back to dashboard</a></div>
      @if (loading) {
        <p>Loading persisted analytics…</p>
      } @else if (summary) {
        <div class="grid">
          <div class="card"><h3>Loss ratio</h3><p>Latest persisted portfolio ratio.</p><strong>{{ summary.lossRatioPercentage }}%</strong></div>
          <div class="card"><h3>Premium earned</h3><p>Latest persisted earned premium.</p><strong>{{ formatCurrency(summary.totalEarnedPremium) }}</strong></div>
          <div class="card"><h3>Open claims</h3><p>Open claims in the latest summary.</p><strong>{{ summary.openClaimsCount }}</strong></div>
        </div>
      } @else {
        <p>No persisted analytics summary is available.</p>
      }
    </section>
  `,
  styles: [`
    .page { max-width: 1000px; margin: 1.5rem auto; }
    .heading { display: flex; justify-content: space-between; gap: 1rem; align-items: center; flex-wrap: wrap; margin-bottom: 1rem; }
    .eyebrow { font-size: .7rem; letter-spacing: .18em; text-transform: uppercase; color: #1d4ed8; font-weight: 700; margin: 0; }
    h2 { margin: .35rem 0 0; font-size: 1.8rem; color: #0f172a; }
    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1rem; }
    .grid .card { margin: 0; } strong { color: #0f172a; }
  `],
})
export class OperationalOverviewComponent implements OnInit {
  private readonly analyticsApi = inject(AnalyticsApiService);
  summary: ExecutiveDashboardSummary | null = null;
  loading = true;

  ngOnInit(): void {
    this.analyticsApi.getDashboardSummary().subscribe({
      next: (summary) => { this.summary = summary; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(value);
  }
}
