import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CreateRecoveryCaseRequest,
  RecoveryCaseListResponse,
  RecoveryCaseResponse,
} from '../models/recovery.models';

function authHeaders(): Record<string, string> {
  const correlationId = typeof crypto !== 'undefined' && 'randomUUID' in crypto
    ? crypto.randomUUID()
    : 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
      const r = (Math.random() * 16) | 0;
      return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
    });
  const headers: Record<string, string> = { 'X-Correlation-ID': correlationId };
  if (typeof localStorage !== 'undefined') {
    const token = localStorage.getItem('is_token');
    if (token) headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

@Injectable({ providedIn: 'root' })
export class RecoveryService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  getCases(customerId: string): Observable<RecoveryCaseListResponse> {
    const params = new HttpParams()
      .set('customerId', customerId)
      .set('page', '0')
      .set('size', '50');
    return this.http.get<RecoveryCaseListResponse>(`${this.base}/api/recovery/cases`, {
      headers: authHeaders(),
      params,
    });
  }

  createCase(request: CreateRecoveryCaseRequest): Observable<RecoveryCaseResponse> {
    return this.http.post<RecoveryCaseResponse>(`${this.base}/api/recovery/cases`, request, {
      headers: authHeaders(),
    });
  }
}
