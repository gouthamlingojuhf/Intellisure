import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ExecutiveDashboardSummary } from '../models/analytics.models';

@Injectable({ providedIn: 'root' })
export class AnalyticsApiService {
  private readonly http = inject(HttpClient);
  private readonly gateway = 'http://localhost:8080';

  getDashboardSummary(): Observable<ExecutiveDashboardSummary> {
    return this.http.get<ExecutiveDashboardSummary>(`${this.gateway}/api/analytics/dashboard/summary`, {
      headers: this.authHeaders(),
    });
  }

  private authHeaders(): HttpHeaders {
    const token = typeof localStorage !== 'undefined' ? localStorage.getItem('is_token') : null;
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
