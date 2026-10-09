# IntelliSure project status

Last reviewed: 2026-10-09

## Architecture and compatibility

- Angular 17 shell with Auth, Claims, Vendor, and Intelligence Module Federation remotes.
- Spring Boot 4.1.1 services on Java 17, Spring Cloud 2025.1.3, WebFlux/R2DBC, Eureka, and Gateway.
- Gateway URL remains `http://localhost:8080` for native development.
- Locked frontend versions and `frontend/package-lock.json` are preserved. The lockfile received a minimal, explicitly approved consistency repair; `frontend/package.json` and all dependency versions remain unchanged.
- Existing Windows startup scripts remain authoritative. An additive backend Docker Compose stack now exists; it does not alter native startup behavior or insert business records. Docker requires caller-provided local-only `MYSQL_ROOT_PASSWORD` and Base64-encoded `INTELLISURE_JWT_SECRET` values; no new credential fallback is committed.

## Completed

- Authentication, registration, login, session restoration, guards, profile, and landing experience.
- Policyholder dashboard, quote creation/list/detail, quote submission/acceptance, policy list/detail, and lifecycle visibility.
- Real Claims MFE integration, FNOL flow, claim list/detail, policy-to-claim navigation, and explicit claims routes.
- FNOL now verifies the submitted policy through Quote & Policy with the propagated JWT before persisting a claim.
- Real Recovery case listing, supported path selection, and progress updates.
- Recovery case creation now verifies the requested customer against the authenticated JWT before persistence.
- Claim Detail now performs a real recovery-case lookup and lets the Policyholder start one when none exists; recovery creation is idempotent per claim and does not require a vendor assignment.
- Real document visibility for customer-owned quote, policy, and claim records.
- Audit event creation is restricted to employee/system roles and records the authenticated JWT actor rather than trusting a caller-supplied user ID; Policyholders remain excluded from audit retrieval.
- Quote, policy, underwriting-decision, and subjectivity reads now verify customer ownership for Policyholders while preserving staff/service access.
- Quote rating no longer contains a fixed 2% placeholder; the rating component now calculates a bounded, product/coverage-aware baseline indication with deductible and waiting-period adjustments. Final offered terms remain underwriter-controlled.
- Real notification inbox, read state, mark-read actions, and gateway-backed topbar notifications.
- Notification creation now restricts Policyholders to their own recipient ID while preserving employee/system workflow notification delivery.
- Docker authenticated runtime check completed with a disposable local account: registration, profile completion, re-login for the customer claim, Gateway quote creation, customer-owned quote read, and customer quote listing all succeeded. The created quote correctly remained in `DRAFT` before submission.
- Full authenticated Docker journey verified with local-only test identities: quote submission moved to `IN_REVIEW` with an assigned underwriter, an approved underwriting decision produced `QUOTED` terms, the policyholder accepted, the underwriter bound an `IN_FORCE` policy, the policyholder filed and read a claim, selected `CUSTOMER_MANAGED` recovery, recorded 50% progress, viewed an empty claim-document state, created a notification, and marked it read.
- Added [service documentation index](Docs/SERVICE_DOCUMENTATION_INDEX.md) covering all infrastructure and business services, current ports/databases, Gateway route ownership, endpoint groups, security boundaries, recovery paths, and runtime verification. Corrected stale Spring Boot/JPA metadata in the existing detailed service documents.
- No Policyholder business-data mocks, fake records, or `X-User-Id` headers in the current implementation.

## Current defects and risks

- Document records remain schema-compatible without a customer column; document-audit-service now resolves QUOTE/POLICY/CLAIM ownership through the existing owning services with the propagated JWT. Unsupported Policyholder entity types are denied, while existing staff roles retain access.
- Full Maven verification remains environment-limited on this Mac: Java 25 is active by default and Java 21 is the only alternate installed, so the supported Java 17 run still belongs on the office laptop. The latest `quote-policy-service` test suite exits successfully under Java 25; `customer-party-service` compiles, but its full test run has one application-context error because the local R2DBC/MySQL database is unavailable. No dependency or test-plugin changes were made.
- A clean install against the configured internal Nexus registry remains unavailable from this Mac because the registry host cannot be resolved. After the explicitly approved minimal lockfile repair, a Node 20 local install using the public npm registry completed without changing `package.json` or regenerating dependency versions. The existing lockfile URLs were preserved.
- The frontend was previously verified with Node 20.20.0; the final audit was also rerun with the currently installed Node 25.6.0. The shared `ui-core` library, shell, Claims and Vendor remotes built successfully, and the configured Karma/ChromeHeadless suite exited successfully. Node 25 reports the expected non-LTS warning; no project dependency or version was changed.
- Registration/profile completion followed by re-login is required because the customer ID is embedded in the login JWT. The profile UI now makes this explicit and the auth effect persists/removes the customer ID consistently.

