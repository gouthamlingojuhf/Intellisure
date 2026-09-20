# IntelliSure Frontend Microfrontend (MFE) Architecture & Implementation Plan

> **Note**: This is a detailed engineering blueprint for building the IntelliSure Enterprise Frontend using Angular 17+, Module Federation (Microfrontend Architecture), NgRx State Management, and Tailwind CSS. As requested, this document serves as the implementation plan (no source code build requested at this stage).

---

## 1. Executive Summary & Architecture Overview

The IntelliSure Frontend is designed as a **Microfrontend (MFE)** architecture to match the underlying distributed microservices backend. By decoupling the monolithic user interface into independently deployable, domain-driven Angular feature applications, multiple engineering teams can build, test, and release features in parallel.

### Key Architectural Pillars:
- **Framework**: Angular 17+ with Signals and Standalone Components.
- **Microfrontend Framework**: `@angular-architects/module-federation` (Webpack Module Federation).
- **State Management**: **NgRx Store** + **NgRx Effects** + **NgRx Entity** (Redux Pattern for predictable, unidirectional data flow).
- **CSS Framework**: **Tailwind CSS** for responsive, utility-first styling with custom corporate insurance design tokens.
- **API Client**: Angular `HttpClient` with Reactive RxJS streams interfacing directly with the Spring WebFlux `api-gateway` (Port 8080).

---

## 2. Microfrontend Shell & Remote Module Decomposition

```
                         ┌─────────────────────────────────┐
                         │   Shell App (Host Container)    │
                         │   Port 4200 (Navigation, Auth)  │
                         └────────────────┬────────────────┘
                                          │
        ┌───────────────────┬─────────────┴───────┬───────────────────┐
        ▼                   ▼                     ▼                   ▼
┌───────────────┐   ┌───────────────┐     ┌───────────────┐   ┌───────────────┐
│   Auth MFE    │   │ Policy/Quote  │     │ Underwriting  │   │  Claims MFE   │
│   Port 4201   │   │  MFE (4202)   │     │  MFE (4203)   │   │  Port 4204    │
└───────────────┘   └───────────────┘     └───────────────┘   └───────────────┘
                            │                     │
                            ▼                     ▼
                    ┌───────────────┐     ┌───────────────┐
                    │  Vendor MFE   │     │ Analytics MFE │
                    │   Port 4205   │     │   Port 4206   │
                    └───────────────┘     └───────────────┘
```

### Module Breakdown & Responsibilities:

| MFE Module Name | Port | Remote Entry Name | Domain Responsibilities | Microservice Backend |
| :--- | :---: | :--- | :--- | :--- |
| `shell-app` | `4200` | Host Container | Global Navigation Header, Footer, Top-level Route Guards, Shared NgRx Store | `api-gateway` (8080) |
| `auth-mfe` | `4201` | `authMfe` | User Login, Self-Registration, Password Management, User Profile Management | `customer-party-service` (8081) |
| `policy-quote-mfe` | `4202` | `policyMfe` | Quote Wizard, Premium Calculator, Policy Issuance, Endorsement Forms | `quote-policy-service` (8082) |
| `underwriting-mfe` | `4203` | `underwritingMfe` | Underwriter Review Queue, Risk Score Inspection, Manual Approval/Rejection | `risk-underwriting-service` (8083) |
| `claims-mfe` | `4204` | `claimsMfe` | Claim Filing Portal, Adjuster Workbench, Loss Valuation, Payout Approvals | `claims-service` (8084) |
| `vendor-mfe` | `4205` | `vendorMfe` | Towing/Repair Partner Portal, Dispatch Tracking, Invoice Submission | `vendor-partner-service` (8085) |
| `analytics-mfe` | `4206` | `analyticsMfe` | Executive Dashboard, Loss Ratio Charts, Risk Score Distribution Graphs | `analytics-intelligence-service` (8089) |

---

## 3. Frontend State Management Strategy (NgRx Architecture)

The frontend state management follows the **NgRx (Redux)** pattern to maintain state consistency across microfrontend modules.

```
                           ┌────────────────────────┐
                           │   Component / View     │
                           └───────────┬────────────┘
                                       │ Dispatch Action
                                       ▼
┌────────────────────────┐         ┌────────────────────────┐
│      NgRx Effect       ├────────►│       NgRx Action      │
│  (API Async Call)      │         └───────────┬────────────┘
└───────────▲────────────┘                     │
            │ HTTP Response                    │ Mutates State
            │                                  ▼
┌───────────┴────────────┐         ┌────────────────────────┐
│   Spring WebFlux API   │         │      NgRx Reducer      │
│   Gateway (8080)       │         └───────────┬────────────┘
└────────────────────────┘                     │
                                               │ Updates Store
                                               ▼
                           ┌────────────────────────┐
                           │      NgRx Store        │
                           └───────────┬────────────┘
                                       │ Selectors (RxJS)
                                       ▼
                           ┌────────────────────────┐
                           │   Component / View     │
                           └────────────────────────┘
```

