export type RecoveryPath = 'NETWORK_VENDOR' | 'CUSTOMER_VENDOR' | 'CUSTOMER_MANAGED';

export interface RecoveryCaseResponse {
  recoveryCaseId: string;
  claimId: string;
  customerId: string;
  severity: string;
  status: string;
  recoveryPath: RecoveryPath;
  recoveryObjective: string;
  recoveryNotes?: string | null;
  targetRestoreDate?: string | null;
  actualRestorationDate?: string | null;
  currentRestorePercent: number;
  ownerId?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface RecoveryCaseListResponse {
  items: RecoveryCaseResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface SelectRecoveryPathRequest {
  recoveryPath: RecoveryPath;
  notes?: string;
}

export interface RecordRecoveryProgressRequest {
  restorePercent: number;
  notes?: string;
}
