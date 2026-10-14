import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  CreateRiskAssessmentRequest,
  CreateRiskFindingRequest,
  CreateRiskRecommendationRequest,
  RiskAssessment,
  RiskFinding,
  RiskRecommendation,
} from '../models/underwriting.models';

@Injectable({ providedIn: 'root' })
export class UnderwritingService {
  private readonly api = inject(ApiService);

  getAssignedQueue(userId: string, role: string | null): Observable<RiskAssessment[]> {
    const norm = (role || '').toUpperCase().replace(/^ROLE_/, '');
    const path = norm === 'RISK_ENGINEER'
      ? `/api/risk-assessments/risk-engineer/${userId}`
      : `/api/risk-assessments/underwriter/${userId}`;
    return this.api.get<RiskAssessment[]>(path);
  }

  createAssessment(req: CreateRiskAssessmentRequest): Observable<RiskAssessment> {
    return this.api.post<RiskAssessment>('/api/risk-assessments', req);
  }

  getAssessmentByQuoteId(quoteId: string): Observable<RiskAssessment> {
    return this.api.get<RiskAssessment>(`/api/risk-assessments/quote/${quoteId}`);
  }

  getAssessmentById(assessmentId: string): Observable<RiskAssessment> {
    return this.api.get<RiskAssessment>(`/api/risk-assessments/${assessmentId}`);
  }

  getFindings(assessmentId: string): Observable<RiskFinding[]> {
    return this.api.get<RiskFinding[]>(`/api/risk-assessments/${assessmentId}/findings`);
  }

  createFinding(assessmentId: string, req: CreateRiskFindingRequest): Observable<RiskFinding> {
    return this.api.post<RiskFinding>(`/api/risk-assessments/${assessmentId}/findings`, req);
  }

  getRecommendations(assessmentId: string): Observable<RiskRecommendation[]> {
    return this.api.get<RiskRecommendation[]>(`/api/risk-assessments/${assessmentId}/recommendations`);
  }

  createRecommendation(assessmentId: string, req: CreateRiskRecommendationRequest): Observable<RiskRecommendation> {
    return this.api.post<RiskRecommendation>(`/api/risk-assessments/${assessmentId}/recommendations`, req);
  }

  updateRecommendationStatus(recommendationId: string, status: string, verifiedBy?: string): Observable<RiskRecommendation> {
    return this.api.patch<RiskRecommendation>(
      `/api/risk-assessments/recommendations/${recommendationId}/status`,
      { status, verifiedBy }
    );
  }

  assignRiskEngineer(assessmentId: string, riskEngineerId?: string): Observable<RiskAssessment> {
    const params = riskEngineerId ? `?riskEngineerId=${riskEngineerId}` : '';
    return this.api.patch<RiskAssessment>(`/api/risk-assessments/${assessmentId}/assign-engineer${params}`, {});
  }

  autoAssignRiskEngineer(assessmentId: string): Observable<RiskAssessment> {
    return this.api.patch<RiskAssessment>(`/api/risk-assessments/${assessmentId}/auto-assign-engineer`, {});
  }

  getAvailableRiskEngineers(): Observable<any[]> {
    return this.api.get<any[]>('/api/users/role/RISK_ENGINEER/available');
  }
}