## Roadmap

### P0 — Stability / environment

- Completed: native startup scripts and locked version inventory reviewed; no Windows startup files were changed.
- Completed: all backend modules except `customer-party-service` compiled with `-DskipTests`; the remaining module was blocked before compilation by local Maven-cache write permissions while resolving an existing BOM.
- Completed: Maven cache access was restored and `customer-party-service` now compiles; its focused JWT suite passes 3/3.
- Completed: Docker images for all 11 backend applications built successfully, the Compose stack started, all nine services registered with Eureka, and MySQL, Eureka, every backend service, and Gateway reported healthy.
- Completed: Docker runtime verification exposed and fixed the Customer & Party numeric JWT-expiration defect and the duplicate Risk/Underwriting `satisfied_at` schema declaration.
- Completed: each service health endpoint returned HTTP 200 through Docker; an unauthenticated Gateway request to `/api/quotes` returned HTTP 401 as expected.
- Completed: API Gateway now compiles after replacing one environment-sensitive Lombok logger generation with explicit SLF4J; no dependency or startup-script changes were required.
- Completed: additive Docker Compose configuration was added after confirming there was no existing Docker support. It uses JDK 17 builder/runtime images, the existing Maven wrappers, existing service ports, schema initialization, and a persistent local-only MySQL volume.
- Completed: after explicit approval, repaired only the missing lockfile metadata needed for `npm ci` consistency; no package versions were changed.
- Completed: frontend shared library, shell, all active remotes, and configured unit tests were verified with Node 20.20.0.
- Risk: a fresh install through the configured internal Nexus still depends on that registry being reachable; do not change registry configuration or dependency versions in the project to work around it.
- Completed: current service documentation was consolidated and cross-checked against source controllers, security configuration, Compose service definitions, and the verified Docker runtime.

### P1 — Policyholder journey

- Completed: registration through notifications using real Gateway APIs; Recovery and Notifications now enforce caller ownership in their services, including staff-role exceptions where the existing workflow permits them.
- Completed: Quote & Policy read endpoints now enforce customer ownership by JWT claim for Policyholders, including resource-by-ID, resource-by-number, customer-list, underwriting-history, and subjectivity reads.
- Completed: authenticated quote-to-policy-to-claim-to-recovery-to-documents/notifications runtime path is verified in Docker using the canonical Policyholder FNOL endpoint `POST /api/claims` and a role-protected underwriter flow. The Claim Detail recovery action and idempotent backend create path are also verified.
- Remaining: repeat the same journey against the native Windows startup path and replace local disposable identities with approved office test accounts when available.
- Tests: focused ownership/token-propagation checks pass for Recovery, Notifications, and Documents; Angular builds and the full configured frontend unit-test suite now pass locally. Remaining verification is authenticated cross-service runtime behavior rather than compilation.

### P2 — Employee roles

