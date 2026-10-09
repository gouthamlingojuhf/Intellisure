# IntelliSure test coverage plan

## Objective

Raise meaningful automated coverage across every backend microservice and every frontend application/library without changing the locked runtime or dependency versions. The target is near-100% business-logic coverage:

- line coverage: at least 95%
- method/class coverage: at least 95%
- branch coverage: at least 90%

Generated Spring Boot bootstrap code and Angular framework bootstrap files may be excluded only when the exclusion is documented and has no business logic. DTOs, mappers, guards, services, controllers, filters, security rules, error handlers, reducers, effects, and route configuration remain in scope.

## Execution order

| Batch | Module | Current state | Required work |
| --- | --- | --- | --- |
| 1 | Eureka + API Gateway | Complete: 100% Eureka line/method/class; 99.35% Gateway line, 94.44% branch, 100% method/class | Keep threshold checks green on Java 17 |
| 2 | Analytics & Intelligence | Service tests are sparse | Cover controller, role rules, unavailable-data paths, converter, filter, DTO mapping |
| 3 | Document & Audit | Security/service tests exist | Cover controller branches, ownership resolution, audit actor rules, error handling |
| 4 | Workflow & Notification | Service/security tests exist | Cover notification ownership, read state, workflow orchestration, controller/error branches |
| 5 | Recovery | Core tests exist | Cover all recovery paths, idempotency, ownership, controller and WebClient behavior |
| 6 | Vendor & Partner | Differentiator tests exist | Cover onboarding, verification, discovery, assignment lifecycle, evidence/performance, role boundaries |
| 7 | Claims | Broad service/security tests exist | Cover FNOL validation, policy verification, claim lifecycle, recovery integration, controller branches |
| 8 | Risk & Underwriting | Workflow tests exist | Cover queues, assessment/decision/referral/subjectivity branches, ownership and authorization |
| 9 | Quote & Policy | Largest existing suite | Measure current coverage, fill all service/controller/scheduler/mapper/security branches, keep lifecycle contract tests |
| 10 | Customer & Party | Broadest existing suite | Fix environment-dependent application-context test setup, cover admin/profile/auth/JWT/error branches |
| 11 | Frontend shell, MFEs, libraries | Smoke specs only in many areas | Add specs file-by-file for guards, routes, API services, state, effects, and business components; run per-project coverage |
| 12 | Cross-service gate | Individual suites complete | Run every backend suite on Java 17, all frontend coverage suites, ownership/security matrix, and final report |

## Test design rules

1. Start with pure service and utility branches, then controllers/security, then external clients and integration paths.
2. Use Mockito only at external boundaries; use real service behavior and Reactor `StepVerifier` for reactive flows.
3. Test both successful and rejected authorization paths for every protected endpoint.
4. Test empty results, validation failures, downstream errors, timeouts, duplicate/idempotent requests, and ownership mismatches.
5. Use test-only fixtures and disposable databases; never add business records, mock fallbacks, or test credentials to application/runtime code.
6. Every new test file must name the class under test and cover one behavior family. Avoid tests that only assert the Spring context starts.
7. Keep coverage reports under each module's `target/` or frontend coverage output; do not commit generated reports.

## Frontend coverage scope

For each Angular project/library, add tests for:

- route guards and role decisions;
- API services, request construction, loading/error/empty states;
- NgRx actions, reducers, selectors, and effects where present;
- component inputs, outputs, keyboard behavior, responsive state, and visible authorization;
- identifier formatting and user-facing empty/error messages.

Run the existing locked toolchain with `npm run test -- --watch=false --browsers=ChromeHeadless --code-coverage` and build each affected project. Do not add packages or regenerate the lockfile.

## Completion evidence

For every batch, record the module, test command, test count, line/branch/method coverage, failures, and any environment limitation in `PROJECT_STATUS.md`. A batch is complete only when its tests pass on the supported Java 17/locked Angular environment and its coverage threshold is met.
