import { Routes } from '@angular/router';
import { DashboardConfig, FeatureDashboardComponent } from '../feature-dashboard/feature-dashboard.component';

const docsDashboard: DashboardConfig = {
  eyebrow: 'Policy docs',
  title: 'Documents & notifications',
  description:
    'Review policy packets, communications, audit evidence, and stewardship touchpoints required across the customer journey.',
  actionLabel: 'Open document queue',
  actionLink: '/docs',
  stats: [
    { label: 'Docs ready', value: '96%', delta: '+4%', tone: 'emerald' },
    { label: 'Pending review', value: '5', delta: '+1%', tone: 'amber' },
    { label: 'Signed packets', value: '1,420', delta: '+18%', tone: 'blue' },
    { label: 'Audit alerts', value: '2', delta: '-1%', tone: 'rose' },
  ],
  timeline: [
    { title: 'Renewal packet issued', detail: 'Policyholder email and PDF packet dispatched successfully.', time: '7 mins ago', tone: 'emerald' },
    { title: 'Notice review', detail: 'Document audit found a missing signer acknowledgment.', time: '23 mins ago', tone: 'amber' },
    { title: 'Outbound SMS sent', detail: 'Claim notification reached the insured with next-step guidance.', time: '1 hour ago', tone: 'blue' },
  ],
  rows: [
    { name: 'Renewal packet', status: 'Healthy', note: 'Delivered with no exceptions', value: '314' },
    { name: 'Proof of loss', status: 'Monitoring', note: 'Awaiting signed affidavit', value: '3 cases' },
    { name: 'Vendor onboarding', status: 'Queued', note: 'Documents staged for approval', value: '2 users' },
    { name: 'Compliance audit', status: 'Review', note: 'One record requires retention check', value: '1 alert' },
  ],
};

export const DOCS_SHELL_ROUTES: Routes = [
  { path: '**', component: FeatureDashboardComponent, data: { dashboard: docsDashboard } },
];