### Store State Slices:
1. **`AuthState`**: `currentUser`, `jwtToken`, `isAuthenticated`, `userRole` (`CUSTOMER`, `UNDERWRITER`, `ADJUSTER`, `VENDOR`, `ADMIN`), `loginError`.
2. **`PolicyState`**: `activeQuote`, `quoteList`, `issuedPolicies`, `selectedPolicy`, `loadingState`.
3. **`ClaimsState`**: `filedClaims`, `activeClaimDetails`, `adjusterQueue`, `payoutStatus`.
4. **`RiskState`**: `pendingRiskAssessments`, `underwritingRules`, `riskScoreResult`.
5. **`UIState`**: `globalSpinner`, `toastNotifications`, `activeTheme`.

---

## 4. UI Component Architecture & Tailwind CSS Design System

### Design Tokens (Tailwind Configuration):
- **Primary Brand Color**: `Deep Navy Blue` (`#1E3A8A` / `bg-blue-900`) - Represents security and trust.
- **Accent Color**: `Emerald Green` (`#059669` / `bg-emerald-600`) - Represents active policies and successful payouts.
- **Warning/High-Risk Color**: `Amber Amber` (`#D97706` / `bg-amber-600`) - Represents pending underwriting reviews.
- **Danger/Rejection Color**: `Rose Red` (`#E11D48` / `bg-rose-600`) - Represents denied claims/quotes.

### Component Taxonomy:
- **Atoms**: Tailwind Buttons (`.btn-primary`, `.btn-secondary`), Badges (`.badge-success`, `.badge-warning`), Form Inputs (`.input-field`).
- **Molecules**: Form Field Groups with validation feedback, Metric Summary Cards, Modal Confirmations.
- **Organisms**:
  - `QuoteWizardComponent`: Multi-step form with dynamic risk inputs.
  - `ClaimsTableComponent`: Paginated, sortable data table with NgRx selectors.
  - `RiskGaugeComponent`: Visual SVG gauge rendering risk score (0 - 100).
  - `AnalyticsChartComponent`: Chart.js / D3 rendering of loss ratio trends.

---

## 5. DTO Interfaces & Reactive Form Validations

### TypeScript DTO Definitions matching Backend Contracts:

```typescript
// Auth DTOs
export interface LoginRequest {
  username: string;
  passwordHash: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  email: string;
  role: string;
}

// Quote DTOs
export interface CreateQuoteRequest {
  customerUuid: string;
  policyType: 'AUTO' | 'HOME' | 'COMMERCIAL' | 'HEALTH';
  coverageAmount: number;
  deductible: number;
  propertyOrVehicleDetails: string;
}

export interface QuoteResponse {
  quoteNumber: string;
  customerUuid: string;
  calculatedPremium: number;
  riskScore: number;
  status: 'DRAFT' | 'CALCULATED' | 'APPROVED' | 'REJECTED' | 'BOUND';
  createdAt: string;
}

// Claim DTOs
export interface FileClaimRequest {
  policyNumber: string;
  incidentDate: string;
  incidentDescription: string;
  claimedAmount: number;
}

export interface ClaimResponse {
  claimNumber: string;
  policyNumber: string;
  claimedAmount: number;
  approvedAmount?: number;
  status: 'FILED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'SETTLED';
  filedAt: string;
}
```

### Form Validation Rules (Angular Reactive Forms):
- **Email**: `Validators.required`, `Validators.email`
- **Password**: `Validators.required`, `Validators.minLength(8)` (Upper, lower, digit, special character regex)
- **SSN / Tax ID**: `Validators.pattern(/^\d{3}-\d{2}-\d{4}$/)`
- **Coverage Amount**: `Validators.required`, `Validators.min(1000)`, `Validators.max(10000000)`
- **VIN (Vehicle Identification Number)**: `Validators.pattern(/^[A-HJ-NPR-Z0-9]{17}$/)`

---

## 6. API Gateway Interceptors & WebFlux Security Integration

### 1. `JwtInterceptor`:
Injects `Authorization: Bearer <jwtToken>` from the NgRx `AuthState` into every outgoing HTTP request to `http://localhost:8080/api/*`.

### 2. `ErrorInterceptor`:
Intercepts HTTP error responses from Spring WebFlux API Gateway:
- **`401 Unauthorized`**: Dispatches `AuthActions.logout()` and redirects to `/auth/login`.
- **`403 Forbidden`**: Shows "Access Denied: Insufficient Role Privileges" toast notification.
- **`404 Not Found`**: Navigates to `/not-found` page.
- **`400 Bad Request`**: Maps backend `ApiError` validation messages to form controls.
- **`500 Internal Error`**: Displays global toast error alert.

---

## 7. Development & Startup Guide (Individual Microfrontend Serves)

To run the Microfrontend applications individually without Docker:

```bash
# 1. Install dependencies in Shell and Remote MFEs
npm install

# 2. Run Shell App (Terminal 1)
npx ng serve shell-app --port 4200

# 3. Run Remote Auth MFE (Terminal 2)
npx ng serve auth-mfe --port 4201

# 4. Run Remote Policy/Quote MFE (Terminal 3)
npx ng serve policy-quote-mfe --port 4202

# 5. Run Remote Underwriting MFE (Terminal 4)
npx ng serve underwriting-mfe --port 4203

# 6. Run Remote Claims MFE (Terminal 5)
npx ng serve claims-mfe --port 4204
```

Access the host shell at: `http://localhost:4200`
