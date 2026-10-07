import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CardComponent, ButtonComponent, BadgeComponent, SkeletonComponent } from 'ui-core';

@Component({
  selector: 'intelligence-dashboard',
  standalone: true,
  imports: [RouterLink, CardComponent, ButtonComponent, BadgeComponent, SkeletonComponent],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div>
          <p class="page-eyebrow">Executive Intelligence</p>
          <h1>Analytics & risk dashboard</h1>
          <p class="page-description">Measure loss ratio, premium performance, subrogation yield, and operational signals from across the insurance lifecycle.</p>
        </div>
        <div class="header-actions">
          <is-button variant="secondary" routerLink="overview">Portfolio overview</is-button>
          <is-button variant="primary" routerLink="alerts">Risk alerts</is-button>
        </div>
      </header>

      <div class="metric-grid" aria-label="Key performance indicators">
        <article class="metric-card">
          <div class="metric-card-top">
            <p>Loss ratio</p>
            <span class="metric-icon metric-amber">%</span>
          </div>
          <strong>62%</strong>
          <div class="metric-footer">
            <span class="trend-negative">+1.5%</span>
            <small>vs. prior quarter</small>
          </div>
        </article>

        <article class="metric-card">
          <div class="metric-card-top">
            <p>Claim frequency</p>
            <span class="metric-icon metric-green">%</span>
          </div>
          <strong>7.8%</strong>
          <div class="metric-footer">
            <span class="trend-positive">-0.6%</span>
            <small>vs. prior quarter</small>
          </div>
        </article>

        <article class="metric-card">
          <div class="metric-card-top">
            <p>Subrogation yield</p>
            <span class="metric-icon metric-blue">%</span>
          </div>
          <strong>18.4%</strong>
          <div class="metric-footer">
            <span class="trend-positive">+2.1%</span>
            <small>vs. prior quarter</small>
          </div>
        </article>

        <article class="metric-card">
          <div class="metric-card-top">
            <p>Priority alerts</p>
            <span class="metric-icon metric-red">!</span>
          </div>
          <strong>21</strong>
          <div class="metric-footer">
            <span class="trend-positive">-3</span>
            <small>vs. prior week</small>
          </div>
        </article>
      </div>

      <div class="dashboard-grid">
        <section class="panel">
          <div class="panel-header">
            <div>
              <p class="panel-kicker">Portfolio segments</p>
              <h2>Performance overview</h2>
            </div>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Segment</th>
                  <th>Loss Ratio</th>
                  <th>Frequency</th>
                  <th>Severity</th>
                  <th>Trend</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td><strong>Commercial GL</strong></td>
                  <td>5.2%</td>
                  <td>3.1%</td>
                  <td>$12.4K</td>
                  <td><span class="trend-positive">↘ Improving</span></td>
                  <td><is-badge variant="success" size="sm">Healthy</is-badge></td>
                </tr>
                <tr>
                  <td><strong>Auto Physical Damage</strong></td>
                  <td>9.1%</td>
                  <td>6.8%</td>
                  <td>$8.7K</td>
                  <td><span class="trend-warning">→ Stable</span></td>
                  <td><is-badge variant="warning" size="sm">Monitoring</is-badge></td>
                </tr>
                <tr>
                  <td><strong>Property Catastrophe</strong></td>
                  <td>13.4%</td>
                  <td>2.4%</td>
                  <td>$45.2K</td>
                  <td><span class="trend-negative">↗ Worsening</span></td>
                  <td><is-badge variant="danger" size="sm">Review</is-badge></td>
                </tr>
                <tr>
                  <td><strong>Subrogation Pipeline</strong></td>
                  <td>—</td>
                  <td>—</td>
                  <td>$2.1M</td>
                  <td><span class="trend-positive">↘ Improving</span></td>
                  <td><is-badge variant="info" size="sm">Queued</is-badge></td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <aside class="panel activity-panel">
          <div class="panel-header">
            <div>
              <p class="panel-kicker">Risk signals</p>
              <h2>Active alerts</h2>
            </div>
            <span class="live-indicator"><i></i> Live</span>
          </div>
          <div class="activity-list">
            <div class="activity-item">
              <span class="activity-dot activity-amber"></span>
              <div>
                <strong>Catastrophe exposure spike</strong>
                <p>Florida wind portfolio exceeds threshold after recent CAT model update.</p>
                <time>12 minutes ago</time>
              </div>
            </div>
            <div class="activity-item">
              <span class="activity-dot activity-red"></span>
              <div>
                <strong>Reserve adequacy breach</strong>
                <p>Commercial auto line reserves below 85% confidence level.</p>
                <time>38 minutes ago</time>
              </div>
            </div>
            <div class="activity-item">
              <span class="activity-dot activity-blue"></span>
              <div>
                <strong>New model deployed</strong>
                <p>Updated severity forecasting model v3.2 now active for GL lines.</p>
                <time>2 hours ago</time>
              </div>
            </div>
            <div class="activity-item">
              <span class="activity-dot activity-green"></span>
              <div>
                <strong>Subrogation recovery</strong>
                <p>$480K recovered on 2023 construction defect portfolio.</p>
                <time>4 hours ago</time>
              </div>
            </div>
          </div>
          <div class="activity-summary">
            <span>Model accuracy</span>
            <strong>94.2%</strong>
            <div><i style="width: 94.2%"></i></div>
          </div>
        </aside>
      </div>
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
      max-width: 1540px;
      margin: 0 auto;
    }
    
    .page-header {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 24px;
      padding: 2px 0 4px;
    }
    
    .header-actions {
      display: flex;
      gap: 8px;
    }
    
    .page-eyebrow {
      margin: 0 0 7px;
      color: var(--claret);
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.14em;
      text-transform: uppercase;
    }
    
    .page-header h1 {
      margin: 0;
      color: var(--ink);
      font-size: clamp(25px, 2.5vw, 34px);
      line-height: 1.08;
      letter-spacing: -0.045em;
    }
    
    .page-description {
      max-width: 730px;
      margin: 9px 0 0;
      color: var(--muted);
      font-size: 12px;
      line-height: 1.6;
    }
    
    .metric-grid {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 14px;
    }
    
    .metric-card {
      position: relative;
      min-height: 132px;
      padding: 18px;
      border: 1px solid var(--border);
      border-radius: 9px;
      background: var(--surface);
      box-shadow: var(--shadow);
      overflow: hidden;
    }
    
    .metric-card::after {
      content: '';
      position: absolute;
      right: -20px;
      bottom: -28px;
      width: 90px;
      height: 90px;
      border-radius: 50%;
      background: var(--warm-light);
      opacity: 0.7;
    }
    
    .metric-card-top {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }
    
    .metric-card-top p {
      margin: 0;
      color: #706b6e;
      font-size: 10px;
      font-weight: 600;
    }
    
    .metric-icon {
      width: 28px;
      height: 28px;
      display: grid;
      place-items: center;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 700;
    }
    
    .metric-icon.metric-blue { color: var(--claret); background: var(--claret-soft); }
    .metric-icon.metric-green { color: var(--success); background: var(--success-light); }
    .metric-icon.metric-amber { color: var(--warning); background: var(--warning-light); }
    .metric-icon.metric-red { color: var(--danger); background: var(--danger-light); }
    
    .metric-card > strong {
      position: relative;
      z-index: 1;
      display: block;
      margin-top: 14px;
      color: #080808;
      font-size: 27px;
      line-height: 1;
      letter-spacing: -0.045em;
    }
    
    .metric-footer {
      position: relative;
      z-index: 1;
      display: flex;
      align-items: center;
      gap: 6px;
      margin-top: 13px;
      font-size: 9px;
    }
    
    .metric-footer small { color: #928c90; }
    .trend-positive { color: var(--success); font-weight: 700; }
    .trend-negative { color: var(--danger); font-weight: 700; }
    .trend-warning { color: var(--warning); font-weight: 700; }
    
    .dashboard-grid {
      display: grid;
      grid-template-columns: minmax(0, 1.55fr) minmax(280px, 0.65fr);
      gap: 16px;
      align-items: start;
    }
    
    .panel {
      border: 1px solid var(--border);
      border-radius: 9px;
      background: var(--surface);
      box-shadow: var(--shadow);
      overflow: hidden;
    }
    
    .panel-header {
      min-height: 65px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 15px 18px;
      border-bottom: 1px solid var(--border);
    }
    
    .panel-header h2 {
      margin: 0;
      color: #171617;
      font-size: 15px;
      letter-spacing: -0.02em;
    }
    
    .panel-kicker {
      margin: 0 0 7px;
      color: var(--claret);
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.14em;
      text-transform: uppercase;
    }
    
    .table-wrap { width: 100%; overflow-x: auto; }
    .table-wrap table { width: 100%; min-width: 680px; border-collapse: collapse; font-size: 10px; }
    .table-wrap th { height: 40px; padding: 0 16px; border-bottom: 1px solid var(--border); background: var(--surface-hover); color: #777174; font-size: 9px; font-weight: 700; letter-spacing: 0.04em; text-align: left; text-transform: uppercase; }
    .table-wrap td { height: 51px; padding: 0 16px; border-bottom: 1px solid #f0edea; color: #625c60; }
    .table-wrap tbody tr:last-child td { border-bottom: 0; }
    .table-wrap tbody tr:hover { background: #fcfbfa; }
    .table-wrap td strong { color: #242224; font-weight: 600; }
    
    .trend-positive { color: var(--success); font-size: 10px; font-weight: 600; }
    .trend-negative { color: var(--danger); font-size: 10px; font-weight: 600; }
    .trend-warning { color: var(--warning); font-size: 10px; font-weight: 600; }
    
    .activity-panel { min-height: 326px; }
    .live-indicator { display: flex; align-items: center; gap: 6px; color: var(--success); font-size: 9px; font-weight: 700; }
    .live-indicator i { width: 6px; height: 6px; border-radius: 50%; background: #2f8b5f; box-shadow: 0 0 0 3px #e7f4ed; animation: pulse 2s infinite; }
    
    @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
    
    .activity-list { padding: 3px 18px 8px; }
    .activity-item { position: relative; display: grid; grid-template-columns: 10px 1fr; gap: 12px; padding: 13px 0; border-bottom: 1px solid #f0edea; }
    .activity-dot { width: 8px; height: 8px; margin-top: 4px; border-radius: 50%; }
    .activity-dot.activity-blue { background: var(--claret); }
    .activity-dot.activity-green { background: var(--success); }
    .activity-dot.activity-amber { background: var(--warning); }
    .activity-dot.activity-red { background: var(--danger); }
    .activity-item strong { display: block; color: #292629; font-size: 10px; }
    .activity-item p { margin: 4px 0; color: #716b6f; font-size: 9px; line-height: 1.45; }
    .activity-item time { color: #9a9597; font-size: 8px; }
    
    .activity-summary { display: grid; grid-template-columns: 1fr auto; gap: 7px; padding: 13px 18px; background: var(--warm-light); color: #827c80; font-size: 9px; }
    .activity-summary strong { color: #272427; }
    .activity-summary div { grid-column: 1/-1; height: 4px; border-radius: 999px; background: #ded8d4; overflow: hidden; }
    .activity-summary i { display: block; height: 100%; border-radius: inherit; background: var(--claret); }
    
    @media (max-width: 1100px) {
      .metric-grid { grid-template-columns: repeat(2, minmax(0,1fr)); }
      .dashboard-grid { grid-template-columns: 1fr; }
      .activity-panel { min-height: auto; }
    }
    
    @media (max-width: 700px) {
      .metric-grid { grid-template-columns: 1fr; }
      .header-actions { flex-direction: column; width: 100%; }
      .header-actions is-button { width: 100%; }
    }
  `],
})
export class IntelligenceDashboardComponent {}