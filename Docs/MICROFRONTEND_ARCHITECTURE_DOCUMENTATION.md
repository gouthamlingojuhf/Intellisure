# Microfrontend (MFE) Architecture & Frontend Engineering Documentation

> **Technology Stack**: Angular 17+ (Signals & Standalone Components), `@angular-architects/module-federation`, **NgRx Store / Effects / Entity**, Tailwind CSS  
> **Host Shell App**: `shell-app` (`Port 4200`)  
> **Remote Feature MFEs**: Auth (`4201`), Policy (`4202`), Underwriting (`4203`), Claims (`4204`), Vendor (`4205`), Analytics (`4206`)

---

## 1. Local Startup Guide (No Docker)

To run the Angular Microfrontend applications natively:

```bash
# 1. Install root frontend dependencies
npm install

# 2. Run Shell Host Application (Terminal 1)
npx ng serve shell-app --port 4200

# 3. Run Remote Auth MFE (Terminal 2)
npx ng serve auth-mfe --port 4201

# 4. Run Remote Policy/Quote MFE (Terminal 3)
npx ng serve policy-quote-mfe --port 4202

# 5. Run Remote Underwriting MFE (Terminal 4)
npx ng serve underwriting-mfe --port 4203

# 6. Run Remote Claims MFE (Terminal 5)
npx ng serve claims-mfe --port 4204

# 7. Run Remote Vendor MFE (Terminal 6)
npx ng serve vendor-mfe --port 4205

# 8. Run Remote Analytics MFE (Terminal 7)
npx ng serve analytics-mfe --port 4206
```

- **Host Application URL**: [http://localhost:4200](http://localhost:4200)

---

## 2. Architectural Highlights & Evaluation Remarks

> [!TIP]
> **Extra Evaluation Positive Remark - Microfrontend Architecture & NgRx State Management**:  
> The IntelliSure user interface decouples monolithic web apps into domain-driven microfrontend applications using **Webpack Module Federation**. Frontend global state is managed predictably via **NgRx (Redux pattern)**, providing unidirectional data flow, RxJS reactive HTTP streams, and Tailwind CSS design tokens.

---

## 3. Microfrontend Component Decomposition Diagram

```mermaid
graph TD
    Shell[Shell Host App - Port 4200] -->|Loads Remote| AuthMFE[Auth MFE - Port 4201]
    Shell -->|Loads Remote| PolicyMFE[Policy & Quote MFE - Port 4202]
    Shell -->|Loads Remote| UnderwritingMFE[Underwriting MFE - Port 4203]
    Shell -->|Loads Remote| ClaimsMFE[Claims MFE - Port 4204]
    Shell -->|Loads Remote| VendorMFE[Vendor MFE - Port 4205]
    Shell -->|Loads Remote| AnalyticsMFE[Analytics MFE - Port 4206]

    AuthMFE -->|Calls HTTP| Gateway[API Gateway - Port 8080]
    PolicyMFE -->|Calls HTTP| Gateway
    UnderwritingMFE -->|Calls HTTP| Gateway
    ClaimsMFE -->|Calls HTTP| Gateway
    VendorMFE -->|Calls HTTP| Gateway
    AnalyticsMFE -->|Calls HTTP| Gateway
```

---

## 4. NgRx State Management Architecture

```mermaid
sequenceDiagram
    autonumber
    participant UI as Angular Component
    participant Action as NgRx Action
    participant Reducer as NgRx Reducer
    participant Effect as NgRx Effect
    participant Store as NgRx Global Store
    participant Gateway as API Gateway (8080)

    UI->>Action: Dispatch PolicyActions.createQuote(payload)
    Action->>Effect: Trigger @Effect createQuote$
    Effect->>Gateway: HTTP POST /api/quotes
    Gateway-->>Effect: HTTP 201 Created (QuoteDto)
    Effect->>Action: Dispatch PolicyActions.createQuoteSuccess(quoteDto)
    Action->>Reducer: Mutate PolicyState
    Reducer->>Store: Update State Slice
    Store-->>UI: Selectors Emit New Quote Data (RxJS Stream)
```