- Completed: Analytics staff dashboard now uses its persisted-summary contract and honest empty states.
- Completed: Underwriting now shows the authenticated underwriter's or risk engineer's real assigned assessment queue, with empty/error states and an employee-only route guard. Both role-specific risk-service queue endpoints reject a non-admin request for another user's queue.
- Completed: Vendor and Analytics deep links now have shell-level role guards aligned with their backend roles; dead static Analytics shell data was removed.
- Completed: Underwriting review now loads the selected assigned quote and exposes the existing assigned-underwriter decision and offer-terms actions, including live coverage values, expiry, rationale, authority, conditions, and clear server error/success states. The page does not expose binding or underwriting-only operations to Policyholders.
- Completed: Vendor MFE now discovers only live verified/active vendors, dispatches explicit `NETWORK_VENDOR` assignments linked to claims or recovery cases, supports accept/decline/progress/completion with notes and evidence IDs, and records post-completion performance scores.
- Completed: Docker runtime verified Vendor onboarding, verification, directory discovery, recovery-linked dispatch, accept, progress, completion, performance scoring, and the empty evidence-list persistence path.
- Completed: role-journey audit aligned the Gateway and shell boundaries: Risk Engineers can read assigned quote context, Claims Adjusters/Managers can reach supported Vendor APIs, and employee roles can use the live recovery/document views without a policyholder profile lookup.
- Completed: Claims FNOL is now shown only to Policyholders in the claims queue; staff retain their operational claim view without being presented a customer-only action.
- Completed: Risk Engineers receive read-only assessment/quote review in the shared Underwriting screen; decision and commercial-term controls remain Underwriter-only to match service authorization.
- Completed: Claims and Intelligence MFE application TypeScript configs no longer compile Jasmine specs as application source, restoring normal remote production builds without dependency changes.
- Completed: landing-page hero and final CTA now follow the authenticated session state; signed-in users see Enter Workspace/Sign Out instead of Sign In/Get Started while the public experience remains unchanged for signed-out users.
- Completed: global UI role handling now keeps Dashboard and Business Profile policyholder-only, removes the customer-facing System Status card, resolves the signed-in display name from `/api/auth/me`, and routes search terms such as policies, claims, vendors, recovery, documents, analytics, and underwriting to their permitted workspace areas.
- Completed: topbar shortcut text is platform-aware (`Ctrl K` on Windows/Linux and `⌘ K` on Apple platforms), and customer-facing monetary values now default to INR formatting.
- Completed: public registration now distinguishes Business Policyholder from Vendor / Service Provider Applicant. Vendor applicants receive a restricted `VENDOR_APPLICANT` role and may submit onboarding applications; activation remains Vendor Manager-controlled.
- Completed: quote creation now explains product lines, includes the requested commercial insurance products, and provides coverage-code selection with auto-filled names plus a Custom coverage option.
- Completed: coverage entry now shows one catalogue selector and displays the standard coverage name automatically; a free-text field appears only for Custom coverage.
- Completed: quote, policy, claim, recovery, profile, and claim-list screens no longer expose internal UUID/customer-ID fields. FNOL policy selection now uses the policy number while retaining the internal policy identifier only in the API request.
- Completed: the workspace topbar is fixed during scrolling, Ctrl/Cmd+K focuses the search field, partial search terms resolve to permitted workspace areas, and toast notifications auto-dismiss after 30 seconds.
- Completed: Vendor assignment screens no longer expose raw payloads or literal UUID instructions in the user-facing workflow; live claim-number and recovery-case selectors keep backend references internal to the existing assignment contract.
- Completed: public `/help` and authenticated `/docs/guide` now provide HTML-rendered platform, Policyholder, Vendor, and employee operating guides with role-aware content and no business-data placeholders.
- Completed: System Administrator/Admin now have a guarded `/admin` workspace with live customer-party directory search, employee account creation, role/status management, and live quote/policy/claim summary navigation.
- Remaining: add deeper assignment referral and subjectivity screens only after their existing backend contracts and role boundaries are verified end-to-end.
- Completed: Vendor assignment creation now selects live claim numbers and recovery cases from Claims and Recovery APIs; internal IDs remain payload-only.
- Completed: Vendor applicants are onboarding-only in the remote route tree. Assignment list/create/detail paths now require Vendor Manager, Claims operations, Admin, or System Administrator roles, matching the backend assignment policy.
- Completed: Claims FNOL is now Policyholder-only at the remote route boundary, while claim list/detail remain limited to Policyholder and claims operational roles. Policyholder quote creation is also protected at the shell route boundary for direct deep links.
- Completed: final responsive header offsets are aligned between the shell and shared ui-core styles so the fixed topbar remains full-width on tablet/mobile layouts.
- Completed: customer-party now exposes an authorized administrative account directory endpoint with role/status/search filters; Quote & Policy now exposes authorized all-quote/all-policy read endpoints for the Admin workspace.
- Dependency: preserve role restrictions and avoid exposing employee-only information to Policyholders.

### P3 — Analytics / Intelligence

- Completed: Intelligence MFE dashboard and overview now read the existing Analytics summary endpoint; static portfolio values and Analytics mock fallbacks were removed. Empty/unavailable summaries render explicit states.
- Completed: Analytics risk-score generation now returns an honest not-implemented response until underwriting data is available; the Intelligence MFE no longer shows static alerts. The Vendor MFE shell no longer renders its previous static vendor directory.
- Remaining: integrate risk scoring with real underwriting data and add a backend alert contract before showing risk alerts.
- Remaining: connect the transparent rating indication to an explicit quote/underwriting contract if the product requires customer-visible indications before underwriter terms are offered.

