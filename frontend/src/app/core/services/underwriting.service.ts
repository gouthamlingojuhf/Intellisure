import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { RiskAssessment } from '../models/underwriting.models';

@Injectable({ providedIn: 'root' })
export class UnderwritingService {
  private readonly api = inject(ApiService);

  getAssignedQueue(userId: string, role: string | null): Observable<RiskAssessment[]> {
    const path = role?.toUpperCase() === 'RISK_ENGINEER'
      ? `/api/risk-assessments/risk-engineer/${userId}`
      : `/api/risk-assessments/underwriter/${userId}`;
    return this.api.get<RiskAssessment[]>(path);
  }
}
