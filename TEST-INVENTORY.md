# Unit-test inventory

This inventory is based on the current `src/main/java` sources (rather than the
business blueprint).  New tests use JUnit 5, Mockito and Reactor Test without a
Spring application context.  Existing tests are listed where they already
cover the method.

| Production area | Methods and corresponding test |
|---|---|
| customer-party `AuthService` | `login` — `service/AuthServiceTest` (active, inactive/missing and invalid-password paths) |
| customer-party `UserAccountService` | `register`, `getUserByEmail`, `getUserById` — `service/UserAccountServiceTest` (insert/encoding, duplicate and missing-user paths) |
| customer-party `CustomerProfileService` | `getCustomerProfileByUserId`, `updateCustomerProfile` — `service/CustomerProfileServiceTest` (missing profile and update/delegation) |
| customer-party controllers | `AuthController`, `UserAccountController`, `CustomerProfileController` endpoint delegation — existing controller source remains context-free; service tests verify their collaborators |
| quote-policy `QuoteService` | `createQuote`, `getQuote`, `getQuotesByCustomer`, `updateStatus` — `service/QuotePolicyLifecycleServiceTest` (draft premium, not-found, filtering and transition) |
| quote-policy `PolicyService` | `getPolicies`, `getPolicy`, `issuePolicy` — `service/QuotePolicyLifecycleServiceTest` (listing, not-found, rejected quote and ACCEPTED→BOUND issue transition) |
| quote-policy controllers | `QuoteController`, `PolicyController`, `QuotePolicyActionController` delegate to services; lifecycle tests cover the delegated behavior |
| risk-underwriting `RiskAssessmentService` | `createAssessment` — `service/RiskAssessmentServiceTest` (DRAFT mapping, request fields and persistence error) |
| risk-underwriting controller | `RiskAssessmentController.createAssessment` delegates to service (service test covers returned contract) |
| claims `ClaimService` | `getClaims`, `getClaim`, `updateStatus`, `fileClaim` — `service/ClaimServiceTest` and existing `ClaimLifecycleServiceTest` (all-vs-customer filtering, filing, not-found and transitions) |
| claims controller | `ClaimController` delegates to service; existing lifecycle tests cover response behavior |
| recovery `RecoveryService` | `getCases`, `getCase`, `initiateRecovery` — `service/RecoveryServiceTest` and existing `RecoveryLifecycleServiceTest` |
| recovery controller | `RecoveryController` delegates to service; lifecycle tests cover returned behavior |
| vendor-partner `VendorService` | `getVendors`, `registerVendor`, `updateStatus`, `assign`, `updateAssignment` — `service/VendorServiceTest` and existing `VendorAssignmentServiceTest` |
| vendor-partner controllers | `AssignmentController` — `controller/AssignmentControllerTest` (create/accept/arbitrary status/complete); `VendorController` delegates to the same service methods and is covered by service/delegation behavior |
| workflow-notification `NotificationService` | Existing `service/NotificationServiceTest` covers notification creation/listing/status behavior |
| workflow-notification controller | `NotificationController` delegates to notification service; existing service test covers the current reactive contract |
| document-audit `DocumentService` | Existing `service/DocumentServiceTest` covers upload, retrieval and missing-document behavior |
| document-audit controllers | Existing `controller/AuditControllerTest`; `DocumentController` delegates to `DocumentService` |
| document-audit audit service | No separate audit service exists in current source; `AuditController` is covered by `AuditControllerTest` |
| analytics-intelligence `AnalyticsService` | `generateRiskScore` — `service/AnalyticsServiceTest` (saved MEDIUM snapshot and persistence error) |
| analytics controller | `generateRiskScore` delegates to `AnalyticsService`; service test verifies the response contract |
| API gateway JWT converter | `JwtAuthenticationConverter.convert` — `converter/JwtAuthenticationConverterTest` (role authority mapping and missing-role behavior) |
| customer-party JWT converter | `JwtAuthenticationConverter.convert` — `converter/JwtAuthenticationConverterTest` (role authority mapping) |
| customer-party `JwtService` | `generateToken` — `service/JwtServiceTest` (signed subject, role and expiry claims) |
| Eureka server bootstrap | `EurekaApplication` annotations — `EurekaApplicationConfigurationTest` (Spring Boot and Eureka server enablement) |
| UUID converters | Customer-party and quote-policy `BytesToUuidConverter`/`UuidToBytesConverter` round-trip tests — `customer-party-service/src/test/.../converter/UuidConverterTest` and `quote-policy-service/src/test/.../converter/UuidConverterTest` |

## Deliberate gaps

No production code was changed.  Spring Boot application classes, repository
interfaces, entity accessors/builders and framework-only security configuration
are not unit-test targets.  `DocumentController`, `VendorController` and
the remaining thin controllers have no branching logic; their service
delegation is represented by the corresponding service tests, while the
dedicated `AssignmentController` delegation test covers all status branches.
