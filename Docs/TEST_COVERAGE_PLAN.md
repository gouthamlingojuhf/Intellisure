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
| 2 | Analytics & Intelligence | Complete: 100% line/method/class; 94.44% branch | Keep threshold checks green on Java 17 |
| 3 | Document & Audit | Complete: 99.39% line; 94.44% branch; 100% method/class | Keep threshold checks green on Java 17 |
| 4 | Workflow & Notification | Complete: 98.6% instruction, 99.3% line, 92.2% branch, 97.3% method, 100% class; 55 tests | Keep threshold checks green on Java 17 |
| 5 | Recovery | Complete: 99.2% instruction, 99.3% line, 90.2% branch, 100% method/class; 60 tests | Keep threshold checks green on Java 17 |
| 6 | Vendor & Partner | Complete: 98.0% line, 90.5% branch, 97.4% method, 95.7% class; 44 tests | Keep threshold checks green on Java 17 |
| 7 | Claims | Complete: 64 tests; 99.36% line, 92.41% branch, 99.69% method, 100% class; `bash mvnw -q verify` passes | Keep threshold checks green on Java 17 |
| 8 | Risk & Underwriting | Complete: 46 tests; 98.25% line, 92.83% branch, 97.93% method, 100% class; `bash mvnw -q verify` passes | Keep threshold checks green on Java 17 |
| 9 | Quote & Policy | Complete: 313 tests; 98.34% line, 90.70% branch, 99.20% method, 100% class; `bash mvnw -q verify` passes | Keep threshold checks green on Java 17 |
| 10 | Customer & Party | Complete: 51 tests; 99.47% line, 98.10% branch, 99.33% method, 100% class; `bash mvnw -q verify` passes | Keep threshold checks green on Java 17 |
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
