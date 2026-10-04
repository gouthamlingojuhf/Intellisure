import { Routes } from '@angular/router';
import { RiskAlertsComponent } from './features/risk-alerts/risk-alerts.component';
import { OperationalOverviewComponent } from './features/operational-overview/operational-overview.component';
import { IntelligenceDashboardComponent } from './features/intelligence-dashboard/intelligence-dashboard.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', component: IntelligenceDashboardComponent },
  { path: 'overview', component: OperationalOverviewComponent },
  { path: 'alerts', component: RiskAlertsComponent },
];
