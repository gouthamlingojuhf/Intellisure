import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { DocumentResponse } from '../models/document.models';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly api = inject(ApiService);

  getDocuments(entityId: string, entityType: string): Observable<DocumentResponse[]> {
    return this.api.get<DocumentResponse[]>('/api/documents', { entityId, entityType });
  }
}
