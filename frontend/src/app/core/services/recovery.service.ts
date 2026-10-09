import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  RecordRecoveryProgressRequest,
  RecoveryCaseListResponse,
  RecoveryCaseResponse,
  SelectRecoveryPathRequest,
} from '../models/recovery.models';

@Injectable({ providedIn: 'root' })
export class RecoveryService {
  private readonly api = inject(ApiService);

  getCases(customerId?: string): Observable<RecoveryCaseListResponse> {
    const params: Record<string, string | number> = { page: 0, size: 50 };
    if (customerId) params['customerId'] = customerId;
    return this.api.get<RecoveryCaseListResponse>('/api/recovery/cases', params);
  }

  selectPath(recoveryCaseId: string, request: SelectRecoveryPathRequest): Observable<RecoveryCaseResponse> {
    return this.api.post<RecoveryCaseResponse>(`/api/recovery/cases/${recoveryCaseId}/path`, request);
  }

  recordProgress(
    recoveryCaseId: string,
    request: RecordRecoveryProgressRequest
  ): Observable<RecoveryCaseResponse> {
    return this.api.post<RecoveryCaseResponse>(`/api/recovery/cases/${recoveryCaseId}/progress`, request);
  }
}
