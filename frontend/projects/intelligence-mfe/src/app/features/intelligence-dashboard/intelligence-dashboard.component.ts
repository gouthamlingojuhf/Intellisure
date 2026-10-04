import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'intelligence-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="card" style="max-width: 1100px; margin: 1.5rem auto;">
      <div style="display: flex; justify-content: space-between; gap: 1rem; align-items: center; flex-wrap: wrap;">
        <div>
          <p style="font-size: 0.7rem; letter-spacing: 0.18em; text-transform: uppercase; color: #1d4ed8; font-weight: 700; margin: 0;">Executive intelligence</p>
          <h1 style="margin: 0.5rem 0 0; font-size: 2.2rem; color: #0f172a;">Analytics & risk dashboard</h1>
        </div>
        <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
          <a routerLink="overview" class="btn-secondary">Portfolio overview</a>
          <a routerLink="alerts" class="btn-primary">Risk alerts</a>
        </div>
      </div>

      <div class="grid" style="grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); margin-top: 1.5rem;">
        <div class="card">
          <div class="kpi">
            <span style="color: #64748b; font-size: 0.8rem;">Loss ratio</span>
            <span class="kpi-value">62%</span>
            <span class="badge badge-warning">+1.5%</span>
          </div>
        </div>
        <div class="card">
          <div class="kpi">
            <span style="color: #64748b; font-size: 0.8rem;">Claim frequency</span>
            <span class="kpi-value">7.8%</span>
            <span class="badge badge-success">-0.6%</span>
          </div>
        </div>
        <div class="card">
          <div class="kpi">
            <span style="color: #64748b; font-size: 0.8rem;">Subrogation yield</span>
            <span class="kpi-value">18.4%</span>
            <span class="badge badge-success">+2.1%</span>
          </div>
        </div>
        <div class="card">
          <div class="kpi">
            <span style="color: #64748b; font-size: 0.8rem;">Priority alerts</span>
            <span class="kpi-value">21</span>
            <span class="badge badge-danger">-3</span>
          </div>
        </div>
      </div>
    </section>
  `,
})
export class IntelligenceDashboardComponent {}
