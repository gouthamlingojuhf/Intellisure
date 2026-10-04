import { Routes } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/module-federation';
import { DashboardConfig } from '../feature-dashboard/feature-dashboard.component';

const analyticsDashboard: DashboardConfig = {
  eyebrow: 'Executive intelligence',
  title: 'Analytics & risk dashboard',
  description:
    'Measure loss ratio, premium performance, subrogation yield, and operational signals from across the insurance lifecycle.',
  actionLabel: 'Open insights',
  actionLink: '/analytics',
  stats: [
    { label: 'Loss ratio', value: '62%', delta: '+1.5%', tone: 'amber' },
    { label: 'Claim frequency', value: '7.8%', delta: '-0.6%', tone: 'emerald' },
    { label: 'Subrogation yield', value: '18.4%', delta: '+2.1%', tone: 'blue' },
    { label: 'Priority alerts', value: '21', delta: '-3', tone: 'rose' },
  ],
  timeline: [
    { title: 'Portfolio trend updated', detail: 'Quarterly loss ratio refreshed with latest runoff data.', time: '11 mins ago', tone: 'blue' },
    { title: 'Claim severity spike', detail: 'Commercial property segment trending above seasonal baseline.', time: '29 mins ago', tone: 'amber' },
    { title: 'Collections forecast', detail: 'Recovery projections revised upward for active recovery cases.', time: '1 hour ago', tone: 'emerald' },
  ],
  rows: [
    { name: 'Commercial GL', status: 'Healthy', note: 'Stable frequency and lower severity', value: '5.2%' },
    { name: 'Auto physical damage', status: 'Monitoring', note: 'Repair costs remain elevated', value: '9.1%' },
    { name: 'Property catastrophe', status: 'Review', note: 'High severity exposures under watch', value: '13.4%' },
    { name: 'Subrogation pipeline', status: 'Queued', note: 'Two large recoveries pending legal finalization', value: '$2.1M' },
  ],
};

/** Shell mounts the intelligence MFE remote (dev: http://localhost:4203/remoteEntry.js). */
export const ANALYTICS_SHELL_ROUTES: Routes = [
  {
    path: '**',
    loadChildren: () =>
      loadRemoteModule({
        type: 'module',
        remoteEntry: 'http://localhost:4203/remoteEntry.js',
        exposedModule: './Routes',
      }).then((m) => m.ANALYTICS_REMOTE_ROUTES),
  },
];
