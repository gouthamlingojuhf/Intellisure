import { NgClass } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

export type DashboardTone = 'blue' | 'emerald' | 'amber' | 'rose' | 'slate';

export interface DashboardStat {
  label: string;
  value: string;
  delta: string;
  tone: DashboardTone;
}

export interface DashboardTimelineItem {
  title: string;
  detail: string;
  time: string;
  tone: DashboardTone;
}

export interface DashboardTableRow {
  name: string;
  status: string;
  note: string;
  value: string;
}

export interface DashboardConfig {
  eyebrow: string;
  title: string;
  description: string;
  actionLabel: string;
  actionLink: string;
  stats: DashboardStat[];
  timeline: DashboardTimelineItem[];
  rows: DashboardTableRow[];
}

@Component({
  selector: 'is-feature-dashboard',
  standalone: true,
  imports: [NgClass, RouterLink],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div>
          <p class="page-eyebrow">{{ dashboard.eyebrow }}</p>
          <h1>{{ dashboard.title }}</h1>
          <p class="page-description">{{ dashboard.description }}</p>
        </div>
        <a [routerLink]="dashboard.actionLink" class="primary-button">{{ dashboard.actionLabel }} <span aria-hidden="true">→</span></a>
      </header>

      <div class="command-bar" aria-label="Dashboard controls">
        <div class="period-control"><span>Reporting period</span><strong>Last 24 hours</strong></div>
        <div class="command-actions"><button type="button">Export</button><button type="button">Share</button></div>
      </div>

      <section class="metric-grid" aria-label="Operational metrics">
        @for (stat of dashboard.stats; track stat.label) {
          <article class="metric-card">
            <div class="metric-card-top"><p>{{ stat.label }}</p><span class="metric-icon" [ngClass]="{'metric-blue': stat.tone === 'blue', 'metric-green': stat.tone === 'emerald', 'metric-amber': stat.tone === 'amber', 'metric-red': stat.tone === 'rose', 'metric-neutral': stat.tone === 'slate'}">↗</span></div>
            <strong>{{ stat.value }}</strong>
            <div class="metric-footer"><span [ngClass]="{'trend-positive': stat.tone === 'emerald', 'trend-negative': stat.tone === 'rose'}">{{ stat.delta }}</span><small>vs. prior period</small></div>
          </article>
        }
      </section>

      <div class="dashboard-grid">
        <section class="panel">
          <div class="panel-header"><div><p class="panel-kicker">Portfolio</p><h2>Operational snapshot</h2></div><button type="button" class="text-button">View all</button></div>
          <div class="table-wrap">
            <table>
              <thead><tr><th>Name</th><th>Status</th><th>Notes</th><th>Value</th></tr></thead>
              <tbody>
                @for (row of dashboard.rows; track row.name) {
                  <tr><td><strong>{{ row.name }}</strong></td><td><span class="status-pill" [ngClass]="{'status-success': row.status === 'Healthy', 'status-warning': row.status === 'Monitoring', 'status-info': row.status === 'Queued', 'status-danger': row.status === 'Review'}">{{ row.status }}</span></td><td>{{ row.note }}</td><td class="table-value">{{ row.value }}</td></tr>
                }
              </tbody>
            </table>
          </div>
        </section>

        <aside class="panel activity-panel">
          <div class="panel-header"><div><p class="panel-kicker">Live operations</p><h2>Recent activity</h2></div><span class="live-indicator"><i></i> Live</span></div>
          <div class="activity-list">
            @for (item of dashboard.timeline; track item.title) {
              <div class="activity-item"><span class="activity-dot" [ngClass]="{'activity-blue': item.tone === 'blue', 'activity-green': item.tone === 'emerald', 'activity-amber': item.tone === 'amber', 'activity-red': item.tone === 'rose', 'activity-neutral': item.tone === 'slate'}"></span><div><strong>{{ item.title }}</strong><p>{{ item.detail }}</p><time>{{ item.time }}</time></div></div>
            }
          </div>
          <div class="activity-summary"><span>System health</span><strong>98.7%</strong><div><i></i></div></div>
        </aside>
      </div>
    </section>
  `,
})
export class FeatureDashboardComponent implements OnInit {
  @Input() dashboard: DashboardConfig = {
    eyebrow: 'Operations',
    title: 'Feature dashboard',
    description: 'A live operational view for the current insurance workflow.',
    actionLabel: 'Open workspace',
    actionLink: '/',
    stats: [
      { label: 'Open items', value: '12', delta: '+8%', tone: 'blue' },
      { label: 'Complete', value: '89%', delta: '+4%', tone: 'emerald' },
      { label: 'At risk', value: '3', delta: '-2%', tone: 'amber' },
      { label: 'Escalations', value: '1', delta: '0%', tone: 'slate' },
    ],
    timeline: [
      { title: 'Workflow running normally', detail: 'No critical issues detected across the domain.', time: '2 mins ago', tone: 'emerald' },
      { title: 'Next review due', detail: 'One task requires action before SLA threshold.', time: '18 mins ago', tone: 'amber' },
      { title: 'API gateway healthy', detail: 'Request latency remains within expected bounds.', time: '42 mins ago', tone: 'blue' },
    ],
    rows: [
      { name: 'Claims scoring', status: 'Healthy', note: 'Risk rules synchronized', value: '99.2%' },
      { name: 'Vendor dispatch', status: 'Queued', note: 'Two assignments waiting for approval', value: '2' },
      { name: 'Document audit', status: 'Monitoring', note: 'One stale record pending review', value: '1' },
      { name: 'Coverage checks', status: 'Review', note: 'Manual review required', value: '3' },
    ],
  };

  constructor(private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    const routeDashboard = this.route.snapshot.data['dashboard'] as Partial<DashboardConfig> | undefined;
    if (routeDashboard) {
      this.dashboard = { ...this.dashboard, ...routeDashboard };
    }
  }
}
