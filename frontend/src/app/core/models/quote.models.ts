export interface CreateQuoteCoverageRequest {
  coverageCode: string;
  coverageName: string;
  requestedLimit: number;
  requestedDeductible: number;
  waitingPeriodDays?: number;
}

export interface CreateQuoteRequest {
  customerId: string;
  productCode: string;
  insuranceNeed: string;
  businessOperations: string;
  requestedEffectiveDate: string;
  coverages: CreateQuoteCoverageRequest[];
}

export interface DeclineQuoteRequest {
  reason: string;
}

export type UnderwritingDecisionType =
  | 'APPROVED'
  | 'APPROVED_WITH_MODIFIED_TERMS'
  | 'MORE_INFORMATION_REQUIRED'
  | 'REFERRED'
  | 'DECLINED';

export interface RecordUnderwritingDecisionRequest {
  decision: UnderwritingDecisionType;
  decisionReason: string;
  authorityLevel?: string;
  conditions?: string;
}

export interface OfferedCoverageRequest {
  coverageCode: string;
  offeredLimit: number;
  offeredDeductible: number;
  coveragePremium: number;
  conditions?: string;
  exclusions?: string;
  waitingPeriodDays?: number;
}

export interface OfferQuoteTermsRequest {
  quoteExpiresAt: string;
  coverages: OfferedCoverageRequest[];
}

export interface QuoteCoverageResponse {
  quoteCoverageId: string;
  quoteId: string;
  coverageCode: string;
  coverageName: string;
  requestedLimit: number;
  offeredLimit?: number | null;
  requestedDeductible: number;
  offeredDeductible?: number | null;
  coveragePremium?: number | null;
  conditions?: string | null;
  exclusions?: string | null;
  waitingPeriodDays?: number | null;
}

export interface QuoteResponse {
  quoteId: string;
  quoteNumber: string;
  customerId: string;
  productCode: string;
  insuranceNeed: string;
  businessOperations: string;
  status: string;
  requestedEffectiveDate: string;
  quoteExpiresAt?: string | null;
  assignedUnderwriterId?: string | null;
  riskAssessmentId?: string | null;
  totalPremium?: number | null;
  submittedAt?: string | null;
  quotedAt?: string | null;
  acceptedByUserId?: string | null;
  acceptedAt?: string | null;
  boundByUserId?: string | null;
  boundAt?: string | null;
  declineReason?: string | null;
  withdrawalReason?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  coverages: QuoteCoverageResponse[];
}

export interface UnderwritingDecisionResponse {
  underwritingDecisionId: string;
  quoteId: string;
  underwriterId?: string | null;
  decision: string;
  decisionReason?: string | null;
  authorityLevel?: string | null;
  conditions?: string | null;
  decidedAt?: string | null;
  createdAt?: string | null;
}

export interface SubjectivityResponse {
  subjectivityId: string;
  quoteId: string;
  subjectivityCode: string;
  description: string;
  status: string;
  satisfiedByUserId?: string | null;
  satisfiedAt?: string | null;
  evidenceDocumentIds?: string[] | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}
