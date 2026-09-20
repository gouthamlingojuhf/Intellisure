# IntelliSure integration-test plan

## Scope and constraints

This plan is derived from `Docs/Test /IntelliSure_Full_Lifecycle_Business_Test_Cases.md`.
The current implementation is a set of independent reactive Spring services with
R2DBC repositories. The requested change adds static, isolated JUnit 5 service
tests; it does not run Maven, start services, use Docker, or connect to MySQL.

No Layer 1 `*L1Test` classes are added in this pass. A real Layer 1 test would
need a supported MySQL/Testcontainers configuration and application wiring that
is not present in the current modules. Adding a fake container or mocking the
migrated persistence layer would provide misleading coverage.

## Current modules and gaps

| Module | Component under test | Current static coverage |
|---|---|---|
| customer-party-service | Authentication | active login, unknown/inactive rejection |
| quote-policy-service | Quote and policy lifecycle | draft creation/pricing, status transition, not-found, acceptance gate, bind/issue |
| claims-service | FNOL and claim lifecycle | filing, estimate preservation, status update, customer filtering, not-found |
| recovery-service | Recovery case lifecycle | initiation, zero progress, customer filtering, not-found |
| vendor-partner-service | Vendor and assignment lifecycle | onboarding, assignment, completion, status update |
| workflow-notification-service | Operational notifications | send, recipient listing, read transition |
| document-audit-service | Evidence metadata and audit history | uploader/document metadata preservation; audit-event creation and entity-filtered listing |

## Layer strategy

* **Layer 1 (local integration):** deferred. When database wiring is available,
  add Testcontainers-backed classes with `*L1Test` names and `@Tag("Layer1")`,
  exercising controllers through real repositories. Do not mock R2DBC clients,
  repositories, or the service being validated.
* **Layer 2 (smoke):** not appropriate for this request because no application
  processes may be started.
* **Layer 3 (Azure):** not applicable; no Azure dependency is implemented in
  the lifecycle paths.
* **Layer 4 (behavioral comparison):** not applicable; no old/new application
  pair is available.

## Validation scenarios

The static suite validates the implemented portions of F1, F2, and the
component-level evidence/operations used by F3. It asserts business state
transitions without requiring a live database. Unsupported cases remain
explicitly pending in `Docs/Test /IntelliSure_Test_Traceability.md`, including
triage, underwriting referral, subjectivities, acceptance-versus-bind-versus-
issue separation, reserves, settlement/payment, recovery disposition, renewal,
non-renewal, SLA escalation, and end-to-end audit orchestration.

## Future setup requirements

Before enabling Layer 1, add a test-only MySQL/Testcontainers dependency (or a
documented compatible local database fixture), test profile schema/data setup,
and controller-level tests per module. Then run the supplied runner scripts
manually; they are intentionally not executed as part of this change.
