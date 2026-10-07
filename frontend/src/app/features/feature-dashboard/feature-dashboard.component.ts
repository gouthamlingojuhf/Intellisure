import { NgClass } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CardComponent, ButtonComponent, BadgeComponent, TableComponent, TableColumn, TableAction } from 'ui-core';

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
  imports: [NgClass, RouterLink, CardComponent, ButtonComponent, BadgeComponent, TableComponent],
  template: `
    <section class="enterprise-page">
      <header class="page-header">
        <div>
          <p class="page-eyebrow">{{ dashboard.eyebrow }}</p>
          <h1>{{ dashboard.title }}</h1>
          <p class="page-description">{{ dashboard.description }}</p>
        </div>
        <is-button variant="primary" [routerLink]="dashboard.actionLink">{{ dashboard.actionLabel }} <span aria-hidden="true">→</span></is-button>
      </header>

      <div class="command-bar" aria-label="Dashboard controls">
        <div class="period-control"><span>Reporting period</span><strong>Last 24 hours</strong></div>
        <div class="command-actions"><is-button variant="secondary" size="sm">Export</is-button><is-button variant="secondary" size="sm">Share</is-button></div>
      </div>

      <section class="metric-grid" aria-label="Operational metrics">
        @for (stat of dashboard.stats; track stat.label) {
          <article class="metric-card">
            <div class="metric-card-top"><p>{{ stat.label }}</p><span class="metric-icon" [ngClass]="getMetricIconClass(stat.tone)">{{ getMetricIcon(stat.tone) }}</span></div>
            <strong>{{ stat.value }}</strong>
            <div class="metric-footer"><is-badge [variant]="getTrendVariant(stat.tone)" size="sm">{{ stat.delta }}</is-badge><small>vs. prior period</small></div>
          </article>
        }
      </section>

      <div class="dashboard-grid">
        <is-card
          title="Operational snapshot"
          kicker="Portfolio"
        >
          <is-table
            [columns]="tableColumns"
            [data]="dashboard.rows"
            [actions]="tableActions"
            [trackByFn]="trackByName"
            [emptyMessage]="'No data available'"
          />
          <ng-template slot="footer">
            <is-button variant="text">View all</is-button>
          </ng-template>
        </is-card>

        <is-card
          title="Recent activity"
          kicker="Live operations"
          class="activity-panel"
        >
          <div class="activity-list">
            @for (item of dashboard.timeline; track item.title) {
              <div class="activity-item"><span class="activity-dot" [ngClass]="getActivityDotClass(item.tone)"></span><div><strong>{{ item.title }}</strong><p>{{ item.detail }}</p><time>{{ item.time }}</time></div></div>
            }
          </div>
          <div class="activity-summary"><span>System health</span><strong>98.7%</strong><div><i style="width: 98.7%"></i></div></div>
          <ng-template slot="footer">
            <is-button variant="ghost" size="sm">View all activity</is-button>
          </ng-template>
        </is-card>
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
    
    .command-bar {
      min-height: 48px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      padding: 7px 12px;
      border: 1px solid var(--border);
      border-radius: 8px;
      background: var(--surface);
      box-shadow: var(--shadow);
    }
    
    .period-control {
      display: flex;
      align-items: center;
      gap: 10px;
      color: #817b7f;
      font-size: 10px;
    }
    
    .period-control strong {
      color: #333033;
      font-size: 10px;
    }
    
    .command-actions {
      display: flex;
      gap: 6px;
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
    
    .dashboard-grid {
      display: grid;
      grid-template-columns: minmax(0, 1.55fr) minmax(280px, 0.65fr);
      gap: 16px;
      align-items: start;
    }
    
    .activity-panel {
      min-height: 326px;
    }
    
    .activity-list {
      padding: 3px 18px 8px;
    }
    
    .activity-item {
      position: relative;
      display: grid;
      grid-template-columns: 10px 1fr;
      gap: 12px;
      padding: 13px 0;
      border-bottom: 1px solid #f0edea;
    }
    
    .activity-dot {
      width: 8px;
      height: 8px;
      margin-top: 4px;
      border-radius: 50%;
    }
    
    .activity-dot.activity-blue { background: var(--claret); }
    .activity-dot.activity-green { background: var(--success); }
    .activity-dot.activity-amber { background: var(--warning); }
    .activity-dot.activity-red { background: var(--danger); }
    .activity-dot.activity-neutral { background: #777; }
    
    .activity-item strong {
      display: block;
      color: #292629;
      font-size: 10px;
    }
    
    .activity-item p {
      margin: 4px 0;
      color: #716b6f;
      font-size: 9px;
      line-height: 1.45;
    }
    
    .activity-item time {
      color: #9a9597;
      font-size: 8px;
    }
    
    .activity-summary {
      display: grid;
      grid-template-columns: 1fr auto;
      gap: 7px;
      padding: 13px 18px;
      background: var(--warm-light);
      color: #827c80;
      font-size: 9px;
    }
    
    .activity-summary strong { color: #272427; }
    .activity-summary div { grid-column: 1/-1; height: 4px; border-radius: 999px; background: #ded8d4; overflow: hidden; }
    .activity-summary i { display: block; height: 100%; border-radius: inherit; background: var(--claret); }
    
    @media (max-width: 1100px) {
      .metric-grid { grid-template-columns: repeat(2, minmax(0,1fr)); }
      .dashboard-grid { grid-template-columns: 1fr; }
      .activity-panel { min-height: auto; }
    }
    
    @media (max-width: 700px) {
      .page-header { align-items: flex-start; flex-direction: column; }
      .metric-grid { grid-template-columns: 1fr; }
      .command-bar { align-items: flex-start; flex-direction: column; }
    }
  `],
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

  tableColumns: TableColumn<DashboardTableRow>[] = [
    { key: 'name', header: 'Name', width: '200px' },
    { key: 'status', header: 'Status', width: '120px', render: this.renderStatus.bind(this) },
    { key: 'note', header: 'Notes', width: '250px' },
    { key: 'value', header: 'Value', width: '120px', align: 'right' },
  ];

  tableActions: TableAction<DashboardTableRow>[] = [
    { label: 'Open', variant: 'text', handler: () => {} },
  ];

  constructor(private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    const routeDashboard = this.route.snapshot.data['dashboard'] as Partial<DashboardConfig> | undefined;
    if (routeDashboard) {
      this.dashboard = { ...this.dashboard, ...routeDashboard };
    }
  }

  trackByName = (index: number, item: DashboardTableRow): string => {
    return item.name;
  }

  getMetricIconClass(tone: DashboardTone): string {
    const classes: Record<DashboardTone, string> = {
      blue: 'metric-blue',
      emerald: 'metric-green',
      amber: 'metric-amber',
      rose: 'metric-red',
      slate: 'metric-neutral',
    };
    return classes[tone] || 'metric-neutral';
  }

  getMetricIcon(tone: DashboardTone): string {
    const icons: Record<DashboardTone, string> = {
      blue: '📊',
      emerald: '✓',
      amber: '⚠',
      rose: '!',
      slate: '📋',
    };
    return icons[tone] || '📋';
  }

  getTrendVariant(tone: DashboardTone): 'success' | 'warning' | 'danger' | 'info' | 'neutral' {
    const variants: Record<DashboardTone, 'success' | 'warning' | 'danger' | 'info' | 'neutral'> = {
      blue: 'info',
      emerald: 'success',
      amber: 'warning',
      rose: 'danger',
      slate: 'neutral',
    };
    return variants[tone] || 'neutral';
  }

  getActivityDotClass(tone: DashboardTone): string {
    const classes: Record<DashboardTone, string> = {
      blue: 'activity-blue',
      emerald: 'activity-green',
      amber: 'activity-amber',
      rose: 'activity-red',
      slate: 'activity-neutral',
    };
    return classes[tone] || 'activity-neutral';
  }

  renderStatus(row: DashboardTableRow, value: string): string {
    const statusMap: Record<string, 'success' | 'warning' | 'danger' | 'info' | 'neutral'> = {
      'Healthy': 'success',
      'Monitoring': 'warning',
      'Queued': 'info',
      'Review': 'danger',
    };
    const variant = statusMap[value] || 'neutral';
    return `<is-badge variant="${variant}" size="sm">${value}</is-badge>`;
  }
}