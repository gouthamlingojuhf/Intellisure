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
    <section class="space-y-6">
      <div class="card">
        <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-blue-700">{{ dashboard.eyebrow }}</p>
            <h1 class="mt-2 text-3xl font-bold text-blue-900">{{ dashboard.title }}</h1>
            <p class="mt-2 max-w-3xl text-gray-600">{{ dashboard.description }}</p>
          </div>

          <a [routerLink]="dashboard.actionLink" class="btn-primary">{{ dashboard.actionLabel }}</a>
        </div>
      </div>

      <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        @for (stat of dashboard.stats; track stat.label) {
          <div class="card">
            <div class="flex items-start justify-between">
              <div>
                <p class="text-sm text-gray-500">{{ stat.label }}</p>
                <p class="mt-2 text-3xl font-bold text-blue-900">{{ stat.value }}</p>
              </div>
              <span
                class="rounded-full px-2 py-1 text-xs font-semibold"
                [ngClass]="{
                  'bg-blue-100 text-blue-800': stat.tone === 'blue',
                  'bg-emerald-100 text-emerald-700': stat.tone === 'emerald',
                  'bg-amber-100 text-amber-700': stat.tone === 'amber',
                  'bg-rose-100 text-rose-700': stat.tone === 'rose',
                  'bg-slate-100 text-slate-700': stat.tone === 'slate'
                }"
              >
                {{ stat.delta }}
              </span>
            </div>
          </div>
        }
      </div>

      <div class="grid gap-6 xl:grid-cols-[1.4fr_0.8fr]">
        <div class="card">
          <div class="mb-4 flex items-center justify-between">
            <h2 class="text-xl font-bold text-blue-900">Operational snapshot</h2>
            <span class="text-sm text-gray-500">Last 24 hours</span>
          </div>

          <div class="overflow-x-auto">
            <table class="min-w-full text-left text-sm">
              <thead class="border-b border-gray-200 text-gray-500">
                <tr>
                  <th class="pb-3 font-medium">Name</th>
                  <th class="pb-3 font-medium">Status</th>
                  <th class="pb-3 font-medium">Notes</th>
                  <th class="pb-3 font-medium text-right">Value</th>
                </tr>
              </thead>
              <tbody>
                @for (row of dashboard.rows; track row.name) {
                  <tr class="border-b border-gray-100 last:border-b-0">
                    <td class="py-3 pr-4 font-medium text-gray-800">{{ row.name }}</td>
                    <td class="py-3 pr-4">
                      <span
                        class="rounded-full px-2 py-1 text-xs font-semibold"
                        [ngClass]="{
                          'bg-emerald-100 text-emerald-700': row.status === 'Healthy',
                          'bg-amber-100 text-amber-700': row.status === 'Monitoring',
                          'bg-blue-100 text-blue-700': row.status === 'Queued',
                          'bg-rose-100 text-rose-700': row.status === 'Review'
                        }"
                      >
                        {{ row.status }}
                      </span>
                    </td>
                    <td class="py-3 pr-4 text-gray-600">{{ row.note }}</td>
                    <td class="py-3 text-right font-semibold text-blue-900">{{ row.value }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>

        <div class="card">
          <h2 class="text-xl font-bold text-blue-900">Recent activity</h2>
          <div class="mt-4 space-y-4">
            @for (item of dashboard.timeline; track item.title) {
              <div class="flex gap-3">
                <span
                  class="mt-1 h-3 w-3 rounded-full"
                  [ngClass]="{
                    'bg-blue-600': item.tone === 'blue',
                    'bg-emerald-600': item.tone === 'emerald',
                    'bg-amber-500': item.tone === 'amber',
                    'bg-rose-500': item.tone === 'rose',
                    'bg-slate-500': item.tone === 'slate'
                  }"
                ></span>
                <div class="flex-1">
                  <p class="font-semibold text-gray-800">{{ item.title }}</p>
                  <p class="text-sm text-gray-600">{{ item.detail }}</p>
                  <p class="mt-1 text-xs text-gray-500">{{ item.time }}</p>
                </div>
              </div>
            }
          </div>
        </div>
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
