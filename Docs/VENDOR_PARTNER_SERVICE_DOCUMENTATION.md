# Vendor & Partner Service

## Runtime identity

- Service: `vendor-partner-service`
- Eureka name: `VENDOR-PARTNER-SERVICE`
- Port: `8085`
- Database: `vendor_partner_db` (MySQL)
- Runtime: Java 17, Spring Boot 4.1.1, WebFlux, R2DBC
- Browser entry point: Vendor MFE through the API Gateway at `http://localhost:8080`

The source controllers, security configuration, and this document describe the current contract. Native Windows startup remains governed by the existing root scripts; Docker is an additive development option.

## Business purpose

This service manages the controlled network used to restore a small business after a claim. It supports:

1. Vendor onboarding and verification.
2. Discovery of verified, active vendors by service type and area.
3. Explicit dispatch of recovery or claim work to a selected vendor.
4. Assignment acceptance, decline, progress, and completion.
5. Evidence document references on completed work.
6. Performance scoring after completion.

Vendor assignment is deliberately explicit. A recommendation or a Policyholder selecting `NETWORK_VENDOR` does not create an assignment automatically.

## Roles and security

The Gateway and service both restrict this surface to employee roles:

- `VENDOR_MANAGER`: manages the vendor network, onboarding, dispatch, and performance.
- `CLAIMS_ADJUSTER` / `CLAIMS_MANAGER`: may work with assignments needed for claims operations.
- `SYSTEM_ADMINISTRATOR` / `ADMIN`: administrative access.

Policyholders cannot access Vendor MFE routes or vendor assignment APIs. `CUSTOMER_VENDOR` and `CUSTOMER_MANAGED` recovery paths must not create VendorAssignment records. Internal calls use the propagated Bearer JWT; there is no `X-User-Id` contract.

## API contract through the Gateway

### Vendor directory

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/vendors` | Search verified and active vendors by `serviceType`, `location`, `capability`, or availability filters. |
| `GET` | `/api/vendors/recommendations` | Discover eligible candidates without creating an assignment. |
| `GET` | `/api/vendors/{vendorId}` | Read one vendor. |
| `PUT` | `/api/vendors/{vendorId}` | Update vendor profile capabilities and service areas. |
| `PATCH` | `/api/vendors/{vendorId}/status` | Activate, suspend, or deactivate a vendor. |

### Onboarding and verification

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/vendors/onboarding-requests` | Submit a vendor onboarding request. |
| `GET` | `/api/vendors/onboarding-requests` | Read onboarding requests. |
| `POST` | `/api/vendors/{vendorId}/verify` | Approve or reject a pending vendor. Approval makes the vendor `VERIFIED` and `ACTIVE`. |

### Assignment lifecycle

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/vendor-assignments` | Dispatch a selected verified/active vendor. |
| `GET` | `/api/vendor-assignments` | Filter by vendor, claim, recovery case, type, status, and date range. |
| `GET` | `/api/vendor-assignments/{assignmentId}` | Read an assignment. |
| `POST` | `/api/vendor-assignments/{assignmentId}/accept` | Accept dispatched work. |
| `POST` | `/api/vendor-assignments/{assignmentId}/decline` | Decline dispatched work with a reason. |
| `PATCH` | `/api/vendor-assignments/{assignmentId}/status` | Move accepted work to `IN_PROGRESS` or `COMPLETED`; completion may include evidence document IDs. |

`CreateVendorAssignmentRequest` supports `vendorId`, `assignmentType`, optional `claimId`, optional `recoveryCaseId`, `recoveryPath`, task description, due date, and priority. A recovery assignment must use `NETWORK_VENDOR`; customer-owned paths are rejected by the service.

The valid work states are `PENDING`, `REQUESTED`, `ASSIGNED`, `DISPATCHED`, `OFFERED`, `ACCEPTED`, `DECLINED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, and `REASSIGNED`. Invalid transitions are rejected, including direct dispatch-to-completion and changes after completion, cancellation, or decline.

### Performance

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/vendors/{vendorId}/performance` | Record quality, timeliness, communication, and outcome scores for a completed assignment. |
| `GET` | `/api/vendors/{vendorId}/performance` | Read the vendor's recorded performance history. |

The service calculates `overallScore` as the average of the four supplied scores. Performance cannot be recorded until the linked assignment is `COMPLETED`.

## Vendor MFE workflow

The remote runs on port `4205` and is mounted at `/vendor` in the shell for employee roles.

1. Open the assignment queue and filter by vendor, claim, or status.
2. Create an assignment and refresh the live directory.
3. Select a verified and active vendor returned by `/api/vendors`; the UI does not accept an arbitrary vendor record as an eligible choice.
4. Link the dispatch to a claim or recovery case and use `NETWORK_VENDOR` for network recovery work.
5. Open the assignment to accept or decline it.
6. Start work with a progress note, then complete it with a completion note and optional Document & Audit evidence IDs.
7. Record performance after completion.

Empty vendor directories and empty assignment queues are valid states and are displayed as empty states. The MFE does not contain seeded vendor records or simulated responses.

## Recovery integration

Recovery remains the central case owner. The cross-service flow is:

```text
Claim / Recovery Case
        |
        | Policyholder chooses NETWORK_VENDOR
        v
Vendor Manager discovers an eligible vendor
        |
        | explicit POST /api/vendor-assignments
        v
DISPATCHED -> ACCEPTED -> IN_PROGRESS -> COMPLETED
        |
        +--> evidence document references
        +--> vendor performance score
```

The Vendor service does not automatically dispatch because a recovery path was selected. The `CUSTOMER_VENDOR` and `CUSTOMER_MANAGED` paths remain central Recovery records without VendorAssignment rows.

## Verification

The focused Vendor service suite covers assignment transitions, verified/active-vendor enforcement, customer-owned path rejection, controller delegation, and role authorization. The Docker runtime flow has also been verified with local-only disposable data through the Gateway: onboarding, approval, directory discovery, recovery-linked dispatch, accept, progress, completion, evidence-list handling, and performance scoring.
