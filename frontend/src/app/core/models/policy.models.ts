export interface PolicyCoverageResponse {
  policyCoverageId: string;
  coverageCode: string;
  coverageName: string;
  limitAmount: number;
  deductibleAmount: number;
  coveragePremium: number;
  conditions?: string | null;
  exclusions?: string | null;
  waitingPeriodDays?: number | null;
  effectiveFrom: string;
  effectiveTo: string;
  createdAt?: string | null;
}

export interface PolicyResponse {
  policyId: string;
  policyNumber: string;
  quoteId?: string | null;
  customerId: string;
  productCode: string;
  status: string;
  startDate: string;
  endDate: string;
  totalPremium: number;
  issuedByUserId?: string | null;
  boundAt?: string | null;
  issuedAt?: string | null;
  expiredAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  coverages: PolicyCoverageResponse[];
}
