import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AcceptAssignmentRequest,
  CreateVendorAssignmentRequest,
  DeclineAssignmentRequest,
  UpdateAssignmentStatusRequest,
  VendorAssignmentListResponse,
  VendorAssignmentResponse,
  VendorOnboardingRequest,
  VendorOnboardingResponse,
  RecordVendorPerformanceRequest,
  VendorPerformanceResponse,
  VendorResponse,
  VerifyVendorRequest,
  ClaimReference,
  RecoveryCaseReference,
} from '../models/vendor.models';

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

/** Standalone remote client: gateway :8080, JWT + correlation ID per request. */
@Injectable({ providedIn: 'root' })
export class VendorApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  createAssignment(request: CreateVendorAssignmentRequest): Observable<VendorAssignmentResponse> {
    return this.http.post<VendorAssignmentResponse>(`${this.base}/api/vendor-assignments`, request, {
      headers: authHeaders(),
    });
  }

  getAssignment(assignmentId: string): Observable<VendorAssignmentResponse> {
    return this.http.get<VendorAssignmentResponse>(`${this.base}/api/vendor-assignments/${assignmentId}`, {
      headers: authHeaders(),
    });
  }

  getAssignments(params: {
    vendorId?: string;
    claimId?: string;
    status?: string;
    page?: number;
    size?: number;
  }): Observable<VendorAssignmentListResponse> {
    let httpParams = new HttpParams();
    for (const [k, v] of Object.entries(params)) {
      if (v !== undefined) httpParams = httpParams.set(k, String(v));
    }
    return this.http.get<VendorAssignmentListResponse>(`${this.base}/api/vendor-assignments`, {
      headers: authHeaders(),
      params: httpParams,
    });
  }

  acceptAssignment(assignmentId: string, request: AcceptAssignmentRequest): Observable<VendorAssignmentResponse> {
    return this.http.post<VendorAssignmentResponse>(
      `${this.base}/api/vendor-assignments/${assignmentId}/accept`,
      request,
      { headers: authHeaders() }
    );
  }

  declineAssignment(assignmentId: string, request: DeclineAssignmentRequest): Observable<VendorAssignmentResponse> {
    return this.http.post<VendorAssignmentResponse>(
      `${this.base}/api/vendor-assignments/${assignmentId}/decline`,
      request,
      { headers: authHeaders() }
    );
  }

  updateAssignmentStatus(
    assignmentId: string,
    request: UpdateAssignmentStatusRequest
  ): Observable<VendorAssignmentResponse> {
    return this.http.patch<VendorAssignmentResponse>(
      `${this.base}/api/vendor-assignments/${assignmentId}/status`,
      request,
      { headers: authHeaders() }
    );
  }

  searchVendors(params: { serviceType?: string; location?: string; page?: number; size?: number }): Observable<{
    items: VendorResponse[];
    page: number;
    size: number;
    totalElements: number;
  }> {
    let httpParams = new HttpParams();
    for (const [k, v] of Object.entries(params)) {
      if (v !== undefined) httpParams = httpParams.set(k, String(v));
    }
    return this.http.get<{ items: VendorResponse[]; page: number; size: number; totalElements: number }>(
      `${this.base}/api/vendors`,
      { headers: authHeaders(), params: httpParams }
    );
  }

  submitOnboarding(request: VendorOnboardingRequest): Observable<VendorOnboardingResponse> {
    return this.http.post<VendorOnboardingResponse>(`${this.base}/api/vendors/onboarding-requests`, request, {
      headers: authHeaders(),
    });
  }

  getOnboardingRequests(): Observable<VendorOnboardingResponse[]> {
    return this.http.get<VendorOnboardingResponse[]>(`${this.base}/api/vendors/onboarding-requests`, {
      headers: authHeaders(),
    });
  }

  verifyVendor(vendorId: string, request: VerifyVendorRequest): Observable<VendorOnboardingResponse> {
    return this.http.post<VendorOnboardingResponse>(`${this.base}/api/vendors/${vendorId}/verify`, request, {
      headers: authHeaders(),
    });
  }

  recordPerformance(vendorId: string, request: RecordVendorPerformanceRequest): Observable<VendorPerformanceResponse> {
    return this.http.post<VendorPerformanceResponse>(`${this.base}/api/vendors/${vendorId}/performance`, request, {
      headers: authHeaders(),
    });
  }

  getPerformance(vendorId: string): Observable<VendorPerformanceResponse[]> {
    return this.http.get<VendorPerformanceResponse[]>(`${this.base}/api/vendors/${vendorId}/performance`, {
      headers: authHeaders(),
    });
  }

  getClaimReferences(): Observable<ClaimReference[]> {
    return this.http.get<ClaimReference[]>(`${this.base}/api/claims`, { headers: authHeaders() });
  }

  getRecoveryCaseReferences(): Observable<{ items: RecoveryCaseReference[] }> {
    return this.http.get<{ items: RecoveryCaseReference[] }>(`${this.base}/api/recovery/cases?page=0&size=100`, { headers: authHeaders() });
  }
}
