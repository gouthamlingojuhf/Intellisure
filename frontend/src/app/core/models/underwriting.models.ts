export type RiskAssessmentStatus =
  | 'DRAFT'
  | 'IN_PROGRESS'
  | 'NEEDS_INFORMATION'
  | 'RISK_ENGINEERING_REQUIRED'
  | 'UNDER_REVIEW'
  | 'REFERRED'
  | 'COMPLETED'
  | 'CANCELLED';

export interface RiskAssessment {
  assessmentId: string;
  assessmentNumber?: string | null;
  quoteId: string;
  policyId?: string | null;
  customerId: string;
  assessmentType?: string | null;
  status: RiskAssessmentStatus;
  assessmentDate?: string | null;
  location?: string | null;
  businessOperations?: string | null;
  riskScore?: number | null;
  riskBand?: string | null;
  summary?: string | null;
  assignedUnderwriterId?: string | null;
  submittedAt?: string | null;
  updatedAt?: string | null;
}
