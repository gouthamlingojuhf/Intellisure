export type RecoverySeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface RecoveryCaseResponse {
  recoveryCaseId: string;
  claimId: string;
  customerId: string;
  severity: RecoverySeverity;
  status: string;
  recoveryPath: string;
  recoveryObjective: string;
  currentRestorePercent: number;
}

export interface RecoveryCaseListResponse {
  items: RecoveryCaseResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface CreateRecoveryCaseRequest {
  claimId: string;
  customerId: string;
  severity: RecoverySeverity;
  recoveryObjective: string;
}
