import { Routes } from '@angular/router';
import { DashboardConfig, FeatureDashboardComponent } from '../feature-dashboard/feature-dashboard.component';

const underwritingDashboard: DashboardConfig = {
  eyebrow: 'Risk review',
  title: 'Underwriting desk',
  description:
    'Balance risk appetite, pricing quality, and approval workflow across property, casualty, and specialty accounts.',
  actionLabel: 'Open cases',
  actionLink: '/underwriting',
  stats: [
    { label: 'Applications', value: '143', delta: '+8%', tone: 'blue' },
    { label: 'Approved', value: '91', delta: '+6%', tone: 'emerald' },
    { label: 'Manual review', value: '12', delta: '+2%', tone: 'amber' },
    { label: 'Exceptions', value: '4', delta: '-1%', tone: 'rose' },
  ],
  timeline: [
    { title: 'Risk engine refreshed', detail: 'Updated hazard and claims data synced for the pricing model.', time: '8 mins ago', tone: 'blue' },
    { title: 'Portfolio watchlist updated', detail: 'Aviation and cyber exposures moved into enhanced review.', time: '22 mins ago', tone: 'amber' },
    { title: 'Decision pack finalized', detail: 'Two mid-market renewals were approved with adjusted terms.', time: '51 mins ago', tone: 'emerald' },
  ],
  rows: [
    { name: 'Property risk profile', status: 'Healthy', note: 'No deterioration in portfolio quality', value: '96%' },
    { name: 'Cyber submission', status: 'Review', note: 'Exposure summary requires legal review', value: '1 case' },
    { name: 'Commercial auto', status: 'Monitoring', note: 'Claims history above threshold', value: '6 units' },
    { name: 'Umbrella renewal', status: 'Queued', note: 'Waiting on reinsurance summary', value: '2 docs' },
  ],
};

export const UNDERWRITING_SHELL_ROUTES: Routes = [
  { path: '**', component: FeatureDashboardComponent, data: { dashboard: underwritingDashboard } },
];
