import { Routes } from '@angular/router';
import { DashboardConfig, FeatureDashboardComponent } from '../feature-dashboard/feature-dashboard.component';

const recoveryDashboard: DashboardConfig = {
  eyebrow: 'Recovery & continuity',
  title: 'Recovery operations',
  description:
    'Track recoveries, salvage outcomes, legal follow-through, and continuity initiatives across active loss portfolios.',
  actionLabel: 'Open recovery queue',
  actionLink: '/recovery',
  stats: [
    { label: 'Active recoveries', value: '11', delta: '+7%', tone: 'blue' },
    { label: 'Cash recovered', value: '$3.4M', delta: '+18%', tone: 'emerald' },
    { label: 'Legal actions', value: '4', delta: '+1%', tone: 'amber' },
    { label: 'Salvage assets', value: '6', delta: '-2%', tone: 'slate' },
  ],
  timeline: [
    { title: 'Recovery case updated', detail: 'Two claimant recoveries moved into final collection review.', time: '4 mins ago', tone: 'emerald' },
    { title: 'Salvage disposition', detail: 'Vehicle auction plan approved for one total-loss claim.', time: '21 mins ago', tone: 'blue' },
    { title: 'Legal escalation', detail: 'Subrogation demand filed against third-party insurer.', time: '55 mins ago', tone: 'amber' },
  ],
  rows: [
    { name: 'Fleet fire claim', status: 'Healthy', note: 'Recovery action on schedule', value: '$810K' },
    { name: 'Total-loss salvage', status: 'Queued', note: 'Auction date pending', value: '1 asset' },
    { name: 'Third-party subrogation', status: 'Monitoring', note: 'Demand letter sent last week', value: '$740K' },
    { name: 'Coverage continuity', status: 'Review', note: 'Policyholder contact verification in progress', value: '2 checks' },
  ],
};

export const RECOVERY_SHELL_ROUTES: Routes = [
  { path: '**', component: FeatureDashboardComponent, data: { dashboard: recoveryDashboard } },
];
