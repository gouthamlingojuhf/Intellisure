import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { CustomerResponse, UpdateCustomerProfileRequest } from '../models/customer.models';

@Injectable({ providedIn: 'root' })
export class CustomerProfileService {
  private readonly api = inject(ApiService);

  /**
   * GET /api/customers/me
   * Fetches the current authenticated user's BusinessCustomer profile.
   * If not created yet, the backend returns 404 (ResourceNotFoundException).
   */
  getProfile(): Observable<CustomerResponse> {
    return this.api.get<CustomerResponse>('/api/customers/me').pipe(
      tap((profile) => {
        if (profile?.customerId && typeof localStorage !== 'undefined') {
          localStorage.setItem('is_customer_id', profile.customerId);
        }
      })
    );
  }

  /**
   * PUT /api/customers/me
   * Creates or updates the authenticated user's BusinessCustomer profile.
   * If new, the backend creates a BusinessCustomer entity with a generated customerId.
   */
  updateProfile(request: UpdateCustomerProfileRequest): Observable<CustomerResponse> {
    return this.api.put<CustomerResponse>('/api/customers/me', request).pipe(
      tap((profile) => {
        if (profile?.customerId && typeof localStorage !== 'undefined') {
          localStorage.setItem('is_customer_id', profile.customerId);
        }
      })
    );
  }
}
