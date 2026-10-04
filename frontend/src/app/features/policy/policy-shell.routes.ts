import { Routes } from '@angular/router';
import { DashboardConfig, FeatureDashboardComponent } from '../feature-dashboard/feature-dashboard.component';

const policyDashboard: DashboardConfig = {
  eyebrow: 'New business',
  title: 'Quote & policy operations',
  description:
    'Monitor quoting volume, binding performance, and policy lifecycle health across personal and commercial lines.',
  actionLabel: 'Review pipeline',
  actionLink: '/policy',
  stats: [
    { label: 'Open quotes', value: '18', delta: '+12%', tone: 'blue' },
    { label: 'Bound policies', value: '64', delta: '+9%', tone: 'emerald' },
    { label: 'In underwriting', value: '9', delta: '+3%', tone: 'amber' },
    { label: 'Expiring soon', value: '2', delta: '-1%', tone: 'slate' },
  ],
  timeline: [
    { title: 'Rate review completed', detail: 'Commercial auto submission passed pricing check.', time: '5 mins ago', tone: 'emerald' },
    { title: 'Manual review queued', detail: 'Two high-value accounts need underwriter signoff.', time: '18 mins ago', tone: 'amber' },
    { title: 'Policy scheduled', detail: 'Renewal pack for a fleet account was generated.', time: '44 mins ago', tone: 'blue' },
  ],
  rows: [
    { name: 'D&O renewal', status: 'Healthy', note: 'Pricing approved', value: '$42.8K' },
    { name: 'Fleet quote', status: 'Monitoring', note: 'Waiting on additional loss history', value: '3 docs' },
    { name: 'Garage policy', status: 'Queued', note: 'Escalated to underwriting', value: '1 case' },
    { name: 'Homeowners bundle', status: 'Review', note: 'Coverage review requested', value: '2 items' },
  ],
};

export const POLICY_SHELL_ROUTES: Routes = [
  { path: '**', component: FeatureDashboardComponent, data: { dashboard: policyDashboard } },
];
