# IntelliSure Demo Service Audit Plan

## Goal
Validate that the project is demo-ready by checking all microservice layers internally, verifying service-to-service routing, and correcting inconsistencies before the presentation.

## Audit checkpoints

### 1. Controller layer audit
For each service:
- Confirm controller path mappings are consistent with the intended API contract
- Confirm controller names, request mappings, and HTTP verbs match the service design
- Check whether controllers directly access repositories instead of using a service layer
- Verify input validation and status handling are consistent

### 2. Service layer audit
For each service:
- Confirm the service owns business logic and not the controller
- Check dependency injection into repositories and external clients
- Validate status transitions and not-found handling
- Check whether DTO mapping is consistent with the API contract

### 3. Repository layer audit
For each service:
- Validate entity mappings and field names
- Check entity relationships and transaction boundaries
- Confirm repository methods match the service usage
- Ensure identifiers and status enums are consistent across modules

### 4. Security / gateway route audit
Check these against the API gateway config:
- Public endpoints
- Authenticated service endpoints
- Role-based access rules
- Actual controller request mappings
- Any endpoint with a mismatch between controller path and gateway allowlist

### 5. Inter-service contract audit
For each service interaction:
- Validate the exact path used by the caller and the exact path exposed by the target service
- Confirm that the gateway allows the route and that it matches the controller mapping
- Check headers, IDs, and request payloads
- Confirm the consumer knows the correct endpoint contract

## Known issues found during the first pass

### Issue 1: API gateway route mismatch for audit events
- Gateway allows `/api/audit/**`
- Actual controller is mapped to `/api/audit-events`
- Result: audit endpoints are not consistently reachable through the gateway

### Issue 2: API gateway route mismatch for vendor assignments
- Gateway allows `/api/vendors/**` and `/api/partners/**`
- Actual assignment controller is `/api/vendor-assignments`
- Result: assignment operations can be blocked by route rules

### Issue 3: direct repository dependency in controller
- `document-audit-service` `AuditController` directly uses `AuditEventRepository`
- This bypasses the service layer and creates architectural inconsistency

### Issue 4: risk route alias not fully protected in gateway
- Controller supports both `/api/risks` and `/api/risk-assessments`
- Gateway currently protects `/api/risks/**` but not the alias path consistently

## Service-by-service validation checklist

### customer-party-service
- [ ] AuthController and service contracts
- [ ] User account / customer profile wiring
- [ ] JWT and role mapping consistency
- [ ] Gateway route coverage

### quote-policy-service
- [ ] QuoteController and PolicyController API contract
- [ ] Accepted/issued policy flow
- [ ] Service-to-service status transitions
- [ ] Gateway route coverage

### risk-underwriting-service
- [ ] Risk assessment endpoint contract
- [ ] Service logic and persistence mapping
- [ ] Alias route coverage and security

### claims-service
- [ ] Claim creation and status change flow
- [ ] Consistency between claim ID and customer ID usage
- [ ] Gateway route coverage

### vendor-partner-service
- [ ] Vendor registration and assignment flow
- [ ] Assignment controller path consistency
- [ ] Gateway route coverage
- [ ] Service methods and DTO mapping

### recovery-service
- [ ] Recovery case creation and query endpoints
- [ ] Service logic and status behavior
- [ ] Gateway route coverage

### workflow-notification-service
- [ ] Notification endpoint contract
- [ ] Notification workflow and status readiness
- [ ] Gateway route coverage

### document-audit-service
- [ ] Document upload metadata flow
- [ ] Audit event recording and retrieval
- [ ] Service layer cleanup
- [ ] Gateway route coverage

### analytics-intelligence-service
- [ ] Risk score endpoint contract
- [ ] Service and persistence mapping
- [ ] Gateway route coverage

### api-gateway
- [ ] Public route rules
- [ ] Role-based restrictions
- [ ] Path matching against all service controllers
- [ ] Alias route coverage for each controller

## Action log

### Status: Verified / fixed
- Gateway route mismatch for audit-events was corrected
- Gateway route mismatch for vendor assignments was corrected
- Controller-level repository usage in the audit service was corrected by introducing an audit service
- Fresh compile validation completed successfully in the current environment

### Verification evidence
- Build command executed successfully in the PowerShell terminal
- Exit code: 0
- Result: all services compiled successfully with the current project state

### Status: To check next
- Review all services for controller/repository direct access patterns
- Review all service routes against gateway security allowlists
- Run a controlled smoke test for the full demo journey
- Validate route aliases and gateway rules for the remaining service endpoints

## Service-by-service audit status

### customer-party-service
- Controller mappings present: `/api/auth`, `/api/customers`, `/api/users`
- Auth and user flows are aligned with gateway public/authenticated rules
- Status: structurally consistent

### quote-policy-service
- Controller mappings present: `/api/quotes`, `/api/policies`
- Quote lifecycle endpoints are consistent and usable for the demo
- Status: structurally consistent

### risk-underwriting-service
- Controller mapping present: `/api/risks` and `/api/risk-assessments`
- Gateway rules now cover the risk alias and the standard risk path
- Status: route coverage improved; still business-logic pending for advanced workflow orchestration

### claims-service
- Controller mapping present: `/api/claims`
- Demo claim flow is valid and aligned to the implemented backend contract
- Status: structurally consistent

### vendor-partner-service
- Controller mappings present: `/api/vendors` and `/api/vendor-assignments`
- Gateway rules were adjusted to cover the assignment path
- Vendor assignment flow is the most relevant demo scenario for operations and completion
- Status: structurally consistent for the demo path

### recovery-service
- Controller mapping present: `/api/recovery`
- Recovery concept exists, but not required for the core demo path
- Status: available but optional

### workflow-notification-service
- Controller mapping present: `/api/notifications`
- Notification lifecycle is valid for demonstration after the main transaction flow
- Status: structurally consistent

### document-audit-service
- Controller mappings present: `/api/documents` and `/api/audit-events`
- Service-layer cleanup completed
- Gateway route audit was corrected for audit-events
- Status: structurally consistent

### analytics-intelligence-service
- Controller mapping present: `/api/analytics`
- Analytics is optional for the core backend demo
- Status: available but not required for the immediate lifecycle

### api-gateway
- Gateway role-based security is present and largely aligned with controller routes
- Status: improved after mismatch fixes, but full smoke test still recommended before final demo

## Demo recommendation
Keep the demo focused on the implemented lifecycle:
- user registration
- login
- profile creation
- quote creation
- quote approval
- policy issuance
- claim creation
- vendor assignment and completion
- notification and audit records

Avoid exposing incomplete workflow assignment and advanced automation features as if they are live production features.

## Final assessment
The project is in a much better state for a backend demo than it was before:
- compile verification succeeded
- major route mismatches were corrected
- controller/repository layering inconsistency in audit was fixed

The project is now suitable for a focused, honest demo of the implemented core backend lifecycle, provided the audience is told that advanced workload balancing and operational workflow orchestration remain future scope.
