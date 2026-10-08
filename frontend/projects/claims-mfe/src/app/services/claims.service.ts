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
}
