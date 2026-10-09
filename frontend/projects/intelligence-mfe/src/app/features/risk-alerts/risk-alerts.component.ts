import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'risk-alerts',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="card page">
      <div class="heading"><div><p class="eyebrow">Risk alerts</p><h2>Escalations & watchlist</h2></div><a routerLink="/" class="btn-secondary">Back to dashboard</a></div>
      <p>No alert feed is exposed by the current Analytics API contract. Alerts will appear here when the backend publishes them.</p>
    </section>
  `,
  styles: [`
    .page { max-width: 900px; margin: 1.5rem auto; }
    .heading { display: flex; justify-content: space-between; gap: 1rem; align-items: center; flex-wrap: wrap; margin-bottom: 1rem; }
    .eyebrow { font-size: .7rem; letter-spacing: .18em; text-transform: uppercase; color: #1d4ed8; font-weight: 700; margin: 0; }
    h2 { margin: .35rem 0 0; font-size: 1.8rem; color: #0f172a; }
  `],
})
export class RiskAlertsComponent {}
