# IntelliSure lifecycle test traceability

These are static JUnit 5 service tests for behavior that currently exists in the
codebase. They were created from
`IntelliSure_Full_Lifecycle_Business_Test_Cases.md`. They were not executed.

## Automated tests added

| Test class | Covered behavior |
|---|---|
| `QuotePolicyLifecycleServiceTest` | Draft quote creation and temporary premium calculation; quote status update, customer listing, and not-found behavior; policy read and issuance blocked unless quote status is `ACCEPTED`; accepted quote becomes `BOUND` and produces an active policy. |
| `ClaimLifecycleServiceTest` | Claim/FNOL persistence with estimated loss; claim status update; customer-filtered and unfiltered listing; missing-claim rejection. |
| `RecoveryLifecycleServiceTest` | Recovery case initiation at zero progress; customer-filtered and unfiltered listing; recovery-case lookup and missing-case rejection. |
| `VendorAssignmentServiceTest` | Vendor starts in `PENDING_ONBOARDING`; vendor listing and status transition; assignment starts `ASSIGNED`, can be accepted, and completion records completion date/state. |
| `NotificationServiceTest` | Notification creation, recipient lookup/listing, and read-state update. |
| `DocumentServiceTest` | Document evidence metadata and uploader preservation. |
| `AuditControllerTest` | Audit event creation with lifecycle metadata and entity-filtered audit listing. |
| `AuthServiceTest` | Active-user login/token issuance; unknown and inactive-user rejection. |

## Case status

### Flow 1 — clean new business

- **Covered by current tests:** F1-T03 (draft creation); the acceptance gate
  in F1-T14/F1-T15 is partially represented by the policy issuance guard;
  F1-T16/F1-T17 are partially represented by quote `BOUND` and policy `ACTIVE`.
- **Pending in production code:** F1-T01, F1-T02, F1-T04 through F1-T13,
  F1-T18, F1-T19, and F1-T20.

The current implementation does not yet provide triage, underwriting work
assignment, subjectivities, expiry, endorsements, renewal transactions, or
separate customer acceptance/bind/issue state transitions.

### Flow 2 — loss, claim, recovery, settlement, renewal

- **Covered by current tests:** F2-T02 (claim creation), F2-T14 (claim
  status mutation), F2-T15 (recovery initiation), F2-T17/F2-T18 (vendor
  assignment and completion), and the evidence behavior in F2-T19.
- **Pending in production code:** F2-T01, F2-T03 through F2-T13, F2-T16,
  F2-T20.

The current implementation does not yet provide policy coverage verification,
investigation, reserves, covered-loss assessment, settlement, payment,
salvage/subrogation, closure gates, or post-claim renewal.

### Flow 3 — exception, referral, conditional acceptance, non-renewal

- **Covered only at component level:** vendor onboarding/assignment status,
  notifications, document metadata, and authentication are tested in
  isolation.
- **Pending in production code:** F3-T01 through F3-T20.

The current implementation does not yet provide needs-information tasks,
SLA escalation, underwriting referral, risk-engineering findings,
subjectivities, conditional quotes, material-change endorsements,
reassessment, renewal decisions, non-renewal, or end-to-end audit history.

## Business invariants

### Partially represented

- **BI-01:** Quotes and policies are separate entities.
- **BI-04:** Policy issuance changes the quote to `BOUND` and creates an
  `ACTIVE` policy.
- **BI-09:** Estimated claim loss is distinct from the currently null payout.
- **BI-12:** Recovery starts with its own progress state.
- **BI-17:** Document metadata is preserved as evidence.

### Not yet automatable from the current production code

BI-02, BI-03, BI-05, BI-06, BI-07, BI-08, BI-10, BI-11, BI-13, BI-14,
BI-15, BI-16, and BI-18.

These remain pending because the corresponding domain state, authorization
boundary, cross-service orchestration, or persistence model is not implemented.
The tests intentionally do not mark missing behavior as passing.

## Test files

- `quote-policy-service/src/test/java/com/intellisure/quotepolicyservice/service/QuotePolicyLifecycleServiceTest.java`
- `claims-service/src/test/java/com/intellisure/claimsservice/service/ClaimLifecycleServiceTest.java`
- `recovery-service/src/test/java/com/intellisure/recoveryservice/service/RecoveryLifecycleServiceTest.java`
- `vendor-partner-service/src/test/java/com/intellisure/vendorpartnerservice/service/VendorAssignmentServiceTest.java`
- `workflow-notification-service/src/test/java/com/intellisure/workflownotificationservice/service/NotificationServiceTest.java`
- `document-audit-service/src/test/java/com/intellisure/documentauditservice/service/DocumentServiceTest.java`
- `document-audit-service/src/test/java/com/intellisure/documentauditservice/controller/AuditControllerTest.java`
- `customer-party-service/src/test/java/com/intellisure/customerpartyservice/service/AuthServiceTest.java`

The authentication suite also covers wrong-password rejection. All tests remain
static JUnit 5 Mockito/Reactor tests and were not executed per the request.
