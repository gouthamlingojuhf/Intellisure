import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'operational-overview',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="card" style="max-width: 1000px; margin: 1.5rem auto;">
      <div style="display: flex; justify-content: space-between; gap: 1rem; align-items: center; flex-wrap: wrap; margin-bottom: 1rem;">
        <div>
          <p style="font-size: 0.7rem; letter-spacing: 0.18em; text-transform: uppercase; color: #1d4ed8; font-weight: 700; margin: 0;">Portfolio overview</p>
          <h2 style="margin: 0.35rem 0 0; font-size: 1.8rem; color: #0f172a;">Operational snapshot</h2>
        </div>
        <a routerLink="/" class="btn-secondary">Back to dashboard</a>
      </div>

      <div class="grid" style="grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));">
        <div class="card">
          <h3>Commercial GL</h3>
          <p>Healthy performance with stable frequency and lower severity.</p>
          <strong style="color: #0f172a;">5.2%</strong>
        </div>
        <div class="card">
          <h3>Auto physical damage</h3>
          <p>Repair costs remain elevated in select corridors.</p>
          <strong style="color: #0f172a;">9.1%</strong>
        </div>
        <div class="card">
          <h3>Property catastrophe</h3>
          <p>High severity exposures remain under active review.</p>
          <strong style="color: #0f172a;">13.4%</strong>
        </div>
      </div>
    </section>
  `,
})
export class OperationalOverviewComponent {}
