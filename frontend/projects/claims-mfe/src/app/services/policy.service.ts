import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PolicyOption } from '../models/policy.models';

function authHeaders(): Record<string, string> {
  const headers: Record<string, string> = {};
  if (typeof localStorage !== 'undefined') {
    const token = localStorage.getItem('is_token');
    if (token) headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

@Injectable({ providedIn: 'root' })
export class PolicyService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  getCustomerPolicies(customerId: string): Observable<PolicyOption[]> {
    return this.http.get<PolicyOption[]>(`${this.base}/api/policies/customer/${customerId}`, {
      headers: authHeaders(),
    });
  }
}
