// DTOs and interfaces matching backend JSON contracts via gateway :8080

import { UserRole, ClaimStatus, QuoteStatus, PolicyStatus, VendorType, VendorVerificationStatus, VendorActiveStatus, AssignmentType, AssignmentStatus, OnboardingStatus, RecoverySeverity, RecoveryCaseStatus, RecoveryPlanStatus, RecoverySupportStatus, SupportType, SupportPriority, DocumentType, NotificationType, NotificationChannel, AuditActor, WorkflowStatus, WorkflowTaskStatus } from './enums';

// ============ AUTH DTOs ============

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken?: string;
  token?: string;
  tokenType?: string;
  expiresIn?: number;
  userId?: string;
  customerId?: string;
  email?: string;
  role?: UserRole | string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

export interface UserProfile {
  userId?: string;
  email?: string;
  displayName?: string;
  role?: UserRole;
  accountStatus?: string;
}

// ============ CUSTOMER PARTY DTOs ============

export interface CustomerResponse {
  customerId: string;
  userId: string;
  businessName: string;
  ownerName: string;
  phone?: string | null;
  businessType?: string | null;
  address?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  postalCode?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CustomerProfile {
  customerId: string;
  email: string;
  displayName: string;
  role: UserRole;
  accountStatus: string;
  createdAt: string;
  updatedAt: string;
}

export interface BusinessCustomerProfile {
  businessId: string;
  legalName: string;
  displayName: string;
  businessType: string;
  taxId: string;
  contactName: string;
  contactPhone: string;
  contactEmail: string;
  address: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  annualRevenue: number;
  employeeCount: number;
  businessOperations: string;
  riskProfile: string;
  createdAt: string;
  updatedAt: string;
}

export interface AdminUserRequest {
  email: string;
  password: string;
  displayName: string;
  role: UserRole;
}

export interface RoleAssignmentRequest {
  userId: string;
  roles: UserRole[];
}

export interface UpdateUserStatusRequest {
  status: string;
}

export interface UpdateCustomerProfileRequest {
  businessName: string;
  ownerName: string;
  phone?: string | null;
  businessType?: string | null;
  address?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  postalCode?: string | null;
}

// ============ QUOTE & POLICY DTOs ============

export interface CreateQuoteRequest {
  customerId: string;
  productCode: string;
  coverageAmount: number;
  deductible: number;
  propertyOrVehicleDetails: string;
}

export interface QuoteResponse {
  quoteId: string;
  quoteNumber: string;
  customerId: string;
  productCode: string;
  status: string;
  totalPremium: number;
  riskScore: number;
  quotedAt: string;
  expiresAt: string;
}

export interface SubmitQuoteRequest {
  quoteId: string;
}

export interface ApproveQuoteRequest {
  quoteId: string;
  approvedBy: string;
  notes?: string;
}

export interface RejectQuoteRequest {
  quoteId: string;
  rejectedBy: string;
  reason: string;
}

export interface IssuePolicyRequest {
  quoteId: string;
  issuedBy: string;
}

export interface PolicyResponse {
  policyId: string;
  policyNumber: string;
  quoteId: string;
  customerId: string;
  productCode: string;
  status: PolicyStatus;
  startDate: string;
  endDate: string;
  totalPremium: number;
  issuedAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface EndorsementResponse {
  endorsementId: string;
  endorsementNumber: string;
  policyId: string;
  endorsementType: string;
  description: string;
  premiumDelta: number;
  status: string;
  effectiveFrom: string;
  effectiveTo: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEndorsementRequest {
  policyId: string;
  endorsementType: string;
  description: string;
  premiumDelta: number;
}

export interface RenewalResponse {
  renewalId: string;
  policyId: string;
  status: string;
  proposedStartDate: string;
  proposedEndDate: string;
  proposedTotalPremium: number;
  proposedCoverageSnapshot: string;
  subjectivities: string[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateEndorsementRequest {
  policyId: string;
  endorsementType: string;
  description: string;
  premiumDelta: number;
}

export interface SubjectivityResponse {
  subjectivityId: string;
  quoteId: string;
  description: string;
  status: string;
  dueDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface QuoteCoverageResponse {
  coverageId: string;
  quoteId: string;
  coverageCode: string;
  coverageName: string;
  requestedLimit: number;
  offeredLimit: number;
  requestedDeductible: number;
  offeredDeductible: number;
  coveragePremium: number;
  conditions: string;
  exclusions: string;
  waitingPeriodDays: number;
}

export interface QuoteVersionResponse {
  versionId: string;
  quoteId: string;
  version: number;
  totalPremium: number;
  coverageSnapshot: string;
  offeredByUserId: string;
  offeredAt: string;
}

// ============ CLAIMS DTOs ============

export interface CreateFNOLRequest {
  policyId: string;
  incidentDate: string;
  description: string;
  estimatedLoss: number;
}

export interface ClaimResponse {
  claimId: string;
  claimNumber: string;
  policyId: string;
  customerId: string;
  incidentDate: string;
  description: string;
  estimatedLoss: number;
  status: ClaimStatus;
  assignedAdjusterId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateClaimStatusRequest {
  status: ClaimStatus;
  notes?: string;
}

export interface AssignClaimRequest {
  claimId: string;
  adjusterId: string;
}

export interface CoverageDecisionResponse {
  decisionId: string;
  claimId: string;
  coverageCode: string;
  decision: string;
  reason: string;
  decidedBy: string;
  decidedAt: string;
}

export interface ClaimFinancialsResponse {
  financialId: string;
  claimId: string;
  reserveAmount: number;
  paidAmount: number;
  incurredAmount: number;
  outstandingReserve: number;
  lastUpdated: string;
}

export interface PaymentResponse {
  paymentId: string;
  claimId: string;
  amount: number;
  paymentType: string;
  status: string;
  processedAt: string;
  referenceNumber: string;
}

export interface SalvageResponse {
  salvageId: string;
  claimId: string;
  description: string;
  estimatedValue: number;
  actualValue: number;
  status: string;
  soldAt?: string;
}

export interface SubrogationResponse {
  subrogationId: string;
  claimId: string;
  thirdPartyCarrier: string;
  amountClaimed: number;
  amountRecovered: number;
  status: string;
  filedAt: string;
  recoveredAt?: string;
}

export interface BusinessIncomeResponse {
  businessIncomeId: string;
  claimId: string;
  monthlyRevenueLoss: number;
  periodStart: string;
  periodEnd: string;
  calculatedAmount: number;
  status: string;
}

export interface ClaimAssessmentResponse {
  assessmentId: string;
  claimId: string;
  assessorId: string;
  assessmentDate: string;
  findings: string;
  recommendedAmount: number;
  status: string;
}

export interface ClaimDocumentResponse {
  documentId: string;
  claimId: string;
  documentType: string;
  fileName: string;
  contentType: string;
  fileSize: number;
  uploadedBy: string;
  uploadedAt: string;
}

// ============ VENDOR & PARTNER DTOs ============

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

export interface VendorSearchRequest {
  serviceType?: string;
  location?: string;
  radiusKm?: number;
  capability?: string;
  availabilityStatus?: string;
  page?: number;
  size?: number;
}

export interface VendorListResponse {
  items: VendorResponse[];
  page: number;
  size: number;
  totalElements: number;
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
  documentIds: string[];
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

export interface VendorOnboardingListResponse {
  items: VendorOnboardingResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface VerifyVendorRequest {
  verificationDecision: 'APPROVE' | 'REJECT';
  verificationNote?: string;
  verifiedDocumentIds?: string[];
}

export interface UpdateVendorStatusRequest {
  activeStatus: string;
  statusReason?: string;
}

export interface UpdateVendorRequest {
  displayName?: string;
  serviceTypes?: string[];
  capabilities?: string[];
  serviceAreas?: string[];
  contactName?: string;
  contactPhone?: string;
  contactEmail?: string;
}

export interface CreateVendorAssignmentRequest {
  vendorId: string;
  assignmentType: string;
  claimId?: string;
  recoveryCaseId?: string;
  taskDescription: string;
  dueDate: string;
  priority: string;
}

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

export interface VendorAssignmentFilterRequest {
  vendorId?: string;
  claimId?: string;
  recoveryCaseId?: string;
  assignmentType?: string;
  status?: string;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
}

export interface VendorAssignmentListResponse {
  items: VendorAssignmentResponse[];
  page: number;
  size: number;
  totalElements: number;
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
  note?: string;
  recordedAt: string;
}

// ============ RECOVERY DTOs ============

export interface CreateRecoveryCaseRequest {
  claimId: string;
  customerId: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  recoveryObjective: string;
  targetRestoreDate: string;
  ownerId?: string;
}

export interface RecoveryCaseResponse {
  recoveryCaseId: string;
  claimId: string;
  customerId: string;
  severity: string;
  status: string;
  recoveryObjective: string;
  targetRestoreDate: string;
  currentRestorePercent: number;
  ownerId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface RecoveryCaseFilterRequest {
  customerId?: string;
  ownerId?: string;
  status?: string;
  severity?: string;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
}

export interface RecoveryCaseListResponse {
  items: RecoveryCaseResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface UpdateRecoveryCaseRequest {
  severity?: string;
  status?: string;
  recoveryObjective?: string;
  targetRestoreDate?: string;
  currentRestorePercent?: number;
  ownerId?: string;
}

export interface UpdateRecoveryStatusRequest {
  status: string;
}

export interface CreateRecoveryPlanRequest {
  recoveryCaseId: string;
  planSummary: string;
  priorityActions: string[];
  vendorAssignmentIds: string[];
  temporaryResourceNeeds: string[];
  targetMilestones: string;
}

export interface RecoveryPlanResponse {
  recoveryPlanId: string;
  recoveryCaseId: string;
  planSummary: string;
  priorityActions: string[];
  vendorAssignmentIds: string[];
  temporaryResourceNeeds: string[];
  targetMilestones: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateRecoveryPlanRequest {
  planSummary?: string;
  priorityActions?: string[];
  vendorAssignmentIds?: string[];
  temporaryResourceNeeds?: string[];
  targetMilestones?: string;
  status?: string;
}

export interface CompleteRecoveryCaseRequest {
  completionSummary: string;
  outcome: string;
}

export interface CreateRecoverySupportRequest {
  recoveryCaseId: string;
  supportType: string;
  description: string;
  priority: string;
  requiredByDate: string;
  location: string;
  vendorAssignmentId?: string;
}

export interface RecoverySupportResponse {
  supportRequestId: string;
  recoveryCaseId: string;
  supportType: string;
  description: string;
  priority: string;
  status: string;
  requiredByDate: string;
  location: string;
  vendorAssignmentId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface RecoverySupportListResponse {
  items: RecoverySupportResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface UpdateRecoverySupportStatusRequest {
  status: string;
}

// ============ DOCUMENT & AUDIT DTOs ============

export interface CreateDocumentMetadataRequest {
  entityId: string;
  entityType: string;
  documentType: DocumentType;
  fileName: string;
  fileSize: number;
  contentType: string;
  storagePath: string;
  sha256Hash?: string;
  version?: number;
}

export interface DocumentResponse {
  documentId: string;
  entityId: string;
  entityType: string;
  documentType: DocumentType;
  fileName: string;
  fileSize: number;
  contentType: string;
  storagePath: string;
  sha256Hash?: string;
  version: number;
  uploadedBy: string;
  createdAt: string;
}

export interface DocumentSearchRequest {
  entityId?: string;
  entityType?: string;
  documentType?: DocumentType;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
}

export interface DocumentListResponse {
  items: DocumentResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface DocumentDownloadResponse {
  documentId: string;
  downloadUrl: string;
  expiresAt: string;
}

export interface CreateAuditEventRequest {
  serviceName: string;
  entityId: string;
  entityType: string;
  action: string;
  userId?: string;
  eventDetails?: string;
  ipAddress?: string;
}

export interface AuditEventResponse {
  auditEventId: string;
  serviceName: string;
  entityId: string;
  entityType: string;
  action: string;
  userId: string | null;
  eventDetails: string | null;
  ipAddress: string | null;
  createdAt: string;
}

export interface AuditEventFilterRequest {
  entityId?: string;
  entityType?: string;
  action?: string;
  serviceName?: string;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
}

export interface AuditEventListResponse {
  items: AuditEventResponse[];
  page: number;
  size: number;
  totalElements: number;
}

// ============ ANALYTICS DTOs ============

export interface LossRatioMetricsResponse {
  metricsId: string;
  totalEarnedPremium: number;
  totalIncurredClaims: number;
  lossAdjustmentExpenses: number;
  lossRatioPercentage: number;
  activePolicyCount: number;
  totalClaimsFiled: number;
  calculatedAt: string;
  periodStart: string;
  periodEnd: string;
}

export interface LossTriangleResponse {
  triangleId: string;
  accidentYear: number;
  developmentYear: number;
  cumulativeIncurredClaims: number;
  cumulativePaidClaims: number;
  caseReserves: number;
  ibnrReserves: number;
  calculatedAt: string;
}

export interface LossTriangleListResponse {
  items: LossTriangleResponse[];
  accidentYear: number;
}

export interface ExecutiveDashboardSummaryResponse {
  summaryId: string;
  totalWrittenPremium: number;
  totalEarnedPremium: number;
  totalIncurredLosses: number;
  lossRatioPercentage: number;
  claimsFrequency: number;
  netSubrogationYield: number;
  activePolicyCount: number;
  totalClaimsFiled: number;
  openClaimsCount: number;
  closedClaimsCount: number;
  calculatedAt: string;
  periodStart: string;
  periodEnd: string;
}

export interface RiskScoreSnapshotResponse {
  snapshotId: string;
  customerId: string;
  riskScore: number;
  riskBand: string;
  keyFactors: string;
  modelVersion: string;
  generatedAt: string;
}

export interface CreateRiskScoreRequest {
  customerId: string;
}

export interface ClaimIntelligenceSnapshotResponse {
  snapshotId: string;
  claimId: string;
  priority: string;
  priorityScore: number;
  fraudRiskFlag: boolean;
  severityBand: string;
  keySignals: string;
  modelVersion: string;
  generatedAt: string;
}

export interface RenewalIntelligenceSnapshotResponse {
  snapshotId: string;
  policyId: string;
  renewalRiskBand: string;
  suggestedAction: string;
  claimTrend: string;
  riskTrend: string;
  premiumChangeIndicator: number;
  keyFactors: string;
  modelVersion: string;
  generatedAt: string;
}

// ============ COMMON / UTILITY ============

export interface ApiError {
  message: string;
  fieldErrors?: Record<string, string>;
}

export interface PaginatedResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
}

export interface ApiResponse<T> {
  data: T;
  success: boolean;
  message?: string;
}