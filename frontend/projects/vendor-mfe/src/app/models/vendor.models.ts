// Vendor DTOs — must match vendor-partner-service JSON contracts via gateway :8080.
// Backend: VendorAssignmentController (/api/vendor-assignments), VendorController,
// VendorOnboardingController, VendorPerformanceController (/api/vendors/**).

export interface VendorAssignmentResponse {
  assignmentId: string;
  vendorId: string;
  assignmentType: string;
  claimId: string | null;
  recoveryCaseId: string | null;
  status: string;
  taskDescription: string;
  dueDate: string;
  priority: string;
  acceptedAt: string | null;
  completedAt: string | null;
  evidenceDocumentIds: string[] | null;
  createdAt: string;
  updatedAt: string;
}

export interface VendorAssignmentListResponse {
  items: VendorAssignmentResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface CreateVendorAssignmentRequest {
  vendorId: string;
  assignmentType: string;
  claimId?: string | null;
  recoveryCaseId?: string | null;
  recoveryPath?: 'NETWORK_VENDOR' | null;
  taskDescription: string;
  dueDate: string;
  priority: string;
}

export interface AcceptAssignmentRequest {
  acceptanceNote?: string;
  expectedStartDate?: string;
}

export interface DeclineAssignmentRequest {
  declineReason: string;
}

export interface UpdateAssignmentStatusRequest {
  status: string;
  progressNote?: string;
  completionDate?: string;
  evidenceDocumentIds?: string[];
}

export interface VendorResponse {
  vendorId: string;
  legalName: string;
  displayName: string;
  vendorType: string;
  serviceTypes: string[];
  capabilities: string[];
  serviceAreas: string[];
  verificationStatus: string;
  activeStatus: string;
  contactPhone: string;
  contactEmail: string;
}

export interface VendorOnboardingResponse {
  onboardingRequestId: string;
  vendorId: string;
  status: string;
  submittedAt: string;
  reviewedAt: string | null;
  reviewerId: string | null;
  rejectionReason: string | null;
}

export interface VendorOnboardingRequest {
  legalName: string;
  displayName: string;
  vendorType: string;
  serviceTypes: string[];
  capabilities: string[];
  serviceAreas: string[];
  contactName: string;
  contactPhone: string;
  contactEmail: string;
  documentIds?: string[];
}

export interface VerifyVendorRequest {
  verificationDecision: 'APPROVE' | 'REJECT';
  verificationNote?: string;
  verifiedDocumentIds?: string[];
}

export interface RecordVendorPerformanceRequest {
  assignmentId: string;
  qualityScore: number;
  timelinessScore: number;
  communicationScore: number;
  outcomeScore: number;
  note?: string;
}

export interface VendorPerformanceResponse {
  vendorId: string;
  assignmentId: string;
  qualityScore: number;
  timelinessScore: number;
  communicationScore: number;
  outcomeScore: number;
  overallScore: number;
  note?: string | null;
  recordedAt: string;
}
