// Backend DTO matching FileClaimRequest in claims-service
export interface FileClaimRequest {
  policyId: string;
  incidentDate: string; // ISO format: YYYY-MM-DD
  description: string;
  estimatedLoss?: number;
}

// Backend DTO matching ClaimResponse in claims-service
export interface ClaimResponse {
  claimId: string;
  policyId: string;
  customerId: string;
  policyNumber?: string;
  claimNumber: string;
  status: string;
  incidentDate: string;
  reportedDate: string;
  description: string;
  incidentDescription?: string;
  estimatedLoss: number;
  estimatedCoveredLoss?: number;
  payoutAmount?: number;
  payableAmount?: number;
  createdAt: string;
  updatedAt: string;
  incidentType?: string;
  incidentLocation?: string;
  coverageDecision?: string;
  coverageDecisionReason?: string;
  assignedAdjusterId?: string;
  coverageConfirmed?: boolean;
  closureReason?: string;
}

// Backward-compatible type alias for existing components
export type ClaimRecord = ClaimResponse;
export type ClaimStatus = string;
