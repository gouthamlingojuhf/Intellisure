# IntelliSure integration-test summary

## Result

The requested automated coverage was added as static JUnit 5 service tests.
Per request, no Maven command, test runner, service, Docker container, or
runtime call was executed. Consequently, execution status is **not run**.

## Tests added or extended

* `quote-policy-service/.../QuotePolicyLifecycleServiceTest.java` — draft
  pricing/validity, quote not-found, status submission, acceptance gate, and
  bind/issue transition.
* `claims-service/.../ClaimLifecycleServiceTest.java` — FNOL estimate/payout
  invariant, status update, missing claim, and customer-filtered/unfiltered
  listing.
* `recovery-service/.../RecoveryLifecycleServiceTest.java` — initiated zero
  progress, missing case, and customer-filtered/unfiltered listing.
* `vendor-partner-service/.../VendorAssignmentServiceTest.java` — onboarding,
  listing, assignment acceptance/completion, and vendor status transition.
* `workflow-notification-service/.../NotificationServiceTest.java` — send,
  recipient listing, and read-state transition.
* `customer-party-service/.../AuthServiceTest.java` — active login, wrong
  password, unknown user, and inactive user rejection.
* `document-audit-service/.../DocumentServiceTest.java` — evidence metadata
  and uploader preservation, including persisted entity verification.
* `document-audit-service/.../AuditControllerTest.java` — audit-event creation
  metadata and entity-filtered listing.

## Traceability

The tests cover implemented portions of F1-T03/F1-T14-F1-T17, F2-T02/F2-T14-
F2-T15/F2-T17-F2-T19, and the component-level operations relevant to F3,
including authentication, notifications, document metadata, and audit event
recording.
`Docs/Test /IntelliSure_Test_Traceability.md` records the complete F1/F2/F3
and BI mapping and lists unsupported cases as pending rather than disabled
passing tests.

## Layer artifacts

`run-layer1-tests.sh` and `run-layer1-tests.ps1` are provided for a future
Testcontainers-backed Layer 1 suite. They verify Docker and filter
`@Tag("Layer1")`/Maven groups. No `*L1Test` class is currently created because
the repository has no real local integration fixture and the request prohibits
runtime execution. Layer 2-4 runners are intentionally omitted.

## Not automatable from current code

Triage, underwriting work assignment/referral, risk engineering, subjectivities,
customer acceptance as a distinct event, bind versus issue, policy coverage
verification, reserves, settlement/payment, salvage/subrogation, closure gates,
renewal/non-renewal, SLA escalation, material-change endorsements, and
cross-service audit orchestration are not represented by current APIs or state
models. They remain pending in the traceability document.
