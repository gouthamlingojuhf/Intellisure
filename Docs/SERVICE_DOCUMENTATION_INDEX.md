# IntelliSure service documentation

Last verified: 2026-10-09

This index is the current implementation reference for the IntelliSure backend. It is based on the controllers, security configuration, `application.yaml` files, Docker Compose configuration, and the locked project versions. The linked service documents contain additional domain and implementation notes.

## Platform rules

- Java 17, Spring Boot 4.1.1, Spring Cloud 2025.1.3, WebFlux, and R2DBC are locked.
- The browser calls `http://localhost:8080`; it must not call service ports directly.
- Customer & Party Service issues JWTs. Gateway validates the JWT and routes requests. Each business service validates the propagated Bearer JWT and enforces ownership or role rules.
- The caller's Bearer JWT is propagated for service-to-service calls. `X-User-Id` and separate internal JWTs are not used.
- Policyholder reads and writes are customer-owned. Empty collections are valid responses and must be rendered as empty states.
- Docker is additive. Native Windows startup scripts and shared application configuration remain unchanged.

## Service matrix

| Component | Directory | Eureka name | Port | Database | Documentation |
|---|---|---|---:|---|---|
| Eureka Server | `eureka` | `EUREKA-SERVER` | 8761 | none | [Eureka](EUREKA_SERVICE_DOCUMENTATION.md) |
| API Gateway | `api-gateway` | `API-GATEWAY` | 8080 | none | [Gateway](API_GATEWAY_DOCUMENTATION.md) |
| Customer & Party | `customer-party-service` | `CUSTOMER-PARTY-SERVICE` | 8081 | `customer_party_db` | [Customer & Party](CUSTOMER_PARTY_SERVICE_DOCUMENTATION.md) |
| Quote & Policy | `quote-policy-service` | `QUOTE-POLICY-SERVICE` | 8082 | `quote_policy_db` | [Quote & Policy](QUOTE_POLICY_SERVICE_DOCUMENTATION.md) |
| Risk & Underwriting | `risk-underwriting-service` | `RISK-UNDERWRITING-SERVICE` | 8083 | `risk_underwriting_db` | [Risk & Underwriting](RISK_UNDERWRITING_SERVICE_DOCUMENTATION.md) |
| Claims | `claims-service` | `CLAIMS-SERVICE` | 8084 | `claims_db` | [Claims](CLAIMS_SERVICE_DOCUMENTATION.md) |
| Vendor & Partner | `vendor-partner-service` | `VENDOR-PARTNER-SERVICE` | 8085 | `vendor_partner_db` | [Vendor & Partner](VENDOR_PARTNER_SERVICE_DOCUMENTATION.md) |
| Recovery & Business Continuity | `recovery-service` | `RECOVERY-SERVICE` | 8086 | `recovery_continuity_db` | [Recovery](RECOVERY_SERVICE_DOCUMENTATION.md) |
| Workflow & Notification | `workflow-notification-service` | `WORKFLOW-NOTIFICATION-SERVICE` | 8087 | `workflow_notification_db` | [Workflow & Notification](WORKFLOW_NOTIFICATION_SERVICE_DOCUMENTATION.md) |
| Document & Audit | `document-audit-service` | `DOCUMENT-AUDIT-SERVICE` | 8088 | `document_audit_db` | [Document & Audit](DOCUMENT_AUDIT_SERVICE_DOCUMENTATION.md) |
| Analytics & Intelligence | `analytics-intelligence-service` | `ANALYTICS-INTELLIGENCE-SERVICE` | 8089 | `analytics_intelligence_db` | [Analytics & Intelligence](ANALYTICS_INTELLIGENCE_SERVICE_DOCUMENTATION.md) |

## Current API surface

All paths below are exposed through the Gateway with the same `/api` prefix. Authentication and role details are enforced by Gateway and the owning service; this list is a route inventory, not a permission grant.

### Eureka Server

- `GET /actuator/health` — service health.
- `GET /eureka/apps` — registered applications when the Eureka API is enabled.
- Dashboard: `http://localhost:8761`.

### API Gateway

- Routes `/api/auth`, `/api/users`, and `/api/customers` to Customer & Party.
- Routes `/api/quotes` and `/api/policies` to Quote & Policy.
- Routes `/api/risk-assessments`, `/api/underwriting`, `/api/risk-evidence`, and related underwriting paths to Risk & Underwriting.
- Routes `/api/claims` to Claims.
- Routes `/api/vendors`, `/api/vendor-assignments`, and `/api/partners` to Vendor & Partner.
- Routes `/api/recovery` and `/api/business-continuity` to Recovery.
- Routes `/api/workflows` and `/api/notifications` to Workflow & Notification.
- Routes `/api/documents` and `/api/audit-events` to Document & Audit.
- Routes `/api/analytics`, `/api/intelligence`, and `/api/risks` to Analytics & Intelligence.
- `GET /actuator/health` — Gateway health.

### Customer & Party Service

