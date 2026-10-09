# IntelliSure role-based E2E verification

This checklist is for the native Windows handover environment. It validates the running system; it does not seed or commit business records.

## 1. Native startup

Run from the repository root in PowerShell:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\build_all.ps1
.\run_all_services.ps1
```

Confirm:

- Eureka shows all expected services as registered.
- Gateway responds at `http://localhost:8080`.
- `http://localhost:8080/actuator/health` returns a healthy response.
- The shell and configured remotes load on their existing ports.
- `git diff -- all_start.ps1 run_all_services.ps1 build_all.ps1` is empty.

If frontend dependencies are absent, use the existing lockfile only:

```powershell
cd frontend
npm ci
```

Do not run `npm update`, `npm audit fix`, or change package files.

## 2. Authenticated role matrix

Use approved test accounts. Record the HTTP result and the visible route for every negative check.

| Role | Must be able to use | Must not be able to use |
| --- | --- | --- |
| Policyholder | Dashboard, Business Profile, quote create/submit/accept, own policies, FNOL, own recovery, documents, notifications | Underwriting decisions, employee administration, another customer’s records, Vendor assignment operations |
| Vendor Applicant | Vendor onboarding submission and status | Assignment list/create/detail, underwriting, policyholder dashboard/profile, employee administration |
| Vendor Manager | Onboarding review, verified vendor directory, assignment dispatch/fulfillment, performance | Policyholder dashboard/profile, underwriting decisions, customer-owned records outside authorized operations |
| Underwriter | Assigned underwriting queue, quote review, decisions, offered terms, permitted policy read | Policyholder quote creation, FNOL creation, employee administration, Vendor fulfillment actions outside role |
| Risk Engineer | Assigned/read-only risk assessment context | Underwriting decisions or offered commercial terms, policyholder quote creation, FNOL creation |
| Claims Adjuster | Operational claims, recovery, supported Vendor dispatch, claim documents | Underwriting decisions, employee administration, Policyholder business profile |
| Claims Manager | Claims oversight, recovery, Vendor operations, permitted analytics | Policyholder business profile, employee administration unless separately assigned |
| System Administrator/Admin | Administration, account search/create/status/role management, enterprise summaries | Underwriting, binding, claim decisions, and Vendor fulfillment unless the account also has that role |

## 3. Ownership and identifier checks

Use two policyholder accounts and verify that account A receives `403` or an empty scoped result when attempting account B’s quote, policy, claim, recovery, document, and notification resource. Verify the same request succeeds for the owning account.

The visible UI must use quote numbers, policy numbers, claim numbers, display names, and statuses. Internal UUIDs may remain in route/API payloads but must not be rendered as labels, table cells, placeholders, raw payloads, or customer-facing error text.

## 4. Policyholder lifecycle acceptance

1. Register a Business Policyholder and complete Business Profile.
2. Re-authenticate so the customer claim is present in the JWT.
3. Create a quote with a standard coverage and confirm the coverage name is catalogue-derived.
4. Create a Custom coverage and confirm the custom-name field appears only for that row.
5. Submit the quote and verify the underwriting status changes through the live API.
6. Complete the authorized underwriting decision/terms flow with an approved employee account.
7. Accept the quote, bind/issue through the authorized employee flow, and verify the policy number.
8. File FNOL by selecting a policy number, not typing an internal identifier.
9. Select `CUSTOMER_VENDOR` or `CUSTOMER_MANAGED` and confirm no VendorAssignment is created.
10. Select `NETWORK_VENDOR` only with an explicitly selected verified vendor, then verify assignment progress and completion.
11. Verify documents, notifications, mark-read behavior, fixed header, search, and the Ctrl+K shortcut.

## 5. Evidence to retain

- Native build output and service startup output.
- Eureka registration screenshot or exported status.
- One successful lifecycle trace per role.
- Negative `401/403` ownership and role checks.
- Final `git status --short` showing a clean worktree.
