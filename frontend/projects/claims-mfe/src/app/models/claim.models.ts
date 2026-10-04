export type ClaimStatus = 'FILED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'SETTLED';

export interface ClaimRecord {
  claimId: string;
  policyNumber: string;
  claimantName: string;
  claimType: string;
  lossDate: string;
  claimedAmount: number;
  status: ClaimStatus;
  adjuster: string;
  reserveAmount: number;
  description: string;
}
