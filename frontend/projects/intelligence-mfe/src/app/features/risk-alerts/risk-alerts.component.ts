import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'risk-alerts',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="card" style="max-width: 900px; margin: 1.5rem auto;">
      <div style="display: flex; justify-content: space-between; gap: 1rem; align-items: center; flex-wrap: wrap; margin-bottom: 1rem;">
        <div>
          <p style="font-size: 0.7rem; letter-spacing: 0.18em; text-transform: uppercase; color: #1d4ed8; font-weight: 700; margin: 0;">Risk alerts</p>
          <h2 style="margin: 0.35rem 0 0; font-size: 1.8rem; color: #0f172a;">Escalations & watchlist</h2>
        </div>
        <a routerLink="/" class="btn-secondary">Back to dashboard</a>
      </div>

      <div class="card" style="margin-bottom: 1rem; border-left: 6px solid #f59e0b;">
        <strong>Commercial GL</strong>
        <p>Severity trend is above seasonal baseline and warrants early triage.</p>
        <span class="badge badge-warning">Monitoring</span>
      </div>

      <div class="card" style="margin-bottom: 1rem; border-left: 6px solid #ef4444;">
        <strong>Property catastrophe</strong>
        <p>Three large events are being reviewed for reserve recalculation.</p>
        <span class="badge badge-danger">Review</span>
      </div>

      <div class="card" style="border-left: 6px solid #22c55e;">
        <strong>Subrogation pipeline</strong>
        <p>Two recoveries are close to settlement and should be prioritized.</p>
        <span class="badge badge-success">Healthy</span>
      </div>
    </section>
  `,
})
export class RiskAlertsComponent {}
