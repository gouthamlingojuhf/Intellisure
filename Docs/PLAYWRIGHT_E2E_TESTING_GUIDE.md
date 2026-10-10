# IntelliSure Playwright End-to-End Testing Guide

This document describes the Playwright end-to-end (E2E) testing suite for the **IntelliSure** enterprise insurance platform. The suite validates every usecase, every page, every actor, and every shared UI component across the Angular 17 Module Federation workspace.

---

## Architecture & Test Organization

The test suite is located in `frontend/e2e/` and driven by `playwright.config.ts`:

```
frontend/
├── playwright.config.ts                      # Playwright root configuration
├── e2e/
│   ├── fixtures/
│   │   ├── mock-data.ts                      # Domain entities (Users, Quotes, Policies, Claims, Docs)
│   │   ├── api-mocks.ts                      # Network mocks enforcing backend R2DBC & security rules
│   │   └── test-base.ts                      # Custom actor fixtures (policyholderPage, underwriterPage, etc.)
│   ├── helpers/
│   │   └── page-helpers.ts                   # Search, theme toggle, sticky header, navigation utilities
│   └── specs/
│       ├── 01-guest-and-auth.spec.ts         # Public landing, /help, login, customer & vendor registration
│       ├── 02-role-access-guards.spec.ts     # Role matrix & Employee Business Profile guard verification
│       ├── 03-actor-policyholder.spec.ts     # Customer dashboard, profile, quote wizard, claims, docs
│       ├── 04-actor-underwriter.spec.ts      # Underwriting dashboard, assigned quotes (NO DRAFTS), review
│       ├── 05-actor-risk-engineer.spec.ts    # Risk queue, /docs access without 403, risk analytics
│       ├── 06-actor-claims-adjuster.spec.ts  # Claims workspace, assigned claims only, damage estimate, vendor
│       ├── 07-actor-claims-manager-and-recovery.spec.ts # Claims oversight, /recovery subrogation & salvage
│       ├── 08-actor-vendor.spec.ts           # Vendor applicant, /vendor work orders, assignment detail
│       ├── 09-actor-admin.spec.ts            # /admin workspace, /quotes (NO DRAFTS), live DB assignment
│       └── 10-cross-cutting-components.spec.ts # Search (⌘K/Ctrl+K), Theme toggle, 32s toast, Sticky header
```

---

## Test Suites Matrix

| Suite | Actor | Scope / Pages Tested | Key Validations & Invariants Enforced |
|---|---|---|---|
| **01** | `Guest` | `/`, `/help`, `/auth`, `/not-found` | Landing page, platform guide, unauthenticated redirects, customer registration, vendor applicant registration. |
| **02** | `All Roles` | Route Guards & Navigation | **CRITICAL**: Employees (`UNDERWRITER`, `CLAIMS_ADJUSTER`, `RISK_ENGINEER`, `ADMIN`) are **NEVER** shown "Business Profile" in navigation and are blocked by `roleGuard` from `/profile`. Only `POLICYHOLDER` has access. |
| **03** | `Policyholder` | `/dashboard`, `/profile`, `/quotes`, `/quotes/new`, `/policy`, `/claims`, `/docs` | Customer can view/edit commercial business profile; create quotes; view own quotes (including drafts); view bound documents on `/docs` without 403. |
| **04** | `Underwriter` | `/underwriting`, `/quotes`, `/quotes/:id`, `/analytics`, `/docs` | **CRITICAL**: Underwriter **NEVER** sees DRAFT quotes; only sees submitted quotes assigned to their `userId`; reviews risk breakdown; accesses `/docs` without 403. |
| **05** | `Risk Engineer`| `/underwriting`, `/docs`, `/analytics` | **CRITICAL**: Validates fix for `RISK_ENGINEER` in `document-audit-service`; zero 403 errors on `/docs`; reviews technical inspection queue. |
| **06** | `Claims Adjuster` | `/claims`, `/claims/:id`, `/vendor`, `/docs` | Views assigned claims only; adjusts damage estimates; dispatches vendor inspection; views loss evidence files. |
| **07** | `Claims Manager` | `/claims`, `/recovery`, `/analytics` | Executive claim approvals; subrogation and salvage recovery tracking; reserve analytics. |
| **08** | `Vendor` | `/vendor`, `/vendor/assignments/:id` | Vendor applicant onboarding; view assigned work orders; submit completion estimates. |
| **09** | `Admin` | `/admin`, `/quotes`, `/analytics`, `/docs` | **CRITICAL**: Admin **NEVER** sees DRAFT quotes; dynamic underwriter assignment modal loads live active underwriters from database API (`/api/users/available?role=UNDERWRITER`). |
| **10** | `Components` | Cross-Cutting Primitives | Topbar search detects OS (`⌘K` on Mac vs `Ctrl+K` on Windows); keyboard shortcut focuses search; Light/Dark theme toggle persists in `localStorage`; Toast notifications have 32s auto-dismissal; Header stays sticky upon scrolling. |

---

## Running the Tests

From the `frontend/` directory:

### 1. Run All Tests
```bash
npm run test:e2e
# or directly with Playwright CLI:
npx playwright test
```

### 2. Run a Specific Test Suite
```bash
# Run role access guard verification:
npx playwright test e2e/specs/02-role-access-guards.spec.ts

# Run admin draft filtering and live DB assignment:
npx playwright test e2e/specs/09-actor-admin.spec.ts

# Run cross-cutting components (Theme, Search, 32s Toast, Sticky Header):
npx playwright test e2e/specs/10-cross-cutting-components.spec.ts
```

### 3. Run in Interactive UI Mode
```bash
npx playwright test --ui
```

### 4. Run in Specific Browser
```bash
npx playwright test --project=chromium
npx playwright test --project=firefox
npx playwright test --project=webkit
```

### 5. Generate and View HTML Reports
```bash
npx playwright show-report
```
