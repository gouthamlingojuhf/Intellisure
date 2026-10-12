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
  assignedRiskEngineerId?: string | null;
  submittedAt?: string | null;
  updatedAt?: string | null;
}

export type FindingSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type ControlStatus = 'ADEQUATE' | 'PARTIALLY_ADEQUATE' | 'INADEQUATE' | 'NOT_PRESENT' | 'NOT_ASSESSED';
export type RecommendationPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type RecommendationStatus =
  | 'PROPOSED'
  | 'COMMUNICATED'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'VERIFIED'
  | 'OVERDUE';

export interface RiskFinding {
  findingId: string;
  assessmentId: string;
  findingType: string;
  description: string;
  severity: FindingSeverity;
  controlStatus: ControlStatus;
  evidenceDocumentIds?: string[] | null;
  createdBy: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateRiskFindingRequest {
  findingType: string;
  description: string;
  severity: FindingSeverity;
  controlStatus: ControlStatus;
  evidenceDocumentIds?: string[];
  createdBy: string;
}

export interface RiskRecommendation {
  recommendationId: string;
  assessmentId: string;
  recommendationType: string;
  description: string;
  priority: RecommendationPriority;
  status: RecommendationStatus;
  requiredBeforeBind: boolean;
  targetDate?: string | null;
  createdBy: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateRiskRecommendationRequest {
  recommendationType: string;
  description: string;
  priority: RecommendationPriority;
  requiredBeforeBind: boolean;
  targetDate?: string | null;
  createdBy: string;
}
