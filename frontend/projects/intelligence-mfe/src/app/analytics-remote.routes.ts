import { Routes } from '@angular/router';
import { IntelligenceDashboardComponent } from './features/intelligence-dashboard/intelligence-dashboard.component';
import { OperationalOverviewComponent } from './features/operational-overview/operational-overview.component';
import { RiskAlertsComponent } from './features/risk-alerts/risk-alerts.component';

/** Exposed to the shell host as `intelligenceMfe/Routes`. */
export const ANALYTICS_REMOTE_ROUTES: Routes = [
  { path: '', pathMatch: 'full', component: IntelligenceDashboardComponent },
  { path: 'overview', component: OperationalOverviewComponent },
  { path: 'alerts', component: RiskAlertsComponent },
];
