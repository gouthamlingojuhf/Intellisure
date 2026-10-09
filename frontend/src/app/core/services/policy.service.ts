import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { PolicyResponse } from '../models/policy.models';

@Injectable({ providedIn: 'root' })
export class PolicyService {
  private readonly api = inject(ApiService);

  getPoliciesByCustomerId(customerId: string): Observable<PolicyResponse[]> {
    return this.api.get<PolicyResponse[]>(`/api/policies/customer/${customerId}`);
  }

  getPolicyById(policyId: string): Observable<PolicyResponse> {
    return this.api.get<PolicyResponse>(`/api/policies/${policyId}`);
  }

  getPolicyByNumber(policyNumber: string): Observable<PolicyResponse> {
    return this.api.get<PolicyResponse>(`/api/policies/number/${policyNumber}`);
  }

  getAllPolicies(): Observable<PolicyResponse[]> {
    return this.api.get<PolicyResponse[]>('/api/policies/admin');
  }
}