- Authentication: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`.
- Profile: `GET /api/customers/me`, `PUT /api/customers/me`.
- User context: `GET /api/users/me`, `GET /api/users/role/{role}`, `GET /api/users/role/CLAIMS_ADJUSTER/available`.
- Administration: `POST /api/admin/users`, `PUT /api/admin/users/{id}/roles`, `PUT /api/admin/users/{id}/status`.
- Important lifecycle rule: after the first profile is created, the policyholder logs in again so the JWT contains the generated `customerId` claim.

### Quote & Policy Service

- Quotes: create, submit, accept, decline, offer terms, bind, read by ID/number/customer, and read underwriting decisions.
- Underwriting handoff: record decisions, offer terms, and reassign an underwriter through the quote endpoints.
- Policies: bind, issue, cancel, reinstate, read by ID/number/customer, and check coverage status.
- Policy lifecycle: endorsements, renewals, and premium audits are grouped under `/api/policies/{policyId}`.
- Subjectivities: create, list, satisfy, and waive under `/api/quotes/{quoteId}/subjectivities`.

### Risk & Underwriting Service

- Assessments: create, start, assign, submit for review, update risk score, read by ID/number/quote, list, and list by underwriter or risk engineer.
- Workflow: review, approve, refer, decline, and decision history routes under `/api/underwriting` and `/api/decisions`.
- Evidence: create, list, verify, and update evidence under `/api/risk-assessments/{assessmentId}/evidence`.
- Policyholders can see appropriate underwriting status/results; only employee roles can perform underwriting decisions.

### Claims Service

- FNOL and claims: `POST /api/claims`, `POST /api/claims/fnol`, list, read, status, adjuster assignment, assessment, reserve, payout, decision, settlement approval, payment, and close.
- Claim financials: `/api/claims/{claimId}/financials`.
- Coverage decisions: `/api/claims/{claimId}/coverage-decisions`.
- Business income: `/api/claims/{claimId}/business-income`.
- Salvage and subrogation: `/api/claims/{claimId}/salvages` and `/api/claims/{claimId}/subrogations`.
- Payments: `/api/claims/{claimId}/payments`.
- FNOL verifies policy ownership/eligibility through Quote & Policy before saving.

### Vendor & Partner Service

- Vendor directory and administration: `/api/vendors`.
- Onboarding and verification: `/api/vendors/onboarding-requests` and `/api/vendors/{vendorId}/verify`.
- Performance: `/api/vendors/{vendorId}/performance`.
- Assignments: `/api/vendor-assignments`, including accept, decline, and status transitions.
- Vendor assignment is not automatically created for customer-managed or customer-vendor recovery paths.

### Recovery & Business Continuity Service

- Cases: create, list, read, update, status, path selection, progress, estimate, and completion under `/api/recovery/cases`.
- Supported paths: `NETWORK_VENDOR`, `CUSTOMER_VENDOR`, and `CUSTOMER_MANAGED`.
- Plans: `/api/recovery/cases/{recoveryCaseId}/plan` and `/api/recovery/cases/plans`.
- Support requests: `/api/recovery/cases/{recoveryCaseId}/support-requests`.
- Recovery remains central even when no vendor assignment exists. Customer-owned case creation is idempotent per claim.

### Workflow & Notification Service

- Notifications: create, list, unread count, filter by type, mark one read, and mark all read under `/api/notifications`.
- Workflows: create and read `/api/workflows`.
- Tasks: create, list, assign, complete, and escalate under `/api/workflows`.
- Policyholders can access their own notifications; employee/system workflows may create operational notifications according to role rules.

### Document & Audit Service

- Documents: create, read by ID, and list under `/api/documents`.
- Audit events: create, list, and read by ID under `/api/audit-events`.
- Policyholder document visibility is resolved against the owning Quote & Policy or Claims service with the propagated JWT.
- Policyholders cannot create or retrieve employee-only audit information.

### Analytics & Intelligence Service

- `GET /api/analytics/dashboard/summary` — persisted operational summary used by the Intelligence MFE.
- `GET /api/analytics/loss-ratio` — loss-ratio reporting contract.
- `GET /api/analytics/loss-triangle/{year}` — loss-triangle reporting contract.
- `POST /api/analytics/risk-score` — currently reports the honest not-implemented state until a real underwriting-backed scoring contract is available.
- Analytics routes are employee-only; the Policyholder experience does not consume employee analytics.

## Running and verifying services

Native Windows startup remains the supported office workflow. For additive Docker development, see [Docker Development](DOCKER_DEVELOPMENT.md). The standard health checks are:

```text
http://localhost:8761/actuator/health
http://localhost:8080/actuator/health
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health
http://localhost:8083/actuator/health
http://localhost:8084/actuator/health
http://localhost:8085/actuator/health
http://localhost:8086/actuator/health
http://localhost:8087/actuator/health
http://localhost:8088/actuator/health
http://localhost:8089/actuator/health
```

The Docker environment was verified with all listed services healthy and the unauthenticated Gateway quote request correctly returning HTTP 401. Authenticated registration, profile completion, re-login, customer-owned quote creation, quote read, and customer quote listing were also verified locally.

## Documentation maintenance

When a controller route, role rule, service port, database name, or inter-service contract changes, update the owning service document and this index in the same change. Source code and runtime security configuration are authoritative over older conceptual examples in the detailed documents.
