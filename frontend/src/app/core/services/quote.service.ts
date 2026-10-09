import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  CreateQuoteRequest,
  DeclineQuoteRequest,
  OfferQuoteTermsRequest,
  QuoteResponse,
  RecordUnderwritingDecisionRequest,
  SubjectivityResponse,
  UnderwritingDecisionResponse,
} from '../models/quote.models';

@Injectable({ providedIn: 'root' })
export class QuoteService {
  private readonly api = inject(ApiService);

  createDraftQuote(request: CreateQuoteRequest): Observable<QuoteResponse> {
    return this.api.post<QuoteResponse>('/api/quotes', request);
  }

  getQuoteById(quoteId: string): Observable<QuoteResponse> {
    return this.api.get<QuoteResponse>(`/api/quotes/${quoteId}`);
  }

  getQuotesByCustomerId(customerId: string): Observable<QuoteResponse[]> {
    return this.api.get<QuoteResponse[]>(`/api/quotes/customer/${customerId}`);
  }

  submitQuote(quoteId: string): Observable<QuoteResponse> {
    return this.api.post<QuoteResponse>(`/api/quotes/${quoteId}/submit`, {});
  }

  acceptQuote(quoteId: string): Observable<QuoteResponse> {
    return this.api.post<QuoteResponse>(`/api/quotes/${quoteId}/accept`, {});
  }

  declineQuote(quoteId: string, reason: string): Observable<QuoteResponse> {
    const body: DeclineQuoteRequest = { reason };
    return this.api.patch<QuoteResponse>(`/api/quotes/${quoteId}/decline`, body);
  }

  getUnderwritingDecisions(quoteId: string): Observable<UnderwritingDecisionResponse[]> {
    return this.api.get<UnderwritingDecisionResponse[]>(`/api/quotes/${quoteId}/underwriting-decisions`);
  }

  recordUnderwritingDecision(quoteId: string, request: RecordUnderwritingDecisionRequest): Observable<UnderwritingDecisionResponse> {
    return this.api.post<UnderwritingDecisionResponse>(`/api/quotes/${quoteId}/underwriting-decisions`, request);
  }

  offerQuoteTerms(quoteId: string, request: OfferQuoteTermsRequest): Observable<QuoteResponse> {
    return this.api.patch<QuoteResponse>(`/api/quotes/${quoteId}/offer-terms`, request);
  }

  getSubjectivities(quoteId: string): Observable<SubjectivityResponse[]> {
    return this.api.get<SubjectivityResponse[]>(`/api/quotes/${quoteId}/subjectivities`);
  }
}