### P4 — Final E2E / demo readiness

- Added [role-based E2E verification checklist](Docs/ROLE_BASED_E2E_CHECKLIST.md) with native startup commands, the role matrix, ownership checks, lifecycle acceptance, and evidence requirements.
- Local verification complete: shared ui-core, shell, Claims, and Vendor builds; configured frontend Karma/ChromeHeadless suite; customer-party and quote-policy Java compilation; quote-policy focused Maven suite. Customer-party full tests remain blocked only by the unavailable local R2DBC/MySQL test context.
- Remaining external verification: run the checklist on the office Windows laptop with approved test identities, run the complete backend test suite on supported Java 17, and retain the evidence listed in the checklist.
- Completed: additive Docker environment was validated without changing the native Windows path; repeat only when the office environment needs a fresh runtime check.

### P5 — Nice-to-have

- Completed: fixed responsive topbar offsets, public/role-aware documentation layout, keyboard search access, semantic live-region states, and role-restricted deep links.
- Remaining: lifecycle timeline, document filtering, richer notification UX, and dashboard visualizations.

## Completed batches

- Implemented service-level ownership enforcement for Recovery and Notifications, with focused tests and no contract/version changes.
- Made initial profile completion explicitly require re-authentication to obtain the backend-issued customer ID claim.
- Added the Recovery service's exception handler inside its Maven module so ownership failures return HTTP 403.
- Removed the gateway's stale `X-User-Id` CORS allowance and corrected the gateway documentation to describe Bearer JWT propagation.
- Replaced Intelligence MFE and Analytics dashboard mock values/fallback generators with the existing persisted-summary API and honest empty states.
- Removed the unused static Vendor MFE directory records so the remote renders its real routed workflows only.
- Repaired the API Gateway compile blocker in `CorrelationIdGlobalFilter` without changing the locked dependency set.
- Added document entity ownership resolution through quote-policy and claims services, with focused customer/staff security coverage and no database migration.
- Tightened document-audit write authorization and authenticated actor attribution, with Policyholder denial coverage.
- Replaced the static Underwriting dashboard with the authenticated user's real role-specific assessment queue and tightened queue ownership authorization.
- Hardened Quote & Policy resource reads with reactive ownership checks and focused cross-customer denial tests.
- Added Claims-to-Quote & Policy ownership verification for authenticated FNOL requests, with save-prevention coverage on denial.
- Removed insecure Claims authorization fallbacks that could bypass ownership or assign a random customer when the security actor was unavailable.
- Added recipient authorization for notification creation and customer ownership verification for recovery-case creation.
- Connected the Claim Detail recovery action to the real Recovery API, added a duplicate-safe recovery-case create path, and opened only the exact Policyholder case-creation route in Recovery security.
- Replaced the fixed quote-rating placeholder with a tested, bounded baseline rating calculation that does not fabricate business records or override human underwriting.
- Replaced the static Underwriting dashboard with the authenticated user's real assessment queue and tightened queue ownership authorization.
- Added the employee Underwriting review action panel against the existing Quote & Policy decision and offer-terms APIs; shell build and configured frontend tests pass without dependency or startup-script changes.
- Completed the Vendor differentiator batch: live eligible-vendor selection, explicit recovery dispatch, fulfillment evidence capture, performance scoring, a service-local JSON evidence mapping fix, focused regression coverage, and updated Vendor service documentation.
- Completed the cross-role journey audit batch: repaired employee recovery/document loading, corrected claims staff affordances, aligned Gateway role matchers for Risk Engineering and claims-supported vendor operations, made underwriting controls role-safe, and repaired Claims/Intelligence remote build configuration.

## Immediate batch

The authenticated Docker journey is complete through quote, underwriting, policy, claim, recovery, documents, notifications, and recovery-linked Vendor dispatch/fulfillment. The UI-stability, documentation/administrator, Vendor access/reference, responsive, and deep-link role-boundary batches are verified with Java compilation, shared ui-core/shell builds, Claims/Vendor builds, and the configured frontend suite. The only remaining evidence-dependent work is the native Windows run, supported Java 17 full backend test run, and authenticated role/ownership execution using approved office accounts; the exact procedure is in `Docs/ROLE_BASED_E2E_CHECKLIST.md`.
