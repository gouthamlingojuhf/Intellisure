import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ClaimResponse, FileClaimRequest } from '../models/claim.models';

const CORRELATION_ID_HEADER = 'X-Correlation-ID';

function correlationId(): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) return crypto.randomUUID();
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
}

function authHeaders(): Record<string, string> {
  const headers: Record<string, string> = { [CORRELATION_ID_HEADER]: correlationId() };
  if (typeof localStorage !== 'undefined') {
    const token = localStorage.getItem('is_token');
    if (token) headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

@Injectable({ providedIn: 'root' })
export class ClaimsService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  /**
   * GET /api/claims (optionally filtered by status or customerId).
   * For policyholders, the backend automatically scopes to the caller's customer ID from the JWT.
   */
  getClaims(status?: string, customerId?: string): Observable<ClaimResponse[]> {
    let params = new HttpParams();
    if (status && status.trim() !== '') params = params.set('status', status.trim());
    if (customerId && customerId.trim() !== '') params = params.set('customerId', customerId.trim());

    return this.http.get<ClaimResponse[]>(`${this.base}/api/claims`, {
      headers: authHeaders(),
      params,
    });
  }

  /**
   * GET /api/claims/${claimId}
   */
  getClaim(claimId: string): Observable<ClaimResponse> {
    return this.http.get<ClaimResponse>(`${this.base}/api/claims/${claimId}`, {
      headers: authHeaders(),
    });
  }

  /**
   * POST /api/claims
   */
  fileClaim(request: FileClaimRequest): Observable<ClaimResponse> {
    return this.http.post<ClaimResponse>(`${this.base}/api/claims`, request, {
      headers: authHeaders(),
    });
  }

  /**
   * PATCH /api/claims/${claimId}/status?value=${status}
   */
  updateStatus(claimId: string, status: string): Observable<ClaimResponse> {
    const params = new HttpParams().set('value', status);
    return this.http.patch<ClaimResponse>(
      `${this.base}/api/claims/${claimId}/status`,
      {},
      {
        headers: authHeaders(),
        params,
      }
    );
  }

  createReserve(claimId: string, reserve: number, reason: string): Observable<ClaimResponse> {
    const params = new HttpParams()
      .set('reserve', reserve.toString())
      .set('reserveReason', reason);
    return this.http.post<ClaimResponse>(
      `${this.base}/api/claims/${claimId}/reserve`,
      {},
      { headers: authHeaders(), params }
    );
  }

  createAssessment(
    claimId: string,
    causeOfLoss: string,
    findings: string,
    covered: boolean,
    totalLossAmount: number,
    coveredLossAmount: number,
    deductibleApplied: number,
    netLossAmount: number
  ): Observable<ClaimResponse> {
    const params = new HttpParams()
      .set('causeOfLoss', causeOfLoss)
      .set('findings', findings)
      .set('covered', covered.toString())
      .set('totalLossAmount', totalLossAmount.toString())
      .set('coveredLossAmount', coveredLossAmount.toString())
      .set('deductibleApplied', deductibleApplied.toString())
      .set('netLossAmount', netLossAmount.toString());
    return this.http.post<ClaimResponse>(
      `${this.base}/api/claims/${claimId}/assessment`,
      {},
      { headers: authHeaders(), params }
    );
  }

  recordDecision(claimId: string, decision: string, reason?: string): Observable<ClaimResponse> {
    let params = new HttpParams().set('decision', decision);
    if (reason) params = params.set('reason', reason);
    return this.http.post<ClaimResponse>(
      `${this.base}/api/claims/${claimId}/decision`,
      {},
      { headers: authHeaders(), params }
    );
  }

  approveSettlement(claimId: string, approvedAmount: number): Observable<ClaimResponse> {
    const userId =
      (typeof localStorage !== 'undefined' ? localStorage.getItem('is_user_id') : null) ||
      '00000000-0000-0000-0000-000000000000';
    const params = new HttpParams()
      .set('approvedAmount', approvedAmount.toString())
      .set('approvedBy', userId);
    return this.http.patch<ClaimResponse>(
      `${this.base}/api/claims/${claimId}/settlement/approve`,
      {},
      { headers: authHeaders(), params }
    );
  }

  closeClaim(claimId: string, reason: string): Observable<ClaimResponse> {
    const params = new HttpParams().set('reason', reason);
    return this.http.patch<ClaimResponse>(
      `${this.base}/api/claims/${claimId}/close`,
      {},
      { headers: authHeaders(), params }
    );
  }

  recordPayment(claimId: string, paymentReference: string): Observable<ClaimResponse> {
    const params = new HttpParams().set('paymentReference', paymentReference);
    return this.http.patch<ClaimResponse>(
      `${this.base}/api/claims/${claimId}/payment`,
      {},
      { headers: authHeaders(), params }
    );
  }

  assignAdjuster(claimId: string, adjusterId: string): Observable<ClaimResponse> {
    return this.http.patch<ClaimResponse>(
      `${this.base}/api/claims/${claimId}/assign-adjuster`,
      { adjusterId },
      { headers: authHeaders() }
    );
  }

  autoAssignAdjuster(claimId: string): Observable<ClaimResponse> {
    return this.http.patch<ClaimResponse>(
      `${this.base}/api/claims/${claimId}/auto-assign-adjuster`,
      {},
      { headers: authHeaders() }
    );
  }

  getAvailableAdjusters(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.base}/api/users/role/CLAIMS_ADJUSTER/available`,
      { headers: authHeaders() }
    );
  }

  getUserById(userId: string): Observable<any> {
    return this.http.get<any>(
      `${this.base}/api/users/${userId}`,
      { headers: authHeaders() }
    );
  }
}
